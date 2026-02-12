package flaxbeard.thaumicexploration.tile;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.network.NetworkRegistry;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.research.ReplicatorRecipes;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;

public class TileEntityReplicator extends TileEntity implements ISidedInventory, IWandable, IAspectContainer {

    private static final int[] slots = { 0 };
    private ItemStack item;
    private final ArrayList<ChunkCoordinates> sources = new ArrayList<>();

    public boolean crafting = false;
    public boolean redstoneState = false;
    public int ticksLeft = 0;
    private int essentiaTicks = 0;

    public AspectList requiredEssentia = new AspectList();
    public AspectList templateEssentia = new AspectList();

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeInventoryNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readInventoryNBT(tag);
    }

    @Override
    public void updateEntity() {
        if (crafting) {
            if (worldObj.isRemote) {
                if (requiredEssentia.size() <= 0) ticksLeft--;
                spawnClientParticles();
            } else {
                updateCraftingServer();
            }
        }
    }

    public void startCrafting() {
        ItemStack input = getStackInSlot(0);
        if (input == null || input.stackSize > 0 || worldObj.isRemote || !ReplicatorRecipes.canStackBeReplicated(input))
            return;

        crafting = true;
        ticksLeft = 100;
        essentiaTicks = 0;

        ItemStack copy = input.copy();
        copy.stackSize = 1;
        requiredEssentia = ThaumcraftCraftingManager.getBonusTags(copy, ThaumcraftCraftingManager.getObjectTags(copy));
        templateEssentia = requiredEssentia.copy();

        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "thaumcraft:craftstart", 0.5F, 1.0F);
        markBlockForUpdate();
    }

    public void cancelCrafting() {
        crafting = false;
        requiredEssentia.aspects.clear();
        templateEssentia.aspects.clear();
        markBlockForUpdate();
        markDirty();
    }

    private void updateCraftingServer() {
        if (requiredEssentia.visSize() > 0) {
            essentiaTicks++;

            if (essentiaTicks > 49) {
                if (sources.isEmpty()) getSurroundings();
                essentiaTicks = 0;
                drainEssentiaFromSources();
            }
            return;
        }

        if (ticksLeft <= 0) {
            finishCrafting();
            return;
        }

        ticksLeft--;
    }

    private void drainEssentiaFromSources() {
        Iterator<ChunkCoordinates> it = sources.iterator();
        while (it.hasNext()) {
            ChunkCoordinates cc = it.next();
            IAspectSource source = getValidAspectSource(worldObj.getTileEntity(cc.posX, cc.posY, cc.posZ));
            if (source == null) {
                it.remove();
                continue;
            }
            for (Aspect aspect : requiredEssentia.getAspects()) {
                if (requiredEssentia.getAmount(aspect) <= 0 || !source.doesContainerContainAmount(aspect, 1)) continue;
                PacketHandler.INSTANCE.sendToAllAround(
                        new PacketFXEssentiaSource(
                                this.xCoord,
                                this.yCoord + 1,
                                this.zCoord,
                                (byte) (this.xCoord - cc.posX),
                                (byte) (this.yCoord - cc.posY + 1),
                                (byte) (this.zCoord - cc.posZ),
                                aspect.getColor()),
                        new NetworkRegistry.TargetPoint(
                                getWorldObj().provider.dimensionId,
                                this.xCoord,
                                this.yCoord,
                                this.zCoord,
                                32.0D));

                source.takeFromContainer(aspect, 1);
                requiredEssentia.remove(aspect, 1);
                markBlockForUpdate();
                return;
            }
        }
        getSurroundings(); // Fallback if none of the sources in range have the required aspect
    }

    private void finishCrafting() {
        item.stackSize++;
        crafting = false;
        requiredEssentia.aspects.clear();
        markBlockForUpdate();
    }

    private void spawnClientParticles() {
        if (ticksLeft >= 100 || templateEssentia.aspects.isEmpty()) return;

        ItemStack example = getStackInSlot(0).copy();
        example.stackSize = 1;

        for (int i = 0; i < 5; i++) {
            ThaumicExploration.proxy.spawnFragmentParticle(
                    worldObj,
                    xCoord + 0.5F + randomOffset(),
                    yCoord + 1.5F + randomOffset(),
                    zCoord + 0.5F + randomOffset(),
                    xCoord + 0.5F,
                    yCoord + 1.5F,
                    zCoord + 0.5F,
                    Block.getBlockFromItem(example.getItem()),
                    example.getItemDamage());
        }

        if (worldObj.rand.nextInt(4) == 0 && ticksLeft > 40) {
            Aspect randomAspect = templateEssentia.getAspects()[worldObj.rand.nextInt(templateEssentia.size())];
            ThaumicExploration.proxy.spawnEssentiaAtLocation(
                    worldObj,
                    xCoord + 0.5F + randomOffset(),
                    yCoord + 1.5F + randomOffset(),
                    zCoord + 0.5F + randomOffset(),
                    xCoord + 0.5F,
                    yCoord + 1.5F,
                    zCoord + 0.5F,
                    5,
                    randomAspect.getColor());
        }

        if (worldObj.rand.nextInt(3) == 0) {
            ThaumicExploration.proxy.spawnBoreSparkle(
                    worldObj,
                    xCoord + 0.5F + randomOffset(),
                    yCoord + 1.5F + randomOffset(),
                    zCoord + 0.5F + randomOffset(),
                    xCoord + 0.5F,
                    yCoord + 1.5F,
                    zCoord + 0.5F);
        }
    }

    private float randomOffset() {
        return (float) (2 * Math.random() - 1.0F);
    }

    private void markBlockForUpdate() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private void getSurroundings() {
        sources.clear();
        for (int xx = -12; xx <= 12; xx++) {
            for (int zz = -12; zz <= 12; zz++) {
                for (int yy = -5; yy <= 10; yy++) {
                    if (xx == 0 && zz == 0 && yy == 0) continue;

                    int x = xCoord + xx;
                    int y = yCoord - yy;
                    int z = zCoord + zz;
                    TileEntity te = worldObj.getTileEntity(x, y, z);
                    if (getValidAspectSource(te) != null) {
                        sources.add(new ChunkCoordinates(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Get the IAspectSource from a TileEntity iff it contains at least one relevant type of essentia.
     *
     * @return IAspectSource from a TileEntity if valid, otherwise null
     */
    private IAspectSource getValidAspectSource(TileEntity source) {
        if (!(source instanceof IAspectSource as)) return null;
        AspectList sourceAspects = as.getAspects();
        for (Aspect aspect : templateEssentia.getAspects()) {
            if (sourceAspects.aspects.containsKey(aspect)) {
                return as;
            }
        }
        return null;
    }

    public void updateRedstoneState(boolean newState) {
        if (!redstoneState && newState && !crafting) {
            startCrafting();
        }
        redstoneState = newState;
    }

    public boolean validLocation() {
        Block block = worldObj.getBlock(xCoord, yCoord + 1, zCoord);
        Material mat = block.getMaterial();
        return mat == Config.airyMaterial || mat == Material.air
                || block.isReplaceable(worldObj, xCoord, yCoord + 1, zCoord)
                || block.getLightOpacity() == 0;
    }

    private void writeInventoryNBT(NBTTagCompound tag) {
        tag.setBoolean("Crafting", crafting);
        tag.setInteger("Ticks", ticksLeft);

        if (item != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            item.writeToNBT(itemTag);
            tag.setTag("Item", itemTag);
        }

        tag.setTag("Aspects", writeEssentiaNBT(requiredEssentia));
        tag.setTag("Template", writeEssentiaNBT(templateEssentia));
    }

    private @NotNull NBTTagCompound writeEssentiaNBT(AspectList list) {
        NBTTagCompound aspects = new NBTTagCompound();

        for (Aspect a : list.getAspects()) {
            if (a == null) continue;
            aspects.setInteger(a.getTag(), list.getAmount(a));
        }

        return aspects;
    }

    private void readInventoryNBT(NBTTagCompound tag) {
        item = null;
        ticksLeft = tag.getInteger("Ticks");
        crafting = tag.getBoolean("Crafting");

        if (tag.hasKey("Items", Constants.NBT.TAG_COMPOUND)) {
            NBTTagList items = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            item = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(0));
        } else if (tag.hasKey("Item", Constants.NBT.TAG_COMPOUND)) {
            item = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("Item"));
        }

        readEssentiaNBT(tag.getCompoundTag("Aspects"), requiredEssentia);
        readEssentiaNBT(tag.getCompoundTag("Template"), templateEssentia);
    }

    private void readEssentiaNBT(NBTTagCompound tag, AspectList list) {
        list.aspects.clear();
        for (String key : tag.func_150296_c()) { // getKeySet()
            Aspect aspect = Aspect.getAspect(key);
            if (aspect == null) continue;
            int amount = tag.getInteger(key);
            if (amount > 0) {
                list.add(aspect, amount);
            }
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeInventoryNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    public void clearSources() {
        sources.clear();
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readInventoryNBT(pkt.func_148857_g());
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return item;
    }

    @Override
    public String getInventoryName() {
        return "replicator";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        item = stack;
        if (item != null && item.stackSize > getInventoryStackLimit()) {
            item.stackSize = getInventoryStackLimit();
        }
        markBlockForUpdate();
        markDirty();
    }

    @Override
    public ItemStack decrStackSize(int i, int count) {
        return item != null ? item.splitStack(Math.min(item.stackSize, count)) : null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        ItemStack stack = item;
        item = null;
        return stack;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return ReplicatorRecipes.canStackBeReplicated(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return slots;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack stack, int side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int side) {
        return item != null && item.stackSize > 0;
    }

    @Override
    public AspectList getAspects() {
        return requiredEssentia;
    }

    @Override
    public void setAspects(AspectList aspects) {}

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public int onWandRightClick(World world, ItemStack wand, EntityPlayer player, int x, int y, int z, int side,
            int md) {
        if (!crafting) {
            startCrafting();
            return 0;
        }
        return -1;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack wand, EntityPlayer player) {
        return wand;
    }

    @Override
    public void onUsingWandTick(ItemStack wand, EntityPlayer player, int count) {}

    @Override
    public void onWandStoppedUsing(ItemStack wand, World world, EntityPlayer player, int count) {}
}
