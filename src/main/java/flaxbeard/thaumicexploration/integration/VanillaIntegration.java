package flaxbeard.thaumicexploration.integration;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.world.World;

public class VanillaIntegration {
    public static boolean isVanillaCauldron(Block block) {
        return block.getClass().equals(BlockCauldron.class);
    }

    public static boolean needsWater(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        return metadata < 3;
    }

    public static void fillWater(BlockCauldron cauldron, World world, int x, int y, int z) {
        cauldron.func_150024_a(world, x, y, z, 3);
    }
}
