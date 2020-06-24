package net.waals.dogecraft.managers;

import net.waals.dogecraft.models.Area;

public class Outpost {

    private String dogePlayer;
    private Area area;

    public Outpost(String dogePlayer, Area area) {
        this.dogePlayer = dogePlayer;
        this.area = area;
    }

    public String getDogePlayer() {
        return dogePlayer;
    }

    public void setDogePlayer(String dogePlayer) {
        this.dogePlayer = dogePlayer;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
