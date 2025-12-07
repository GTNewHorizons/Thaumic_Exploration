package flaxbeard.thaumicexploration.event;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.common.ConfigTX;
import flaxbeard.thaumicexploration.integration.TTIntegration;
import flaxbeard.thaumicexploration.item.ItemTXArmorSpecial;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumicboots.api.IBoots;

public class TXBootsEventHandler {

    public static final PlayerCapabilities genericPlayerCapabilities = new PlayerCapabilities();

    @SubscribeEvent
    public void livingTick(LivingUpdateEvent event) {
        if ((event.entity instanceof EntityPlayer)) {
            EntityPlayer player = (EntityPlayer) event.entity;
            checkAir(player);

            if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemWandCasting) {
                ItemWandCasting wand = (ItemWandCasting) player.getHeldItem().getItem();
                if (wand.getObjectInUse(player.getHeldItem(), player.worldObj) != null
                        && wand.getCap(player.getHeldItem()) == ThaumicExploration.WAND_CAP_MECHANIST) {
                    if (wand.getObjectInUse(player.getHeldItem(), player.worldObj) instanceof INode) {
                        if (!player.getHeldItem().hasTagCompound()) {
                            player.getHeldItem().setTagCompound(new NBTTagCompound());
                        }
                        if (!player.getHeldItem().stackTagCompound.hasKey("nodeTicks")) {
                            player.getHeldItem().stackTagCompound.setInteger("nodeTicks", 0);
                        }
                        int nodeTicks = player.getHeldItem().stackTagCompound.getInteger("nodeTicks");
                        nodeTicks++;

                        IWandable node = wand.getObjectInUse(player.getHeldItem(), player.worldObj);
                        TileEntity nodeAsTE = (TileEntity) node;
                        node.onUsingWandTick(player.getHeldItem(), player, nodeTicks);
                        if (nodeTicks % 3 == 0) {
                            if (player.worldObj.rand.nextBoolean()) {
                                player.worldObj.playSound(
                                        nodeAsTE.xCoord,
                                        nodeAsTE.yCoord,
                                        nodeAsTE.zCoord,
                                        "tile.piston.in",
                                        0.1F + (float) (0.5F * Math.random()),
                                        0.75F,
                                        false);
                            } else {
                                player.worldObj.playSound(
                                        nodeAsTE.xCoord,
                                        nodeAsTE.yCoord,
                                        nodeAsTE.zCoord,
                                        "tile.piston.out",
                                        0.1F + (float) (0.5F * Math.random()),
                                        0.75F,
                                        false);
                            }
                        }
                        player.getHeldItem().stackTagCompound.setInteger("nodeTicks", nodeTicks);
                    }
                }
                if (wand.getCap(player.getHeldItem()) == ThaumicExploration.WAND_CAP_SOJOURNER
                        && !player.worldObj.isRemote) {
                    if (!player.getHeldItem().hasTagCompound()) {
                        player.getHeldItem().setTagCompound(new NBTTagCompound());
                    }
                    if (!player.getHeldItem().stackTagCompound.hasKey("nodeTicks")) {
                        player.getHeldItem().stackTagCompound.setInteger("nodeTicks", 0);
                        player.getHeldItem().stackTagCompound.setInteger("drainX", 0);
                        player.getHeldItem().stackTagCompound.setInteger("drainY", 0);
                        player.getHeldItem().stackTagCompound.setInteger("drainZ", 0);
                    }
                    int nodeTicks = player.getHeldItem().stackTagCompound.getInteger("nodeTicks");
                    nodeTicks++;
                    if (nodeTicks >= 20) {
                        AspectList emptyAspects = new AspectList();
                        for (Aspect aspect : Aspect.getPrimalAspects()) {
                            if (wand.getVis(player.getHeldItem(), aspect) < wand.getMaxVis(player.getHeldItem())) {
                                emptyAspects.add(aspect, 1);
                            }
                        }
                        ArrayList<ChunkCoordinates> nodes = new ArrayList<ChunkCoordinates>();
                        for (int xx = -8; xx <= 8; xx++) {
                            for (int yy = -8; yy <= 8; yy++) {
                                for (int zz = -8; zz <= 8; zz++) {
                                    TileEntity te = player.worldObj.getTileEntity(
                                            ((int) player.posX) + xx,
                                            ((int) player.posY) + yy,
                                            ((int) player.posZ) + zz);
                                    if ((te instanceof INode) && emptyAspects.size() > 0) {
                                        boolean canAdd = false;
                                        for (Aspect aspect : emptyAspects.getAspects()) {
                                            if (((INode) te).getAspects().getAmount(aspect) > 1) {
                                                canAdd = true;
                                            }
                                        }
                                        if (canAdd) nodes.add(
                                                new ChunkCoordinates(
                                                        ((int) player.posX) + xx,
                                                        ((int) player.posY) + yy,
                                                        ((int) player.posZ) + zz));
                                    }
                                }
                            }
                        }
                        ChunkCoordinates randNode;
                        if (nodes.size() != 0) {
                            if (!nodes.contains(
                                    new ChunkCoordinates(
                                            player.getHeldItem().stackTagCompound.getInteger("drainX"),
                                            player.getHeldItem().stackTagCompound.getInteger("drainY"),
                                            player.getHeldItem().stackTagCompound.getInteger("drainZ")))) {
                                randNode = nodes.get(player.worldObj.rand.nextInt(nodes.size()));
                                player.getHeldItem().stackTagCompound.setInteger("drainX", randNode.posX);
                                player.getHeldItem().stackTagCompound.setInteger("drainY", randNode.posY);
                                player.getHeldItem().stackTagCompound.setInteger("drainZ", randNode.posZ);
                            } else {
                                randNode = new ChunkCoordinates(
                                        new ChunkCoordinates(
                                                player.getHeldItem().stackTagCompound.getInteger("drainX"),
                                                player.getHeldItem().stackTagCompound.getInteger("drainY"),
                                                player.getHeldItem().stackTagCompound.getInteger("drainZ")));
                            }
                            INode node = (INode) player.worldObj
                                    .getTileEntity(randNode.posX, randNode.posY, randNode.posZ);
                            AspectList possibleAspects = new AspectList();
                            for (Aspect aspect : emptyAspects.getAspects()) {
                                if (node.getAspects().getAmount(aspect) > 1) {
                                    possibleAspects.add(aspect, 1);
                                }
                            }
                            Aspect takeAspect = possibleAspects.getAspects()[player.worldObj.rand
                                    .nextInt(possibleAspects.getAspects().length)];
                            node.takeFromContainer(takeAspect, 1);
                            player.worldObj.markBlockForUpdate(randNode.posX, randNode.posY, randNode.posZ);
                            wand.addVis(player.getHeldItem(), takeAspect, 1, true);
                        }

                        nodeTicks = 0;
                    }
                    player.getHeldItem().stackTagCompound.setInteger("nodeTicks", nodeTicks);
                }
            }

            // TODO tainted band
            boolean isTainted = false;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                if (player.inventory.getStackInSlot(i) != null
                        && player.inventory.getStackInSlot(i).getItem() == ThaumicExploration.charmTaint) {
                    isTainted = true;
                    break;
                }
            }

            if (!player.getEntityData().hasKey("tainted")) {
                player.getEntityData().setBoolean("tainted", isTainted);
            }
            boolean wasTainted = player.getEntityData().getBoolean("tainted");
            if (wasTainted && !isTainted && !player.capabilities.disableDamage) {
                player.attackEntityFrom(DamageSourceTX.noTaint, 999);
            }
            if (!wasTainted && isTainted) {
                player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "thaumcraft:zap", 1.0F, 1.0F);
            }
            player.getEntityData().setBoolean("tainted", isTainted);

            if (!(player.worldObj.getBiomeGenForCoords((int) player.posX, (int) player.posZ)
                    == ThaumcraftWorldGenerator.biomeTaint)) {
                if (isTainted) {
                    if (!player.getEntityData().hasKey("taintGracePeriod")) {
                        player.getEntityData().setInteger("taintGracePeriod", 0);
                    }
                    int taintGP = player.getEntityData().getInteger("taintGracePeriod");
                    taintGP++;
                    player.getEntityData().setInteger("taintGracePeriod", taintGP);
                    if (player.getActivePotionEffect(ThaumicExploration.potionTaintWithdrawl) == null
                            && taintGP > 100) {
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (player.inventory.getStackInSlot(i) != null
                                    && player.inventory.getStackInSlot(i).getItem() == ConfigItems.itemResource
                                    && (player.inventory.getStackInSlot(i).getItemDamage() == 11
                                            || player.inventory.getStackInSlot(i).getItemDamage() == 12)) {
                                player.inventory.decrStackSize(i, 1);
                                taintGP = 0;
                                player.getEntityData().setInteger("taintGracePeriod", 0);
                                break;
                            }
                        }

                        if (taintGP > 100) player
                                .addPotionEffect(new PotionEffect(ThaumicExploration.potionTaintWithdrawl.id, 100, 1));
                    }
                }

            } else {
                if (!player.worldObj.isRemote) {
                    if (isTainted) {
                        player.getEntityData().setInteger("taintGracePeriod", 0);
                        if (player.getFoodStats().getFoodLevel() < 4) {
                            if (!player.getEntityData().hasKey("taintFoodBuff")) {
                                player.getEntityData().setInteger("taintFoodBuff", 0);
                            }
                            int taint = player.getEntityData().getInteger("taintFoodBuff");
                            taint++;
                            if (taint > 80) {
                                player.getFoodStats().addStats(1, 0.0F);
                                taint = 0;
                                if (player.worldObj.isRemote) {
                                    Thaumcraft.proxy.swarmParticleFX(player.worldObj, player, 0.1F, 10.0F, 0.0F);
                                }
                            }

                            player.getEntityData().setInteger("taintFoodBuff", taint);
                        } else {
                            if (!player.getEntityData().hasKey("taintFoodBuff")) {
                                player.getEntityData().setInteger("taintFoodBuff", 0);
                            }
                            player.getEntityData().setInteger("taintFoodBuff", 0);
                        }
                    }
                }
            }
            /*
             * //TODO for (int i = 0; i<player.inventory.getSizeInventory(); i++) { if
             * (player.inventory.getStackInSlot(i) != null && player.inventory.getStackInSlot(i).getItem() ==
             * ThaumicExploration.charmTaint) { player.removePotionEffect(PotionFluxTaint.instance.id); break; } }
             */
        }

        if ((event.entity.worldObj.isRemote) && (this.prevStep.containsKey(Integer.valueOf(event.entity.getEntityId())))
                && ((((EntityPlayer) event.entity).inventory.armorItemInSlot(0) == null)
                        || (((EntityPlayer) event.entity).inventory.armorItemInSlot(0).getItem()
                                != ThaumicExploration.bootsMeteor
                                && ((EntityPlayer) event.entity).inventory.armorItemInSlot(0).getItem()
                                        != ThaumicExploration.bootsComet))) {
            event.entity.stepHeight = ((Float) this.prevStep.get(Integer.valueOf(event.entity.getEntityId())))
                    .floatValue();
            this.prevStep.remove(Integer.valueOf(event.entity.getEntityId()));
        }
    }

    HashMap<Integer, Float> prevStep = new HashMap();

    @SubscribeEvent
    public void playerJumps(LivingEvent.LivingJumpEvent event) {
        // if not a player
        if (!(event.entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.entity;

        // if no boots equipped
        if (player.inventory.armorItemInSlot(0) == null) {
            return;
        }

        Item item = player.inventory.armorItemInSlot(0).getItem();

        if ((item instanceof ItemTXArmorSpecial) && !((EntityPlayer) event.entity).isSneaking()) {
            double jump = ((ItemTXArmorSpecial) item).jumpBonus;
            if (ThaumicExploration.isBootsActive) {
                jump *= applyJump(player);
            }
            event.entityLiving.motionY += jump;
        } else if (item == ThaumicExploration.bootsMeteor && player.isSneaking()) {
            Vec3 vector = event.entityLiving.getLook(0.5F);
            double total = Math.abs(vector.zCoord + vector.xCoord);
            double jump = 0;
            if (ThaumicExploration.isThaumTinkererActive) {
                jump = TTIntegration.getAscentLevel(player);
            }
            if (jump >= 1) {
                jump *= 0.5; // bizarre, but I made this a bit more efficient (was originally (jump*2)/4)
            }
            if (ThaumicExploration.isBootsActive) {
                jump *= applyJump(player);
            }

            if (vector.yCoord < total) vector.yCoord = total;

            player.motionY += ((jump + 1) * vector.yCoord) / 1.5F; // no good way to represent, 2/3 equivalent.
            player.motionZ += (jump + 1) * vector.zCoord * 4;
            player.motionX += (jump + 1) * vector.xCoord * 4;

        }
    }

    @Optional.Method(modid = "thaumicboots")
    public double applyJump(EntityPlayer player) {
        return IBoots.isJumpEnabled(player.inventory.armorItemInSlot(0));
    }

    // TODO boot effect
    public void checkAir(EntityPlayer player) {
        // meteor boots
        if ((player.inventory.armorItemInSlot(0) != null)
                && (player.inventory.armorItemInSlot(0).getItem() == ThaumicExploration.bootsMeteor)) {

            Vec3 vector = player.getLook(1.0F);
            ItemStack item = player.inventory.armorItemInSlot(0);
            if (!item.hasTagCompound()) {
                NBTTagCompound par1NBTTagCompound = new NBTTagCompound();
                item.setTagCompound(par1NBTTagCompound);
                item.stackTagCompound.setBoolean("IsSmashing", false);
                item.stackTagCompound.setInteger("smashTicks", 0);
                item.stackTagCompound.setInteger("airTicks", 0);
            }
            boolean smashing = item.stackTagCompound.getBoolean("IsSmashing");
            int ticks = item.stackTagCompound.getInteger("smashTicks");
            int ticksAir = item.stackTagCompound.getInteger("airTicks");

            if (player.onGround || player.isOnLadder()) {
                int size = 0;
                if (ticks > 5) size = 1;
                if (ticks > 10) size = 2;
                if (ticks > 15) size = 3;
                smashing = false;
                ticks = 0;
                ticksAir = 0;
                if (size > 0) {
                    player.worldObj.createExplosion(player, player.posX, player.posY, player.posZ, size, false);
                }
            }

            // COME ON AND SLAM
            if (!player.onGround && !player.isOnLadder() && !player.isInWater()) {
                if (!player.isSneaking()) {
                    ticksAir++;
                }

                if (player.isSneaking() && ticksAir > 5) {
                    smashing = true;
                }
            }

            if (player.capabilities.isFlying) {
                smashing = false;
                ticks = 0;
                ticksAir = 0;
            }
            if (smashing) {

                player.worldObj.spawnParticle(
                        "flame",
                        (double) (player.posX + Math.random() - 0.5F),
                        (double) (player.posY + Math.random() - 0.5F),
                        (double) (player.posZ + Math.random() - 0.5F),
                        0.0D,
                        0.0D,
                        0.0D);
                player.worldObj.spawnParticle(
                        "smoke",
                        (double) (player.posX + Math.random() - 0.5F),
                        (double) (player.posY + Math.random() - 0.5F),
                        (double) (player.posZ + Math.random() - 0.5F),
                        0.0D,
                        0.0D,
                        0.0D);
                player.worldObj.spawnParticle(
                        "flame",
                        (double) (player.posX + Math.random() - 0.5F),
                        (double) (player.posY + Math.random() - 0.5F),
                        (double) (player.posZ + Math.random() - 0.5F),
                        0.0D,
                        0.0D,
                        0.0D);
                player.motionY -= 0.1F;
                ticks++;
            } else {
                double motion = Math.abs(player.motionX) + Math.abs(player.motionZ) + Math.abs(0.5 * player.motionY);
                if (!player.isWet() && motion > 0.1F) {
                    player.worldObj.spawnParticle(
                            "flame",
                            (double) (player.posX + Math.random() - 0.5F),
                            (double) (player.boundingBox.minY + 0.25F + ((Math.random() - 0.5) * 0.25F)),
                            (double) (player.posZ + Math.random() - 0.5F),
                            0.0D,
                            0.025D,
                            0.0D);
                    // player.worldObj.spawnParticle("flame", (double)(player.posX + Math.random()-0.5F),
                    // (double)(player.boundingBox.minY + 0.25F + ((Math.random()-0.5)*0.25F)), (double)(player.posZ +
                    // Math.random()-0.5F), 0.0D, 0.025D, 0.0D);
                }
            }
            item.stackTagCompound.setBoolean("IsSmashing", smashing);
            item.stackTagCompound.setInteger("smashTicks", ticks);
            item.stackTagCompound.setInteger("airTicks", ticksAir);
        }

        // comet boots
        else if ((player.inventory.armorItemInSlot(0) != null)
                && (player.inventory.armorItemInSlot(0).getItem() == ThaumicExploration.bootsComet)) {

                    Vec3 vector = player.getLook(1.0F);
                    ItemStack item = player.inventory.armorItemInSlot(0);
                    if (!item.hasTagCompound()) {
                        NBTTagCompound par1NBTTagCompound = new NBTTagCompound();
                        item.setTagCompound(par1NBTTagCompound);
                        item.stackTagCompound.setInteger("runTicks", 0);
                    }

                    // anti-griefing config
                    if (ConfigTX.allowBootsIce) {
                        for (int x = -5; x < 6; x++) {
                            for (int z = -5; z < 6; z++) {
                                if ((player.worldObj.getBlock(
                                        (int) (player.posX + x),
                                        (int) (player.posY - 1),
                                        (int) (player.posZ + z)) == Blocks.water
                                        || player.worldObj.getBlock(
                                                (int) (player.posX + x),
                                                (int) (player.posY - 1),
                                                (int) (player.posZ + z)) == Blocks.water)
                                        && player.worldObj.getBlock(
                                                (int) (player.posX + x),
                                                (int) player.posY - 1,
                                                (int) (player.posZ + z)).getMaterial() == Material.water
                                        && player.worldObj.getBlockMetadata(
                                                (int) (player.posX + x),
                                                (int) player.posY - 1,
                                                (int) (player.posZ + z)) == 0
                                        && !player.isInWater()
                                        && (Math.abs(x) + Math.abs(z) < 8)) {
                                    player.worldObj.setBlock(
                                            (int) (player.posX + x),
                                            (int) player.posY - 1,
                                            (int) (player.posZ + z),
                                            ThaumicExploration.meltyIce);
                                    player.worldObj.spawnParticle(
                                            "snowballpoof",
                                            (int) (player.posX + x),
                                            (int) player.posY,
                                            (int) (player.posZ + z),
                                            0.0D,
                                            0.025D,
                                            0.0D);
                                }
                            }
                        }
                    }

                    int ticks = item.stackTagCompound.getInteger("runTicks");

                    double motion = Math.abs(player.motionX) + Math.abs(player.motionZ) + Math.abs(player.motionY);
                    if (motion > 0.1F || !player.onGround || player.isOnLadder()) {
                        if (ticks < 100) ticks++;
                    } else {
                        ticks = 0;
                    }

                    if (!player.isWet() && motion > 0.1F) {
                        player.worldObj.spawnParticle(
                                "fireworksSpark",
                                (double) (player.posX + Math.random() - 0.5F),
                                (double) (player.boundingBox.minY + 0.25F + ((Math.random() - 0.5) * 0.25F)),
                                (double) (player.posZ + Math.random() - 0.5F),
                                0.0D,
                                0.025D,
                                0.0D);
                        // player.worldObj.spawnParticle("snowballpoof", (double)(player.posX + Math.random()-0.5F),
                        // (double)(player.boundingBox.minY + 0.25F + ((Math.random()-0.5)*0.25F)), (double)(player.posZ
                        // +
                        // Math.random()-0.5F), 0.0D, 0.025D, 0.0D);
                    }

                    item.stackTagCompound.setInteger("runTicks", ticks);
                }
    }
}
