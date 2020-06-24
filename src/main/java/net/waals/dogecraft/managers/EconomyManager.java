package net.waals.dogecraft.managers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Town;
import net.waals.dogecraft.models.Withdrawal;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EconomyManager {

    private DogeCraft plugin;
    private ArrayList<String> payers;
    private ArrayList<String> withdrawers;
    private DogePlayerManager dogePlayerManager;
    private Firestore db;

    public EconomyManager(DogeCraft plugin, DogePlayerManager dogePlayerManager, Firestore db) {
        this.plugin = plugin;
        this.dogePlayerManager = dogePlayerManager;
        this.db = db;
        this.withdrawers = new ArrayList<>();
        this.payers = new ArrayList<>();
    }

    public void pay(String fromName, String toName, double amount) throws ExecutionException, InterruptedException {
        DogePlayer from = null;
        DogePlayer to = null;
        from = dogePlayerManager.getPlayerByName(fromName);
        to = dogePlayerManager.getPlayerByName(toName);
        from.setBalance(from.getBalance()-amount);
        to.setBalance(to.getBalance()+amount);
        dogePlayerManager.storePlayer(from);
        dogePlayerManager.storePlayer(to);
    }

    public void payUuid(String fromUuid, String toUuid, double amount) throws ExecutionException, InterruptedException {
        DogePlayer from = null;
        DogePlayer to = null;
        from = dogePlayerManager.getPlayer(fromUuid);
        to = dogePlayerManager.getPlayer(toUuid);
        from.setBalance(from.getBalance()-amount);
        to.setBalance(to.getBalance()+amount);
        dogePlayerManager.storePlayer(from);
        dogePlayerManager.storePlayer(to);
    }

    public void deposit(DogePlayer dogePlayer, double amount) {
        dogePlayer.setBalance(dogePlayer.getBalance()+amount);
        dogePlayerManager.storePlayer(dogePlayer);
    }

    public void deduct(DogePlayer dogePlayer, double amount) throws Exception {
        if(dogePlayer.getBalance() < amount) {
            throw new Exception("Insufficient funds.");
        }
        dogePlayer.setBalance(dogePlayer.getBalance()-amount);
        dogePlayerManager.storePlayer(dogePlayer);
    }

    public boolean checkIfFundsAvailable(DogePlayer dogePlayer, double amount) {
        boolean available = false;
        if(dogePlayer.getBalance() >= amount) {
            available = true;
        }
        return available;
    }

    public boolean checkIfFundsAvailable(Town town, double amount) {
        boolean available = false;
        if(town.getBalance() >= amount) {
            available = true;
        }
        return available;
    }

    public void addPayer(String payer) {
        this.payers.add(payer);
    }

    public ArrayList<String> getPayers() {
        return this.payers;
    }

    public void removePayer(String payer) {
        for(String currentPayer : payers) {
            if(currentPayer.equalsIgnoreCase(payer)) {
                payers.remove(currentPayer);
                break;
            }
        }
    }

    public void addWithdrawer(String payer) {
        this.withdrawers.add(payer);
    }

    public ArrayList<String> getWithdrawers() {
        return this.withdrawers;
    }

    public void removeWithdrawer(String payer) {
        for(String currentPayer : withdrawers) {
            if(currentPayer.equalsIgnoreCase(payer)) {
                withdrawers.remove(currentPayer);
                break;
            }
        }
    }

    public void storeWithdrawal(Withdrawal withdrawal) {
        CollectionReference ref = db.collection("withdrawals");
        ApiFuture<DocumentReference> result = ref.add(withdrawal.toMap());
    }
}
