package flaxbeard.thaumicexploration.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.tile.TileEntityEverburnUrn;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public class BlockEverburnUrn extends BlockContainer {

    public static IIcon middleSide;
    public static IIcon topSide;
    public static IIcon topTop;
    public static IIcon bottomTop;
    public static IIcon bottomBottom;
    public static IIcon topBottom;
    public IIcon texture;

    public BlockEverburnUrn() {
        super(Material.rock);
        // TODO Auto-generated constructor stub
    }

    public TileEntity createNewTileEntity(World par1World) {
        return new TileEntityEverburnUrn();
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
        float f2 = (float) par4;
        float f3 = 0.52F;
        float f4 = par5Random.nextFloat() * 0.6F - 0.3F;
        par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        // par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        // par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        // par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        // par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        // par1World.spawnParticle("lava", f, f1, f2, 0.0D, 1.0D, 0.0D);
        if (Math.random() < 0.1) {}

        par1World.spawnParticle("smoke", (double) (f + f4), (double) f1 + 0.2F, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
        par1World.spawnParticle("flame", (double) (f + f4), (double) f1 + 0.2F, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
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

        TileEntityEverburnUrn entity = (TileEntityEverburnUrn) world.getTileEntity(par2, par3, par4);

        if (entityPlayer.inventory.getCurrentItem() != null) {
            if (FluidContainerRegistry.isEmptyContainer(entityPlayer.inventory.getCurrentItem())) {
                ItemStack newStack = entityPlayer.inventory.getCurrentItem();
                ItemStack filledStack = FluidContainerRegistry.fillFluidContainer(entity.getFluid(), newStack);
                if (filledStack != null) {
                    entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
                    entity.drain(FluidContainerRegistry.getFluidForFilledItem(filledStack).amount, true);
                    if (!entityPlayer.inventory.addItemStackToInventory(filledStack)) {
                        entityPlayer.dropPlayerItemWithRandomChoice(filledStack, false);
                    }
                    world.playSoundAtEntity(entityPlayer, "liquid.lava", 0.5F, 1.0F);
                    world.markBlockForUpdate(par2, par3, par4);
                }
            } else if (entityPlayer.inventory.getCurrentItem().getItem() instanceof IFluidContainerItem) {
                IFluidContainerItem itm = (IFluidContainerItem)
                        entityPlayer.inventory.getCurrentItem().getItem();
                int fill = itm.fill(entityPlayer.inventory.getCurrentItem(), entity.getFluid(), true);
                if (fill > 0) {
                    entity.drain(fill, true);
                    world.playSoundAtEntity(entityPlayer, "liquid.lava", 0.5F, 1.0F);
                    world.markBlockForUpdate(par2, par3, par4);
                }
            }
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        // TODO Auto-generated method stub
        return new TileEntityEverburnUrn();
    }
}
