package flaxbeard.thaumicexploration.integration.nei;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;
import flaxbeard.thaumicexploration.ThaumicExploration;

public class IMCForNEI {

    public static void IMCSender() {
        sendHandler("ThaumicExploration.replicator", "ThaumicExploration:replicator", 127, 0);
        sendCatalyst("ThaumicExploration.replicator", "ThaumicExploration:replicator");
    }

    private static void sendHandler(String handlerName, String stack, int height, int recipesPerPage) {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handler", handlerName);
        NBT.setString("modName", "Thaumic Exploration");
        NBT.setString("modId", ThaumicExploration.MODID);
        NBT.setBoolean("modRequired", true);
        NBT.setString("itemName", stack);
        NBT.setInteger("handlerHeight", height);
        NBT.setInteger("maxRecipesPerPage", recipesPerPage);
        NBT.setBoolean("showOverlayButton", false);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", NBT);
    }

    private static void sendCatalyst(String handlerName, String stack) {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handlerID", handlerName);
        NBT.setString("itemName", stack);
        NBT.setInteger("priority", 0);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerCatalystInfo", NBT);
    }
}
