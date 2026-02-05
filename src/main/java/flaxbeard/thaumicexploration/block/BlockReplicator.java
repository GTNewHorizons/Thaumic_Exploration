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

    public final IIcon[] icon = new IIcon[3];

    public BlockReplicator() {
        super(Material.iron);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityReplicator replicator) {
            ItemStack template = replicator.getStackInSlot(0);
            if (template != null && template.stackSize > 0) {
                InventoryUtils.dropItems(world, x, y, z);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return ThaumicExploration.replicatorRenderID;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityReplicator replicator) {
            boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
            replicator.updateRedstoneState(powered);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
            float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityReplicator replicator)) return true;

        ItemStack held = player.getCurrentEquippedItem();

        if (replicator.crafting && (held == null || !(held.getItem() instanceof ItemWandCasting))) {
            replicator.cancelCrafting();
        }

        ItemStack template = replicator.getStackInSlot(0);

        if (template != null && (held == null || !(held.getItem() instanceof ItemWandCasting))) {
            ejectBlockFromReplicator(world, x, y, z, replicator, template);
            return true;
        }

        if (held != null && ReplicatorRecipes.canStackBeReplicated(held)) {
            trySetTemplateBlock(world, x, y, z, replicator, held, player);
            return true;
        }

        return true;
    }

    private void ejectBlockFromReplicator(World world, int x, int y, int z, TileEntityReplicator replicator,
            ItemStack template) {
        if (template.stackSize > 0) {
            EntityItem item = new EntityItem(world, x + 0.5, y + 1.2F, z + 0.5, template.copy());

            item.motionX = 0;
            item.motionY = 0.2F;
            item.motionZ = 0;

            world.spawnEntityInWorld(item);
            template.stackSize = 0;
        } else {
            replicator.setInventorySlotContents(0, null);
        }

        world.markBlockForUpdate(x, y, z);
        world.playSoundEffect(
                x,
                y,
                z,
                "random.pop",
                0.2F,
                (world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.5F);
    }

    private void trySetTemplateBlock(World world, int x, int y, int z, TileEntityReplicator replicator, ItemStack held,
            EntityPlayer player) {
        ItemStack newTemplate = held.copy();
        newTemplate.stackSize = 0;
        replicator.setInventorySlotContents(0, newTemplate);

        // If you want to consume the item: (uncomment if needed)
        // if (--held.stackSize <= 0) player.setCurrentItemOrArmor(0, null);

        world.markBlockForUpdate(x, y, z);
        world.playSoundEffect(
                x,
                y,
                z,
                "random.pop",
                0.2F,
                (world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.6F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister ir) {
        super.registerBlockIcons(ir);
        this.icon[0] = ir.registerIcon("thaumicexploration:replicatorBottom");
        this.icon[1] = ir.registerIcon("thaumicexploration:replicator");
        this.icon[2] = ir.registerIcon("thaumicexploration:replicatorTop");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        return (side == 0 || side == 1) ? icon[0] : icon[1];
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityReplicator();
    }
}
