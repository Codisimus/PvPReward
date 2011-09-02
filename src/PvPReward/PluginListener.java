
package PvPReward;

import org.bukkit.event.server.ServerListener;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    private Methods methods = new Methods();
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (PvPReward.permissions == null && !useOP) {
            Plugin permissions = PvPReward.pm.getPlugin("Permissions");
            if (permissions != null) {
                PvPReward.permissions = ((Permissions)permissions).getHandler();
                System.out.println("[PvPReward] Successfully linked with Permissions!");
            }
        }
        if (Register.economy == null)
            System.err.println("[PvPReward] Config file outdated, Please regenerate");
        else if (!methods.hasMethod()) {
            try {
                methods.setMethod(PvPReward.pm.getPlugin(Register.economy));
                if (methods.hasMethod()) {
                    Register.econ = methods.getMethod();
                    System.out.println("[PvPReward] Successfully linked with "+
                            Register.econ.getName()+" "+Register.econ.getVersion()+"!");
                }
            }
            catch (Exception e) {
            }
        }
    }
}
