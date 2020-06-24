package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.TownManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class AdminCommands implements CommandExecutor {

    private TownManager townManager;

    public AdminCommands(TownManager townManager) {
        this.townManager = townManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(command.getName().equalsIgnoreCase("removeshopcosmetics") && commandSender instanceof ConsoleCommandSender) {
            townManager.getShopUtil().removeShopCosmetics();
        }
        if(command.getName().equalsIgnoreCase("stopdogecraft") && commandSender.isOp()) {
            townManager.getShopUtil().removeShopCosmetics();
            Bukkit.getServer().shutdown();
        }
        return false;
    }
}
