package co.sblock.users;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.chat.Color;
import co.sblock.module.Module;

/**
 * Class that keeps track of players currently logged on to the game.
 * 
 * @author FireNG, Jikoo
 */
public class Users extends Module {

	private static Users instance;

	/* The Map of Player UUID and relevant SblockUsers currently online. */
	private static final Map<UUID, OfflineUser> users = new ConcurrentHashMap<>();

	@Override
	protected void onEnable() {
		instance = this;
		for (Player p : Bukkit.getOnlinePlayers()) {
			getGuaranteedUser(p.getUniqueId());
			team(p);
		}
	}

	@Override
	protected void onDisable() {
		for (OfflineUser u : Users.getUsers().toArray(new OfflineUser[0])) {
			unloadUser(u.getUUID()).save();
			unteam(u.getUUID().toString().replace("-", "").substring(0, 16));
		}
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "Sblock UserManager";
	}

	public static Users getInstance() {
		return instance;
	}

	/**
	 * User is not guaranteed to be online, but an OfflineUser will be fetched no matter what.
	 * 
	 * @param uuid
	 * @return
	 */
	public static OfflineUser getGuaranteedUser(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid);
		}
		OfflineUser user = OfflineUser.load(uuid);
		if (user.isOnline()) {
			user = user.getOnlineUser();
			users.put(uuid, user);
		} else {
			return user;
		}
		return user;
	}

	protected static OnlineUser getOnlineUser(UUID uuid) {
		if (users.containsKey(uuid)) {
			OfflineUser user = users.get(uuid);
			if (user instanceof OnlineUser) {
				return (OnlineUser) user;
			}
		}
		return null;
	}

	/**
	 * Adds the given User.
	 * 
	 * @param user the User to add
	 */
	public static void addUser(OfflineUser user) {
		users.put(user.getUUID(), user);
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the name of the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public static OfflineUser unloadUser(UUID userID) {
		if (!users.containsKey(userID)) {
			return null;
		}
		return users.remove(userID);
	}

	/**
	 * Gets a Collection of OfflineUsers currently loaded.
	 * 
	 * @return the OfflineUsers
	 */
	public static Collection<OfflineUser> getUsers() {
		return users.values();
	}

	/**
	 * Add a Player to their group's Team.
	 * 
	 * @param p the Player
	 */
	public static void team(Player p) {
		String teamPrefix = null;
		for (org.bukkit.ChatColor c : org.bukkit.ChatColor.values()) {
			if ((teamPrefix == null || c.isFormat()) && p.hasPermission("sblockchat." + c.name().toLowerCase())) {
				teamPrefix = c.toString();
			}
		}
		if (teamPrefix != null) {
			// Do nothing, we've got a fancy override going on
		} else if (p.hasPermission("sblock.horrorterror")) {
			teamPrefix = Color.RANK_HORRORTERROR.toString();
		} else if (p.hasPermission("sblock.denizen")) {
			teamPrefix = Color.RANK_DENIZEN.toString();
		} else if (p.hasPermission("sblock.felt")) {
			teamPrefix = Color.RANK_FELT.toString();
		} else if (p.hasPermission("sblock.helper")) {
			teamPrefix = Color.RANK_HELPER.toString();
		} else if (p.hasPermission("sblock.donator")) {
			teamPrefix = Color.RANK_DONATOR.toString();
		} else if (p.hasPermission("sblock.godtier")) {
			teamPrefix = Color.RANK_GODTIER.toString();
		} else {
			teamPrefix = Color.RANK_HERO.toString();
		}
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		String teamName = p.getName();
		Team team = board.getTeam(teamName);
		if (team == null) {
			team = board.registerNewTeam(teamName);
		}
		team.setPrefix(teamPrefix);
		team.addPlayer(p);

		Objective objective = board.getObjective("deaths");
		if (objective == null) {
			objective = board.registerNewObjective("deaths", "deathCount");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}

		// Since Mojang doesn't, we'll force deathcount to persist - it's been a feature for ages
		Score score = objective.getScore(p.getDisplayName());
		score.setScore(p.getStatistic(Statistic.DEATHS));
	}

	public static void unteam(Player player) {
		unteam(player.getName());
	}

	private static void unteam(String teamName) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
		if (team != null) {
			team.unregister();
		}
	}

	public static Location getSpawnLocation() {
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
}
