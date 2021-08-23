/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2020 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.compat.v1_17_R1.entity.types;

import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.types.MySkeletonHorse;
import de.Keyle.MyPet.compat.v1_17_R1.entity.EntityMyPet;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IJumpable;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

import static de.Keyle.MyPet.compat.v1_17_R1.CompatManager.ENTITY_LIVING_broadcastItemBreak;

@EntitySize(width = 1.4F, height = 1.6F)
public class EntityMySkeletonHorse extends EntityMyPet implements IJumpable {

	protected static final EntityDataAccessor<Boolean> AGE_WATCHER = SynchedEntityData.defineId(EntityMySkeletonHorse.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Byte> SADDLE_CHEST_WATCHER = SynchedEntityData.defineId(EntityMySkeletonHorse.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_WATCHER = SynchedEntityData.defineId(EntityMySkeletonHorse.class, EntityDataSerializers.o);

	int soundCounter = 0;
	int rearCounter = -1;

	public EntityMySkeletonHorse(Level world, MyPet myPet) {
		super(world, myPet);
	}

	/**
	 * Possible visual horse effects:
	 * 4 saddle
	 * 8 chest
	 * 32 head down
	 * 64 rear
	 * 128 mouth open
	 */
	protected void applyVisual(int value, boolean flag) {
		int i = getSynchedEntityData().get(SADDLE_CHEST_WATCHER);
		if (flag) {
			this.getEntityData().set(SADDLE_CHEST_WATCHER, (byte) (i | value));
		} else {
			this.getEntityData().set(SADDLE_CHEST_WATCHER, (byte) (i & (~value)));
		}
	}

	@Override
	public boolean attack(Entity entity) {
		boolean flag = false;
		try {
			flag = super.attack(entity);
			if (flag) {
				applyVisual(64, true);
				rearCounter = 10;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	protected String getDeathSound() {
		return "entity.skeleton_horse.death";
	}

	@Override
	protected String getHurtSound() {
		return "entity.skeleton_horse.hurt";
	}

	@Override
	protected String getLivingSound() {
		return "entity.skeleton_horse.ambient";
	}


	@Override
	public InteractionResult handlePlayerInteraction(EntityHuman entityhuman, InteractionHand enumhand, ItemStack itemStack) {
		if (super.handlePlayerInteraction(entityhuman, enumhand, itemStack).a()) {
			return InteractionResult.CONSUME;
		}

		if (itemStack != null && canUseItem()) {
			if (itemStack.getItem() == Items.lL && !getMyPet().hasSaddle() && getOwner().getPlayer().isSneaking() && canEquip()) {
				getMyPet().setSaddle(CraftItemStack.asBukkitCopy(itemStack));
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					itemStack.shrink(1);
					if (itemStack.getCount() <= 0) {
						entityhuman.getInventory().setItem(entityhuman.getInventory().selected, ItemStack.EMPTY);
					}
				}
				return InteractionResult.CONSUME;
			} else if (itemStack.getItem() == Items.pq && getOwner().getPlayer().isSneaking() && canEquip()) {
				if (getMyPet().hasSaddle()) {
					EntityItem entityitem = new EntityItem(this.t, this.locX(), this.locY() + 1, this.locZ(), CraftItemStack.asNMSCopy(getMyPet().getSaddle()));
					entityitem.ap = 10;
					entityitem.setMot(entityitem.getMot().add(0, this.Q.nextFloat() * 0.05F, 0));
					this.t.addEntity(entityitem);
				}

				makeSound("entity.sheep.shear", 1.0F, 1.0F);
				getMyPet().setSaddle(null);
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					try {
						itemStack.hurtAndBreak(1, entityhuman, (entityhuman1) -> entityhuman1.broadcastBreakEvent(enumhand));
					} catch (Error e) {
						// TODO REMOVE
						itemStack.hurtAndBreak(1, entityhuman, (entityhuman1) -> {
							try {
								ENTITY_LIVING_broadcastItemBreak.invoke(entityhuman1, enumhand);
							} catch (IllegalAccessException | InvocationTargetException ex) {
								ex.printStackTrace();
							}
						});
					}
				}

				return InteractionResult.CONSUME;
			} else if (Configuration.MyPet.SkeletonHorse.GROW_UP_ITEM.compare(itemStack) && getMyPet().isBaby() && getOwner().getPlayer().isSneaking()) {
				if (itemStack != ItemStack.EMPTY && !entityhuman.getAbilities().instabuild) {
					itemStack.shrink(1);
					if (itemStack.getCount() <= 0) {
						entityhuman.getInventory().setItem(entityhuman.getInventory().selected, ItemStack.EMPTY);
					}
				}
				getMyPet().setBaby(false);
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		getEntityData().define(AGE_WATCHER, false);
		getEntityData().define(SADDLE_CHEST_WATCHER, (byte) 0);
		getEntityData().define(OWNER_WATCHER, Optional.empty());
	}

	@Override
	public void updateVisuals() {
		this.getEntityData().set(AGE_WATCHER, getMyPet().isBaby());
		applyVisual(4, getMyPet().hasSaddle());
	}

	@Override
	public void onLivingUpdate() {
		boolean oldRiding = hasRider;
		super.onLivingUpdate();
		if (!hasRider) {
			if (rearCounter > -1 && rearCounter-- == 0) {
				applyVisual(64, false);
				rearCounter = -1;
			}
		}
		if (oldRiding != hasRider) {
			if (hasRider) {
				applyVisual(4, true);
			} else {
				applyVisual(4, getMyPet().hasSaddle());
			}
		}
	}

	@Override
	public void playStepSound(BlockPosition blockposition, IBlockData blockdata) {
		if (!blockdata.getMaterial().isLiquid()) {
			IBlockData blockdataUp = this.t.getType(blockposition.up());
			SoundEffectType soundeffecttype = blockdata.getStepSound();
			if (blockdataUp.getBlock() == Blocks.cK) {
				soundeffecttype = blockdata.getStepSound();
			}
			if (this.isVehicle()) {
				++this.soundCounter;
				if (this.soundCounter > 5 && this.soundCounter % 3 == 0) {
					this.playSound(SoundEffects.iB, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
				} else if (this.soundCounter <= 5) {
					this.playSound(SoundEffects.iH, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
				}
			} else if (!blockdata.getMaterial().isLiquid()) {
				this.soundCounter += 1;
				playSound(SoundEffects.iH, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
			} else {
				playSound(SoundEffects.iG, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
			}
		}
	}

	@Override
	public MySkeletonHorse getMyPet() {
		return (MySkeletonHorse) myPet;
	}

	@Override
	public void a(int i) {
		// I don't know.
	}

	/* Jump power methods */
	@Override
	public boolean a() {
		return true;
	}

	@Override
	public void b(int i) {
		this.jumpPower = i;
	}

	@Override
	public void b() {
	}
}
