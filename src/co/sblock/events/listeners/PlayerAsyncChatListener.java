package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.chat.Message;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.UserManager;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class PlayerAsyncChatListener implements Listener {

	private String[] tests = new String[] {"It is certain.", "It is decidedly so.",
			"Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see, yes.",
			"Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
			"Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
			"Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
			"My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.",
			"Testing complete. Proceeding with operation.", "A critical fault has been discovered while testing.",
			"Error: Test results contaminated.", "tset", "/ping"};

	/**
	 * The event handler for AsyncPlayerChatEvents.
	 * 
	 * @param event the AsyncPlayerChatEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		// Clear recipients so as to not duplicate messages for global chat
		event.getRecipients().clear();
		Message message = new Message(UserManager.getGuaranteedUser(event.getPlayer().getUniqueId()), event.getMessage());
		// Ensure message can be sent
		if (!message.validate(true)) {
			return;
		}
		// Test
		if (message.getCleanedMessage().equalsIgnoreCase("test")) {
			event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
			return;
		}
		if (message.getCleanedMessage().equalsIgnoreCase("hal") || message.getCleanedMessage().equalsIgnoreCase("dirk")) {
			event.getPlayer().sendMessage(ColorDef.HAL + "What?");
			return;
		}
		// Uncancel global chat to play nice with IRC plugins/Dynmap
		if (message.getChannel().getType() == ChannelType.REGION) {
			event.setCancelled(false);
		}
		// Clear @channels, though /me and escaping will remain
		message.send();
		event.setMessage(message.getCleanedMessage());
		event.setFormat("[" + message.getChannel().getName() + "] <%1$s> %2$s");

		String msg = event.getMessage().toLowerCase();
		if (msg.startsWith("halc ") || msg.startsWith("halculate ") || msg.startsWith("evhal ") || msg.startsWith("evhaluate ")) {
			msg = msg.substring(msg.indexOf(' ')).trim();
			final Message hal = new Message("Lil Hal", "");
			hal.setMessage(SblockChat.getChat().getHalculator().evhaluate(msg));
			hal.addColor(ChatColor.RED);
			hal.setChannel(message.getChannel());
			// Delay reply to prevent global channels logging reply before original message
			new BukkitRunnable() {
				@Override
				public void run() {
					hal.send();
				}
			}.runTaskLaterAsynchronously(Sblock.getInstance(), 1L);
			return;
		}

		SblockChat.getChat().getHal().handleMessage(message);
	}
}
