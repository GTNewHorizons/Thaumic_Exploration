package flaxbeard.thaumicexploration.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.research.ReplicatorRecipes;
import flaxbeard.thaumicexploration.tile.TileEntityReplicator;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.InventoryUtils;

public class BlockReplicator extends BlockContainer {

    public IIcon[] icon = new IIcon[3];

    public BlockReplicator() {
        super(Material.iron);
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6) {
        super.breakBlock(par1World, par2, par3, par4, par5, par6);
        TileEntity tileEntity = par1World.getTileEntity(par2, par3, par4);
        if (tileEntity instanceof TileEntityReplicator) {
            if (((TileEntityReplicator) tileEntity).getStackInSlot(0) != null
                    && ((TileEntityReplicator) tileEntity).getStackInSlot(0).stackSize > 0) {
                InventoryUtils.dropItems(par1World, par2, par3, par4);
            }
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block par5) {
        boolean flag = world.isBlockIndirectlyGettingPowered(x, y, z);

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if ((tileEntity instanceof TileEntityReplicator)) {
            TileEntityReplicator ped = (TileEntityReplicator) tileEntity;
            ped.updateRedstoneState(flag);
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7,
            float par8, float par9) {
        if (world.isRemote) {
            return true;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if ((!(tileEntity instanceof TileEntityReplicator))) return true;

        TileEntityReplicator ped = (TileEntityReplicator) tileEntity;
        final ItemStack heldItemStack = player.getCurrentEquippedItem();
        if (ped.crafting && (heldItemStack == null || !(heldItemStack.getItem() instanceof ItemWandCasting))) {
            ped.cancelCrafting();
        }

        if (ped.getStackInSlot(0) != null
                && (heldItemStack == null || !(heldItemStack.getItem() instanceof ItemWandCasting))) {
            ItemStack itemstack = ped.getStackInSlot(0);

            if (itemstack.stackSize > 0) {
                float f = world.rand.nextFloat() * 0.8F + 0.1F;
                float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                float f2 = world.rand.nextFloat();
                EntityItem entityitem;
                int k1 = itemstack.stackSize;
                entityitem = new EntityItem(
                        world,
                        ((float) x + f),
                        ((float) y + f1),
                        ((float) z + f2),
                        new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
                float f3 = 0.05F;
                entityitem.motionX = ((float) world.rand.nextGaussian() * f3);
                entityitem.motionY = ((float) world.rand.nextGaussian() * f3 + 0.2F);
                entityitem.motionZ = ((float) world.rand.nextGaussian() * f3);

                if (itemstack.hasTagCompound()) {
                    entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                }
                world.spawnEntityInWorld(entityitem);
            }
            TileEntity te = world.getTileEntity(x, y, z);
            ItemStack template = ((TileEntityReplicator) te).getStackInSlot(0).copy();
            if (template.stackSize > 0) {
                template.stackSize = template.stackSize - 1;

                ((TileEntityReplicator) te).setInventorySlotContents(0, template);
            } else {
                ((TileEntityReplicator) te).setInventorySlotContents(0, null);
            }
            world.markBlockForUpdate(x, y, z);
            world.playSoundEffect(
                    x,
                    y,
                    z,
                    "random.pop",
                    0.2F,
                    ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);

            return true;
        }
        if (heldItemStack != null && heldItemStack.getItem() != null) {
            if (ReplicatorRecipes.canStackBeReplicated(heldItemStack)) {
                // IN
                ItemStack i = heldItemStack.copy();
                i.stackSize = 0;
                ped.setInventorySlotContents(0, i);
                // player.getCurrentEquippedItem().stackSize -= 1;
                if (heldItemStack.stackSize == 0) {
                    player.setCurrentItemOrArmor(0, null);
                }
                // player.inventory.onInventoryChanged();
                world.markBlockForUpdate(x, y, z);
                world.playSoundEffect(
                        x,
                        y,
                        z,
                        "random.pop",
                        0.2F,
                        ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);
                return true;
            }
        }

        return true;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ThaumicExploration.replicatorRenderID;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister ir) {
        super.registerBlockIcons(ir);
        this.icon[0] = ir.registerIcon("thaumicexploration:replicatorBottom");
        this.icon[1] = ir.registerIcon("thaumicexploration:replicator");
        this.icon[2] = ir.registerIcon("thaumicexploration:replicatorTop");
    }

    @Override
    public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int side) {
        if (side == 0 || side == 1) {
            return this.icon[0];
        }
        return this.icon[1];
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        // TODO Auto-generated method stub
        return new TileEntityReplicator();
    }
}
