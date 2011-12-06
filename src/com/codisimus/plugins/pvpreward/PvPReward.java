package com.codisimus.plugins.pvpreward;

import com.codisimus.plugins.pvpreward.listeners.pluginListener;
import com.codisimus.plugins.pvpreward.listeners.entityListener;
import com.codisimus.plugins.pvpreward.listeners.blockListener;
import com.codisimus.plugins.pvpreward.listeners.commandListener;
import com.codisimus.plugins.pvpreward.listeners.entityListener.RewardType;
import com.codisimus.plugins.pvpreward.listeners.playerListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Permissions
 *
 * @author Codisimus
 */
public class PvPReward extends JavaPlugin {
    public static Server server;
    public static PermissionManager permissions;
    public static String karmaName;
    public static String outlawName;
    public static int cooldownTime;
    public static boolean negative;
    public static PluginManager pm;
    public Properties p;

    /**
     * Clears all graves that exist when this Plugin is disabled
     *
     */
    @Override
    public void onDisable () {
        if (!entityListener.digGraves)
            return;

        for (Record record: SaveSystem.records)
            //Reset the Sign to AIR if there is one
            if (record.signLocation != null)
                record.signLocation.getBlock().setTypeId(0);
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        server = getServer();
        pm = server.getPluginManager();
        checkFiles();
        loadConfig();
        SaveSystem.load();
        registerEvents();
        getCommand("pvp").setExecutor(new commandListener());
        System.out.println("PvPReward "+this.getDescription().getVersion()+" is enabled!");
        if (cooldownTime != 0)
            cooldown();
    }
    
    /**
     * Makes sure all needed files exist
     *
     */
    public void checkFiles() {
        if (!new File("plugins/PvPReward/config.properties").exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from PvPReward.jar to the appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    public void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/PvPReward.jar");
            ZipEntry entry = jar.getEntry(fileName);

            //Create the destination folder if it does not exist
            String destination = "plugins/PvPReward/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();

            //Copy the file
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
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
    public void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/PvPReward/config.properties"));
        }
        catch (Exception e) {
        }
        entityListener.deadedMessage = format(loadValue("KilledMessage"));
        entityListener.killerMessage = format(loadValue("KillerMessage"));
        entityListener.deadedNotEnoughMoneyMessage = format(loadValue("KilledNotEnoughMoney"));
        entityListener.killerNotEnoughMoneyMessage = format(loadValue("KillerNotEnoughMoney"));
        entityListener.outlawBroadcast = format(loadValue("OutlawBroadcast"));
        entityListener.noLongerOutlawBroadcast = format(loadValue("NoLongerOutlawBroadcast"));
        entityListener.karmaDecreasedMessage = format(loadValue("KarmaDecreased"));
        entityListener.karmaIncreasedMessage = format(loadValue("KarmaIncreased"));
        entityListener.karmaNoChangeMessage = format(loadValue("KarmaNoChange"));
        
        String tollType = loadValue("DeathTollType");
        if (tollType.equalsIgnoreCase("none")) {
            entityListener.tollAsPercent = false;
            entityListener.tollAmount = 0;
        }
        else {
            entityListener.deathTollMessage = format(loadValue("DeathTollMessage"));
            entityListener.tollAmount = Double.parseDouble(loadValue("DeathToll"));
            entityListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));
            
            if (tollType.equalsIgnoreCase("percent")) 
                entityListener.tollAsPercent = true;
            else if (tollType.equalsIgnoreCase("flatrate")) {
                entityListener.tollAsPercent = false;
                entityListener.tollAmount = Double.parseDouble(loadValue("DeathToll"));
            } 
        }
        
        entityListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));
        entityListener.digGraves = Boolean.parseBoolean(loadValue("DigGraves"));
        playerListener.denyTeleMessage = format(loadValue("DenyTeleMessage"));
        playerListener.denyTele = Boolean.parseBoolean(loadValue("DenyTele"));
        playerListener.telePenalty = Integer.parseInt(loadValue("TelePenalty"));
        playerListener.penalizeLoggers = Boolean.parseBoolean(loadValue("PenalizeLoggers"));
        Record.combatTimeOut = Integer.parseInt(loadValue("CombatTime")) * 1000;
        Record.graveTimeOut = Integer.parseInt(loadValue("GraveTime")) * 1000;
        Record.graveRob = format(loadValue("GraveRobMessage"));
        entityListener.outlawTag = format(loadValue("OutlawTag"));
        karmaName = loadValue("KarmaName");
        outlawName = loadValue("OutlawName");
        cooldownTime = Integer.parseInt(loadValue("CooldownTime")) * 60000;
        Register.economy = loadValue("Economy");
        pluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
        entityListener.rewardType = RewardType.valueOf(loadValue("RewardType").toUpperCase());
        entityListener.percent = Integer.parseInt(loadValue("Percent"));
        entityListener.amount = Double.parseDouble(loadValue("Amount"));
        entityListener.hi = Integer.parseInt(loadValue("High"));
        entityListener.lo = Integer.parseInt(loadValue("Low"));
        entityListener.threshold = Integer.parseInt(loadValue("KarmaThreshold"));
        entityListener.modifier = Integer.parseInt(loadValue("OutlawModifier")) / 100;
        entityListener.max = Integer.parseInt(loadValue("ModifierMax")) / 100;
        entityListener.whole = Boolean.parseBoolean(loadValue("WholeNumbers"));
        PvPReward.negative = Boolean.parseBoolean(loadValue("Negative"));
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[PvPReward] Missing value for "+key+" in config file");
            System.err.println("[PvPReward] Please regenerate config file");
        }

        return p.getProperty(key);
    }
    
    /**
     * Registers events for the PvPReward Plugin
     *
     */
    public void registerEvents() {
        playerListener playerListener = new playerListener();
        entityListener entityListener = new entityListener();
        pm.registerEvent(Type.PLUGIN_ENABLE, new pluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BREAK, new blockListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Monitor, this);
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
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(killer, "pvpreward.getreward") && permissions.has(deaded, "pvpreward.givereward");

        return true;
    }
    
    /**
     * Returns true the given Player has the given Permission node
     * 
     * @param player The Player who would receive the reward
     * @param node The String of the node
     * @return true if either Player does not have proper permission
     */
    public static boolean hasPermisson(Player player, String node) {
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(player, "pvpreward."+node);

        //Return Bukkit Permission value
        return player.hasPermission("pvpreward."+node);
    }
    
    /**
     * Subtracts 1 karma from all online Players' Records
     * This is repeated on an interval of the assigned cooldownTime
     */
    public static void cooldown() {
        //Start a new thread
        Thread cooldown = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.currentThread().sleep(cooldownTime);
                        
                        for (Record record: SaveSystem.records) {
                            //Check if the Player is online
                            Player player = server.getPlayer(record.name);
                            if (player != null) {
                                record.karma--;
                                
                                if (record.karma == entityListener.amount) {
                                    player.setDisplayName(player.getName());
                                    
                                    PvPReward.server.broadcastMessage(entityListener.getMsg(
                                            entityListener.noLongerOutlawBroadcast,
                                            1, player.getName(), "", record.karma+""));
                                }
                                
                                SaveSystem.save();
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
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}
