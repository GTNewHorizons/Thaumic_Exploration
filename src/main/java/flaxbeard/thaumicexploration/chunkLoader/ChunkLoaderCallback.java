package flaxbeard.thaumicexploration.chunkLoader;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

/**
 * Created by nekosune on 04/09/14.
 */
public class ChunkLoaderCallback implements ForgeChunkManager.OrderedLoadingCallback {

    @Override
    public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world,
            int maxTicketCount) {
        List<ForgeChunkManager.Ticket> warpChunks = new ArrayList<ForgeChunkManager.Ticket>();
        List<ForgeChunkManager.Ticket> restChunks = new ArrayList<ForgeChunkManager.Ticket>();

        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagCompound cmp = ticket.getModData();
            if (cmp != null && cmp.hasKey("warpChunk") && cmp.getBoolean("warpChunk")) warpChunks.add(ticket);
            else restChunks.add(ticket);
        }
        ArrayList<ForgeChunkManager.Ticket> res = new ArrayList<ForgeChunkManager.Ticket>(warpChunks);
        res.addAll(restChunks);
        return res;
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            int xPos = ticket.getModData().getInteger("xCoord");
            int yPos = ticket.getModData().getInteger("yCoord");
            int zPos = ticket.getModData().getInteger("zCoord");
            if (world.getTileEntity(xPos, yPos, zPos) != null) {
                if (world.getTileEntity(xPos, yPos, zPos) instanceof ITXChunkLoader) {
                    ((ITXChunkLoader) world.getTileEntity(xPos, yPos, zPos)).forceChunkLoading(ticket);
                    continue;
                }
            }
            ForgeChunkManager.releaseTicket(ticket);
        }
    }
}
