package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * Listener for PlayerInteractEntityEvents.
 * 
 * @author Jikoo
 */
public class PlayerInteractEntityListener implements Listener {

	/**
	 * EventHandler for PlayerInteractEntityEvents.
	 * 
	 * @param event the PlayerInteractEntityEvent
	 */
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			event.getPlayer().sendMessage(ChatColor.RED + "You huff and you puff, but all you get is a bit hyperventilated.");
			event.setCancelled(true);
			return;
		}
	}
}