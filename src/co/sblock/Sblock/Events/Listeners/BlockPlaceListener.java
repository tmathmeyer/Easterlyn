package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Direction;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.Machines.Type.PBO;

/**
 * Listener for BlockPlaceEvents.
 * 
 * @author Jikoo
 */
public class BlockPlaceListener implements Listener {

	/**
	 * Event handler for Machine construction.
	 * 
	 * @param event the BlockPlaceEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			// Block registered as part of a machine. Most likely removed by explosion or similar.
			// Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
			// location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You decide against fussing with the internals of this machine.");
		}

		// Machine place logic
		for (MachineType mt : MachineType.values()) {
			ItemStack is = mt.getUniqueDrop();
			is.setAmount(event.getItemInHand().getAmount());
			if (is.equals(event.getItemInHand())) {
				if (mt == MachineType.PERFECT_BUILDING_OBJECT) {
					new PBO(event.getBlock().getLocation(), "").assemble(event);
					break;
				}
				try {
					SblockMachines.getMachines().getManager().addMachine(
							event.getBlock().getLocation(), mt, mt.getData(event),
							Direction.getFacingDirection(event.getPlayer())).assemble(event);
				} catch (NullPointerException e) {
					SblockMachines.getMachines().getLogger().debug("Invalid machine placed.");
					event.setBuild(false);
					event.setCancelled(true);
				}
				break;
			}
		}
	}
}
