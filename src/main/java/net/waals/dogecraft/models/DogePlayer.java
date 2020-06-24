package net.waals.dogecraft.models;

import org.bukkit.Location;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DogePlayer {

    private String uuid;
    private String address;
    private double balance;
    private String town;
    private String currentTown;
    private String requested;
    private String role;
    private String name;
    private Location lastDeath;
    private String referredBy;
    private long lastDonation;
    private Arena inArena;
    private boolean fighting;

    public DogePlayer(String uuid, String address, double balance, String town, String requested, String role, String name, String referredBy, /*Date lastDonation*/String lastDonation) {
        this.uuid = uuid;
        this.address = address;
        this.balance = balance;
        this.town = town;
        this.referredBy = referredBy;
        this.currentTown = null;
        this.requested = requested;
        this.role = role;
        this.name = name;
        this.inArena = null;
        this.lastDeath = null;
        this.fighting = false;
        if(lastDonation != null && !lastDonation.equalsIgnoreCase("")) {
            this.lastDonation = Long.valueOf(lastDonation);
        } else {
            this.lastDonation = 0;
        }
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getAddress() {
        return this.address;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("address", this.address);
        data.put("balance", this.balance);
        data.put("town", this.town);
        data.put("requested", this.requested);
        data.put("role", this.role);
        data.put("name", this.name);
        data.put("referredBy", this.referredBy);
        data.put("lastDonation", this.lastDonation + "");
        return data;
    }

    public String getCurrentTown() {
        return this.currentTown;
    }

    public void setCurrentTown(String name) {
        this.currentTown = name;
    }

    public String getTown() {
        return this.town;
    }

    public void setTown(String townName) {
        this.town = townName;
    }

    public void setRequested(String requested) {
        this.requested = requested;
    }

    public String getRequested() {
        return this.requested;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLastDeath() {
        return lastDeath;
    }

    public void setLastDeath(Location lastDeath) {
        this.lastDeath = lastDeath;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public long getLastDonation() {
        return lastDonation;
    }

    public void setLastDonation(long lastDonation) {
        this.lastDonation = lastDonation;
    }

    public boolean isDonor() {
        if(lastDonation != 0) {
            Calendar c=Calendar.getInstance();
            c.setTime(new Date(lastDonation));
            c.add(Calendar.DATE,30);
            if(c.getTime().compareTo(new Date())<0){
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public Arena getInArena() {
        return inArena;
    }

    public void setInArena(Arena inArena) {
        this.inArena = inArena;
    }

    public boolean isFighting() {
        return fighting;
    }

    public void setFighting(boolean fighting) {
        this.fighting = fighting;
    }
}
