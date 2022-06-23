package com.github.sawors.commands;

import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArUnNickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player target;
        
        if (args.length >= 1 && sender instanceof Player){
            target = (Player) sender;
            target.displayName(Component.text(""));
            target.playerListName(Component.text(""));
            ArTeamManager.syncPlayerColorWithTeam(target);
            return true;
        }
        return false;
    }
}
