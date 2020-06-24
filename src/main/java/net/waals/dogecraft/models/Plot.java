package net.waals.dogecraft.models;

import java.util.ArrayList;
import java.util.Date;

public class Plot {

    private Area area;
    private String owner;

    private Point sign;
    private double rent;
    private Date lastCollection;
    private ArrayList<Shop> shops;

    public Plot(String owner, Area area, Point sign, double rent, ArrayList<Shop> shops) {
        this.area = area;
        this.sign = sign;
        this.rent = rent;
        this.owner = owner;
        this.lastCollection = null;
        this.shops = shops;
    }

    public Area getArea() {
        return this.area;
    }

    public Point getSignLocation() {
        return this.sign;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public double getRent() {
        return rent;
    }

    public void setLastCollection(Date date) {
        this.lastCollection = date;
    }

    public Date getLastCollection() {
        return this.lastCollection;
    }

    public ArrayList<Shop> getShops() {
        return this.shops;
    }

   public void addShop(Shop shop) {
        this.shops.add(shop);
   }

   public void removeShop(Shop shop) {
        this.shops.remove(shop);
   }
}
