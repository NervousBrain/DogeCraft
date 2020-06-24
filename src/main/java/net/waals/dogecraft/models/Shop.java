package net.waals.dogecraft.models;

public class Shop {

    private String dogePlayer;
    private Point location;
    private ShopItem item;
    private double sellPrice;
    private double buyPrice;

   public Shop(String dogePlayer, Point location, ShopItem item, double sellPrice, double buyPrice) {
       this.dogePlayer = dogePlayer;
       this.location = location;
       this.item = item;
       this.sellPrice = sellPrice;
       this.buyPrice = buyPrice;
   }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getDogePlayer() {
        return dogePlayer;
    }

    public void setDogePlayer(String dogePlayer) {
        this.dogePlayer = dogePlayer;
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

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }
}
