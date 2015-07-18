package co.sblock.commands.entry;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.events.packets.ParticleEffectWrapper;
import co.sblock.events.packets.ParticleUtils;

/**
 * SblockCommand for riding a firework in style.
 * 
 * @author Jikoo
 */
public class CrotchRocketCommand extends SblockCommand {

	public CrotchRocketCommand() {
		super("crotchrocket");
		this.setDescription("Uncomfortably fun!");
		this.setUsage("/crotchrocket");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		final Player player = (Player) sender;
		return launch(player);
	}

	public boolean launch(Player player) {
		player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);

		final Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.setPassenger(player);

		ParticleUtils.getInstance().addEntity(firework, new ParticleEffectWrapper(Effect.FIREWORKS_SPARK, 5));

		final int velocityCorrectionTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				firework.setVelocity(new Vector(0, 2, 0));
			}
		}, 0, 1L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				ParticleUtils.getInstance().removeAllEffects(firework);
				Bukkit.getScheduler().cancelTask(velocityCorrectionTask);
				firework.remove();
			}
		}, 40L);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
