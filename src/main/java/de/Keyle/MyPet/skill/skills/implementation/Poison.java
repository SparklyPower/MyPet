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

package de.Keyle.MyPet.skill.skills.implementation;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.skill.ISkillActive;
import de.Keyle.MyPet.skill.skills.info.ISkillInfo;
import de.Keyle.MyPet.skill.skills.info.PoisonInfo;
import de.Keyle.MyPet.util.MyPetUtil;
import de.Keyle.MyPet.util.locale.MyPetLocales;
import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spout.nbt.IntTag;
import org.spout.nbt.StringTag;

import java.util.Random;

public class Poison extends PoisonInfo implements ISkillInstance, ISkillActive
{
    private static Random random = new Random();
    private MyPet myPet;

    public Poison(boolean addedByInheritance)
    {
        super(addedByInheritance);
    }

    public void setMyPet(MyPet myPet)
    {
        this.myPet = myPet;
    }

    public MyPet getMyPet()
    {
        return myPet;
    }

    public boolean isActive()
    {
        return chance > 0 && duration > 0;
    }

    public void upgrade(ISkillInfo upgrade, boolean quiet)
    {
        if (upgrade instanceof PoisonInfo)
        {
            boolean valuesEdit = false;
            if (upgrade.getProperties().getValue().containsKey("chance"))
            {
                if (!upgrade.getProperties().getValue().containsKey("addset_chance") || ((StringTag) upgrade.getProperties().getValue().get("addset_chance")).getValue().equals("add"))
                {
                    chance += ((IntTag) upgrade.getProperties().getValue().get("chance")).getValue();
                }
                else
                {
                    chance = ((IntTag) upgrade.getProperties().getValue().get("chance")).getValue();
                }
                valuesEdit = true;
            }
            if (upgrade.getProperties().getValue().containsKey("duration"))
            {
                if (!upgrade.getProperties().getValue().containsKey("addset_duration") || ((StringTag) upgrade.getProperties().getValue().get("addset_duration")).getValue().equals("add"))
                {
                    duration += ((IntTag) upgrade.getProperties().getValue().get("duration")).getValue();
                }
                else
                {
                    duration = ((IntTag) upgrade.getProperties().getValue().get("duration")).getValue();
                }
                valuesEdit = true;
            }
            chance = Math.min(chance, 100);
            if (!quiet && valuesEdit)
            {
                myPet.sendMessageToOwner(MyPetUtil.formatText(MyPetLocales.getString("Message.Skill.Poison.Upgrade", myPet.getOwner().getLanguage()), myPet.getPetName(), chance, duration));
            }
        }
    }

    public String getFormattedValue()
    {
        return chance + "% -> " + duration + "sec";
    }

    public void reset()
    {
        chance = 0;
        duration = 0;
    }

    public boolean activate()
    {
        return random.nextDouble() <= chance / 100.;
    }

    public int getDuration()
    {
        return duration;
    }

    public void poisonTarget(LivingEntity target)
    {
        PotionEffect effect = new PotionEffect(PotionEffectType.POISON, getDuration() * 20, 1);
        target.addPotionEffect(effect);
        target.getWorld().playEffect(target.getLocation(), Effect.POTION_BREAK, 0x008040);
    }

    @Override
    public ISkillInstance cloneSkill()
    {
        Poison newSkill = new Poison(this.isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}