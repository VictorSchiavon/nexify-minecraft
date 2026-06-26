package gg.nexify.minecraft.handler;

import gg.nexify.minecraft.NexifyPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class MoneyHandler implements CommandHandler {

    private final NexifyPlugin plugin;
    private final boolean deposit;

    public MoneyHandler(NexifyPlugin plugin, boolean deposit) {
        this.plugin = plugin;
        this.deposit = deposit;
    }

    @Override
    public String execute(Player player, String value) {
        Economy economy = plugin.getEconomy();
        if (economy == null) {
            plugin.getLogger().warning("Vault economy not found. Cannot process money command.");
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid money amount in delivery: " + value);
            return null;
        }

        if (deposit) {
            economy.depositPlayer(player, amount);
            return economy.format(amount);
        } else {
            economy.withdrawPlayer(player, amount);
            return null;
        }
    }
}
