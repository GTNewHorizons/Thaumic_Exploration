package flaxbeard.thaumicexploration.interop;

import net.minecraft.entity.player.EntityPlayer;
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

    public static void setHunger(int hunger, EntityPlayer player) {
        AppleCoreAPI.mutator.setHunger(player, player.getFoodStats().getFoodLevel() + hunger);
    }

    public static void setSaturation(float saturation, EntityPlayer player) {
        AppleCoreAPI.mutator.setSaturation(player, player.getFoodStats().getSaturationLevel() + saturation);
    }
}
