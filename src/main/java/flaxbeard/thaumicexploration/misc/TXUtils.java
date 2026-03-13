package flaxbeard.thaumicexploration.misc;

import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

public class TXUtils {

    public static boolean isPlayerOnline(String username) {
        MinecraftServer server = MinecraftServer.getServer();
        // func_152612_a is getPlayerByUsername
        return server.getConfigurationManager().func_152612_a(username) != null;
    }

    public static void addWarpPermOfflinePlayer(String owner, int addedWarp) {

        File playerDataDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "playerdata");
        File playerDataFile = new File(playerDataDir, owner + ".thaum");

        NBTTagCompound playerData = new NBTTagCompound();
        if (!playerDataFile.exists()) {
            try {
                playerData = CompressedStreamTools.read(playerDataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int currentWarp = playerData.getInteger("Thaumcraft.eldritch");
        playerData.setInteger("Thaumcraft.eldritch", currentWarp + addedWarp);

        try {
            File tempFile = new File(playerDataDir, owner + ".thaum");
            CompressedStreamTools.write(playerData, tempFile);
            if (playerDataFile.exists()) playerDataFile.delete();
            tempFile.renameTo(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
