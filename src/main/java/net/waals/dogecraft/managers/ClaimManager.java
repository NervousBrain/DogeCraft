package net.waals.dogecraft.managers;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.Claim;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.util.AreaUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ClaimManager {

    private DogeCraft plugin;
    private ArrayList<Claim> claims;
    private AreaUtil au;

    public ClaimManager(DogeCraft plugin, AreaUtil au) {
        this.plugin = plugin;
        this.claims = new ArrayList<Claim>();
        this.au = au;
    }

    public Claim getClaimFromPlayer(Player player) {
        Claim claim = null;
        for(int i = 0; i < claims.size(); i++) {
            if (claims.get(i).getPlayer().getUniqueId() == player.getUniqueId()) {
                claim = claims.get(i);
                break;
            }
        }
        return claim;
    }

    public Claim startClaim(Player player, Claim.Type type) {
        Claim claim = null;

        if(type == Claim.Type.TOWN_CREATION) {
            claim = new Claim(Claim.Type.TOWN_CREATION, player, 1000);
            claims.add(claim);
            claim.setStage(Claim.Stage.NAMING);
        }

        if(type == Claim.Type.PLOT_CREATION) {
            claim = new Claim(Claim.Type.PLOT_CREATION, player, 0);
            claims.add(claim);
            claim.setStage(Claim.Stage.LAND_CLAIM);
        }

        if(type == Claim.Type.SHOP_CREATION) {
            claim = new Claim(Claim.Type.SHOP_CREATION, player, 0);
            claims.add(claim);
            claim.setStage(Claim.Stage.SET_CHEST);
        }

        if(type == Claim.Type.TOWN_EXTENSION) {
            claim = new Claim(Claim.Type.TOWN_EXTENSION, player, 0);
            claims.add(claim);
            claim.setStage(Claim.Stage.LAND_CLAIM);
        }

        if(type == Claim.Type.OUTPOST_CREATION) {
            claim = new Claim(Claim.Type.TOWN_EXTENSION, player, 0);
            claims.add(claim);
            claim.setStage(Claim.Stage.LAND_CLAIM);
        }

        if(type == Claim.Type.ARENA_CREATION) {
            claim = new Claim(Claim.Type.ARENA_CREATION, player, 0);
            claims.add(claim);
            claim.setStage(Claim.Stage.LAND_CLAIM);
        }

        return claim;
    }

    public void endClaim(Claim claim) {
        claims.remove(claim);
    }
}
