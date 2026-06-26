package gg.nexify.minecraft.delivery;

import gg.nexify.minecraft.api.model.NexifyCommand;

import java.util.List;

public class PendingDelivery {

    private final String deliveryId;
    private final String playerName;
    private final List<NexifyCommand> commands;

    public PendingDelivery(String deliveryId, String playerName, List<NexifyCommand> commands) {
        this.deliveryId = deliveryId;
        this.playerName = playerName;
        this.commands = commands;
    }

    public String getDeliveryId() { return deliveryId; }
    public String getPlayerName() { return playerName; }
    public List<NexifyCommand> getCommands() { return commands; }
}
