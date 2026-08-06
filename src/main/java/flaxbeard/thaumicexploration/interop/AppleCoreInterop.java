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
    }

    /**
     * Notifies that the food was actually consumed, so mods like Nutrition can grant its nutrients. Both events are
     * posted in the same order vanilla eating posts them, as listeners may expect them to come in pairs.
     */
    public static void postFoodEaten(ItemStack food, EntityPlayer player) {
        FoodValues values = AppleCoreAPI.accessor.getFoodValues(food);
        MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodStatsAddition(player, values));
        MinecraftForge.EVENT_BUS.post(
                new FoodEvent.FoodEaten(
                        player,
                        food,
                        values,
                        values.hunger,
                        values.hunger * values.saturationModifier * 2f));
    }

    public static void setSaturation(float saturation, EntityPlayer player) {
        AppleCoreAPI.mutator.setSaturation(player, player.getFoodStats().getSaturationLevel() + saturation);
    }
}
