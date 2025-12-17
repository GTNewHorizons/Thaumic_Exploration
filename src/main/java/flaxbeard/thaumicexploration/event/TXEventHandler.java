package flaxbeard.thaumicexploration.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import baubles.api.BaublesApi;
import codechicken.lib.packet.PacketCustom;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import flaxbeard.thaumicexploration.ThaumicExploration;
import flaxbeard.thaumicexploration.ai.EntityAICreeperDummy;
import flaxbeard.thaumicexploration.ai.EntityAINearestAttackablePureTarget;
import flaxbeard.thaumicexploration.common.ConfigTX;
import flaxbeard.thaumicexploration.data.BoundJarNetworkManager;
import flaxbeard.thaumicexploration.data.TXWorldData;
import flaxbeard.thaumicexploration.entity.EntityTaintacleMinion;
import flaxbeard.thaumicexploration.tile.TileEntityBoundChest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.TileJarFillable;

public class TXEventHandler {

    private HashMap<String, ArrayList<EntityItem>> lastKilled = new HashMap<String, ArrayList<EntityItem>>();

    public TXEventHandler() {}

    @SubscribeEvent
    public void handleWorldLoad(WorldEvent.Load event) {
        TXWorldData.get(event.world);
    }

    @SubscribeEvent
    public void handleTaintSpawns(EntityJoinWorldEvent event) {
        if (event.entity instanceof ITaintedMob) {
            EntityLiving mob = (EntityLiving) event.entity;
            List<EntityAITaskEntry> tasksToRemove = new ArrayList<EntityAITaskEntry>();
            for (Object entry : mob.targetTasks.taskEntries) {
                EntityAITaskEntry entry2 = (EntityAITaskEntry) entry;
                if (entry2.action instanceof EntityAINearestAttackableTarget) {
                    tasksToRemove.add((EntityAITaskEntry) entry);
                }
            }
            for (EntityAITaskEntry entry : tasksToRemove) {
                mob.targetTasks.removeTask(entry.action);
            }
            // System.out.println("brainwashed1");
            mob.targetTasks.addTask(
                    1,
                    new EntityAINearestAttackablePureTarget((EntityCreature) mob, EntityPlayer.class, 0, true));
        }
    }

    @SubscribeEvent
    public void handleMobDrop(LivingDropsEvent event) {
        if (event.source == DamageSourceTX.soulCrucible) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void handleTeleport(EnderTeleportEvent event) {
        if (event.entityLiving instanceof EntityEnderman || event.entityLiving instanceof EntityPlayer) {
            if (event.entityLiving.isPotionActive(ThaumicExploration.potionBinding)) {
                event.setCanceled(true);
            }
        }
    }

    public static boolean isEnderIO = Loader.isModLoaded("EnderIO");

    @SubscribeEvent
    public void stopCreeperExplosions(LivingUpdateEvent event) {
        if (!isEnderIO && ConfigTX.enchantmentNVEnable && event.entityLiving.getEquipmentInSlot(4) != null) {
            ItemStack heldItem = event.entityLiving.getEquipmentInSlot(4);
            int nightVision = EnchantmentHelper
                    .getEnchantmentLevel(ThaumicExploration.enchantmentNightVision.effectId, heldItem);
            if (nightVision > 0 && (!event.entityLiving.isPotionActive(Potion.nightVision.id)
                    || event.entityLiving.getActivePotionEffect(Potion.nightVision).getDuration() < 210)) {
                event.entityLiving.addPotionEffect(new PotionEffect(Potion.nightVision.id, 210, 1, true));
            }
        }
        if (event.entityLiving instanceof EntityCreeper
                && event.entityLiving.isPotionActive(ThaumicExploration.potionBinding)) {
            EntityCreeper creeper = (EntityCreeper) event.entityLiving;
            int size = creeper.tasks.taskEntries.size();
            for (int i = 0; i < size; i++) {
                EntityAITaskEntry entry = (EntityAITaskEntry) creeper.tasks.taskEntries.get(i);
                if (entry.action instanceof EntityAICreeperSwell) {
                    entry.action = new EntityAICreeperDummy(creeper);
                    creeper.tasks.taskEntries.set(i, entry);
                }
            }
            // ReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper) event.entityLiving, 2,
            // LibObfuscation.TIME_SINCE_IGNITED);
        } else if (event.entityLiving instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper) event.entityLiving;
            int size = creeper.tasks.taskEntries.size();
            for (int i = 0; i < size; i++) {
                EntityAITaskEntry entry = (EntityAITaskEntry) creeper.tasks.taskEntries.get(i);
                if (entry.action instanceof EntityAICreeperDummy) {
                    entry.action = new EntityAICreeperSwell(creeper);
                    creeper.tasks.taskEntries.set(i, entry);
                    creeper.setCreeperState(1);
                }
            }
        }
    }

    @SubscribeEvent
    public void handleEnchantmentAttack(LivingAttackEvent event) {

        if ((event.entityLiving instanceof EntityEnderman || event.entityLiving instanceof EntityCreeper
                || event.entityLiving instanceof EntityPlayer)
                && event.source.getSourceOfDamage() instanceof EntityLivingBase
                && ConfigTX.enchantmentBindingEnable) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
            ItemStack heldItem = attacker.getHeldItem();
            if (heldItem == null) return;

            int binding = EnchantmentHelper
                    .getEnchantmentLevel(ThaumicExploration.enchantmentBinding.effectId, heldItem);
            if (binding > 1) {
                event.entityLiving.addPotionEffect(new PotionEffect(ThaumicExploration.potionBinding.id, 50, 1));
            }
        }
        if (event.source.getSourceOfDamage() instanceof EntityLivingBase && ConfigTX.enchantmentBindingEnable) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
            ItemStack heldItem = attacker.getHeldItem();
            if (heldItem == null) return;

            int binding = EnchantmentHelper
                    .getEnchantmentLevel(ThaumicExploration.enchantmentBinding.effectId, heldItem);
            if (binding > 0) {
                event.entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 50, 1));
            }
        }
        if (event.source.getSourceOfDamage() instanceof EntityLivingBase && ConfigTX.enchantmentDisarmEnable) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
            ItemStack heldItem = attacker.getHeldItem();
            if (heldItem == null) return;

            int disarm = EnchantmentHelper.getEnchantmentLevel(ThaumicExploration.enchantmentDisarm.effectId, heldItem);
            if (disarm > 0 && !(event.entityLiving instanceof EntityPlayer)) {
                if (event.entityLiving.getHeldItem() != null && !event.entityLiving.worldObj.isRemote
                        && (disarm >= 5 || event.entityLiving.worldObj.rand.nextInt(10 - (2 * disarm)) == 0)) {
                    ItemStack itemstack = event.entityLiving.getHeldItem();
                    event.entityLiving.setCurrentItemOrArmor(0, null);
                    World world = event.entityLiving.worldObj;
                    double x = event.entityLiving.posX;
                    double y = event.entityLiving.posY;
                    double z = event.entityLiving.posZ;
                    float f = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = world.rand.nextFloat();
                    EntityItem entityitem;
                    int k1 = world.rand.nextInt(21) + 10;

                    k1 = itemstack.stackSize;

                    entityitem = new EntityItem(
                            world,
                            (double) ((float) x + f),
                            (double) ((float) y + f1),
                            (double) ((float) z + f2),
                            new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
                    float f3 = 0.05F;
                    entityitem.motionX = (double) ((float) world.rand.nextGaussian() * f3);
                    entityitem.motionY = (double) ((float) world.rand.nextGaussian() * f3 + 0.2F);
                    entityitem.motionZ = (double) ((float) world.rand.nextGaussian() * f3);

                    if (itemstack.hasTagCompound()) {
                        entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                    }
                    world.spawnEntityInWorld(entityitem);
                }
            }
        }
        if (event.source.getSourceOfDamage() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
            ItemStack heldItem = attacker.getHeldItem();
            if (heldItem == null) return;
            if (heldItem.getItem() instanceof ItemWandCasting) {
                ItemWandCasting wand = (ItemWandCasting) heldItem.getItem();
                WandRod test = wand.getRod(heldItem);
                if (test == ThaumicExploration.STAFF_ROD_NECRO) {
                    event.entityLiving.addPotionEffect(new PotionEffect(Potion.wither.id, 100));
                }
            }
        }
    }

    private ArrayList<ChunkCoordinates> safeBlocks(EntityPlayer player) {
        ArrayList<ChunkCoordinates> safeSpots = new ArrayList<ChunkCoordinates>();
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                if (player.worldObj.isAirBlock((int) player.posX + x, (int) player.posY, (int) player.posZ + z)) {
                    if (!player.worldObj
                            .isAirBlock((int) player.posX + x, (int) player.posY - 1, (int) player.posZ + z)) {
                        if (player.worldObj.isSideSolid(
                                (int) player.posX + x,
                                (int) player.posY - 1,
                                (int) player.posZ + z,
                                ForgeDirection.UP)) {
                            safeSpots.add(
                                    new ChunkCoordinates(
                                            (int) player.posX + x,
                                            (int) player.posY,
                                            (int) player.posZ + z));
                        }
                    }
                }
            }
        }
        return safeSpots;
    }

    private boolean hasBauble(Item item, IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i) != null) {
                if (inventory.getStackInSlot(i).getItem() == item) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void handleTaint(LivingHurtEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            if (player.onGround && hasBauble(ThaumicExploration.tentacleRing, BaublesApi.getBaubles(player))) {
                ArrayList<ChunkCoordinates> safeSpots = safeBlocks(player);
                if (safeSpots.size() > 0) {
                    int rand = player.worldObj.rand.nextInt(safeSpots.size());
                    EntityTaintacleMinion minion = new EntityTaintacleMinion(player.worldObj);
                    minion.setPosition(
                            safeSpots.get(rand).posX + 0.5F,
                            safeSpots.get(rand).posY,
                            safeSpots.get(rand).posZ + 0.5F);
                    player.worldObj.spawnEntityInWorld(minion);
                    minion.playSpawnSound();
                }
            }
        }
        if (event.entityLiving.worldObj.rand.nextInt(4) < 3) {
            if (event.source.damageType == "mob") {
                if (event.source.getSourceOfDamage() instanceof ITaintedMob) {
                    if (event.entityLiving instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) event.entityLiving;
                        for (int i = 0; i < 10; i++) {
                            // if (player.inventory.getStackInSlot(i) != null)

                            if (player.inventory.getStackInSlot(i) != null
                                    && player.inventory.getStackInSlot(i).getItem()
                                            == ThaumicExploration.charmNoTaint) {
                                event.setCanceled(true);
                                break;
                            }
                        }
                    }
                }
            }
            if (event.source == DamageSourceThaumcraft.taint || event.source == DamageSourceThaumcraft.tentacle
                    || event.source == DamageSourceThaumcraft.swarm) {
                if (event.entityLiving instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) event.entityLiving;
                    for (int i = 0; i < 10; i++) {
                        if (player.inventory.getStackInSlot(i) != null
                                && player.inventory.getStackInSlot(i).getItem() == ThaumicExploration.charmNoTaint) {
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleItemUse(PlayerInteractEvent event) {
        byte type = 0;

        if (event.entityPlayer.worldObj.blockExists(event.x, event.y, event.z)) {
            if (event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z) == Blocks.chest) {

                if (event.entityPlayer.inventory.getCurrentItem() != null) {
                    if (event.entityPlayer.inventory.getCurrentItem().getItem() == ThaumicExploration.chestSeal) {
                        type = 1;
                    } else if (event.entityPlayer.inventory.getCurrentItem().getItem()
                            == ThaumicExploration.chestSealLinked) {
                                type = 2;
                            }
                }
            } else
                if (event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z) == ThaumicExploration.boundChest) {
                    World world = event.entityPlayer.worldObj;
                    // System.out.println(event.entityPlayer.worldObj.isRemote +
                    // ItemBlankSeal.itemNames[((TileEntityBoundChest) world.getBlockTileEntity(event.x, event.y,
                    // event.z)).getSealColor()]);
                    if (event.entityPlayer.inventory.getCurrentItem() != null) {
                        if (event.entityPlayer.inventory.getCurrentItem().getItem() == ThaumicExploration.chestSeal) {
                            int color = ((TileEntityBoundChest) world
                                    .getTileEntity(event.x, event.y, event.z)).clientColor;
                            type = 3;
                            if (15 - (event.entityPlayer.inventory.getCurrentItem().getItemDamage()) == color) {
                                int nextID = ((TileEntityBoundChest) world.getTileEntity(event.x, event.y, event.z)).id;
                                ItemStack linkedSeal = new ItemStack(
                                        ThaumicExploration.chestSealLinked,
                                        1,
                                        event.entityPlayer.inventory.getCurrentItem().getItemDamage());
                                NBTTagCompound tag = new NBTTagCompound();
                                tag.setInteger("ID", nextID);
                                tag.setInteger("x", event.x);
                                tag.setInteger("y", event.y);
                                tag.setInteger("z", event.z);
                                tag.setInteger("dim", world.provider.dimensionId);
                                linkedSeal.setTagCompound(tag);

                                event.entityPlayer.inventory.addItemStackToInventory(linkedSeal);
                                if (!event.entityPlayer.capabilities.isCreativeMode) event.entityPlayer.inventory
                                        .decrStackSize(event.entityPlayer.inventory.currentItem, 1);
                            }
                            event.setCanceled(true);
                        }
                    }
                }
        }

        if (event.entityPlayer.worldObj.blockExists(event.x, event.y, event.z)) {
            if (event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z) == ConfigBlocks.blockJar
                    && event.entityPlayer.worldObj.getBlockMetadata(event.x, event.y, event.z) == 0) {
                if (event.entityPlayer.inventory.getCurrentItem() != null
                        && ((TileJarFillable) event.entityPlayer.worldObj
                                .getTileEntity(event.x, event.y, event.z)).aspectFilter == null
                        && ((TileJarFillable) event.entityPlayer.worldObj
                                .getTileEntity(event.x, event.y, event.z)).amount == 0) {
                    if (event.entityPlayer.inventory.getCurrentItem().getItem() == ThaumicExploration.jarSeal) {
                        type = 4;
                    } else if (event.entityPlayer.inventory.getCurrentItem().getItem()
                            == ThaumicExploration.jarSealLinked) {
                                type = 5;
                            }
                }
            }
        }

        if (event.entityPlayer.worldObj.isRemote && type > 0) {
            ByteBuf buf = Unpooled.buffer();
            ByteBufOutputStream out = new ByteBufOutputStream(buf);

            try {
                out.writeByte(1);
                out.writeInt(event.entityPlayer.worldObj.provider.dimensionId);
                out.writeInt(event.x);
                out.writeInt(event.y);
                out.writeInt(event.z);
                out.writeByte(type);
                out.writeInt(event.entityPlayer.getEntityId());
                FMLProxyPacket packet = new FMLProxyPacket(buf, "tExploration");
                ThaumicExploration.channel.sendToServer(packet);
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        for (Map.Entry<String, AspectList> entry : BoundJarNetworkManager.getData().networks.entrySet()) {
            PacketCustom.sendToPlayer(
                    BoundJarNetworkManager.getPacket(new Tuple(entry.getKey(), entry.getValue())),
                    event.player);
        }
    }
}
