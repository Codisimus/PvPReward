
package PvPReward;

import java.util.Random;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class PvPRewardEntityListener extends EntityListener {
    protected static String killedMessage;
    protected static String killerMessage;
    protected static String killedNotEnoughMoney;
    protected static String killerNotEnoughMoney;
    protected static String outlawBroadcast;
    protected static String noLongerOutlawBroadcast;
    protected static String karmaDecreased;
    protected static String karmaIncreased;
    protected static String karmaNoChange;
    protected static String deathTollType;
    protected static String deathTollMessage;
    protected static double deathToll;
    protected static boolean digGraves;
    protected static String rewardType;
    protected static int percent;
    protected static double amount;
    protected static int threshold;
    protected static int modifier;
    protected static int max;
    protected static int hi;
    protected static int lo;
    protected static boolean whole;

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player wounded = (Player)event.getEntity();
            Record record = SaveSystem.findRecord(wounded.getName());
            record.updateWhereabouts(wounded);
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
                entity = sub.getDamager();
                if (entity instanceof Player) {
                    Player attacker = (Player)entity;
                    if (!event.isCancelled())
                        record.setAttacker(attacker.getName());
                }
            }
        }
    }

    @Override
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player deaded = (Player)event.getEntity();
            Record deadedRecord = SaveSystem.findRecord(deaded.getName());
            if (digGraves)
                deadedRecord.digGrave(event.getDrops());
            dropMoney(deaded);
            rewardPvP(deaded, deadedRecord);
        }
    }
    
    /**
     * Death toll is taken from the killed Player
     * 
     * @param deaded The player who was killed
     */
    private static void dropMoney(Player deaded) {
        if (deathTollType.equalsIgnoreCase("none") || deathToll == 0)
            return;
        if (PvPReward.hasPermisson(deaded, "ignoredeathtoll"))
            return;
        double dropped = deathToll;
        if (deathTollType.equalsIgnoreCase("Percent"))
            dropped = Register.getPercentMoney(deaded, deathToll/100);
        dropped = trim(dropped);
        Register.takeMoney(deaded, dropped);
        deaded.sendMessage(getMsg(deathTollMessage, dropped, "", "", ""));
    }

    /**
     * Gives killer money from killed based on the set reward type
     * killer is found by looking at the killed Players record
     * 
     * @param deaded The player who was killed
     */
    protected static void rewardPvP(Player deaded, Record deadedRecord) {
        String attacker = deadedRecord.getKiller();
        if (attacker.equals(deadedRecord.player))
            return;
        deadedRecord.setAttacker("");
        if (attacker.equals(""))
            return;
        Player killer = PvPReward.server.getPlayer(attacker);
        if (PvPReward.permissions != null && !PvPReward.hasPermisson(killer, deaded))
            return;
        Record killerRecord = SaveSystem.findRecord(killer.getName());
        double deadedKDR = deadedRecord.addDeath();
        double killerKDR = killerRecord.addKill();
        Random random = new Random();
        double bonus = 0;
        double reward = amount;
        if (rewardType.equalsIgnoreCase("PercentKDR"))
            reward = Register.getPercentMoney(deaded, (deadedKDR/killerKDR)/100);
        else if (rewardType.equalsIgnoreCase("Percent"))
            reward = Register.getPercentMoney(deaded, percent/100);
        else if (rewardType.equalsIgnoreCase("PercentRange") || rewardType.equalsIgnoreCase("Karma")) {
            if (rewardType.equalsIgnoreCase("Karma")) {
                int deadedKarma = deadedRecord.karma;
                if (deaded.isOnline())
                    deaded.sendMessage(getMsg(karmaDecreased, 1, deadedRecord.player, killerRecord.player, deadedKarma+""));
                if (deadedKarma == amount) {
                    if (deaded.isOnline())
                        deaded.setDisplayName(deadedRecord.player);
                    PvPReward.server.broadcastMessage(getMsg(noLongerOutlawBroadcast, 1, deadedRecord.player, killerRecord.player, deadedKarma+""));
                }
                int killerKarma = killerRecord.karma;
                int percentOfSteal;
                if (deadedKarma > amount) {
                    percentOfSteal = 100;
                    killerRecord.karma = killerRecord.karma-2;
                    SaveSystem.save();
                    killer.sendMessage(getMsg(karmaNoChange, 0, deadedRecord.player, killerRecord.player, killerKarma+""));
                }
                else {
                    percentOfSteal = (int)percent + deadedKarma;
                    killer.sendMessage(getMsg(karmaIncreased, 2, deadedRecord.player, killerRecord.player, killerKarma+""));
                    if (killerKarma == amount + 1 || killerKarma == amount + 2) {
                        killer.setDisplayName(PvPReward.getPrefix(killerRecord.player));
                        PvPReward.server.broadcastMessage(getMsg(outlawBroadcast, 2, deadedRecord.player, killerRecord.player, killerKarma+""));
                    }
                }
                int roll = random.nextInt(100);
                    if (roll >= percentOfSteal)
                        return;
                int multiplier = (int)((killerKarma - amount) / threshold);
                bonus = multiplier * (modifier);
                if (bonus > 0)
                    if (bonus > max)
                        bonus = max;
                else if (bonus < 0)
                    if (bonus < max)
                        bonus = max;
            }
            double rewardPercent = random.nextInt((hi + 1) - lo);
            rewardPercent = (rewardPercent + lo) / 100;
            reward = Register.getPercentMoney(deaded, rewardPercent);
            reward = reward + (reward * bonus);
        }
        else if (rewardType.equalsIgnoreCase("Range")) {
            reward = random.nextInt((hi + 1) - lo);
            reward = reward + lo;
        }
        reward = trim(reward);
        if (!Register.takeMoney(deaded, reward)) {
            if (deaded.isOnline())
                deaded.sendMessage(getMsg(killedNotEnoughMoney, reward, deadedRecord.player, killerRecord.player, "karma not available"));
            killer.sendMessage(getMsg(killerNotEnoughMoney, reward, deadedRecord.player, killerRecord.player, "karma not available"));
            return;
        }
        Register.giveMoney(killer, reward);
        if (deaded.isOnline())
            deaded.sendMessage(getMsg(killedMessage, reward, deadedRecord.player, killerRecord.player, "karma not available"));
        killer.sendMessage(getMsg(killerMessage, reward, deadedRecord.player, killerRecord.player, "karma not available"));
    }
    
    /**
     * Replaces specific values in the given message
     * 
     * @param msg The message that will be modified
     * @param killed the name of the killed player
     * @param killed the name of the killer
     * @param killed the amount of karma
     * @return The modified message
     */
    protected static String getMsg(String msg, double amount, String killed, String killer, String karma) {
        msg = msg.replaceAll("<killed>", killed);
        msg = msg.replaceAll("<killer>", killer);
        msg = msg.replaceAll("<karma>", karma);
        msg = msg.replaceAll("<amount>", Register.format(amount).replace(".00", ""));
        return msg;
    }
    
    /**
     * Trims the money amount down
     * Cast to int if WholeNumbers is set to true in the config
     * Gets rid of all but two places after the decimal
     * 
     * @param money The double value that will be trimmed
     * @return The double value that has been trimmed
     */
    private static double trim(double money) {
        if (whole)
            return (int)money;
        return ((long)(money*100))/100;
    }
}