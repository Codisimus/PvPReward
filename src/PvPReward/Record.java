
package PvPReward;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * A Record contains a Players name number of kills/deaths, karma level
 * A Record also contains the last known attacker within the time frame
 * 
 * @author Codisimus
 */
class Record {
    protected String player;
    public int kills;
    public int deaths = 0;
    public int karma = 0;
    public double kdr = 0;
    private String attacker = "";
    protected long attackedTime = 0;
    protected static int combatTimeOut;
    protected static int graveTimeOut;
    private Block lastKnown;
    protected Location signLocation;
    protected Sign tombstone;
    private LinkedList<ItemStack> grave = new LinkedList<ItemStack>();
    protected static String graveRob;

    /**
     * Constructs a new Record and sets the kdr
     * 
     * @param player The name of the player on Record
     * @param kills The amount of other Players the Player killed
     * @param deaths The amount of times this Player was killed
     * @param karma The current karma level of the Player
     */
    public Record(String player, int kills, int deaths, int karma) {
        this.player = player;
        this.kills = kills;
        this.deaths = deaths;
        this.karma = karma;
        if (deaths == 0)
            kdr = kills;
        else
            kdr = (double)kills/deaths;
        long temp =(long)(kdr*100);
        kdr = (double)temp/100;
    }
    
    /**
     * Sets the last known location, and inventory
     * 
     * @param wounded The Player who took damage
     */
    protected void updateWhereabouts(Player wounded) {
        lastKnown = wounded.getLocation().getBlock();
    }

    /**
     * Sets the attacker and time of attack
     * 
     * @param attacker The name of the attacker of this Player
     */
    protected void setAttacker(String attacker) {
        this.attacker = attacker;
        attackedTime = System.currentTimeMillis();
    }

    /**
     * Returns the killer of this Player
     * 
     * @return The attacker if it is within the given time
     */
    public String getKiller() {
        long deathTime = System.currentTimeMillis();
        if ((attackedTime + combatTimeOut) < deathTime)
            return "";
        return attacker;
    }
    
    /**
     * Adds a kill to the Record and 2 karma points
     * 
     * @return The new kdr
     */
    protected double addKill() {
        kills++;
        karma = karma + 2;
        if (deaths == 0)
            kdr = kills;
        else
            kdr = (double)kills/deaths;
        long temp =(long)(kdr*100);
        kdr = (double)temp/100;
        SaveSystem.save();
        return kdr;
    }

    /**
     * Adds a death to the Record and subtracts a karma point
     * 
     * @return The new kdr
     */
    protected double addDeath() {
        deaths++;
        karma--;
        if (karma < 0)
            karma = 0;
        if (kills == 0)
            kdr = 1/deaths;
        else
            kdr = (double)kills/deaths;
        long temp =(long)(kdr*100);
        kdr = (double)temp/100;
        SaveSystem.save();
        return kdr;
    }

    /**
     * Creates a tombstone for specific amount of time
     * 
     * @param dropped A List of items that were dropped
     */
    protected void digGrave(List<ItemStack> dropped) {
        grave.clear();
        for (int i=0; i<dropped.size(); i++) {
            if (dropped.get(i) != null) {
                grave.add(dropped.get(i));
                dropped.remove(i);
            }
        }
        lastKnown.setType(Material.SIGN_POST);
        signLocation = lastKnown.getLocation();
        tombstone = (Sign)lastKnown.getState();
        tombstone.setLine(1, "Here Lies");
        tombstone.setLine(2, player);
        //Start a new thread
        Thread dig = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(graveTimeOut);
                }
                catch (Exception e) {
                }
                lastKnown.setType(Material.AIR);
                signLocation = null;
            }
        };
        dig.start();
    }
    
    /**
     * Put items from grave into Player's Inventory
     * Also removes tombstone
     * 
     * @param graveRobber The Player taking the items
     */
    protected void robGrave(Player graveRobber) {
        PlayerInventory sack = graveRobber.getInventory();
        for (ItemStack item : grave) {
            sack.addItem(item);
        }
        graveRobber.sendMessage(graveRob);
        tombstone.getBlock().setType(Material.AIR);
    }
}
