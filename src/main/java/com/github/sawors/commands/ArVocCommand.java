package com.github.sawors.commands;

import com.github.sawors.Main;
import com.github.sawors.discordbot.ArDBotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ArVocCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1){
            switch(args[0]){
                case"generate":
                case"gen":
                    ArDBotManager.createGameCategory();
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            ArDBotManager.createTeamRoles();
                            sender.sendMessage(ChatColor.LIGHT_PURPLE+"roles created");
                        }
                    }.runTaskLater(Main.getPlugin(), 20);
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            ArDBotManager.createTeamChannels();
                            sender.sendMessage(ChatColor.LIGHT_PURPLE+"channels created");
                        }
                    }.runTaskLater(Main.getPlugin(), 40);
                    return true;
                case"roles":
                    ArDBotManager.giveTeamRoles();
                    return true;
                case"split":
                    ArDBotManager.sendUsersToTeamChannels();
                    break;
                case"group":
                    break;
                case"delete":
                    ArDBotManager.deleteRoles();
                    sender.sendMessage(ChatColor.LIGHT_PURPLE+"roles deleted");
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            ArDBotManager.deleteCategory();
                            sender.sendMessage(ChatColor.LIGHT_PURPLE+"team channels and category deleted");
                        }
                    }.runTaskLater(Main.getPlugin(), 40);
            }
        }
        return false;
    }
}
