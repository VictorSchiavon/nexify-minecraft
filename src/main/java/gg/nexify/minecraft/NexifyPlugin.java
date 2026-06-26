package gg.nexify.minecraft;

import gg.nexify.minecraft.api.NexifyAPI;
import gg.nexify.minecraft.delivery.DeliveryManager;
import gg.nexify.minecraft.handler.CommandRouter;
import gg.nexify.minecraft.listener.PlayerJoinListener;
import gg.nexify.minecraft.scheduler.DeliveryScheduler;
import gg.nexify.minecraft.util.ColorUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NexifyPlugin extends JavaPlugin {

    private NexifyAPI api;
    private DeliveryManager deliveryManager;
    private Economy economy;
    private Permission permissions;
    private int schedulerTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String token = getConfig().getString("nexify.api-token", "");
        if (token.isEmpty() || token.equals("YOUR_API_TOKEN_HERE")) {
            getLogger().severe("API token not configured! Set nexify.api-token in config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String baseUrl = getConfig().getString("nexify.api-url", "https://api.nexify.gg");
        api = new NexifyAPI(baseUrl, token);

        setupVault();

        CommandRouter router = new CommandRouter(this);
        deliveryManager = new DeliveryManager(this, router);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, deliveryManager), this);

        startScheduler();

        getLogger().info("Nexify plugin enabled! Polling every " +
                getConfig().getInt("nexify.poll-interval", 30) + "s.");
    }

    @Override
    public void onDisable() {
        if (schedulerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(schedulerTaskId);
        }
        getLogger().info("Nexify plugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("nexify")) return false;

        if (!sender.hasPermission("nexify.admin")) {
            sender.sendMessage(ColorUtil.color("&cVocê não tem permissão."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color("&6Nexify &7v" + getDescription().getVersion()));
            sender.sendMessage(ColorUtil.color("&7/nexify reload &8- Recarregar config"));
            sender.sendMessage(ColorUtil.color("&7/nexify check &8- Verificar entregas agora"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                stopScheduler();
                String token = getConfig().getString("nexify.api-token", "");
                String baseUrl = getConfig().getString("nexify.api-url", "https://api.nexify.gg");
                api = new NexifyAPI(baseUrl, token);
                startScheduler();
                sender.sendMessage(ColorUtil.color("&aConfiguração recarregada!"));
                break;

            case "check":
                sender.sendMessage(ColorUtil.color("&eVerificando entregas pendentes..."));
                Bukkit.getScheduler().runTaskAsynchronously(this,
                        new DeliveryScheduler(this, deliveryManager));
                break;

            case "status":
                sender.sendMessage(ColorUtil.color("&6Nexify Status"));
                sender.sendMessage(ColorUtil.color("&7Economy: " + (economy != null ? "&a" + economy.getName() : "&cNão encontrado")));
                sender.sendMessage(ColorUtil.color("&7Permissions: " + (permissions != null ? "&a" + permissions.getName() : "&cNão encontrado")));
                sender.sendMessage(ColorUtil.color("&7Polling: &a" + getConfig().getInt("nexify.poll-interval", 30) + "s"));
                break;

            default:
                sender.sendMessage(ColorUtil.color("&cComando desconhecido."));
        }

        return true;
    }

    private void startScheduler() {
        long interval = getConfig().getInt("nexify.poll-interval", 30) * 20L;
        schedulerTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this, new DeliveryScheduler(this, deliveryManager), interval, interval).getTaskId();
    }

    private void stopScheduler() {
        if (schedulerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(schedulerTaskId);
            schedulerTaskId = -1;
        }
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault not found. Economy and permission group commands will be unavailable.");
            return;
        }
        RegisteredServiceProvider<Economy> econProvider =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (econProvider != null) economy = econProvider.getProvider();

        RegisteredServiceProvider<Permission> permProvider =
                getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) permissions = permProvider.getProvider();
    }

    public NexifyAPI getApi() { return api; }
    public Economy getEconomy() { return economy; }
    public Permission getPermissions() { return permissions; }
}
