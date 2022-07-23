// package flaxbeard.thaumicexploration.item.focus;
//
// import java.util.ArrayList;
// import java.util.List;
//
// import net.minecraft.client.renderer.texture.IconRegister;
// import net.minecraft.entity.ai.EntityAIAttackOnCollide;
// import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
// import net.minecraft.entity.ai.EntityAITaskEntry;
// import net.minecraft.entity.item.EntityItem;
// import net.minecraft.entity.monster.EntityEnderman;
// import net.minecraft.entity.monster.EntityZombie;
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.item.Item;
// import net.minecraft.item.ItemStack;
// import net.minecraft.nbt.NBTTagCompound;
// import net.minecraft.util.AxisAlignedBB;
// import net.minecraft.util.Icon;
// import net.minecraft.util.MovingObjectPosition;
// import net.minecraft.world.World;
// import thaumcraft.api.aspects.Aspect;
// import thaumcraft.api.aspects.AspectList;
// import thaumcraft.api.wands.IWandFocus;
// import thaumcraft.client.codechicken.core.vec.Vector3;
// import thaumcraft.common.items.wands.ItemWandCasting;
// import cpw.mods.fml.relauncher.Side;
// import cpw.mods.fml.relauncher.SideOnly;
// import flaxbeard.thaumicexploration.ThaumicExploration;
// import flaxbeard.thaumicexploration.ai.EntityAIArrowAttackNecromancy;
// import flaxbeard.thaumicexploration.ai.EntityAIAttackOnCollideReplacement;
// import flaxbeard.thaumicexploration.ai.EntityAINearestAttackableTargetNecromancy;
// import flaxbeard.thaumicexploration.data.NecromancyMobProperties;
//
// public class ItemFocusNecromancy extends Item implements IWandFocus {
//
//	private static final AspectList visUsage = new AspectList().add(Aspect.AIR, 1);
//	private static Icon orn;
//	private static Icon depth;
//	
//	
//	public ItemFocusNecromancy(int par1) {
//		super(par1);
//	}
//	
//	@Override
//	public Icon getFocusDepthLayerIcon() {
//		return depth;
//	}
//
//	@Override
//	public void onUsingFocusTick(ItemStack stack, EntityPlayer player, int ticks) {
//		if (!stack.hasTagCompound()) {
//			NBTTagCompound par1NBTTagCompound = new NBTTagCompound();
//			stack.setTagCompound(par1NBTTagCompound );
//			stack.stackTagCompound.setInteger("progress",0);
//			stack.stackTagCompound.setInteger("lastItem",0);
//		}
//	
//		int lastItem = stack.stackTagCompound.getInteger("lastItem");
//		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
//	
//		Vector3 target = Vector3.fromEntityCenter(player);
//
//		final int range = 6;
//		final double distance = range - 1;
//		if(!player.isSneaking())
//			target.add(new Vector3(player.getLookVec()).multiply(distance));
//
//		target.y += 0.5;
//
//		List<EntityItem> entities = player.worldObj.getEntitiesWithinAABB(EntityItem.class,
// AxisAlignedBB.getBoundingBox(target.x - range, target.y - range, target.z - range, target.x + range, target.y +
// range, target.z + range));
//		boolean foundItem = false;
//		boolean sameItem = false;
//		int currentItem = 0;
//		EntityItem targetItem = null;
//		if(!entities.isEmpty() && wand.consumeAllVis(stack, player, getVisCost(), true)) {
//			for(EntityItem item : entities) {
//				ItemStack entityStack = item.getDataWatcher().getWatchableObjectItemStack(10);
//				if (entityStack.itemID == lastItem) {
//					sameItem = true;
//					foundItem = true;
//					currentItem = lastItem;
//					targetItem = item;
//					break;
//				}
//				if (entityStack.itemID == Item.rottenFlesh.itemID || entityStack.itemID == Item.bone.itemID) {
//					foundItem = true;
//					currentItem = entityStack.itemID;
//					targetItem = item;
//				}
//			}
//		}
//		if (!sameItem) {
//			stack.stackTagCompound.setInteger("progress",0);
//		}
//		int progress = stack.stackTagCompound.getInteger("progress");
//		if (foundItem) {
//			stack.stackTagCompound.setInteger("progress",progress+1);
//			progress = progress + 1;
//			stack.stackTagCompound.setInteger("lastItem",currentItem);
//		}
//		//System.out.println(progress);
//		if (progress > 0) {
//			int particleCheck = (int) (1200/progress);
//			if (particleCheck == 0) {
//				particleCheck = 1;
//			}
//			if (progress % particleCheck == 0) {
//				targetItem.worldObj.spawnParticle("spell", targetItem.posX, targetItem.posY, targetItem.posZ, 0.0D, 0.0D, 0.0D);
//			}
//		}
//		if (progress >= 65) {
//			stack.stackTagCompound.setInteger("progress",0);
//			ItemStack stack2 = targetItem.getDataWatcher().getWatchableObjectItemStack(10);
//	
//			ItemStack newStack = stack2;
//			newStack.stackSize--;
//			targetItem.setEntityItemStack(newStack);
//			if (!targetItem.worldObj.isRemote) {
//				if (stack2.itemID == Item.rottenFlesh.itemID) {
//					EntityZombie mob = new EntityZombie(targetItem.worldObj);
//					mob.setPosition(targetItem.posX, targetItem.posY, targetItem.posZ);
//					List<EntityAITaskEntry> tasksToRemove = new ArrayList<EntityAITaskEntry>();
//					for ( Object entry : mob.targetTasks.taskEntries)
//					{
//						EntityAITaskEntry entry2 = (EntityAITaskEntry)entry;
//						if (entry2.action instanceof EntityAINearestAttackableTarget)
//						{
//							tasksToRemove.add((EntityAITaskEntry) entry);
//						}
//					}
//					for (EntityAITaskEntry entry : tasksToRemove)
//					{
//						mob.targetTasks.removeTask(entry.action);
//					}
//					for ( Object entry : mob.tasks.taskEntries)
//					{
//						EntityAITaskEntry entry2 = (EntityAITaskEntry)entry;
//						if (entry2.action instanceof EntityAIAttackOnCollide)
//						{
//							tasksToRemove.add((EntityAITaskEntry) entry);
//						}
//					}
//					for (EntityAITaskEntry entry : tasksToRemove)
//					{
//						mob.tasks.removeTask(entry.action);
//					}
//					mob.tasks.addTask(2, new EntityAIAttackOnCollideReplacement(mob, EntityZombie.class, 1.0D, false));
//					mob.targetTasks.addTask(0, new EntityAINearestAttackableTargetNecromancy(mob, EntityZombie.class, 0, true));
//					if(NecromancyMobProperties.get(mob) == null)
//					{
//						NecromancyMobProperties.register(mob);
//					}
//					targetItem.worldObj.spawnEntityInWorld(mob);
//				}
//				else if (stack2.itemID == Item.bone.itemID) {
//					EntityEnderman mob = new EntityEnderman(targetItem.worldObj);
//					mob.setPosition(targetItem.posX, targetItem.posY, targetItem.posZ);
//					List<EntityAITaskEntry> tasksToRemove = new ArrayList<EntityAITaskEntry>();
//					for ( Object entry : mob.targetTasks.taskEntries)
//					{
//						EntityAITaskEntry entry2 = (EntityAITaskEntry)entry;
//						if (entry2.action instanceof EntityAINearestAttackableTarget)
//						{
//							tasksToRemove.add((EntityAITaskEntry) entry);
//						}
//					}
//					for (EntityAITaskEntry entry : tasksToRemove)
//					{
//						mob.targetTasks.removeTask(entry.action);
//					}
//					for ( Object entry : mob.tasks.taskEntries)
//					{
//						EntityAITaskEntry entry2 = (EntityAITaskEntry)entry;
//						if (entry2.action instanceof EntityAIAttackOnCollide)
//						{
//							tasksToRemove.add((EntityAITaskEntry) entry);
//						}
//					}
//					for (EntityAITaskEntry entry : tasksToRemove)
//					{
//						mob.tasks.removeTask(entry.action);
//					}
//					mob.setCurrentItemOrArmor(0, new ItemStack(Item.bow));
//					mob.tasks.addTask(2, new EntityAIArrowAttackNecromancy(mob, 1.0D, 20, 60, 15.0F));
//					mob.targetTasks.addTask(0, new EntityAINearestAttackableTargetNecromancy(mob, EntityZombie.class, 0, true));
//					if(NecromancyMobProperties.get(mob) == null)
//					{
//						NecromancyMobProperties.register(mob);
//					}
//					targetItem.worldObj.spawnEntityInWorld(mob);
//				}
//			}
//		}
//	}
//
//	@Override
//	public String getSortingHelper(ItemStack itemstack) {
//		return "NECROMANCY";
//	}
//
//
//	@Override
//	public int getFocusColor() {
//		return 0xFFFFFF;
//	}
//
//	@Override
//	public AspectList getVisCost() {
//		return visUsage;
//	}
//
//	@Override
//	public boolean isVisCostPerTick() {
//		return true;
//	}
//	
//	@SideOnly(Side.CLIENT)
//	public void registerIcons(IconRegister ir)
//	{
//		super.registerIcons(ir);
//		this.orn = ir.registerIcon("thaumicexploration:necromancyOrn");
//		this.depth = ir.registerIcon("thaumicexploration:necromancyDepth");
//		//System.out.println(ThaumicExploration.focusNecromancy.getUnlocalizedName().replaceAll("item.", "") + "Orn");
//	}
//
//
//	@Override
//	public Icon getOrnament() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public WandFocusAnimation getAnimation() {
//		// TODO Auto-generated method stub
//		return WandFocusAnimation.WAVE;
//	}
//
//	@Override
//	public ItemStack onFocusRightClick(ItemStack itemstack, World world,
//			EntityPlayer player, MovingObjectPosition movingobjectposition) {
//		// TODO Auto-generated method stub
//		player.setItemInUse(itemstack, Integer.MAX_VALUE);
//
//		return itemstack;
//	}
//
//	@Override
//	public void onPlayerStoppedUsingFocus(ItemStack itemstack, World world,
//			EntityPlayer player, int count) {
//		itemstack.stackTagCompound.setInteger("progress", 0);
//	
//	}
//
//	@Override
//	public boolean onFocusBlockStartBreak(ItemStack itemstack, int x, int y,
//			int z, EntityPlayer player) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean acceptsEnchant(int id) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
// }
