package flaxbeard.thaumicexploration.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class TXWorldData extends WorldSavedData {

    private static final String IDENTIFIER = "teWorldData";

    private int nextBoundChestID = 1;

    public TXWorldData() {
        super(IDENTIFIER);
    }

    public TXWorldData(String identifier) {
        super(identifier);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nextBoundChestID = nbt.getInteger("nextChestID");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("nextChestID", nextBoundChestID);
    }

    public int getNextBoundChestID() {
        markDirty();
        return nextBoundChestID++;
    }

    public static TXWorldData get(World world) {
        TXWorldData data = (TXWorldData) world.loadItemData(TXWorldData.class, IDENTIFIER);
        if (data == null) {
            data = new TXWorldData();
            world.setItemData(IDENTIFIER, data);
        }
        return data;
    }
}
