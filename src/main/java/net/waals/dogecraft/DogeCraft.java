package net.waals.dogecraft;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import net.iso2013.mlapi.api.MultiLineAPI;
import net.milkbowl.vault.economy.Economy;
import net.waals.dogecraft.commands.*;
import net.waals.dogecraft.guis.TownAdminGui;
import net.waals.dogecraft.listeners.PlayerListener;
import net.waals.dogecraft.listeners.WorldListener;
import net.waals.dogecraft.managers.*;
import net.waals.dogecraft.runnables.CheckInTownRunnable;
import net.waals.dogecraft.runnables.LotteryRunnable;
import net.waals.dogecraft.runnables.RentRunnable;
import net.waals.dogecraft.util.AreaUtil;
import net.waals.dogecraft.util.tags.TagUtil;
import net.waals.dogecraft.wallet.CoreWallet;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;


import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DogeCraft extends JavaPlugin {

    public static DynmapCommonAPI dapi = null;
    private CoreWallet wallet;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private TownManager townManager;
    private static Economy econ = null;
    private LotteryManager lotteryManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("dogecraft-797cd")
                .build();
        FirebaseApp.initializeApp(options);
        Firestore db = FirestoreClient.getFirestore();
        dogePlayerManager = new DogePlayerManager(db, null);
        economyManager = new EconomyManager(this, dogePlayerManager, db);
        wallet = new CoreWallet(dogePlayerManager, economyManager);

        try {
            wallet.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dogePlayerManager.setWallet(wallet.getWallet());
        AreaUtil areaUtil = new AreaUtil(this);
        arenaManager = new ArenaManager(this, areaUtil, dogePlayerManager);
        townManager = new TownManager(areaUtil, this, arenaManager);
        lotteryManager = new LotteryManager(dogePlayerManager, economyManager);
        ClaimManager claimManager = new ClaimManager(this, areaUtil);
        TownAdminGui townGui = new TownAdminGui(townManager, claimManager, this, dogePlayerManager, economyManager);
        getServer().getPluginManager().registerEvents(new PlayerListener(arenaManager, dogePlayerManager, claimManager, areaUtil, townManager, economyManager, this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(townManager), this);
        this.getCommand("balance").setExecutor(new BalanceCommand(dogePlayerManager));
        this.getCommand("money").setExecutor(new BalanceCommand(dogePlayerManager));
        this.getCommand("bal").setExecutor(new BalanceCommand(dogePlayerManager));
        this.getCommand("deposit").setExecutor(new WalletCommands(dogePlayerManager, wallet, economyManager));
        this.getCommand("withdraw").setExecutor(new WalletCommands(dogePlayerManager, wallet, economyManager));
        WildCommand wc = new WildCommand(townManager, this);
        TeleportationCommands tc = new TeleportationCommands(dogePlayerManager);
        this.getCommand("back").setExecutor(tc);
        this.getCommand("tpa").setExecutor(tc);
        this.getCommand("tpaccept").setExecutor(tc);
        this.getCommand("tpdeny").setExecutor(tc);
        this.getCommand("wild").setExecutor(wc);
        this.getCommand("rtp").setExecutor(wc);
        this.getCommand("spawn").setExecutor(tc);
        this.getCommand("donate").setExecutor(new DonationCommand(economyManager, dogePlayerManager));
        this.getCommand("removeshopcosmetics").setExecutor(new AdminCommands(townManager));
        this.getCommand("dogecoin").setExecutor(new WalletCommands(dogePlayerManager, wallet, economyManager));
        this.getCommand("ref").setExecutor(new ReferralCommand(dogePlayerManager, economyManager));
        this.getCommand("menu").setExecutor(new MenuCommand(dogePlayerManager, claimManager, townManager, this, economyManager));
        this.getCommand("pay").setExecutor(new PayCommand(dogePlayerManager, economyManager));
        this.getCommand("lottery").setExecutor(new LotteryCommand(lotteryManager, economyManager, dogePlayerManager));
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckInTownRunnable(townManager, dogePlayerManager, areaUtil), 0L, 25L);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new RentRunnable(townManager, dogePlayerManager, economyManager, areaUtil), 0L, 5*60*20L);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new LotteryRunnable(lotteryManager, dogePlayerManager, economyManager), 0L, 5*60*20L);
        try {
            townManager.load();
            this.townManager.spawnAllShopCosmetics();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MultiLineAPI lineAPI = (MultiLineAPI) Bukkit.getPluginManager().getPlugin("MultiLineAPI");
        if (lineAPI == null) {
            throw new IllegalStateException("Failed to start demo plugin! MultiLineAPI could not be found!");
        }
        lineAPI.addDefaultTagController(new TagUtil(dogePlayerManager, this));

        this.setupEconomy();
        registerDynmap(this);

    }

    @Override
    public void onDisable() {
        wallet.stop();
        try {
            lotteryManager.close();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HandlerList.unregisterAll();
        townManager.getShopUtil().removeShopCosmetics();
        townManager.store();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
       // getServer().getServicesManager().register(Economy.class, economyManager, this, ServicePriority.Highest);
    }

    public static void registerDynmap(JavaPlugin p) {
        dapi = (DynmapCommonAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (dapi == null) {
            Bukkit.getServer().getPluginManager().disablePlugin(p);
        }
    }
}


