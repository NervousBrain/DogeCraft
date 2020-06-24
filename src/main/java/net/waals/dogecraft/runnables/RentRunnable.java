package net.waals.dogecraft.runnables;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Plot;
import net.waals.dogecraft.models.Shop;
import net.waals.dogecraft.models.Town;
import net.waals.dogecraft.util.AreaUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class RentRunnable implements Runnable {

    private TownManager townManager;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private AreaUtil areaUtil;

    public RentRunnable(TownManager townManager, DogePlayerManager dogePlayerManager, EconomyManager economyManager, AreaUtil areaUtil) {
        this.townManager = townManager;
        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
        this.areaUtil = areaUtil;
    }

    @Override
    public void run() {
        townManager.store();
        for(Town currentTown : townManager.getTowns()) {
            for(Plot currentPlot : currentTown.getPlots()) {
                DogePlayer dogePlayer = null;
                Instant now = Instant.now();
                if(currentPlot.getOwner() != null && currentPlot.getLastCollection().toInstant().isBefore(now.minus( 24 , ChronoUnit.HOURS))&&
                    (currentPlot.getLastCollection().toInstant().isBefore(now))) {
                    try {
                        dogePlayer = dogePlayerManager.getPlayer(currentPlot.getOwner());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(dogePlayer != null) {
                        if(economyManager.checkIfFundsAvailable(dogePlayer, currentPlot.getRent())) {
                            try {
                                economyManager.deduct(dogePlayer, currentPlot.getRent());
                                currentTown.deposit(currentPlot.getRent());
                                currentPlot.setLastCollection(new Date());
                                dogePlayerManager.storePlayer(dogePlayer);
                            } catch (Exception e) {

                            }
                        } else {
                            for(Shop shop : currentPlot.getShops()) {
                                townManager.getShopUtil().removeShopCosmetics(shop);
                            }
                            currentPlot.getShops().clear();
                            currentPlot.setOwner(null);
                            currentPlot.setLastCollection(null);
                            Sign sign = (Sign) Bukkit.getServer().getWorld("world").getBlockAt(areaUtil.pointToLocation(currentPlot.getSignLocation())).getState();
                            sign.setLine(0, ChatColor.DARK_GREEN + "[RENT]");
                            sign.setLine(1, currentPlot.getRent() + "" + ChatColor.GOLD + " Ð");
                            sign.setLine(2, "Right-click");
                            sign.setLine(3, "to rent");
                            sign.update();
                        }
                    }
                }
            }
        }
    }

}
