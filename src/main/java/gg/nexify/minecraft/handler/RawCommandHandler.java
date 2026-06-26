package gg.nexify.minecraft.handler;

import gg.nexify.minecraft.NexifyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RawCommandHandler implements CommandHandler {

    private final NexifyPlugin plugin;

    public RawCommandHandler(NexifyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String execute(Player player, String value) {
        // Replace {player} placeholder with actual player name
        String command = value.replace("{player}", player.getName());

        // Must run on the main thread
        if (Bukkit.isPrimaryThread()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }

        return command;
    }
}
