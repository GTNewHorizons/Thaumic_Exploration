package flaxbeard.thaumicexploration.client.render;

import flaxbeard.thaumicexploration.client.render.model.ModelSoulBrazier;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;

/**
 * Created by nekosune on 03/08/14.
 */
public class TileEntitySoulBrazierRenderer extends TileEntitySpecialRenderer {
    private ModelSoulBrazier brazierModel = new ModelSoulBrazier();
    private static final ResourceLocation baseTexture =
            new ResourceLocation("thaumicexploration:textures/models/soulBrazier.png");

    @Override
    public void renderTileEntityAt(TileEntity tile, double d0, double d1, double d2, float par8) {
        // TODO Auto-generated method stub

        TileEntitySoulBrazier brazier = (TileEntitySoulBrazier) tile;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d0 + 0.5F, (float) d1 + 1.5f, (float) d2 + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.bindTexture(baseTexture);
        this.brazierModel.render(null, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);

        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glPopMatrix();
        // this.brazierModel.renderAll(0.0625F);
        if (brazier.checkPower()) {
            GL11.glPushMatrix();
            GL11.glAlphaFunc(516, 0.003921569F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 1);
            GL11.glDepthMask(false);
            float alpha = MathHelper.sin(Minecraft.getMinecraft().renderViewEntity.ticksExisted / 8.0F) * 0.1F + 0.5F;
            UtilsFX.bindTexture("textures/misc/node_bubble.png");

            int count = brazier.count % 60 == 0 || (brazier.count - 1) % 60 == 0
                    ? 7
                    : 37; // PERHAPS THIS COULD BE THE LIGHTNING

            UtilsFX.renderFacingQuad(
                    tile.xCoord + 0.5D,
                    tile.yCoord + 1.5D,
                    tile.zCoord + 0.5D,
                    0.0F,
                    0.7F, // SIZE
                    count / 37.0F * alpha * 4.0F, // ALPHA
                    1,
                    0,
                    par8,
                    11665663); // COLOR
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glAlphaFunc(516, 0.1F);
            GL11.glPopMatrix();
        }
    }
}
