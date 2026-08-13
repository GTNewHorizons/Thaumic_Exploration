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
import net.minecraft.nbt.NBTTagList;
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
    private static final String FOOD_QUEUE_TAG = "foodQueue";
    public static List<String> foodBlacklist = new ArrayList<String>();
    public static Map<String, Boolean> foodCache = new HashMap<String, Boolean>();
    private final int MAX_NOURISHMENT_SIZE_TALISMAN = 1000;

    public ItemFoodTalisman() {
        super();
        this.maxStackSize = 1;
        this.setMaxDamage(MAX_NOURISHMENT_SIZE_TALISMAN);
        initFoodBlackList();
    }

    private void initFoodBlackList() {
        Collections.addAll(foodBlacklist, ALL_FOOD_BLACKLIST_NAMES);
    }

    @Override
    public void addInformation(ItemStack talisman, EntityPlayer player, List tooltip, boolean isSelected) {
        setDefaultTags(talisman);
        tooltip.add(
                "Currently holds " + talisman.stackTagCompound.getInteger("nourishment") + " points of nourishment.");
        // super.addInformation(talisman, player, par3List, isSelected);
    }

    @Override
    public void onUpdate(ItemStack talisman, World world, Entity entity, int slot, boolean isSelected) {
        if (!(entity instanceof EntityPlayer player) || entity.ticksExisted % 20 != 0) {
            return;
        }

        if (!world.isRemote) {
            setDefaultTags(talisman);
            tryAbsorbFood(talisman, player, world);

            int before = talisman.stackTagCompound.getInteger("nourishment");
            tryFeedPlayer(talisman, player);
            consumeQueuedFood(talisman, player, before - talisman.stackTagCompound.getInteger("nourishment"));

            talisman.setItemDamage(talisman.getMaxDamage() - talisman.stackTagCompound.getInteger("nourishment"));
        }
    }

    private void tryAbsorbFood(ItemStack talisman, EntityPlayer player, World world) {
        int nourishment = talisman.stackTagCompound.getInteger("nourishment");
        if (nourishment >= MAX_NOURISHMENT_SIZE_TALISMAN) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack food = player.inventory.getStackInSlot(i);
            if (food == null || !isEdible(food, player)) {
                continue;
            }

            float heal;
            float saturation;
            if (Loader.isModLoaded("AppleCore")) {
                heal = AppleCoreInterop.getHeal(food);
                saturation = AppleCoreInterop.getSaturation(food) * 2f * heal;
            } else {
                heal = ((ItemFood) food.getItem()).func_150905_g(food);
                saturation = ((ItemFood) food.getItem()).func_150906_h(food) * 2;
            }

            int foodNourishment = Math.round((saturation + heal) / 2);
            int newNourishment = Math.min(nourishment + foodNourishment, MAX_NOURISHMENT_SIZE_TALISMAN);
            queueFood(talisman, food, newNourishment - nourishment);
            nourishment = newNourishment;
            talisman.stackTagCompound.setInteger("nourishment", nourishment);

            if (food.stackSize <= 1) {
                player.inventory.setInventorySlotContents(i, null);
            }
            player.inventory.decrStackSize(i, 1);

            world.playSoundAtEntity(
                    player,
                    "random.eat",
                    0.5F + 0.5F * world.rand.nextInt(2),
                    (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
            break; // to only eat one food per update
        }
    }

    private void tryFeedPlayer(ItemStack talisman, EntityPlayer player) {
        FoodStats food = player.getFoodStats();
        int nourishment = talisman.stackTagCompound.getInteger("nourishment");

        int hungerDeficit = 20 - food.getFoodLevel();
        if (hungerDeficit > 0 && nourishment > 0) {
            int toFeed = Math.min(nourishment, hungerDeficit);
            nourishment -= toFeed;

            if (Loader.isModLoaded("AppleCore")) {
                AppleCoreInterop.setHunger(toFeed, player);
            } else {
                ObfuscationReflectionHelper.setPrivateValue(
                        FoodStats.class,
                        food,
                        food.getFoodLevel() + toFeed,
                        "field_75127_a",
                        "foodLevel");
            }
        }

        float saturationDeficit = food.getFoodLevel() - food.getSaturationLevel();
        if (saturationDeficit > 0 && nourishment > 0) {
            float toSaturate = Math.min(nourishment, saturationDeficit);
            nourishment -= Math.round(toSaturate);

            if (Loader.isModLoaded("AppleCore")) {
                AppleCoreInterop.setSaturation(toSaturate, player);
            } else {
                ObfuscationReflectionHelper.setPrivateValue(
                        FoodStats.class,
                        food,
                        food.getSaturationLevel() + toSaturate,
                        "field_75125_b",
                        "foodSaturationLevel");
            }
        }

        talisman.stackTagCompound.setInteger("nourishment", Math.max(nourishment, 0));
    }

    /**
     * Remembers which food produced the stored nourishment, so that nutrient-tracking mods can be notified only when
     * that nourishment is actually spent on the player.
     */
    private void queueFood(ItemStack talisman, ItemStack food, int points) {
        if (points <= 0 || !Loader.isModLoaded("AppleCore")) {
            return;
        }

        ItemStack single = food.copy();
        single.stackSize = 1;

        NBTTagList queue = talisman.stackTagCompound.getTagList(FOOD_QUEUE_TAG, 10);
        for (int i = 0; i < queue.tagCount(); i++) {
            NBTTagCompound entry = queue.getCompoundTagAt(i);
            ItemStack queued = ItemStack.loadItemStackFromNBT(entry.getCompoundTag("item"));
            if (entry.getInteger("points") == points && ItemStack.areItemStacksEqual(queued, single)) {
                entry.setInteger("units", entry.getInteger("units") + 1);
                talisman.stackTagCompound.setTag(FOOD_QUEUE_TAG, queue);
                return;
            }
        }

        NBTTagCompound item = new NBTTagCompound();
        single.writeToNBT(item);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setTag("item", item);
        entry.setInteger("points", points);
        entry.setInteger("units", 1);
        queue.appendTag(entry);
        talisman.stackTagCompound.setTag(FOOD_QUEUE_TAG, queue);
    }

    /**
     * Posts one FoodEaten event per queued food that the given amount of spent nourishment covers.
     */
    private void consumeQueuedFood(ItemStack talisman, EntityPlayer player, int spent) {
        if (spent <= 0 || !Loader.isModLoaded("AppleCore")) {
            return;
        }

        NBTTagList queue = talisman.stackTagCompound.getTagList(FOOD_QUEUE_TAG, 10);
        while (spent > 0 && queue.tagCount() > 0) {
            NBTTagCompound entry = queue.getCompoundTagAt(0);
            int points = entry.getInteger("points");
            int units = entry.getInteger("units");
            if (points <= 0 || units <= 0) {
                queue.removeTag(0);
                continue;
            }

            int progress = entry.getInteger("progress") + spent;
            int eaten = Math.min(progress / points, units);
            if (eaten > 0) {
                ItemStack food = ItemStack.loadItemStackFromNBT(entry.getCompoundTag("item"));
                if (food != null) {
                    for (int i = 0; i < eaten; i++) {
                        AppleCoreInterop.postFoodEaten(food, player);
                    }
                }
            }

            int leftover = progress - eaten * points;
            if (eaten >= units) {
                queue.removeTag(0);
                spent = leftover; // carry the remainder over to the next queued food
            } else {
                entry.setInteger("units", units - eaten);
                entry.setInteger("progress", leftover);
                spent = 0;
            }
        }
        talisman.stackTagCompound.setTag(FOOD_QUEUE_TAG, queue);
    }

    private void setDefaultTags(ItemStack talisman) {
        NBTTagCompound tags = talisman.stackTagCompound;
        if (tags == null) {
            talisman.setTagCompound(new NBTTagCompound());
            tags = talisman.stackTagCompound;
        }
        if (!tags.hasKey("nourishment")) {
            tags.setInteger("nourishment", 0);
        }
        if (tags.hasKey("food") || tags.hasKey("saturation")) {
            float oldFood = tags.getFloat("food");
            float oldSaturation = tags.getFloat("saturation");
            int newNourishment = Math.round((oldFood + oldSaturation) / 2);
            int nourishment = Math.min(newNourishment, MAX_NOURISHMENT_SIZE_TALISMAN);

            tags.setInteger("nourishment", nourishment);
            tags.removeTag("food");
            tags.removeTag("saturation");
        }
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
