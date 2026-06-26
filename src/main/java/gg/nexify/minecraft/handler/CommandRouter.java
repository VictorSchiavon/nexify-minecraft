package gg.nexify.minecraft.handler;

import gg.nexify.minecraft.NexifyPlugin;
import gg.nexify.minecraft.api.model.NexifyCommand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes Nexify commands (e.g. "minecraft:addItem") to the correct handler.
 *
 * Store owners configure commands in the Nexify dashboard.
 * Supported commands:
 *   minecraft:addItem      — give item(s) to player   | value: MATERIAL:amount
 *   minecraft:removeItem   — take item(s) from player | value: MATERIAL:amount
 *   minecraft:addMoney     — deposit economy money     | value: amount  (requires Vault)
 *   minecraft:removeMoney  — withdraw economy money    | value: amount  (requires Vault)
 *   minecraft:addGroup     — add permission group      | value: groupname  (requires Vault)
 *   minecraft:removeGroup  — remove permission group   | value: groupname  (requires Vault)
 *   minecraft:addTempGroup — timed permission group    | value: groupname:days  (requires Vault)
 *   minecraft:command      — run any console command   | value: command (use {player} placeholder)
 */
public class CommandRouter {

    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandRouter(NexifyPlugin plugin) {
        handlers.put("minecraft:additem",      new ItemHandler(plugin, true));
        handlers.put("minecraft:removeitem",   new ItemHandler(plugin, false));
        handlers.put("minecraft:addmoney",     new MoneyHandler(plugin, true));
        handlers.put("minecraft:removemoney",  new MoneyHandler(plugin, false));
        handlers.put("minecraft:addgroup",     new GroupHandler(plugin, GroupHandler.Action.ADD));
        handlers.put("minecraft:removegroup",  new GroupHandler(plugin, GroupHandler.Action.REMOVE));
        handlers.put("minecraft:addtempgroup", new GroupHandler(plugin, GroupHandler.Action.ADD_TEMP));
        handlers.put("minecraft:command",      new RawCommandHandler(plugin));
    }

    /**
     * Executes the command for the given player.
     * @return a human-readable label of what was delivered, or null if nothing happened
     */
    public String execute(Player player, NexifyCommand cmd) {
        String key = cmd.getCommand().toLowerCase();
        CommandHandler handler = handlers.get(key);
        if (handler != null) {
            return handler.execute(player, cmd.getCommandValue());
        }
        // Fallback: treat unknown commands as raw server commands
        return new RawCommandHandler(null).execute(player, cmd.getCommand() + " " + cmd.getCommandValue());
    }
}
