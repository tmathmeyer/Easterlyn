package co.sblock.Sblock.Chat;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Utilities.Sblogger;

public class ChatModule extends Module {

	private static ChatModule instance;
	private ChannelManager cm = new ChannelManager();
	private ChatModuleCommandListener clistener = new ChatModuleCommandListener();
	private static Sblogger log = new Sblogger("SblockChat");

	@Override
	protected void onEnable() {
		slog().info("Enabling SblockChat");
		instance = this;
		this.registerCommands(clistener);
		cm.loadAllChannels();
		this.cm.createDefaultSet();
		slog().info("SblockChat enabled");
	}

	@Override
	protected void onDisable() {
		cm.saveAllChannels();
		instance = null;
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public static ChatModule getChatModule() {
		return instance;
	}

	public static Sblogger slog() {
		return log;
	}
}