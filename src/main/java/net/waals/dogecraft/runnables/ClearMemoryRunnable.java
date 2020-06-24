package net.waals.dogecraft.runnables;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ClearMemoryRunnable implements Runnable{

    private DogePlayerManager dogePlayerManager;

    public ClearMemoryRunnable(DogePlayerManager dogePlayerManager) {
        this.dogePlayerManager = dogePlayerManager;
    }

    @Override
    public void run() {
        for(DogePlayer dogePlayer : dogePlayerManager.getDogePlayers()) {
            if(Bukkit.getPlayer(UUID.fromString(dogePlayer.getUuid())) == null) {
                dogePlayerManager.getDogePlayers().remove(dogePlayer);
            }
        }
    }
}
