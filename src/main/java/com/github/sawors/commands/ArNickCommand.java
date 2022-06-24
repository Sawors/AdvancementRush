package com.github.sawors.commands;

import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class ArNickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    
        Player target = null;
        Component name = Component.text("");
    
        if(args.length >= 2 && Bukkit.getPlayer(args[0]) != null){
            target = Bukkit.getPlayer(args[0]);
            name = Component.text(args[1]);
    
        } else if (args.length == 1 && sender instanceof Player){
            target = (Player) sender;
            name = Component.text(args[0]);
            
        }
    
        if(target != null){
            target.displayName(name);
            target.playerListName(name);
            target.customName(name);
            ArTeamManager.syncPlayerColorWithTeam(target);
            return true;
        }
    
        
        return false;
    }
}
