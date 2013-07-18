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

package de.Keyle.MyPet.chatcommands;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.skill.skills.implementation.Behavior;
import de.Keyle.MyPet.skill.skills.info.BehaviorInfo.BehaviorState;
import de.Keyle.MyPet.util.MyPetPermissions;
import de.Keyle.MyPet.util.MyPetUtil;
import de.Keyle.MyPet.util.locale.MyPetLocales;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandBehavior implements CommandExecutor, TabCompleter
{
    private static List<String> behaviorList = new ArrayList<String>();
    private static List<String> emptyList = new ArrayList<String>();

    static
    {
        behaviorList.add("normal");
        behaviorList.add("friendly");
        behaviorList.add("aggressive");
        behaviorList.add("raid");
        behaviorList.add("farm");
        behaviorList.add("duel");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player petOwner = (Player) sender;
            if (MyPetList.hasMyPet(petOwner))
            {
                MyPet myPet = MyPetList.getMyPet(petOwner);

                if (myPet.getStatus() == PetState.Despawned)
                {
                    sender.sendMessage(MyPetUtil.formatText(MyPetLocales.getString("Message.CallFirst", petOwner), myPet.getPetName()));
                    return true;
                }
                else if (myPet.getSkills().hasSkill("Behavior"))
                {
                    Behavior behaviorSkill = (Behavior) myPet.getSkills().getSkill("Behavior");
                    if (args.length == 1)
                    {
                        if ((args[0].equalsIgnoreCase("friendly") || args[0].equalsIgnoreCase("friend")))
                        {
                            if (!MyPetPermissions.hasExtended(petOwner, "MyPet.user.extended.Behavior.Friendly") || !behaviorSkill.isModeUsable(BehaviorState.Friendly))
                            {
                                myPet.sendMessageToOwner(MyPetLocales.getString("Message.NotAllowed", petOwner));
                                return true;
                            }
                            behaviorSkill.activateBehavior(Behavior.BehaviorState.Friendly);
                        }
                        else if ((args[0].equalsIgnoreCase("aggressive") || args[0].equalsIgnoreCase("Aggro")))
                        {
                            if (!MyPetPermissions.hasExtended(petOwner, "MyPet.user.extended.Behavior.aggressive") || !behaviorSkill.isModeUsable(BehaviorState.Aggressive))
                            {
                                myPet.sendMessageToOwner(MyPetLocales.getString("Message.NotAllowed", petOwner));
                                return true;
                            }
                            behaviorSkill.activateBehavior(Behavior.BehaviorState.Aggressive);
                        }
                        else if (args[0].equalsIgnoreCase("farm"))
                        {
                            if (!MyPetPermissions.hasExtended(petOwner, "MyPet.user.extended.Behavior.Farm") || !behaviorSkill.isModeUsable(BehaviorState.Farm))
                            {
                                myPet.sendMessageToOwner(MyPetLocales.getString("Message.NotAllowed", petOwner));
                                return true;
                            }
                            behaviorSkill.activateBehavior(BehaviorState.Farm);
                        }
                        else if (args[0].equalsIgnoreCase("raid"))
                        {
                            if (!MyPetPermissions.hasExtended(petOwner, "MyPet.user.extended.Behavior.Raid") || !behaviorSkill.isModeUsable(BehaviorState.Raid))
                            {
                                myPet.sendMessageToOwner(MyPetLocales.getString("Message.NotAllowed", petOwner));
                                return true;
                            }
                            behaviorSkill.activateBehavior(Behavior.BehaviorState.Raid);
                        }
                        else if (args[0].equalsIgnoreCase("duel"))
                        {
                            if (!MyPetPermissions.hasExtended(petOwner, "MyPet.user.extended.Behavior.Duel") || !behaviorSkill.isModeUsable(BehaviorState.Duel))
                            {
                                myPet.sendMessageToOwner(MyPetLocales.getString("Message.NotAllowed", petOwner));
                                return true;
                            }
                            behaviorSkill.activateBehavior(Behavior.BehaviorState.Duel);
                        }
                        else if (args[0].equalsIgnoreCase("normal"))
                        {
                            behaviorSkill.activateBehavior(Behavior.BehaviorState.Normal);
                        }
                        else
                        {
                            behaviorSkill.activate();
                            return false;
                        }
                    }
                    else
                    {
                        behaviorSkill.activate();
                    }
                }
                return true;
            }
            else
            {
                sender.sendMessage(MyPetLocales.getString("Message.DontHavePet", petOwner));
            }
            return true;
        }
        sender.sendMessage("You can't use this command from server console!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (strings.length == 1)
        {
            return behaviorList;
        }
        return emptyList;
    }
}