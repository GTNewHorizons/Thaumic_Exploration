package flaxbeard.thaumicexploration.interop;

import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/**
 * Created by Katrina on 25/06/2015.
 */
public class SoulBrazierProvider implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
        return null;
    }

    @Override
    public List<String> getWailaHead(
            ItemStack itemStack,
            List<String> list,
            IWailaDataAccessor iWailaDataAccessor,
            IWailaConfigHandler iWailaConfigHandler) {
        return list;
    }

    @Override
    public List<String> getWailaBody(
            ItemStack itemStack,
            List<String> list,
            IWailaDataAccessor iWailaDataAccessor,
            IWailaConfigHandler iWailaConfigHandler) {

        if (!(iWailaDataAccessor.getTileEntity() instanceof TileEntitySoulBrazier)) return list;
        TileEntitySoulBrazier brazier = (TileEntitySoulBrazier) iWailaDataAccessor.getTileEntity();

        list.add(StatCollector.translateToLocalFormatted("ttwaila.soulBrazier.essentia", brazier.currentEssentia));
        list.add(StatCollector.translateToLocalFormatted("ttwaila.soulBrazier.vis", brazier.currentVis));

        if (brazier.active) {
            list.add(StatCollector.translateToLocal("ttwaila.soulBrazier.active"));
            list.add(StatCollector.translateToLocalFormatted("ttwaila.soulBrazier.warp", brazier.storedWarp));
        } else list.add(StatCollector.translateToLocal("ttwaila.soulBrazier.notActive"));

        list.add(StatCollector.translateToLocalFormatted("ttwaila.soulBrazier.owner", brazier.owner.getName()));

        return list;
    }

    @Override
    public List<String> getWailaTail(
            ItemStack itemStack,
            List<String> list,
            IWailaDataAccessor iWailaDataAccessor,
            IWailaConfigHandler iWailaConfigHandler) {
        return list;
    }

    @Override
    public NBTTagCompound getNBTData(
            EntityPlayerMP entityPlayerMP,
            TileEntity tileEntity,
            NBTTagCompound nbtTagCompound,
            World world,
            int i,
            int i1,
            int i2) {
        return nbtTagCompound;
    }
}
