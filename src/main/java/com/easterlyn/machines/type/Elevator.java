package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.computer.BadButton;
import com.easterlyn.machines.type.computer.GoodButton;
import com.easterlyn.machines.type.computer.Program;
import com.easterlyn.machines.type.computer.Programs;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;
import com.easterlyn.utilities.InventoryUtils;
import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * power 20 * 1 second = 19 blocks up
 *
 * @author Jikoo
 */
public class Elevator extends Machine implements InventoryHolder {

	private final Protections protections;
	private final ItemStack drop;

	public Elevator(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Elevator");
		this.protections = plugin.getModule(Protections.class);

		getShape().setVectorData(new Vector(0, 0, 0),Material.PURPUR_PILLAR);
		getShape().setVectorData(new Vector(0, 1, 0), Material.HEAVY_WEIGHTED_PRESSURE_PLATE);

		drop = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Elevator");
			drop.setItemMeta(itemMeta);
		});

		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "elevator"), drop);
		recipe.shape("BDB", "ACA", "BAB");
		recipe.setIngredient('A', Material.ENDER_EYE);
		recipe.setIngredient('B', Material.PHANTOM_MEMBRANE);
		recipe.setIngredient('C', new RecipeChoice.MaterialChoice(Material.PURPUR_BLOCK, Material.PURPUR_PILLAR));
		recipe.setIngredient('D', Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		plugin.getServer().addRecipe(recipe);
	}

	private int getCurrentBoost(ConfigurationSection storage) {
		return storage.getInt("duration", 1);
	}

	public int adjustBlockBoost(ConfigurationSection storage, int difference) {
		int boost = getCurrentBoost(storage) + difference;
		if (boost < 1) {
			return 1;
		}
		if (boost > 50) {
			boost = 50;
		}
		storage.set("duration", boost);
		return boost;
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			event.setResult(Event.Result.DENY);
			return true;
		}
		event.setResult(Event.Result.DENY);
		Program program = Programs.getProgramByIcon(event.getCurrentItem());
		if (program != null) {
			program.execute((Player) event.getWhoClicked(), event.getCurrentItem());
		}
		return true;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		Player player = event.getPlayer();
		// Allow sneaking players to cross or place blocks, but don't allow elevators to trigger redstone devices.
		if (player.isSneaking()) {
			return event.getAction() == Action.PHYSICAL;
		}
		if (event.getClickedBlock() == null) {
			return true;
		}
		if (event.getAction() == Action.PHYSICAL) {
			event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
					Sound.ENTITY_ENDER_DRAGON_FLAP, 0.2F, 0F);
			int duration = storage.getInt("duration");
			// Effect power is 0-indexed.
			player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 19, true), true);
			PermissionAttachment attachment = player.addAttachment(getPlugin(), (int) (duration * 1.5));
			if (attachment != null) {
				attachment.setPermission("nocheatplus.checks.moving.creativefly", true);
			}
			return true;
		}
		Location interacted = event.getClickedBlock().getLocation();
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canOpenChestsAt(player, interacted)) {
				player.sendMessage(Language.getColor("bad") + "You do not have permission to adjust elevators here!");
				return true;
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inventory = getInventory();
			inventory.setItem(3, ((GoodButton) Programs.getProgramByName("GoodButton"))
					.getIconFor(ChatColor.GREEN + "Increase Boost"));
			ItemStack gauge = new ItemStack(Material.ELYTRA);
			ItemMeta meta = gauge.getItemMeta();
			meta.setDisplayName(ChatColor.GOLD + "Ticks of Boost");
			meta.setLore(Arrays.asList(ChatColor.WHITE + "1 tick = 1/20 second",
					ChatColor.WHITE + "Roughly, +1 block/tick"));
			gauge.setItemMeta(meta);
			gauge.setAmount(getCurrentBoost(storage));
			inventory.setItem(4, gauge);
			inventory.setItem(5, ((BadButton) Programs.getProgramByName("BadButton"))
					.getIconFor(ChatColor.RED + "Decrease Boost"));
			event.getPlayer().openInventory(inventory);
		}
		return true;
	}

	@Override
	public int getCost() {
		return 200;
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return getPlugin().getServer().createInventory(this, 9, "Elevator Configuration");
	}

}
