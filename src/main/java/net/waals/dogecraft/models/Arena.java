package net.waals.dogecraft.models;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Arena {

    private Area area;
    private Point spawnA;
    private Point spawnB;
    private transient Player playerA;
    private transient Player playerB;
    private Point signLocation;
    private transient ItemStack[] invA;
    private transient ItemStack[] invB;
    private double amount;
    private transient ArrayList<String> queue;
    private transient GameState state;
    private transient int task;

    public Arena(Area area, Point spawnA, Point spawnB, double amount, Point signLocation) {
        this.area = area;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
        this.playerA = null;
        this.playerB = null;
        this.queue = new ArrayList<String>();
        this.amount = amount;
        this.signLocation = signLocation;
    }

    public ItemStack[] getInvA() {
        return invA;
    }

    public void setInvA(ItemStack[] invA) {
        this.invA = invA;
    }

    public ItemStack[] getInvB() {
        return invB;
    }

    public void setInvB(ItemStack[] invB) {
        this.invB = invB;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public enum GameState {
        FIGHTING,
        ENDED,
        PREGAME,
        WAITING
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Point getSpawnA() {
        return spawnA;
    }

    public void setSpawnA(Point spawnA) {
        this.spawnA = spawnA;
    }

    public Point getSpawnB() {
        return spawnB;
    }

    public void setSpawnB(Point spawnB) {
        this.spawnB = spawnB;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public void setPlayerA(Player playerA) {
        this.playerA = playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setPlayerB(Player playerB) {
        this.playerB = playerB;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public GameState getState() {
        if(state == null) {
            this.state = GameState.WAITING;
        }
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Point getSignLocation() {
        return this.signLocation;
    }

    public void setSignLocation(Point signLocation) {
        this.signLocation = signLocation;
    }

    public ArrayList<String> getQueue() {
        if(this.queue == null) {
            this.queue = new ArrayList<>();
        }
        return this.queue;
    }

    public void setQueue(ArrayList<String> queue) {
        this.queue = queue;
    }
}
