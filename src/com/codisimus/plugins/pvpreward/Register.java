package com.codisimus.plugins.pvpreward;

import com.codisimus.plugins.pvpreward.register.payment.Method;
import com.codisimus.plugins.pvpreward.register.payment.Method.MethodAccount;
import org.bukkit.entity.Player;

/**
 * Manages payment for stealing and dropping money
 * Uses Nijikokun's Register API
 *
 * @author Codisimus
 */
public class Register {
    public static String economy;
    public static Method econ;

    /**
     * Returns the double of the percent of money of a Player's account balance
     * 
     * @param percent The percentage that will be the reward
     * @param deaded The Player whose balance will be checked
     * @return the double of the percent of money of a Player's account balance
     */
    public static double getPercentMoney(Player deaded, double percent) {
        return econ.getAccount(deaded.getName()).balance() * percent;
    }

    /**
     * Subtracts a specific amount from the Player's total balance
     * 
     * @param deaded The Player who was killed and gets money taken from them
     * @param amount The amount which will be taken
     * @return true if the transaction was successful
     */
    public static boolean takeMoney(Player deaded, double amount) {
        //Cancel if the amount is 0
        if (amount == 0)
            return false;

        MethodAccount account = econ.getAccount(deaded.getName());
        
        //Cancel if the Player can not afford the transaction
        if (!account.hasEnough(amount))
            return false;

        account.subtract(amount);
        return true;
    }

    /**
     * Adds money to the Player's balance and returns the currency name
     * 
     * @param killer The Player who is getting money added to their account
     * @param reward The amount that will be added to the account
     */
    public static void giveMoney(Player killer, double reward) {
        MethodAccount account = econ.getAccount(killer.getName());
        account.add(reward);
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return econ.format(amount).replace(".00", "");
    }
}
