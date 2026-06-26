package gg.nexify.minecraft.listener;

import gg.nexify.minecraft.NexifyPlugin;
import gg.nexify.minecraft.delivery.DeliveryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final NexifyPlugin plugin;
    private final DeliveryManager deliveryManager;

    public PlayerJoinListener(NexifyPlugin plugin, DeliveryManager deliveryManager) {
        this.plugin = plugin;
        this.deliveryManager = deliveryManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 1-second delay so the player is fully loaded before receiving items
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> deliveryManager.onPlayerJoin(event.getPlayer()),
                20L
        );
    }
}
