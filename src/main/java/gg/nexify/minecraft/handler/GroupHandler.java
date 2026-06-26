package gg.nexify.minecraft.handler;

import gg.nexify.minecraft.NexifyPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

public class GroupHandler implements CommandHandler {

    public enum Action { ADD, REMOVE, ADD_TEMP }

    private final NexifyPlugin plugin;
    private final Action action;

    public GroupHandler(NexifyPlugin plugin, Action action) {
        this.plugin = plugin;
        this.action = action;
    }

    @Override
    public String execute(Player player, String value) {
        Permission perms = plugin.getPermissions();
        if (perms == null) {
            plugin.getLogger().warning("Vault permissions not found. Cannot process group command.");
            return null;
        }

        if (action == Action.ADD_TEMP) {
            // value format: groupname:days
            String[] parts = value.split(":", 2);
            String group = parts[0];
            // Standard Vault doesn't support timed groups; add permanently and log duration
            perms.playerAddGroup(player.getWorld().getName(), player.getName(), group);
            String days = parts.length > 1 ? parts[1] : "?";
            plugin.getLogger().info("Added " + player.getName() + " to group " + group +
                    " (temporary: " + days + " days — manage expiry via your permissions plugin)");
            return "Cargo " + group + " (" + days + " dias)";
        }

        if (action == Action.ADD) {
            perms.playerAddGroup(player.getWorld().getName(), player.getName(), value);
            return "Cargo " + value;
        } else {
            perms.playerRemoveGroup(player.getWorld().getName(), player.getName(), value);
            return null;
        }
    }
}
