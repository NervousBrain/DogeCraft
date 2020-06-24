package net.waals.dogecraft.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.waals.dogecraft.util.AreaUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

//Refactor with extending classes
public class Claim {

    private Type type;
    private Stage stage;
    private Player player;
    private Inventory inventory;
    private Area area;
    private Point p1;
    private Point p2;
    private String name;
    private double rent;
    private double price;
    private double wager;
    private double sellPrice;
    private double buyPrice;
    private ShopItem item;
    private Point shopLocation;
    private Point spawnA;
    private Point spawnB;

    public Claim(Type type, Player player, double price) {
        this.type = type;
        this.player = player;
        if (type == Type.TOWN_CREATION) {
            this.stage = Stage.NAMING;
        }
        if (type == Type.PLOT_CREATION) {
            this.stage = Stage.LAND_CLAIM;
        }
        this.inventory = player.getInventory();
        this.area = new Area(null, null);
        this.rent = 0.0;
        this.shopLocation = null;
        this.price = price;
        this.spawnA = null;
        this.spawnB = null;
    }

    public ShopItem getItem() {
        return item;
    }

    public void setItem(ShopItem item) {
        this.item = item;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Point getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(Point shopLocation) {
        this.shopLocation = shopLocation;
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

    public double getWager() {
        return wager;
    }

    public void setWager(double wager) {
        this.wager = wager;
    }

    public enum Type {
        TOWN_CREATION,
        PLOT_CREATION,
        TOWN_EXTENSION,
        SHOP_CREATION,
        OUTPOST_CREATION,
        ARENA_CREATION
    }

    public enum Stage {
        NAMING,
        NAMING_VERIFICATION,
        LAND_CLAIM,
        LAND_CLAIM_VERIFICAITON,
        SET_WAGER,
        SIGN_PLACEMENT,
        SET_RENT,
        SET_BUY_PRICE,
        SET_SELL_PRICE,
        SET_CHEST,
        SET_SPAWN_A,
        SET_SPAWN_B
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (type == Type.PLOT_CREATION) {
            switch (stage) {
                case LAND_CLAIM:
                    player.sendMessage(ChatColor.GOLD + ">> Select the plot area by clicking on two blocks defining it. You can type abort at any time to abort");
                    this.p1 = null;
                    this.p2 = null;
                    this.area = null;
                    break;
                case SET_RENT:
                    player.sendMessage(ChatColor.GOLD + ">> Type the daily rent in the chat.");
                    break;
                case SIGN_PLACEMENT:
                    player.sendMessage(ChatColor.GOLD + ">> Select a sign by left-clicking it.");
                    break;
            }
        }
        if (type == Type.TOWN_CREATION) {
            switch (stage) {
                case NAMING:
                    TextComponent cm = new TextComponent(">> How should your town be called? You can type abort at any time to abort");
                    player.spigot().sendMessage(cm);
                    break;
                case NAMING_VERIFICATION:
                    player.sendMessage(ChatColor.GOLD + ">> Are you sure? If so: type confirm in the chat");
                    break;
                case LAND_CLAIM:
                    player.sendMessage(ChatColor.GOLD + ">> Select you're starting Area by clicking on two blocks defining it");
                    this.p1 = null;
                    this.p2 = null;
                    this.area = null;
            }
        }
        if(type == Type.TOWN_EXTENSION) {
            switch (stage) {
                case LAND_CLAIM:
                    player.sendMessage(ChatColor.GOLD + ">> Select the area to expand upon by clicking on two blocks defining it. You can type abort at any time to abort");
                    this.p1 = null;
                    this.p2 = null;
                    this.area = null;
                    break;
            }
        }
        if(type == Type.SHOP_CREATION) {
            switch (stage) {
                case SET_CHEST:
                    player.sendMessage(ChatColor.GOLD + ">> Punch to chest you want to use as a chest, with the Item stack you want to sell");
                    break;
                case SET_SELL_PRICE:
                    player.sendMessage(ChatColor.GOLD + ">> Type the price to sell the item for in chat. (Set to 0 to disable selling)");
                    break;
                case SET_BUY_PRICE:
                    player.sendMessage(ChatColor.GOLD + ">> Type the price to buy the item for in chat. (Set to 0 to disable buying)");
                    break;
            }
        }
        if(type == Type.OUTPOST_CREATION) {
            switch (stage) {
                case LAND_CLAIM:
                    player.sendMessage(ChatColor.GOLD + ">> Select Area by clicking on two blocks defining it");
                    break;
                case NAMING_VERIFICATION:
                    player.sendMessage(ChatColor.GOLD + ">> Are you sure? If so: type confirm in the chat. Otherwise type redo.");
                    break;
            }
        }
        if(type == Type.ARENA_CREATION) {
            switch (stage) {
                case LAND_CLAIM:
                    player.sendMessage(ChatColor.GOLD + ">> Select the Arena by clicking on two blocks defining it");
                    break;
                case LAND_CLAIM_VERIFICAITON:
                    player.sendMessage(ChatColor.GOLD + ">> Are you sure? If so: type confirm in the chat. Otherwise type redo.");
                    break;
                case SET_SPAWN_A:
                    player.sendMessage(ChatColor.GOLD + ">> Select the first spawn.");
                    break;
                case SET_SPAWN_B:
                    player.sendMessage(ChatColor.GOLD + ">> Select the second spawn.");
                    break;
                case SIGN_PLACEMENT:
                    player.sendMessage(ChatColor.GOLD + ">> Select the a sign.");
                    break;
                case SET_WAGER:
                    player.sendMessage(ChatColor.GOLD + ">> Type the wager in chat.");
                    break;
            }
        }
    }

    public Stage getStage() {
        return this.stage;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Area getArea() {
        return this.area;
    }

    public void setP1(Point point) {
        this.p1 = point;
    }

    public void setP2(Point point) {
        this.p2 = point;
    }

    public Point getP1() {
        return this.p1;
    }

    public Point getP2() {
        return this.p2;
    }

    public void setArea() {
        this.area = new Area(this.p1, this.p2);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return this.type;
    }

    public void setChestLocation(Point point) {
        this.shopLocation = point;
    }

    public double getRent() {
        return this.rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }
}
