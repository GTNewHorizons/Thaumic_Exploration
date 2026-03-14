package flaxbeard.thaumicexploration.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.misc.TXUtils;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import thaumcraft.common.Thaumcraft;

/**
 * Created by nekosune on 03/08/14.
 */
public class BlockSoulBrazier extends BlockContainer {

    public BlockSoulBrazier() {
        super(Material.rock);
        setBlockTextureName("thaumicexploration:soulBrazier");
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int metadata) {
        return new TileEntitySoulBrazier();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntitySoulBrazier entity = ((TileEntitySoulBrazier) world.getTileEntity(x, y, z));
            String ownerUsername = entity.owner.getName();
            EntityPlayerMP player = TXUtils.getPlayerByUsername(ownerUsername);
            if (player != null) {
                Thaumcraft.proxy.getPlayerKnowledge().addWarpPerm(ownerUsername, entity.storedWarp);
                player.addChatComponentMessage(new ChatComponentTranslation("soulbrazier.returnWarp"));
            } else {
                TXUtils.addWarpPermOfflinePlayer(ownerUsername, entity.storedWarp);
            }
            ForgeChunkManager
                    .unforceChunk(entity.heldChunk, new ChunkCoordIntPair(entity.xCoord >> 4, entity.zCoord >> 4));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
        return ((TileEntitySoulBrazier) worldIn.getTileEntity(x, y, z)).setActive(player);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        ((TileEntitySoulBrazier) worldIn.getTileEntity(x, y, z)).owner = ((EntityPlayer) placer).getGameProfile();
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

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
        setBlockBounds(0.175F, 0.0F, 0.175F, 0.925F, 1.0F, 0.925F);
        // super.setBlockBoundsBasedOnState(par1iBlockAccess, par2, par3, par4);
    }
}
