package net.waals.dogecraft.util;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.Area;
import net.waals.dogecraft.models.Point;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AreaUtil {

    private DogeCraft plugin;
    private Map<Area, Integer> highlights;

    public AreaUtil(DogeCraft plugin) {
        this.plugin = plugin;
        this.highlights = new HashMap<Area, Integer>();
    }


    public void highlightArea(Area a, Player p) {
        ArrayList<Location> edgePoints = new ArrayList<Location>();
        Point dist = a.getDistance();
        for(int i = 0; i <= dist.getX(); i++) {
            for(int j = 0; j <= dist.getZ(); j++) {
                if(i == dist.getX() || i == 0 || j == dist.getZ() || j == 0) {
                    for(int y = p.getLocation().getBlockY()-4; y < p.getLocation().getBlockY()+12; y++) {
                        Location point1;
                        Location point2;
                        if(a.getP1().getX() <= a.getP2().getX()) {
                            if(a.getP1().getZ() <= a.getP2().getZ()) {
                                point1 = new Location(p.getWorld(),a.getP1().getX()+i + 0.5, y, a.getP1().getZ()+j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()-i + 0.5, y, a.getP2().getZ()-j + 0.5);
                            } else {
                                point1 = new Location(p.getWorld(),a.getP1().getX()+i + 0.5, y, a.getP1().getZ()-j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()-i + 0.5, y, a.getP2().getZ()+j + 0.5);
                            }
                        } else {
                            if(a.getP1().getZ() <= a.getP2().getZ()) {
                                point1 = new Location(p.getWorld(),a.getP1().getX()-i + 0.5, y, a.getP1().getZ()+j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()+i + 0.5, y, a.getP2().getZ()-j + 0.5);
                            } else {
                                point1 = new Location(p.getWorld(),a.getP1().getX()-i + 0.5, y, a.getP1().getZ()-j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()+i + 0.5, y, a.getP2().getZ()+j + 0.5);
                            }
                        }
                        edgePoints.add(point1);
                        edgePoints.add(point2);
                    }
                }
            }
        }
        int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                for(Location loc : edgePoints) {
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1);
                    p.spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
                }
            }
        }, 0L, 5L);
        highlights.put(a, task);
    }

    public void highlightArea(Area a, Player p, Color color) {
        ArrayList<Location> edgePoints = new ArrayList<Location>();
        Point dist = a.getDistance();
        for(int i = 0; i <= dist.getX(); i++) {
            for(int j = 0; j <= dist.getZ(); j++) {
                if(i == dist.getX() || i == 0 || j == dist.getZ() || j == 0) {
                    for(int y = p.getLocation().getBlockY()-4; y < p.getLocation().getBlockY()+12; y++) {
                        Location point1;
                        Location point2;
                        if(a.getP1().getX() <= a.getP2().getX()) {
                            if(a.getP1().getZ() <= a.getP2().getZ()) {
                                point1 = new Location(p.getWorld(),a.getP1().getX()+i + 0.5, y, a.getP1().getZ()+j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()-i + 0.5, y, a.getP2().getZ()-j + 0.5);
                            } else {
                                point1 = new Location(p.getWorld(),a.getP1().getX()+i + 0.5, y, a.getP1().getZ()-j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()-i + 0.5, y, a.getP2().getZ()+j + 0.5);
                            }
                        } else {
                            if(a.getP1().getZ() <= a.getP2().getZ()) {
                                point1 = new Location(p.getWorld(),a.getP1().getX()-i + 0.5, y, a.getP1().getZ()+j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()+i + 0.5, y, a.getP2().getZ()-j + 0.5);
                            } else {
                                point1 = new Location(p.getWorld(),a.getP1().getX()-i + 0.5, y, a.getP1().getZ()-j + 0.5);
                                point2 = new Location(p.getWorld(), a.getP2().getX()+i + 0.5, y, a.getP2().getZ()+j + 0.5);
                            }
                        }
                        edgePoints.add(point1);
                        edgePoints.add(point2);
                    }
                }
            }
        }
        int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                for(Location loc : edgePoints) {
                    Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);
                    p.spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
                }
            }
        }, 0L, 20L);
        highlights.put(a, task);
    }

    public void stopHighlight(Area a) {
        Area get = null;
        for(Area ar : highlights.keySet()) {
            if(a.isEqual(ar)) {
                get = ar;
                break;
            }
        }
        if(get != null) {
            Bukkit.getScheduler().cancelTask(highlights.get(get));
        }
    }

    public Location pointToLocation(Point point) {
        return new Location(Bukkit.getWorld("world"), point.getX(), point.getY(), point.getZ());
    }

    public boolean inArea(Area a, Point p) {
        boolean in = false;
        if(a.getP1().getX() <= a.getP2().getX()) {
            if(a.getP1().getZ() <= a.getP2().getZ()) {
                in = (p.getX() >= a.getP1().getX() && p.getZ() >= a.getP1().getZ() &&
                        p.getX() <= a.getP2().getX() && p.getZ() <= a.getP2().getZ());
            } else {
                in = (p.getX() >= a.getP1().getX() && p.getZ() <= a.getP1().getZ() &&
                        p.getX() <= a.getP2().getX() && p.getZ() >= a.getP2().getZ());
            }
        } else {
            if(a.getP1().getZ() <= a.getP2().getZ()) {
                in = (p.getX() <= a.getP1().getX() && p.getZ() >= a.getP1().getZ() &&
                        p.getX() >= a.getP2().getX() && p.getZ() <= a.getP2().getZ());
            } else {
                in = (p.getX() <= a.getP1().getX() && p.getZ() <= a.getP1().getZ() &&
                        p.getX() >= a.getP2().getX() && p.getZ() >= a.getP2().getZ());
            }
        }
        return in;
    }

    public Point locationTonPoint(Location location) {
        return new Point(location.getBlockX(), location.getBlockZ());
    }

    public Point locationToPointY(Location location) {
        return new Point(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean inArea(Area a, Area b) {
        boolean in = inArea(a, b.getP1()) && inArea(a, b.getP2());
        return in;
    }

    public boolean overlapping(Area a, Area b) {
        boolean overlap = inArea(a, b.getP1()) || inArea(a, b.getP2());
        return overlap;
    }
}
