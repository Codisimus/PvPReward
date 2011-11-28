package com.codisimus.plugins.pvpreward.listeners;

import com.codisimus.plugins.pvpreward.PvPReward;
import com.codisimus.plugins.pvpreward.Register;
import org.bukkit.event.server.ServerListener;
import com.codisimus.plugins.pvpreward.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for Permission/Economy plugins whenever a Plugin is enabled
 * 
 * @author Codisimus
 */
public class pluginListener extends ServerListener {
    public static boolean useBP;

    /**
     * Executes methods to look for various types of plugins to link
     *
     * @param event The PluginEnableEvent that occurred
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        linkPermissions();
        linkEconomy();
    }

    /**
     * Finds and links a Permission plugin
     *
     */
    public void linkPermissions() {
        //Return if we have already have a permissions plugin
        if (PvPReward.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!PvPReward.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useBP)
            return;

        PvPReward.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[PvPReward] Successfully linked with PermissionsEx!");
    }

    /**
     * Finds and links an Economy plugin
     *
     */
    public void linkEconomy() {
        //Return if we already have an Economy plugin
        if (Methods.hasMethod())
            return;

        //Return if no Economy is wanted
        if (Register.economy.equalsIgnoreCase("none"))
            return;

        //Set preferred plugin if there is one
        if (!Register.economy.equalsIgnoreCase("auto"))
            Methods.setPreferred(Register.economy);

        //Find an Economy Plugin (will first look for preferred Plugin)
        Methods.setMethod(PvPReward.pm);

        //Return if no Economy Plugin was found
        if (!Methods.hasMethod())
            return;

        //Reset Methods if the preferred Economy was not found
        if (!Methods.getMethod().getName().equalsIgnoreCase(Register.economy) && !Register.economy.equalsIgnoreCase("auto")) {
            Methods.reset();
            return;
        }

        Register.econ = Methods.getMethod();
        System.out.println("[PvPReward] Successfully linked with "+Register.econ.getName()+" "+Register.econ.getVersion()+"!");
    }
}
