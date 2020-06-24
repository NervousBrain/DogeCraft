package net.waals.dogecraft.commands;

import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TeleportationCommands implements CommandExecutor {

    private DogePlayerManager dogePlayerManager;
    private HashMap<String, Player> tpas;
    private Location spawn;

    public TeleportationCommands(DogePlayerManager dogePlayerManager) {
        this.dogePlayerManager = dogePlayerManager;
        this.tpas = new HashMap<>();
        this.spawn = new Location(Bukkit.getWorld("world"), -258, 64.5, -456, -178.7f, -2.1f);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            DogePlayer dogePlayer = null;
            Player player = (Player) sender;
            try {
                dogePlayer = dogePlayerManager.getPlayer(player.getUniqueId().toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!dogePlayer.isFighting()) {
                if(command.getName().equalsIgnoreCase("back")) {
                    if(dogePlayer.getLastDeath() != null) {
                        player.teleport(dogePlayer.getLastDeath());
                        dogePlayer.setLastDeath(null);
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Seems like you haven't died since you last executed /back.");
                    }
                }
                if(command.getName().equalsIgnoreCase("tpa")) {
                    if(args.length == 1) {
                        Player toTp = null;
                        for(Player p : Bukkit.getOnlinePlayers()) {
                            if(p.getName().equalsIgnoreCase(args[0])) {
                                toTp = p;
                                break;
                            }
                        }
                        if(toTp != null) {
                            toTp.sendMessage(ChatColor.GOLD + ">> " + player.getName() + " would like to teleport to you. Type /tpaccept or /tpdeny.");
                            player.sendMessage(ChatColor.GOLD + ">> You've sent a teleportation request to " + toTp.getName() + ".");
                            tpas.put(toTp.getUniqueId().toString(), player);
                        } else {
                            player.sendMessage(ChatColor.RED + ">> Seems like this player isn't online.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> /tpa <name>");
                    }
                }
                if(command.getName().equalsIgnoreCase("tpaccept")) {
                    if(tpas.get(player.getUniqueId().toString()) != null) {
                        if(tpas.get(player.getUniqueId().toString()).isOnline()) {
                            tpas.get(player.getUniqueId().toString()).teleport(player.getLocation());
                            player.sendMessage(ChatColor.GOLD + ">> " + player.getName() + " teleported to you.");
                            tpas.get(player.getUniqueId().toString()).sendMessage(ChatColor.GOLD + ">> Teleporting to " + player.getName() + ".");
                            tpas.remove(player.getUniqueId().toString());
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Nobody requested to teleport to you.");
                    }

                }
                if(command.getName().equalsIgnoreCase("tpdeny")) {

                    if(tpas.get(player.getUniqueId().toString()) != null) {
                        player.sendMessage(ChatColor.RED + ">> Rejected teleportation request from " + tpas.get(player.getUniqueId().toString()));
                        tpas.get(player.getUniqueId().toString()).sendMessage(ChatColor.RED + ">> " + player.getName() + " rejected your teleportation request.");
                        tpas.remove(player.getUniqueId().toString());
                    } else {
                        player.sendMessage(ChatColor.RED + ">> Nobody requested to teleport to you.");
                    }

                }
                if(command.getName().equalsIgnoreCase("spawn")) {
                    player.teleport(this.spawn);
                }
            } else {
                player.sendMessage(ChatColor.RED + ">> You cannot do this in a fight.");
            }
        }

        return false;
    }
}
