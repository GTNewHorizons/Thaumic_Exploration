package flaxbeard.thaumicexploration.integration.nei;

import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;
import static thaumcraft.codechicken.lib.render.CCRenderState.changeTexture;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.ItemList;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import flaxbeard.thaumicexploration.research.ReplicatorRecipes;

public class ReplicatorHandler extends TemplateRecipeHandler {

    private static List<ItemStack> validItems = new ArrayList<>();
    private static final int COLUMNS = 9;
    private static final int ROWS_PER_PAGE = 6;
    private static final int ITEMS_PER_PAGE = COLUMNS * ROWS_PER_PAGE;

    private class CachedReplicatorRecipe extends CachedRecipe {

        private Point focus;
        private final List<PositionedStack> items = new ArrayList<>();

        public CachedReplicatorRecipe(ItemStack focusStack, int startIndex, int endIndex) {
            int row = 0;
            int col = 0;

            for (int i = startIndex; i < endIndex && i < validItems.size(); i++) {
                ItemStack stack = validItems.get(i);

                int xPos = 3 + 18 * col;
                int yPos = 2 + 18 * row;

                this.items.add(new PositionedStack(stack, xPos, yPos));

                if (NEIClientUtils.areStacksSameTypeCrafting(focusStack, stack)) {
                    this.focus = new Point(xPos - 1, yPos - 1);
                }

                col++;
                if (col >= COLUMNS) {
                    col = 0;
                    row++;
                }
            }
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return items;
        }
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            buildPages(null);
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (ItemStack item : validItems) {
            if (NEIClientUtils.areStacksSameType(item, result)) buildPages(result);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        loadCraftingRecipes(ingredient);
    }

    private void buildPages(ItemStack focusStack) {
        int total = validItems.size();

        for (int start = 0; start < total; start += ITEMS_PER_PAGE) {
            int end = start + ITEMS_PER_PAGE;
            arecipes.add(new CachedReplicatorRecipe(focusStack, start, end));
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(2, 1, 0, 0, 162, 108);

        CachedReplicatorRecipe recipe = (CachedReplicatorRecipe) this.arecipes.get(recipeIndex);
        Point focus = recipe.focus;
        if (focus != null) {
            GuiDraw.drawTexturedModalRect(focus.x, focus.y, 162, 0, 18, 18);
        }
    }

    // This is needed to pull up the handler when finding the usages of the Thaumic Replicator.
    @Override
    public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(new Rectangle(0, 0, 0, 0), "ThaumicExploration.replicator"));
    }

    @Override
    public String getOverlayIdentifier() {
        return "ThaumicExploration.replicator";
    }

    @Override
    public String getGuiTexture() {
        return "ThaumicExploration:textures/gui/nei/replicator.png";
    }

    @Override
    public String getRecipeName() {
        return I18n.format("te.nei.replicator");
    }

    public static void init() {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : ItemList.items) {
            if (ReplicatorRecipes.canStackBeReplicated(stack)) {
                result.add(stack);
            }
        }

        if (!result.isEmpty()) {
            validItems = result;
            return;
        }

        for (Object obj : Item.itemRegistry) {
            Item item = (Item) obj;

            List<ItemStack> subItems = new ArrayList<>();
            try {
                item.getSubItems(item, null, subItems);
            } catch (Exception e) {
                continue;
            }

            if (subItems.isEmpty()) {
                subItems.add(new ItemStack(item));
            }

            for (ItemStack stack : subItems) {
                if (ReplicatorRecipes.canStackBeReplicated(stack)) {
                    result.add(stack);
                }
            }
        }

        validItems = result;
    }
}
