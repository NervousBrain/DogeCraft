package net.waals.dogecraft.commands;

import com.github.stefvanschie.inventoryframework.Gui;
import net.waals.dogecraft.guis.TownAdminGui;
import net.waals.dogecraft.guis.TownGui;
import net.waals.dogecraft.managers.ClaimManager;
import net.waals.dogecraft.models.Claim;
import net.waals.dogecraft.util.RandomUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand implements CommandExecutor {

    private ClaimManager cm;
    private TownAdminGui townGui;

    public CreateCommand(ClaimManager cm, TownAdminGui townGui) {
        this.cm = cm;
        this.townGui = townGui;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if(command.getName().equalsIgnoreCase("create")) {
                String type = args[0];
                if(type.equalsIgnoreCase("town") && player.isOp()) {
                    cm.startClaim(player, Claim.Type.TOWN_CREATION);
                }
            }
        }
        return false;
    }
}
