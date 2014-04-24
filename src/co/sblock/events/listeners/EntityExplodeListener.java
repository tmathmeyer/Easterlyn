package co.sblock.events.listeners;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for EntityExplodeEvents.
 * 
 * @author Jikoo
 */
public class EntityExplodeListener implements Listener {

	/**
	 * EventHandler for EntityExplodeEvents.
	 * 
	 * @param event the EntityExplodeEvent
	 */
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		HashSet<Machine> affected = new HashSet<Machine>();
		for (Block b : event.blockList()) {
			Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
			if (m != null) {
				affected.add(m);
			}
		}

		for (Machine m : affected) {
			m.dodge();
		}
	}
}