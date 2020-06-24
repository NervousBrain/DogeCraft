package net.waals.dogecraft.managers;

import net.waals.dogecraft.models.DogePlayer;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class LotteryManager {

    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private HashMap<String, Integer> tickets;
    private Random rand;

    public LotteryManager(DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
        this.tickets = new HashMap<>();
        rand = new Random();
    }

    public String pickWinner() {
        double r = rand.nextDouble() * getPot();
        String winner = null;
        int total = 0;
        for (String entry : tickets.keySet()) {
            if ((total+tickets.get(entry)) >= r) {
                winner = entry;
                break;
            }
            total+=tickets.get(entry);
        }
        return winner;
    }

    public void reset() {
        tickets.clear();
        rand = new Random();
    }

    public void close() throws ExecutionException, InterruptedException {
        for(String s : tickets.keySet()) {
            DogePlayer dogePlayer = null;
            dogePlayer = dogePlayerManager.getPlayer(s);
            economyManager.deposit(dogePlayer, tickets.get(s));
        }
        reset();
    }

    public void add(String player, int amount) {
        if(tickets.get(player) != null) {
            int current = tickets.get(player);
            tickets.put(player, current+amount);
        } else {
            tickets.put(player, amount);
        }
    }

    public int getNumberOfTickets(String player) {
        if(tickets.get(player) != null) {
            return tickets.get(player);
        } else {
            return 0;
        }
    }

    public double getPot() {
        double amount = 0;
        for(double d : tickets.values()) {
            amount += d;
        }
        return amount;
    }
}
