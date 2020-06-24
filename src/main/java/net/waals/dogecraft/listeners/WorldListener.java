package net.waals.dogecraft.listeners;

import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.Point;
import net.waals.dogecraft.models.Town;
import org.bitcoinj.params.MainNetParams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class WorldListener implements Listener {

    private TownManager townManager;
    private Location spawn;

    public WorldListener(TownManager townManager) {
        this.townManager = townManager;
        this.spawn = new Location(Bukkit.getWorld("world"), -258, 64.5, -456, -178.7f, -2.1f);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            if(entity.getType() == EntityType.CAVE_SPIDER ||
                    entity.getType() == EntityType.CREEPER ||
                    entity.getType() == EntityType.SPIDER ||
                    entity.getType() == EntityType.ZOMBIE ||
                    entity.getType() == EntityType.SKELETON ||
                    entity.getType() == EntityType.DROWNED ||
                    entity.getType() == EntityType.ENDERMAN ||
                    entity.getType() == EntityType.WITCH ||
                    entity.getType() == EntityType.ZOMBIE_VILLAGER ||
                    entity.getType() == EntityType.PIG_ZOMBIE
            ) {
                Point point = new Point(event.getLocation().getBlockX(), event.getLocation().getBlockZ());
                for(Town currentTown : townManager.getTowns()) {
                    if(currentTown.hasFlag(Town.Flag.MOB_SPAWN)) {
                        if(townManager.inTown(currentTown.getName(), point)) {
                            event.setCancelled(true);
                        }
                    }
                }
                if(event.getLocation().getWorld().equals(Bukkit.getWorld("world"))) {
                    if(this.spawn.distanceSquared(event.getLocation()) <= 20000) {
                        event.setCancelled(true);
                    } else {
                        double rand = Math.random();
                        double x = spawn.distanceSquared(event.getLocation()) / 20000000;
                        if(x < rand) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent event) {
        for(String stack : townManager.getShopUtil().getStacks()) {
            if(event.getEntity().getUniqueId().toString().equalsIgnoreCase(stack)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        for(String stack : townManager.getShopUtil().getStacks()) {
            if(event.getEntity().getUniqueId().toString().equalsIgnoreCase(stack)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        for(Block block : event.blockList()) {
            Point point = new Point(block.getX(), block.getZ());
            for(Town currentTown : townManager.getTowns()) {
                if(townManager.inTown(currentTown.getName(), point) && !currentTown.hasFlag(Town.Flag.EXPLOSION)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
