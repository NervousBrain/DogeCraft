package net.waals.dogecraft.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.models.DogePlayer;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.graalvm.compiler.phases.common.DeadCodeEliminationPhase.Optionality.Optional;

public class DonationCommand implements CommandExecutor {

    private EconomyManager economyManager;
    private DogePlayerManager dogePlayerManager;
    private LuckPerms lpApi;

    public DonationCommand(EconomyManager economyManager, DogePlayerManager dogePlayerManager) {
        this.economyManager = economyManager;
        this.dogePlayerManager = dogePlayerManager;
        lpApi = LuckPermsProvider.get();
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
            if(args.length != 1) {
                player.sendMessage(ChatColor.GOLD + ">> Thank you for considering to donate!");
                player.sendMessage(ChatColor.GOLD + ">> If you donate more than 1000 Doge you will get the following perks: ");
                player.sendMessage(ChatColor.GOLD + ">> A swaggy green nametag.");
                player.sendMessage(ChatColor.GOLD + ">> The ability to mine mob spawners with silk touch.");
                //player.sendMessage(ChatColor.GOLD + ">> A boatload of cool cosmetic items.");
                player.sendMessage(ChatColor.GOLD + ">> My eternal gratitude! ");
                player.sendMessage(ChatColor.GOLD + ">> And there is more to come in the future.");
            } else {
                if(NumberUtils.isCreatable(args[0])) {
                    double amount = NumberUtils.toDouble(args[0]);
                    if(amount > 0) {
                        if(economyManager.checkIfFundsAvailable(dogePlayer, amount)) {
                            try {
                                economyManager.deduct(dogePlayer, amount);
                                player.sendMessage(ChatColor.GOLD + ">> Thank you for donating to the server! :)");
                                if(amount >= 1000) {
                                    User u = lpApi.getUserManager().getUser(UUID.fromString(player.getUniqueId().toString()));
                                    u.setPrimaryGroup("donor");
                                    lpApi.getUserManager().saveUser(u);
                                    dogePlayer.setLastDonation(System.currentTimeMillis());
                                    dogePlayerManager.storePlayer(dogePlayer);
                                    player.sendMessage(ChatColor.GOLD + ">> You unlocked the donor perks. :D");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + ">> You have insufficient funds.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Please enter a valid value.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + ">> Please enter a valid value.");
                }
            }
        }
        return false;
    }
}
