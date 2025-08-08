package flaxbeard.thaumicexploration.integration;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.emoniph.witchery.blocks.BlockKettle;
import com.emoniph.witchery.blocks.BlockKettle.TileEntityKettle;
import com.emoniph.witchery.brewing.BlockCauldron;
import com.emoniph.witchery.brewing.TileEntityCauldron;

public class WitcheryIntegration {

    public static boolean isCauldron(Block block) {
        return block.getClass().equals(BlockCauldron.class);
    }

    public static boolean needsWaterCauldron(TileEntity tile) {
        TileEntityCauldron cauldron = (TileEntityCauldron) tile;
        return cauldron.getLiquidQuantity() < cauldron.getMaxLiquidQuantity();
    }

    public static void fillWaterCauldron(TileEntity tile, World world) {
        TileEntityCauldron cauldron = (TileEntityCauldron) tile;
        BlockCauldron cauldronBlock = (BlockCauldron) cauldron.getBlockType();
        cauldronBlock.tryFillWith(
                world,
                cauldron.xCoord,
                cauldron.yCoord,
                cauldron.zCoord,
                new FluidStack(FluidRegistry.WATER, 1000));
        cauldron.getWorldObj()
                .func_147453_f(cauldron.xCoord, cauldron.yCoord, cauldron.zCoord, cauldron.getBlockType());
    }

    public static boolean isKettle(Block block) {
        return block.getClass().equals(BlockKettle.class);
    }

    public static boolean needsWaterKettle(TileEntity tile) {
        TileEntityKettle kettle = (TileEntityKettle) tile;
        return !kettle.isFilled();
    }

    public static void fillWaterKettle(TileEntity tile, World world) {
        TileEntityKettle kettle = (TileEntityKettle) tile;
        BlockKettle kettleBlock = (BlockKettle) kettle.getBlockType();
        kettleBlock.tryFillWith(
                world,
                kettle.xCoord,
                kettle.yCoord,
                kettle.zCoord,
                new FluidStack(FluidRegistry.WATER, 1000));
        kettle.getWorldObj().func_147453_f(kettle.xCoord, kettle.yCoord, kettle.zCoord, kettle.getBlockType());
    }
}
