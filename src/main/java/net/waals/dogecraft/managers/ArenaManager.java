package net.waals.dogecraft.managers;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.*;
import net.waals.dogecraft.util.AreaUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ArenaManager {

    private DogeCraft plugin;
    private AreaUtil areaUtil;
    private DogePlayerManager dogePlayerManager;

    public ArenaManager(DogeCraft plugin, AreaUtil areaUtil, DogePlayerManager dogePlayerManager) {
        this.plugin = plugin;
        this.areaUtil = areaUtil;
        this.dogePlayerManager = dogePlayerManager;
    }

    public Arena createArena(Area area, Point a, Point b, double wager, Point signLocation, Town town) {
        Arena arena = new Arena(area, a, b, wager, signLocation);
        if (town.getArenas() == null) {
            town.setArenas(new ArrayList<>());
        }
        town.getArenas().add(arena);
        return arena;
    }

    private void removePotionEffects(Player player) {
        for(PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
        }
    }

    public void updateSign(Arena arena) {
        System.out.println(arena.getSignLocation().getY());
        Sign sign = (Sign) Bukkit.getServer().getWorld("world").getBlockAt(areaUtil.pointToLocation(arena.getSignLocation())).getState();
        sign.setLine(0, ChatColor.GREEN + "[ARENA]");
        sign.setLine(1, ChatColor.BLUE + "Wager: " + arena.getAmount() + "Đ");
        sign.setLine(2, arena.getQueue().size() + " player(s)");
        sign.setLine(3, "in Queue");
        sign.update();
    }

    public void initiateGame(Arena arena) throws ExecutionException, InterruptedException, IOException {
            DogePlayer a = null;
            DogePlayer b = null;
            arena.setPlayerA(Bukkit.getPlayer(UUID.fromString(arena.getQueue().get(0))));
            arena.setPlayerB(Bukkit.getPlayer(UUID.fromString(arena.getQueue().get(1))));
            a = dogePlayerManager.getPlayer(arena.getPlayerA().getUniqueId().toString());
            b = dogePlayerManager.getPlayer(arena.getPlayerB().getUniqueId().toString());
            a.setFighting(true);
            b.setFighting(true);
            arena.getPlayerB().closeInventory();
            arena.getPlayerA().closeInventory();
            arena.setInvA(arena.getPlayerA().getInventory().getContents());
            arena.setInvB(arena.getPlayerB().getInventory().getContents());
            arena.getPlayerA().getInventory().clear();
            arena.getPlayerB().getInventory().clear();
            arena.getQueue().subList(0, 1).clear();
            Location fixed1 = areaUtil.pointToLocation(arena.getSpawnA()).add(new Vector(0, 2, 0));
            Location fixed2 = areaUtil.pointToLocation(arena.getSpawnB()).add(new Vector(0, 2, 0));
            Vector diff1 = fixed1.toVector().subtract(fixed2.toVector());
            Vector diff2 = fixed2.toVector().subtract(fixed1.toVector());
            diff1.normalize();
            diff2.normalize();
            arena.getPlayerA().teleport(areaUtil.pointToLocation(arena.getSpawnA()).add(new Vector(0, 1, 0)).setDirection(diff2));
            arena.getPlayerB().teleport(areaUtil.pointToLocation(arena.getSpawnB()).add(new Vector(0, 1, 0)).setDirection(diff1));
            removePotionEffects(arena.getPlayerA());
            removePotionEffects(arena.getPlayerB());
            arena.getPlayerA().setHealth(20);
            arena.getPlayerB().setHealth(20);
            arena.getPlayerA().setFoodLevel(20);
            arena.getPlayerB().setFoodLevel(20);
            ItemStack axe = new ItemStack(Material.IRON_AXE);
            ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
            ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
            ItemStack food = new ItemStack(Material.BREAD);
            food.setAmount(16);
            ItemStack shield = new ItemStack(Material.SHIELD);
            arena.getPlayerA().getInventory().setHelmet(helmet);
            arena.getPlayerA().getInventory().setItemInOffHand(shield);
            arena.getPlayerA().getInventory().setLeggings(leggings);
            arena.getPlayerA().getInventory().setChestplate(chestplate);
            arena.getPlayerA().getInventory().setBoots(boots);
            arena.getPlayerA().getInventory().addItem(axe);
            arena.getPlayerA().getInventory().addItem(food);
            arena.getPlayerB().getInventory().setHelmet(helmet);
            arena.getPlayerB().getInventory().setItemInOffHand(shield);
            arena.getPlayerB().getInventory().setLeggings(leggings);
            arena.getPlayerB().getInventory().setChestplate(chestplate);
            arena.getPlayerB().getInventory().setBoots(boots);
            arena.getPlayerB().getInventory().addItem(axe);
            arena.getPlayerB().getInventory().addItem(food);
            updateSign(arena);
            arena.getPlayerB().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10));
            arena.getPlayerA().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10));
            final int[] i = {3};
            int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if (i[0] > 0) {
                        arena.getPlayerA().sendTitle(ChatColor.RED + "Game starting in " + i[0] + "..", "", 2, 16, 2);
                        arena.getPlayerB().sendTitle(ChatColor.RED + "Game starting in " + i[0] + "..", "", 2, 16, 2);
                        i[0]--;
                    } else {
                        arena.getPlayerA().sendTitle(ChatColor.RED + "Fight!", "", 2, 16, 2);
                        arena.getPlayerB().sendTitle(ChatColor.RED + "Fight!", "", 2, 16, 2);
                        arena.setState(Arena.GameState.FIGHTING);
                        plugin.getServer().getScheduler().cancelTask(arena.getTask());
                    }
                }
            }, 0L, 20L);
            arena.setTask(task);
    }

    public void checkIfGameCanStart(Arena arena) throws ExecutionException, InterruptedException, IOException {
        System.out.println(arena.getState().toString());
        System.out.println((arena.getQueue().size() +""));
        if(arena.getQueue().size() >= 2 && arena.getState() == Arena.GameState.WAITING) {
            arena.setState(Arena.GameState.FIGHTING);
            this.initiateGame(arena);
        }
    }

    public void endGame(Arena arena, Player looser) throws ExecutionException, InterruptedException {
        arena.setState(Arena.GameState.ENDED);
        plugin.getServer().getScheduler().cancelTask(arena.getTask());
        DogePlayer a = null;
        DogePlayer b = null;
        a = dogePlayerManager.getPlayer(arena.getPlayerA().getUniqueId().toString());
        b = dogePlayerManager.getPlayer(arena.getPlayerB().getUniqueId().toString());
        a.setFighting(false);
        b.setFighting(false);
        a.setInArena(null);
        b.setInArena(null);
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hidePlayer(plugin, looser);
        }
        arena.getPlayerA().getInventory().clear();
        arena.getPlayerB().getInventory().clear();
        arena.getPlayerA().setFoodLevel(20);
        arena.getPlayerB().setHealth(20);
        arena.getPlayerB().setFoodLevel(20);
        arena.getPlayerA().setHealth(20);
        if(looser == arena.getPlayerA()) {
            arena.getPlayerB().sendTitle(ChatColor.GREEN + "You won!", "", 2, 56, 2);
            arena.getPlayerA().sendTitle(ChatColor.RED + "You lost!", "", 2, 56, 2);
        } else {
            arena.getPlayerA().sendTitle(ChatColor.GREEN + "You won!", "", 2, 56, 2);
            arena.getPlayerB().sendTitle(ChatColor.RED + "You lost!", "", 2, 56, 2);
        }
        final int[] i = {3};
        int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(i[0] > 0) {
                    i[0]--;
                } else {
                    arena.getPlayerA().getInventory().setContents(arena.getInvA());
                    arena.getPlayerB().getInventory().setContents(arena.getInvB());
                    arena.getPlayerA().teleport(areaUtil.pointToLocation(arena.getSignLocation()));
                    arena.getPlayerB().teleport(areaUtil.pointToLocation(arena.getSignLocation()));
                    arena.setPlayerA(null);
                    arena.setPlayerB(null);
                    arena.setInvA(null);
                    arena.setInvB(null);
                    arena.setState(Arena.GameState.WAITING);
                    try {
                        checkIfGameCanStart(arena);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                        player.showPlayer(plugin, looser);
                    }
                    plugin.getServer().getScheduler().cancelTask(arena.getTask());
                }
            }
        },0L, 20L);
        arena.setTask(task);
    }

    public void saveData(Arena arena) throws IOException {
        if(Bukkit.getOfflinePlayer(arena.getPlayerA().getUniqueId()) != null) {
            for (int i = 0; i < arena.getInvA().length; i++) {
                plugin.getConfig().set(arena.getPlayerA().getUniqueId().toString() + ".inv." + i, arena.getInvA()[i]);
            }
            plugin.getConfig().set(arena.getPlayerA().getUniqueId().toString() + ".loc", areaUtil.pointToLocation(arena.getSignLocation()));
        } if(Bukkit.getOfflinePlayer(arena.getPlayerB().getUniqueId()) != null) {
            for (int i = 0; i < arena.getInvB().length; i++) {
                plugin.getConfig().set(arena.getPlayerB().getUniqueId().toString() + ".inv." + i, arena.getInvB()[i]);
            }
            plugin.getConfig().set(arena.getPlayerB().getUniqueId().toString() + ".loc", areaUtil.pointToLocation(arena.getSignLocation()));
        }
        plugin.saveConfig();
    }
}
