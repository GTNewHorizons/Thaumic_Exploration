package flaxbeard.thaumicexploration.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;
import flaxbeard.thaumicexploration.ThaumicExploration;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.items.armor.Hover;
import thaumicboots.api.IBoots;

@Interface(iface = "thaumicboots.api.IBoots", modid = "thaumicboots")
public class ItemTXArmorSpecial extends ItemArmor implements IRepairable, IRunicArmor, IBoots {

    public double jumpBonus = 0.2750000059604645D;

    public ItemTXArmorSpecial(ItemArmor.ArmorMaterial par2EnumArmorMaterial, int par3, int par4) {
        super(par2EnumArmorMaterial, par3, par4);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String layer) {
        if (stack.getItem() == ThaumicExploration.bootsMeteor)
            return "thaumicexploration:textures/models/armor/bootsMeteor.png";
        return "thaumicexploration:textures/models/armor/bootsComet.png";
    }

    @Override
    public EnumRarity getRarity(ItemStack par1ItemStack) {
        return EnumRarity.rare;
    }

    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return par2ItemStack.isItemEqual(new ItemStack(Items.leather)) ? true
                : super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (player.inventory.armorItemInSlot(0).getItem() == ThaumicExploration.bootsMeteor) {
            if (player.fallDistance > 0.0F) {
                player.fallDistance = 0.0F;
            }
        }
        if (getIntertialState(itemStack) && player.moveForward == 0
                && player.moveStrafing == 0
                && player.capabilities.isFlying) {
            player.motionX *= 0.5;
            player.motionZ *= 0.5;
        }
        boolean omniMode = false;
        if (ThaumicExploration.isBootsActive) {
            omniMode = isOmniEnabled(itemStack);
            if ((player.moveForward == 0F && player.moveStrafing == 0F && omniMode)
                    || (player.moveForward <= 0F && !omniMode)) {
                return;
            }
        }
        if (player.moveForward != 0.0F || player.moveStrafing != 0.0F) {
            int haste = EnchantmentHelper
                    .getEnchantmentLevel(Config.enchHaste.effectId, player.inventory.armorItemInSlot(0));
            Item item = player.inventory.armorItemInSlot(0).getItem();
            if (item instanceof ItemTXArmorSpecial) {
                if (player.worldObj.isRemote) {
                    if (!Thaumcraft.instance.entityEventHandler.prevStep
                            .containsKey(Integer.valueOf(player.getEntityId()))) {
                        Thaumcraft.instance.entityEventHandler.prevStep
                                .put(Integer.valueOf(player.getEntityId()), Float.valueOf(player.stepHeight));
                    }
                    player.stepHeight = 1.0F;
                }
            }

            if (item == ThaumicExploration.bootsMeteor) {
                float bonus = 0.055F;
                movementEffects(player, bonus, itemStack);

                // This seems to be redundant ???
                if (player.fallDistance > 0.0F) {
                    player.fallDistance = 0.0F;
                }
            } else if (item == ThaumicExploration.bootsComet) {
                if (!player.inventory.armorItemInSlot(0).hasTagCompound()) {
                    NBTTagCompound par1NBTTagCompound = new NBTTagCompound();
                    player.inventory.armorItemInSlot(0).setTagCompound(par1NBTTagCompound);
                    player.inventory.armorItemInSlot(0).stackTagCompound.setInteger("runTicks", 0);
                }
                int ticks = player.inventory.armorItemInSlot(0).stackTagCompound.getInteger("runTicks");
                float bonus = 0.110F;
                bonus = bonus + ((ticks / 5) * 0.003F);
                movementEffects(player, bonus, itemStack);
                if (player.fallDistance > 0.25F) {
                    player.fallDistance -= 0.25F;
                }
            }
        }
    }

    public void movementEffects(EntityPlayer player, float bonus, ItemStack itemStack) {
        float speedMod = (float) getSpeedModifier(itemStack);
        if (player.isInWater()) {
            bonus /= 4.0F;
        }
        if (player.onGround || player.isOnLadder() || player.capabilities.isFlying) {
            bonus *= speedMod;
            if (ThaumicExploration.isBootsActive) {
                applyOmniState(player, bonus, itemStack);
            } else if (player.moveForward > 0F) {
                player.moveFlying(0.0F, player.moveForward, bonus);
            }
        } else if (Hover.getHover(player.getEntityId())) {
            player.jumpMovementFactor = 0.03F;
        } else {
            player.jumpMovementFactor = 0.05F;
        }
    }

    public int getRunicCharge(ItemStack arg0) {
        return 0;
    }

    // Thaumic Boots Methods:

    @Optional.Method(modid = "thaumicboots")
    public void applyOmniState(EntityPlayer player, float bonus, ItemStack itemStack) {
        if (player.moveForward != 0.0) {
            player.moveFlying(0.0F, player.moveForward, bonus);
        }
        if (player.moveStrafing != 0.0 && getOmniState(itemStack)) {
            player.moveFlying(player.moveStrafing, 0.0F, bonus);
        }
    }

    // Avoid NSM Exception when ThaumicBoots is not present.
    public double getSpeedModifier(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("speed")) {
            return stack.stackTagCompound.getDouble("speed");
        }
        return 1.0;
    }

    public double getJumpModifier(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("jump")) {
            return stack.stackTagCompound.getDouble("jump");
        }
        return 1.0;
    }

    public boolean getOmniState(ItemStack stack) {
        if (stack.stackTagCompound != null) {
            return stack.stackTagCompound.getBoolean("omni");
        }
        return false;
    }

    public boolean getIntertialState(ItemStack stack) {
        if (stack.stackTagCompound != null) {
            return stack.stackTagCompound.getBoolean("inertiacanceling");
        }
        return false;
    }
}
