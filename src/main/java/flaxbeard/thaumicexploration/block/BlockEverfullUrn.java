package flaxbeard.thaumicexploration.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.tile.TileEntityEverfullUrn;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class BlockEverfullUrn extends BlockContainer {

    public static IIcon middleSide;
    public static IIcon topSide;
    public static IIcon topTop;
    public static IIcon bottomTop;
    public static IIcon bottomBottom;
    public static IIcon topBottom;
    public IIcon texture;

    public BlockEverfullUrn() {
        super(Material.rock);
        // TODO Auto-generated constructor stub
    }

    public TileEntity createNewTileEntity(World par1World) {
        return new TileEntityEverfullUrn();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IIconRegister) {
        super.registerBlockIcons(par1IIconRegister);
        this.middleSide = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnMS");
        this.topSide = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnTS");
        this.topTop = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnTT");
        this.bottomTop = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnBT");
        this.bottomBottom = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnBB");
        this.topBottom = par1IIconRegister.registerIcon("thaumicExploration:everfullUrnTB");
    }

    public void setBlockBoundsBasedOnState(IBlockAccess world, int i, int j, int k) {
        float f = (14.0F / 16.0F);
        float f1 = (2.0F / 16.0F);
        float f2 = (9.0F / 16.0F);
        float f3 = (6.0F / 16.0F);
        float f4 = (5.0F / 16.0F);
        setBlockBounds(f1, 0.0F, f1, f, 1.0F, f);
    }

    public void addCollisionBoxesToList(
            World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
        float f = (14.0F / 16.0F);
        float f1 = (2.0F / 16.0F);
        float f2 = (9.0F / 16.0F);
        float f3 = (6.0F / 16.0F);
        float f4 = (5.0F / 16.0F);
        setBlockBounds(f1, 0.0F, f1, f, 1.0F, f);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random) {
        int l = par1World.getBlockMetadata(par2, par3, par4);
        float f = (float) par2 + 0.5F;
        float f1 = (float) par3 + 1.0F;
        float f2 = (float) par4 + 0.5F;
        float f3 = 0.52F;
        float f4 = par5Random.nextFloat() * 0.6F - 0.3F;
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        par1World.spawnParticle("splash", f, f1, f2, 0.0D, 1.0D, 0.0D);
        if (Math.random() < 0.1) {}

        // par1World.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
        // par1World.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);

    }

    public boolean isOpaqueCube() {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ThaumicExploration.everfullUrnRenderID;
    }

    public boolean onBlockActivated(
            World world,
            int par2,
            int par3,
            int par4,
            EntityPlayer entityPlayer,
            int par6,
            float par7,
            float par8,
            float par9) {
        FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
        ItemStack activeStack = entityPlayer.getCurrentEquippedItem();

        if (activeStack != null) {
            if (activeStack.getItem() == Items.bucket) {
                world.playSoundAtEntity(entityPlayer, "liquid.water", 0.5F, 1.0F);
                activeStack.stackSize -= 1;
                if (!entityPlayer.inventory.addItemStackToInventory(new ItemStack(Items.water_bucket, 1))) {
                    entityPlayer.dropItem(Items.water_bucket, 1);
                }
            } else if (FluidContainerRegistry.isEmptyContainer(activeStack)) {
                ItemStack filledStack = FluidContainerRegistry.fillFluidContainer(water, activeStack.splitStack(1));
                if (filledStack != null) {
                    world.playSoundAtEntity(entityPlayer, "liquid.water", 0.5F, 1.0F);
                    if (!entityPlayer.inventory.addItemStackToInventory(filledStack)) {
                        entityPlayer.dropPlayerItemWithRandomChoice(filledStack, false);
                    }
                } else {
                    activeStack.stackSize += 1;
                }
            } else if (activeStack.getItem() instanceof IFluidContainerItem) {
                ItemStack filledStack = activeStack.splitStack(1);
                if (((IFluidContainerItem) filledStack.getItem()).fill(filledStack, water, true) > 0) {
                    world.playSoundAtEntity(entityPlayer, "liquid.water", 0.5F, 1.0F);
                    if (!entityPlayer.inventory.addItemStackToInventory(filledStack)) {
                        entityPlayer.dropPlayerItemWithRandomChoice(filledStack, false);
                    }
                } else {
                    activeStack.stackSize += 1;
                }
            }

            if (activeStack.stackSize == 0) {
                entityPlayer.inventory.setItemStack(null);
            }
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        // TODO Auto-generated method stub
        return new TileEntityEverfullUrn();
    }
}
