package co.sblock.events.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockGrowEvents.
 * 
 * @author Jikoo
 */
public class GrowListener implements Listener {

	/**
	 * An event handler for a change that is caused by or affects a Block in a
	 * Machine.
	 * 
	 * @param event the BlockGrowEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleGrow(event));
		}
	}
}