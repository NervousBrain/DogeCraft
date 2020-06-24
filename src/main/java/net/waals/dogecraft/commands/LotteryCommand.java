package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.managers.LotteryManager;
import net.waals.dogecraft.models.DogePlayer;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class LotteryCommand implements CommandExecutor {

    private LotteryManager lotteryManager;
    private EconomyManager economyManager;
    private DogePlayerManager dogePlayerManager;

    public LotteryCommand(LotteryManager lotteryManager, EconomyManager economyManager, DogePlayerManager dogePlayerManager) {

        this.lotteryManager = lotteryManager;
        this.economyManager = economyManager;
        this.dogePlayerManager = dogePlayerManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if(args.length == 0) {
                player.sendMessage(ChatColor.GOLD + ">> The pot is " + lotteryManager.getPot()  + " DOGE.");
                player.sendMessage(ChatColor.GOLD + ">> You can buy tickets with /lottery enter <amount of tickets>");
                player.sendMessage(ChatColor.GOLD + ">> Each ticket costs 1 DOGE.");
                player.sendMessage(ChatColor.GOLD + ">> You currently have " + lotteryManager.getNumberOfTickets(player.getUniqueId().toString()) + " tickets.");
            } else {
                if(args[0].equalsIgnoreCase("enter")) {
                    if(isInteger(args[1])) {
                        DogePlayer dogePlayer = null;
                        try {
                            dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int amount = NumberUtils.toInt(args[1]);
                        if(amount > 0) {
                            if(economyManager.checkIfFundsAvailable(dogePlayer, amount)) {
                                try {
                                    economyManager.deduct(dogePlayer, amount);
                                    lotteryManager.add(player.getUniqueId().toString(), amount);
                                    player.sendMessage(ChatColor.GOLD + ">> You bought " + amount + " lottery tickets.");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + ">> You have insufficient funds.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Look ma, I'm hacking.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Please enter an integer for the number of lottery tickets.");
                    }
                } else {
                    if(!player.isOp()) {
                        player.sendMessage(ChatColor.RED + ">> /lottery enter <amount of tickets>");
                    } else {
                        if(args[0].equalsIgnoreCase("draw")) {
                            DogePlayer winner = null;
                            String winnerUUID = lotteryManager.pickWinner();
                            try {
                                winner = dogePlayerManager.getPlayer(winnerUUID);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + ">> " + winner.getName() + " won " + lotteryManager.getPot() + " DOGE in the lottery!");
                            economyManager.deposit(winner, lotteryManager.getPot());
                            lotteryManager.reset();
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
