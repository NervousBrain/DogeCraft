package net.waals.dogecraft.models;

import java.util.HashMap;
import java.util.Map;

public class Withdrawal {

    private String user;
    private String toAddress;
    private double amount;

    public Withdrawal(String user, String toAddress, double amount) {
        this.user = user;
        this.toAddress = toAddress;
        this.amount = amount;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("toAddress", this.toAddress);
        data.put("user", this.user);
        data.put("amount", this.amount);
        return data;
    }
}
