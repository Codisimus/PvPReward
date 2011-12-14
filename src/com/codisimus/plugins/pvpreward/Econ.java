package com.codisimus.plugins.pvpreward;

import net.milkbowl.vault.economy.Economy;

/**
 * Manages payment/rewards of using Warps
 * 
 * @author Codisimus
 */
public class Econ {
    public static Economy economy;

    /**
     * Returns the double of the percent of money of a Player's account balance
     * 
     * @param player The name of the Player whose balance will be checked
     * @param percent The percentage that will be the reward
     * @return the double of the percent of money of a Player's account balance
     */
    public static double getPercentMoney(String player, double percent) {
        return economy.getBalance(player) * percent;
    }

    /**
     * Subtracts a specific amount from the Player's total balance
     * 
     * @param player The Player who was killed and gets money taken from them
     * @param amount The amount which will be taken
     * @return true if the transaction was successful
     */
    public static boolean takeMoney(String player, double amount) {
        //Cancel if the amount is 0
        if (amount == 0)
            return false;
        
        //Cancel if the Player can not afford the transaction
        if (!economy.has(player, amount))
            return false;

        economy.withdrawPlayer(player, amount);
        return true;
    }

    /**
     * Adds money to the Player's balance and returns the currency name
     * 
     * @param player The Player who is getting money added to their account
     * @param amount The amount that will be added to the account
     */
    public static void giveMoney(String player, double amount) {
        economy.depositPlayer(player, amount);
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return economy.format(amount).replace(".00", "");
    }
}
