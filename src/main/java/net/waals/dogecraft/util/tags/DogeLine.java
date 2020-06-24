package net.waals.dogecraft.util.tags;

import net.iso2013.mlapi.api.tag.TagController;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class DogeLine implements TagController.TagLine {

    private DogePlayerManager dogePlayerManager;

    public DogeLine(DogePlayerManager dogePlayerManager) {
        this.dogePlayerManager = dogePlayerManager;
    }

    @Override
    public String getText(Entity entity, Player player) {
        String res = null;
        if(entity instanceof Player) {
            Player p = (Player) entity;
            DogePlayer dogePlayer = null;
            try {
                dogePlayer = dogePlayerManager.getPlayer(p.getUniqueId().toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(dogePlayer != null) {
                if(dogePlayer.getTown() != null) {
                    res = ChatColor.GOLD + "[" + dogePlayer.getTown() + "]";
                }
            }
        }
        return res;
    }

    @Override
    public boolean keepSpaceWhenNull(Entity entity) {
        return false;
    }
}
