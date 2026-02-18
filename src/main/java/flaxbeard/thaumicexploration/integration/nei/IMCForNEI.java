package flaxbeard.thaumicexploration.integration.nei;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;
import flaxbeard.thaumicexploration.ThaumicExploration;

public class IMCForNEI {

    public static void IMCSender() {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handler", "ThaumicExploration.replicator");
        NBT.setString("modName", "Thaumic Exploration");
        NBT.setString("modId", ThaumicExploration.MODID);
        NBT.setBoolean("modRequired", true);
        NBT.setString("itemName", "ThaumicExploration:replicator");
        NBT.setInteger("handlerHeight", 18);
        NBT.setBoolean("multipleWidgetsAllowed", true);
        NBT.setBoolean("showFavoritesButton", false);
        NBT.setBoolean("showOverlayButton", false);
        NBT.setInteger("maxRecipesPerPage", 0);
        NBT.setBoolean("showOverlayButton", false);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", NBT);
        sendCatalyst("ThaumicExploration.replicator", "ThaumicExploration:replicator");
    }

    private static void sendCatalyst(String handlerName, String stack) {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handlerID", handlerName);
        NBT.setString("itemName", stack);
        NBT.setInteger("priority", 0);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerCatalystInfo", NBT);
    }
}
