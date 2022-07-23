package flaxbeard.thaumicexploration.common;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

public class ConfigTX {

    public static int potionTaintWithdrawlID;
    public static int potionBindingID;
    public static int enchantmentBindingID;
    public static int enchantmentNightVisionID;
    public static int enchantmentDisarmID;

    public static boolean enchantmentBindingEnable;
    public static boolean enchantmentNVEnable;
    public static boolean enchantmentDisarmEnable;

    public static boolean prefix;
    public static boolean breadWand;
    public static boolean brainsGolem;
    public static boolean allowBoundInventories;
    public static boolean allowReplication;
    public static boolean allowCrucSouls;
    public static boolean allowThinkTank;
    public static boolean allowFood;
    public static boolean allowUrnWater;
    public static boolean allowUrnLava;
    public static boolean allowBoots;
    public static boolean allowBootsIce;
    public static boolean allowSojourner;
    public static boolean allowMechanist;
    public static boolean allowEnchants;
    public static boolean allowTainturgy;
    public static boolean allowSBChunkLoading;
    public static boolean allowMagicPlankReplication;
    public static boolean allowModWoodReplication;
    public static boolean allowModStoneReplication;
    public static boolean allowEverfullUrnFillNearby;
    public static boolean allowThaumcraftCrucibleRefill;
    public static boolean allowThaumcraftSpaRefill;
    public static boolean allowVanillaCauldronRefill;
    public static boolean allowBotaniaApothecaryPetalRefill;
    public static boolean allowWitcheryCauldronRefill;
    public static boolean allowWitcheryKettleRefill;
    // public static boolean voidJarLightning;

    static Configuration config;

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        potionTaintWithdrawlID = config.get("Potion", "Taint Withdrawl", 32).getInt();
        potionBindingID = config.get("Potion", "Binding", 31).getInt();

        enchantmentBindingID = config.get("Enchantment", "Binding", 77).getInt();
        enchantmentNightVisionID = config.get("Enchantment", "Night Vision", 78).getInt();
        enchantmentDisarmID = config.get("Enchantment", "Disarming", 79).getInt();

        enchantmentBindingEnable =
                config.get("Enchantment", "Binding enabled", true).getBoolean(true);
        enchantmentNVEnable =
                config.get("Enchantment", "Night Vision enabled", true).getBoolean(true);
        enchantmentDisarmEnable =
                config.get("Enchantment", "Disarm enabled", true).getBoolean(true);

        // allowOsmotic = config.get("Miscellaneous", "Add new enchantments to Thaumic Tinkerer's Osmotic Enchanter
        // (Requires TT Build 72+)", true).getBoolean(true);
        prefix = config.get("Miscellaneous", "Display [TX] prefix before Thaumic Exploration research", true)
                .getBoolean(true);
        breadWand =
                config.get("Easter Eggs", "Enable Thaumic Frenchurgy", false).getBoolean(false);
        brainsGolem = config.get("Miscellaneous", "Use Purified Brains in advanced golems", true)
                .getBoolean(true);
        // taintBloom = config.get("Miscellaneous", "Move the Etheral Bloom to the Tainturgy tab",
        // true).getBoolean(true);
        allowBoundInventories =
                config.get("Miscellaneous", "Enable bound inventories", true).getBoolean(true);
        allowReplication =
                config.get("Miscellaneous", "Enable Thaumic Replicator", true).getBoolean(true);
        allowCrucSouls =
                config.get("Miscellaneous", "Enable Crucible of Souls", true).getBoolean(true);
        allowThinkTank = config.get("Miscellaneous", "Enable Think Tank", true).getBoolean(true);
        allowFood = config.get("Miscellaneous", "Enable Talisman of Nourishment", true)
                .getBoolean(true);
        allowUrnLava = config.get("Miscellaneous", "Enable Everburn Urn", true).getBoolean(true);
        allowUrnWater = config.get("Miscellaneous", "Enable Everfull Urn", true).getBoolean(true);
        allowBoots = config.get("Miscellaneous", "Enable Boots of the Meteor/Comet", true)
                .getBoolean(true);
        allowBootsIce = config.get("Miscellaneous", "Allow Boots of the Comet to create ice", true)
                .getBoolean(true);
        allowSojourner = config.get("Miscellaneous", "Enable Sojourner's Wand Caps", true)
                .getBoolean(true);
        allowMechanist = config.get("Miscellaneous", "Enable Mechanist's Wand Caps", true)
                .getBoolean(true);
        allowEnchants =
                config.get("Miscellaneous", "Enable TX Enchantments", true).getBoolean(true);
        allowTainturgy =
                config.get("Miscellaneous", "Enable Wispy Dreamcatcher", true).getBoolean(true);
        allowSBChunkLoading = config.get("Miscellaneous", "Enable Soul Brazier Chunkloading", true)
                .getBoolean(true);
        allowMagicPlankReplication = config.get("Replicator", "Allow replication of Greatwood/Silverwood planks", true)
                .getBoolean(true);
        allowModWoodReplication = config.get("Replicator", "Allow replication of other mods' logs and planks", true)
                .getBoolean(true);
        allowModStoneReplication = config.get("Replicator", "Allow replication of other mods' stone blocks", true)
                .getBoolean(true);

        allowEverfullUrnFillNearby = config.get(
                        "EverfullUrn",
                        "Allow the Everfull Urn to automatically fill up supported nearby containers. Overrides all other container settings",
                        true)
                .getBoolean(true);
        allowThaumcraftCrucibleRefill = config.get(
                        "EverfullUrn", "Allow the Everfull Urn to fill up Thaumcraft Crucibles", true)
                .getBoolean(true);
        allowThaumcraftSpaRefill = config.get("EverfullUrn", "Allow the Everfull Urn to fill up Thaumcraft Spas", true)
                .getBoolean(true);
        allowVanillaCauldronRefill = config.get(
                        "EverfullUrn", "Allow the Everfull Urn to fill up vanilla Cauldrons", true)
                .getBoolean(true);
        allowBotaniaApothecaryPetalRefill = config.get(
                        "EverfullUrn", "Allow the Everfull Urn to fill up Botania Petal Apothecaries", true)
                .getBoolean(true);
        allowWitcheryCauldronRefill = config.get(
                        "EverfullUrn", "Allow the Everfull Urn to fill up Witchery Cauldrons", true)
                .getBoolean(true);
        allowWitcheryKettleRefill = config.get(
                        "EverfullUrn", "Allow the Everfull Urn to fill up Witchery Kettles", true)
                .getBoolean(true);

        // voidJarLightning = config.get("Miscellaneous", "Enable effects on oblivion jar", false).getBoolean(false);

        config.save();
    }
}
