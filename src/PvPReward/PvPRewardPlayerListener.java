
package PvPReward;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author Cody
 */
public class PvPRewardPlayerListener extends PlayerListener {
    protected static String denyTeleMessage;
    protected static int telePenalty;
    protected static boolean denyTele;
    protected static boolean penalizeLoggers;

    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        if (split[0].equals("/pvp")) {
            event.setCancelled(true);
            try {
                if (split[1].startsWith(PvPReward.outlawName)) {
                    player.sendMessage("§eCurrent "+PvPReward.outlawName+"s: ");
                    LinkedList<Record> records = SaveSystem.getRecords();
                    for (Record record : records) {
                        if (record.karma > PvPRewardEntityListener.amount)
                            player.sendMessage("§2"+record.player);
                    }
                }
                else if (split[1].equals(PvPReward.karmaName)) {
                    Record record = SaveSystem.findRecord(player.getName());
                    player.sendMessage("§2Current "+PvPReward.karmaName+" level:§b "+record.karma);
                    player.sendMessage("§2"+PvPReward.outlawName+" status at §b"+ (int)PvPRewardEntityListener.amount);
                }
                else if (split[1].equals("kdr")) {
                    Record record = SaveSystem.findRecord(player.getName());
                    player.sendMessage("§2Current Kills:§b "+record.kills);
                    player.sendMessage("§2Current Deaths:§b "+record.deaths);
                    player.sendMessage("§2Current KDR:§b "+record.kdr);
                }
                else if (split[1].equals("rank")) {
                    int rank = 1;
                    double kdr = SaveSystem.findRecord(player.getName()).kdr;
                    LinkedList<Record> records = SaveSystem.getRecords();
                    for (Record record : records) {
                        if (record.kdr > kdr)
                            rank++;
                    }
                    player.sendMessage("§2Current Rank:§b "+rank);
                }
                else if (split[1].equals("top")) {
                    LinkedList<Record> records = SaveSystem.getRecords();
                    Record one = records.getFirst();
                    for (Record record : records) {
                        if (record.kdr > one.kdr)
                            one = record;
                    }
                    Record two = records.getFirst();
                    for (Record record : records) {
                        if (record.kdr > two.kdr && record.kdr < one.kdr)
                            two = record;
                    }
                    Record three = records.getFirst();
                    for (Record record : records) {
                        if (record.kdr > three.kdr && record.kdr < two.kdr)
                                three = record;
                    }
                    Record four = records.getFirst();
                    for (Record record : records) {
                        if (record.kdr > four.kdr && record.kdr < three.kdr)
                            four = record;
                    }
                    Record five = records.getFirst();
                    for (Record record : records) {
                        if (record.kdr > five.kdr && record.kdr < four.kdr)
                            five = record;
                    }
                    player.sendMessage("§eTop Five KDRs:");
                    player.sendMessage("§2"+one.player+":§b "+one.kdr);
                    player.sendMessage("§2"+two.player+":§b "+two.kdr);
                    player.sendMessage("§2"+three.player+":§b "+three.kdr);
                    player.sendMessage("§2"+four.player+":§b "+four.kdr);
                    player.sendMessage("§2"+five.player+":§b "+five.kdr);
                }
                else
                    throw new Exception();
            }
            catch (Exception e) {
                player.sendMessage("§e  PvPReward Help Page:");
                if (PvPRewardEntityListener.rewardType.equalsIgnoreCase("karma")) {
                    player.sendMessage("§2/pvp "+PvPReward.outlawName+"s§b List current "+PvPReward.outlawName+"s");
                    player.sendMessage("§2/pvp "+PvPReward.karmaName+"§b List current "+PvPReward.karmaName+" level");
                }
                player.sendMessage("§2/pvp kdr§b List current KDR");
                player.sendMessage("§2/pvp rank§b List current rank");
                player.sendMessage("§2/pvp top§b List top 5 KDRs");
            }
        }
        else
            return;
    }

    @Override
    public void onPlayerJoin (PlayerJoinEvent event) {
        if (PvPRewardEntityListener.rewardType.equalsIgnoreCase("karma")) {
            Player player = event.getPlayer();
            LinkedList<Record> records = SaveSystem.getRecords();
            for (Record record : records) {
                if (record.player.equals(player.getName()))
                    if (record.karma > PvPRewardEntityListener.amount)
                        player.setDisplayName(PvPReward.getPrefix(player.getName()));
            }
        }
    }

    @Override
    public void onPlayerTeleport (PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        LinkedList<Record> records = SaveSystem.getRecords();
        for (Record record : records) {
            if (record.player.equals(player.getName())) {
                if (!record.getKiller().equals("") && denyTele) {
                    player.sendMessage(denyTeleMessage);
                    event.setCancelled(true);
                    player.damage(telePenalty);
                }
                return;
            }
        }
    }

    @Override
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player quiter = event.getPlayer();
        LinkedList<Record> records = SaveSystem.getRecords();
        for (Record record : records) {
            if (record.player.equals(quiter.getName())) {
                if (!record.getKiller().equals("") && penalizeLoggers)
                    PvPRewardEntityListener.rewardPvP(quiter, record);
                return;
            }
        }
    }
    
    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Checks if Action was clicking a Block
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (block.getType().equals(Material.SIGN_POST)) {
                Sign sign = (Sign)block.getState();
                Record record = SaveSystem.findRecord(sign.getLine(2));
                if (record != null && record.signLocation != null)
                    if (block.getLocation().equals(record.signLocation))
                        record.robGrave(player);
            }
        }
    }
}
