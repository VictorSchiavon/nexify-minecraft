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
        ItemHandler addItem    = new ItemHandler(plugin, true);
        ItemHandler removeItem = new ItemHandler(plugin, false);
        MoneyHandler addMoney    = new MoneyHandler(plugin, true);
        MoneyHandler removeMoney = new MoneyHandler(plugin, false);
        GroupHandler addGroup    = new GroupHandler(plugin, GroupHandler.Action.ADD);
        GroupHandler removeGroup = new GroupHandler(plugin, GroupHandler.Action.REMOVE);
        GroupHandler addTempGroup = new GroupHandler(plugin, GroupHandler.Action.ADD_TEMP);
        RawCommandHandler rawCmd = new RawCommandHandler(plugin);

        // minecraft:* namespace
        handlers.put("minecraft:additem",      addItem);
        handlers.put("minecraft:removeitem",   removeItem);
        handlers.put("minecraft:addmoney",     addMoney);
        handlers.put("minecraft:removemoney",  removeMoney);
        handlers.put("minecraft:addgroup",     addGroup);
        handlers.put("minecraft:removegroup",  removeGroup);
        handlers.put("minecraft:addtempgroup", addTempGroup);
        handlers.put("minecraft:command",      rawCmd);

        // fivemarket:* namespace (same store, different game tag)
        handlers.put("fivemarket:additem",      addItem);
        handlers.put("fivemarket:removeitem",   removeItem);
        handlers.put("fivemarket:addmoney",     addMoney);
        handlers.put("fivemarket:removemoney",  removeMoney);
        handlers.put("fivemarket:addgroup",     addGroup);
        handlers.put("fivemarket:removegroup",  removeGroup);
        handlers.put("fivemarket:addtempgroup", addTempGroup);
        handlers.put("fivemarket:command",      rawCmd);
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
