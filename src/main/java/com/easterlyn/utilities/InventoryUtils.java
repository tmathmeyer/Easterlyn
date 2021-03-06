package com.easterlyn.utilities;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.utilities.tuple.Triple;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.Container;
import net.minecraft.server.v1_15_R1.Containers;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MerchantRecipe;
import net.minecraft.server.v1_15_R1.MerchantRecipeList;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutWindowData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

/**
 * A set of useful methods for inventory functions.
 *
 * @author Jikoo
 */
public class InventoryUtils {

	public static final String ITEM_UNIQUE = ChatColor.DARK_PURPLE + "Unique";
	public static final ItemStack AIR = new ItemStack(Material.AIR);

	private static BiMap<String, String> items;
	private static HashSet<ItemStack> uniques;

	private static BiMap<String, String> getItems() {
		if (items != null) {
			return items;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Easterlyn")).getResource("items.csv"))))) {
			items = HashBiMap.create();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] row = line.split(",");
				items.put(row[0], row[1]);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load items from items.csv!", e);
		}
		return items;
	}

	public static String getItemName(ItemStack item) {
		Material material = item.getType();
		String name = getItems().get(material.name());
		if (name == null) {
			// Even special-cased materials should have an entry.
			name = TextUtils.getFriendlyName(material);
		}
		if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION
				|| material == Material.TIPPED_ARROW) {
			if (!item.hasItemMeta()) {
				return name;
			}
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof PotionMeta) {
				return TextUtils.getFriendlyName(material) + " of " + getPotionName((PotionMeta) meta);
			}
			return name;
		}
		return name;
	}

	private static String getPotionName(PotionMeta meta) {
		PotionData base;
		try {
			base = meta.getBasePotionData();
		} catch (IllegalArgumentException e) {
			// This can be thrown by Spigot when converting a valid potion with odd data values.
			return "Questionable Validity";
		}
		if (base.getType() != PotionType.UNCRAFTABLE) {
			StringBuilder name = new StringBuilder();
			if (base.isExtended()) {
				name.append("Extended ");
			}
			name.append(TextUtils.getFriendlyName(base.getType()));
			if (base.isUpgraded()) {
				name.append(" II");
			}
			return name.toString();
		}
		if (!meta.hasCustomEffects()) {
			return "No Effect";
		}
		if (meta.getCustomEffects().size() > 1) {
			return "Multiple Effects";
		}
		PotionEffect effect = meta.getCustomEffects().get(0);
		PotionEffectType type = effect.getType();
		boolean extended = !type.isInstant() && effect.getDuration() > 3600 * type.getDurationModifier();
		StringBuilder name = new StringBuilder();
		if (extended) {
			name.append("Extended ");
		}
		name.append(TextUtils.getFriendlyName(type.getName()));
		if (effect.getAmplifier() > 0) {
			// Effect power is 0-indexed
			name.append(' ').append(NumberUtils.romanFromInt(effect.getAmplifier() + 1));
		}
		return name.toString();
	}

	public static boolean isMisleadinglyNamed(String name, Material material) {
		String materialName = getItems().inverse().get(name);
		return materialName != null && !materialName.equals(material.name());
	}

	public static Material matchMaterial(String search) {
		String searchMaterialName = search.toUpperCase().replace(' ', '_');

		try {
			return Material.valueOf(searchMaterialName);
		} catch (IllegalArgumentException ignored) {}

		String searchFriendlyName = search.replace('_', ' ');

		// TODO ignoreCase
		String materialName = getItems().inverse().get(searchFriendlyName);
		if (materialName != null) {
			return Material.valueOf(materialName);
		}

		Material material = null;

		float matchLevel = 0F;
		search = searchFriendlyName.toLowerCase(Locale.ENGLISH);
		for (Entry<String, String> entry : getItems().entrySet()) {
			float current = StringMetric.compare(search, entry.getValue().toLowerCase(Locale.ENGLISH));
			if (current > matchLevel) {
				matchLevel = current;
				material = Material.getMaterial(entry.getKey());
			}
			if (current == 1F) {
				return material;
			}
		}

		// Allow more fuzziness for longer named items
		if (matchLevel > (.7F - (1F / material.name().length()))) {
			return material;
		}
		return null;
	}

	public static ItemStack cleanNBT(ItemStack originalItem) {
		if (originalItem == null || !originalItem.hasItemMeta()) {
			return originalItem;
		}

		ItemMeta originalMeta = originalItem.getItemMeta();
		if (originalMeta == null) {
			// Unnecessary, but it keeps the compiler happy.
			return originalItem;
		}

		ItemStack cleanedItem = new ItemStack(originalItem.getType());
		// Why Bukkit doesn't have a constructor ItemStack(MaterialData) I don't know.
		cleanedItem.setData(originalItem.getData());
		cleanedItem.setAmount(originalItem.getAmount());

		ItemMeta cleanedMeta = cleanedItem.getItemMeta();
		if (cleanedMeta == null) {
			return cleanedItem;
		}

		// Banners
		biConsumeAs(BannerMeta.class, originalMeta, cleanedMeta,
				(oldMeta, newMeta) -> newMeta.setPatterns(oldMeta.getPatterns()));

		// Book and quill/Written books
		biConsumeAs(BookMeta.class, originalMeta, cleanedMeta, (oldMeta, newMeta) -> {
			if (oldMeta.hasPages()) {
				newMeta.setPages(oldMeta.getPages());
			}
			if (oldMeta.hasAuthor()) {
				newMeta.setAuthor(oldMeta.getAuthor());
			}
			if (oldMeta.hasTitle()) {
				newMeta.setTitle(oldMeta.getTitle());
			}
		});

		// Durability
		biConsumeAs(Damageable.class, originalMeta, cleanedMeta, (oldMeta, newMeta) ->
				newMeta.setDamage(Math.max(Math.min(oldMeta.getDamage(), originalItem.getType().getMaxDurability()), 0)));

		// Single effect fireworks
		biConsumeAs(FireworkEffectMeta.class, originalMeta, cleanedMeta,
				(oldMeta, newMeta) -> newMeta.setEffect(oldMeta.getEffect()));

		// Fireworks/Firework stars
		biConsumeAs(FireworkMeta.class, originalMeta, cleanedMeta, (oldMeta, newMeta) -> {
			newMeta.setPower(oldMeta.getPower());
			newMeta.addEffects(oldMeta.getEffects());
		});

		// Leather armor color
		biConsumeAs(LeatherArmorMeta.class, originalMeta, cleanedMeta,
				(oldMeta, newMeta) -> newMeta.setColor(oldMeta.getColor()));

		// Enchanted books
		biConsumeAs(EnchantmentStorageMeta.class, originalMeta, cleanedMeta,
				(oldMeta, newMeta) -> oldMeta.getStoredEnchants().forEach(
						(enchantment, level) -> newMeta.addStoredEnchant(enchantment, level, true)));

		// Map ID
		biConsumeAs(MapMeta.class, originalMeta, cleanedMeta, (oldMeta, newMeta) -> {
				newMeta.setMapView(oldMeta.getMapView());
				newMeta.setColor(oldMeta.getColor());
				newMeta.setLocationName(oldMeta.getLocationName());
				newMeta.setScaling(oldMeta.isScaling());
		});

		// Potions
		biConsumeAs(PotionMeta.class, originalMeta, cleanedMeta, (oldMeta, newMeta) -> {
				newMeta.setBasePotionData(oldMeta.getBasePotionData());
				newMeta.setColor(oldMeta.getColor());
				oldMeta.getCustomEffects().forEach(effect -> {
					// Custom effects are fine, but amplifiers that are way too high are not
					if (effect.getAmplifier() < 5 && effect.getAmplifier() >= 0) {
						newMeta.addCustomEffect(effect, true);
					}
				});
		});

		// Repairable would preserve anvil tags on tools, we'll avoid that

		// Skulls
		biConsumeAs(SkullMeta.class, originalMeta, cleanedMeta,
				(oldMeta, newMeta) -> newMeta.setOwningPlayer(oldMeta.getOwningPlayer()));

		// Normal meta
		if (originalMeta.hasDisplayName()) {
			cleanedMeta.setDisplayName(originalMeta.getDisplayName());
		}

		if (originalMeta.hasEnchants()) {
			for (Entry<Enchantment, Integer> entry : originalMeta.getEnchants().entrySet()) {
				cleanedMeta.addEnchant(entry.getKey(), entry.getValue(), true);
			}
		}

		if (originalMeta.hasLore()) {
			cleanedMeta.setLore(originalMeta.getLore());
		}

		cleanedItem.setItemMeta(cleanedMeta);
		return cleanedItem;
	}

	public static <T> void consumeAs(Class<T> metaClazz, Object obj, Consumer<T> consumer) {
		if (!metaClazz.isInstance(obj)) {
			return;
		}
		consumer.accept(metaClazz.cast(obj));
	}

	public static <T> void biConsumeAs(Class<T> clazz, Object obj1, Object obj2, BiConsumer<T, T> consumer) {
		consumeAs(clazz, obj1, cast1 -> consumeAs(clazz, obj2, cast2 -> consumer.accept(cast1, cast2)));
	}

	public static HashSet<ItemStack> getUniqueItems(Easterlyn plugin) {
		if (uniques == null) {
			uniques = new HashSet<>();
			for (Machine machine : plugin.getModule(Machines.class).getMachinesByName().values()) {
				uniques.add(machine.getUniqueDrop());
			}
		}
		return uniques;
	}

	public static boolean isUniqueItem(Easterlyn plugin, ItemStack toCheck) {
		if (toCheck == null) {
			return false;
		}

		if (Captcha.isCaptcha(toCheck)) {
			return true;
		}

		if (toCheck.hasItemMeta()) {
			ItemMeta meta = toCheck.getItemMeta();
			if (meta.hasLore() && meta.getLore().contains(ITEM_UNIQUE)) {
				return true;
			}
		}

		for (ItemStack is : getUniqueItems(plugin)) {
			if (is.isSimilar(toCheck)) {
				return true;
			}
		}

		return false;
	}

	public static int getAddFailures(Map<Integer, ItemStack> failures) {
		int count = 0;
		for (ItemStack is : failures.values()) {
			count += is.getAmount();
		}
		return count;
	}

	/**
	 * Reduces an ItemStack by the given quantity. If the ItemStack would have a
	 * quantity of 0, returns null.
	 *
	 * @param is the ItemStack to reduce
	 * @param amount the amount to reduce the ItemStack by
	 *
	 * @return the reduced ItemStack
	 */
	public static ItemStack decrement(ItemStack is, int amount) {
		if (is == null || is.getType() == Material.AIR) {
			return null;
		}
		if (is.getAmount() > amount) {
			is.setAmount(is.getAmount() - amount);
		} else {
			is = null;
		}
		return is;
	}

	public static void decrementHeldItem(PlayerInteractEvent event, int amount) {
		boolean main = isMainHand(event);
		PlayerInventory inv = event.getPlayer().getInventory();
		setHeldItem(inv, main, decrement(getHeldItem(inv, main), amount));
	}

	public static boolean isMainHand(PlayerInteractEvent event) {
		return event.getHand() == EquipmentSlot.HAND;
	}

	public static ItemStack getHeldItem(PlayerInteractEvent event) {
		return getHeldItem(event.getPlayer().getInventory(), isMainHand(event));
	}

	private static ItemStack getHeldItem(PlayerInventory inv, boolean mainHand) {
		return mainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();
	}

	public static void setHeldItem(PlayerInventory inv, boolean mainHand, ItemStack item) {
		if (mainHand) {
			inv.setItemInMainHand(item);
		} else {
			inv.setItemInOffHand(item);
		}
	}

	/**
	 * Checks if there is space in the given Inventory to add the given ItemStack.
	 *
	 * @param is the ItemStack
	 * @param inv the Inventory to check
	 *
	 * @return true if the ItemStack can be fully added
	 */
	public static boolean hasSpaceFor(ItemStack is, Inventory inv) {
		if (is == null || is.getType() == Material.AIR) {
			return true;
		}
		ItemStack toAdd = is.clone();
		for (ItemStack invStack : inv.getContents()) {
			if (invStack == null) {
				return true;
			}
			if (!invStack.isSimilar(toAdd)) {
				continue;
			}
			toAdd.setAmount(toAdd.getAmount() - toAdd.getMaxStackSize() + invStack.getAmount());
			if (toAdd.getAmount() <= 0) {
				return true;
			}
		}
		return false;
	}

	public static void updateWindowSlot(Player player, int slot) {
		if (!(player instanceof CraftPlayer)) {
			return;
		}
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		nmsPlayer.playerConnection.sendPacket(
				new PacketPlayOutSetSlot(nmsPlayer.activeContainer.windowId, slot,
						nmsPlayer.activeContainer.getSlot(slot).getItem()));
	}

	public static void changeWindowName(Player player, String name) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer entityPlayer = craftPlayer.getHandle();

		if (entityPlayer.playerConnection == null) {
			return;
		}

		entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId,
				smartContainerMatch(entityPlayer.activeContainer), new ChatMessage(name)));
		entityPlayer.updateInventory(entityPlayer.activeContainer);

	}

	private static Containers smartContainerMatch(Container container) {
		if (container.getType() != Containers.GENERIC_9X3) {
			return container.getType();
		}
		switch (container.items.size()) {
			case 9:
				return Containers.GENERIC_9X1;
			case 18:
				return Containers.GENERIC_9X2;
			case 36:
				return Containers.GENERIC_9X4;
			case 45:
				return Containers.GENERIC_9X5;
			case 54:
				return Containers.GENERIC_9X6;
			case 27:
			default:
				return Containers.GENERIC_9X3;
		}
	}

	public static String getNameFromAnvil(InventoryView view) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return null;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("l");
			field.setAccessible(true);
			return (String) field.get(nmsInventory);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setAnvilExpCost(InventoryView view, int cost) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("a");
			field.set(nmsInventory, cost);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateAnvilExpCost(InventoryView view) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		EntityPlayer entityPlayer = ((CraftPlayer) view.getPlayer()).getHandle();
		if (entityPlayer.playerConnection == null) {
			return;
		}
		entityPlayer.playerConnection.sendPacket(new PacketPlayOutWindowData(entityPlayer.activeContainer.windowId, 0,
				((AnvilInventory) view.getTopInventory()).getRepairCost()));
	}

	@SafeVarargs
	public static void updateVillagerTrades(Player player, Triple<ItemStack, ItemStack, ItemStack>... recipes) {
		if (recipes == null || recipes.length == 0) {
			// Setting result in a villager inventory with recipes doesn't play nice clientside.
			// To make life easier, if there are no recipes, don't send the trade recipe packet.
			return;
		}

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		if (nmsPlayer.activeContainer.getBukkitView().getType() != InventoryType.MERCHANT) {
			return;
		}

		MerchantRecipeList list = new MerchantRecipeList();
		for (Triple<ItemStack, ItemStack, ItemStack> recipe : recipes) {
			// The client can handle having empty results for recipes, but will crash upon removing the result.
			if (recipe.getRight().getType() == Material.AIR) {
				continue;
			}
			list.add(new MerchantRecipe(CraftItemStack.asNMSCopy(recipe.getLeft()), CraftItemStack.asNMSCopy(recipe.getMiddle()),
					CraftItemStack.asNMSCopy(recipe.getRight()), 0, Integer.MAX_VALUE, 0, 0));
		}

		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindowMerchant(nmsPlayer.activeContainer.windowId, list, 5, 0, false, false));
		player.updateInventory();
	}

	public static String recipeToText(Recipe recipe) {
		if (recipe instanceof FurnaceRecipe) {
			return String.format("SMELT: %s -> %s", itemToText(((FurnaceRecipe) recipe).getInput()), itemToText(recipe.getResult()));
		} else if (recipe instanceof ShapelessRecipe) {
			StringBuilder builder = new StringBuilder("SHAPELESS: ");
			for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
				builder.append(itemToText(ingredient)).append(" + ");
			}
			builder.replace(builder.length() - 2, builder.length(), "-> ").append(recipe.getResult());
			return builder.toString();
		} else if (recipe instanceof ShapedRecipe) {
			StringBuilder builder = new StringBuilder("SHAPED:\n");
			Map<Character, String> mappings = new HashMap<>();
			int longestMapping = 3; // "AIR".length() == 3
			for (Map.Entry<Character, ItemStack> mapping : ((ShapedRecipe) recipe).getIngredientMap().entrySet()) {
				String newMapping = itemToText(mapping.getValue());
				longestMapping = Math.max(longestMapping, newMapping.length());
				mappings.put(mapping.getKey(), newMapping);
			}
			for (String line : ((ShapedRecipe) recipe).getShape()) {
				for (char character : line.toCharArray()) {
					builder.append('[');
					String mapping = mappings.getOrDefault(character, "AIR");
					double padding = (longestMapping - mapping.length()) / 2.0;
					double roundPadding = Math.floor(padding);
					for (int i = 0; i < roundPadding; i++) {
						builder.append(' ');
					}
					builder.append(mapping);
					// Post-pad additional space for odd numbers
					padding = Math.floor(padding + 0.5);
					for (int i = 0; i < padding; i++) {
						builder.append(' ');
					}
					builder.append("] ");
				}
				builder.delete(builder.length() - 1, builder.length());
				builder.append('\n');
			}
			builder.delete(builder.length() - 1, builder.length()).append(" -> ").append(itemToText(recipe.getResult()));
			return builder.toString();
		}
		return recipe.toString();
	}

	private static String itemToText(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return "AIR";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(item.getType().name());
		if (item.getAmount() != 1) {
			builder.append('x').append(item.getAmount());
		}
		return builder.toString();
	}

}
