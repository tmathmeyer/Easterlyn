package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.machines.MachineInventoryTracker;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.tuple.Triple;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Simulates a Totem Lathe from Sburb.
 *
 * @author Dublek, Jikoo
 */
public class TotemLathe extends Machine {

	private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

	private final Captcha captcha;
	private final MachineInventoryTracker tracker;
	private final ItemStack drop;

	public TotemLathe(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Totem Lathe");
		this.captcha = plugin.getModule(Captcha.class);
		tracker = machines.getInventoryTracker();
		Shape shape = getShape();
		MaterialDataValue m = new Shape.MaterialDataValue(Material.QUARTZ_PILLAR);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 1, 0), m);
		m = new Shape.MaterialDataValue(Material.CHISELED_QUARTZ_BLOCK);
		shape.setVectorData(new Vector(0, 2, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.WEST).withBlockData(Bisected.class, Direction.UP);
		shape.setVectorData(new Vector(1, 0, 0), m);
		shape.setVectorData(new Vector(1, 2, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_SLAB);
		shape.setVectorData(new Vector(0, 3, 0), m);
		shape.setVectorData(new Vector(1, 3, 0), m);
		shape.setVectorData(new Vector(2, 3, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_SLAB).withBlockData(Bisected.class, Direction.UP);
		shape.setVectorData(new Vector(2, 0, 0), m);
		shape.setVectorData(new Vector(3, 0, 0), m);
		m = new Shape.MaterialDataValue(Material.DAYLIGHT_DETECTOR);
		shape.setVectorData(new Vector(1, 1, 0), m);
		m = new Shape.MaterialDataValue(Material.ANVIL).withBlockData(Rotatable.class, Direction.WEST);
		shape.setVectorData(new Vector(3, 1, 0), m);
		m = new Shape.MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(2, 2, 0), m);

		drop = new ItemStack(Material.ANVIL);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Totem Lathe");
			drop.setItemMeta(itemMeta);
		});
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (event.getPlayer().isSneaking()) {
			return false;
		}
		openInventory(event.getPlayer(), storage);
		return true;
	}

	/**
	 * Open a Totem Lathe inventory for a Player.
	 *
	 * @param player the Player
	 */
	private void openInventory(Player player, ConfigurationSection storage) {
		tracker.openVillagerInventory(player, this, getKey(storage));
		InventoryUtils.updateVillagerTrades(player, getExampleRecipes());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			// Clicked inv is not the top.
			return false;
		}
		if (event.getSlot() != 2 || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR) {
			// No result
			return false;
		}
		// Item is being crafted
		Inventory top = event.getView().getTopInventory();
		Player player = (Player) event.getWhoClicked();
		int decrement;
		if (event.getClick().name().contains("SHIFT")) {
			// This is not a good way to handle shift clicks for normal crafting, but in
			// this case the result is guaranteed to be a single item that stacks to 64.
			decrement = Math.min(top.getItem(0).getAmount(), top.getItem(1).getAmount());
			ItemStack add = event.getCurrentItem().clone();
			add.setAmount(decrement);
			if (InventoryUtils.hasSpaceFor(add, player.getInventory())) {
				player.getInventory().addItem(add);
			} else {
				return true;
			}
		} else if (event.getCursor() == null
				|| event.getCursor().getType() == Material.AIR
				|| (event.getCursor().isSimilar(event.getCurrentItem())
				&& event.getCursor().getAmount() + event.getCurrentItem().getAmount()
				<= event.getCursor().getMaxStackSize())) {
			decrement = 1;
			ItemStack result = event.getCurrentItem().clone();
			if (result.isSimilar(event.getCursor())) {
				result.setAmount(result.getAmount() + event.getCursor().getAmount());
			}
			event.setCursor(result);
		} else {
			return true;
		}
		event.setCurrentItem(null);
		top.setItem(0, InventoryUtils.decrement(top.getItem(0), decrement));
		top.setItem(1, InventoryUtils.decrement(top.getItem(1), decrement));
		player.updateInventory();
		return true;
	}

	@Override
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		return false;
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the player who is using the Totem Lathe
	 */
	private void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || tracker.hasNoMachineOpen(player)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}

			Inventory open = player.getOpenInventory().getTopInventory();
			ItemStack card = null;
			ItemStack result = InventoryUtils.AIR;
			ItemStack slot0 = open.getItem(0);
			ItemStack slot1 = open.getItem(1);
			if (Captcha.isDowel(slot0)) {
				card = slot1;
			} else if (Captcha.isDowel(slot1)) {
				card = slot0;
			}
			if (Captcha.isPunch(card)) {
				result = captcha.getTotemForPunch(card);
			}
			if (slot0 != null) {
				slot0 = slot0.clone();
				slot0.setAmount(1);
			} else {
				slot0 = InventoryUtils.AIR;
			}
			if (slot1 != null) {
				slot1 = slot1.clone();
				slot1.setAmount(1);
			} else {
				slot1 = InventoryUtils.AIR;
			}
			// Set items
			open.setItem(2, result);
			InventoryUtils.updateVillagerTrades(player, getExampleRecipes(),
					new Triple<>(slot0, slot1, result));
			InventoryUtils.updateWindowSlot(player, 2);
		});
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	/**
	 * Singleton for getting usage help ItemStacks.
	 */
	private static Triple<ItemStack, ItemStack, ItemStack> getExampleRecipes() {
		if (exampleRecipes == null) {
			exampleRecipes = createExampleRecipes();
		}
		return exampleRecipes;
	}

	/**
	 * Creates the ItemStacks used in displaying usage help.
	 *
	 * @return the example recipe
	 */
	private static Triple<ItemStack, ItemStack, ItemStack> createExampleRecipes() {
		ItemStack input1 = new ItemStack(Material.NETHER_BRICK);
		InventoryUtils.consumeAs(ItemMeta.class, input1.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Cruxite Totem");
			input1.setItemMeta(itemMeta);
		});

		ItemStack input2 = new ItemStack(Material.BOOK);
		InventoryUtils.consumeAs(ItemMeta.class, input2.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Punchcard");
			input2.setItemMeta(itemMeta);
		});

		ItemStack result = new ItemStack(Material.NETHER_BRICK);
		InventoryUtils.consumeAs(ItemMeta.class, result.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Carved Totem");
			result.setItemMeta(itemMeta);
		});

		return new Triple<>(input1, input2, result);
	}

}
