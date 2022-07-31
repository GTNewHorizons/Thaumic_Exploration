package flaxbeard.thaumicexploration.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.block.BlockCrucibleSouls;
import flaxbeard.thaumicexploration.tile.TileEntityCrucibleSouls;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import thaumcraft.client.renderers.block.BlockRenderer;

public class BlockCrucibleSoulsRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(
                renderer,
                block,
                ((BlockCrucibleSouls) block).IIcon[2],
                ((BlockCrucibleSouls) block).IIcon[4],
                ((BlockCrucibleSouls) block).IIcon[3],
                ((BlockCrucibleSouls) block).IIcon[3],
                ((BlockCrucibleSouls) block).IIcon[3],
                ((BlockCrucibleSouls) block).IIcon[3],
                true);
    }

    public boolean renderWorldBlock(
            IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

        int metadata = world.getBlockMetadata(x, y, z);
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        TileEntityCrucibleSouls te = (TileEntityCrucibleSouls) world.getTileEntity(x, y, z);
        renderer.setRenderBoundsFromBlock(block);
        renderer.renderStandardBlock(block, x, y, z);
        IIcon outerSide = null;
        IIcon innerSide = ((BlockCrucibleSouls) block).IIcon[5];
        IIcon bottom = ((BlockCrucibleSouls) block).IIcon[6];
        if (te.drainTicks > 0) {
            outerSide = ((BlockCrucibleSouls) block).IIcon[7];
        } else {
            outerSide = ((BlockCrucibleSouls) block).IIcon[3];
        }

        float f5 = 0.123F;

        setBrightness(world, x, y, z, block);

        renderer.renderFaceXPos(block, x - 1.0F + f5, y, z, innerSide);
        renderer.renderFaceXNeg(block, x + 1.0F - f5, y, z, innerSide);
        renderer.renderFaceZPos(block, x, y, z - 1.0F + f5, innerSide);
        renderer.renderFaceZNeg(block, x, y, z + 1.0F - f5, innerSide);
        renderer.renderFaceYPos(block, x, y - 1.0F + 0.25F, z, bottom);
        renderer.renderFaceYNeg(block, x, y + 1.0F - 0.75F, z, bottom);

        renderer.renderFaceXPos(block, x, y, z, outerSide);
        renderer.renderFaceXNeg(block, x, y, z, outerSide);
        renderer.renderFaceZPos(block, x, y, z, outerSide);
        renderer.renderFaceZNeg(block, x, y, z, outerSide);

        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.setRenderBoundsFromBlock(block);
        return true;
    }

    public boolean shouldRender3DInInventory() {
        return true;
    }

    public int getRenderId() {
        return ThaumicExploration.crucibleSoulsRenderID;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        // TODO Auto-generated method stub
        return (modelId == ThaumicExploration.crucibleSoulsRenderID);
    }
}
