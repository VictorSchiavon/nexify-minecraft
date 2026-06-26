package gg.nexify.minecraft.handler;

import gg.nexify.minecraft.NexifyPlugin;
import gg.nexify.minecraft.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemHandler implements CommandHandler {

    private final NexifyPlugin plugin;
    private final boolean give;

    public ItemHandler(NexifyPlugin plugin, boolean give) {
        this.plugin = plugin;
        this.give = give;
    }

    @Override
    public String execute(Player player, String value) {
        // value format: MATERIAL_NAME:amount  (e.g. DIAMOND:5 or DIAMOND_SWORD:1)
        String[] parts = value.split(":", 2);
        String materialName = parts[0].toUpperCase();
        int amount = 1;
        if (parts.length == 2) {
            try { amount = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
        }

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Unknown material in delivery: " + materialName);
            return null;
        }

        if (give) {
            giveItem(player, material, amount);
            return amount + "x " + formatName(materialName);
        } else {
            removeItem(player, material, amount);
            return null;
        }
    }

    private void giveItem(Player player, Material material, int amount) {
        ItemStack stack = new ItemStack(material, amount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);

        if (!leftover.isEmpty() && plugin.getConfig().getBoolean("drop-items-when-full", true)) {
            leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            String prefix = ColorUtil.color(plugin.getConfig().getString("messages.prefix", "&8[&6Nexify&8] "));
            String msg = ColorUtil.color(plugin.getConfig().getString("messages.item-inventory-full",
                    "&cInventário cheio. O item &f{item} &cfoi dropado no chão."));
            player.sendMessage(prefix + msg.replace("{item}", formatName(material.name())));
        }
    }

    private void removeItem(Player player, Material material, int amount) {
        player.getInventory().removeItem(new ItemStack(material, amount));
    }

    private String formatName(String name) {
        return name.replace("_", " ").toLowerCase()
                .substring(0, 1).toUpperCase() +
                name.replace("_", " ").toLowerCase().substring(1);
    }
}
