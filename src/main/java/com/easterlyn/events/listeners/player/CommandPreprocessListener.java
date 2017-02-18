package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.utility.OopsCommand;
import com.easterlyn.discord.Discord;
import com.easterlyn.discord.DiscordPlayer;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PermissionUtils;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Listener for PlayerCommandPreprocessEvents.
 * 
 * @author Jikoo
 */
public class CommandPreprocessListener extends EasterlynListener {

	private final AwayFromKeyboard afk;
	private final Chat chat;
	private final Discord discord;
	private final Language lang;
	private final Spectators spectators;
	private final SimpleCommandMap map;

	public CommandPreprocessListener(Easterlyn plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.chat = plugin.getModule(Chat.class);
		this.discord = plugin.getModule(Discord.class);
		this.lang = plugin.getModule(Language.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.map = plugin.getCommandMap();

		PermissionUtils.addParent("easterlyn.commands.unfiltered", UserRank.HEAD_MOD.getPermission());
		PermissionUtils.addParent("easterlyn.commands.unfiltered", "easterlyn.spam");
		PermissionUtils.addParent("easterlyn.commands.unlogged", UserRank.MOD.getPermission());
		PermissionUtils.addParent("easterlyn.commands.unlogged", "easterlyn.spam");
	}

	/**
	 * EventHandler for PlayerCommandPreprocessEvents.
	 * 
	 * @param event the PlayerCommandPreprocessEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		afk.extendActivity(event.getPlayer());

		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("easterlyn.commands.unfiltered") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon + 1));
		}

		String command = event.getMessage().substring(1, space > 0 ? space : event.getMessage().length()).toLowerCase();
		Command cmd = map.getCommand(command);

		if (!event.getPlayer().hasPermission("easterlyn.commands.unlogged") && cmd != null
				&& !discord.getConfig().getStringList("discord.command-log-blacklist").contains(cmd.getName())) {
			discord.log(event.getPlayer().getName() + " issued command: " + event.getMessage());
		}

		if (((OopsCommand) map.getCommand("oops"))
				.handleFailedCommand(event.getPlayer(), command, space > 0
						? event.getMessage().substring(space + 1) : null)) {
			event.setCancelled(true);
			return;
		}

		if (cmd == null) {
			return;
		}

		if (spectators.isSpectator(event.getPlayer().getUniqueId())
				&& cmd.getName().equals("sethome")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(lang.getValue("events.command.spectatefail"));
			return;
		}

		if (cmd.getName().equals("mail")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("send")
					&& chat.testForMute(event.getPlayer())) {
				event.setCancelled(true);
			}
		} else if (cmd.getName().equals("prism")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("undo")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(lang.getValue("events.command.prismUndoCrash"));
			}
		} else if (cmd.getName().equals("fly")) {
			if ((event.getPlayer().hasPermission("essentials.fly")
					|| event.getPlayer().hasPermission("essentials.*"))) {
				event.getPlayer().setFallDistance(0);
			}
		} else if (cmd.getName().equals("gc")) {
			if (!event.getPlayer().hasPermission("essentials.gc")
					&& !event.getPlayer().hasPermission("essentials.*")) {
				event.setMessage("/tps");
				command = "tps";
			}
		}

		if (event.getPlayer() instanceof DiscordPlayer) {
			if (!discord.getConfig().getStringList("discord.command-whitelist").contains(cmd.getName())) {
				event.getPlayer().sendMessage('/' + cmd.getName() + " isn't allowed from Discord, sorry!");
				event.setCancelled(true);
			}
		}

	}

}