package flaxbeard.thaumicexploration.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import flaxbeard.thaumicexploration.interop.AppleCoreInterop;
import flaxbeard.thaumicexploration.misc.FakePlayerPotion;
import thaumcraft.common.config.ConfigItems;

public class ItemFoodTalisman extends Item {

    private static final String[] ALL_FOOD_BLACKLIST_NAMES = { ConfigItems.itemManaBean.getUnlocalizedName(),
            ConfigItems.itemZombieBrain.getUnlocalizedName(), "item.foodstuff.0.name", "ic2.itemterrawart", };
    public static List<String> foodBlacklist = new ArrayList<String>();
    public static Map<String, Boolean> foodCache = new HashMap<String, Boolean>();
    private final int MAX_HEAL_SIZE_TALISMAN = 1000;
    private final int MAX_SAT_SIZE_TALISMAN = 1000;

    public ItemFoodTalisman() {
        super();
        this.maxStackSize = 1;
        this.setMaxDamage(MAX_HEAL_SIZE_TALISMAN);
        initFoodBlackList();
    }

    private void initFoodBlackList() {
        Collections.addAll(foodBlacklist, ALL_FOOD_BLACKLIST_NAMES);
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        setDefaultTags(par1ItemStack);
        par3List.add(
                "Currently holds " + (int) par1ItemStack.stackTagCompound.getFloat("food")
                        + " food points and "
                        + (int) par1ItemStack.stackTagCompound.getFloat("saturation")
                        + " saturation points.");
        // super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int slot, boolean isSelected) {

        if (!(entity instanceof EntityPlayer && entity.ticksExisted % 20 == 0)) return;

        EntityPlayer player = (EntityPlayer) entity;

        if (!world.isRemote) {
            setDefaultTags(itemStack);
        }

        if (!world.isRemote && (itemStack.stackTagCompound.getFloat("food") < MAX_HEAL_SIZE_TALISMAN
                || itemStack.stackTagCompound.getFloat("saturation") < MAX_SAT_SIZE_TALISMAN)) {
            for (int i = 0; i < 10; i++) {
                if (itemStack.stackTagCompound.getFloat("food") >= MAX_HEAL_SIZE_TALISMAN
                        && itemStack.stackTagCompound.getFloat("saturation") >= MAX_SAT_SIZE_TALISMAN) {
                    break;
                }
                if (player.inventory.getStackInSlot(i) == null) {
                    continue;
                }
                if (!isEdible(player.inventory.getStackInSlot(i), player)) {
                    continue;
                }
                ItemStack food = player.inventory.getStackInSlot(i);
                float sat;
                float heal;
                if (Loader.isModLoaded("AppleCore")) {
                    heal = AppleCoreInterop.getHeal(food);
                    sat = getSaturationFood(food, heal);
                } else {
                    sat = ((ItemFood) food.getItem()).func_150906_h(food) * 2;

                    heal = ((ItemFood) food.getItem()).func_150905_g(food);
                }

                if (itemStack.stackTagCompound.getFloat("food") + (int) heal >= MAX_HEAL_SIZE_TALISMAN) {
                    itemStack.stackTagCompound.setFloat("food", MAX_HEAL_SIZE_TALISMAN);
                } else {
                    itemStack.stackTagCompound
                            .setFloat("food", itemStack.stackTagCompound.getFloat("food") + (int) heal);
                }
                if (itemStack.stackTagCompound.getFloat("saturation") + sat >= MAX_SAT_SIZE_TALISMAN) {
                    itemStack.stackTagCompound.setFloat("saturation", MAX_SAT_SIZE_TALISMAN);
                } else {
                    itemStack.stackTagCompound
                            .setFloat("saturation", itemStack.stackTagCompound.getFloat("saturation") + sat);
                }

                if (food.stackSize <= 1) {
                    player.inventory.setInventorySlotContents(i, null);
                }
                player.inventory.decrStackSize(i, 1);

                world.playSoundAtEntity(
                        player,
                        "random.eat",
                        0.5F + 0.5F * (float) player.worldObj.rand.nextInt(2),
                        (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            }
        }
        if ((player.getFoodStats().getFoodLevel() < 20)
                && (MAX_HEAL_SIZE_TALISMAN - itemStack.stackTagCompound.getFloat("food")) > 0) {
            float sat = itemStack.stackTagCompound.getFloat("food");
            float finalSat = 0;
            if (20 - player.getFoodStats().getFoodLevel() < sat) {
                finalSat = sat - (20 - player.getFoodStats().getFoodLevel());
                sat = 20 - player.getFoodStats().getFoodLevel();
            }
            if (Loader.isModLoaded("AppleCore")) {
                AppleCoreInterop.setHunger((int) sat, player);
            } else if (!world.isRemote) {
                ObfuscationReflectionHelper.setPrivateValue(
                        FoodStats.class,
                        player.getFoodStats(),
                        (int) (player.getFoodStats().getFoodLevel() + sat),
                        "field_75127_a",
                        "foodLevel");
            }
            if (!world.isRemote) {
                itemStack.stackTagCompound.setFloat("food", finalSat);
                itemStack.setItemDamage(itemStack.getItemDamage());
            }
        }
        if ((player.getFoodStats().getSaturationLevel() < player.getFoodStats().getFoodLevel())
                && itemStack.stackTagCompound.getFloat("saturation") > 0) {
            float sat = itemStack.stackTagCompound.getFloat("saturation");
            float finalSat = 0;
            if (player.getFoodStats().getFoodLevel() - player.getFoodStats().getSaturationLevel() < sat) {
                finalSat = sat - (player.getFoodStats().getFoodLevel() - player.getFoodStats().getSaturationLevel());
                sat = player.getFoodStats().getFoodLevel() - player.getFoodStats().getSaturationLevel();
            }
            if (Loader.isModLoaded("AppleCore")) {
                AppleCoreInterop.setSaturation(sat, player);
            } else if (!world.isRemote) {
                ObfuscationReflectionHelper.setPrivateValue(
                        FoodStats.class,
                        player.getFoodStats(),
                        (player.getFoodStats().getFoodLevel() + sat),
                        "field_75125_b",
                        "foodSaturationLevel");
            }
            if (!world.isRemote) {
                itemStack.stackTagCompound.setFloat("saturation", finalSat);
                itemStack.setItemDamage(itemStack.getItemDamage());
            }
        }
        // TODO WIP shit
        itemStack.setItemDamage(itemStack.getMaxDamage() - ((int) itemStack.stackTagCompound.getFloat("food")));
        // itemStack.stackTagCompound.getFloat("food")
    }

    private void setDefaultTags(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("saturation")) {
            itemStack.stackTagCompound.setFloat("saturation", 0);
        }
        if (!itemStack.stackTagCompound.hasKey("food")) {
            itemStack.stackTagCompound.setFloat("food", 0);
        }
    }

    private float getSaturationFood(ItemStack food, float heal) {
        return AppleCoreInterop.getSaturation(food) * 2f * heal;
    }

    private boolean isEdible(ItemStack food, EntityPlayer player) {
        String foodName = food.getUnlocalizedName();

        if (foodCache.containsKey(foodName.toLowerCase())) {
            return foodCache.get(foodName.toLowerCase());
        }
        for (String item : foodBlacklist) {
            if (item.equalsIgnoreCase(foodName)) {
                foodCache.put(foodName.toLowerCase(), false);
                return false;
            }
        }

        if (Loader.isModLoaded("AppleCore")) {
            try {
                if (AppleCoreInterop.getHeal(food) > 0) {
                    foodCache.put(foodName.toLowerCase(), true);
                    return true;
                }
            } catch (Exception e) {
                foodCache.put(foodName.toLowerCase(), false);
                return false;
            }
        }

        if (food.getItem() instanceof ItemFood) {
            try {

                for (int i = 1; i < 25; i++) {
                    EntityPlayer fakePlayer = new FakePlayerPotion(
                            player.worldObj,
                            new GameProfile(null, "foodTabletPlayer"));
                    fakePlayer.setPosition(0.0F, 999.0F, 0.0F);
                    ((ItemFood) food.getItem()).onEaten(food.copy(), player.worldObj, fakePlayer);
                    if (Loader.isModLoaded("HungerOverhaul")) {
                        if (fakePlayer.getActivePotionEffects().size() > 1) {
                            foodCache.put(foodName.toLowerCase(), false);
                            return false;
                        } else if (fakePlayer.getActivePotionEffects().size() == 1) {
                            Class<?> clazz = Class.forName("iguanaman.hungeroverhaul.HungerOverhaul");
                            Field fields = clazz.getField("potionWellFed");
                            Potion effect = (Potion) fields.get(null);
                            if (effect != null) {
                                if (fakePlayer.getActivePotionEffect(effect) == null) {
                                    foodCache.put(foodName.toLowerCase(), false);
                                    return false;
                                }
                            }
                        }
                    } else {
                        if (fakePlayer.getActivePotionEffects().size() > 0) {
                            foodCache.put(foodName.toLowerCase(), false);
                            return false;
                        }
                    }
                }
                foodCache.put(foodName.toLowerCase(), true);
                return true;
            } catch (Exception e) {
                foodCache.put(foodName.toLowerCase(), false);
                return false;
            }
        }
        foodCache.put(foodName.toLowerCase(), false);
        return false;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.uncommon; //
    }
}
