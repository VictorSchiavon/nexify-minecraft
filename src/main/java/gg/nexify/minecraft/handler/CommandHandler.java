package gg.nexify.minecraft.handler;

import org.bukkit.entity.Player;

public interface CommandHandler {
    /**
     * @return human-readable label of what was delivered, or null on failure
     */
    String execute(Player player, String value);
}
