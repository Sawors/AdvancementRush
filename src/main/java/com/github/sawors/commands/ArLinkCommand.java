package com.github.sawors.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ArLinkCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 2){
            //  /arlink [player] [team]
            /*if(args[0].equals("showlist")){
                ArDataBase.printPlayerTeamLink();
            } else {
                String player = args[0];
                String team = args[1];
                ArDataBase.setPlayerTeamLink(Bukkit.getPlayerUniqueId(player),team);
            }*/
        }
        return false;
    }
}
