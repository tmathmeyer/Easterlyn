package co.sblock.users;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import co.sblock.chat.ColorDef;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author Jikoo, Dublek
 */
public enum Region {
	EARTH, INNERCIRCLE, OUTERCIRCLE, FURTHESTRING,
	MEDIUM, LOWAS, LOLAR, LOHAC, LOFAF, UNKNOWN;

	/**
	 * Gets the name of the Region.
	 * 
	 * @return Region.name() in lower case
	 */
	public String getRegionName() {
		return this.name().toLowerCase();
	}

	/**
	 * Case-insensitive alternative to valueOf.
	 * 
	 * @param s the String to match
	 * 
	 * @return the Region that matches, Region.Earth if invalid.
	 */
	public static Region uValueOf(String s) {
		s = s.toUpperCase();
		try {
			return (Region.valueOf(s));
		} catch (IllegalArgumentException e) {
			if (s.contains("MEDIUM")) {
				return Region.MEDIUM;
			}
			return Region.EARTH;
		}
	}

	/**
	 * Gets the ChatColor that indicates users are in a specific Region.
	 * 
	 * @param r the Region to get the ChatColor of
	 * 
	 * @return the relevant ChatColor
	 */
	public static ChatColor getRegionColor(Region r) {
		switch (r) {
		case EARTH:
			return ColorDef.WORLD_EARTH;
		case FURTHESTRING:
			return ColorDef.WORLD_FURTHESTRING;
		case INNERCIRCLE:
			return ColorDef.WORLD_INNERCIRCLE;
		case LOFAF:
		case LOHAC:
		case LOLAR:
		case LOWAS:
		case MEDIUM:
			return ColorDef.WORLD_MEDIUM;
		case OUTERCIRCLE:
			return ColorDef.WORLD_OUTERCIRCLE;
		default:
			return ColorDef.DEFAULT;
		}
	}

	/**
	 * Gets the Region that a Location is within.
	 * 
	 * @param l the Location to get the Region of
	 * 
	 * @return the relevant Region
	 */
	public static Region getLocationRegion(Location l) {
		try {
			Region r = Region.uValueOf(l.getWorld().getName());
			if (r.equals(Region.MEDIUM)) {
				boolean plusX = (l.getBlockX() >= 0);
				boolean plusZ = (l.getBlockZ() >= 0);
				if (plusX && plusZ)
					r = Region.LOWAS;
				else if (!plusX && plusZ)
					r = Region.LOLAR;
				else if (!plusX && !plusZ)
					r = Region.LOHAC;
				else if (plusX && !plusZ)
					r = Region.LOFAF;
			}
			return r;
		} catch (IllegalStateException e) {
			// Player is in an invalid world
			// For the sake of region channels, default to earth.
			return Region.EARTH;
		}
	}
}