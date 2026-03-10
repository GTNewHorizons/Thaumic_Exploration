package flaxbeard.thaumicexploration.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.mojang.authlib.GameProfile;

import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.chunkLoader.ITXChunkLoader;
import flaxbeard.thaumicexploration.common.ConfigTX;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockTaintFibres;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.tiles.TileVisRelay;

/**
 * Created by nekosune on 03/08/14.
 */
public class TileEntitySoulBrazier extends TileVisRelay implements IEssentiaTransport, ITXChunkLoader {

    public int storedWarp;
    public int currentEssentia;
    public int currentVis;
    public boolean active;
    public GameProfile owner;
    public int tick;
    private static final int VisCapacity = 16;
    private static final int EssentiaCapacity = 16;
    public static int EssentiaRate = 1;
    public static int VisRate = 3;
    public ForgeChunkManager.Ticket heldChunk;

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        storedWarp = nbttagcompound.getInteger("storedWarp");
        currentEssentia = nbttagcompound.getInteger("currentEssentia");
        currentVis = nbttagcompound.getInteger("currentVis");
        active = nbttagcompound.getBoolean("active");
        owner = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("owner"));
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("storedWarp", storedWarp);
        nbttagcompound.setInteger("currentEssentia", currentEssentia);
        nbttagcompound.setInteger("currentVis", currentVis);
        nbttagcompound.setBoolean("active", active);
        NBTTagCompound gameProfile = new NBTTagCompound();
        NBTUtil.func_152460_a(gameProfile, owner);
        nbttagcompound.setTag("owner", gameProfile);
    }

    public boolean setActive(EntityPlayer player) {
        if (worldObj.isRemote) {
            return true;
        }
        if (!EntityPlayer.func_146094_a(player.getGameProfile()).equals(owner.getId())) {
            player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.invalidplayer"));
            return false;
        }
        if (!hasPower()) {
            player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.nopower"));
            return false;
        }
        if (this.getDistanceFrom(player.posX, player.posY, player.posZ) > 50) {
            player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.norange"));
            return false;
        }
        int playerWarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(owner.getName());
        if (playerWarp <= 0) {
            player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.noWarp"));
            return false;
        }

        active = true;
        storedWarp += playerWarp;
        Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(owner.getName(), 0);
        // TODO send a message about the weight of madness lifting to the brazier
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            return;
        }
        super.updateEntity();
        if (this.tick == 360) {
            this.tick = 0;
        }
        this.tick += 1;

        if (this.tick % 5 == 0) {
            if (this.currentVis < VisCapacity) {
                getVis();
            }
            if (this.currentEssentia < EssentiaCapacity) {
                getEssentia();
            }
        }

        if (active) {
            if (heldChunk == null && ConfigTX.allowSBChunkLoading) addTicket();

            if (this.tick % 10 == 0 && ThaumicExploration.proxy.getIsReadyForWisp()) {
                ThaumicExploration.proxy.spawnActiveBrazierParticle(worldObj, xCoord, yCoord, zCoord);
            }
            if (this.tick % 45 == 0) changeTaint();
            if (this.tick % 60 == 0) spendPower();

            if (!hasPower()) {
                active = false;
                int currentWarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(owner.getName());
                int totalWarp = currentWarp + storedWarp;
                if (currentWarp < totalWarp) {
                    // TODO send a message: The Familiar weight of old madness
                    Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(owner.getName(), totalWarp);
                }
                storedWarp = 0;
                ForgeChunkManager
                        .unforceChunk(this.heldChunk, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));
                this.heldChunk = null;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    private void getVis() {
        currentVis += this.consumeVis(Aspect.FIRE, 1);
    }

    public void getEssentia() {
        TileEntity connectable = ThaumcraftApiHelper
                .getConnectableTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.DOWN);
        if (connectable == null) {
            return;
        }
        IEssentiaTransport ic = (IEssentiaTransport) connectable;
        Aspect aspect = Aspect.DEATH;
        if (ic.canOutputTo(ForgeDirection.UP)
                && ic.getSuctionAmount(ForgeDirection.UP) < getSuctionAmount(ForgeDirection.DOWN)) {
            addEssentia(aspect, ic.takeEssentia(aspect, 1, ForgeDirection.UP), ForgeDirection.DOWN);
        }
    }

    public boolean hasPower() {
        return currentEssentia > 0 && currentVis > 0;
    }

    private void spendPower() {
        currentEssentia = Math.max(0, currentEssentia - EssentiaRate);
        currentVis = Math.max(0, currentVis - VisRate);
    }

    public void changeTaint() {
        int x = this.xCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
        int y = 0;
        int z = this.zCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
        BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(x, z);
        if (bg.biomeID != ThaumcraftWorldGenerator.biomeTaint.biomeID) {
            float offsetY = (float) (Math.sin(Math.toRadians(tick * 1.0F)) / 4.0F);
            float offsetZ = (float) (Math.sin(Math.toRadians(tick * 3.0F)) / 4.0F);
            float offsetX = (float) (Math.cos(Math.toRadians(tick * 3.0F)) / 4.0F);
            boolean found = false;
            for (int yTest = yCoord - 5; yTest <= yCoord + 5; yTest++) {
                if (worldObj.getBlock(x, yTest, z) != Blocks.air && worldObj.getBlock(x, yTest, z) == Blocks.air) {
                    found = true;
                    y = yTest;
                    break;
                }
            }
            ThaumicExploration.proxy.spawnLightningBolt(
                    worldObj,
                    xCoord + 0.5F + offsetX,
                    yCoord + 1.5F + offsetY,
                    zCoord + offsetZ + 0.5F,
                    x,
                    found ? y : yCoord - 1,
                    z);
            Utils.setBiomeAt(this.worldObj, x, z, ThaumcraftWorldGenerator.biomeTaint);
        }
        if ((Config.hardNode) && (this.worldObj.rand.nextBoolean())) {
            x = this.xCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
            z = this.zCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
            y = this.yCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
            if (!BlockTaintFibres.spreadFibres(this.worldObj, x, y, z));
        }
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public boolean isConnectable(ForgeDirection forgeDirection) {
        return forgeDirection == ForgeDirection.DOWN;
    }

    @Override
    public boolean canInputFrom(ForgeDirection forgeDirection) {
        return forgeDirection == ForgeDirection.DOWN;
    }

    @Override
    public boolean canOutputTo(ForgeDirection forgeDirection) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int i) {}

    @Override
    public Aspect getSuctionType(ForgeDirection forgeDirection) {
        return (forgeDirection == ForgeDirection.DOWN) ? Aspect.DEATH : null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection forgeDirection) {
        return (forgeDirection == ForgeDirection.DOWN) ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int i, ForgeDirection forgeDirection) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int i, ForgeDirection forgeDirection) {
        int filled = EssentiaCapacity - currentEssentia;
        if (i < filled) {
            currentEssentia += i;
            filled = i;
        } else {
            currentEssentia = EssentiaCapacity;
        }
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        return filled;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection forgeDirection) {
        return (forgeDirection == ForgeDirection.DOWN) ? Aspect.DEATH : null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection forgeDirection) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public void forceChunkLoading(ForgeChunkManager.Ticket ticket) {
        if (ConfigTX.allowSBChunkLoading) {
            this.heldChunk = ticket;
            ForgeChunkManager.forceChunk(this.heldChunk, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));
        }
    }

    @Override
    public void addTicket() {
        ForgeChunkManager.Ticket newTicket = ForgeChunkManager
                .requestTicket(ThaumicExploration.instance, this.worldObj, ForgeChunkManager.Type.NORMAL);
        newTicket.getModData().setInteger("xCoord", this.xCoord);
        newTicket.getModData().setInteger("yCoord", this.yCoord);
        newTicket.getModData().setInteger("zCoord", this.zCoord);
        newTicket.getModData().setBoolean("warpChunk", true);
        this.heldChunk = newTicket;
        ForgeChunkManager.forceChunk(this.heldChunk, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));
    }

    @Override
    public void removeTicket(ForgeChunkManager.Ticket ticket) {
        if (heldChunk != null) {
            ForgeChunkManager.releaseTicket(this.heldChunk);
            this.heldChunk = null;
        }
    }
}
