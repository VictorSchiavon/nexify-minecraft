package gg.nexify.minecraft.api.model;

import com.google.gson.annotations.SerializedName;

public class NexifyCommand {

    @SerializedName("id")
    private String id;

    @SerializedName("command")
    private String command;

    @SerializedName("command_value")
    private String commandValue;

    @SerializedName("status_payment")
    private String statusPayment;

    public String getId() { return id; }
    public String getCommand() { return command; }
    public String getCommandValue() { return commandValue; }
    public String getStatusPayment() { return statusPayment; }
}
