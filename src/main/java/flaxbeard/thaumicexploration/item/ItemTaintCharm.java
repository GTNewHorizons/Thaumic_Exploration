package flaxbeard.thaumicexploration.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.potions.PotionFluxTaint;

public class ItemTaintCharm extends Item {

    public ItemTaintCharm() {

        this.maxStackSize = 1;
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
        if (!par2World.isRemote) {
            if (par3Entity.ticksExisted % 20 == 0) {
                if (par3Entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) par3Entity;
                    boolean removed = false;

                    player.removePotionEffect(PotionFluxTaint.instance.id);
                    /*
                    Collection<PotionEffect> potions = player.getActivePotionEffects();

                                   for (PotionEffect potion : potions) {
                                       int id = potion.getPotionID();
                                       //boolean badEffect;
                                       //badEffect = ReflectionHelper.getPrivateValue(Potion.class, Potion.potionTypes[id], new String[]{"isBadEffect", "field_76418_K"});
                                       if (Potion.potionTypes[id] instanceof PotionFluxTaint) {
                                           //badEffect = true;
                                           player.removePotionEffect(id);
                                           removed = true;
                                       }

                                   }

                    if(removed) {
                    	par2World.playSoundAtEntity(player, "thaumcraft:wand", 0.3F, 0.1F);
                    }
                    */
                }
            }
        }
    }
}
