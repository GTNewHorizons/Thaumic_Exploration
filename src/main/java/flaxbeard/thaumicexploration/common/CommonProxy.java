package flaxbeard.thaumicexploration.common;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CommonProxy {

    // Client stuff
    public void registerRenderers() {
        // Nothing here as the server doesn't render graphics!
    }

    public void addRecipes() {}

    public void setUnicode() {}

    public void spawnWaterOnPlayer(World worldObj, int xCoord, int yCoord, int zCoord, EntityPlayer player) {}

    public void spawnRandomWaterFountain(World worldObj, int xCoord, int yCoord, int zCoord) {}

    public void spawnWaterAtLocation(World worldObj, double xCoord, double yCoord, double zCoord, double dX, double dY,
            double dZ) {}

    public void spawnBoreSparkle(World worldObj, double xCoord, double yCoord, double zCoord, double x2, double y2,
            double z2) {}

    public void spawnHarvestParticle(World worldObj, double xCoord, double yCoord, double zCoord, double x2, double y2,
            double z2) {}

    public void spawnActiveBrazierParticle(World worldObj, int xCoord, int yCoord, int zCoord) {}

    public boolean getIsReadyForWisp() {
        return true;
    }

    public void spawnFragmentParticle(World worldObj, double xCoord, double yCoord, double zCoord, double x2, double y2,
            double z2, Block block, int id) {
        // TODO Auto-generated method stub

    }

    public void spawnEssentiaAtLocation(World worldObj, double xCoord, double yCoord, double zCoord, double dX,
            double dY, double dZ, int size, int color) {
        // TODO Auto-generated method stub

    }

    public void crucibleBubble(World world, float x, float y, float z, float cr, float cg, float cb) {
        // TODO Auto-generated method stub

    }

    public void spawnLightningBolt(World worldObj, double xCoord, double yCoord, double zCoord, double dX, double dY,
            double dZ) {
        // TODO Auto-generated method stub

    }

    public void spawnHarvestParticleSlow(World worldObj, double xCoord, double yCoord, double zCoord, double x2,
            double y2, double z2) {
        // TODO Auto-generated method stub

    }

    public void spawnFragmentParticleSlow(World worldObj, double xCoord, double yCoord, double zCoord, double x2,
            double y2, double z2, Block block, int id) {
        // TODO Auto-generated method stub

    }

    public boolean isServer() {
        return true;
    }

    public World getOverworld() {
        return DimensionManager.getWorld(0);
    }
}
