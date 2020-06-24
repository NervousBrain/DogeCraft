package net.waals.dogecraft.util.tags;

import net.iso2013.mlapi.api.tag.TagController;
import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TagUtil implements TagController {

    private DogePlayerManager dogePlayerManager;
    private DogeCraft plugin;

    public TagUtil(DogePlayerManager dogePlayerManager, DogeCraft plugin) {
        this.dogePlayerManager = dogePlayerManager;
        this.plugin = plugin;
    }

    @Override
    public List<TagController.TagLine> getFor(Entity entity) {
        if(entity instanceof Player) {
            return Collections.singletonList(new DogeLine(dogePlayerManager));
        } else {
            return null;
        }
    }

    @Override
    public String getName(Entity target, Player viewer, String previous) {
        if(target instanceof Player) {
            Player player = (Player) target;
            if(player.hasPermission("dogecraft.donor")) {
                if(player.isOp()) {
                    return ChatColor.RED + previous;
                } else {
                    return ChatColor.GREEN + previous;
                }
            } else {
                return previous;
            }
        } else {
            return previous;
        }
    }

    @Override
    public EntityType[] getAutoApplyFor() {
        return new EntityType[]{
                EntityType.PLAYER
        };
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getNamePriority() {
        return 0;
    }
}
