package flaxbeard.thaumicexploration.interop;

import net.minecraft.item.ItemStack;

import squeek.applecore.api.AppleCoreAPI;

/**
 * Created by Katrina on 28/06/2015.
 */
public class AppleCoreInterop {

    public static int getHeal(ItemStack itemStack) {
        return AppleCoreAPI.accessor.getFoodValues(itemStack).hunger;
    }

    public static float getSaturation(ItemStack itemStack) {
        return AppleCoreAPI.accessor.getFoodValues(itemStack).saturationModifier;
    }
}
