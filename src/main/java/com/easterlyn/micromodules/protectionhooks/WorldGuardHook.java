package com.easterlyn.micromodules.protectionhooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Hook for the protection plugin <a href=http://dev.bukkit.org/bukkit-plugins/worldguard/>WorldGuard</a>.
 *
 * @author Jikoo
 */
public class WorldGuardHook extends ProtectionHook {

	public WorldGuardHook() {
		super("WorldGuard");
	}

	@Override
	public boolean isProtected(Location location) {
		if (!isHookUsable()) {
			return false;
		}
		return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(location)).size() > 0;
	}

	@Override
	public boolean canMobsSpawn(Location location) {
		if (!isHookUsable()) {
			return true;
		}
		return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(location))
				.queryState(null, Flags.MOB_SPAWNING) == State.ALLOW;
	}

	@Override
	public boolean canUseButtonsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(location))
				.queryState(null, Flags.ENTRY, Flags.USE) == State.ALLOW;
	}

	@Override
	public boolean canOpenChestsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(location))
				.queryState(null, Flags.ENTRY, Flags.CHEST_ACCESS) == State.ALLOW;
	}

	@Override
	public boolean canBuildAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(location))
				.queryState(null, Flags.ENTRY, Flags.BUILD) == State.ALLOW;
	}

}
