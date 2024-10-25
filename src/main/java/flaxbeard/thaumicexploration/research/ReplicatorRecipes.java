package flaxbeard.thaumicexploration.research;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.tuple.ImmutablePair;

import flaxbeard.thaumicexploration.common.ConfigTX;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

public class ReplicatorRecipes {

    private static final ArrayList<Item> allowedItemsWildcard = new ArrayList<>();
    private static final ArrayList<ImmutablePair<Item, Integer>> allowedItems = new ArrayList<>();
    private static final ArrayList<Item> forbiddenItems = new ArrayList<>();

    static {
        if (!ConfigTX.allowModWoodReplication) {
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.wooden_slab));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.birch_stairs));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.oak_stairs));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.jungle_stairs));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.spruce_stairs));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.log));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.log2));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.planks));
        }
        if (!ConfigTX.allowModStoneReplication) {
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.stone));
            allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.cobblestone));
        }
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.mossy_cobblestone));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.stone_stairs));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.sand));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.sandstone));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.sandstone_stairs));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.brick_block));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.brick_stairs));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.stonebrick));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.stone_brick_stairs));
        // allowedItems.add(Item.getItemFromBlock(Blocks.blockNetherQuartz),OreDictionary.WILDCARD_VALUE));
        // allowedItems.add(Item.getItemFromBlock(Blocks.stairsNetherQuartz),OreDictionary.WILDCARD_VALUE));
        // allowedItems.add(Item.getItemFromBlock(Blocks.stoneSingleSlab),7));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.nether_brick));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.nether_brick_stairs));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.soul_sand));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.gravel));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.glass));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.grass));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.dirt));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.snow));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.clay));
        allowedItemsWildcard.add(Item.getItemFromBlock(Blocks.hardened_clay));
        allowedItemsWildcard.trimToSize();
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone), 0));
        if (!ConfigTX.allowModWoodReplication && ConfigTX.allowMagicPlankReplication) {
            allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice), 6));
            allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice), 7));
        }
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 0));
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 3));
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 1));
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 5));
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 4));
        allowedItems.add(ImmutablePair.of(Item.getItemFromBlock(Blocks.stone_slab), 6));
        allowedItems.trimToSize();
        forbiddenItems.add(Item.getItemFromBlock(ConfigBlocks.blockMagicalLog));
        forbiddenItems.add(Item.getItemFromBlock(ConfigBlocks.blockMagicalLeaves));
        if (ConfigTX.allowModWoodReplication && !ConfigTX.allowMagicPlankReplication) {
            forbiddenItems.add(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice));
        }
        forbiddenItems.trimToSize();
    }

    public static boolean canStackBeReplicated(ItemStack stack) {
        Item item = stack.getItem();
        if (allowedItemsWildcard.contains(item)) {
            return true;
        }
        if (allowedItems.contains(ImmutablePair.of(item, stack.getItemDamage()))) {
            return true;
        }
        if (forbiddenItems.contains(item)) {
            return false;
        }
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int id : oreIDs) {
            String oreName = OreDictionary.getOreName(id);
            if (checkOreDictRules(oreName)) {
                AspectList ot = ThaumcraftCraftingManager.getObjectTags(stack);
                ot = ThaumcraftCraftingManager.getBonusTags(stack, ot);
                if (ot.getAspects().length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkOreDictRules(String oreName) {
        return oreName != null && (oreName.equals("logWood") || oreName.equals("treeLeaves")
                || oreName.equals("slabWood")
                || oreName.equals("stairWood")
                || ConfigTX.allowModStoneReplication && (oreName.equals("stone") || oreName.equals("cobblestone"))
                || ConfigTX.allowModWoodReplication && oreName.equals("plankWood"));
    }

}
