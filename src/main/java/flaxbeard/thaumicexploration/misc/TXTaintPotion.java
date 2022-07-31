package flaxbeard.thaumicexploration.misc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.event.DamageSourceTX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class TXTaintPotion extends PotionFluxTaint {

    public TXTaintPotion(int par1, boolean par2, int par3) {
        super(par1, par2, par3);
        setIconIndex(0, 0);
    }

    @SideOnly(Side.CLIENT)
    public int getStatusIconIndex() {
        UtilsFX.bindTexture("textures/misc/potions.png");
        return super.getStatusIconIndex();
    }

    @Override
    public void performEffect(EntityLivingBase target, int par2) {
        if (target.worldObj.getBiomeGenForCoords((int) target.posX, (int) target.posZ)
                == ThaumcraftWorldGenerator.biomeTaint) {
            target.removePotionEffect(ThaumicExploration.potionTaintWithdrawl.id);
        }
        if ((!target.isEntityUndead()) && ((target.getMaxHealth() > 1.0F) || ((target instanceof EntityPlayer)))) {
            target.attackEntityFrom(DamageSourceTX.noTaint, 1.0F);
        }
    }
}
