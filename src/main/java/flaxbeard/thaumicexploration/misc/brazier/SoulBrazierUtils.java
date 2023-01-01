package flaxbeard.thaumicexploration.misc.brazier;

import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketSyncWarp;

public class SoulBrazierUtils {

    public static final String PlayerWarpDataFileName = "PlayerWarpQueueData.dat";
    public static final File PlayerWarpDataFile;

    static {
        PlayerWarpDataFile = getPlayerWarpDataFile();
    }

    public static UUID getPlayerUUID(EntityPlayer aPlayer) {
        return aPlayer.getGameProfile().getId();
    }

    public static EntityPlayer getPlayerFromUUID(String aUUID) {
        return getPlayerFromUUID(UUID.fromString(aUUID));
    }

    public static EntityPlayer getPlayerFromUUID(UUID aUUID) {
        List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : allPlayers) {
            if (player.getGameProfile().getId().equals(aUUID)) {
                return player;
            }
        }
        return null;
    }

    public static boolean isPlayerOnline(UUID aUUID) {
        return getPlayerFromUUID(aUUID) != null;
    }

    public static boolean isPlayerInRangeOfBrazier(EntityPlayer aPlayer, TileEntitySoulBrazier aTile) {
        double aDistance = aTile.getDistanceFrom(aPlayer.posX, aPlayer.posY, aPlayer.posZ);
        return aDistance <= 50;
    }

    public static void syncPermWarp(EntityPlayerMP player) {
        PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte) 0), player);
    }

    /*
     * Player Data Handling - Warp Queue
     */

    public static int doesPlayerHaveWarpQueued(UUID aPlayer) {
        if (aPlayer == null) {
            return 0;
        }
        ArrayList<String> aWarpData = readPlayerWarpQueueDataFromFile();
        int aQueueCount = 0;
        for (String warpData : aWarpData) {
            if (warpData != null && warpData.length() > 0 && warpData.contains(aPlayer.toString())) {
                aQueueCount++;
            }
        }
        return aQueueCount;
    }

    public static void writePlayerWarpToQueueFile(UUID aPlayer, TileEntitySoulBrazier aTile) {
        if (aPlayer == null || aTile.storedWarp < 0) {
            return;
        }
        String aUUID = aPlayer.toString();
        String warpKey = aUUID + "@" + aTile.storedWarp + "@" + aTile.xCoord + "@" + aTile.yCoord + "@" + aTile.zCoord
                + "" + aTile.getWorldObj().provider.dimensionId;
        ArrayList<String> aNewDataList = new ArrayList<String>();
        ArrayList<String> aWarpData = readPlayerWarpQueueDataFromFile();
        if (aWarpData.contains(warpKey)) {
            for (String warpData : aWarpData) {
                if (warpData != null && warpData.length() > 0) {
                    if (warpData.contains(aUUID)) {
                        aNewDataList.add(warpKey);
                    } else {
                        aNewDataList.add(warpData);
                    }
                }
            }
        } else {
            aNewDataList.addAll(aWarpData);
            aNewDataList.add(warpKey);
        }
        if (!aNewDataList.isEmpty()) {
            writePlayerWarpQueueDataToFile(aNewDataList);
        }
    }

    public static SoulBrazierQueueData getPlayerDataFromWarpQueue(UUID aPlayer) {
        if (aPlayer == null) {
            return null;
        }
        ArrayList<String> aWarpData = readPlayerWarpQueueDataFromFile();
        for (String warpData : aWarpData) {
            if (warpData != null && warpData.length() > 0 && warpData.contains(aPlayer.toString())) {
                String[] playerData = warpData.split("@");
                if (playerData.length == 6) {
                    int aQueuedWarp = Integer.parseInt(playerData[1]);
                    int aX = Integer.parseInt(playerData[2]);
                    int aY = Integer.parseInt(playerData[3]);
                    int aZ = Integer.parseInt(playerData[4]);
                    int aDimID = Integer.parseInt(playerData[5]);
                    return new SoulBrazierQueueData(aPlayer, aQueuedWarp, aX, aY, aZ, aDimID);
                }
            }
        }
        return null;
    }

    public static boolean removePlayerDataFromWarpQueue(UUID aPlayer, int aX, int aY, int aZ, int aDimID) {
        if (aPlayer == null) {
            return false;
        }
        ArrayList<String> aNewDataList = new ArrayList<String>();
        ArrayList<String> aWarpData = readPlayerWarpQueueDataFromFile();
        for (String warpData : aWarpData) {
            if (warpData != null && warpData.length() > 0) {
                if (!warpData.contains(aPlayer.toString())) {
                    aNewDataList.add(warpData);
                } else {
                    if (!warpData.contains(aX + "@" + aY + "@" + aZ + "@" + aDimID)) {
                        aNewDataList.add(warpData);
                    }
                }
            }
        }
        if (!aNewDataList.isEmpty()) {
            writePlayerWarpQueueDataToFile(aNewDataList);
        }
        return true;
    }

    private static void writePlayerWarpQueueDataToFile(ArrayList<String> aData) {
        File warpFile = getPlayerWarpDataFile();
        sortWarpDataList(aData);
        BufferedWriter writer;
        try {
            if (!warpFile.exists()) {
                warpFile.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(warpFile.getPath()));
            for (String aPlayerData : aData) {
                writer.write(aPlayerData);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> readPlayerWarpQueueDataFromFile() {
        File warpFile = getPlayerWarpDataFile();
        BufferedReader reader;
        ArrayList<String> aData = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(warpFile.getPath()));
            String line;
            while ((line = reader.readLine()) != null && line.length() > 0) {
                aData.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aData;
    }

    private static void sortWarpDataList(ArrayList<String> aData) {
        Collections.sort(aData);
    }

    private static File getSaveDirectory() {
        return new File(DimensionManager.getCurrentSaveRootDirectory(), ThaumicExploration.MODID);
    }

    private static File getPlayerWarpDataFile() {
        if (PlayerWarpDataFile != null && PlayerWarpDataFile.exists()) {
            return PlayerWarpDataFile;
        }
        String dir = getSaveDirectory().getPath();
        File warpFile = new File(dir + PlayerWarpDataFileName);
        if (!warpFile.exists()) {
            try {
                warpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return warpFile;
    }
}
