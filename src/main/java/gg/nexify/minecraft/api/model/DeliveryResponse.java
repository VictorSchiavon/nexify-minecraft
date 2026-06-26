package gg.nexify.minecraft.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeliveryResponse {

    @SerializedName("hasPending")
    private boolean hasPending;

    @SerializedName("deliveryId")
    private String deliveryId;

    @SerializedName("data")
    private Data data;

    public boolean hasPending() { return hasPending; }
    public String getDeliveryId() { return deliveryId; }
    public Data getData() { return data; }

    public static class Data {
        @SerializedName("transaction_reference")
        private String transactionReference;

        @SerializedName("buyer")
        private String buyer;

        @SerializedName("status_payment")
        private String statusPayment;

        @SerializedName("variable")
        private Variable variable;

        public String getBuyer() { return buyer; }
        public String getTransactionReference() { return transactionReference; }
        public Variable getVariable() { return variable; }
    }

    public static class Variable {
        @SerializedName("variable_reference")
        private String variableReference;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("commands")
        private List<NexifyCommand> commands;

        public List<NexifyCommand> getCommands() { return commands; }
        public int getQuantity() { return quantity; }
    }
}
