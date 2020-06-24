package net.waals.dogecraft.commands;

import net.milkbowl.vault.chat.Chat;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.models.DogePlayer;
import net.waals.dogecraft.models.Withdrawal;
import net.waals.dogecraft.wallet.CoreWallet;
import org.apache.commons.lang3.math.NumberUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.wallet.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class WalletCommands implements CommandExecutor {

    private DogePlayerManager dogePlayerManager;
    private CoreWallet wallet;
    private EconomyManager economyManager;

    public WalletCommands(DogePlayerManager dogePlayerManager, CoreWallet wallet, EconomyManager economyManager) {
        this.dogePlayerManager = dogePlayerManager;
        this.wallet = wallet;
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
            if(command.getName().equalsIgnoreCase("dogecoin")) {
                player.sendMessage(ChatColor.GOLD + ">> You can learn more about Dogecoin here: https://dogecoin.com");
            }
            if(command.getName().equalsIgnoreCase("deposit")) {
                player.sendMessage(">> Deposit DOGE to this address to receive it in-game: https://dogechain.info/address/" + dogePlayer.getAddress());
            }
            if(command.getName().equalsIgnoreCase("withdraw")) {
                if(args.length == 2) {
                    if(NumberUtils.isCreatable(args[0])) {
                        long amount = NumberUtils.toLong(args[0]);
                        if(economyManager.checkIfFundsAvailable(dogePlayer, NumberUtils.toDouble(args[0]))) {
                            Wallet.SendResult result = null;
                            try {
                                result = wallet.sendCoins(Address.fromBase58(wallet.getNetworkParameters(), args[1]), Coin.valueOf(amount).multiply(100000000));
                                player.sendMessage(ChatColor.GOLD + ">> DOGE are on their way :)");
                                player.sendMessage(ChatColor.GOLD + ">> https://dogechain.info/tx/" + result.tx.getHashAsString());
                                try {
                                    economyManager.deduct(dogePlayer, NumberUtils.toDouble(args[0]));
                                    dogePlayerManager.storePlayer(dogePlayer);
                                    economyManager.storeWithdrawal(new Withdrawal(dogePlayer.getUuid(), args[1], NumberUtils.toDouble(args[0])));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (InsufficientMoneyException e) {
                                player.sendMessage(ChatColor.RED + ">> Something went wrong. Please contact an Admin.");
                                e.printStackTrace();
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> You have insufficient funds");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Please type a valid number for the amount");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + ">> /withdraw <amount> <address>");
                }
            }
        }
        return false;
    }
}
