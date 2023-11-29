package flaxbeard.thaumicexploration.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import flaxbeard.thaumicexploration.common.StringID;
import flaxbeard.thaumicexploration.data.BoundJarNetworkManager;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.TileJarFillable;

public class TileEntityBoundJar extends TileJarFillable {

    public int accessTicks = 0;

    public int clientColor = 0;
    public int colour;
    public AspectList aspectList = new AspectList();
    public String networkName = StringID.getName();

    public int getMinimumSuction() {
        return 40;
    }

    public Aspect getSuctionType(ForgeDirection loc) {
        return this.aspect;
    }

    public int getSuctionAmount(ForgeDirection loc) {
        if (this.amount < this.maxAmount) {
            return 40;
        }
        return 0;
    }

    public Aspect getEssentiaType(ForgeDirection loc) {
        return this.aspect;
    }

    public int getEssentiaAmount(ForgeDirection loc) {
        return this.amount;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound access = new NBTTagCompound();
        access.setInteger("accessTicks", this.accessTicks);
        access.setInteger("amount", this.amount);
        if (this.aspect != null) {
            access.setString("aspect", this.aspect.getTag());
        }
        access.setInteger("color", this.getSealColor());
        access.setString("network", this.networkName);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, access);
    }

    public int getAccessTicks() {
        return this.accessTicks;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        aspectList.remove(aspect);

        aspectList.add(aspect, amount);
        AspectList oldAspects = BoundJarNetworkManager.getAspect(networkName);
        oldAspects.remove(oldAspects.getAspects()[0]);
        oldAspects.add(aspect, amount);
        if (!worldObj.isRemote) {
            BoundJarNetworkManager.markDirty(networkName);
        }
    }

    public void setColor(int color) {
        colour = color;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound access = pkt.func_148857_g();
        this.accessTicks = access.getInteger("accessTicks");
        this.amount = access.getInteger("amount");
        if (access.getString(("aspect")) != null) {
            this.aspect = Aspect.getAspect(access.getString("aspect"));
        }
        this.setColor(access.getInteger("color"));
        this.clientColor = access.getInteger("color");
        this.networkName = access.getString("network");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspect = Aspect.getAspect(nbttagcompound.getString("Aspect"));
        this.amount = nbttagcompound.getShort("Amount");
        this.facing = nbttagcompound.getByte("facing");
        this.networkName = nbttagcompound.getString("network");
        this.colour = nbttagcompound.getInteger("colour");
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        if (this.aspect != null) {
            nbttagcompound.setString("Aspect", this.aspect.getTag());
        }
        nbttagcompound.setShort("Amount", (short) this.amount);
        nbttagcompound.setByte("facing", (byte) this.facing);
        nbttagcompound.setString("network", this.networkName);
        nbttagcompound.setInteger("colour", getSealColor());
    }

    public int getSealColor() {
        return colour;
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        this.updateEntity();

        if (am == 0) {
            return am;
        }
        if (((this.amount < this.maxAmount) && (tt == this.aspect)) || (this.amount == 0)) {
            this.aspect = tt;
            int added = Math.min(am, this.maxAmount - this.amount);
            this.amount += added;
            am -= added;
        }
        this.accessTicks = 80;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        this.updateEntity();
        if ((this.amount >= am) && (tt == this.aspect)) {
            this.amount -= am;
            if (this.amount <= 0) {
                this.aspect = null;
                this.amount = 0;
            }
            this.accessTicks = 80;
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            return true;
        }
        return false;
    }

    public void updateEntity() {

        aspectList = BoundJarNetworkManager.getAspect(networkName).copy();

        if (!this.worldObj.isRemote) {
            if (this.accessTicks > 0) {
                --this.accessTicks;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

            if (aspect != aspectList.getAspects()[0]) {
                this.accessTicks = 80;
                aspect = aspectList.getAspects()[0];
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

            if (amount != aspectList.getAmount(aspect)) {
                this.accessTicks = 80;
                amount = aspectList.getAmount(aspect);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }

        super.updateEntity();
    }

    @Override
    public void setAspects(AspectList aspects) {
        super.setAspects(aspects);
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }
}
