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
import net.waals.dogecraft.models.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class TownGui {

    private PaginatedPane pages;
    private PaginatedPane townList;
    private StaticPane townListMenu;
    private StaticPane townMenu;
    private TownManager townManager;
    private DogeCraft plugin;
    private Gui townMainGui;
    private Gui townListGui;
    private ClaimManager claimManager;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;

    public TownGui(TownManager townManager, DogeCraft plugin, ClaimManager claimManager, DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.townManager = townManager;
        this.plugin = plugin;
        this.townMainGui = new Gui(plugin, 1, "Menu");
        this.claimManager = claimManager;
        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
    }

    public void show(Player player) {
        if(economyManager.getPayers().contains(player.getUniqueId().toString())) {
            economyManager.removePayer(player.getUniqueId().toString());
        }
        if(economyManager.getWithdrawers().contains(player.getUniqueId().toString())) {
            economyManager.removeWithdrawer(player.getUniqueId().toString());
        }
        this.townMenu = new StaticPane(0, 0, 9, 1);
        DogePlayer dogePlayer = null;
        try {
            dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
        } catch(Exception e) {

        }
        if(dogePlayer.getRequested() == null && dogePlayer.getTown() == null) {
            ItemStack createTownItem = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta createTownItemMeta = createTownItem.getItemMeta();
            createTownItemMeta.setDisplayName("Create a Town");
            createTownItemMeta.setLore(Arrays.asList("Cost: 500 DOGE"));
            createTownItem.setItemMeta(createTownItemMeta);

            ItemStack joinTownItem = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta joinTownItemMeta = joinTownItem.getItemMeta();
            joinTownItemMeta.setDisplayName("Join a town");
            joinTownItem.setItemMeta(joinTownItemMeta);


            DogePlayer finalDogePlayer1 = dogePlayer;
            townMenu.addItem(new GuiItem(createTownItem, event -> {
                player.closeInventory();
                if(player.getWorld().equals(Bukkit.getWorld("world"))) {
                    if(economyManager.checkIfFundsAvailable(finalDogePlayer1, 500)) {
                        claimManager.startClaim(player, Claim.Type.TOWN_CREATION);
                    } else {
                        player.sendMessage(ChatColor.RED + ">> You have insufficient funds to do that.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + ">> You can only do this in the overworld.");
                }
            }), 3, 0 );

            townMenu.addItem(new GuiItem(joinTownItem, event -> {
                this.createTownList(player);
                this.townListGui.show(player);
            }),5, 0);

            townMainGui.addPane(townMenu);
        } else if(dogePlayer.getRequested() != null) {
            ItemStack pendingItem = new ItemStack(Material.RED_DYE);
            ItemMeta pendingItemMeta = pendingItem.getItemMeta();
            pendingItemMeta.setDisplayName("Request to " +  dogePlayer.getRequested() + " pending..");
            pendingItemMeta.setLore(Arrays.asList("Click to cancel request"));
            pendingItem.setItemMeta(pendingItemMeta);

            DogePlayer finalDogePlayer = dogePlayer;
            townMenu.addItem(new GuiItem(pendingItem, event -> {
                townManager.getTown(finalDogePlayer.getRequested()).removeRequest(player.getUniqueId().toString());
                finalDogePlayer.setRequested(null);
                dogePlayerManager.storePlayer(finalDogePlayer);
                this.show(player);
            }), 4, 0 );

            townMainGui.addPane(townMenu);
        } else if(dogePlayer.getTown() != null) {
            ItemStack leaveItem = new ItemStack(Material.RED_DYE);
            ItemMeta leaveItemItemMeta = leaveItem.getItemMeta();
            leaveItemItemMeta.setDisplayName("Leave town");
            leaveItemItemMeta.setLore(Arrays.asList(ChatColor.RED + "" + ChatColor.BOLD + "YOU WILL LOSE ALL YOUR PLOTS"));
            leaveItem.setItemMeta(leaveItemItemMeta);

            ItemStack bankItem = new ItemStack(Material.GOLD_INGOT);
            ItemMeta bankItemMeta = bankItem.getItemMeta();
            bankItemMeta.setDisplayName("Pay into town bank");
            bankItem.setItemMeta(bankItemMeta);

            ItemStack shopItem = new ItemStack(Material.CHEST);
            ItemMeta shopItemMeta = shopItem.getItemMeta();
            shopItemMeta.setDisplayName("Create a shop");
            shopItem.setItemMeta(shopItemMeta);

            ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta tpItemMeta = tpItem.getItemMeta();
            tpItemMeta.setDisplayName("Town Warps");
            tpItem.setItemMeta(tpItemMeta);

            ItemStack outpostItem = new ItemStack(Material.BONE);
            ItemMeta outpostItemMeta = outpostItem.getItemMeta();
            outpostItemMeta.setDisplayName("Outpost menu");
            outpostItem.setItemMeta(outpostItemMeta);

            townMenu.addItem(new GuiItem(bankItem, event -> {
                economyManager.addPayer(player.getUniqueId().toString());
                player.sendMessage(ChatColor.GOLD + ">> Type into the chat how much you would like to pay in.");
                player.closeInventory();
            }), 3, 0 );


            DogePlayer finalDogePlayer2 = dogePlayer;
            townMenu.addItem(new GuiItem(leaveItem, event -> {
                player.sendMessage(ChatColor.RED + ">> You left " + finalDogePlayer2.getTown() + ".");
                player.closeInventory();
                try {
                    townManager.leaveTown(dogePlayerManager.getPlayer(player.getUniqueId().toString()));
                    dogePlayerManager.storePlayer(dogePlayerManager.getPlayer(player.getUniqueId().toString()));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }), 5, 0 );

            townMenu.addItem(new GuiItem(shopItem, event -> {
                claimManager.startClaim(player, Claim.Type.SHOP_CREATION);
                player.closeInventory();
            }), 1, 0 );

            townMenu.addItem(new GuiItem(tpItem, event -> {
                createWarpList(player);
                this.townListGui.show(player);
            }), 7, 0 );

            townMainGui.addPane(townMenu);
        }

        ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName("~");
        fillerItem.setItemMeta(fillerItemMeta);

        this.townMenu.fillWith(fillerItem);

        this.townMenu.setOnClick(event -> {
            event.setCancelled(true);
        });

        townMainGui.show(player);
    }

    private void createWarpList(Player player) {
        this.townListGui = new Gui(plugin, 6, "Select a town");
        townList = new PaginatedPane(0, 1, 9, 4);
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
            townMainGui.show(player);
        }), 0,0);

        townListGui.addPane(menu);
        townListGui.addPane(townList);
        townListGui.addPane(back);
        townListGui.addPane(forward);
    }

    private void createTownList(Player player) {
        this.townListGui = new Gui(plugin, 6, "Select a town to join");
        townList = new PaginatedPane(0, 1, 9, 4);
        for(int i = 0; i < townManager.getTowns().size(); i+=36) {
            StaticPane pane = new StaticPane(0,0,9,4);
            for(int j = 0; j < 36; j++) {
                if((i+j) < townManager.getTowns().size()) {
                    Town currentTown = townManager.getTowns().get(i+j);
                    ItemStack item = new ItemStack(Material.GRASS_BLOCK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(currentTown.getName());
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
                        currentTown.addRequest(player.getUniqueId().toString());
                        player.sendMessage(">> Requested " + currentTown.getName());
                        DogePlayer dogePlayer = null;
                        try {
                            dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dogePlayer.setRequested(currentTown.getName());
                        dogePlayerManager.storePlayer(dogePlayer);
                        player.closeInventory();
                    }), x, y);
                } else {
                    break;
                }
            }
            townList.addPane(i/36, pane);
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
            townMainGui.show(player);
        }), 0,0);

        townListGui.addPane(menu);
        townListGui.addPane(townList);
        townListGui.addPane(back);
        townListGui.addPane(forward);

    }
}
