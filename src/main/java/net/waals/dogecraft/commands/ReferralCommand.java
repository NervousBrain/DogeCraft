package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class ReferralCommand implements CommandExecutor {

    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;

    public ReferralCommand(DogePlayerManager dogePlayerManager, EconomyManager economyManager) {

        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if(args.length == 1) {
                DogePlayer dogePlayer = null;
                try {
                    dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DogePlayer referredBy = null;
                try {
                    referredBy = dogePlayerManager.getPlayerByName(args[0]);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(referredBy != null) {
                    if(!dogePlayer.getUuid().equalsIgnoreCase(referredBy.getUuid())) {
                        if(dogePlayer.getReferredBy() == null) {
                            dogePlayer.setReferredBy(referredBy.getUuid());
                            economyManager.deposit(dogePlayer, 10);
                            economyManager.deposit(referredBy, 10);
                            player.sendMessage(ChatColor.GOLD + ">> You received 10 Doge.");
                        } else {
                            player.sendMessage(ChatColor.RED + ">> You already did that.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Find some friends.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + args[0] + " is not on our records.");
                }
            } else {
                player.sendMessage(ChatColor.RED + ">> /ref <Player>");
            }
        }
        return false;
    }
}
