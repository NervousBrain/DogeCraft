package net.waals.dogecraft.runnables;

import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.Town;

public class NPCRunnable implements Runnable{

    private TownManager townManager;

    public NPCRunnable(TownManager townManager) {
        this.townManager = townManager;
    }

    @Override
    public void run() {
        for(Town currentTown : townManager.getTowns()) {

        }
    }
}
