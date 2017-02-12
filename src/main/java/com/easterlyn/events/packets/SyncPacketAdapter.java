package com.easterlyn.events.packets;


import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.easterlyn.Easterlyn;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PermissionUtils;

import org.bukkit.command.Command;

/**
 * @author Jikoo
 */
public class SyncPacketAdapter extends PacketAdapter {

	public SyncPacketAdapter(Easterlyn plugin) {
		super(plugin, PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE);

		PermissionUtils.addParent("easterlyn.commands.unfiltered", UserRank.HEAD_MOD.getPermission());
	}

	/**
	 * Edit packets outgoing to the client.
	 * 
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.PacketEvent)
	 * 
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("easterlyn.commands.unfiltered")) {
				return;
			}
			event.getPacket().getStringArrays().write(0,
					Arrays.stream(event.getPacket().getStringArrays().read(0)).filter(completion -> {
						if (completion.length() < 1 || completion.indexOf('/') != 0 || completion.indexOf(':') < 0) {
							return true;
						}
						// Certain commands injected by plugins can still be completed for players lacking permissions.
						// Looking at you, WG.
						Command command = ((Easterlyn) this.getPlugin()).getCommandMap().getCommand(completion.substring(1));
						return command == null || command.getPermission() == null || command.getPermission().isEmpty()
								|| event.getPlayer().hasPermission(command.getPermission());
					}).toArray(size -> new String[size]));
		}
	}

	/**
	 * Check a packet from the client.
	 * 
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketReceiving(PacketEvent)
	 * 
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("easterlyn.commands.unfiltered")) {
				return;
			}
			String completing = event.getPacket().getStrings().read(0);
			int colon = completing.indexOf(':'), space = completing.indexOf(' ');
			if (colon > 0 && (space < 0 || space > colon)) {
				event.setCancelled(true);
			}
		}
	}

}
