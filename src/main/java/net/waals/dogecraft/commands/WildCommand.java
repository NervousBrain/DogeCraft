package net.waals.dogecraft.commands;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.Point;
import net.waals.dogecraft.models.Town;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class WildCommand implements CommandExecutor {

    private TownManager townManager;
    private ArrayList<String> cooldown;
    private DogeCraft plugin;

    public WildCommand(TownManager townManager, DogeCraft plugin) {
        this.townManager = townManager;
        this.plugin = plugin;
        this.cooldown = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(!inCooldown(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.GREEN + ">> Generating terrain. Teleportation will commence in a few moments.");
                Location loc = getWilderniss();
                int x = loc.getChunk().getX();
                int z = loc.getChunk().getZ();
                loc.getChunk().load();
                for(int i = -1; i < 1; i++) {
                    for(int j = -1; j < 1; j++) {
                        Bukkit.getWorld("world").getChunkAt(x+i, z+j).load();
                    }
                }
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.sendMessage(ChatColor.GREEN + ">> You shall go where no man has gone before you..");
                        player.teleport(loc);
                    }
                }, 5*20L);
                cooldown.add(player.getUniqueId().toString());
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for(String down : cooldown) {
                            if(down.equalsIgnoreCase(player.getUniqueId().toString())) {
                                cooldown.remove(down);
                                break;
                            }
                        }
                    }
                }, 60*20L);
            } else {
                player.sendMessage(ChatColor.RED + ">> This command is in cooldown.");
            }
        }
        return false;
    }

    private boolean inCooldown(String uuid) {
            boolean cool = false;
            for(String down : cooldown) {
                if(down.equalsIgnoreCase(uuid)) cool = true;
                break;
            }
            return cool;
    }

    private Location getWilderniss() {
        int x;
        int z;
        x = RandomUtils.nextInt(0 + 1000) + 400;
        z = RandomUtils.nextInt(0 + 1000) + 400;
        if (RandomUtils.nextBoolean()) {
            x *= -1;
        }
        if (RandomUtils.nextBoolean()) {
            z *= -1;
        }
        Point point = new Point(x, z);
        boolean inATown = false;
        for(Town town : townManager.getTowns()) {
            inATown = townManager.inTown(town.getName(), point);
        }
        Location loc = new Location(Bukkit.getWorld("world"), x, Bukkit.getWorld("world").getHighestBlockYAt(x, z), z);
        if(loc.getBlock().getType().equals(Material.WATER) || loc.getBlock().getType().equals(Material.LAVA)) {
            inATown = true;
        }
        if(inATown) {
            return getWilderniss();
        } else {
            return loc.add(new Vector(0, 1.5, 0));
        }
    }
}
