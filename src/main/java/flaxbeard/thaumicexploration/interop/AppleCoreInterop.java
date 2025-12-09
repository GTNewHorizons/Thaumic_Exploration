package flaxbeard.thaumicexploration.interop;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;

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

    public static void setHunger(int hunger, EntityPlayer player) {
        AppleCoreAPI.mutator.setHunger(player, player.getFoodStats().getFoodLevel() + hunger);
        MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodStatsAddition(player, new FoodValues(hunger, 0)));
    }

    public static void setSaturation(float saturation, EntityPlayer player) {
        AppleCoreAPI.mutator.setSaturation(player, player.getFoodStats().getSaturationLevel() + saturation);
        // We only post the FoodStatsAddition event for nutrition, which ignores it if the hunger didn't increase
        // so there's no need to post it here
    }
}
