package co.sblock.utilities.rawmessages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.Module;
import co.sblock.module.SblockCommand;
import co.sblock.users.User;
import co.sblock.utilities.Log;

/**
 * @author Jikoo
 */
public class RawAnnouncer extends Module implements CommandListener {

	private int taskId;
	private List<MessageElement> announcements;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		announcements = this.constructAnnouncements();
		

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				MessageElement msg = announcements.get((int) (Math.random() * announcements.size()));
				Log.anonymousInfo(msg.getConsoleFriendly());
				String announcement = msg.toString();
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							"tellraw " + p.getName() + " " + announcement);
				}
			}
		}, 0, 1200 * 15); // 15 minutes in between rawnouncments

		this.registerCommands(this);
	}

	/**
	 * Creates a List of all announcements.
	 * 
	 * @return the List created
	 */
	private List<MessageElement> constructAnnouncements() {
		List<MessageElement> msgs = new ArrayList<MessageElement>();

		msgs.add(new MessageHalement("Join us on our subreddit, ").addExtra(
				new MessageElement("/r/Sblock", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL,
								"http://www.reddit.com/r/sblock"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to go!")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("If you're having difficulty with chat, ").addExtra(
				new MessageElement("/sc ?", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.RUN_COMMAND, "/sc ?"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click to run!")),
				new MessageElement(" is your friend!", ChatColor.RED)));

		msgs.add(new MessageHalement("Remember, we are in ").addExtra(
				new MessageElement("ALPHA", ChatColor.GOLD, ChatColor.BOLD)
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.DARK_RED + "We reserve the right to fuck up badly.")),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("Join us on ").addExtra(
				new MessageElement("Mumble", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL,
								"http://mumble.sourceforge.net/"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
								ChatColor.GOLD + "Click here to download!")),
				new MessageElement(" for voice chat! The server is ", ChatColor.RED),
				new MessageElement("   sblock.co", ChatColor.AQUA),
				new MessageElement(", port ", ChatColor.RED),
				new MessageElement("25560", ChatColor.AQUA),
				new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("It appears that enchanting furnaces is very beneficial."
				+ " You might consider giving it a try."));

		msgs.add(new MessageHalement("Curious about your fellow players' classpects? Have a look at their ")
				.addExtra(new MessageElement("/profile", ChatColor.AQUA)
								.addClickEffect(new MessageClick(MessageClick.ClickEffect.RUN_COMMAND, "/profile"))
								.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT,
										ChatColor.GOLD + "Click to run!")),
						new MessageElement("!", ChatColor.RED)));

		msgs.add(new MessageHalement("It is your generosity that keeps Sblock alive. Please consider ").addExtra(
				new MessageElement("donating", ChatColor.AQUA)
						.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://adf.ly/NThbj"))
						.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click here to go!")),
				new MessageElement(" to help.", ChatColor.RED)));

		msgs.add(new MessageHalement("To sleep without dreaming, sneak while right clicking your bed!"));

		msgs.add(new MessageHalement("If you're using our resource pack, we suggest you ").addExtra(
				new MessageElement("download", ChatColor.AQUA)
				.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://sblock.co/rpack/"))
				.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click to see all Sblock rpacks!")),
		new MessageElement(" the sound pack as well.", ChatColor.RED)));

		msgs.add(new MessageHalement("Interested in jamming with your fellow Sblock players? Join our ").addExtra(
				new MessageElement("plug.dj room", ChatColor.AQUA)
				.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://plug.dj/sblock/"))
				.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Click join!")),
		new MessageElement(" to listen and play!", ChatColor.RED)));

		msgs.add(new MessageHalement("Sblock UHC is in the works! Check out ").addExtra(
				new MessageElement("the subreddit post", ChatColor.AQUA)
				.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, "http://www.reddit.com/r/sblock/comments/29znh9/group_event_sblock_uhc/"))
				.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, ChatColor.GOLD + "Check it out!")),
		new MessageElement(" for more info! May the best team win.", ChatColor.RED)));

		return msgs;
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		Bukkit.getScheduler().cancelTask(taskId);
	}

	@SuppressWarnings("deprecation")
	@CommandDenial
	@CommandDescription("Force a raw message announcement or talk as Hal.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/hal [0-8|text]")
	@SblockCommand(consoleFriendly = true)
	public boolean hal(CommandSender s, String[] args) {
		MessageElement msg;
		if (args.length == 1) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > announcements.size()) {
					s.sendMessage(ChatColor.RED.toString() + announcements.size() + " announcements exist currently.");
					msgNum = announcements.size();
				}
				msg = announcements.get(msgNum - 1);
			} catch (NumberFormatException e) {
				msg = new MessageHalement(args[0]);
			}
		} else if (args.length > 0) {
			msg = new MessageHalement(StringUtils.join(args, ' '));
		} else {
			msg = announcements.get((int) (Math.random() * announcements.size()));
		}
		Log.anonymousInfo(msg.getConsoleFriendly());
		String announcement = msg.toString();
		for (Player p : Bukkit.getOnlinePlayers()) {
			User.getUser(p.getUniqueId()).rawHighlight(announcement);
			//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + announcement);
		}
		return true;
	}

	@Override
	protected String getModuleName() {
		return "RawAnnouncer";
	}
}
