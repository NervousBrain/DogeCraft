package net.waals.dogecraft.listeners;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.managers.*;
import net.waals.dogecraft.models.*;
import net.waals.dogecraft.util.AreaUtil;
import net.waals.dogecraft.util.RandomUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;


import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class PlayerListener implements Listener {

    private ArenaManager arenaManager;
    private DogePlayerManager dogePlayerManager;
    private ClaimManager claimManager;
    private TownManager townManager;
    private AreaUtil areaUtil;
    private EconomyManager economyManager;
    private Location spawn;
    private DogeCraft plugin;

    public PlayerListener(ArenaManager arenaManager, DogePlayerManager dogePlayerManager, ClaimManager claimManager, AreaUtil areaUtil, TownManager townManager, EconomyManager economyManager, DogeCraft plugin) {
        this.arenaManager = arenaManager;
        this.dogePlayerManager = dogePlayerManager;
        this.claimManager = claimManager;
        this.areaUtil = areaUtil;
        this.townManager = townManager;
        this.economyManager = economyManager;
        this.plugin = plugin;
        this.spawn = new Location(Bukkit.getWorld("world"), -258, 64.5, -456, -178.7f, -2.1f);
        Bukkit.getWorld("world").setSpawnLocation(this.spawn);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws ExecutionException, InterruptedException {
        Player player = event.getPlayer();
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
        if(dogePlayer == null) {
            player.teleport(this.spawn);
            dogePlayerManager.loadDogePlayer(dogePlayerManager.createDogePlayer(player.getUniqueId().toString(), player.getName()));
            player.sendMessage(ChatColor.GOLD + ">> Welcome! Have 15 Doge on us :)");
            player.sendMessage(ChatColor.GOLD + ">> Follow the path in front of you to learn more.");
        } else {
            dogePlayer.setName(player.getName());
            dogePlayerManager.storePlayer(dogePlayer);
            dogePlayerManager.loadDogePlayer(dogePlayer);
        }
        if(plugin.getConfig().contains(player.getUniqueId().toString())) {
            ItemStack[] content = new ItemStack[41];
            for (int i = 0; i < 41; i++) {
                content[i] = plugin.getConfig().getItemStack(player.getUniqueId().toString() + ".inv." + i);
            }
            player.getInventory().setContents(content);
            player.teleport(plugin.getConfig().getLocation(player.getUniqueId().toString() + ".loc"));
            plugin.getConfig().set(player.getUniqueId().toString(), null);
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) throws ExecutionException, InterruptedException {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            DogePlayer dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
            if(dogePlayer.isFighting()) {
                if(player.getHealth() <= event.getDamage()) {
                    event.setCancelled(true);
                    dogePlayer.getInArena().getPlayerA().setHealth(10);
                    dogePlayer.getInArena().getPlayerB().setHealth(10);
                    arenaManager.endGame(dogePlayer.getInArena(), player);
                    Firework fw = (Firework) event.getEntity().getLocation().getWorld().spawnEntity(((Player) event.getEntity()).getEyeLocation().add(new Vector(0, -1, 0)), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    fwm.setPower(2);
                    fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(false).build());
                    fw.setFireworkMeta(fwm);
                    fw.detonate();
                }
            }
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) throws ExecutionException, InterruptedException {
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Point point = new Point(event.getEntity().getLocation().getBlockX(), event.getEntity().getLocation().getBlockZ());
            DogePlayer a = dogePlayerManager.getPlayer(event.getDamager().getUniqueId().toString());
            DogePlayer b = dogePlayerManager.getPlayer(event.getEntity().getUniqueId().toString());
            if(!(a.isFighting() && b.isFighting())) {
                for(Town currentTown : townManager.getTowns()) {
                    if(currentTown.hasFlag(Town.Flag.PVP)) {
                        if(townManager.inTown(currentTown.getName(), point)) {
                            event.setCancelled(true);
                        }
                    }
                }
                if(event.getDamager().getWorld().equals(Bukkit.getWorld("world"))) {
                    if(this.spawn.distanceSquared(event.getDamager().getLocation()) <= 20000) {
                        event.setCancelled(true);
                    }
                }
            } else if(event.getDamager() instanceof Player) {
                Plot plot = townManager.getPlotFromPoint(point);
                if(plot != null) {
                    if(!event.getDamager().getUniqueId().toString().equalsIgnoreCase(plot.getOwner())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) throws ExecutionException, InterruptedException {
        Point point = new Point(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SPAWNER &&
        townManager.canManipulate(dogePlayer, point)) {
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
            ItemMeta meta = stack.getItemMeta();
            EntityType type = EntityType.valueOf(meta.getLore().get(0));
            Block block = event.getBlock();
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            spawner.setSpawnedType(type);
            spawner.update();
        }
        point = new Point(event.getBlock().getX(), event.getBlock().getZ());
        if(event.getPlayer().getWorld().equals(Bukkit.getWorld("world"))) {
            if(this.spawn.distanceSquared(event.getBlock().getLocation()) <= 20000) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + ">> Travel a little further from spawn.");
            }
        }
        boolean allowed = true;
        boolean stop = false;
        for(Town currentTown : townManager.getTowns()) {
            if(townManager.inTown(currentTown.getName(), point)) {
                allowed = false;
                if(dogePlayer.getTown() != null) {
                    if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName()) && (dogePlayer.getRole().equalsIgnoreCase("OWNER") || (dogePlayer.getRole().equalsIgnoreCase("STAFF")))) {
                        stop = true;
                        allowed = true;
                        for(Plot plot : currentTown.getPlots()) {
                            if(areaUtil.inArea(plot.getArea(), point)) {
                                if(plot.getOwner() != null) {
                                    if(!plot.getOwner().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                                        allowed = false;
                                        stop = true;
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    } else if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName())) {
                        for(Plot plot : currentTown.getPlots()) {
                            if(plot.getOwner() != null) {
                                if(plot.getOwner().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                                    if(areaUtil.inArea(plot.getArea(), point)) {
                                        allowed = true;
                                        stop = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(stop) break;
                    }
                }
            }
        }
        if(!allowed) {
            event.setCancelled(true);
        } else if(event.getBlock().getType().equals(Material.CHEST)) {
            Point point1 = new Point(event.getBlock().getX()-1, event.getBlock().getY(), event.getBlock().getZ());
            Point point2 = new Point(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()-1);
            Point point3 = new Point(event.getBlock().getX()+1, event.getBlock().getY(), event.getBlock().getZ());
            Point point4 = new Point(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()+1);
            if(!(townManager.getShopFromPoint(point1) == null && townManager.getShopFromPoint(point2) == null
            && townManager.getShopFromPoint(point3) == null && townManager.getShopFromPoint(point4) == null)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + ">> You can't place a chest here.");
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        for(String stack : townManager.getShopUtil().getStacks()) {
            if(event.getItem().equals(stack)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if(event.getInventory().getType().equals(InventoryType.CHEST)) {
            Location loc = event.getInventory().getLocation();
            if(loc != null) {
                Point point = new Point(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                Shop shop = townManager.getShopFromPoint(point);
                if(shop != null) {
                    boolean violation = false;
                    for(ItemStack itemStack : event.getInventory().getContents()) {
                        ItemStack selling = townManager.getShopUtil().buildItemStack(shop.getItem());
                        if(!selling.isSimilar(itemStack) && itemStack != null) {
                            violation = true;
                            event.getPlayer().getWorld().dropItemNaturally(loc, itemStack);
                            event.getInventory().remove(itemStack);
                        }
                    }
                    if(violation) {
                        event.getPlayer().sendMessage(ChatColor.RED + ">> You can only place items in the chest that you are selling.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) throws Exception {
        Player player = event.getPlayer();
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
        Claim claim = claimManager.getClaimFromPlayer(player);
        String message = event.getMessage();
        if(claim != null) {
            if(event.getMessage().equalsIgnoreCase("abort")) {
                claimManager.endClaim(claim);
                player.sendMessage(ChatColor.GOLD + ">> Aborted!");
                event.setCancelled(true);
            }
            if(claim.getType() == Claim.Type.TOWN_CREATION) {
                Claim.Stage stage = claim.getStage();
                switch (stage) {
                    case NAMING:
                        boolean allowed = true;
                        try {
                            townManager.checkName(event.getMessage());
                        } catch (Exception e) {
                            allowed = false;
                            player.sendMessage(ChatColor.RED + ">> " + e.getMessage());
                        }
                        if(allowed) {
                            claim.setName(event.getMessage());
                            claim.setStage(Claim.Stage.NAMING_VERIFICATION);
                            event.setCancelled(true);
                        }
                        break;
                    case NAMING_VERIFICATION:
                        if(message.equalsIgnoreCase("confirm")) {
                            claim.setStage(Claim.Stage.LAND_CLAIM);
                            event.setCancelled(true);
                        }
                        break;
                    case LAND_CLAIM_VERIFICAITON:
                        if(message.equalsIgnoreCase("redo")) {
                            areaUtil.stopHighlight(claim.getArea());
                            claim.setStage(Claim.Stage.LAND_CLAIM);
                            event.setCancelled(true);
                        }
                        if(message.equalsIgnoreCase("confirm")) {
                            if(economyManager.checkIfFundsAvailable(dogePlayer, 500)) {
                                townManager.createTown(claim.getName(), claim.getArea(), player);
                                dogePlayer.setRole("OWNER");
                                dogePlayer.setTown(claim.getName());
                                areaUtil.stopHighlight(claim.getArea());
                                economyManager.deduct(dogePlayer, 500);
                                dogePlayerManager.storePlayer(dogePlayer);
                            } else {
                                player.sendMessage(ChatColor.RED + ">> You have insufficient Funds to do this!");
                                player.sendMessage(ChatColor.RED + ">> Town creation cancelled!");
                            }

                            claimManager.endClaim(claim);
                            event.setCancelled(true);
                        }
                        break;

                }
            } else if(claim.getType() == Claim.Type.OUTPOST_CREATION && claim.getStage() == Claim.Stage.LAND_CLAIM_VERIFICAITON) {
                if(message.equalsIgnoreCase("redo")) {
                    areaUtil.stopHighlight(claim.getArea());
                    claim.setStage(Claim.Stage.LAND_CLAIM);
                    event.setCancelled(true);
                }
                if(message.equalsIgnoreCase("confirm")) {
                    if(player.hasPermission("dogecraft.donor")) {
                        townManager.createOutpost(player.getUniqueId().toString(), claim.getArea());
                        player.sendMessage(ChatColor.GOLD + ">> Outpost created.");
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Only donors can do this!");
                    }
                    areaUtil.stopHighlight(claim.getArea());
                    claimManager.endClaim(claim);
                    event.setCancelled(true);
                }
            } else if(claim.getType() == Claim.Type.PLOT_CREATION) {
                Claim.Stage stage = claim.getStage();
                switch (stage) {
                    case LAND_CLAIM_VERIFICAITON:
                        if(message.equalsIgnoreCase("redo")) {
                            areaUtil.stopHighlight(claim.getArea());
                            claim.setStage(Claim.Stage.LAND_CLAIM);
                            event.setCancelled(true);
                        }
                        if(message.equalsIgnoreCase("confirm")) {
                            claim.setStage(Claim.Stage.SET_RENT);
                            areaUtil.stopHighlight(claim.getArea());
                            event.setCancelled(true);
                        }
                        break;
                    case SET_RENT:
                        if(NumberUtils.isCreatable(event.getMessage())) {
                            claim.setRent(NumberUtils.toDouble(event.getMessage()));
                            claim.setStage(Claim.Stage.SIGN_PLACEMENT);
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Please type a number.");
                        }
                        event.setCancelled(true);
                        break;
                }
            } else if(claim.getType() == Claim.Type.TOWN_EXTENSION) {
                Claim.Stage stage = claim.getStage();
                switch (stage) {
                    case LAND_CLAIM_VERIFICAITON:
                        if(message.equalsIgnoreCase("redo")) {
                            areaUtil.stopHighlight(claim.getArea());
                            claim.setStage(Claim.Stage.LAND_CLAIM);
                            event.setCancelled(true);
                        }
                        if(message.equalsIgnoreCase("confirm")) {
                            if(economyManager.checkIfFundsAvailable(townManager.getTown(dogePlayer.getTown()), 50)) {
                                townManager.getTown(dogePlayer.getTown()).getAreas().add(claim.getArea());
                                townManager.getTown(dogePlayer.getTown()).deduct(50);
                                townManager.stopHighlighting(townManager.getTown(dogePlayer.getTown()));
                                player.sendMessage(ChatColor.GOLD + ">> Town extended.");
                            } else {
                                player.sendMessage(ChatColor.RED + ">> Your town has insufficient Funds to do this!");
                                player.sendMessage(ChatColor.RED + ">> Town extension cancelled!");
                            }
                            areaUtil.stopHighlight(claim.getArea());
                            claimManager.endClaim(claim);
                            event.setCancelled(true);
                        }
                        break;
                }
            } else if(claim.getType() == Claim.Type.SHOP_CREATION) {
                Claim.Stage stage = claim.getStage();
                switch (stage) {
                    case SET_SELL_PRICE:
                        if(NumberUtils.isCreatable(event.getMessage())) {
                            if(NumberUtils.toDouble(event.getMessage()) >= 0) {
                                claim.setSellPrice(NumberUtils.toDouble(event.getMessage()));
                                claim.setStage(Claim.Stage.SET_BUY_PRICE);
                            } else {
                                player.sendMessage(ChatColor.RED + ">> Number must be >0.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Please enter a valid number.");
                        }
                        event.setCancelled(true);
                        break;
                    case SET_BUY_PRICE:
                        if(NumberUtils.isCreatable(event.getMessage())) {
                            if(NumberUtils.toDouble(event.getMessage()) >= 0) {
                                claim.setBuyPrice(NumberUtils.toDouble(event.getMessage()));
                                Shop shop = new Shop(event.getPlayer().getUniqueId().toString(), claim.getShopLocation(), claim.getItem(), claim.getSellPrice(), claim.getBuyPrice());
                                Plot plot = townManager.getPlotFromPoint(claim.getShopLocation());
                                plot.addShop(shop);
                                player.sendMessage(ChatColor.GOLD + ">> You are selling " + shop.getItem().getMaterial() + " for " + shop.getSellPrice() + " DOGE.");
                                townManager.getShopUtil().addShopCosmetics(shop);
                                claimManager.endClaim(claim);
                            } else {
                                player.sendMessage(ChatColor.RED + ">> Number must be >0.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Please enter a valid number.");
                        }
                        event.setCancelled(true);
                        break;
                }
            } else if(claim.getType() == Claim.Type.ARENA_CREATION) {
                switch(claim.getStage()) {
                    case LAND_CLAIM_VERIFICAITON:
                        if(message.equalsIgnoreCase("redo")) {
                            areaUtil.stopHighlight(claim.getArea());
                            claim.setStage(Claim.Stage.LAND_CLAIM);
                            event.setCancelled(true);
                        }
                        if(message.equalsIgnoreCase("confirm")) {
                            claim.setStage(Claim.Stage.SET_SPAWN_A);
                            event.setCancelled(true);
                        }
                        break;
                    case SET_WAGER:
                        if(NumberUtils.isCreatable(event.getMessage())) {
                            if(NumberUtils.toDouble(event.getMessage()) >= 0) {
                                double amount = NumberUtils.toDouble(event.getMessage());
                                claim.setWager(amount);
                                player.sendMessage(ChatColor.GOLD + ">> Set the wager to " + claim + " DOGE.");
                                claim.setStage(Claim.Stage.SIGN_PLACEMENT);
                            } else {
                                player.sendMessage(ChatColor.RED + ">> Number must be >0.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Please enter a valid number.");
                        }
                        event.setCancelled(true);
                        break;
                }
            }
        } else {
            if(economyManager.getPayers().contains(player.getUniqueId().toString())) {
                if(NumberUtils.isCreatable(event.getMessage())) {
                    double amount = NumberUtils.toDouble(event.getMessage());
                    if(amount > 0) {
                        if(economyManager.checkIfFundsAvailable(dogePlayer, amount)) {
                            townManager.getTown(dogePlayer.getTown()).deposit(amount);
                            economyManager.deduct(dogePlayer, amount);
                            dogePlayerManager.storePlayer(dogePlayer);
                            player.sendMessage(ChatColor.GOLD + "You've payed " + amount + " DOGE into the town bank.");
                        } else {
                            player.sendMessage(ChatColor.RED + ">> You've got insufficient funds. Cancelling..");
                        }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Not a valid number. Cancelling..");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Not a valid number. Cancelling..");
                    }
                economyManager.removePayer(player.getUniqueId().toString());
            } else if(economyManager.getWithdrawers().contains(player.getUniqueId().toString())) {
                if(NumberUtils.isCreatable(event.getMessage())) {
                    double amount = NumberUtils.toDouble(event.getMessage());
                    if(amount > 0) {
                        if(economyManager.checkIfFundsAvailable(townManager.getTown(dogePlayer.getTown()), amount)) {
                            townManager.getTown(dogePlayer.getTown()).deduct(amount);
                            economyManager.deposit(dogePlayer, amount);
                            dogePlayerManager.storePlayer(dogePlayer);
                            player.sendMessage(ChatColor.GOLD + "You've withdrawn " + amount + " DOGE into the town bank.");
                        } else {
                            player.sendMessage(ChatColor.RED + ">> The towns got insufficient funds. Cancelling..");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Not a valid number. Cancelling..");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + ">> Not a valid number. Cancelling..");
                }
                economyManager.removeWithdrawer(player.getUniqueId().toString());
            }
            if (!event.isCancelled()){
                if(event.getMessage().startsWith("!")) {
                    if(dogePlayer.getTown() != null) {
                        if(dogePlayer.isDonor()) {
                            if(player.isOp()) {
                                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    DogePlayer dp = dogePlayerManager.getPlayer(p.getUniqueId().toString());
                                    if(dp.getTown().equalsIgnoreCase(dogePlayer.getTown())) {
                                       p.sendMessage(ChatColor.GOLD + "[" + dogePlayer.getTown() + "] " + ChatColor.RED + player.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage().substring(1));
                                    }
                                }

                            } else {
                                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    DogePlayer dp = dogePlayerManager.getPlayer(p.getUniqueId().toString());
                                    if(dp.getTown().equalsIgnoreCase(dogePlayer.getTown())) {
                                        p.sendMessage(ChatColor.GOLD + "[" + dogePlayer.getTown() + "] " + ChatColor.GREEN + player.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage().substring(1));
                                    }
                                }
                            }
                        } else {
                            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                                DogePlayer dp = dogePlayerManager.getPlayer(p.getUniqueId().toString());
                                if(dp.getTown().equalsIgnoreCase(dogePlayer.getTown())) {
                                    p.sendMessage(ChatColor.GOLD + "[" + dogePlayer.getTown() + "] " + ChatColor.WHITE + player.getDisplayName() + ": " + event.getMessage().substring(1));
                                }
                            }
                        }

                    } else {
                        player.sendMessage(ChatColor.RED + ">> Only citizens can do that.");
                    }
                } else {
                    if(dogePlayer.isDonor()) {
                        if(player.isOp()) {
                            Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage());
                        } else {
                            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage());
                        }
                    } else {
                        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getDisplayName() + ": " + event.getMessage());
                    }
                }

            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) throws ExecutionException, InterruptedException, IOException {
        Claim claim = claimManager.getClaimFromPlayer(event.getPlayer());
        if(claim != null) {
            DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
            if(claim.getType() == Claim.Type.TOWN_CREATION) {
                if(claim.getStage() == Claim.Stage.LAND_CLAIM) {
                    if(event.getAction() == Action.LEFT_CLICK_BLOCK &&
                            event.getClickedBlock() != null) {
                        Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                        if (claim.getP1() == null) {
                            claim.setP1(point);
                        } else if (claim.getP2() == null) {
                            claim.setP2(point);
                            claim.setArea();
                            if (townManager.checkIfFree(claim.getArea())) {
                                int xSize = claim.getArea().getDistance().getX();
                                int zSize = claim.getArea().getDistance().getZ();
                                if((xSize >= 50 && xSize <= 100) && (zSize >= 50 && zSize <= 100)) {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> Invalid size! The town area must be at least 50x50 and no more than 100x100.");
                                    claim.setStage(Claim.Stage.LAND_CLAIM);
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.GOLD + ">> Are you sure? Type confirm or redo");
                                    areaUtil.highlightArea(claim.getArea(), event.getPlayer());
                                    claim.setStage(Claim.Stage.LAND_CLAIM_VERIFICAITON);
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> This area overlaps at least partly with an established town. " +
                                        "Please select a new area.");
                                claim.setStage(Claim.Stage.LAND_CLAIM);
                            }

                        }
                        event.setCancelled(true);
                    }
                }
            } else if(claim.getType() == Claim.Type.PLOT_CREATION) {
                if(claim.getStage() == Claim.Stage.LAND_CLAIM) {
                    if(event.getAction() == Action.LEFT_CLICK_BLOCK &&
                            event.getClickedBlock() != null) {
                        Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                        if (claim.getP1() == null) {
                            claim.setP1(point);
                        } else if (claim.getP2() == null) {
                            claim.setP2(point);
                            claim.setArea();
                            if (townManager.checkIfPlotAllowed(claim.getArea(), dogePlayer.getTown())) {
                                event.getPlayer().sendMessage(ChatColor.GOLD + ">> Are you sure? Type confirm or redo");
                                areaUtil.highlightArea(claim.getArea(), event.getPlayer());
                                claim.setStage(Claim.Stage.LAND_CLAIM_VERIFICAITON);
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> The plot must be completely within in the perimeter of your town and is not allowed to overlap other plots. " +
                                        "Please select a new area.");
                                claim.setStage(Claim.Stage.LAND_CLAIM);
                            }

                        }
                        event.setCancelled(true);
                }
            } else if(claim.getType() == Claim.Type.PLOT_CREATION && claim.getStage() == Claim.Stage.SIGN_PLACEMENT) {
                    if(event.getAction() == Action.LEFT_CLICK_BLOCK &&
                            event.getClickedBlock() != null) {
                        if(event.getClickedBlock().getType() == Material.SPRUCE_SIGN ||
                                event.getClickedBlock().getType() == Material.SPRUCE_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.ACACIA_SIGN ||
                                event.getClickedBlock().getType() == Material.ACACIA_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.BIRCH_SIGN ||
                                event.getClickedBlock().getType() == Material.BIRCH_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.DARK_OAK_SIGN ||
                                event.getClickedBlock().getType() == Material.DARK_OAK_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.JUNGLE_SIGN ||
                                event.getClickedBlock().getType() == Material.JUNGLE_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.OAK_SIGN ||
                                event.getClickedBlock().getType() == Material.OAK_WALL_SIGN
                        ) {
                            Point signLocation = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                            townManager.createPlot(dogePlayer.getTown(), dogePlayer.getUuid(), claim.getArea(), claim.getRent(), signLocation);
                            event.getPlayer().sendMessage(ChatColor.GOLD + ">> Plot successfully created!");
                            claimManager.endClaim(claim);
                            Sign sign = (Sign) event.getClickedBlock().getState();
                            sign.setLine(0, ChatColor.DARK_GREEN + "[RENT]");
                            sign.setLine(1, claim.getRent() + "" + ChatColor.GOLD + " Ð");
                            sign.setLine(2, "Right-click");
                            sign.setLine(3, "to rent");
                            sign.update();
                        }
                    }
                }
                event.setCancelled(true);
            } else if(claim.getType() == Claim.Type.TOWN_EXTENSION) {
                if(claim.getStage() == Claim.Stage.LAND_CLAIM) {
                    if(event.getAction() == Action.LEFT_CLICK_BLOCK &&
                            event.getClickedBlock() != null) {
                        Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                        if (claim.getP1() == null) {
                            claim.setP1(point);
                        } else if (claim.getP2() == null) {
                            claim.setP2(point);
                            claim.setArea();
                            if (townManager.checkIfExtensionAllowed(dogePlayer.getTown(), claim.getArea())) {
                                int xSize = claim.getArea().getDistance().getX();
                                int zSize = claim.getArea().getDistance().getZ();
                                if((xSize >= 25 && xSize <= 50) && (zSize >= 25 && zSize <= 50)) {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> Invalid size! The area must be at least 25x25 and no more than 50x50.");
                                    claim.setStage(Claim.Stage.LAND_CLAIM);
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.GOLD + ">> Are you sure? Type confirm or redo");
                                    areaUtil.highlightArea(claim.getArea(), event.getPlayer());
                                    claim.setStage(Claim.Stage.LAND_CLAIM_VERIFICAITON);
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> The area must overlap with your own town but is not allowed to overlap with a different town. #IKnowItSeemsConvoluted");
                                claim.setStage(Claim.Stage.LAND_CLAIM);
                            }

                        }
                        event.setCancelled(true);
                    }
                }
            } else if(claim.getType() == Claim.Type.SHOP_CREATION && claim.getStage().equals(Claim.Stage.SET_CHEST)) {
                if(event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.CHEST)) {
                    ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                    Chest chest = (Chest) event.getClickedBlock().getState();
                    if(!(chest instanceof DoubleChest)) {
                        boolean damaged = false;
                        if(hand.getItemMeta() instanceof Damageable) {
                            Damageable damageable = (Damageable) hand.getItemMeta();
                            damaged = damageable.getDamage() > 0;
                        }
                        if(!(hand.getType().equals(Material.AIR) || hand == null) && !damaged) {
                            Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                            if(townManager.checkIfRentedByDogePlayer(dogePlayer, point)) {
                                if(townManager.getShopFromPoint(point) == null) {
                                    claim.setStage(Claim.Stage.SET_SELL_PRICE);
                                    claim.setShopLocation(point);
                                    claim.setItem(townManager.getShopUtil().itemStackToShopItem(hand));
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> This chest is already in use.");
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> You can only create a shop in a plot rented by yourself.");
                            }
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + ">> You can't sell air. And your item is not allowed to be damaged.");
                        }
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + ">> You can only use single chests.");
                    }


                } else if(event.getClickedBlock() != null && !event.getClickedBlock().getType().equals(Material.CHEST)) {
                    claimManager.endClaim(claim);
                    event.getPlayer().sendMessage(ChatColor.RED + ">> This is not a chest. Cancelling..");
                }

                event.setCancelled(true);
            } else if(claim.getType() == Claim.Type.OUTPOST_CREATION) {
                if(claim.getStage() == Claim.Stage.LAND_CLAIM) {
                    if(event.getAction() == Action.LEFT_CLICK_BLOCK &&
                            event.getClickedBlock() != null) {
                        Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                        if (claim.getP1() == null) {
                            claim.setP1(point);
                        } else if (claim.getP2() == null) {
                            claim.setP2(point);
                            claim.setArea();
                            if (townManager.checkIfFree(claim.getArea())) {
                                int xSize = claim.getArea().getDistance().getX();
                                int zSize = claim.getArea().getDistance().getZ();
                                if((xSize >= 25 && xSize <= 100) && (zSize >= 25 && zSize <= 100)) {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> Invalid size! The area must be at least 25x25 and no more than 50x50.");
                                    claim.setStage(Claim.Stage.LAND_CLAIM);
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.GOLD + ">> Are you sure? Type confirm or redo");
                                    areaUtil.highlightArea(claim.getArea(), event.getPlayer());
                                    claim.setStage(Claim.Stage.LAND_CLAIM_VERIFICAITON);
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> The area must not overlap with an established town or outpost.");
                                claim.setStage(Claim.Stage.LAND_CLAIM);
                            }

                        }
                        event.setCancelled(true);
                    }
                }
            } else if(claim.getType() == Claim.Type.ARENA_CREATION && event.getAction() == Action.LEFT_CLICK_BLOCK &&
                    event.getClickedBlock() != null) {
                Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                event.setCancelled(true);
                switch (claim.getStage()) {
                    case LAND_CLAIM:
                        if (claim.getP1() == null) {
                            claim.setP1(point);
                        } else if (claim.getP2() == null) {
                            claim.setP2(point);
                            claim.setArea();
                            if (townManager.checkIfPlotAllowed(claim.getArea(), dogePlayer.getTown())) {
                                int xSize = claim.getArea().getDistance().getX();
                                int zSize = claim.getArea().getDistance().getZ();
                                if((xSize >= 25 && xSize <= 50) && (zSize >= 25 && zSize <= 50)) {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> Invalid size! The arena must be at least 25x25 and no more than 50x50.");
                                    claim.setStage(Claim.Stage.LAND_CLAIM);
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.GOLD + ">> Are you sure? Type confirm or redo");
                                    areaUtil.highlightArea(claim.getArea(), event.getPlayer());
                                    claim.setStage(Claim.Stage.LAND_CLAIM_VERIFICAITON);
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + ">> The area must not overlap with an established town or outpost.");
                                claim.setStage(Claim.Stage.LAND_CLAIM);
                            }

                        }
                        break;
                    case SET_SPAWN_A:
                        if(areaUtil.inArea(claim.getArea(), point)) {
                            Point spawnA = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                            claim.setSpawnA(spawnA);
                            event.getPlayer().sendMessage(ChatColor.GOLD + ">> First spawn point set!");
                            claim.setStage(Claim.Stage.SET_SPAWN_B);
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + ">> Must be within the arena.");
                        }
                        break;
                    case SET_SPAWN_B:
                        if(areaUtil.inArea(claim.getArea(), point)) {
                            Point spawnB = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                            claim.setSpawnB(spawnB);
                            event.getPlayer().sendMessage(ChatColor.GOLD + ">> Second spawn point set!");
                            claim.setStage(Claim.Stage.SET_WAGER);
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + ">> Must be within the arena.");
                        }
                        break;
                    case SIGN_PLACEMENT:
                        if(event.getClickedBlock().getType() == Material.SPRUCE_SIGN ||
                                event.getClickedBlock().getType() == Material.SPRUCE_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.ACACIA_SIGN ||
                                event.getClickedBlock().getType() == Material.ACACIA_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.BIRCH_SIGN ||
                                event.getClickedBlock().getType() == Material.BIRCH_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.DARK_OAK_SIGN ||
                                event.getClickedBlock().getType() == Material.DARK_OAK_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.JUNGLE_SIGN ||
                                event.getClickedBlock().getType() == Material.JUNGLE_WALL_SIGN ||
                                event.getClickedBlock().getType() == Material.OAK_SIGN ||
                                event.getClickedBlock().getType() == Material.OAK_WALL_SIGN
                        ) {
                            Point signLocation = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                            Arena arena = arenaManager.createArena(claim.getArea(), claim.getSpawnA(), claim.getSpawnB(), claim.getWager(), signLocation, townManager.getTown(dogePlayer.getTown()));
                            /*Sign sign = (Sign) areaUtil.pointToLocation(signLocation).getBlock().getState();
                            sign.setLine(0, ChatColor.DARK_GREEN + "[ARENA]");
                            sign.setLine(1, ChatColor.GOLD + "Wager: " + arena.getAmount() + "Đ");
                            sign.setLine(2, arena.getQueue().size() + " player(s)");
                            sign.setLine(3, "in Queue");
                            sign.update();*/
                            arenaManager.updateSign(arena);
                            event.getPlayer().sendMessage(ChatColor.GOLD + ">> Arena created!");
                            claimManager.endClaim(claim);
                        }
                        break;
                }
                event.setCancelled(true);
            }
        }

        if(event.getAction().equals(Action.PHYSICAL)) {
            Point point = new Point(event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getY(), event.getClickedBlock().getLocation().getBlockZ());
            if(!townManager.canManipulate(dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString()), point)) {
                event.setCancelled(true);
            }
        }

        if((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && event.getClickedBlock() != null) {
            if(event.getClickedBlock().getType() == Material.CHEST) {
                DogePlayer dogePlayer = null;
                Chest chest = (Chest) event.getClickedBlock().getState();
                dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
                Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                for(Town currentTown : townManager.getTowns()) {
                    for(Plot currentPlot : currentTown.getPlots()) {
                        if(areaUtil.inArea(currentPlot.getArea(), point)) {
                            if(currentPlot.getOwner() != null) {
                                if(!currentPlot.getOwner().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                                    point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                                    Shop shop = townManager.getShopFromPointAndPlot(currentPlot, point);
                                    if(shop != null) {
                                        DogePlayer seller = dogePlayerManager.getPlayer(shop.getDogePlayer());
                                        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && shop.getSellPrice() != 0) {
                                            if(economyManager.checkIfFundsAvailable(dogePlayer, shop.getSellPrice())) {
                                                if(event.getPlayer().getInventory().firstEmpty() != -1) {
                                                    if(townManager.getShopUtil().removeItems(chest.getBlockInventory(), townManager.getShopUtil().buildItemStack(shop.getItem()))) {
                                                        event.getPlayer().getInventory().addItem(townManager.getShopUtil().buildItemStack(shop.getItem()));
                                                        economyManager.payUuid(dogePlayer.getUuid(), shop.getDogePlayer(), shop.getSellPrice());
                                                        event.getPlayer().sendMessage(ChatColor.GOLD + ">> You bought " + shop.getItem().getAmount() + "x " + shop.getItem().getMaterial() + " for " + shop.getSellPrice() + " DOGE.");
                                                    } else {
                                                        event.getPlayer().sendMessage(ChatColor.RED + ">> Sold out.");
                                                    }
                                                } else {
                                                    event.getPlayer().sendMessage(ChatColor.RED + ">> Your inventory is full.");
                                                }
                                            } else {
                                                event.getPlayer().sendMessage(ChatColor.RED + ">> You have insufficient Funds.");
                                            }
                                        } else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) && shop.getBuyPrice() != 0) {
                                            if(economyManager.checkIfFundsAvailable(seller, shop.getBuyPrice())) {
                                                if(chest.getBlockInventory().firstEmpty() != -1) {
                                                    if(townManager.getShopUtil().removeItems(event.getPlayer().getInventory(), townManager.getShopUtil().buildItemStack(shop.getItem()))) {
                                                        chest.getBlockInventory().addItem(townManager.getShopUtil().buildItemStack(shop.getItem()));
                                                        economyManager.payUuid(shop.getDogePlayer(), dogePlayer.getUuid(), shop.getBuyPrice());
                                                        event.getPlayer().sendMessage(ChatColor.GOLD + ">> You sold " + shop.getItem().getAmount() + "x " + shop.getItem().getMaterial() + " for " + shop.getBuyPrice() + " DOGE.");
                                                    } else {
                                                        event.getPlayer().sendMessage(ChatColor.RED + ">> You don't have enough of " + shop.getItem().getMaterial() + " on you");
                                                    }
                                                } else {
                                                    event.getPlayer().sendMessage(ChatColor.RED + ">> The shop is full.");
                                                }
                                            } else {
                                                event.getPlayer().sendMessage(ChatColor.RED + ">> Seller has insufficient funds.");
                                            }
                                        }

                                    }
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            } else if(event.getClickedBlock().getType().equals(Material.DARK_OAK_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.ACACIA_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.BIRCH_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.JUNGLE_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.OAK_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.SPRUCE_DOOR) ||
                    event.getClickedBlock().getType().equals(Material.DARK_OAK_TRAPDOOR) ||
                    event.getClickedBlock().getType().equals(Material.ACACIA_TRAPDOOR) ||
                    event.getClickedBlock().getType().equals(Material.BIRCH_TRAPDOOR) ||
                    event.getClickedBlock().getType().equals(Material.JUNGLE_TRAPDOOR) ||
                    event.getClickedBlock().getType().equals(Material.OAK_TRAPDOOR) ||
                    event.getClickedBlock().getType().equals(Material.SPRUCE_TRAPDOOR)
            ) {
                Point point = new Point(event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getY(), event.getClickedBlock().getLocation().getBlockZ());
                Plot plot = townManager.getPlotFromPoint(point);
                if(plot != null) {
                    if(!event.getPlayer().getUniqueId().toString().equalsIgnoreCase(plot.getOwner())) {
                        event.setCancelled(true);
                    }
                }
            }
            if(event.getClickedBlock().getType() == Material.SPRUCE_SIGN ||
                    event.getClickedBlock().getType() == Material.SPRUCE_WALL_SIGN ||
                    event.getClickedBlock().getType() == Material.ACACIA_SIGN ||
                    event.getClickedBlock().getType() == Material.ACACIA_WALL_SIGN ||
                    event.getClickedBlock().getType() == Material.BIRCH_SIGN ||
                    event.getClickedBlock().getType() == Material.BIRCH_WALL_SIGN ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_SIGN ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_WALL_SIGN ||
                    event.getClickedBlock().getType() == Material.JUNGLE_SIGN ||
                    event.getClickedBlock().getType() == Material.JUNGLE_WALL_SIGN ||
                    event.getClickedBlock().getType() == Material.OAK_SIGN ||
                    event.getClickedBlock().getType() == Material.OAK_WALL_SIGN
            ) {
                Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
                Plot plot = null;
                Town town = null;
                for(Town currentTown : townManager.getTowns()) {
                    for(Plot currentPlot : currentTown.getPlots()) {
                        if(currentPlot.getSignLocation().isEqual(point)) {
                            plot = currentPlot;
                            town = currentTown;
                            break;
                        }
                    }
                    if(plot != null) break;
                }
                Arena arena = null;
                for(Town currentTown : townManager.getTowns()) {
                    for(Arena currentArena : currentTown.getArenas()) {
                        if(currentArena.getSignLocation().isEqual(point)) {
                            arena = currentArena;
                            town = currentTown;
                            break;
                        }
                    }
                    if(plot != null) break;
                }
                DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
                if(plot != null) {
                    if(dogePlayer.getTown() != null) {
                        if(dogePlayer.getTown().equalsIgnoreCase(town.getName())) {
                            Sign sign = (Sign) event.getClickedBlock().getState();
                            if(plot.getOwner() == null || plot.getOwner().equalsIgnoreCase("")) {
                                try {
                                    economyManager.deduct(dogePlayer, plot.getRent());
                                    dogePlayerManager.storePlayer(dogePlayer);
                                    town.deposit(plot.getRent());
                                    plot.setOwner(dogePlayer.getUuid());
                                    plot.setLastCollection(new Date());
                                    sign.setLine(0,"");
                                    sign.setLine(1, ChatColor.DARK_BLUE + dogePlayer.getName());
                                    sign.setLine(2, "");
                                    sign.setLine(3, "");
                                    sign.update();
                                } catch (Exception e) {
                                    event.getPlayer().sendMessage(ChatColor.RED + ">> You have insufficient funds!");
                                }
                            } else {
                                if(plot.getOwner().equalsIgnoreCase(dogePlayer.getUuid())) {
                                    townManager.clearPlot(plot);
                                    event.getPlayer().sendMessage(ChatColor.GOLD + ">> You've stopped renting this plot.");
                                }
                            }
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + ">> You are not a citizen of this town!");
                        }
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + ">> You are not a citizen of this town!");
                    }
                } else if(arena != null) {
                    if(dogePlayer.getInArena() != null) {
                        if(dogePlayer.getInArena().equals(arena)) {
                            dogePlayer.setInArena(null);
                            arena.getQueue().remove(dogePlayer.getUuid());
                            event.getPlayer().sendMessage(ChatColor.GOLD + ">> You left this arena.");
                        } else {
                            dogePlayer.getInArena().getQueue().remove(dogePlayer.getUuid());
                            arena.getQueue().add(dogePlayer.getUuid());
                            arenaManager.updateSign(dogePlayer.getInArena());
                            dogePlayer.setInArena(arena);
                            arenaManager.checkIfGameCanStart(arena);
                        }
                    } else {
                        arena.getQueue().add(dogePlayer.getUuid());
                        dogePlayer.setInArena(arena);
                        event.getPlayer().sendMessage(ChatColor.GOLD + ">> You've joined this arena.");
                        arenaManager.checkIfGameCanStart(arena);
                    }
                    arenaManager.updateSign(arena);
                }
            }
            DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
            Point manPoint = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
            if(!townManager.canManipulate(dogePlayer, manPoint)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(!event.isBedSpawn()) {
            event.getPlayer().teleport(this.spawn);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) throws ExecutionException, InterruptedException {
        Player player = event.getPlayer();
        if(player.getWorld().equals(Bukkit.getWorld("world"))) {
            if(this.spawn.distanceSquared(event.getBlock().getLocation()) <= 20000) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + ">> Travel a little further from spawn.");
            }
        }
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
        Point point = new Point(event.getBlock().getX(), event.getBlock().getZ());
        boolean allowed = true;
        boolean stop = false;
        for(Town currentTown : townManager.getTowns()) {
            if(townManager.inTown(currentTown.getName(), point)) {
                allowed = false;
                if(dogePlayer.getTown() != null) {
                    if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName()) && (dogePlayer.getRole().equalsIgnoreCase("OWNER") || (dogePlayer.getRole().equalsIgnoreCase("STAFF")))) {
                        stop = true;
                        allowed = true;
                        for(Plot plot : currentTown.getPlots()) {
                            if(areaUtil.inArea(plot.getArea(), point)) {
                                if(plot.getOwner() != null) {
                                    if(!plot.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                                        allowed = false;
                                        stop = true;
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    } else if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName())) {
                        for(Plot plot : currentTown.getPlots()) {
                            if(plot.getOwner() != null) {
                                if(plot.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                                    if(areaUtil.inArea(plot.getArea(), point)) {
                                        allowed = true;
                                        stop = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(stop) break;
                    }
                }
            }
        }
        if(!allowed) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) throws ExecutionException, InterruptedException {
        Player player = event.getPlayer();
        Point point = new Point(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
        if(player != null) {
            if(player.getWorld().equals(Bukkit.getWorld("world"))) {
                if(this.spawn.distanceSquared(event.getBlock().getLocation()) <= 20000) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + ">> Travel a little further from spawn.");
                }
            }
            DogePlayer dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
            if(event.getBlock().getType().equals(Material.DIAMOND_ORE) &&
                    !player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) &&
                    !player.getInventory().getItemInOffHand().containsEnchantment(Enchantment.SILK_TOUCH)
            ) {
                RandomUtil randomUtil = new RandomUtil();
                double rand = randomUtil.randomGaussianNumber();
                player.sendMessage(ChatColor.GOLD + ">> You've mined " + rand + " DOGE.");
                economyManager.deposit(dogePlayer, rand);
                dogePlayerManager.storePlayer(dogePlayer);
            }
            if(event.getBlock().getType().equals(Material.SPAWNER) &&
                    (!player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) ||
                    !player.getInventory().getItemInOffHand().containsEnchantment(Enchantment.SILK_TOUCH)) && dogePlayer.isDonor()) {
                CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
                ItemStack stack = new ItemStack(Material.SPAWNER);
                ItemMeta meta = stack.getItemMeta();
                meta.setLore(Arrays.asList(spawner.getSpawnedType().toString()));
                stack.setItemMeta(meta);
                player.getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
            }
            if(event.getBlock().getType() == Material.SPRUCE_SIGN ||
                    event.getBlock().getType() == Material.SPRUCE_WALL_SIGN ||
                    event.getBlock().getType() == Material.ACACIA_SIGN ||
                    event.getBlock().getType() == Material.ACACIA_WALL_SIGN ||
                    event.getBlock().getType() == Material.BIRCH_SIGN ||
                    event.getBlock().getType() == Material.BIRCH_WALL_SIGN ||
                    event.getBlock().getType() == Material.DARK_OAK_SIGN ||
                    event.getBlock().getType() == Material.DARK_OAK_WALL_SIGN ||
                    event.getBlock().getType() == Material.JUNGLE_SIGN ||
                    event.getBlock().getType() == Material.JUNGLE_WALL_SIGN ||
                    event.getBlock().getType() == Material.OAK_SIGN ||
                    event.getBlock().getType() == Material.OAK_WALL_SIGN
            ) {
                Plot delete = null;
                String townName = null;
                Arena deleteArena = null;
                for(Town currentTown : townManager.getTowns()) {
                    for(Plot currentPlot : currentTown.getPlots()) {
                        if(currentPlot.getSignLocation().isEqual(point)) {
                            if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName()) && dogePlayer.getRole().equals("OWNER")) {
                                delete = currentPlot;
                                townName = currentTown.getName();
                                break;
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                    for(Arena arena : currentTown.getArenas()) {
                        if(arena.getSignLocation().isEqual(point)) {
                            if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName()) && dogePlayer.getRole().equals("OWNER")) {
                                deleteArena = arena;
                                townName = currentTown.getName();
                                break;
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                    if(delete != null || deleteArena != null) break;
                }
                if(delete != null) {
                    if(delete.getOwner() != null) {
                        player.sendMessage(ChatColor.RED + ">> You can not delete a plot that is currently occupied.");
                        event.setCancelled(true);
                    } else {
                        townManager.removePlot(townName, delete);
                        player.sendMessage(ChatColor.GOLD + ">> Plot removed!");
                    }
                }
                if(deleteArena != null) {
                    if(deleteArena.getState() != Arena.GameState.WAITING) {
                        player.sendMessage(ChatColor.RED + ">> You can not delete an arena during a fight.");
                        event.setCancelled(true);
                    } else {
                        townManager.getTown(townName).getArenas().remove(deleteArena);
                        player.sendMessage(ChatColor.GOLD + ">> Arena removed!");
                    }
                }
            }
            boolean allowed = true;
            boolean stop = false;
            for(Town currentTown : townManager.getTowns()) {
                if(townManager.inTown(currentTown.getName(), point)) {
                    allowed = false;
                    if(dogePlayer.getTown() != null) {
                        if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName()) && (dogePlayer.getRole().equalsIgnoreCase("OWNER") || (dogePlayer.getRole().equalsIgnoreCase("STAFF")))) {
                            stop = true;
                            allowed = true;
                            for(Plot plot : currentTown.getPlots()) {
                                if(areaUtil.inArea(plot.getArea(), point)) {
                                    if(plot.getOwner() != null) {
                                        if(!plot.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                                            allowed = false;
                                            stop = true;
                                        }
                                        break;
                                    }
                                }
                            }
                            break;
                        } else if(dogePlayer.getTown().equalsIgnoreCase(currentTown.getName())) {
                            for(Plot plot : currentTown.getPlots()) {
                                if(plot.getOwner() != null) {
                                    if(plot.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                                        if(areaUtil.inArea(plot.getArea(), point)) {
                                            allowed = true;
                                            stop = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(stop) break;
                        }
                    }
                }
            }
            if(!allowed) {
                event.setCancelled(true);
            } else if(event.getBlock().getType().equals(Material.CHEST)) {
                Shop shop = townManager.getShopFromPoint(point);
                if(shop != null) {
                    for(Plot currentPlot : townManager.getPlotsOwnedByDogePlayer(dogePlayer)) {
                        if(currentPlot.getShops().contains(shop)) {
                            townManager.getShopUtil().removeShopCosmetics(shop);
                            currentPlot.getShops().remove(shop);
                            player.sendMessage(ChatColor.GOLD + ">> Shop removed.");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) throws ExecutionException, InterruptedException, IOException {
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getPlayer().getUniqueId().toString());
        if(dogePlayer.isFighting()) {
            arenaManager.saveData(dogePlayer.getInArena());
            arenaManager.endGame(dogePlayer.getInArena(), event.getPlayer());
        }
        if(dogePlayer.getInArena() != null) {
            dogePlayer.getInArena().getQueue().remove(dogePlayer.getUuid());
            arenaManager.updateSign(dogePlayer.getInArena());
        }
        dogePlayerManager.unloadDogePlayer(event.getPlayer().getUniqueId().toString());
        Claim claim = claimManager.getClaimFromPlayer(event.getPlayer());
        if(claim != null) {
            claimManager.endClaim(claim);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) throws ExecutionException, InterruptedException {
        DogePlayer dogePlayer = dogePlayerManager.getPlayer(event.getEntity().getUniqueId().toString());
        dogePlayer.setLastDeath(event.getEntity().getLocation());
        event.getEntity().sendMessage(ChatColor.GOLD + ">> It looks like you've met an untimely demise. Type /back to go where you last were.");
    }
}
