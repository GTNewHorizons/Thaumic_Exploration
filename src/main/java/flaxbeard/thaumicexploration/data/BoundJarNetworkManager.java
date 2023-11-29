package flaxbeard.thaumicexploration.data;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;

import codechicken.lib.packet.PacketCustom;
import flaxbeard.thaumicexploration.ThaumicExploration;
import thaumcraft.api.aspects.AspectList;

/**
 * Created by Katrina on 16/06/2015.
 */
public class BoundJarNetworkManager {

    public class BoundJarHandler implements PacketCustom.IClientPacketHandler {

        @Override
        public void handlePacket(PacketCustom packetCustom, Minecraft minecraft,
                INetHandlerPlayClient iNetHandlerPlayClient) {
            String id = packetCustom.readString();
            if (!data.networks.containsKey(id)) data.networks.put(id, new AspectList());
            data.networks.get(id).readFromNBT(packetCustom.readNBTTagCompound());
        }
    }

    private static BoundJarNetworkData data = new BoundJarNetworkData();

    public static BoundJarNetworkData getData() {
        if (data == null) loadData();

        return data;
    }

    private static void loadData() {

        World world = ThaumicExploration.proxy.getOverworld();
        if (world != null) {

            data = (BoundJarNetworkData) world.mapStorage
                    .loadData(BoundJarNetworkData.class, BoundJarNetworkData.IDENTIFIER);
            if (data == null) {
                data = new BoundJarNetworkData();
                data.markDirty();
                world.mapStorage.setData(BoundJarNetworkData.IDENTIFIER, data);
            }
        }
    }

    public static Packet getPacket(Tuple data) {
        PacketCustom packetCustom = new PacketCustom(ThaumicExploration.instance, 2);
        packetCustom.writeString((String) data.getFirst());
        NBTTagCompound compound = new NBTTagCompound();
        ((AspectList) data.getSecond()).writeToNBT(compound);
        packetCustom.writeNBTTagCompound(compound);
        return packetCustom.toPacket();
    }

    public static void markDirty() {
        data.markDirty();
        World world = ThaumicExploration.proxy.getOverworld();
        if (world != null) {
            world.mapStorage.saveAllData();
        }
    }

    public static AspectList getAspect(String network) {
        if (!getData().networks.containsKey(network)) getData().networks.put(network, new AspectList());
        return getData().networks.get(network);
    }

    public static void markDirty(String network) {
        markDirty();
        PacketCustom.sendToClients(getPacket(new Tuple(network, getData().networks.get(network))));
    }
}
