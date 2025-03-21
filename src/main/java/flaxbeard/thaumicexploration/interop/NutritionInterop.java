package flaxbeard.thaumicexploration.interop;

import net.minecraft.entity.player.EntityPlayer;

import ca.wescook.nutrition.data.NutrientManager;
import ca.wescook.nutrition.data.PlayerDataHandler;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.nutrients.NutrientUtils;

public class NutritionInterop {

    // Adapted from ca.wescook.nutrition.network.PacketNormalizeServerNutrients.Handler::onMessage
    // and ca.wescook.nutrition.events.EventWorldTick::clientTickEvent
    public static void addNutritionFromHunger(EntityPlayer player, int hunger) {
        NutrientManager manager = PlayerDataHandler.getForPlayer(player);
        float nutrientDelta = NutrientUtils.getNutrientValue(hunger, NutrientList.get().size());
        for (Nutrient nutrient : NutrientList.get()) {
            Float currentValue = manager.get(nutrient);
            if (currentValue > 50f) {
                manager.set(nutrient, Math.max(50f, currentValue - nutrientDelta));
            } else if (currentValue < 50f) {
                manager.set(nutrient, Math.min(50f, currentValue + nutrientDelta));
            }
        }
    }
}
