package flaxbeard.thaumicexploration.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
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
import thaumcraft.common.items.armor.Hover;
import thaumicboots.api.IBoots;
import thaumicboots.mixins.early.minecraft.EntityLivingBaseAccessor;

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
        if (itemStack.getItem() == ThaumicExploration.bootsMeteor) {
            if (player.fallDistance > 0.0F) {
                player.fallDistance = 0.0F;
            }
        }
        if (getInertiaState(itemStack) && player.moveForward == 0
                && player.moveStrafing == 0
                && player.capabilities.isFlying) {
            player.motionX *= 0.5;
            player.motionZ *= 0.5;
        }
        if (ThaumicExploration.isBootsActive) {
            boolean omniMode = getOmniState(itemStack);
            if ((player.moveForward == 0F && player.moveStrafing == 0F && omniMode)
                    || (player.moveForward <= 0F && !omniMode)) {
                return;
            }
        }
        if (player.moveForward != 0.0F || player.moveStrafing != 0.0F) {
            if (player.worldObj.isRemote && !player.isSneaking() && getStepAssistState(itemStack)) {
                if (!Thaumcraft.instance.entityEventHandler.prevStep
                        .containsKey(Integer.valueOf(player.getEntityId()))) {
                    Thaumcraft.instance.entityEventHandler.prevStep
                            .put(Integer.valueOf(player.getEntityId()), Float.valueOf(player.stepHeight));
                }
                player.stepHeight = 1.0F;
            }

            if (itemStack.getItem() == ThaumicExploration.bootsMeteor) {
                float bonus = 0.055F;
                movementEffects(player, bonus, itemStack);

                // This seems to be redundant ???
                if (player.fallDistance > 0.0F) {
                    player.fallDistance = 0.0F;
                }
            } else if (itemStack.getItem() == ThaumicExploration.bootsComet) {
                if (!itemStack.hasTagCompound()) {
                    NBTTagCompound par1NBTTagCompound = new NBTTagCompound();
                    itemStack.setTagCompound(par1NBTTagCompound);
                    itemStack.stackTagCompound.setInteger("runTicks", 0);
                }
                int ticks = itemStack.stackTagCompound.getInteger("runTicks");
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
            player.jumpMovementFactor = 0.01F * speedMod + 0.02F;
        } else {
            player.jumpMovementFactor = 0.03F * speedMod + 0.02F;
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
        if (getOmniState(itemStack)) {
            if (player.moveStrafing != 0.0) {
                player.moveFlying(player.moveStrafing, 0.0F, bonus);
            }
            boolean jumping = ((EntityLivingBaseAccessor) player).getIsJumping();
            boolean sneaking = player.isSneaking();
            if (sneaking && !jumping && !player.onGround) {
                player.motionY -= bonus;
            } else if (jumping && !sneaking) {
                player.motionY += bonus;
            }
        }
    }

    // Avoid NSM Exception when ThaumicBoots is not present.
    public double getSpeedModifier(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("speed")) {
            return stack.stackTagCompound.getDouble("speed");
        }
        return 1.0;
    }

    public boolean getOmniState(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("omni")) {
            return stack.stackTagCompound.getBoolean("omni");
        }
        return true;
    }

    public boolean getStepAssistState(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("step")) {
            return stack.stackTagCompound.getBoolean("step");
        }
        return true;
    }

    public boolean getInertiaState(ItemStack stack) {
        if (stack.stackTagCompound != null) {
            return stack.stackTagCompound.getBoolean("inertiacanceling");
        }
        return false;
    }
}
