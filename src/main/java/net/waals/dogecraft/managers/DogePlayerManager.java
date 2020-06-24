package net.waals.dogecraft.managers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import net.waals.dogecraft.models.DogePlayer;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class DogePlayerManager {

    private ArrayList<DogePlayer> dogePlayers;
    private Firestore db;
    private CollectionReference docRef;
    private Wallet wallet;

    public DogePlayerManager(Firestore db, Wallet wallet) {
        this.wallet = wallet;
        this.dogePlayers = new ArrayList<DogePlayer>();
        this.db = db;
        this.docRef = db.collection("players");
    }

    public DogePlayer getPlayer(String uuid) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        for(DogePlayer currentDogePlayer : dogePlayers) {
            if(currentDogePlayer.getUuid().equals(uuid)) {
                dogePlayer = currentDogePlayer;
                break;
            }
        }
        if(dogePlayer == null) {
            dogePlayer = queryForPlayer(uuid);
        }
        return dogePlayer;
    }

    public DogePlayer getPlayerByAddress(String address) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        for(DogePlayer currentDogePlayer : dogePlayers) {
            if(currentDogePlayer.getAddress().equals(address)) {
                dogePlayer = currentDogePlayer;
                break;
            }
        }
        if(dogePlayer == null) {
            dogePlayer = this.queryForPlayerByAddress(address);
        }
        return dogePlayer;
    }

    public DogePlayer getPlayerByName(String name) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        for(DogePlayer currentDogePlayer : dogePlayers) {
            if(currentDogePlayer.getName().equals(name)) {
                dogePlayer = currentDogePlayer;
                break;
            }
        }
        if(dogePlayer == null) {
            dogePlayer = this.queryForPlayerByName(name);
        }
        return dogePlayer;
    }

    public DogePlayer createDogePlayer(String uuid, String name) {
        DogePlayer dogePlayer = new DogePlayer(uuid, wallet.freshReceiveAddress().toString(), 15, null, null,  null, name, null, null);
        this.storePlayer(dogePlayer);
        return dogePlayer;
    }

    public void storePlayer(DogePlayer dogePlayer) {
        DocumentReference ref = docRef.document(dogePlayer.getUuid());
        ApiFuture<WriteResult> result = ref.set(dogePlayer.toMap());
    }

    public DogePlayer queryForPlayer(String uuid) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        ApiFuture<DocumentSnapshot> future = docRef.document(uuid).get();
        DocumentSnapshot document = future.get();
        if(document.exists()) {
            System.out.println(document.getData().toString());
            System.out.println(document.getData().size());
            dogePlayer = new DogePlayer(document.getId(),
                    (String) document.getData().get("address"),
                    (int) document.getData().get("balance"),
                    (String) document.getData().get("town"),
                    (String) document.getData().get("requested"),
                    (String) document.getData().get("role"),
                    (String) document.getData().get("name"),
                    (String) document.getData().get("referredBy"), (String) document.getData().get("lastDonation"));
        }
        return dogePlayer;
    }

    public DogePlayer queryForPlayerByAddress(String address) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        ApiFuture<QuerySnapshot> future = docRef.whereEqualTo("address", address).get();
        if(future.get().getDocuments().size() > 0) {
            QueryDocumentSnapshot document = future.get().getDocuments().get(0);
            if(document.exists()) {
                System.out.println(document.getData().toString());
                dogePlayer = new DogePlayer(document.getId(),
                        (String) document.getData().get("address"),
                        (int) document.getData().get("balance"),
                        (String) document.getData().get("town"),
                        (String) document.getData().get("requested"),
                        (String) document.getData().get("role"),
                        (String) document.getData().get("name"),
                        (String) document.getData().get("referredBy"), (String) document.getData().get("lastDonation"));

            }
        }
        return dogePlayer;
    }

    public DogePlayer queryForPlayerByName(String name) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = null;
        ApiFuture<QuerySnapshot> future = docRef.whereEqualTo("name", name).get();
        if(future.get().getDocuments().size() > 0) {
            QueryDocumentSnapshot document = future.get().getDocuments().get(0);
            if(document.exists()) {
                dogePlayer = new DogePlayer(document.getId(),
                        (String) document.getData().get("address"),
                        (int) document.getData().get("balance"),
                        (String) document.getData().get("town"),
                        (String) document.getData().get("requested"),
                        (String) document.getData().get("role"),
                        (String) document.getData().get("name"),
                        (String) document.getData().get("referredBy"), (String) document.getData().get("lastDonation"));
            }
        }
        return dogePlayer;
    }


    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public ArrayList<DogePlayer> getDogePlayers() {
        return this.dogePlayers;
    }

    public void loadDogePlayer(DogePlayer dogePlayer) throws ExecutionException, InterruptedException {
        this.dogePlayers.add(dogePlayer);
    }

    public void unloadDogePlayer(String uuid) throws ExecutionException, InterruptedException {
        this.dogePlayers.remove(this.getPlayer(uuid));
    }
}


