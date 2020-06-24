package net.waals.dogecraft.models;

import java.util.ArrayList;
import java.util.EnumSet;

public class Town {

    private String name;
    private ArrayList<Area> areas;
    private ArrayList<Arena> arenas;
    private ArrayList<Plot> plots;
    private ArrayList<String> members;
    private ArrayList<String> requests;
    private EnumSet<Flag> flags;
    private double balance;
    private Point spawn;

    public Town(String name,
                ArrayList<Arena> arenas, ArrayList<String> members, ArrayList<Area> areas,
                ArrayList<Plot> plots, ArrayList<String> requests, EnumSet<Flag> flags,
                double balance,
                Point spawn) {
        this.name = name;
        this.areas = areas;
        this.members = members;
        this.spawn = spawn;
        if(arenas != null) {
            this.arenas = arenas;
        } else {
            this.arenas = new ArrayList<>();
        }
        this.plots = plots;
        this.requests = requests;
        this.flags = flags;
        this.plots = plots;
        this.balance = balance;
    }

    public ArrayList<Arena> getArenas() {
        if(arenas == null) {
            this.arenas = new ArrayList<>();
        }
        return arenas;
    }

    public void setArenas(ArrayList<Arena> arenas) {
        this.arenas = arenas;
    }

    public Point getSpawn() {
        return spawn;
    }

    public void setSpawn(Point spawn) {
        this.spawn = spawn;
    }

    public enum Role {
        OWNER("OWNER"),
        ADMIN("ADMIN"),
        STAFF("STAFF"),
        CITIZEN("CITIZEN");

        private String name;

        Role(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    public enum Flag {
        EXPLOSION, MOB_SPAWN, PVP
    }

    public ArrayList<Area> getAreas() {
        return this.areas;
    }

    public String getName() {
        return this.name;
    }

    public void addRequest(String uuid) {
        this.requests.add(uuid);
    }

    public void removeRequest(String uuid) {
        this.requests.remove(uuid);
    }

    public void acceptRequest(String uuid) {
        if(requests.contains(uuid)) {
            requests.remove(uuid);
            members.add(uuid);
        }
    }

    public ArrayList<String> getMembers() {
        return this.members;
    }

    public ArrayList<String> getRequests() {
        return this.requests;
    }

    public ArrayList<Plot> getPlots() {
        return this.plots;
    }

    public void addPlot(Plot plot) {
        this.plots.add(plot);
    }

    public void removePlot(Plot plot) {
        this.plots.remove(plot);
    }

    public boolean hasFlag(Flag flag) {
        boolean has = false;
        if(flags.contains(flag)) has = true;
        return has;
    }

    public void toggleFlag(Flag flag) {
        if(hasFlag(flag)) {
            flags.remove(flag);
        } else {
            flags.add(flag);
        }
    }

    public double getBalance() {
        return this.balance;
    }

    public void deposit(double amount) {
        this.balance+=amount;
    }

    public void deduct(double amount) {
        this.balance-=amount;
    }

}
