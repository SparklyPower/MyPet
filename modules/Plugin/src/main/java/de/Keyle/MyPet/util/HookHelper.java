/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2016 Keyle
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

package de.Keyle.MyPet.util;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.util.Since;
import de.Keyle.MyPet.api.util.hooks.types.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class HookHelper extends de.Keyle.MyPet.api.util.hooks.HookHelper {
    @Override
    public boolean canHurt(Player attacker, Player defender, boolean viceversa) {
        if (!attacker.getWorld().getPVP()) {
            return false;
        }
        if (!canHurt(attacker, defender) || (viceversa && !canHurt(defender, attacker))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canHurt(Player attacker, Player defender) {
        if (Configuration.Misc.DISABLE_PET_VS_PLAYER) {
            return false;
        }
        if (attacker != null && defender != null && attacker != defender) {
            if (!attacker.getWorld().getPVP()) {
                return false;
            }
            List<PlayerVersusPlayerHook> pvpHooks = MyPetApi.getPluginHookManager().getHooks(PlayerVersusPlayerHook.class);
            for (PlayerVersusPlayerHook hook : pvpHooks) {
                if (!hook.canHurt(attacker, defender)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canHurt(Player attacker, Entity defender) {
        if (defender instanceof Player) {
            return canHurt(attacker, (Player) defender);
        }
        if (attacker != null && defender != null && attacker != defender) {
            List<PlayerVersusEntityHook> pvpHooks = MyPetApi.getPluginHookManager().getHooks(PlayerVersusEntityHook.class);
            for (PlayerVersusEntityHook hook : pvpHooks) {
                if (!hook.canHurt(attacker, defender)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Deprecated
    @Since("24.11.2016")
    public boolean canUseMyPet(MyPetPlayer player) {
        return isInArena(player);
    }

    @Override
    public boolean canMyPetFlyAt(Location location) {
        return false;
    }

    @Override
    public boolean isInArena(MyPetPlayer player) {
        List<ArenaHook> arenaHooks = MyPetApi.getPluginHookManager().getHooks(ArenaHook.class);
        for (ArenaHook hook : arenaHooks) {
            if (!hook.isInArena(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isInParty(Player player) {
        List<PartyHook> arenaHooks = MyPetApi.getPluginHookManager().getHooks(PartyHook.class);
        for (PartyHook hook : arenaHooks) {
            if (!hook.isInParty(player)) {
                return false;
            }
        }
        return false;
    }

    @Override
    public List<Player> getPartyMembers(Player player) {
        List<PartyHook> arenaHooks = MyPetApi.getPluginHookManager().getHooks(PartyHook.class);
        for (PartyHook hook : arenaHooks) {
            List<Player> members = hook.getPartyMembers(player);
            if (members != null) {
                return members;
            }
        }
        return null;
    }

    public EconomyHook getEconomy() {
        List<EconomyHook> arenaHooks = MyPetApi.getPluginHookManager().getHooks(EconomyHook.class);
        for (EconomyHook hook : arenaHooks) {
            return hook;
        }
        return null;
    }
}
