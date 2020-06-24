package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class BalanceCommand implements CommandExecutor {

    private DogePlayerManager dogePlayerManager;

    public BalanceCommand(DogePlayerManager dogePlayerManager) {
        this.dogePlayerManager = dogePlayerManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            try {
                player.sendMessage(">> " + dogePlayerManager.getPlayer(player.getUniqueId().toString()).getBalance() + " DOGE");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
