
package PvPReward;

import com.nijiko.permissions.PermissionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Codisimus
 */
public class PvPReward extends JavaPlugin {
    protected static Server server;
    protected static PermissionHandler permissions;
    protected static String outlawTag;
    protected static String karmaName;
    protected static String outlawName;
    private static int cooldownTime;
    protected static PluginManager pm;
    private Properties p;

    @Override
    public void onDisable () {
    }

    @Override
    public void onEnable () {
        server = getServer();
        checkFiles();
        loadConfig();
        SaveSystem.loadFromFile();
        PvPRewardPlayerListener playerListener = new PvPRewardPlayerListener();
        PvPRewardEntityListener entityListener = new PvPRewardEntityListener();
        pm = server.getPluginManager();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new PluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BREAK, new PvPRewardBlockListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        System.out.println("PvPReward "+this.getDescription().getVersion()+" is enabled!");
        if (cooldownTime != 0)
            cooldown();
    }
    
    /**
     * Makes sure all needed files exist
     * Register.jar is for economy support
     */
    private void checkFiles() {
        File file = new File("lib/Register.jar");
        if (!file.exists() || file.length() < 43000)
            moveFile("Register.jar");
        file = new File("plugins/PvPReward/config.properties");
        if (!file.exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from PvPReward.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            JarFile jar = new JarFile("plugins/PvPReward.jar");
            ZipEntry entry = jar.getEntry(fileName);
            String destination = "plugins/PvPReward/";
            if (fileName.equals("Register.jar")) {
                System.out.println("[PvPReward] Moving Files... Please Reload Server");
                destination = "lib/";
            }
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads settings from the config.properties file
     * 
     */
    private void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/PvPReward/config.properties"));
        }
        catch (Exception e) {
        }
        PvPRewardEntityListener.killedMessage = loadValue("KilledMessage").replaceAll("&", "§");
        PvPRewardEntityListener.killerMessage = loadValue("KillerMessage").replaceAll("&", "§");
        PvPRewardEntityListener.killedNotEnoughMoney = loadValue("KilledNotEnoughMoney").replaceAll("&", "§");
        PvPRewardEntityListener.killerNotEnoughMoney = loadValue("KillerNotEnoughMoney").replaceAll("&", "§");
        PvPRewardEntityListener.outlawBroadcast = loadValue("OutlawBroadcast").replaceAll("&", "§");
        PvPRewardEntityListener.noLongerOutlawBroadcast = loadValue("NoLongerOutlawBroadcast").replaceAll("&", "§");
        PvPRewardEntityListener.karmaDecreased = loadValue("KarmaDecreased").replaceAll("&", "§");
        PvPRewardEntityListener.karmaIncreased = loadValue("KarmaIncreased").replaceAll("&", "§");
        PvPRewardEntityListener.karmaNoChange = loadValue("KarmaNoChange").replaceAll("&", "§");
        PvPRewardEntityListener.deathTollMessage = loadValue("DeathTollMessage").replaceAll("&", "§");
        PvPRewardEntityListener.deathTollType = loadValue("DeathTollType");
        PvPRewardEntityListener.deathToll = Double.parseDouble(loadValue("DeathToll"));
        PvPRewardEntityListener.digGraves = Boolean.parseBoolean(loadValue("DigGraves"));
        PvPRewardPlayerListener.denyTeleMessage = loadValue("DenyTeleMessage").replaceAll("&", "§");
        PvPRewardPlayerListener.denyTele = Boolean.parseBoolean(loadValue("DenyTele"));
        PvPRewardPlayerListener.telePenalty = Integer.parseInt(loadValue("TelePenalty"));
        PvPRewardPlayerListener.penalizeLoggers = Boolean.parseBoolean(loadValue("PenalizeLoggers"));
        Record.combatTimeOut = Integer.parseInt(loadValue("CombatTime"))*1000;
        Record.graveTimeOut = Integer.parseInt(loadValue("GraveTime"))*1000;
        Record.graveRob = loadValue("GraveRobMessage").replaceAll("&", "§");
        outlawTag = loadValue("OutlawTag").replaceAll("&", "§");
        karmaName = loadValue("KarmaName");
        outlawName = loadValue("OutlawName");
        cooldownTime = Integer.parseInt(loadValue("CooldownTime"))*60000;
        Register.economy = loadValue("Economy");
        PluginListener.useOP = Boolean.parseBoolean(loadValue("UseOP"));
        PvPRewardEntityListener.rewardType = loadValue("RewardType");
        PvPRewardEntityListener.percent = Integer.parseInt(loadValue("Percent"));
        PvPRewardEntityListener.amount = Double.parseDouble(loadValue("Amount"));
        PvPRewardEntityListener.hi = Integer.parseInt(loadValue("High"));
        PvPRewardEntityListener.lo = Integer.parseInt(loadValue("Low"));
        PvPRewardEntityListener.threshold = Integer.parseInt(loadValue("KarmaThreshold"));
        PvPRewardEntityListener.modifier = Integer.parseInt(loadValue("OutlawModifier"))/100;
        PvPRewardEntityListener.max = Integer.parseInt(loadValue("ModifierMax"))/100;
        PvPRewardEntityListener.whole = Boolean.parseBoolean(loadValue("WholeNumbers"));
    }

    /**
     * Prints error for missing values
     * 
     */
    private String loadValue(String key) {
        if (!p.containsKey(key)) {
            System.err.println("[PvPReward] Missing value for "+key+" in config file");
            System.err.println("[PvPReward] Please regenerate config file");
        }
        return p.getProperty(key);
    }

    /**
     * Returns true if either player does not have proper permission
     * killer will be checked for "pvpreward.getreward"
     * deaded will be checked for "pvpreward.givereward"
     * 
     * @param killer The Player who would receive the reward
     * @param deaded The Player who the reward would be taken from
     * @return true if either player does not have proper permission
     */
    public static boolean hasPermisson(Player killer, Player deaded) {
        if (permissions != null)
            return permissions.has(killer, "pvpreward.getreward") && permissions.has(deaded, "pvpreward.givereward");
        else
            return true;
    }
    
    /**
     * Returns true the given player has the given Permission node
     * 
     * @param player The Player who would receive the reward
     * @param node The String of the node
     * @return true if either player does not have proper permission
     */
    public static boolean hasPermisson(Player player, String node) {
        if (permissions != null)
            return permissions.has(player, "pvpreward.ignoredeathtoll");
        else
            return false;
    }

    /**
     * Replaces specific values in the given prefix
     * This method also adds colors
     * 
     * @param name The name of the Player
     * @return The modified prefix
     */
    protected static String getPrefix(String name) {
        return outlawTag.replaceAll("<name>", name).replaceAll("&", "§");
    }
    
    /**
     * Subtracts 1 karma from all online Players' Records
     * This is repeated after a given amount of time
     */
    private static void cooldown() {
        //Start a new thread
        Thread cooldown = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.currentThread().sleep(cooldownTime);
                        LinkedList<Record> records = SaveSystem.getRecords();
                        for (Record record : records) {
                            Player player = server.getPlayer(record.player);
                            if (player != null) {
                                record.karma--;
                                if (record.karma == PvPRewardEntityListener.amount) {
                                    player.setDisplayName(player.getName());
                                    PvPReward.server.broadcastMessage(PvPRewardEntityListener.getMsg(
                                            PvPRewardEntityListener.noLongerOutlawBroadcast,
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
}
