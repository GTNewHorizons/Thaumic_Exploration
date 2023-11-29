package flaxbeard.thaumicexploration.integration;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;

import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.common.ConfigTX;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumic.tinkerer.common.enchantment.EnchantmentVampirism;
import thaumic.tinkerer.common.enchantment.core.EnchantmentManager;
import thaumic.tinkerer.common.lib.LibEnchantIDs;
import thaumic.tinkerer.common.lib.LibMisc;
import thaumic.tinkerer.common.lib.LibResearch;

public class TTIntegration {

    public static void registerEnchants() {
        if (ConfigTX.enchantmentBindingEnable) EnchantmentManager.registerExponentialCostData(
                ThaumicExploration.enchantmentBinding,
                "thaumicexploration:textures/tabs/binding.png",
                false,
                new AspectList().add(Aspect.ENTROPY, 15).add(Aspect.ORDER, 15),
                "ENCHBINDING");
        if (ConfigTX.enchantmentNVEnable) EnchantmentManager.registerExponentialCostData(
                ThaumicExploration.enchantmentNightVision,
                "thaumicexploration:textures/tabs/nightVision.png",
                false,
                new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 10).add(Aspect.ORDER, 20),
                "ENCHNIGHTVISION");
        if (ConfigTX.enchantmentDisarmEnable) EnchantmentManager.registerExponentialCostData(
                ThaumicExploration.enchantmentDisarm,
                "thaumicexploration:textures/tabs/disarm.png",
                false,
                new AspectList().add(Aspect.AIR, 12).add(Aspect.ORDER, 7).add(Aspect.ENTROPY, 7),
                "ENCHDISARM");
    }

    public static boolean canApplyTogether(Enchantment par1Enchantment, Enchantment par2Enchantment) {
        if (par2Enchantment == ThaumicExploration.enchantmentBinding)
            return !(par1Enchantment instanceof EnchantmentVampirism);
        return true;
    }

    public static boolean okVersion() {
        String ver = LibMisc.VERSION.substring(LibMisc.VERSION.lastIndexOf("-") + 1);
        int version = Integer.parseInt(ver);
        System.out.println("!THAUMIC TINKERER VERSION: " + version + "!");
        return (version > 71);
        // return false;
    }

    public static String keyRepairer() {
        return LibResearch.KEY_REPAIRER;
        // return "";
    }

    public static int getAscentLevel(EntityPlayer player) {
        int boost = EnchantmentHelper
                .getMaxEnchantmentLevel(LibEnchantIDs.idAscentBoost, player.inventory.armorInventory);
        return boost;
        // return 0;
    }

    public static String keyEnchanter() {
        return LibResearch.KEY_ENCHANTER;
        // return "";
    }
}
