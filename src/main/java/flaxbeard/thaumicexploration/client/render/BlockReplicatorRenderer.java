package flaxbeard.thaumicexploration.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.block.BlockReplicator;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.block.BlockRenderer;

public class BlockReplicatorRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        IIcon side = ((BlockReplicator) block).icon[1];
        IIcon top = ((BlockReplicator) block).icon[0];
        IIcon spikes = ((BlockReplicator) block).icon[2];

        drawFaces(renderer, block, top, top, side, side, side, side, true);

        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        tessellator.startDrawingQuads();
        renderTopSpikes(renderer, block, 0, 0, 0, spikes, 0);
        tessellator.draw();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        renderer.renderStandardBlock(block, x, y, z);

        IIcon spikes = ((BlockReplicator) block).icon[2];

        float[] offsets = { 0.001F, 0.0F, -0.999F };
        for (float offset : offsets) {
            renderTopSpikes(renderer, block, x, y, z, spikes, offset);
        }
        return true;
    }

    private void renderTopSpikes(RenderBlocks renderer, Block block, int x, int y, int z, IIcon icon, float offset) {
        renderer.renderFaceXPos(block, x + offset, y + 1.0F, z, icon);
        renderer.renderFaceXNeg(block, x - offset, y + 1.0F, z, icon);
        renderer.renderFaceZPos(block, x, y + 1.0F, z + offset, icon);
        renderer.renderFaceZNeg(block, x, y + 1.0F, z - offset, icon);
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return modelId == ThaumicExploration.replicatorRenderID;
    }

    @Override
    public int getRenderId() {
        return ThaumicExploration.replicatorRenderID;
    }
}
