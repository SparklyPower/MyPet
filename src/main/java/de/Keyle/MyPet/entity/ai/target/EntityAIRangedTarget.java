/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2013 Keyle
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

package de.Keyle.MyPet.entity.ai.target;

import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import net.minecraft.server.v1_5_R2.EntityArrow;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.PathfinderGoal;
import net.minecraft.server.v1_5_R2.World;
import org.bukkit.craftbukkit.v1_5_R2.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityAIRangedTarget extends PathfinderGoal
{
    private MyPet myPet;
    private final EntityMyPet entityMyPet;
    private EntityLiving target;
    private int shootTimer;
    private float walkSpeed;
    private int lastSeenTimer;
    private int fireRate;
    private float rangeSquared;

    public EntityAIRangedTarget(MyPet myPet, float walkSpeed, int fireRate, float range)
    {
        this.myPet = myPet;
        this.shootTimer = -1;
        this.lastSeenTimer = 0;
        this.entityMyPet = myPet.getCraftPet().getHandle();
        this.walkSpeed = walkSpeed;
        this.fireRate = fireRate;
        this.rangeSquared = (range * range);
    }

    public boolean a()
    {
        EntityLiving goalTarget = this.entityMyPet.goalTarget;

        if (goalTarget == null || !goalTarget.isAlive() || myPet.getRangedDamage() <= 0 || !entityMyPet.canMove())
        {
            return false;
        }
        double e = this.entityMyPet.e(goalTarget.locX, goalTarget.boundingBox.b, goalTarget.locZ);
        if (myPet.getDamage() > 0 && e < 16)
        {
            return false;
        }
        this.target = goalTarget;
        return true;
    }

    public boolean b()
    {
        if (target == null || !target.isAlive() || myPet.getRangedDamage() <= 0 || !entityMyPet.canMove())
        {
            return false;
        }
        if (myPet.getDamage() > 0 && this.entityMyPet.e(target.locX, target.boundingBox.b, target.locZ) < 16)
        {
            return false;
        }
        return true;
    }

    public void d()
    {
        EntityTargetEvent.TargetReason reason = this.target.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.entityMyPet, null, reason);

        this.target = null;
        this.lastSeenTimer = 0;
        this.shootTimer = -1;
    }

    public void e()
    {
        double distanceToTarget = this.entityMyPet.e(this.target.locX, this.target.boundingBox.b, this.target.locZ);
        boolean canSee = this.entityMyPet.aD().canSee(this.target);

        if (canSee)
        {
            this.lastSeenTimer++;
        }
        else
        {
            this.lastSeenTimer = 0;
        }

        if ((distanceToTarget <= this.rangeSquared) && (this.lastSeenTimer >= 20))
        {
            this.entityMyPet.getNavigation().g();
        }
        else
        {
            this.entityMyPet.getNavigation().a(this.target, this.walkSpeed);
        }

        this.entityMyPet.getControllerLook().a(this.target, 30.0F, 30.0F);

        if (--this.shootTimer <= 0)
        {
            if (distanceToTarget < this.rangeSquared && canSee)
            {
                shootProjectile(this.target, myPet.getRangedDamage());
                this.shootTimer = this.fireRate;
            }
        }
    }

    public void shootProjectile(EntityLiving target, float damage)
    {
        World world = target.world;
        EntityArrow entityarrow = new EntityArrow(world, entityMyPet, target, 1.6F, 14 - world.difficulty * 4);
        entityarrow.b(damage);
        entityMyPet.makeSound("random.bow", 1.0F, 1.0F / (entityMyPet.aE().nextFloat() * 0.4F + 0.8F));
        world.addEntity(entityarrow);
    }
}
