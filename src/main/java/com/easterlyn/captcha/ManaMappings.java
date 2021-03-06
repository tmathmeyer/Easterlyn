package com.easterlyn.captcha;

import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.recipe.RecipeWrapper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_15_R1.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Utility for calculating mana costs.
 *
 * @author Jikoo
 */
public class ManaMappings {

	private static Map<Material, Double> manaMappings;

	@SuppressWarnings("DuplicateBranchesInSwitch") // IDE does not recognize lack of break statement yielding differing results
	public static double expCost(Effects effects, ItemStack toCreate) {
		if (toCreate == null || toCreate.getAmount() < 1) {
			return Double.MAX_VALUE;
		}

		double cost = getMana().getOrDefault(toCreate.getType(), Double.MAX_VALUE);
		if (cost == Double.MAX_VALUE) {
			// Item cannot be made with mana
			return Double.MAX_VALUE;
		}

		ItemMeta meta = toCreate.hasItemMeta() ? toCreate.getItemMeta() : null;

		if (meta == null) {
			// No additional costs from meta, finish fast.
			if (Double.MAX_VALUE / toCreate.getAmount() <= cost) {
				return Double.MAX_VALUE;
			}
			return cost * toCreate.getAmount();
		}

		if (InventoryUtils.isUniqueItem(effects.getPlugin(), toCreate)) {
			return Double.MAX_VALUE;
		}

		if (toCreate.getEnchantments().size() > 0 || meta.hasDisplayName() || meta.hasLore() || meta.isUnbreakable()) {
			switch (toCreate.getType()) {
				case DRAGON_BREATH:
				case FIREWORK_STAR:
				case PAPER:
					// Special case: items used for unique cards, slips, or objects.
					return Double.MAX_VALUE;
				default:
					break;
			}
		}

		// In case of shulker boxes, etc. do not (yet) allow duplicating unless empty.
		if (meta instanceof BlockStateMeta) {
			BlockState state = ((BlockStateMeta) meta).getBlockState();
			if (state instanceof InventoryHolder) {
				for (ItemStack item : ((InventoryHolder) state).getInventory().getContents()) {
					if (item != null && item.getType() != Material.AIR) {
						return Double.MAX_VALUE;
					}
				}
			}
		}

		if (meta instanceof MapMeta) {
			MapMeta mapMeta = (MapMeta) meta;
			if (mapMeta.hasLocalizedName()) {
				// Map is an exploration map.
				switch (mapMeta.getLocalizedName()) {
					case "filled_map.monument":
						// Monument map.
						cost += 1200;
						break;
					case "filled_map.mansion":
						// Mansions are rarer than monuments, roughly 4/3 worth in vanilla.
						cost += 1600;
						break;
					default:
						// Just in case.
						cost += 2000;
						break;
				}
			}
		}

		if (meta instanceof FireworkMeta) {
			FireworkMeta fireworkMeta = (FireworkMeta) meta;
			cost += Math.abs(fireworkMeta.getPower()) * getMana().get(Material.GUNPOWDER);
			for (FireworkEffect effect : fireworkMeta.getEffects()) {
				switch (effect.getType()) {
					case BALL_LARGE:
						cost += getMana().get(Material.FIRE_CHARGE);
						break;
					case STAR:
						cost += getMana().get(Material.GOLD_NUGGET);
						break;
					case BURST:
						cost += getMana().get(Material.FEATHER);
						break;
					case CREEPER:
						cost += getMana().get(Material.WITHER_SKELETON_SKULL);
						break;
					case BALL:
						// Default effect, no cost
					default:
						break;
				}
				if (effect.hasFlicker()) {
					cost += getMana().get(Material.GLOWSTONE_DUST);
				}
				if (effect.hasTrail()) {
					cost += getMana().get(Material.DIAMOND);
				}
				// Flat cost of 1 mana per color
				cost += effect.getColors().size();
				cost += effect.getFadeColors().size();
			}

			if (toCreate.getType() == Material.FIREWORK_ROCKET) {
				// Firework stars each require 1 gunpowder in addition to other components
				cost += manaMappings.get(Material.GUNPOWDER) * fireworkMeta.getEffects().size();
				// 3 fireworks per craft
				cost /= 3;
			}
		}

		if (meta instanceof PotionMeta) {
			PotionMeta potionMeta = (PotionMeta) meta;

			if (potionMeta.hasCustomEffects()) {
				// Custom potions are unsupported.
				return Double.MAX_VALUE;
			}

			PotionData potionData = potionMeta.getBasePotionData();

			switch (potionData.getType()) {
				case WATER:
					break;
				case MUNDANE:
					// Sugar is the cheapest ingredient that creates mundane
					cost += getMana().get(Material.SUGAR);
					break;
				case THICK:
					cost += getMana().get(Material.GLOWSTONE_DUST);
					break;
				case AWKWARD:
					cost += getMana().get(Material.NETHER_WART);
					break;
				case INVISIBILITY:
					// Corrupted night vision
					cost += getMana().get(Material.FERMENTED_SPIDER_EYE);
				case NIGHT_VISION:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.GOLDEN_CARROT);
					break;
				case JUMP:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.RABBIT_FOOT);
					break;
				case FIRE_RESISTANCE:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.MAGMA_CREAM);
					break;
				case SLOWNESS:
					// Corrupted speed/leaping, speed is cheaper
					cost += getMana().get(Material.FERMENTED_SPIDER_EYE);
				case SPEED:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.SUGAR);
					break;
				case WATER_BREATHING:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.PUFFERFISH);
					break;
				case INSTANT_HEAL:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.GLISTERING_MELON_SLICE);
					break;
				case INSTANT_DAMAGE:
					// Corrupted poison/instant health, poison is cheaper
					cost += getMana().get(Material.FERMENTED_SPIDER_EYE);
				case POISON:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.SPIDER_EYE);
					break;
				case REGEN:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.GHAST_TEAR);
					break;
				case STRENGTH:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.BLAZE_POWDER);
					break;
				case WEAKNESS:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.FERMENTED_SPIDER_EYE);
					break;
				case TURTLE_MASTER:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.TURTLE_HELMET);
					break;
				case SLOW_FALLING:
					cost += getMana().get(Material.NETHER_WART) + getMana().get(Material.PHANTOM_MEMBRANE);
					break;
				case LUCK:
				case UNCRAFTABLE:
					return Double.MAX_VALUE;
			}

			if (potionData.isExtended()) {
				cost += getMana().get(Material.REDSTONE);
			}

			if (potionData.isUpgraded()) {
				cost += getMana().get(Material.GLOWSTONE_DUST);
			}
		}

		if (meta instanceof SuspiciousStewMeta) {
			for (PotionEffect potionEffect : ((SuspiciousStewMeta) meta).getCustomEffects()) {
				if (potionEffect.getType().equals(PotionEffectType.FIRE_RESISTANCE)) {
					cost += getMana().get(Material.ALLIUM);
				} else if (potionEffect.getType().equals(PotionEffectType.BLINDNESS)) {
					cost += getMana().get(Material.AZURE_BLUET);
				} else if (potionEffect.getType().equals(PotionEffectType.SATURATION)) {
					cost += getMana().get(Material.BLUE_ORCHID);
				} else if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
					cost += getMana().get(Material.CORNFLOWER);
				} else if (potionEffect.getType().equals(PotionEffectType.POISON)) {
					cost += getMana().get(Material.LILY_OF_THE_VALLEY);
				} else if (potionEffect.getType().equals(PotionEffectType.REGENERATION)) {
					cost += getMana().get(Material.OXEYE_DAISY);
				} else if (potionEffect.getType().equals(PotionEffectType.NIGHT_VISION)) {
					cost += getMana().get(Material.POPPY);
				} else if (potionEffect.getType().equals(PotionEffectType.WEAKNESS)) {
					cost += getMana().get(Material.WHITE_TULIP);
				} else if (potionEffect.getType().equals(PotionEffectType.FIRE_RESISTANCE)) {
					cost += getMana().get(Material.WITHER_ROSE);
				}
			}
		}

		if (meta.hasEnchants()) {
			for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
				double enchantCost = getEnchantCost(entry.getKey(), entry.getValue(), false);
				if (Double.MAX_VALUE - enchantCost <= cost) {
					return Double.MAX_VALUE;
				}
				cost += enchantCost;
			}
			if (toCreate.getType().getMaxDurability() == 0) {
				if (Double.MAX_VALUE / 4 <= cost) {
					return Double.MAX_VALUE;
				}
				cost *= 4;
			}
		}

		if (meta instanceof EnchantmentStorageMeta) {
			for (Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
				double enchantCost = getEnchantCost(entry.getKey(), entry.getValue(), true);
				if (Double.MAX_VALUE - enchantCost <= cost) {
					return Double.MAX_VALUE;
				}
				cost += enchantCost;
			}
		}

		if (toCreate.getItemMeta().hasDisplayName()) {
			// Naming an unenchanted item in an anvil costs 1 additional level in 1.8. Since we're nice, a fixed cost.
			if (Double.MAX_VALUE - 15 <= cost) {
				return Double.MAX_VALUE;
			}
			cost += 15;
		}

		int effectCost = 0;
		for (Entry<Effect, Integer> effect : effects.getEffects(true, toCreate).entrySet()) {
			int entryCost = effect.getKey().getCost();
			if (entryCost == Integer.MAX_VALUE || Integer.MAX_VALUE / effect.getValue() <= entryCost) {
				return Double.MAX_VALUE;
			}
			entryCost *= effect.getValue();
			if (Integer.MAX_VALUE - entryCost <= effectCost) {
				return Double.MAX_VALUE;
			}
			effectCost += entryCost;
		}
		// if item contains special lore and doesn't need repair, raise price
		if (toCreate.getType().getMaxDurability() == 0) {
			if (Integer.MAX_VALUE / 4 <= effectCost) {
				return Double.MAX_VALUE;
			}
			effectCost *= 4;
		}
		if (effectCost < 0 || Double.MAX_VALUE - effectCost <= cost) {
			return Double.MAX_VALUE;
		}
		cost += effectCost;

		if (Double.MAX_VALUE / toCreate.getAmount() <= effectCost) {
			return Double.MAX_VALUE;
		}
		cost *= toCreate.getAmount();

		return cost > 0 ? cost : Integer.MAX_VALUE;
	}

	/**
	 * Gets a value for an enchantment.
	 * <p>
	 *     This is based on internal rarity values and may need updating between versions.
	 * </p>
	 * @param enchantment the Enchantment
	 * @param level the level of the Enchantment
	 * @param stored whether or not the Enchantment is in a book and not useable
	 * @return the cost of the enchantment
	 */
	private static double getEnchantCost(Enchantment enchantment, double level, boolean stored) {
		double enchantCost = net.minecraft.server.v1_15_R1.Enchantment.Rarity.COMMON.a() + 10
				- (enchantment instanceof CraftEnchantment ? ((CraftEnchantment) enchantment).getHandle().d().a()
						: net.minecraft.server.v1_15_R1.Enchantment.Rarity.UNCOMMON.a());
		enchantCost *= (stored ? 60 : 65);
		// Balance: Base cost on percentage of max level, not only current level
		enchantCost *= Math.pow(2D, Math.abs(level)) / Math.pow(2D, enchantment.getMaxLevel());
		if (enchantment.getKey().getKey().contains("curse")) {
			// Curses are also treasure, should be handled first.
			enchantCost /= 2.5;
		} else if (enchantment.isTreasure()) {
			// Rarer, increase cost.
			enchantCost *= 1.5;
		}
		return enchantCost;
	}

	public static Map<Material, Double> getMana() {
		if (manaMappings != null) {
			return manaMappings;
		}

		manaMappings = createBaseMana();

		for (String variant : new String[] { "BRAIN", "BUBBLE", "FIRE", "HORN", "TUBE" }) {
			manaMappings.put(Material.matchMaterial(String.format("%s_CORAL_FAN", variant)), 4D);
			manaMappings.put(Material.matchMaterial(String.format("%s_CORAL", variant)), 16D);
			manaMappings.put(Material.matchMaterial(String.format("%s_CORAL_BLOCK", variant)), 64D);
			manaMappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL_FAN", variant)), 2D);
			manaMappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL", variant)), 8D);
			manaMappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL_BLOCK", variant)), 32D);
		}

		// Fill from recipes
		for (Material material : Material.values()) {
			addRecipeCosts(material);
		}

		// Special cases
		manaMappings.put(Material.CHIPPED_ANVIL, manaMappings.get(Material.ANVIL) / 3 * 2);
		manaMappings.put(Material.DAMAGED_ANVIL, manaMappings.get(Material.ANVIL) / 3);
		manaMappings.put(Material.FILLED_MAP, manaMappings.get(Material.MAP));
		manaMappings.put(Material.FIREWORK_ROCKET, manaMappings.get(Material.PAPER));
		manaMappings.put(Material.FIREWORK_STAR, manaMappings.get(Material.GUNPOWDER));
		manaMappings.put(Material.POTION, manaMappings.get(Material.GLASS_BOTTLE) + 1);
		manaMappings.put(Material.SPLASH_POTION, manaMappings.get(Material.POTION) + manaMappings.get(Material.GUNPOWDER));
		manaMappings.put(Material.LINGERING_POTION, manaMappings.get(Material.SPLASH_POTION) + manaMappings.get(Material.DRAGON_BREATH));
		manaMappings.put(Material.TIPPED_ARROW, manaMappings.get(Material.LINGERING_POTION) / 8 + manaMappings.get(Material.ARROW));
		manaMappings.put(Material.COD_BUCKET, manaMappings.get(Material.COD) + manaMappings.get(Material.BUCKET));
		manaMappings.put(Material.PUFFERFISH_BUCKET, manaMappings.get(Material.PUFFERFISH) + manaMappings.get(Material.BUCKET));
		manaMappings.put(Material.SALMON_BUCKET, manaMappings.get(Material.SALMON) + manaMappings.get(Material.BUCKET));
		manaMappings.put(Material.TROPICAL_FISH_BUCKET, manaMappings.get(Material.TROPICAL_FISH) + manaMappings.get(Material.BUCKET));
		manaMappings.put(Material.SUSPICIOUS_STEW, manaMappings.get(Material.BOWL) + manaMappings.get(Material.RED_MUSHROOM)
				+ manaMappings.get(Material.BROWN_MUSHROOM));
		// TODO why are these not detecting properly
		manaMappings.put(Material.BONE_BLOCK, manaMappings.get(Material.BONE_MEAL) * 9);
		manaMappings.put(Material.DRIED_KELP_BLOCK, manaMappings.get(Material.DRIED_KELP_BLOCK) * 9);

		for (DyeColor color : DyeColor.values()) {
			manaMappings.put(Material.matchMaterial(color.name() + "_SHULKER_BOX"), manaMappings.get(Material.SHULKER_BOX));
			manaMappings.put(Material.matchMaterial(color.name() + "_CONCRETE"),
					manaMappings.get(Material.matchMaterial(color.name() + "_CONCRETE_POWDER")) + 3);
		}

		manaMappings.put(Material.DEBUG_STICK,
				manaMappings.get(Material.NETHER_STAR) + 2 * manaMappings.get(Material.END_ROD));

		manaMappings.remove(Material.EMERALD);
		manaMappings.remove(Material.EMERALD_BLOCK);
		manaMappings.remove(Material.EMERALD_ORE);
		manaMappings.remove(Material.LAPIS_LAZULI);
		manaMappings.remove(Material.LAPIS_BLOCK);
		manaMappings.remove(Material.LAPIS_ORE);

		manaMappings.entrySet().removeIf(entry -> entry.getValue() == Double.MAX_VALUE);

		return manaMappings;
	}

	private static Map<Material, Double> createBaseMana() {
		Map<Material, Double> values = new HashMap<>();

		for (Material material : Material.values()) {
			switch (material) {
				case BEETROOT_SEEDS:
				case CLAY_BALL:
				case DEAD_BUSH:
				case DIRT:
				case FERN:
				case GRAVEL:
				case GRASS:
				case ACACIA_LEAVES:
				case BIRCH_LEAVES:
				case DARK_OAK_LEAVES:
				case JUNGLE_LEAVES:
				case OAK_LEAVES:
				case SPRUCE_LEAVES:
				case MELON_SLICE:
				case POISONOUS_POTATO:
				case SAND:
				case WHEAT_SEEDS:
				case SNOWBALL:
					values.put(material, 1D);
					break;
				case ALLIUM:
				case AZURE_BLUET:
				case BAMBOO:
				case BEETROOT:
				case CACTUS:
				case CARROT:
				case CHORUS_FRUIT:
				case COBBLESTONE:
				case CORNFLOWER:
				case DANDELION:
				case KELP:
				case LILY_PAD:
				case MUSHROOM_STEM:
				case NETHER_BRICK:
				case ORANGE_TULIP:
				case PAPER:
				case PINK_TULIP:
				case POPPY:
				case RABBIT_HIDE:
				case RED_MUSHROOM:
				case RED_MUSHROOM_BLOCK:
				case RED_SAND:
				case RED_TULIP:
				case SEAGRASS:
				case SOUL_SAND:
				case SUGAR_CANE:
				case SWEET_BERRIES:
				case VINE:
				case WHEAT:
				case WHITE_TULIP:
					values.put(material, 2D);
					break;
				case ANDESITE:
				case BROWN_MUSHROOM:
				case BROWN_MUSHROOM_BLOCK:
				case BLUE_ORCHID:
				case COCOA_BEANS:
				case CYAN_DYE:
				case DIORITE:
				case GRANITE:
				case LAPIS_LAZULI:
				case LILAC:
				case LILY_OF_THE_VALLEY:
				case NETHERRACK:
				case OXEYE_DAISY:
				case PEONY:
				case POTATO:
				case PURPLE_DYE:
				case ROSE_BUSH:
				case ROTTEN_FLESH:
				case STONE:
					values.put(material, 3D);
					break;
				case ARROW:
				case CHICKEN:
				case FEATHER:
				case LARGE_FERN:
				case TALL_GRASS:
				case SUNFLOWER:
					values.put(material, 4D);
					break;
				case CLAY:
				case FLINT:
				case RABBIT:
				case COD:
				case SALMON:
				case BLACK_WOOL:
				case BLUE_WOOL:
				case BROWN_WOOL:
				case CYAN_WOOL:
				case GRAY_WOOL:
				case GREEN_WOOL:
				case LIGHT_BLUE_WOOL:
				case LIGHT_GRAY_WOOL:
				case LIME_WOOL:
				case MAGENTA_WOOL:
				case ORANGE_WOOL:
				case PINK_WOOL:
				case PURPLE_WOOL:
				case RED_WOOL:
				case WHITE_WOOL:
				case YELLOW_WOOL:
					values.put(material, 5D);
					break;
				case BAKED_POTATO:
				case CARVED_PUMPKIN:
				case EGG:
				case NETHER_BRICKS:
				case PUMPKIN:
					values.put(material, 6D);
					break;
				case COOKED_CHICKEN:
				case ACACIA_LOG:
				case BIRCH_LOG:
				case DARK_OAK_LOG:
				case JUNGLE_LOG:
				case OAK_LOG:
				case SPRUCE_LOG:
				case ACACIA_WOOD:
				case BIRCH_WOOD:
				case DARK_OAK_WOOD:
				case JUNGLE_WOOD:
				case OAK_WOOD:
				case SPRUCE_WOOD:
				case STRIPPED_ACACIA_LOG:
				case STRIPPED_BIRCH_LOG:
				case STRIPPED_DARK_OAK_LOG:
				case STRIPPED_JUNGLE_LOG:
				case STRIPPED_OAK_LOG:
				case STRIPPED_SPRUCE_LOG:
				case STRIPPED_ACACIA_WOOD:
				case STRIPPED_BIRCH_WOOD:
				case STRIPPED_DARK_OAK_WOOD:
				case STRIPPED_JUNGLE_WOOD:
				case STRIPPED_OAK_WOOD:
				case STRIPPED_SPRUCE_WOOD:
				case MUTTON:
				case BEEF:
				case REDSTONE:
				case SEA_PICKLE:
				case STRING:
					values.put(material, 8D);
					break;
				case COOKED_COD:
				case COOKED_SALMON:
				case NETHER_WART:
				case PRISMARINE_SHARD:
					values.put(material, 9D);
					break;
				case END_STONE:
				case GLOWSTONE_DUST:
				case ICE:
				case LEATHER:
				case MELON:
				case MOSSY_COBBLESTONE:
				case PORKCHOP:
				case TROPICAL_FISH:
				case PUFFERFISH:
				case SLIME_BALL:
				case BLACK_STAINED_GLASS:
				case BLUE_STAINED_GLASS:
				case BROWN_STAINED_GLASS:
				case CYAN_STAINED_GLASS:
				case GRAY_STAINED_GLASS:
				case GREEN_STAINED_GLASS:
				case LIGHT_BLUE_STAINED_GLASS:
				case LIGHT_GRAY_STAINED_GLASS:
				case LIME_STAINED_GLASS:
				case MAGENTA_STAINED_GLASS:
				case ORANGE_STAINED_GLASS:
				case PINK_STAINED_GLASS:
				case PURPLE_STAINED_GLASS:
				case RED_STAINED_GLASS:
				case WHITE_STAINED_GLASS:
				case YELLOW_STAINED_GLASS:
					values.put(material, 10D);
					break;
				case APPLE:
				case BONE:
				case COAL:
				case COOKED_BEEF:
				case GOLD_NUGGET:
				case RABBIT_FOOT:
				case SPIDER_EYE:
				case INK_SAC:
					values.put(material, 12D);
					break;
				case BLACK_TERRACOTTA:
				case BLUE_TERRACOTTA:
				case BROWN_TERRACOTTA:
				case CYAN_TERRACOTTA:
				case GRAY_TERRACOTTA:
				case GREEN_TERRACOTTA:
				case LIGHT_BLUE_TERRACOTTA:
				case LIGHT_GRAY_TERRACOTTA:
				case LIME_TERRACOTTA:
				case MAGENTA_TERRACOTTA:
				case ORANGE_TERRACOTTA:
				case PINK_TERRACOTTA:
				case PURPLE_TERRACOTTA:
				case RED_TERRACOTTA:
				case WHITE_TERRACOTTA:
				case YELLOW_TERRACOTTA:
				case TERRACOTTA:
				case EXPERIENCE_BOTTLE: // 11 exp to fill a bottle, bottle worth roughly 1 and some padding
					values.put(material, 13D);
					break;
				case COOKED_PORKCHOP:
					values.put(material, 14D);
					break;
				case ACACIA_SAPLING:
				case BIRCH_SAPLING:
				case CHORUS_FLOWER:
				case DARK_OAK_SAPLING:
				case JUNGLE_SAPLING:
				case OAK_SAPLING:
				case SADDLE:
				case SPRUCE_SAPLING:
					values.put(material, 16D);
					break;
				case GUNPOWDER:
				case MAP: // Not crafted, right click
				case MYCELIUM:
				case PODZOL:
					values.put(material, 20D);
					break;
				case ENCHANTED_BOOK:
					values.put(material, 25D);
					break;
				case PACKED_ICE:
					values.put(material, 28D);
					break;
				case BLAZE_ROD:
				case GRASS_BLOCK:
					values.put(material, 30D);
					break;
				case BLACK_BANNER:
				case BLUE_BANNER:
				case BROWN_BANNER:
				case CYAN_BANNER:
				case GRAY_BANNER:
				case GREEN_BANNER:
				case LIGHT_BLUE_BANNER:
				case LIGHT_GRAY_BANNER:
				case LIME_BANNER:
				case MAGENTA_BANNER:
				case ORANGE_BANNER:
				case PINK_BANNER:
				case PURPLE_BANNER:
				case RED_BANNER:
				case WHITE_BANNER:
				case YELLOW_BANNER:
				case COBWEB:
				case GHAST_TEAR:
				case PHANTOM_MEMBRANE:
				case PRISMARINE_CRYSTALS:
					values.put(material, 35D);
					break;
				case QUARTZ:
				case DRAGON_BREATH:
					values.put(material, 37D);
					break;
				case IRON_INGOT:
					values.put(material, 41D);
					break;
				case COAL_ORE:
				case NETHER_QUARTZ_ORE:
					values.put(material, 44D);
					break;
				case IRON_ORE:
				case MUSIC_DISC_13:
				case MUSIC_DISC_CAT:
				case NAUTILUS_SHELL:
					values.put(material, 50D);
					break;
				case MUSIC_DISC_11:
				case MUSIC_DISC_BLOCKS:
				case MUSIC_DISC_CHIRP:
				case MUSIC_DISC_FAR:
				case MUSIC_DISC_MALL:
				case MUSIC_DISC_MELLOHI:
				case MUSIC_DISC_STAL:
				case MUSIC_DISC_STRAD:
				case MUSIC_DISC_WAIT:
				case MUSIC_DISC_WARD:
					values.put(material, 70D);
					break;
				case OBSIDIAN:
				case REDSTONE_ORE:
					values.put(material, 81D);
					break;
				case ENDER_PEARL:
				case WITHER_ROSE:
					values.put(material, 90D);
					break;
				case GOLD_INGOT:
					values.put(material, 108D);
					break;
				case GOLD_ORE:
				case LAVA_BUCKET:
				case MILK_BUCKET:
				case WATER_BUCKET:
					values.put(material, 138D);
					break;
				case DIAMOND:
					values.put(material, 167D);
					break;
				case DIAMOND_ORE:
					values.put(material, 187D);
					break;
				case IRON_HORSE_ARMOR:
					values.put(material, 261D);
					break;
				case GLOBE_BANNER_PATTERN:
					values.put(material, 300D);
					break;
				case NAME_TAG:
					values.put(material, 405D);
					break;
				case CHAINMAIL_BOOTS:
					values.put(material, 600D);
					break;
				case GOLDEN_HORSE_ARMOR:
					values.put(material, 663D);
					break;
				case CHAINMAIL_HELMET:
				case SCUTE:
				case SHULKER_SHELL:
					values.put(material, 750D);
					break;
				case DIAMOND_HORSE_ARMOR:
				case WET_SPONGE:
					values.put(material, 1000D);
					break;
				case CHAINMAIL_LEGGINGS:
					values.put(material, 1050D);
					break;
				case CHAINMAIL_CHESTPLATE:
					values.put(material, 1200D);
					break;
				case BELL:
					values.put(material, 1336D);
					break;
				case DRAGON_HEAD:
				case WITHER_SKELETON_SKULL:
					values.put(material, 3000D);
					break;
				case ELYTRA:
				case TRIDENT:
					values.put(material, 3142D);
					break;
				case TOTEM_OF_UNDYING:
				case HEART_OF_THE_SEA:
					values.put(material, 5000D);
					break;
				case NETHER_STAR:
					values.put(material, 10000D);
					break;
				case PLAYER_HEAD:
				case CREEPER_HEAD:
				case ZOMBIE_HEAD:
				case SKELETON_SKULL:
					values.put(material, 16000D);
					break;
				case DRAGON_EGG:
					values.put(material, 32000D);
					break;
				// Unobtainable, don't bother searching recipes
				case AIR:
				case BARRIER:
				case BEDROCK:
				case COMMAND_BLOCK:
				case COMMAND_BLOCK_MINECART:
				case REPEATING_COMMAND_BLOCK:
				case CHAIN_COMMAND_BLOCK:
				case END_PORTAL:
				case END_PORTAL_FRAME:
				case SPAWNER:
				// Money
				case EMERALD:
				case EMERALD_BLOCK:
				case EMERALD_ORE:
				case LAPIS_BLOCK:
				case LAPIS_ORE:
				// Added later
				case POTION:
				case TIPPED_ARROW:
				case SPLASH_POTION:
				case LINGERING_POTION:
				// Duplicate via other means, not alchemy
				case WRITTEN_BOOK:
				case WRITABLE_BOOK:
					values.put(material, Double.MAX_VALUE);
				default:
					break;
			}
		}
		return values;
	}

	private static void addRecipeCosts(Material material) {
		addRecipeCosts(material, new ArrayList<>());
	}

	private static double addRecipeCosts(Material material, List<Material> pastMaterials) {
		// Check if calculated already
		if (manaMappings.containsKey(material)) {
			return manaMappings.get(material);
		}

		// Check if mid-calculation
		if (pastMaterials.contains(material)) {
			return Double.MAX_VALUE;
		}

		// Create a new list for sub-elements
		pastMaterials = new ArrayList<>(pastMaterials);
		// Add to mid-calc list
		pastMaterials.add(material);

		double minimum = Double.MAX_VALUE;

		nextRecipe: for (Recipe bukkitRecipe : Bukkit.getRecipesFor(new ItemStack(material))) {
			ItemStack result = bukkitRecipe.getResult();
			int amount = result.getAmount();
			if (amount < 1) {
				continue;
			}

			RecipeWrapper recipe = new RecipeWrapper(bukkitRecipe);

			double newMinimum = 0;

			for (Map.Entry<EnumSet<Material>, Integer> ingredient : recipe.getRecipeIngredients().entrySet()) {
				if (ingredient.getValue() == null || ingredient.getValue() < 1) {
					continue nextRecipe;
				}

				double bestMaterialPrice = Double.MAX_VALUE;
				for (Material potential : ingredient.getKey()) {
					if (pastMaterials.contains(potential)) {
						continue;
					}

					bestMaterialPrice = Math.min(bestMaterialPrice, addRecipeCosts(potential, pastMaterials));

					if (potential.name().endsWith("_BUCKET")) {
						// Buckets are not consumed in crafting.
						// Iron ingots are 41, hardcoded bucket value here to avoid a lot of extra code to prevent issues
						newMinimum -= 123 * ingredient.getValue();
					}
				}

				if (bestMaterialPrice < 0 || bestMaterialPrice > Double.MAX_VALUE / ingredient.getValue()) {
					continue nextRecipe;
				}

				bestMaterialPrice *= ingredient.getValue();

				if (Double.MAX_VALUE - bestMaterialPrice < newMinimum) {
					continue nextRecipe;
				}

				newMinimum += bestMaterialPrice;
			}

			if (newMinimum <= 0 || newMinimum == Double.MAX_VALUE) {
				continue;
			}

			// Coal is 12, 8 smelts per coal = 1.5 cost per smelt. Hardcoded to prevent a bit of extra code and checks.
			if (bukkitRecipe instanceof CookingRecipe) {
				if (newMinimum >= Double.MAX_VALUE - 1.5) {
					continue;
				}
				newMinimum += 1.5;
			}

			if (newMinimum == Double.MAX_VALUE) {
				continue;
			}

			newMinimum /= amount;
			if (newMinimum < minimum) {
				minimum = newMinimum;
			}
		}

		// No value = no make.
		if (minimum <= 0) {
			minimum = Double.MAX_VALUE;
		}

		// Map and return.
		manaMappings.put(material, minimum);
		return minimum;
	}

}
