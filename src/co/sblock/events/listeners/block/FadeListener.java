package co.sblock.events.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockFadeEvents.
 * 
 * @author Jikoo
 */
public class FadeListener implements Listener {

	/**
	 * EventHandler for BlockFadeEvents.
	 * 
	 * @param event the BlockFadeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleFade(event));
		}
	}
}