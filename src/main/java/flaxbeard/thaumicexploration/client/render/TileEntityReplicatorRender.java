package flaxbeard.thaumicexploration.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import flaxbeard.thaumicexploration.tile.TileEntityReplicator;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class TileEntityReplicatorRender extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "thaumicexploration:textures/blocks/replicatorRunes.png");

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        TileEntityReplicator replicator = (TileEntityReplicator) tile;
        ItemStack stack = replicator.getStackInSlot(0);

        if (stack == null || replicator.templateEssentia.size() == 0) return;

        if (replicator.crafting || replicator.ticksLeft > 0) {
            renderAspectRunes(replicator, x, y, z);
        }

        if (replicator.validLocation()) {
            renderFloatingItem(stack, x, y, z, partialTicks, replicator.crafting, replicator.ticksLeft);
        }
    }

    private void renderFloatingItem(ItemStack stack, double x, double y, double z, float partialTicks, boolean crafting,
            int ticksLeft) {
        Minecraft mc = Minecraft.getMinecraft();
        float ticks = mc.renderViewEntity.ticksExisted + partialTicks;
        float hover = MathHelper.sin(ticks % 32767.0F / 16.0F) * 0.05F;
        float scale = (stack.getItem() instanceof ItemBlock) ? 2.0F : 1.0F;
        if (crafting) scale *= (100F - ticksLeft) / 100F;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.15F + hover, (float) z + 0.5F);
        GL11.glRotatef(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(scale, scale, scale);

        EntityItem entityItem = new EntityItem(mc.theWorld, 0.0D, 0.0D, 0.0D, stack.copy());
        entityItem.hoverStart = 0.0F;

        if (!crafting && stack.stackSize == 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
        }

        RenderManager.instance.renderEntityWithPosYaw(entityItem, 0, 0, 0, 0, 0);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void renderAspectRunes(TileEntityReplicator replicator, double x, double y, double z) {
        Tessellator tessellator = Tessellator.instance;

        final int TEX_H = 16;
        final int RUNE_TOP_PX = 3;
        final int RUNE_HEIGHT_PX = 10;
        final int RUNE_BOTTOM_PX = RUNE_TOP_PX + RUNE_HEIGHT_PX;

        final float runeHeight = RUNE_HEIGHT_PX / (float) TEX_H; // 10 / 16
        final float runeBottom = (1.0f - runeHeight) / 2.0f;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        bindTexture(TEXTURE);

        GL11.glDisable(GL11.GL_LIGHTING);

        for (int i = 0; i < 4; i++) {
            Aspect aspect = selectAspectForRender(replicator.templateEssentia, i);

            float current = replicator.templateEssentia.getAmount(aspect);
            float required = replicator.requiredEssentia.getAmount(aspect);

            float fillRatio = current <= 0 ? 0f : MathHelper.clamp_float((current - required) / current, 0f, 1f);

            int filledPixels = MathHelper.clamp_int(Math.round(fillRatio * RUNE_HEIGHT_PX), 0, RUNE_HEIGHT_PX);

            if (filledPixels == 0) {
                GL11.glRotatef(90F, 0, 1, 0);
                continue;
            }

            float yFill = runeBottom + filledPixels / (float) TEX_H;

            float vBottom = RUNE_BOTTOM_PX / (float) TEX_H;
            float vFill = (RUNE_BOTTOM_PX - filledPixels) / (float) TEX_H;

            tessellator.startDrawingQuads();
            tessellator.setBrightness(0xF000F0);
            tessellator.setColorOpaque_I(aspect.getColor());

            tessellator.addVertexWithUV(0.5, yFill, -0.501, 0, vFill);
            tessellator.addVertexWithUV(0.5, runeBottom, -0.501, 0, vBottom);
            tessellator.addVertexWithUV(-0.5, runeBottom, -0.501, 1, vBottom);
            tessellator.addVertexWithUV(-0.5, yFill, -0.501, 1, vFill);

            tessellator.draw();

            GL11.glRotatef(90F, 0, 1, 0);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private Aspect selectAspectForRender(AspectList list, int index) {
        Aspect[] aspects = list.getAspects();
        if (aspects.length == 0) return null;
        return aspects[index % aspects.length];
    }
}
