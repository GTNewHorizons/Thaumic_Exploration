package flaxbeard.thaumicexploration.misc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

import thaumcraft.common.Thaumcraft;

public class SaveUtils {

    public static EntityPlayerMP getPlayerByUsername(String username) {
        MinecraftServer server = MinecraftServer.getServer();
        // func_152612_a is getPlayerByUsername
        return server.getConfigurationManager().func_152612_a(username);
    }

    public static EntityPlayerMP getPlayerByUUID(UUID uuid) {
        List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : allPlayers) {
            if (uuid.equals(player.getGameProfile().getId())) {
                return player;
            }
        }
        return null;
    }

    public static boolean addWarpPermOfflinePlayer(String owner, int addedWarp) {

        Path playerDataDir = DimensionManager.getCurrentSaveRootDirectory().toPath().resolve("playerdata");
        Path playerDataPath = playerDataDir.resolve(owner + ".thaum");
        Path backupDataPath = playerDataDir.resolve(owner + ".thaumback");

        if (!Files.exists(playerDataPath)) {
            Thaumcraft.log.error("Error reading {}'s Thaumcraft data at {}.", owner, playerDataPath);
            return false;
        }

        NBTTagCompound playerData;
        try (FileInputStream fis = new FileInputStream(playerDataPath.toFile())) {
            playerData = CompressedStreamTools.readCompressed(fis);
        } catch (IOException e) {
            Thaumcraft.log.error("Error reading {}'s Thaumcraft data at {}.", owner, playerDataPath, e);
            return false;
        }

        int currentWarp = playerData.getInteger("Thaumcraft.eldritch");
        playerData.setInteger("Thaumcraft.eldritch", currentWarp + addedWarp);

        Path tempPath = playerDataDir.resolve(owner + "_temp.thaum");
        try (FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
            CompressedStreamTools.writeCompressed(playerData, fos);
        } catch (IOException | SecurityException e) {
            Thaumcraft.log.error("Error writing {}'s Thaumcraft data at {}.", owner, tempPath, e);
            return false;
        }

        try {
            Files.copy(tempPath, backupDataPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempPath, playerDataPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | SecurityException e) {
            Thaumcraft.log.error("Error moving {}'s Thaumcraft data at {}.", owner, tempPath, e);
            return false;
        }

        return true;
    }
}
