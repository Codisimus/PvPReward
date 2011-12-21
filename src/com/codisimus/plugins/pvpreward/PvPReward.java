package com.codisimus.plugins.pvpreward;

import com.codisimus.plugins.pvpreward.listeners.EntityEventListener;
import com.codisimus.plugins.pvpreward.listeners.BlockEventListener;
import com.codisimus.plugins.pvpreward.listeners.CommandListener;
import com.codisimus.plugins.pvpreward.listeners.EntityEventListener.RewardType;
import com.codisimus.plugins.pvpreward.listeners.PlayerEventListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class PvPReward extends JavaPlugin {
    public static Server server;
    public static Permission permission;
    public static String karmaName;
    public static String outlawName;
    public static int cooldownTime;
    public static boolean negative;
    private static boolean enabled = true;
    private static PluginManager pm;
    private static Properties p;
    public static LinkedList<Record> records = new LinkedList<Record>();
    private static boolean save = true;

    /**
     * Clears all graves that exist when this Plugin is disabled
     *
     */
    @Override
    public void onDisable () {
        if (!EntityEventListener.digGraves)
            return;

        for (Record record: records)
            //Reset the Sign to AIR if there is one
            if (record.signLocation != null)
                record.signLocation.getBlock().setTypeId(0);
        
        //Disable cooldown Thread
        enabled = false;
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        server = getServer();
        pm = server.getPluginManager();
        
        //Load Config settings
        loadSettings();
        
        //Find Permissions
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        
        //Find Economy
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            Econ.economy = economyProvider.getProvider();
        
        //Load Records Data
        loadData();
        
        //Register Events
        PlayerEventListener playerListener = new PlayerEventListener();
        EntityEventListener entityListener = new EntityEventListener();
        pm.registerEvent(Type.BLOCK_BREAK, new BlockEventListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Monitor, this);
        getCommand("pvp").setExecutor(new CommandListener());
        
        System.out.println("PvPReward "+this.getDescription().getVersion()+" is enabled!");
        
        //Start cooldown Thread if there is one
        if (cooldownTime != 0)
            cooldown();
    }
    
    /**
     * Moves file from PvPReward.jar to the appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/PvPReward.jar");
            ZipEntry entry = jar.getEntry(fileName);

            //Create the destination folder if it does not exist
            String destination = "plugins/PvPReward/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();

            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            
            //Copy the file
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception moveFailed) {
            System.err.println("[PvPReward] File Move Failed!");
            moveFailed.printStackTrace();
        }
    }

    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        p = new Properties();
        try {
            //Copy the file from the jar if it is missing
            if (!new File("plugins/PvPReward/config.properties").exists())
                moveFile("config.properties");
            
            FileInputStream fis = new FileInputStream("plugins/PvPReward/config.properties");
            p.load(fis);
            
            EntityEventListener.deadedMsg = format(loadValue("KilledMessage"));
            EntityEventListener.killerMsg = format(loadValue("KillerMessage"));
            EntityEventListener.deadedNotEnoughMoneyMsg = format(loadValue("KilledNotEnoughMoney"));
            EntityEventListener.killerNotEnoughMoneyMsg = format(loadValue("KillerNotEnoughMoney"));
            EntityEventListener.outlawBroadcast = format(loadValue("OutlawBroadcast"));
            EntityEventListener.noLongerOutlawBroadcast = format(loadValue("NoLongerOutlawBroadcast"));
            EntityEventListener.karmaDecreasedMsg = format(loadValue("KarmaDecreased"));
            EntityEventListener.karmaIncreasedMsg = format(loadValue("KarmaIncreased"));
            EntityEventListener.karmaNoChangeMsg = format(loadValue("KarmaNoChange"));

            String tollType = loadValue("DeathTollType");
            if (tollType.equalsIgnoreCase("none")) {
                EntityEventListener.tollAsPercent = false;
                EntityEventListener.tollAmount = 0;
            }
            else {
                EntityEventListener.deathTollMsg = format(loadValue("DeathTollMessage"));
                EntityEventListener.tollAmount = Double.parseDouble(loadValue("DeathToll"));
                EntityEventListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));

                if (tollType.equalsIgnoreCase("percent")) 
                    EntityEventListener.tollAsPercent = true;
                else if (tollType.equalsIgnoreCase("flatrate")) {
                    EntityEventListener.tollAsPercent = false;
                    EntityEventListener.tollAmount = Double.parseDouble(loadValue("DeathToll"));
                } 
            }
            
            EntityEventListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));
            EntityEventListener.digGraves = Boolean.parseBoolean(loadValue("DigGraves"));

            PlayerEventListener.denyTeleMsg = format(loadValue("DenyTeleMessage"));
            PlayerEventListener.denyTele = Boolean.parseBoolean(loadValue("DenyTele"));
            PlayerEventListener.telePenalty = Integer.parseInt(loadValue("TelePenalty"));
            PlayerEventListener.penalizeLoggers = Boolean.parseBoolean(loadValue("PenalizeLoggers"));

            Record.combatTimeOut = Integer.parseInt(loadValue("CombatTime")) * 1000;
            Record.graveTimeOut = Integer.parseInt(loadValue("GraveTime")) * 1000;
            Record.graveRob = format(loadValue("GraveRobMessage"));

            EntityEventListener.outlawTag = format(loadValue("OutlawTag"));
            karmaName = loadValue("KarmaName");
            outlawName = loadValue("OutlawName");
            cooldownTime = Integer.parseInt(loadValue("CooldownTime")) * 60000;

            EntityEventListener.rewardType = RewardType.valueOf(loadValue("RewardType").toUpperCase());
            EntityEventListener.percent = Integer.parseInt(loadValue("Percent"));
            EntityEventListener.amount = Double.parseDouble(loadValue("Amount"));
            EntityEventListener.hi = Integer.parseInt(loadValue("High"));
            EntityEventListener.lo = Integer.parseInt(loadValue("Low"));

            EntityEventListener.threshold = Integer.parseInt(loadValue("KarmaThreshold"));
            EntityEventListener.modifier = Integer.parseInt(loadValue("OutlawModifier")) / 100;
            EntityEventListener.max = Integer.parseInt(loadValue("ModifierMax")) / 100;
            EntityEventListener.whole = Boolean.parseBoolean(loadValue("WholeNumbers"));

            negative = Boolean.parseBoolean(loadValue("Negative"));
            
            EntityEventListener.tollDisabledIn = new LinkedList<String>
                    (Arrays.asList(loadValue("DisableDeathTollInWorlds").split(", ")));
            EntityEventListener.rewardDisabledIn = new LinkedList<String>
                    (Arrays.asList(loadValue("DisableRewardInWorlds").split(", ")));
            
            EntityEventListener.outlawGroup = loadValue("OutlawGroup");
            EntityEventListener.normalGroup = loadValue("NormalGroup");
            
            fis.close();
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load PvPReward "+this.getDescription().getVersion());
            missingProp.printStackTrace();
        }
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[PvPReward] Missing value for "+key+" in config file");
            System.err.println("[PvPReward] Please regenerate config file");
        }

        return p.getProperty(key);
    }

    /**
     * Returns false if either Player does not have proper permission
     * killer will be checked for "pvpreward.getreward"
     * deaded will be checked for "pvpreward.givereward"
     * 
     * @param killer The Player who would receive the reward
     * @param deaded The Player who the reward would be taken from
     * @return true both Players have proper permission
     */
    public static boolean hasPermisson(Player killer, Player deaded) {
        return permission.has(killer, "pvpreward.getreward") && permission.has(deaded, "pvpreward.givereward");
    }
    
    /**
     * Returns true the given Player has the given Permission node
     * 
     * @param player The Player who would receive the reward
     * @param node The String of the node
     * @return true if either Player does not have proper permission
     */
    public static boolean hasPermisson(Player player, String node) {
        return permission.has(player, "pvpreward."+node);
    }
    
    /**
     * Subtracts 1 karma from all online Players' Records
     * This is repeated on an interval of the assigned cooldownTime
     */
    private static void cooldown() {
        //Start a new thread
        Thread cooldown = new Thread() {
            @Override
            public void run() {
                try {
                    while (enabled) {
                        Thread.currentThread().sleep(cooldownTime);
                        
                        for (Record record: records) {
                            //Check if the Player is online
                            Player player = server.getPlayer(record.name);
                            if (player != null) {
                                record.karma--;
                                
                                //Check if the Player is no longer an Outlaw
                                if (record.karma == EntityEventListener.amount) {
                                    //Add the Player to the Normal group if there is one
                                    if (!EntityEventListener.normalGroup.equals("")) {
                                        PvPReward.permission.playerRemoveGroup(player,
                                                EntityEventListener.outlawGroup);
                                        PvPReward.permission.playerAddGroup(player,
                                                EntityEventListener.normalGroup);
                                    }
                    
                                    player.setDisplayName(player.getName());
                                    
                                    PvPReward.server.broadcastMessage(EntityEventListener.getMsg(
                                            EntityEventListener.noLongerOutlawBroadcast,
                                            1, player.getName(), "", record.karma+""));
                                }
                                
                                save();
                            }
                        }
                    }
                }
                catch (Exception e) {
                }
            }
        };
        cooldown.start();
    }

    /**
     * Adds various Unicode characters and colors to a string
     *
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }

    /**
     * Reads save file to load PvPReward data
     * Saving is turned off if an error occurs
     */
    private static void loadData() {
        try {
            new File("plugins/PvPReward/pvpreward.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/PvPReward/pvpreward.save"));
            String line = bReader.readLine();
            while (line != null) {
                String[] split = line.split(";");
                
                String player = split[0];
                int kills = Integer.parseInt(split[1]);
                int deaths = Integer.parseInt(split[2]);
                int karma = Integer.parseInt(split[3]);
                
                records.add(new Record(player, kills, deaths, karma));
                line = bReader.readLine();
            }
            
            bReader.close();
        }
        catch (Exception loadFailed) {
            save = false;
            System.out.println("[PvPReward] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save() {
        //Cancel if saving is turned off
        if (!save)
            return;

        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/PvPReward/pvpreward.save"));
            for(Record record: records) {
                //Write data in the format "name;kills;deaths;karma"
                bWriter.write(record.name.concat(";"));
                bWriter.write(record.kills+";");
                bWriter.write(record.deaths+";");
                bWriter.write(String.valueOf(record.karma));
                
                //Write each Record on a new line
                bWriter.newLine();
            }

            bWriter.close();
        }
        catch (Exception saveFailed) {
            System.err.println("[PvPReward] Save Failed!");
            saveFailed.printStackTrace();
        }
    }

    /**
     * Returns the Record for the given Player
     * A new Record is created if one is not found
     * 
     * @param player The name of the Player
     * @return The Record of the Player
     */
    public static Record getRecord(String player) {
        for(Record record: records)
            if (record.name.equals(player))
                return record;

        //Create a new Record
        Record newRecord = new Record(player);
        records.add(newRecord);
        return newRecord;
    }

    /**
     * Returns the Record for the given Player
     * 
     * @param player The name of the Player
     * @return The Record of the Player
     */
    public static Record findRecord(String player) {
        for(Record record: records)
            if (record.name.equals(player))
                return record;

        //Return null because the Player does not have a Record
        return null;
    }
}
