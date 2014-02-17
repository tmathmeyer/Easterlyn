package co.sblock.Sblock.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * A tiny class used to ensure that all announcements follow the same format.
 * 
 * @author Jikoo
 *
 */
public class Broadcast {

	/**
	 * Broadcast as Lil Hal to all users.
	 */
	public static void lilHal(String msg) {
		Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + msg);
	}

	/**
	 * General broadcast to all users.
	 */
	public static void general(String msg) {
		Bukkit.broadcastMessage(msg);
	}
}