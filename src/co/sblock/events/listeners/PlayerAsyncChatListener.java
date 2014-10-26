package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.chat.Message;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.ChannelType;
import co.sblock.data.SblockData;
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
		if (event.getMessage().equalsIgnoreCase("test")) {
			event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
			return;
		}
		// Clear recipients so as to not duplicate messages for global chat
		event.getRecipients().clear();
		Message message = new Message(UserManager.getUser(event.getPlayer().getUniqueId()), event.getMessage());
		if (message.getSender() == null) {
			event.getPlayer().sendMessage(ChatColor.BOLD
					+ "[o] Your Sblock playerdata appears to not be loaded."
					+ "\nI'll take care of that for you, but make sure your /profile is correct.");
			SblockData.getDB().loadUserData(event.getPlayer().getUniqueId());
			return;
		}
		// Ensure message can be sent
		if (!message.validate(true)) {
			return;
		}
		// Uncancel global chat to play nice with IRC plugins/Dynmap
		if (message.getChannel().getType() == ChannelType.REGION) {
			event.setCancelled(false);
		}
		// Clear @channels, though /me and escaping will remain
		message.prepare();
		event.setMessage(message.getConsoleMessage());
		message.send();
		event.setFormat("[" + message.getChannel().getName() + "] <%1$s> %2$s");

		if (event.getMessage().toLowerCase().startsWith("halc ") || event.getMessage().toLowerCase().startsWith("halculate ")) {
			com.fathzer.soft.javaluator.DoubleEvaluator eval = new com.fathzer.soft.javaluator.DoubleEvaluator();
			String substring = event.getMessage().substring(event.getMessage().indexOf(' '));
			Message hal = new Message("Lil Hal", "");
			try {
				 hal.setMessage(substring.trim() + " = " + eval.evaluate(substring));
			} catch (IllegalArgumentException e) {
				if (substring.matches("\\A\\s*[Mm]([Yy]|[Aa][Hh]?) ([Dd][Ii][Cc]?[Kk]|[Cc][Oo][Cc][Kk]|[Pp][Ee][Nn][Ii][Ss])\\s*\\Z")) {
					hal.setMessage("Sorry, your equation is too tiny for me to read.");
				} else {
					hal.setMessage("Sorry, I can't read that equation!");
				}
			}
			hal.addColor(ChatColor.RED);
			hal.setChannel(message.getChannel());
			hal.prepare();
			hal.send();
			return;
		}

		SblockChat.getChat().getHal().handleMessage(message);
	}
}
