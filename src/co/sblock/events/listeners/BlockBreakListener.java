package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.progression.ServerMode;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BlockBreakListener implements Listener {

	/**
	 * The event handler for Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleBreak(event));
		}

		OfflineUser u = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (u != null && u.isServer()) {
			event.setCancelled(!ServerMode.getInstance().isWithinRange(u, event.getBlock()));
		}
	}
}
