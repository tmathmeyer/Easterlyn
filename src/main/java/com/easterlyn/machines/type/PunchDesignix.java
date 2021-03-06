package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.effects.Effects;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.tuple.Triple;
import java.util.Arrays;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
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

/**
 * Simulate a Sburb Punch Designix in Minecraft.
 *
 * @author Jikoo
 */
public class PunchDesignix extends Machine {

	/* The ItemStacks used to create usage help trade offers */
	private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

	private final ItemStack drop;
	private final Captcha captcha;
	private final Effects effects;

	public PunchDesignix(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Punch Designix");
		this.captcha = plugin.getModule(Captcha.class);
		this.effects = plugin.getModule(Effects.class);
		Shape shape = getShape();
		shape.setVectorData(new Vector(0, 0, 0), new Shape.MaterialDataValue(Material.QUARTZ_STAIRS)
				.withBlockData(Rotatable.class, Direction.WEST).withBlockData(Bisected.class, Direction.UP));
		shape.setVectorData(new Vector(1, 0, 0), new Shape.MaterialDataValue(Material.QUARTZ_STAIRS)
				.withBlockData(Rotatable.class, Direction.EAST).withBlockData(Bisected.class, Direction.UP));
		MaterialDataValue m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS)
				.withBlockData(Rotatable.class, Direction.NORTH).withBlockData(Bisected.class, Direction.UP);
		shape.setVectorData(new Vector(0, 1, 0), m);
		shape.setVectorData(new Vector(1, 1, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_SLAB).withBlockData(Bisected.class, Direction.UP);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		m = new Shape.MaterialDataValue(Material.LIGHT_GRAY_CARPET);
		shape.setVectorData(new Vector(0, 1, -1), m);
		shape.setVectorData(new Vector(1, 1, -1), m);

		drop = new ItemStack(Material.QUARTZ_STAIRS);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Punch Designix");
			drop.setItemMeta(itemMeta);
		});

		createExampleRecipes();
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

	@Override
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		if (event.getSlot() != 2 || event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			updateInventory(event.getWhoClicked().getUniqueId(), false);
			return false;
		}
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			updateInventory(event.getWhoClicked().getUniqueId(), false);
			return true;
		}
		// Clicking an item in result slot

		Inventory merchant = event.getInventory();

		// Possible results:
		// 1) slot 0 is captcha/punch, slot 1 is blank captcha. Result: slot 2 = punch 0. 1 consumed.
		// 2) slot 0 is punch, slot 1 is punch. Result: slot 2 = combine 0, 1. 0, 1 consumed.
		ItemStack result = getCombinedPunch(merchant.getItem(0), merchant.getItem(1), effects);

		if (result == null) {
			updateInventory(event.getWhoClicked().getUniqueId(), false);
			return true;
		}

		int crafts = 1;

		boolean copyPunch = Captcha.isBlankCaptcha(merchant.getItem(1));
		boolean updateInputSlot0 = false;

		// Clicking a villager result slot with vanilla client treats right clicks as left clicks.
		if (event.getClick().name().contains("SHIFT")) {
			// Shift-clicks are craft-max attempts.
			if (Captcha.isPunch(merchant.getItem(0)) && copyPunch) {
				ItemStack punched = merchant.getItem(1);
				crafts = punched != null ? punched.getAmount() : 0;
			} else {
				crafts = getMaximumCrafts(merchant.getItem(0), merchant.getItem(1));
			}
			result.setAmount(crafts);

			// Decrement number of crafts by number of items that failed to be added
			// This is only works because the result will always be a single item that stacks to 64
			crafts -= InventoryUtils.getAddFailures(event.getWhoClicked().getInventory().addItem(result));
		} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
				|| event.getCursor().isSimilar(result)
				&& event.getCursor().getAmount() < event.getCursor().getType().getMaxStackSize()) {
			// Set cursor to single stack
			result.setAmount(event.getCursor() == null || event.getCursor().getType() == Material.AIR ? 1 : event.getCursor().getAmount() + 1);
			event.setCursor(result);
			updateInputSlot0 = copyPunch;
		} else {
			// Invalid craft, cancel and update result
			updateInventory(event.getWhoClicked().getUniqueId(), false);
			return true;
		}

		// This will be recalculated in the synchronous delayed inventory update task.
		event.setCurrentItem(InventoryUtils.decrement(result, crafts));

		// If second item is a captcha, first item is a punchcard being copied. Do not decrement.
		if (!copyPunch) {
			merchant.setItem(0, InventoryUtils.decrement(merchant.getItem(0), crafts));
		}

		// In all cases (combine, punch single, copy punch) if second is not null it decrements.
		merchant.setItem(1, InventoryUtils.decrement(merchant.getItem(1), crafts));

		updateInventory(event.getWhoClicked().getUniqueId(), updateInputSlot0);
		return true;
	}

	@Override
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId(), false);
		return false;
	}

	/**
	 * Calculates the maximum number of items that can be crafted with the given ItemStacks.
	 *
	 * @param slot1 the first ItemStack
	 * @param slot2 the second ItemStack
	 *
	 * @return the least of the two, or, if slot2 is null, the amount in slot1
	 */
	private int getMaximumCrafts(ItemStack slot1, ItemStack slot2) {
		return slot2 == null || slot2.getType() == Material.AIR ? slot1.getAmount() : Math.min(slot1.getAmount(), slot2.getAmount());
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the player who is using the Punch Designix
	 * @param updateInputSlot0 whether or not input slot 0 should be forcibly updated
	 */
	private void updateInventory(final UUID id, final boolean updateInputSlot0) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || getMachines().getInventoryTracker().hasNoMachineOpen(player)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}
			if (updateInputSlot0) {
				InventoryUtils.updateWindowSlot(player, 0);
			}
			Inventory open = player.getOpenInventory().getTopInventory();
			// TODO this seems to fail to update properly when punch in slot 0 is re-punched
			ItemStack result = getCombinedPunch(open.getItem(0), open.getItem(1), effects);
			open.setItem(2, result);
			ItemStack inputSlot1 = open.getItem(0);
			if (inputSlot1 == null) {
				inputSlot1 = InventoryUtils.AIR;
			}
			inputSlot1 = inputSlot1.clone();
			inputSlot1.setAmount(1);
			ItemStack inputSlot2 = open.getItem(1);
			if (inputSlot2 == null) {
				inputSlot2 = InventoryUtils.AIR;
			}
			inputSlot2 = inputSlot2.clone();
			inputSlot2.setAmount(1);
			if (result == null) {
				result = InventoryUtils.AIR;
			}
			InventoryUtils.updateVillagerTrades(player, getExampleRecipes(),
					new Triple<>(inputSlot1, inputSlot2, result));
			InventoryUtils.updateWindowSlot(player, 2);
		});
	}

	/**
	 * Creates a punchcard from two cards.
	 *
	 * @param card1 the first card
	 * @param card2 the second card
	 *
	 * @return the ItemStack created or null if invalid cards are provided
	 */
	private ItemStack getCombinedPunch(ItemStack card1, ItemStack card2, Effects effects) {
		boolean punch1 = Captcha.isPunch(card1);
		boolean captcha1 = Captcha.isCaptcha(card1);

		if (!captcha1 && !punch1) {
			return null;
		}

		if (captcha1 || !Captcha.isPunch(card2)) {
			if (Captcha.isBlankCaptcha(card1)) {
				return null;
			}
			if (!Captcha.isBlankCaptcha(card2)) {
				return null;
			}
			ItemStack is = captcha.getPunchForCaptcha(card1, effects);
			if (!punch1 && card1.isSimilar(is)) {
				// Prevent creation of punches will yield useless totems.
				return null;
			}
			return is;
		}

		ItemStack item1 = captcha.getItemForCaptcha(card1);
		ItemStack item2 = captcha.getItemForCaptcha(card2);
		InventoryUtils.biConsumeAs(ItemMeta.class, item1.getItemMeta(), item2.getItemMeta(), (itemMeta1, itemMeta2) -> {
			itemMeta1.setLore(effects.organizeEffectLore(itemMeta1.getLore(), false,
					false, true, itemMeta2.getLore().toArray(new String[0])));
			item1.setItemMeta(itemMeta1);
		});
		ItemStack result = captcha.getPunchForCaptcha(captcha.getCaptchaForItem(item1), effects);
		if (result.getItemMeta() != null && result.getItemMeta().getDisplayName().equals("Captchacard")) {
			return null;
		}
		return result;
	}

	/**
	 * Open a PunchDesignix inventory for a Player.
	 *
	 * @param player the Player
	 */
	private void openInventory(Player player, ConfigurationSection storage) {
		getMachines().getInventoryTracker().openVillagerInventory(player, this, getKey(storage));
		InventoryUtils.updateVillagerTrades(player, getExampleRecipes());
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
		ItemStack input1 = new ItemStack(Material.BOOK);
		InventoryUtils.consumeAs(ItemMeta.class, input1.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Slot 1 options:");
			itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "1) Captchacard or Punchcard",
					ChatColor.GOLD + "2) Punchcard " + ChatColor.DARK_RED + "(consumed)"));
			input1.setItemMeta(itemMeta);
		});

		ItemStack input2 = new ItemStack(Material.BOOK);
		InventoryUtils.consumeAs(ItemMeta.class, input2.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Slot 2 options:");
			itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "1) Blank Captchacard " + ChatColor.DARK_RED + "(consumed)",
					ChatColor.GOLD + "2) Punchcard " + ChatColor.DARK_RED + "(consumed)"));
			input2.setItemMeta(itemMeta);
		});

		ItemStack result = new ItemStack(Material.BOOK);
		InventoryUtils.consumeAs(ItemMeta.class, result.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Punchcard Result:");
			itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "1) Punchcard for Card 1",
					ChatColor.GOLD + "2) Modified Punchcard:",
					ChatColor.YELLOW + "    Card for item from card 1",
					ChatColor.YELLOW + "    with lore from card 2 added"));
			result.setItemMeta(itemMeta);
		});

		return new Triple<>(input1, input2, result);
	}

}
