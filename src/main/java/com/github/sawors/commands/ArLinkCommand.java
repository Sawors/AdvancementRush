package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ArLinkCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 2){
            //  /arlink [player] [team]
            String player = args[0];
            String team = args[1];
            ArDataBase.setPlayerTeamLink(Bukkit.getPlayerUniqueId(player),team);
        }
        return false;
    }
}
