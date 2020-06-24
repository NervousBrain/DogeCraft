package net.waals.dogecraft.runnables;

import net.md_5.bungee.api.ChatColor;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.Area;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Point;
import net.waals.dogecraft.models.Town;
import net.waals.dogecraft.util.AreaUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class CheckInTownRunnable implements Runnable {

    private TownManager townManager;
    private DogePlayerManager dogePlayerManager;
    private AreaUtil areaUtil;

    public CheckInTownRunnable(TownManager townManager, DogePlayerManager dogePlayerManager,
                               AreaUtil areaUtil) {
        this.townManager = townManager;
        this.dogePlayerManager = dogePlayerManager;
        this.areaUtil = areaUtil;
    }

    @Override
    public void run() {
        for(Player currentPlayer : Bukkit.getOnlinePlayers()) {
            DogePlayer currentDogePlayer = null;
            Point currentPoint = areaUtil.locationTonPoint(currentPlayer.getLocation());
            if(currentPlayer.getWorld().equals(Bukkit.getWorld("world"))) {
                if(currentPlayer.getLocation().distanceSquared(new Location(Bukkit.getWorld("world"), -258, 64.5, -456)) > 20000) {
                    try {
                        currentDogePlayer = dogePlayerManager.getPlayer(currentPlayer.getUniqueId().toString());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(currentDogePlayer.getCurrentTown() == null) {
                        for(Town currentTown : townManager.getTowns()) {
                            if(townManager.inTown(currentTown.getName(), currentPoint)) {
                                currentDogePlayer.setCurrentTown(currentTown.getName());
                                currentPlayer.sendTitle(ChatColor.GOLD + currentTown.getName(), "", 10, 30, 10);
                            }
                        }
                    } else {
                        boolean inTown = false;
                        for(Town town : townManager.getTowns()) {
                            if(townManager.inTown(town.getName(), currentPoint)) {
                                inTown = true;
                                break;
                            }
                        }
                        if(!inTown) {
                            currentPlayer.sendTitle(ChatColor.DARK_GRAY + "Wilderness", "", 10, 30, 10);
                            currentDogePlayer.setCurrentTown(null);
                        }
                    }
                }
            }
        }
    }
}
