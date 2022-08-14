package flaxbeard.thaumicexploration.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.client.render.BlockCrucibleSoulsRenderer;
import flaxbeard.thaumicexploration.client.render.BlockEverfullUrnRenderer;
import flaxbeard.thaumicexploration.client.render.BlockFloatyCandleRenderer;
import flaxbeard.thaumicexploration.client.render.BlockReplicatorRenderer;
import flaxbeard.thaumicexploration.client.render.BlockSoulBrazierRenderer;
import flaxbeard.thaumicexploration.client.render.BlockTrashJarRenderer;
import flaxbeard.thaumicexploration.client.render.ItemRenderThinkTank;
import flaxbeard.thaumicexploration.client.render.TileEntityBoundChestRender;
import flaxbeard.thaumicexploration.client.render.TileEntityBoundJarRender;
import flaxbeard.thaumicexploration.client.render.TileEntityFloatyCandleRender;
import flaxbeard.thaumicexploration.client.render.TileEntityRenderCrucibleSouls;
import flaxbeard.thaumicexploration.client.render.TileEntityReplicatorRender;
import flaxbeard.thaumicexploration.client.render.TileEntitySoulBrazierRenderer;
import flaxbeard.thaumicexploration.client.render.TileEntityThinkTankRender;
import flaxbeard.thaumicexploration.client.render.TileEntityTrashJarRenderer;
import flaxbeard.thaumicexploration.common.CommonProxy;
import flaxbeard.thaumicexploration.packet.TXClientPacketHandler;
import flaxbeard.thaumicexploration.tile.TileEntityBoundChest;
import flaxbeard.thaumicexploration.tile.TileEntityBoundJar;
import flaxbeard.thaumicexploration.tile.TileEntityCrucibleSouls;
import flaxbeard.thaumicexploration.tile.TileEntityFloatyCandle;
import flaxbeard.thaumicexploration.tile.TileEntityReplicator;
import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import flaxbeard.thaumicexploration.tile.TileEntityThinkTank;
import flaxbeard.thaumicexploration.tile.TileEntityTrashJar;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.client.fx.particles.FXBoreParticles;
import thaumcraft.client.fx.particles.FXBoreSparkle;
import thaumcraft.client.fx.particles.FXEssentiaTrail;
import thaumcraft.client.fx.particles.FXWisp;

public class ClientProxy extends CommonProxy {

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public void registerRenderers() {
        ThaumicExploration.channel.register(new TXClientPacketHandler());
        // RenderingRegistry.registerEntityRenderingHandler(EntityCandleFlame.class, new
        // RenderCandleFlame(ThaumicExploration.theCandle));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoundJar.class, new TileEntityBoundJarRender());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoundChest.class, new TileEntityBoundChestRender());
        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntityCrucibleSouls.class, new TileEntityRenderCrucibleSouls());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReplicator.class, new TileEntityReplicatorRender());
        // ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNecroPedestal.class, new
        // TileEntityNecroPedestalRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFloatyCandle.class, new TileEntityFloatyCandleRender());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoulBrazier.class, new TileEntitySoulBrazierRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrashJar.class, new TileEntityTrashJarRenderer());

        TileEntitySpecialRenderer renderThinkTank = new TileEntityThinkTankRender();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThinkTank.class, renderThinkTank);

        RenderingRegistry.registerBlockHandler(ThaumicExploration.floatCandleRenderID, new BlockFloatyCandleRenderer());
        RenderingRegistry.registerBlockHandler(ThaumicExploration.soulBrazierRenderID, new BlockSoulBrazierRenderer());
        // RenderingRegistry.registerBlockHandler(ThaumicExploration.necroPedestalRenderID, new
        // BlockNecroPedestalRenderer());
        // RenderingRegistry.registerBlockHandler(ThaumicExploration.candleSkullRenderID, new
        // BlockSkullCandleRenderer());
        RenderingRegistry.registerBlockHandler(ThaumicExploration.everfullUrnRenderID, new BlockEverfullUrnRenderer());
        RenderingRegistry.registerBlockHandler(ThaumicExploration.replicatorRenderID, new BlockReplicatorRenderer());
        RenderingRegistry.registerBlockHandler(
                ThaumicExploration.crucibleSoulsRenderID, new BlockCrucibleSoulsRenderer());
        RenderingRegistry.registerBlockHandler(ThaumicExploration.trashJarRenderID, new BlockTrashJarRenderer());
        MinecraftForgeClient.registerItemRenderer(
                Item.getItemFromBlock(ThaumicExploration.thinkTankJar),
                new ItemRenderThinkTank(renderThinkTank, new TileEntityThinkTank()));
        // MinecraftForgeClient.registerItemRenderer(ThaumicExploration.everfullUrn.blockID, new
        // ItemRenderEverfullUrn(new TileEntityEverfullUrnRender(), new TileEntityEverfullUrn()));
    }

    @Override
    public void setUnicode() {
        Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(false);
    }

    @Override
    public void spawnWaterOnPlayer(World worldObj, int xCoord, int yCoord, int zCoord, EntityPlayer player) {
        if (!worldObj.isRemote) return;
        FXEssentiaTrail fx = new FXEssentiaTrail(
                worldObj,
                xCoord + 0.5F,
                yCoord + 1.1F,
                zCoord + 0.5F,
                player.posX,
                player.posY,
                player.posZ,
                5,
                Aspect.TOOL.getColor(),
                1.0F);
        ParticleEngine.instance.addEffect(worldObj, fx);
    }

    @Override
    public void spawnLightningBolt(
            World worldObj, double xCoord, double yCoord, double zCoord, double dX, double dY, double dZ) {
        if (!worldObj.isRemote) return;
        FXLightningBolt bolt =
                new FXLightningBolt(worldObj, xCoord, yCoord, zCoord, dX, dY, dZ, worldObj.rand.nextLong(), 6, 0.5F, 5);
        bolt.defaultFractal();
        bolt.setType(5);
        bolt.setWidth(0.068F);
        bolt.finalizeBolt();
    }

    @Override
    public void spawnRandomWaterFountain(World worldObj, int xCoord, int yCoord, int zCoord) {
        if (!worldObj.isRemote) return;
        FXEssentiaTrail fx = new FXEssentiaTrail(
                worldObj,
                xCoord + 0.5F,
                yCoord + 1.1F,
                zCoord + 0.5F,
                xCoord + 0.5F + ((Math.random()) - 0.5),
                yCoord + 2.1F,
                zCoord + 0.5F + ((Math.random()) - 0.5),
                5,
                Aspect.TOOL.getColor(),
                1.0F);
        ParticleEngine.instance.addEffect(worldObj, fx);
    }

    @Override
    public void spawnWaterAtLocation(
            World worldObj, double xCoord, double yCoord, double zCoord, double dX, double dY, double dZ) {
        if (!worldObj.isRemote) return;
        FXEssentiaTrail fx =
                new FXEssentiaTrail(worldObj, xCoord, yCoord, zCoord, dX, dY, dZ, 5, Aspect.TOOL.getColor(), 1.0F);
        ParticleEngine.instance.addEffect(worldObj, fx);
    }

    @Override
    public void spawnEssentiaAtLocation(
            World worldObj,
            double xCoord,
            double yCoord,
            double zCoord,
            double dX,
            double dY,
            double dZ,
            int size,
            int color) {
        if (!worldObj.isRemote) return;
        FXEssentiaTrail fx = new FXEssentiaTrail(worldObj, xCoord, yCoord, zCoord, dX, dY, dZ, size, color, 1.0F);
        ParticleEngine.instance.addEffect(worldObj, fx);
    }

    @Override
    public void spawnActiveBrazierParticle(World worldObj, int xCoord, int yCoord, int zCoord) {
        if (!worldObj.isRemote) return;
        // FXEssentiaTrail fx = new FXEssentiaTrail(worldObj, xCoord+0.5F, yCoord+1.1F, zCoord+0.5F, xCoord+0.5F,
        // yCoord+1.5F, zCoord+0.5F, 5, Aspect.DARKNESS.getColor(), 1.0F);
        float offsetY = 0;
        float offsetZ = 0;
        float offsetX = 0;

        if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) instanceof TileEntitySoulBrazier) {
            TileEntitySoulBrazier brazier = (TileEntitySoulBrazier) worldObj.getTileEntity(xCoord, yCoord, zCoord);
            offsetY = (float) (Math.sin(Math.toRadians(brazier.count * 1.0F)) / 4.0F);
            offsetZ = (float) (Math.sin(Math.toRadians(brazier.count * 3.0F)) / 4.0F);
            offsetX = (float) (Math.cos(Math.toRadians(brazier.count * 3.0F)) / 4.0F);
        }
        // ParticleEngine.instance.addEffect(worldObj, fs);
        FXWisp ef = new FXWisp(
                worldObj,
                xCoord + 0.55F + offsetX,
                yCoord + 1.5F + offsetY,
                zCoord + 0.55F + offsetZ,
                (float) Math.random() / 1.125F,
                (float) (178.0F / 255.0F),
                (float) 0.0f,
                (float) (255.0F / 255.0F));

        ef.setGravity(0);
        ef.shrink = false;
        ef.noClip = true;
        ef.blendmode = 770;

        ParticleEngine.instance.addEffect(worldObj, ef);
        ef = new FXWisp(
                worldObj,
                xCoord + 0.55F + offsetX,
                yCoord + 1.5F + offsetY,
                zCoord + 0.55F + offsetZ,
                (float) Math.random() / 1.5F,
                (float) 0.1f,
                (float) 0.1f,
                (float) 0.1f);
        ef.setGravity(0);
        ef.shrink = false;
        ef.noClip = true;
        ef.blendmode = 770;

        ParticleEngine.instance.addEffect(worldObj, ef);
    }

    @Override
    public void spawnBoreSparkle(
            World worldObj, double xCoord, double yCoord, double zCoord, double x2, double y2, double z2) {
        if (!worldObj.isRemote) return;
        FXBoreSparkle fb = new FXBoreSparkle(worldObj, xCoord, yCoord, zCoord, x2, y2, z2);

        fb.setRBGColorF(0.4F + worldObj.rand.nextFloat() * 0.2F, 0.2F, 0.6F + worldObj.rand.nextFloat() * 0.3F);
        ParticleEngine.instance.addEffect(worldObj, fb);
    }

    @Override
    public void spawnHarvestParticle(
            World worldObj, double xCoord, double yCoord, double zCoord, double x2, double y2, double z2) {
        if (!worldObj.isRemote) return;
        FXBoreParticles fb = new FXBoreParticles(
                worldObj, xCoord, yCoord, zCoord, x2, y2, z2, Blocks.coal_block, worldObj.rand.nextInt(6), 3);
        fb.setAlphaF(0.3F);
        fb.motionX = ((float) worldObj.rand.nextGaussian() * 0.03F);
        fb.motionY = ((float) worldObj.rand.nextGaussian() * 0.03F);
        fb.motionZ = ((float) worldObj.rand.nextGaussian() * 0.03F);
        ParticleEngine.instance.addEffect(worldObj, fb);
    }

    @Override
    public void spawnFragmentParticle(
            World worldObj,
            double xCoord,
            double yCoord,
            double zCoord,
            double x2,
            double y2,
            double z2,
            Block block,
            int id) {
        if (!worldObj.isRemote) return;
        FXBoreParticles fb =
                new FXBoreParticles(worldObj, xCoord, yCoord, zCoord, x2, y2, z2, block, worldObj.rand.nextInt(6), id);

        fb.setAlphaF(0.3F);
        fb.motionX = ((float) worldObj.rand.nextGaussian() * 0.03F);
        fb.motionY = ((float) worldObj.rand.nextGaussian() * 0.03F);
        fb.motionZ = ((float) worldObj.rand.nextGaussian() * 0.03F);
        ParticleEngine.instance.addEffect(worldObj, fb);
    }

    @Override
    public boolean getIsReadyForWisp() {
        return FMLClientHandler.instance().getClient().renderViewEntity != null;
    }
}
