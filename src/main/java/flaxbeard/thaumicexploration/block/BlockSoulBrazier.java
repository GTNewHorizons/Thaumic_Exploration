package flaxbeard.thaumicexploration.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import com.mojang.authlib.GameProfile;

import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.misc.brazier.SoulBrazierUtils;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import thaumcraft.common.Thaumcraft;

/**
 * Created by nekosune on 03/08/14.
 */
public class BlockSoulBrazier extends BlockContainer {

    public BlockSoulBrazier() {
        super(Material.rock);
        setResistance(2000.0F);
        setBlockTextureName("thaumicexploration:soulBrazier");
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {}

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntitySoulBrazier entity = ((TileEntitySoulBrazier) world.getTileEntity(x, y, z));

            Thaumcraft.proxy.getPlayerKnowledge().addWarpPerm(entity.owner.getName(), entity.storedWarp);
            if (SoulBrazierUtils.isPlayerOnline(entity.owner.getId())) {
                EntityPlayer player = SoulBrazierUtils.getPlayerFromUUID(entity.owner.getId());
                SoulBrazierUtils.syncPermWarp((EntityPlayerMP) player);
            }
            ForgeChunkManager
                    .unforceChunk(entity.heldChunk, new ChunkCoordIntPair(entity.xCoord >> 4, entity.zCoord >> 4));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z,
            EntityPlayer player, int side, float subX, float subY, float subZ) {
        super.onBlockActivated(
                worldIn,
                x,
                y,
                z,
                player,
                side,
                subX,
                subY,
                subZ);
        // if(player.getGameProfile().getId().equals(((TileEntitySoulBrazier)worldIn.getTileEntity(x,y,z)).owner.getId()))
        // {
        return ((TileEntitySoulBrazier) worldIn.getTileEntity(x, y, z))
                .setActive(player);
    }

    @Override
    public void onBlockClicked(World worldIn, int x, int y, int z,
            EntityPlayer player) {
        super.onBlockClicked(worldIn, x, y, z, player);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z,
            EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);

        GameProfile profile = ((EntityPlayer) placer).getGameProfile();
        ((TileEntitySoulBrazier) worldIn.getTileEntity(x, y, z)).owner = profile;
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
    public TileEntity createNewTileEntity(World worldIn, int metadata) {
        return new TileEntitySoulBrazier();
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
        setBlockBounds(0.175F, 0.0F, 0.175F, 0.925F, 1.0F, 0.925F);
        // super.setBlockBoundsBasedOnState(par1iBlockAccess, par2, par3, par4);
    }
}
