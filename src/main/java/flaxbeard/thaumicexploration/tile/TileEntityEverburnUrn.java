package flaxbeard.thaumicexploration.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;

public class TileEntityEverburnUrn extends TileEntity implements IFluidTank, IFluidHandler {

    private int ticks = 0;
    public float ignisVis;
    public static int CONVERSION_FACTOR = 250;

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        ignisVis = nbttagcompound.getFloat("ignisVis");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setFloat("ignisVis", ignisVis);
    }

    @Override
    public FluidStack getFluid() {
        return new FluidStack(FluidRegistry.LAVA, getFluidAmount());
    }

    @Override
    public int getFluidAmount() {
        return (int) Math.floor(ignisVis * CONVERSION_FACTOR);
    }

    @Override
    public int getCapacity() {
        return 4 * CONVERSION_FACTOR;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        float drained = Math.min(maxDrain, getFluidAmount());
        if (doDrain) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            ignisVis = ignisVis - (drained / 250);
        }
        return new FluidStack(FluidRegistry.LAVA, (int) drained);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (!resource.isFluidEqual(new FluidStack(FluidRegistry.LAVA, 1)) || !(from == ForgeDirection.UP)) {
            return null;
        }
        return this.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (from == ForgeDirection.UP) {
            return this.drain(maxDrain, doDrain);
        } else {
            return new FluidStack(FluidRegistry.LAVA, 0);
        }
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return from == ForgeDirection.UP;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { this.getInfo() };
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        this.ticks++;
        if (this.ticks == 10) {
            if (this.ignisVis < 16) {
                ignisVis += VisNetHandler
                        .drainVis(this.worldObj, this.xCoord, this.yCoord, this.zCoord, Aspect.FIRE, 1);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
            ticks = 0;
        }
    }
}
