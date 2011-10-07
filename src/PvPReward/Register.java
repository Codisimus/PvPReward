
package PvPReward;

import com.codisimus.pvpreward.register.payment.Method;
import com.codisimus.pvpreward.register.payment.Method.MethodAccount;
import org.bukkit.entity.Player;

/**
 *
 * @author Codisimus
 */
public class Register {
    protected static String economy;
    protected static Method econ;

    /**
     * Returns the double of the percent of money of a players account balance
     * 
     * @param percent The percentage that will be the reward
     * @param deaded The player whose balance will be checked
     * @return the double of the percent of money of a players account balance
     */
    protected static double getPercentMoney(Player deaded, double percent) {
        return econ.getAccount(deaded.getName()).balance() * percent;
    }

    /**
     * Subtracts a specific amount from the players total balance
     * 
     * @param deaded The player who was killed and gets money taken from them
     * @param amount The amount which will be taken
     * @return true if the transaction was successful
     */
    protected static boolean takeMoney(Player deaded, double amount) {
        if (amount == 0)
            return false;
        MethodAccount account = econ.getAccount(deaded.getName());
        if (!account.hasEnough(amount))
            return false;
        account.subtract(amount);
        return true;
    }

    /**
     * Adds money to the players balance and returns the currency name
     * 
     * @param killer The player who is getting money added to their account
     * @param reward The amount that will be added to the account
     */
    protected static void giveMoney(Player killer, double reward) {
        MethodAccount account = econ.getAccount(killer.getName());
        account.add(reward);
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    protected static String format(double amount) {
        return econ.format(amount);
    }
}
