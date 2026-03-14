package flaxbeard.thaumicexploration.misc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

import thaumcraft.common.Thaumcraft;

public class TXUtils {

    public static EntityPlayerMP getPlayerByUsername(String username) {
        MinecraftServer server = MinecraftServer.getServer();
        // func_152612_a is getPlayerByUsername
        return server.getConfigurationManager().func_152612_a(username);
    }

    public static void addWarpPermOfflinePlayer(String owner, int addedWarp) {

        Path playerDataDir = DimensionManager.getCurrentSaveRootDirectory().toPath().resolve("playerdata");
        Path playerDataPath = playerDataDir.resolve(owner + ".thaum");

        if (!Files.exists(playerDataPath)) {
            return;
        }

        NBTTagCompound playerData;
        try (FileInputStream fis = new FileInputStream(playerDataPath.toFile())) {
            playerData = CompressedStreamTools.readCompressed(fis);
        } catch (IOException e) {
            e.printStackTrace();
            Thaumcraft.log.fatal("Error reading {}'s Thaumcraft data at {}.", owner, playerDataPath.toString());
            return;
        }

        int currentWarp = playerData.getInteger("Thaumcraft.eldritch");
        playerData.setInteger("Thaumcraft.eldritch", currentWarp + addedWarp);

        Path tempPath = playerDataDir.resolve(owner + "_temp.thaum");
        try (FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
            CompressedStreamTools.writeCompressed(playerData, fos);
            Files.move(tempPath, playerDataPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            Thaumcraft.log.fatal("Error writing {}'s Thaumcraft data at {}", owner, tempPath.toString());
        }

        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException e) {
            e.printStackTrace();
            Thaumcraft.log.fatal("Error deleting {}'s temporary Thaumcraft data at {}", owner, tempPath.toString());
        }

        Thaumcraft.log.info("Returned {} warp to {} from their Soul Brazier", addedWarp, owner);
    }
}
