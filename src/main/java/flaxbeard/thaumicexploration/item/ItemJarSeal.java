package flaxbeard.thaumicexploration.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.data.BoundJarNetworkManager;
import flaxbeard.thaumicexploration.misc.NBTHelper;
import flaxbeard.thaumicexploration.tile.TileEntityBoundJar;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.codechicken.lib.colour.Colour;
import thaumcraft.codechicken.lib.colour.ColourRGBA;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJarFillable;

/**
 * Created by Katrina on 16/06/2015.
 */
public class ItemJarSeal extends Item {

    public IIcon theIcon;

    @Override
    public void registerIcons(IIconRegister p_94581_1_) {
        super.registerIcons(p_94581_1_);
        theIcon = p_94581_1_.registerIcon(this.iconString + "Inset");
    }

    public ItemJarSeal() {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(64);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float dx,
            float dy, float dz) {
        if (world.getBlock(x, y, z) == ConfigBlocks.blockJar) {
            TileJarFillable entity = (TileJarFillable) world.getTileEntity(x, y, z);
            if (entity.amount == 0 && entity.aspectFilter == null) {
                world.setBlock(x, y, z, ThaumicExploration.boundJar);
                String id = getNetwork(stack);
                TileEntityBoundJar tileEntity = (TileEntityBoundJar) world.getTileEntity(x, y, z);
                tileEntity.colour = stack.getItemDamage();

                if (id != null) {
                    tileEntity.networkName = id;
                    AspectList aList = BoundJarNetworkManager.getAspect(id);
                    tileEntity.aspect = aList.getAspects()[0];
                    tileEntity.amount = aList.getAmount(tileEntity.aspect);
                    tileEntity.markDirty();
                }
                world.markBlockForUpdate(x, y, z);
                player.inventory.getCurrentItem().stackSize--;
                if (player.inventory.getCurrentItem().stackSize <= 0)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        } else if (world.getBlock(x, y, z) == ThaumicExploration.boundJar) {
            TileEntityBoundJar tileEntity = (TileEntityBoundJar) world.getTileEntity(x, y, z);
            ItemStack newStack = new ItemStack(this, 1, tileEntity.colour);
            setNetwork(newStack, tileEntity.networkName);
            player.inventory.getCurrentItem().stackSize--;
            if (player.inventory.getCurrentItem().stackSize <= 0)
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            if (!player.inventory.addItemStackToInventory(newStack))
                player.dropPlayerItemWithRandomChoice(newStack, true);
        }
        return super.onItemUse(stack, player, world, x, y, z, side, dx, dy, dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < 16; i++) {
            ItemStack stack = new ItemStack(this, 1, i);
            // setNetwork(stack, StringID.getName()); // Testing code to test name and rendering
            p_150895_3_.add(stack);
        }
    }

    @Override
    public int getColorFromItemStack(ItemStack p_82790_1_, int p_82790_2_) {

        int damage = p_82790_1_.getItemDamage();
        int color = ItemDye.field_150922_c[damage];

        Colour colour = new ColourRGBA(color);
        if (p_82790_2_ == 0) return colour.rgba();
        else if (getNetwork(p_82790_1_) != null) return colour.invert().rgba();
        else return colour.rgba();
    }

    @Override
    public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_) {
        switch (p_77618_2_) {
            case 0:
                return itemIcon;
            case 1:
                return theIcon;
            default:
                return itemIcon;
        }
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        if (item.getItemDamage() <= 15) {
            return this.getUnlocalizedName() + ":" + ItemChestSeal.itemNames[15 - item.getItemDamage()];
        }
        return "";
    }

    public String getNetwork(ItemStack stack) {
        if (stack.hasDisplayName()) return stack.getDisplayName();

        if (NBTHelper.getItemStackTag(stack).hasKey("network"))
            return NBTHelper.getItemStackTag(stack).getString("network");
        else return null;
    }

    public void setNetwork(ItemStack stack, String networkName) {
        NBTHelper.getItemStackTag(stack).setString("network", networkName);
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
        String network = getNetwork(p_77624_1_);
        if (network != null)
            p_77624_3_.add(StatCollector.translateToLocalFormatted("txitems.boundJar.networkInfo", network));
        else p_77624_3_.add(StatCollector.translateToLocal("txitems.boundJar.noNetworkInfo"));
    }
}
