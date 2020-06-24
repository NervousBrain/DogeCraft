package net.waals.dogecraft.runnables;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.managers.LotteryManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.concurrent.ExecutionException;

public class LotteryRunnable implements Runnable{

    private LotteryManager lotteryManager;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private int i;

    public LotteryRunnable(LotteryManager lotteryManager, DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.lotteryManager = lotteryManager;
        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
        this.i = 0;
    }

    @Override
    public void run() {
        if (i >= 5) {
            if(lotteryManager.getPot() > 0) {
                DogePlayer winner = null;
                String winnerUUID = lotteryManager.pickWinner();
                try {
                    winner = dogePlayerManager.getPlayer(winnerUUID);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + ">> " + winner.getName() + " won " + lotteryManager.getPot() + " DOGE in the lottery!");
                economyManager.deposit(winner, lotteryManager.getPot());
                lotteryManager.reset();
            }
            i = 0;
        } else {
            i++;
        }
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + ">> The lottery will be drawn in " + (30-(i*5)) + " minutes.");
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + ">> The current pot is " + lotteryManager.getPot() + " DOGE.");
    }
}
