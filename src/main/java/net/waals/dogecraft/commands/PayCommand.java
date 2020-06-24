package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.models.DogePlayer;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.security.util.IOUtils;

import java.util.concurrent.ExecutionException;

public class PayCommand implements CommandExecutor {

    DogePlayerManager dogePlayerManager;
    EconomyManager economyManager;

    public PayCommand(DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.dogePlayerManager = dogePlayerManager;
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
            if(args.length == 2) {
                DogePlayer beneficiary = null;
                if(!args[1].equalsIgnoreCase(player.getName())) {
                    try {
                        beneficiary = dogePlayerManager.getPlayerByName(args[1]);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(beneficiary != null) {
                        if(NumberUtils.isCreatable(args[0])) {
                            double amount = NumberUtils.toDouble(args[0]);
                            if(economyManager.checkIfFundsAvailable(dogePlayer, amount)) {
                                try {
                                    economyManager.pay(player.getName(), args[1], amount);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                player.sendMessage(ChatColor.GOLD + ">> You've sent " + beneficiary.getName() + " " + amount + " DOGE.");

                                if(Bukkit.getServer().getPlayer(args[1]) != null) {
                                    Bukkit.getServer().getPlayer(args[1]).sendMessage(ChatColor.GOLD + ">> You've received " + amount + " DOGE from " + dogePlayer.getName() + ".");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + ">> You have insufficient funds.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Please enter a valid amount.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> This player was not found.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + ">> You can't pay yourself. Well you could but that would be pointless.");
                }
            } else {
                player.sendMessage(ChatColor.RED + ">> /pay <amount> <user>");
            }
        } else {
            double rand = 2.5 + Math.random() * (7.5 - 2.5);
            double amount =  (Math.round(rand* 100.0) / 100.0);
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + args[0] + " voted and received " + amount + " DOGE!");
            try {
                economyManager.deposit(dogePlayerManager.getPlayerByName(args[0]), amount);
                dogePlayerManager.storePlayer(dogePlayerManager.getPlayerByName(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
