package flaxbeard.thaumicexploration.interop;

import flaxbeard.thaumicexploration.tile.TileEntityBoundJar;
import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/**
 * Created by Katrina on 17/06/2015.
 */
public class BoundJarProvider implements IWailaDataProvider {
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
        if (!(iWailaDataAccessor.getTileEntity() instanceof TileEntityBoundJar)) return list;

        TileEntityBoundJar jar = (TileEntityBoundJar) iWailaDataAccessor.getTileEntity();
        String network = jar.networkName;
        String colour =
                StatCollector.translateToLocal("item.dyePowder." + ItemDye.field_150921_b[jar.colour] + ".name");
        list.add(StatCollector.translateToLocalFormatted("txwaila.boundJar.network", network));
        list.add(StatCollector.translateToLocalFormatted("txwaila.boundJar.colour", colour));
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
