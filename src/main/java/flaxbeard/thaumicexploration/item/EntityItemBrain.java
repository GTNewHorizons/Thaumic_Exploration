package flaxbeard.thaumicexploration.item;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityItemBrain extends EntityItem {
    public EntityItemBrain(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
    }

    public EntityItemBrain(World par1World) {
        super(par1World);
    }

    public EntityItemBrain(World par1World, double par2, double par4, double par6, ItemStack par8ItemStack) {
        super(par1World, par2, par4, par6, par8ItemStack);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer par1EntityPlayer) {
        if (age > 120) {
            super.onCollideWithPlayer(par1EntityPlayer);
        }
    }
}
