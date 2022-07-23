package flaxbeard.thaumicexploration.client.render;

import flaxbeard.thaumicexploration.client.render.model.ModelJarOverlay;
import flaxbeard.thaumicexploration.tile.TileEntityBoundJar;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.tile.TileJarRenderer;
import thaumcraft.common.tiles.TileJar;

public class TileEntityBoundJarRender extends TileJarRenderer {
    private ModelJarOverlay model = new ModelJarOverlay();
    private static final ResourceLocation overlayn =
            new ResourceLocation("thaumicexploration:textures/blocks/boundchestoverlaynone.png");
    private static final ResourceLocation overlay0 =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlay0.png");
    private static final ResourceLocation seal =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlayseal.png");
    private static final ResourceLocation overlay1 =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlay1.png");
    private static final ResourceLocation overlay2 =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlay2.png");
    private static final ResourceLocation overlay3 =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlay3.png");
    private static final ResourceLocation overlay4 =
            new ResourceLocation("thaumicexploration:textures/blocks/boundjaroverlay4.png");
    private static final ResourceLocation[] overlays = {overlayn, overlay0, overlay1, overlay2, overlay3, overlay4};

    public void renderTileEntityAt(TileJar tile, double x, double y, double z, float f) {
        super.renderTileEntityAt(tile, x, y, z, f);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + 0.5F, (float) z + 1);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        int j = ((TileEntityBoundJar) tile).getSealColor();
        float r = (float) (ItemDye.field_150922_c[j] >> 16 & 0xff) / 255F;
        float g = (float) (ItemDye.field_150922_c[j] >> 8 & 0xff) / 255F;
        float b = (float) (ItemDye.field_150922_c[j] & 0xff) / 255F;
        GL11.glColor4f(r, g, b, 1.0F);
        int ticks = ((TileEntityBoundJar) tile).getAccessTicks();
        if (ticks > 0) {
            double divisor = (80 / 6) + 0.0001;
            double frame = ((ticks - 1) / divisor) - 1;
            int trueFrame = (int) Math.ceil(frame + 0.5);
            if (trueFrame > 5) {
                trueFrame = 5;
            }
            if (trueFrame < 0) {
                trueFrame = 0;
            }
            this.bindTexture(overlays[trueFrame]);
        } else {
            this.bindTexture(overlays[0]);
        }
        this.model.renderAll();
        this.bindTexture(seal);
        this.model.renderAll();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
