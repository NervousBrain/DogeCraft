package net.waals.dogecraft.commands;

import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.guis.TownAdminGui;
import net.waals.dogecraft.guis.TownGui;
import net.waals.dogecraft.managers.ClaimManager;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.managers.TownManager;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Town;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class MenuCommand implements CommandExecutor {

    private TownGui townGui;
    private TownAdminGui townAdminGui;
    private DogePlayerManager dogePlayerManager;
    private ClaimManager claimManager;
    private TownManager townManager;
    private DogeCraft plugin;
    private EconomyManager economyManager;

    public MenuCommand(DogePlayerManager dogePlayerManager, ClaimManager claimManager, TownManager townManager, DogeCraft plugin, EconomyManager economyManager) {
        this.dogePlayerManager = dogePlayerManager;
        this.claimManager = claimManager;
        this.townManager = townManager;
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            DogePlayer dogePlayer = null;
            try {
                dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!dogePlayer.isFighting()) {
                if(economyManager.getPayers().contains(player.getUniqueId().toString())) {
                    economyManager.getPayers().remove(player.getUniqueId().toString());
                }
                if(dogePlayer != null) {
                    Town town = townManager.getTown(dogePlayer.getTown());
                    if(town == null) {
                        townGui = new TownGui(townManager, plugin, claimManager, dogePlayerManager, economyManager);
                        townGui.show(player);
                    } else if(dogePlayer.getTown().equalsIgnoreCase(town.getName())) {
                        if(dogePlayer.getRole().equals("OWNER")) {
                            townAdminGui = new TownAdminGui(townManager, claimManager, plugin, dogePlayerManager, economyManager);
                            townAdminGui.show(player, dogePlayer.getTown());
                        } else {
                            townGui = new TownGui(townManager, plugin, claimManager, dogePlayerManager, economyManager);
                            townGui.show(player);
                        }

                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + ">> You cannot do this while you are fighting!");
            }

        }
        return false;
    }
}
