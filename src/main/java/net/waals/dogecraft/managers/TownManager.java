package net.waals.dogecraft.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.*;
import net.waals.dogecraft.util.AreaUtil;
import net.waals.dogecraft.util.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;

public class TownManager {

    private ArrayList<Town> towns;
    private ArrayList<Outpost> outposts;
    private AreaUtil areaUtil;
    private DogeCraft plugin;
    private ShopUtil shopUtil;
    private ArenaManager arenaManager;

    public TownManager(AreaUtil areaUtil, DogeCraft plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.towns = new ArrayList<Town>();
        this.outposts = new ArrayList<>();
        this.areaUtil = areaUtil;
        this.shopUtil = new ShopUtil(plugin, areaUtil);
        this.shopUtil.start();
    }

    public Town createTown(String name, Area startingArea, Player founder) {
        ArrayList<String> members = new ArrayList<>();
        members.add(founder.getUniqueId().toString());
        ArrayList<Area> area = new ArrayList<Area>();
        area.add(startingArea);
        ArrayList<String> requests = new ArrayList<String>();
        ArrayList<Plot> plots = new ArrayList<Plot>();
        EnumSet<Town.Flag> flags = EnumSet.of(Town.Flag.EXPLOSION, Town.Flag.MOB_SPAWN, Town.Flag.PVP);
        Town town = new Town(name, null, members, area, plots, requests, flags, 0, null);
        towns.add(town);
        founder.sendMessage(">> Your town has been created!");
        return town;
    }

    public Outpost createOutpost(String dogePlayer, Area area) {
        Outpost outpost = null;
        outpost = new Outpost(dogePlayer, area);
        outposts.add(outpost);
        return outpost;
    }

    public ArrayList<Town> getTowns() {
        return this.towns;
    }

    public Town getTown(String name) {
        Town town = null;
        for(Town currentTown : this.towns) {
            if(currentTown.getName().equalsIgnoreCase(name)) {
                town = currentTown;
                break;
            }
        }
        return town;
    }

    public void store() {
        try (FileWriter file = new FileWriter("towns.json")) {
            Gson gson = new Gson();
            String townList = gson.toJson(this.towns);
            file.write(townList);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        File file = new File("towns.json");
        if(file.exists()) {
            try {
                String townList = new String(Files.readAllBytes(Paths.get("towns.json")));
                if(!townList.isEmpty()) {
                    Gson gson = new Gson();
                    Type townListType = new TypeToken<ArrayList<Town>>(){}.getType();
                    this.towns = gson.fromJson(townList, townListType);
                }

                //System.out.println(towns.get(0).getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(Town currentTown : towns) {
            for(Arena arena : currentTown.getArenas()) {
                arenaManager.updateSign(arena);
            }
            DynmapAPI dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");
            MarkerSet m = dynmap.getMarkerAPI().createMarkerSet("town." + currentTown.getName(), "towns", dynmap.getMarkerAPI().getMarkerIcons(), false);
            int i = 0;
            for(Area area : currentTown.getAreas()) {
                String markerid = currentTown.getName() + "_" + i;
                AreaMarker am = m.createAreaMarker(markerid, currentTown.getName(), false, Bukkit.getWorld("world").getName(), new double[1000], new double[1000], false);
                double[] d1 = {area.getP1().getX(), area.getP2().getX()};
                double[] d2 = {area.getP1().getZ(), area.getP2().getZ()};
                am.setCornerLocations(d1, d2);
                if(i == 0) {
                    am.setLabel(currentTown.getName());
                }
                i++;
            }
        }
    }

    public boolean inTown(String name, Point point) {
        boolean in = false;
        Town town = this.getTown(name);
        if(town != null) {
            for(Area area : town.getAreas()) {
                if(areaUtil.inArea(area, point)) {
                    in = true;
                    break;
                }
            }
        }
        return in;
    }

    public boolean inTown(String name, Area area) {
        boolean in = false;
        in = this.inTown(name, area.getP1()) && this.inTown(name, area.getP2());
        return in;
    }

    public boolean checkIfFree(Area area) {
        boolean free = true;
        for(Town currentTown : towns) {
            for(Area currentArea : currentTown.getAreas()) {
                free = !areaUtil.overlapping(currentArea, area);
                if(!free) break;
            }
            if(!free) break;
        }
        return free;
    }

    public ArrayList<Plot> getPlotsOwnedByDogePlayer(DogePlayer dogePlayer) {
        ArrayList<Plot> plots = new ArrayList<>();
        Town town = getTown(dogePlayer.getTown());
        for(Plot currentPlot : town.getPlots()) {
            if(currentPlot.getOwner() != null) {
                if (currentPlot.getOwner().equalsIgnoreCase(dogePlayer.getUuid())) {
                    plots.add(currentPlot);
                }
            }
        }
        return plots;
    }

    public boolean checkIfRentedByDogePlayer(DogePlayer dogePlayer, Point point) {
        boolean owned = false;
        for(Plot currentPlot : getPlotsOwnedByDogePlayer(dogePlayer)) {
            if(areaUtil.inArea(currentPlot.getArea(), point)) {
                owned = true;
                break;
            }
        }
        return owned;
    }

    public boolean checkIfExtensionAllowed(String townName, Area area) {
        boolean allowed = false;
        Town town = this.getTown(townName);
        for(Area currentArea : town.getAreas()) {
            allowed = areaUtil.overlapping(currentArea, area);
            if(allowed) break;
        }
        for(Town currentTown : towns) {
            if(!currentTown.getName().equalsIgnoreCase(townName)) {
                for(Area currentArea : currentTown.getAreas()) {
                    allowed = !areaUtil.overlapping(currentArea, area);
                    if(!allowed) break;
                }
            }
            if(!allowed) break;
        }
        return allowed;
    }

    public Plot getPlotFromPoint(Point point) {
        Plot plot = null;
        for(Town currentTown : towns) {
            boolean stop = false;
            for(Plot currentPlot : currentTown.getPlots()) {
                if(areaUtil.inArea(currentPlot.getArea(), point)) {
                    plot = currentPlot;
                    stop = true;
                }
            }
            if(stop) break;
        }
        return plot;
    }

    public Shop getShopFromPoint(Point point) {
        Shop shop = null;
        for(Town currentTown : towns) {
            boolean stop = false;
            for(Plot currentPlot : currentTown.getPlots()) {
                for(Shop currentShop : currentPlot.getShops()) {
                    if(currentShop.getLocation().isEqual(point)) {
                        shop = currentShop;
                        stop = true;
                    }
                }
                if(stop) break;
            }
            if(stop) break;
        }
        return shop;
    }

    public boolean checkIfPlotAllowed(Area area, String townName) {
        boolean allowed = true;
        Town town = this.getTown(townName);
        if(this.inTown(townName, area)) {
            if(town != null) {
                for(Plot currentPlot : town.getPlots()) {
                    if(areaUtil.overlapping(currentPlot.getArea(), area)) {
                        allowed = false;
                        break;
                    }
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

    public void checkName(String name) throws Exception {
        if(name.length() > 16) {
            throw new Exception("The name must not be longer than 16 characters.");
        }
        for (int i = 0; i < name.length(); i++) {
            if ((Character.isLetter(name.charAt(i)) == false)) {
                throw new Exception("The name must only contain letters");
            }
        }
        for(Town currentTown : towns) {
            if (currentTown.getName().equalsIgnoreCase(name)) {
                throw new Exception("This name already exists.");
            }
        }
    }

    public void createPlot(String townName, String uuid, Area area, double rent, Point signLocation) {
        Plot plot = new Plot(null, area, signLocation, rent, new ArrayList<Shop>());
        Town town = this.getTown(townName);
        if(town != null) {
            town.addPlot(plot);
        }
    }

    public void removePlot(String townName, Plot plot) {
        Town town = this.getTown(townName);
        town.removePlot(plot);
    }

    public Shop getShopFromPointAndPlot(Plot plot, Point point) {
        Shop shop = null;
        for(Shop currentShop : plot.getShops()) {
            if(currentShop.getLocation().isEqual(point)) {
                shop = currentShop;
                break;
            }
        }
        return shop;
    }

    public void leaveTown(DogePlayer dogePlayer) {
        Town town = getTown(dogePlayer.getTown());
        if(town.getMembers().contains(dogePlayer)) {
            town.getMembers().remove(dogePlayer);
        }
        for(Plot currentPlot : town.getPlots()) {
            if(currentPlot.getOwner() != null) {
                if(currentPlot.getOwner().equalsIgnoreCase(dogePlayer.getUuid())) {
                    clearPlot(currentPlot);
                }
            }
        }
        dogePlayer.setTown(null);
        dogePlayer.setRole(null);
    }

    public ShopUtil getShopUtil() {
        return shopUtil;
    }

    public void clearPlot(Plot plot) {
        plot.setOwner(null);
        Location loc = new Location(Bukkit.getWorld("world"), plot.getSignLocation().getX(), plot.getSignLocation().getY(), plot.getSignLocation().getZ());
        Sign sign = (Sign) loc.getBlock().getState();
        sign.setLine(0, ChatColor.DARK_GREEN + "[RENT]");
        sign.setLine(1, plot.getRent() + "" + ChatColor.GOLD + " Ð");
        sign.setLine(2, "Right-click");
        sign.setLine(3, "to rent");
        sign.update();
        for(Shop shop : plot.getShops()) {
            shopUtil.removeShopCosmetics(shop);
        }
        plot.getShops().clear();
    }

    public void spawnAllShopCosmetics() {
        for(Town currentTown : towns) {
            for(Plot currentPlot : currentTown.getPlots()) {
                for(Shop currentShop : currentPlot.getShops()) {
                    shopUtil.addShopCosmetics(currentShop);
                }
            }
        }
    }

    public void highlightTown(Town town, Player player) {
        for(Area a : town.getAreas()) {
            areaUtil.highlightArea(a, player, Color.GREEN);
        }
    }

    public void stopHighlighting(Town town) {
        for(Area a : town.getAreas()) {
            areaUtil.stopHighlight(a);
        }
    }

    public boolean canManipulate(DogePlayer dogePlayer, Point point) {
        boolean can = false;
        Town town = null;
        for(Town currentTown : towns) {
            if(inTown(currentTown.getName(), point)) {
                town = currentTown;
                break;
            }
        }
        if(town != null) {
            Plot plot = getPlotFromPoint(point);
            if(plot != null) {
                if(plot.getOwner().equalsIgnoreCase(dogePlayer.getUuid())) {
                    can = true;
                }
            } else if(dogePlayer.getTown().equalsIgnoreCase(town.getName()) &&
                    (dogePlayer.getRole().equalsIgnoreCase("OWNER") ||
                            dogePlayer.getRole().equalsIgnoreCase("STAFF"))) {
                can = true;
            }
        } else {
            can = true;
        }
        return can;
    }

    public void resolveGames() {
        for(Town currentTown : towns) {
            for(Arena arena : currentTown.getArenas()) {
                arena.getPlayerA().getInventory().setContents(arena.getInvA());
                arena.getPlayerB().getInventory().setContents(arena.getInvB());
                arena.getPlayerA().teleport(areaUtil.pointToLocation(arena.getSignLocation()));
                arena.getPlayerB().teleport(areaUtil.pointToLocation(arena.getSignLocation()));
                arena.getPlayerA().setHealth(20);
                arena.getPlayerB().setHealth(20);
                arena.getPlayerA().setFoodLevel(20);
                arena.getPlayerB().setFoodLevel(20);
            }
        }
    }
}
