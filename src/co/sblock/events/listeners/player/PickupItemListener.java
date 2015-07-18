package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PickupItemListener implements Listener {

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		OfflineUser offUser = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (!(offUser instanceof OnlineUser)) {
			return;
		}
		OnlineUser user = (OnlineUser) offUser;

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
