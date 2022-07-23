package flaxbeard.thaumicexploration.misc.brazier;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class SoulBrazierQueue {

    @SubscribeEvent
    public void onConnected(PlayerLoggedInEvent event) {
        EntityPlayer aJoiningPlayer = event.player;
        UUID aPlayerUUID = SoulBrazierUtils.getPlayerUUID(aJoiningPlayer);
        int queueSize = SoulBrazierUtils.doesPlayerHaveWarpQueued(aPlayerUUID);
        if (queueSize > 0) {
            String aPlayerName = aJoiningPlayer.getGameProfile().getName();
            for (int i = 0; i < queueSize; i++) {
                SoulBrazierQueueData aQueueData = SoulBrazierUtils.getPlayerDataFromWarpQueue(aPlayerUUID);
                int aCurrentWarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(aPlayerName);
                int aTotalWarp = aCurrentWarp + aQueueData.aQueuedWarpToAdd;
                Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(aPlayerName, aTotalWarp);
                if (SoulBrazierUtils.removePlayerDataFromWarpQueue(
                        aPlayerUUID, aQueueData.aTileX, aQueueData.aTileY, aQueueData.aTileZ, aQueueData.aDimension)) {
                    updateSoulBrazier(
                            getWorldFromID(aQueueData.aDimension),
                            aQueueData.aTileX,
                            aQueueData.aTileY,
                            aQueueData.aTileZ);
                }
            }
        }
    }

    private static World getWorldFromID(int aID) {
        return MinecraftServer.getServer().worldServerForDimension(aID);
    }

    private static boolean updateSoulBrazier(World aWorld, int aX, int aY, int aZ) {
        TileEntity aTile = aWorld.getTileEntity(aX, aY, aZ);
        if (aTile instanceof TileEntitySoulBrazier) {
            TileEntitySoulBrazier aBrazier = (TileEntitySoulBrazier) aTile;
            aBrazier.hasWarpQueue = false;
            return true;
        }
        return false;
    }
}
