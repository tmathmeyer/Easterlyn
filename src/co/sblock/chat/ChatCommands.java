package co.sblock.chat;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.data.SblockData;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Broadcast;
import co.sblock.utilities.Log;

/**
 * Command handler for all Chat-related commands.
 * 
 * @author Dublek, Jikoo
 */
public class ChatCommands implements CommandListener {

	@CommandDescription("List all colors.")
	@CommandUsage("&c/color")
	@SblockCommand
	public boolean color(CommandSender sender, String[] args) {
		sender.sendMessage(ColorDef.listColors());
		return true;
	}

	@CommandDenial("&0&lYOU SEE NOTHING.")
	@CommandDescription("The voice of the god Anaash. Can be recolored for plaintext messaging.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/an <text>")
	@SblockCommand(consoleFriendly = true)
	public boolean an(CommandSender sender, String[] text) {
		Broadcast.general(ChatColor.BLACK + ChatColor.BOLD.toString() + ChatColor.translateAlternateColorCodes('&', StringUtils.join(text, ' ').toUpperCase()));
		return true;
	}

	@CommandDenial("The aetherial realm eludes your grasp once more.")
	@CommandDescription("For usage in console largely. Talks in #Aether.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/aether <text>")
	@SblockCommand(consoleFriendly = true)
	public boolean aether(CommandSender sender, String[] text) {
		String message = "[#Aether]";
		if (!text[0].equals(">")) {
			message += " ";
		}
		message += StringUtils.join(text, ' ');
		for (Player p : Bukkit.getOnlinePlayers()) {
			User u = User.getUser(p.getUniqueId());
			if (!u.isSuppressing()) {
				u.sendMessage(message, true);
			}
		}
		Bukkit.getConsoleSender().sendMessage(message);
		return true;
	}

	@CommandDenial("&0Lul.")
	@CommandDescription("/le, now with 250% more &kbrain pain.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/lel <text>")
	@SblockCommand(consoleFriendly = true)
	public boolean lel(CommandSender sender, String[] text) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			msg.append(text[i].toUpperCase()).append(' ');
		}
		StringBuilder lelOut = new StringBuilder();
		for (int i = 0; i < msg.length();) {
			for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
				if (i >= msg.length())
					break;
				lelOut.append(ColorDef.RAINBOW[j]).append(ChatColor.MAGIC).append(msg.charAt(i));
				i++;
			}
		}
		Broadcast.general(lelOut.substring(0, lelOut.length() - 1 > 0 ? lelOut.length() - 1 : 0));
		return true;
	}

	@CommandDenial("&0Le no. Le /le is reserved for le fancy people.")
	@CommandDescription("&4He's already here!")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/le <text>")
	@SblockCommand(consoleFriendly = true)
	public boolean le(CommandSender sender, String[] text) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			msg.append(text[i].toUpperCase()).append(' ');
		}
		StringBuilder leOut = new StringBuilder();
		for (int i = 0; i < msg.length();) {
			for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
				if (i >= msg.length())
					break;
				leOut.append(ColorDef.RAINBOW[j]).append(msg.charAt(i));
				i++;
			}
		}
		Broadcast.general(leOut.substring(0, leOut.length() - 1 > 0 ? leOut.length() - 1 : 0));
		return true;
	}

	@CommandDescription("Check data stored for a player")
	@CommandUsage("/whois <exact player>")
	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		if (sender instanceof Player && !sender.hasPermission("group.denizen")) {
			((Player) sender).performCommand("profile " + target[0]);
			return true;
		}
		Player p = Bukkit.getPlayer(target[0]);
		if (p == null) {
			SblockData.getDB().startOfflineLookup(sender, target[0]);
			return true;
		}
		User u = User.getUser(p.getUniqueId());
		sender.sendMessage(u.toString());
		return true;
	}

	@CommandDenial("&l[o] You try to be the white text guy, but fail to be the white text guy. "
					+ "No one can be the white text guy except for the white text guy.")
	@CommandDescription("> Be the white text guy")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/o <text>")
	@SblockCommand(consoleFriendly = true)
	public boolean o(CommandSender sender, String text[]) {
		if (text == null || text.length == 0) {
			sender.sendMessage(ChatColor.BOLD + "[o] If you're going to speak for me, please proceed.");
			return true;
		}
		StringBuilder o = new StringBuilder(ChatColor.BOLD.toString()).append("[o] ");
		for (String s : text) {
			o.append(s).append(' ');
		}
		Broadcast.general(o.substring(0, o.length() - 1 > 0 ? o.length() - 1 : 0));
		return true;
	}

	@CommandDenial
	@CommandDescription("YOU CAN'T ESCAPE THE RED MILES.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/sban <target>")
	@SblockCommand(consoleFriendly = true)
	public boolean sban(CommandSender sender, String[] args) {
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a player.");
			return true;
		}
		String target = args[0];
		StringBuilder reason = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			reason.append(args[i]).append(' ');
		}
		if (args.length == 1) {
			reason.append("Git wrekt m8.");
		}
		if (target.contains(".")) { // IPs probably shouldn't be announced.
			Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(target, reason.toString(), null, "sban");
		} else {
			Broadcast.general(ChatColor.DARK_RED + target
					+ " has been wiped from the face of the multiverse. " + reason.toString());
			Player p = Bukkit.getPlayer(target);
			if (p != null) {
				User victim = User.getUser(p.getUniqueId());
				SblockData.getDB().addBan(victim, reason.toString());
				SblockData.getDB().deleteUser(victim.getPlayerName());
				victim.getPlayer().kickPlayer(reason.toString());
			} else {
				Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target, reason.toString(), null, "sban");
			}
		}
		Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
		return true;
	}

	@CommandDenial
	@CommandDescription("DO THE WINDY THING.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/unsban <UUID|name|IP>")
	@SblockCommand(consoleFriendly = true)
	public boolean unsban(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			return false;
		}
		SblockData.getDB().removeBan(target[0]);
		if (target[0].contains(".")) {
			sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + target[0]
					+ " may be an IP.");
		} else {
			Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + target[0] + " has been unbanned.");
		}
		return true;
	}

	@CommandDescription("SblockChat's main command")
	@CommandUsage("/sc")
	@SblockCommand
	public boolean sc(CommandSender sender, String[] args) {
		User user = User.getUser(((Player) sender).getUniqueId());
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatMsgs.helpDefault());
			return true;
		}

		args[0] = args[0].toLowerCase();
		if (args[0].equals("c")) {
			return scC(user, args);
		} else if (args[0].equals("l")) {
			return scL(user, args);
		} else if (args[0].equals("leave")) {
			return scLeave(user, args);
		} else if (args[0].equals("list")) {
			return scList(user, args);
		} else if (args[0].equals("listall")) {
			return scListAll(user, args);
		} else if (args[0].equals("new")) {
			return scNew(user, args);
		} else if (args[0].equals("nick")) {
			return scNick(user, args);
		} else if (args[0].equals("suppress")) {
			user.setSuppressing(!user.isSuppressing());
			user.sendMessage(ChatColor.GREEN + "Suppression toggled!", false);
			return true;
		} else if (args[0].equals("channel")) {
			return scChannel(user, args);
		} else if (args[0].equals("global")) {
			return scGlobal(user, args);
		} else {
			sender.sendMessage(ChatMsgs.helpDefault());
		}
		return true;
	}

	@CommandDescription("#>does an action")
	@CommandUsage("/me (@channel) <message>")
	@SblockCommand
	public boolean me(CommandSender sender, String[] args) {
		User.getUser(((Player) sender).getUniqueId()).chat(StringUtils.join(args, ' '), true);
		return true;
	}

	@CommandDenial
	@CommandDescription("Help people find their way")
	@CommandPermission("group.felt")
	@CommandUsage("/forcechannel <channel> <player>")
	@SblockCommand(consoleFriendly = true)
	public boolean forcechannel(CommandSender sender, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[0]);
		if (c == null) {
			sender.sendMessage(ChatMsgs.errorInvalidChannel(args[0]));
			return true;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		User user = User.getUser(p.getUniqueId());
		user.setCurrent(c);
		sender.sendMessage(ChatColor.GREEN + "Channel forced!");
		return true;
	}

	private boolean scC(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCC(), false);
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]), false);
			return true;
		}
		if (c.getType().equals(ChannelType.REGION) && !user.isListening(c)) {
			user.sendMessage(ChatMsgs.errorRegionChannelJoin(), false);
			return true;
		}
		user.setCurrent(c);
		return true;
	}

	private boolean scL(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCL(), false);
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]), false);
			return true;
		}
		if (c.getType().equals(ChannelType.REGION)) {
			user.sendMessage(ChatMsgs.errorRegionChannelJoin(), false);
			return true;
		}
		user.addListening(c);
		return true;
	}

	private boolean scLeave(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCLeave(), false);
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]), false);
			user.removeListening(args[1]);
			return true;
		}
		if (c.getType().equals(ChannelType.REGION)) {
			user.sendMessage(ChatMsgs.errorRegionChannelLeave(), false);
			return true;
		}
		user.removeListening(args[1]);
		return true;
		
	}

	private boolean scList(User user, String[] args) {
		StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append("Currently pestering: ");
		for (String s : user.getListening()) {
			sb.append(s).append(' ');
		}
		user.sendMessage(sb.toString(), false);
		return true;
	}

	private boolean scListAll(User user, String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.YELLOW).append("All channels: ");
		for (Channel c : ChannelManager.getChannelManager().getChannelList().values()) {
			ChatColor cc;
			if (user.isListening(c)) {
				cc = ChatColor.YELLOW;
			} else if (c.getAccess().equals(AccessLevel.PUBLIC)) {
				cc = ChatColor.GREEN;
			} else {
				cc = ChatColor.RED;
			}
			sb.append(cc).append(c.getName()).append(' ');
		}
		user.sendMessage(sb.toString(), false);
		return true;
	}

	private boolean scNew(User user, String[] args) {
		if (args.length != 4) {
			user.sendMessage(ChatMsgs.helpSCNew(), false);
			return true;
		}
		if (args[1].length() > 16) {
			user.sendMessage(ChatMsgs.errorChannelName(), false);
		} else if (args[1].charAt(0) != '#' && !user.getPlayer().hasPermission("group.denizen")) {
			user.sendMessage(ChatMsgs.errorChannelName(), false);
		} else if (ChannelType.getType(args[3]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidType(args[3]), false);
		} else if (AccessLevel.getAccess(args[2]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidAccess(args[2]), false);
		} else {
			ChannelManager.getChannelManager().createNewChannel(args[1],
					AccessLevel.getAccess(args[2]), user.getUUID(), ChannelType.getType(args[3]));
			Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
			user.sendMessage(ChatMsgs.onChannelCreation(c), false);
		}
		return true;
	}

	private boolean scNick(User user, String[] args) {
		Channel c = user.getCurrent();
		if (c == null) {
			user.sendMessage(ChatMsgs.errorNoCurrent(), false);
			return true;
		}
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCNick(), false);
			return true;
		}
		if (!(c instanceof NickChannel)) {
			user.sendMessage(ChatMsgs.unsupportedOperation(c.getName()), false);
			return true;
		}
		if (args[1].equalsIgnoreCase("list")) {
			if (c.getType() == ChannelType.NICK) {
				user.sendMessage(ChatColor.YELLOW
						+ "You can use any nick you want in a nick channel.", false);
				return true;
			}
			StringBuilder sb = new StringBuilder(ChatColor.YELLOW.toString()).append("Nicks: ");
			for (CanonNicks n : CanonNicks.values()) {
				if (n != CanonNicks.SERKITFEATURE) {
					sb.append(ChatColor.AQUA).append(n.getName());
					sb.append(ChatColor.YELLOW).append(", ");
				}
			}
			user.sendMessage(sb.substring(0, sb.length() - 4).toString(), false);
			return true;
		}
		if (args.length == 2) {
			user.sendMessage(ChatMsgs.helpSCNick(), false);
			return true;
		}
		if (args[1].equalsIgnoreCase("set")) {
			c.setNick(user, StringUtils.join(args, ' ', 2, args.length));
			return true;
		} else if (args[1].equalsIgnoreCase("remove")) {
			c.removeNick(user, true);
			return true;
		} else {
			user.sendMessage(ChatMsgs.helpSCNick(), false);
			return true;
		}
	}

	private boolean scGlobal(User user, String[] args) {
		if (!user.getPlayer().hasPermission("group.denizen")) {
			return false;
		}
		if (args.length == 4 && args[1].equalsIgnoreCase("setnick")) {
			scGlobalSetNick(user, args);
			return true;
		} else if (args.length >= 3) {
			if (args[1].equalsIgnoreCase("mute")) {
				scGlobalMute(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("unmute")) {
				scGlobalUnmute(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("rmnick")) {
				scGlobalRmNick(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("clearnicks")) {
				for (User u : UserManager.getUserManager().getUserlist()) {
					if (!u.getPlayer().getDisplayName().equals(u.getPlayerName())) {
						u.getPlayer().setDisplayName(u.getPlayerName());
					}
				}
			}
		}
		user.sendMessage(ChatMsgs.helpGlobalMod(), false);
		return true;
	}

	private void scGlobalSetNick(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]), false);
			return;
		}
		User victim = User.getUser(Bukkit.getPlayer(args[2]).getUniqueId());
		victim.getPlayer().setDisplayName(args[3]);
		String msg = ChatMsgs.onUserSetGlobalNick(args[2], args[3]);
		for (User u : UserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg, false);
		}
		Log.anonymousInfo(msg);
	}

	private void scGlobalRmNick(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]), false);
			return;
		}
		User victim = User.getUser(p.getUniqueId());
		String msg = ChatMsgs.onUserRmGlobalNick(args[2], p.getDisplayName());
		for (User u : UserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg, false);
		}
		Log.anonymousInfo(msg);
		p.setDisplayName(victim.getPlayerName());
	}

	private void scGlobalMute(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]), false);
			return;
		}
		User victim = User.getUser(p.getUniqueId());
		victim.setMute(true);
		String msg = ChatMsgs.onUserMute(args[2]);
		for (User u : UserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg, false);
		}
		Log.anonymousInfo(msg);
	}

	private void scGlobalUnmute(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]), false);
			return;
		}
		User victim = User.getUser(p.getUniqueId());
		victim.setMute(false);;
		String msg = ChatMsgs.onUserUnmute(args[2]);
		for (User u : UserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg, false);
		}
		Log.anonymousInfo(msg);
	}

	private boolean scChannel(User user, String[] args) {
		Channel c = user.getCurrent();
		if (args.length == 2 && args[1].equalsIgnoreCase("info")) {
			user.sendMessage(c.toString(), false);
			return true;
		}
		if (!c.isChannelMod(user)) {
			user.sendMessage(ChatMsgs.onChannelCommandFail(c.getName()), false);
			return true;
		}
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpChannelMod(), false);
			if (c.isOwner(user)) {
				user.sendMessage(ChatMsgs.helpChannelOwner(), false);
			}
			return true;
		} else if (args.length >= 2 && args[1].equalsIgnoreCase("getlisteners")) {
			StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW);
			sb.append("Channel members: ");
			for (UUID userID : c.getListening()) {
				User u = UserManager.getUserManager().getUser(userID);
				if (u.getCurrent().equals(c)) {
					sb.append(ChatColor.GREEN);
				} else {
					sb.append(ChatColor.YELLOW);
				}
				sb.append(u.getPlayerName()).append(' ');
			}
			user.sendMessage(sb.toString(), false);
			return true;
		} else if (args.length >= 3) {
			if (args[1].equalsIgnoreCase("kick")) {
				c.kickUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("ban")) {
				c.banUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("approve")) {
				c.approveUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("deapprove")) {
				c.deapproveUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			}
		}
		if (c.isOwner(user)) {
			if (args.length >= 4 && args[1].equalsIgnoreCase("mod")) {
				if (args[2].equalsIgnoreCase("add")) {
					c.addMod(user, Bukkit.getPlayer(args[3]).getUniqueId());
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					c.removeMod(user, Bukkit.getPlayer(args[3]).getUniqueId());
					return true;
				} else {
					user.sendMessage(ChatMsgs.helpChannelMod(), false);
					if (c.isOwner(user)) {
						user.sendMessage(ChatMsgs.helpChannelOwner(), false);
					}
					return true;
				}
			} else if (args.length >= 3 && args[1].equalsIgnoreCase("unban")) {
				ChannelManager.getChannelManager().getChannel(c.getName())
						.unbanUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args.length >= 2 && args[1].equalsIgnoreCase("disband")) {
				c.disband(user);
				return true;
			}
		}
		user.sendMessage(ChatMsgs.helpChannelMod(), false);
		if (c.isOwner(user)) {
			user.sendMessage(ChatMsgs.helpChannelOwner(), false);
		}
		return true;
	}
}
