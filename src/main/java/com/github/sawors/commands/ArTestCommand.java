package com.github.sawors.commands;

import com.github.sawors.DataBase;
import com.github.sawors.teams.ArTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        ArrayList<UUID> mbrs = new ArrayList<>();
        
        
        
        ArTeam team = new ArTeam("TeamName", Color.LIME,0, mbrs);
        if(sender instanceof Player){
            mbrs.add(((Player) sender).getUniqueId());
            mbrs.add(((Player) sender).getUniqueId());
            mbrs.add(((Player) sender).getUniqueId());
            
            String test = DataBase.teamMembersSerialize(team);
            
            sender.sendMessage(ChatColor.GOLD+"INSERT INTO teams(name,color,points,players) VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+",'"+test+"')");
            for(UUID p : DataBase.teamMembersDeserialize(test)){
                sender.sendMessage(Objects.requireNonNull(Bukkit.getPlayer(p)).getName());
            }
            return true;
        }
        
        return false;
    }
}