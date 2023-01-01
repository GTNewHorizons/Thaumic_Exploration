package flaxbeard.thaumicexploration.block;

import com.mojang.authlib.GameProfile;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.misc.brazier.SoulBrazierUtils;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import thaumcraft.common.Thaumcraft;

/**
 * Created by nekosune on 03/08/14.
 */
public class BlockSoulBrazier extends BlockContainer {

    public BlockSoulBrazier() {
        super(Material.rock);
        setBlockTextureName("thaumicexploration:soulBrazier");
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        if (!world.isRemote) {
            TileEntitySoulBrazier entity = ((TileEntitySoulBrazier) world.getTileEntity(x, y, z));

            Thaumcraft.proxy.getPlayerKnowledge().addWarpPerm(entity.owner.getName(), entity.storedWarp);
            if (SoulBrazierUtils.isPlayerOnline(entity.owner.getId())) {
                EntityPlayer player = SoulBrazierUtils.getPlayerFromUUID(entity.owner.getId());
                SoulBrazierUtils.syncPermWarp((EntityPlayerMP) player);
            }
            ForgeChunkManager.unforceChunk(
                    entity.heldChunk, new ChunkCoordIntPair(entity.xCoord >> 4, entity.zCoord >> 4));
        }
        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public boolean onBlockActivated(
            World p_149727_1_,
            int p_149727_2_,
            int p_149727_3_,
            int p_149727_4_,
            EntityPlayer p_149727_5_,
            int p_149727_6_,
            float p_149727_7_,
            float p_149727_8_,
            float p_149727_9_) {
        super.onBlockActivated(
                p_149727_1_,
                p_149727_2_,
                p_149727_3_,
                p_149727_4_,
                p_149727_5_,
                p_149727_6_,
                p_149727_7_,
                p_149727_8_,
                p_149727_9_);
        // if(p_149727_5_.getGameProfile().getId().equals(((TileEntitySoulBrazier)p_149727_1_.getTileEntity(p_149727_2_,p_149727_3_,p_149727_4_)).owner.getId())) {
        return ((TileEntitySoulBrazier) p_149727_1_.getTileEntity(p_149727_2_, p_149727_3_, p_149727_4_))
                .setActive(p_149727_5_);
    }

    @Override
    public void onBlockClicked(
            World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_) {
        super.onBlockClicked(p_149699_1_, p_149699_2_, p_149699_3_, p_149699_4_, p_149699_5_);
    }

    @Override
    public void onBlockPlacedBy(
            World p_149689_1_,
            int p_149689_2_,
            int p_149689_3_,
            int p_149689_4_,
            EntityLivingBase p_149689_5_,
            ItemStack p_149689_6_) {
        super.onBlockPlacedBy(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, p_149689_5_, p_149689_6_);

        GameProfile profile = ((EntityPlayer) p_149689_5_).getGameProfile();
        ((TileEntitySoulBrazier) p_149689_1_.getTileEntity(p_149689_2_, p_149689_3_, p_149689_4_)).owner = profile;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ThaumicExploration.soulBrazierRenderID;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntitySoulBrazier();
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
        setBlockBounds(0.175F, 0.0F, 0.175F, 0.925F, 1.0F, 0.925F);
        // super.setBlockBoundsBasedOnState(par1iBlockAccess, par2, par3, par4);
    }
}
