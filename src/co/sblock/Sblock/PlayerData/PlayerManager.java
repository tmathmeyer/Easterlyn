package co.sblock.Sblock.PlayerData;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;

/**
 * Class that keeps track of players currently logged on to the game
 * @author FireNG
 *
 */
public class PlayerManager {

    private static PlayerManager manager;
    
    private Map<String, SblockPlayer> players;
    
    PlayerManager() {
	manager = this;
	this.players = new HashMap<String, SblockPlayer>();
    }
    
    /**
     * Adds a player that has logged on to the players list
     * @param player The player that has logged on
     */
    public void addPlayer(Player player) {
	Map<String, Object> result = DatabaseManager.getDatabaseManager().loadPlayer(player.getName());
	if(result != null)
	{

	    PlayerClass pclass = PlayerClass.valueOf((String) result.get("class"));
	    PlayerAspect aspect = PlayerAspect.valueOf((String) result.get("aspect"));
	    DreamPlanet dplanet = DreamPlanet.valueOf((String) result.get("dPlanet"));
	    MediumPlanet mplanet = MediumPlanet.valueOf((String) result.get("mPlanet"));
	    players.put(player.getName(), new SblockPlayer(player.getName(), pclass, aspect, mplanet, dplanet, ((Integer) result.get("towerNum")).intValue(), (Boolean) result.get("sleepState"), parseLocation((String) result.get("previousLoc"))));
	}
	else
	    //New player...
	    players.put(player.getName(), new SblockPlayer(player.getName()));
    }
    
    /**
     * Removes a player from the players list that has left the server
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        players.remove(player.getName());        
    }

    /**
     * Converts the given string into a Bukkit Location object
     * @param string The string to convert
     * @return The Location object represented by this string, or null if the input string is null
     * @throws IllegalArgumentException if the given string is not valid
     */
    public static Location parseLocation(String loc) {
	if(loc == null)
	    return null;
	try {
	    String[] parts = loc.split(",");
	    return new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
	}
	catch(NumberFormatException e) {
	    throw new IllegalArgumentException("String is not in correct format.", e);
	}
	catch(ArrayIndexOutOfBoundsException e) {
	    throw new IllegalArgumentException("String is not in correct format.", e);
	}
    }
    
    /**
     * Converts the given Bukkit Location object to a string for storing in the database.
     * @param loc The Location object to convert
     * @return a String representation of the object
     */
    public static String locationToString(Location loc) {
	if(loc == null)
	    return null;
	return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
    
    public static PlayerManager getPlayerManager()
    {
	if(manager == null)
	    manager = new PlayerManager();
	return manager;
    }

}
