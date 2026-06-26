package gg.nexify.minecraft.scheduler;

import gg.nexify.minecraft.NexifyPlugin;
import gg.nexify.minecraft.api.model.DeliveryResponse;
import gg.nexify.minecraft.delivery.DeliveryManager;

public class DeliveryScheduler implements Runnable {

    private final NexifyPlugin plugin;
    private final DeliveryManager deliveryManager;

    public DeliveryScheduler(NexifyPlugin plugin, DeliveryManager deliveryManager) {
        this.plugin = plugin;
        this.deliveryManager = deliveryManager;
    }

    @Override
    public void run() {
        // Drain all pending deliveries in one poll cycle
        int processed = 0;
        try {
            boolean hasMore = true;
            while (hasMore) {
                DeliveryResponse response = plugin.getApi().fetchPending();
                hasMore = deliveryManager.processPending(response);
                if (hasMore) processed++;
                // Safety: avoid infinite loop on API errors
                if (processed > 100) break;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Nexify] Error checking deliveries: " + e.getMessage());
        }

        if (processed > 0) {
            plugin.getLogger().info("[Nexify] Processed " + processed + " delivery(ies).");
        }
    }
}
