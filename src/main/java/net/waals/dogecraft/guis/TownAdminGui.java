package net.waals.dogecraft.guis;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.managers.ClaimManager;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.Claim;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Point;
import net.waals.dogecraft.models.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TownAdminGui {

    private TownManager townManager;
    private ClaimManager claimManager;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private Player player;
    private Gui mainMenu;
    private Gui requestsMenu;
    private Gui playersMenu;
    private Gui flagMenu;
    private DogeCraft plugin;
    private Town town;
    private Gui townListGui;

    public TownAdminGui(TownManager townManager, ClaimManager claimManager, DogeCraft plugin, DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.townManager = townManager;
        this.claimManager = claimManager;
        this.economyManager = economyManager;
        this.player = null;
        this.plugin = plugin;
        this.dogePlayerManager = dogePlayerManager;
        this.town = null;
    }

    public void show(Player player, String townName) {
        this.player = player;
        this.town = townManager.getTown(townName);
        mainMenu = new Gui(plugin, 3, "Admin menu");
        StaticPane menu = new StaticPane(0, 0, 9, 3);

        ItemStack requestItem = new ItemStack(Material.PAPER);
        ItemMeta requestItemMeta = requestItem.getItemMeta();
        requestItemMeta.setDisplayName("Show requests");
        requestItemMeta.setLore(Arrays.asList("› " + townManager.getTown(townName).getRequests().size() + " requests"));
        requestItem.setItemMeta(requestItemMeta);

        ItemStack playersItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playersItemMeta = playersItem.getItemMeta();
        playersItemMeta.setDisplayName("Show players");
        playersItemMeta.setLore(Arrays.asList("› " + townManager.getTown(townName).getMembers().size() + " players"));
        playersItem.setItemMeta(playersItemMeta);

        ItemStack plotItem = new ItemStack(Material.MYCELIUM);
        ItemMeta plotItemMeta = plotItem.getItemMeta();
        plotItemMeta.setDisplayName("Add member plot");
        plotItem.setItemMeta(plotItemMeta);

        ItemStack flagItem = new ItemStack(Material.RED_BANNER);
        ItemMeta flagItemMeta = flagItem.getItemMeta();
        flagItemMeta.setDisplayName("Town flags");
        flagItem.setItemMeta(flagItemMeta);

        ItemStack bankItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta bankItemMeta = bankItem.getItemMeta();
        bankItemMeta.setDisplayName("Town bank");
        bankItemMeta.setLore(Arrays.asList("Balance: " + town.getBalance() + " DOGE", "Left-click to deposit", "Right-click to withdraw"));
        bankItem.setItemMeta(bankItemMeta);

        ItemStack extendItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta extendItemMeta = extendItem.getItemMeta();
        extendItemMeta.setDisplayName("Extend the town");
        extendItemMeta.setLore(Arrays.asList("Cost: 50 DOGE"));
        extendItem.setItemMeta(extendItemMeta);

        ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName("~");
        fillerItem.setItemMeta(fillerItemMeta);

        ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpItemMeta = tpItem.getItemMeta();
        tpItemMeta.setDisplayName("Town Warps");
        tpItem.setItemMeta(tpItemMeta);

        ItemStack arenaItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta arenaItemMeta = arenaItem.getItemMeta();
        arenaItemMeta.setDisplayName("Create arena");
        arenaItem.setItemMeta(arenaItemMeta);

        ItemStack spawnItem = new ItemStack(Material.COMPASS);
        ItemMeta spawnItemMeta = spawnItem.getItemMeta();
        spawnItemMeta.setDisplayName("Set town spawn");
        spawnItem.setItemMeta(spawnItemMeta);

        menu.addItem(new GuiItem(flagItem, event -> {
            try {
                showFlags();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 0, 0);

        menu.addItem(new GuiItem(plotItem, event -> {
            claimManager.startClaim(player, Claim.Type.PLOT_CREATION);
            player.closeInventory();
        }), 2, 0);

        menu.addItem(new GuiItem(playersItem, event -> {
            try {
                showPlayers();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 4, 0);

        menu.addItem(new GuiItem(requestItem, event -> {
            try {
                showRequests();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 6, 0);

        menu.addItem(new GuiItem(bankItem, event ->{
            if(event.isLeftClick()) {
                economyManager.addPayer(player.getUniqueId().toString());
                player.sendMessage(ChatColor.GOLD + ">> Type the amount you would like to deposit into the chat.");
            } else if(event.isRightClick()) {
                economyManager.addWithdrawer(player.getUniqueId().toString());
                player.sendMessage(ChatColor.GOLD + ">> Type the amount you would like to withdraw into the chat.");
            }

            player.closeInventory();
        }), 8, 0);

        menu.addItem(new GuiItem(extendItem, event -> {
            if(economyManager.checkIfFundsAvailable(town, 50)) {
                townManager.highlightTown(town, player);
                claimManager.startClaim(player, Claim.Type.TOWN_EXTENSION);
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + ">> Your town does not have enough funds available.");
            }
        }), 1, 1);

        ItemStack shopItem = new ItemStack(Material.CHEST);
        ItemMeta shopItemMeta = shopItem.getItemMeta();
        shopItemMeta.setDisplayName("Create a shop");
        shopItem.setItemMeta(shopItemMeta);

        menu.addItem(new GuiItem(shopItem, event -> {
            economyManager.addPayer(player.getUniqueId().toString());
            claimManager.startClaim(player, Claim.Type.SHOP_CREATION);
            player.closeInventory();
        }), 3, 1);

        menu.addItem(new GuiItem(tpItem, event -> {
            createWarpList(player);
            this.townListGui.show(player);
        }), 5, 1 );

        menu.addItem(new GuiItem(arenaItem, event -> {
            claimManager.startClaim(player, Claim.Type.ARENA_CREATION);
            player.closeInventory();
        }), 7, 1 );

        menu.addItem(new GuiItem(spawnItem, event -> {
            Point spawn = new Point(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
            if(townManager.inTown(town.getName(), spawn)) {
                town.setSpawn(spawn);
                player.sendMessage(ChatColor.GOLD + ">> Town spawn set to your current location.");
            } else {
                player.sendMessage(ChatColor.RED + ">> Spawnpoint needs to be inside the town.");
            }
            player.closeInventory();
        }), 0, 2 );



        menu.setOnClick(event -> {
            event.setCancelled(true);
        });

        menu.fillWith(fillerItem);

        mainMenu.addPane(menu);

        mainMenu.show(player);
    }

    private void createWarpList(Player player) {
        this.townListGui = new Gui(plugin, 6, "Select a town");
        PaginatedPane townList = new PaginatedPane(0, 1, 9, 4);
        for (int i = 0; i < townManager.getTowns().size(); i += 36) {
            StaticPane pane = new StaticPane(0, 0, 9, 4);
            for (int j = 0; j < 36; j++) {
                if ((i + j) < townManager.getTowns().size()) {
                    Town currentTown = townManager.getTowns().get(i + j);
                    ItemStack item = new ItemStack(Material.GRASS_BLOCK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(currentTown.getName());
                    meta.setLore(Arrays.asList("Teleport here"));
                    item.setItemMeta(meta);
                    int x = 0;
                    int y = 0;
                    if (j < 9) {
                        y = 0;
                        x = j;
                    } else if (j < 18) {
                        y = 1;
                        x = j - 9;
                    } else if (j < 27) {
                        y = 2;
                        x = j - 18;
                    } else {
                        y = 3;
                        x = j - 27;
                    }
                    pane.addItem(new GuiItem(item, event -> {
                        if(currentTown.getSpawn() != null) {
                            player.teleport(new Location(Bukkit.getWorld("world"), currentTown.getSpawn().getX(), currentTown.getSpawn().getY(), currentTown.getSpawn().getZ()));
                        } else {
                            player.teleport(Bukkit.getWorld("world").getHighestBlockAt(currentTown.getAreas().get(0).getP1().getX(), currentTown.getAreas().get(0).getP1().getZ()).getLocation().add(new Vector(0, 3.5, 0)));
                        }
                    }), x, y);
                } else {
                    break;
                }
            }
            townList.addPane(i / 36, pane);
        }

        ItemStack forwardItem = new ItemStack(Material.ARROW);
        ItemMeta forwardItemMeta = forwardItem.getItemMeta();
        forwardItemMeta.setDisplayName("->");
        forwardItem.setItemMeta(forwardItemMeta);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("<-");
        backItem.setItemMeta(backItemMeta);

        ItemStack menuItem = new ItemStack(Material.BARRIER);
        ItemMeta menuItemMeta = menuItem.getItemMeta();
        menuItemMeta.setDisplayName("Back to Menu");
        menuItem.setItemMeta(menuItemMeta);

        StaticPane back = new StaticPane(2, 5, 1, 1);
        StaticPane forward = new StaticPane(6, 5, 1, 1);
        back.addItem(new GuiItem(backItem, event -> {
            townList.setPage(townList.getPage() - 1);

            if (townList.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            townListGui.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(forwardItem, event -> {
            townList.setPage(townList.getPage() + 1);

            if (townList.getPage() == townList.getPages() - 1) {
                forward.setVisible(false);
            }

            back.setVisible(true);
            townListGui.update();
        }), 0, 0);

        if(townList.getPages() <= 1) {
            forward.setVisible(false);
        }

        StaticPane menu = new StaticPane(4, 0, 1, 1);
        menu.addItem(new GuiItem(menuItem, event -> {
            mainMenu.show(player);
        }), 0,0);

        townListGui.addPane(menu);
        townListGui.addPane(townList);
        townListGui.addPane(back);
        townListGui.addPane(forward);
    }

    public void showFlags() throws ExecutionException, InterruptedException {
        this.flagMenu = new Gui(plugin, 1, "Flags");

        StaticPane menu = new StaticPane(0, 0, 9, 1);

        ItemStack spawnItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta spawnItemMeta = spawnItem.getItemMeta();
        spawnItemMeta.setDisplayName("Hostile mob spawning");
        if(!town.hasFlag(Town.Flag.MOB_SPAWN)) {
            spawnItemMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "Enabled", "› Click to toggle"));
        } else {
            spawnItemMeta.setLore(Arrays.asList(ChatColor.RED + "Disabled", "› Click to toggle"));
        }
        spawnItem.setItemMeta(spawnItemMeta);

        ItemStack pvpItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta pvpItemMeta = pvpItem.getItemMeta();
        pvpItemMeta.setDisplayName("PVP");
        if(!town.hasFlag(Town.Flag.PVP)) {
            pvpItemMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "Enabled", "› Click to toggle"));
        } else {
            pvpItemMeta.setLore(Arrays.asList(ChatColor.RED + "Disabled", "› Click to toggle"));
        }
        pvpItem.setItemMeta(pvpItemMeta);

        ItemStack explosionItem = new ItemStack(Material.TNT);
        ItemMeta explosionItemMeta = explosionItem.getItemMeta();
        explosionItemMeta.setDisplayName("Explosion block damage");
        if(!town.hasFlag(Town.Flag.EXPLOSION)) {
            explosionItemMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "Enabled", "› Click to toggle"));
        } else {
            explosionItemMeta.setLore(Arrays.asList(ChatColor.RED + "Disabled", "› Click to toggle"));
        }
        explosionItem.setItemMeta(explosionItemMeta);

        menu.addItem(new GuiItem(spawnItem, event -> {
            town.toggleFlag(Town.Flag.MOB_SPAWN);
            try {
                this.showFlags();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 1, 0);

        menu.addItem(new GuiItem(pvpItem, event -> {
            town.toggleFlag(Town.Flag.PVP);
            try {
                this.showFlags();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 3, 0);

        menu.addItem(new GuiItem(explosionItem, event -> {
            town.toggleFlag(Town.Flag.EXPLOSION);
            try {
                this.showFlags();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), 5, 0);

        ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName("~");
        fillerItem.setItemMeta(fillerItemMeta);

        menu.fillWith(fillerItem);

        menu.setOnClick(event -> {
            event.setCancelled(true);
        });

        flagMenu.addPane(menu);

        flagMenu.show(player);
    }

    public void showRequests() throws ExecutionException, InterruptedException {
        this.requestsMenu = new Gui(plugin, 6, "Requests");
        PaginatedPane requestList = new PaginatedPane(0, 1, 9, 4);
        for(int i = 0; i < town.getRequests().size(); i+=36) {
            StaticPane pane = new StaticPane(0,0,9,4);
            for(int j = 0; j < 36; j++) {
                if((i+j) < town.getRequests().size()) {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = item.getItemMeta();
                    String currentRequest = town.getRequests().get(i+j);
                    meta.setDisplayName(Bukkit.getOfflinePlayer(UUID.fromString(currentRequest)).getName());
                    meta.setLore(Arrays.asList("Left-click to accept", "Right-click to decline"));
                    item.setItemMeta(meta);
                    int x = 0;
                    int y = 0;
                    if(j < 9) {
                        y = 0;
                        x = j;
                    } else if(j < 18) {
                        y = 1;
                        x = j - 9;
                    } else if(j < 27) {
                        y = 2;
                        x= j - 18;
                    } else {
                        y = 3;
                        x = j - 27;
                    }
                    player.sendMessage(currentRequest);
                    DogePlayer dogePlayer = dogePlayerManager.getPlayer(currentRequest);
                    pane.addItem(new GuiItem(item, event -> {
                        if(event.isLeftClick()) {
                            town.acceptRequest(currentRequest);
                            dogePlayer.setTown(town.getName());
                            dogePlayer.setRole("CITIZEN");
                        } else {
                            town.removeRequest(currentRequest);
                        }
                        dogePlayer.setRequested(null);
                        dogePlayerManager.storePlayer(dogePlayer);
                        try {
                            showRequests();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }), x, y);
                } else {
                    break;
                }
            }
            requestList.addPane(i/36, pane);
        }

        ItemStack forwardItem = new ItemStack(Material.ARROW);
        ItemMeta forwardItemMeta = forwardItem.getItemMeta();
        forwardItemMeta.setDisplayName("->");
        forwardItem.setItemMeta(forwardItemMeta);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("<-");
        backItem.setItemMeta(backItemMeta);

        ItemStack menuItem = new ItemStack(Material.BARRIER);
        ItemMeta menuItemMeta = menuItem.getItemMeta();
        menuItemMeta.setDisplayName("Back to Menu");
        menuItem.setItemMeta(menuItemMeta);

        StaticPane back = new StaticPane(2, 5, 1, 1);
        StaticPane forward = new StaticPane(6, 5, 1, 1);
        back.addItem(new GuiItem(backItem, event -> {
            requestList.setPage(requestList.getPage() - 1);

            if (requestList.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            requestsMenu.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(forwardItem, event -> {
            requestList.setPage(requestList.getPage() + 1);

            if (requestList.getPages() <= 1) {
                forward.setVisible(false);
            }

            back.setVisible(true);
            requestsMenu.update();
        }), 0, 0);

        if(requestList.getPages() <= 1) {
            forward.setVisible(false);
        }

        StaticPane menu = new StaticPane(4, 0, 1, 1);
        menu.addItem(new GuiItem(menuItem, event -> {
            show(player, town.getName());
        }), 0,0);

        requestsMenu.addPane(menu);
        requestsMenu.addPane(requestList);
        requestsMenu.addPane(forward);
        requestsMenu.addPane(back);

        player.sendMessage(requestList.getPages() + "");
        requestsMenu.show(player);

    }

    public void showPlayers() throws ExecutionException, InterruptedException {
        this.playersMenu = new Gui(plugin, 6, "Players");
        PaginatedPane playerList = new PaginatedPane(0, 1, 9, 4);
        for(int i = 0; i < town.getMembers().size(); i+=36) {
            StaticPane pane = new StaticPane(0,0,9,4);
            for(int j = 0; j < 36; j++) {
                if((i+j) < town.getMembers().size()) {
                    DogePlayer currentDogePlayer = dogePlayerManager.getPlayer(town.getMembers().get(i+j));
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Bukkit.getOfflinePlayer(UUID.fromString(currentDogePlayer.getUuid())).getName());
                    if(!currentDogePlayer.getUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                        meta.setLore(Arrays.asList(currentDogePlayer.getRole(), "Left-click to promote", "Right-click to demote"));
                    } else {
                        meta.setLore(Arrays.asList("You"));
                    }
                    item.setItemMeta(meta);
                    int x = 0;
                    int y = 0;
                    if(j < 9) {
                        y = 0;
                        x = j;
                    } else if(j < 18) {
                        y = 1;
                        x = j - 9;
                    } else if(j < 27) {
                        y = 2;
                        x= j - 18;
                    } else {
                        y = 3;
                        x = j - 27;
                    }
                    pane.addItem(new GuiItem(item, event -> {
                        if(!currentDogePlayer.getUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                            if(event.isLeftClick()) {
                                currentDogePlayer.setRole("STAFF");
                            } else {
                                currentDogePlayer.setRole("CITIZEN");
                            }
                            dogePlayerManager.storePlayer(currentDogePlayer);
                            try {
                                showPlayers();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }), x, y);
                } else {
                    break;
                }
            }
            playerList.addPane(i/36, pane);
        }

        ItemStack forwardItem = new ItemStack(Material.ARROW);
        ItemMeta forwardItemMeta = forwardItem.getItemMeta();
        forwardItemMeta.setDisplayName("->");
        forwardItem.setItemMeta(forwardItemMeta);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("<-");
        backItem.setItemMeta(backItemMeta);

        ItemStack menuItem = new ItemStack(Material.BARRIER);
        ItemMeta menuItemMeta = menuItem.getItemMeta();
        menuItemMeta.setDisplayName("Back to Menu");
        menuItem.setItemMeta(menuItemMeta);

        StaticPane back = new StaticPane(2, 5, 1, 1);
        StaticPane forward = new StaticPane(6, 5, 1, 1);
        back.addItem(new GuiItem(backItem, event -> {
            playerList.setPage(playerList.getPage() - 1);

            if (playerList.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            playersMenu.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(forwardItem, event -> {
            playerList.setPage(playerList.getPage() + 1);

            if (playerList.getPages() <= 1) {
                forward.setVisible(false);
            }

            back.setVisible(true);
            playersMenu.update();
        }), 0, 0);

        if(playerList.getPages() <= 1) {
            forward.setVisible(false);
        }

        StaticPane menu = new StaticPane(4, 0, 1, 1);
        menu.addItem(new GuiItem(menuItem, event -> {
            show(player, town.getName());
        }), 0,0);

        playersMenu.addPane(menu);
        playersMenu.addPane(playerList);
        playersMenu.addPane(forward);
        playersMenu.addPane(back);

        player.sendMessage(playerList.getPages() + "");
        playersMenu.show(player);

    }
}
