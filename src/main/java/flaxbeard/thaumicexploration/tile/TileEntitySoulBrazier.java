package flaxbeard.thaumicexploration.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import flaxbeard.thaumicexploration.misc.brazier.SoulBrazierUtils;
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
    public boolean hasWarpQueue = false;
    public GameProfile owner;
    public int count;
    private int ticks = 0;
    private static int EssentiaCapacity = 16;
    private static int VisCapacity = 16;
    public static int EssentiaRate = 1;
    public static int VisRate = 3;
    public ForgeChunkManager.Ticket heldChunk;
    public static boolean renderWisp = false;

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        storedWarp = nbttagcompound.getInteger("storedWarp");
        currentEssentia = nbttagcompound.getInteger("currentEssentia");
        currentVis = nbttagcompound.getInteger("currentVis");
        active = nbttagcompound.getBoolean("active");
        hasWarpQueue = nbttagcompound.getBoolean("hasWarpQueue");
        owner = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("owner"));
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("storedWarp", storedWarp);
        nbttagcompound.setInteger("currentEssentia", currentEssentia);
        nbttagcompound.setInteger("currentVis", currentVis);
        nbttagcompound.setBoolean("active", active);
        nbttagcompound.setBoolean("hasWarpQueue", hasWarpQueue);
        NBTTagCompound gameProfile = new NBTTagCompound();
        NBTUtil.func_152460_a(gameProfile, owner);
        nbttagcompound.setTag("owner", gameProfile);
    }

    public boolean setActive(EntityPlayer player) {
        if (!worldObj.isRemote && !hasWarpQueue) {
            int playerWarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(owner.getName());
            if (playerWarp <= 0) {
                player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.noWarp"));
                return false;
            }
            if (!EntityPlayer.func_146094_a(player.getGameProfile()).equals(owner.getId())) {

                player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.invalidplayer"));
                return false;
            }
            if (!checkPower()) {
                player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.nopower"));
                return false;
            }
            if (!SoulBrazierUtils.isPlayerInRangeOfBrazier(player, this)) {
                player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.norange"));
                return false;
            }
            active = true;
            storedWarp += playerWarp;
            Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(owner.getName(), 0);
            SoulBrazierUtils.syncPermWarp((EntityPlayerMP) player);

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.count == Integer.MAX_VALUE) {
            this.count = 0;
        }
        this.count += 1;
        if (hasWarpQueue) {
            return;
        }
        if (!renderWisp && ThaumicExploration.proxy.getIsReadyForWisp()) {
            renderWisp = true;
        }
        if (this.count % 10 == 0 && renderWisp && active) {
            ThaumicExploration.proxy.spawnActiveBrazierParticle(worldObj, xCoord, yCoord, zCoord);
        }
        if ((this.count % 5 == 0) && (this.currentEssentia < EssentiaCapacity)) {
            fillJar();
        }
        changeTaint();

        getPower();
        if (active) {
            if (heldChunk == null && ConfigTX.allowSBChunkLoading) addTicket();
            if (this.count % 60 == 0) spendPower();
            if (!checkPower()) {
                active = false;

                if (!worldObj.isRemote) {

                    // Is player online?
                    if (SoulBrazierUtils.isPlayerOnline(owner.getId())) {
                        int aCurrentWarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(owner.getName());
                        int aTotalWarp = aCurrentWarp + storedWarp;

                        if (aCurrentWarp != aTotalWarp) {
                            Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(owner.getName(), aTotalWarp);
                            EntityPlayer player = SoulBrazierUtils.getPlayerFromUUID(owner.getId());
                            SoulBrazierUtils.syncPermWarp((EntityPlayerMP) player);
                        }
                    }
                    // Queue warp addition to file for next join.
                    else {
                        SoulBrazierUtils.writePlayerWarpToQueueFile(owner.getId(), this);
                        hasWarpQueue = true;
                    }
                }
                storedWarp = 0;
                ForgeChunkManager
                        .unforceChunk(this.heldChunk, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));
                this.heldChunk = null;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    public void fillJar() {
        TileEntity te = ThaumcraftApiHelper
                .getConnectableTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.DOWN);
        if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport) te;
            if (!ic.canOutputTo(ForgeDirection.UP)) {
                return;
            }

            Aspect ta = Aspect.DEATH;
            if ((ic.getEssentiaAmount(ForgeDirection.UP) > 0)
                    && (ic.getSuctionAmount(ForgeDirection.UP) < getSuctionAmount(ForgeDirection.UP))
                    && (getSuctionAmount(ForgeDirection.UP) >= ic.getMinimumSuction())) {
                ta = ic.getEssentiaType(ForgeDirection.UP);
            }

            if ((ta != null) && (ic.getSuctionAmount(ForgeDirection.UP) < getSuctionAmount(ForgeDirection.DOWN)))
                addEssentia(ta, ic.takeEssentia(ta, 1, ForgeDirection.UP), ForgeDirection.DOWN);
        }
    }

    public boolean checkPower() {
        return currentEssentia > 0 && currentVis > 0;
    }

    private void spendPower() {
        currentEssentia = Math.max(0, currentEssentia - EssentiaRate);
        currentVis = Math.max(0, currentVis - VisRate);
    }

    private void getPower() {
        if (this.count % 5 == 0) {
            if (this.currentVis < VisCapacity) {
                currentVis += this.consumeVis(Aspect.FIRE, 1);
            }
        }
    }

    public void changeTaint() {
        if (active && (this.count % 50 == 0)) {
            int x = 0;
            int z = 0;
            int y = 0;
            x = this.xCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);

            z = this.zCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
            BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(x, z);
            if (bg.biomeID != ThaumcraftWorldGenerator.biomeTaint.biomeID) {
                float offsetY = (float) (Math.sin(Math.toRadians(count * 1.0F)) / 4.0F);
                float offsetZ = (float) (Math.sin(Math.toRadians(count * 3.0F)) / 4.0F);
                float offsetX = (float) (Math.cos(Math.toRadians(count * 3.0F)) / 4.0F);
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
        int newEssentia = 0;
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

        this.heldChunk = ticket;
        ForgeChunkManager.forceChunk(this.heldChunk, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));
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
