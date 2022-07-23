package flaxbeard.thaumicexploration.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Katrina on 16/06/2015.
 */
public class NBTHelper {

    public static NBTTagCompound getItemStackTag(ItemStack stack) {
        NBTTagCompound cmp = stack.getTagCompound();
        if (cmp == null) {
            stack.setTagCompound(new NBTTagCompound());
            cmp = stack.getTagCompound();
        }
        return cmp;
    }
}
