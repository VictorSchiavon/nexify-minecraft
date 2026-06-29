package gg.nexify.minecraft.delivery;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gg.nexify.minecraft.NexifyPlugin;
import gg.nexify.minecraft.api.model.DeliveryResponse;
import gg.nexify.minecraft.api.model.NexifyCommand;
import gg.nexify.minecraft.handler.CommandRouter;
import gg.nexify.minecraft.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryManager {

    private final NexifyPlugin plugin;
    private final CommandRouter router;
    private final Gson gson = new Gson();
    private final File queueFile;

    // playerName (lowercase) -> list of pending deliveries
    private final Map<String, List<PendingDelivery>> offlineQueue = new ConcurrentHashMap<>();

    public DeliveryManager(NexifyPlugin plugin, CommandRouter router) {
        this.plugin = plugin;
        this.router = router;
        this.queueFile = new File(plugin.getDataFolder(), "pending_queue.json");
        loadQueue();
    }

    /**
     * Called from the scheduler thread. Processes a single pending delivery from the API.
     * Returns true if there was a delivery to process (caller should loop).
     */
    public boolean processPending(DeliveryResponse response) {
        if (!response.hasPending()) return false;

        String deliveryId = response.getDeliveryId();
        DeliveryResponse.Data data = response.getData();
        if (data == null || data.getVariable() == null) {
            markComplete(deliveryId);
            return true;
        }

        String playerName = data.getBuyer();
        List<NexifyCommand> commands = data.getVariable().getCommands();

        if (commands == null || commands.isEmpty()) {
            markComplete(deliveryId);
            return true;
        }

        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> deliverToPlayer(player, deliveryId, commands));
            markComplete(deliveryId);
        } else {
            queueForOffline(playerName, deliveryId, commands);
            markComplete(deliveryId);
        }

        return true;
    }

    /**
     * Called when a player joins. Delivers any queued items for them.
     */
    public void onPlayerJoin(Player player) {
        String key = player.getName().toLowerCase();
        List<PendingDelivery> pending = offlineQueue.remove(key);
        if (pending == null || pending.isEmpty()) return;

        String prefix = ColorUtil.color(plugin.getConfig().getString("messages.prefix", "&8[&6Nexify&8] "));
        String pendingMsg = ColorUtil.color(plugin.getConfig().getString("messages.delivery-pending",
                "&eVocê tem &f{count} &eproduto(s) pendente(s). Entregando..."));

        player.sendMessage(prefix + pendingMsg.replace("{count}", String.valueOf(pending.size())));

        for (PendingDelivery delivery : pending) {
            deliverToPlayer(player, delivery.getDeliveryId(), delivery.getCommands());
        }

        saveQueue();
    }

    private void deliverToPlayer(Player player, String deliveryId, List<NexifyCommand> commands) {
        List<String> delivered = new ArrayList<>();
        for (NexifyCommand cmd : commands) {
            String label = router.execute(player, cmd);
            if (label != null) delivered.add(label);
        }

        if (!delivered.isEmpty()) {
            String prefix = ColorUtil.color(plugin.getConfig().getString("messages.prefix", "&8[&6Nexify&8] "));
            String successMsg = ColorUtil.color(plugin.getConfig().getString("messages.delivery-success",
                    "&aVocê recebeu um produto: &f{product}"));
            String productList = String.join(", ", delivered);
            player.sendMessage(prefix + successMsg.replace("{product}", productList));
        }
    }

    private void queueForOffline(String playerName, String deliveryId, List<NexifyCommand> commands) {
        String key = playerName.toLowerCase();
        offlineQueue.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new PendingDelivery(deliveryId, playerName, commands));
        saveQueue();
    }

    private void markComplete(String deliveryId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getApi().markComplete(deliveryId);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to mark delivery " + deliveryId + " as complete: " + e.getMessage());
            }
        });
    }

    private void saveQueue() {
        try {
            plugin.getDataFolder().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(queueFile), StandardCharsets.UTF_8)) {
                gson.toJson(offlineQueue, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save offline queue: " + e.getMessage());
        }
    }

    private void loadQueue() {
        if (!queueFile.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(queueFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, List<PendingDelivery>>>(){}.getType();
            Map<String, List<PendingDelivery>> saved = gson.fromJson(reader, type);
            if (saved != null) offlineQueue.putAll(saved);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load offline queue: " + e.getMessage());
        }
    }
}
