package com.github.sawors.commands;

import com.github.sawors.teams.ArTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArUnNickCommand extends ArTeamManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player target;
        
        if (args.length >= 1 && Bukkit.getPlayer(args[0]) != null){
            target = Bukkit.getPlayer(args[0]);
            target.displayName(null);
            target.playerListName(null);
            target.customName(null);
            ArTeamManager.syncPlayerColorWithTeam(target);
            return true;
        }
        return false;
    }
}
