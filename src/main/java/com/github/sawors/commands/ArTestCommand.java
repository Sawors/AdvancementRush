package com.github.sawors.commands;

import com.github.sawors.DataBase;
import com.github.sawors.teams.ArTeam;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        ArrayList<Player> mbrs = new ArrayList<>();
        
        
        
        ArTeam team = new ArTeam("TeamName", Color.LIME,0, mbrs);
        if(sender instanceof Player){
            mbrs.add(((Player) sender));
            mbrs.add(((Player) sender));
            mbrs.add(((Player) sender));
            mbrs.add(((Player) sender));
            mbrs.add(((Player) sender));
            String test = DataBase.teamMembersSerialize(team);
            
            sender.sendMessage(ChatColor.GOLD+"INSERT INTO teams(name,color,points,players) VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+","+test+")");
            for(Player p : DataBase.teamMembersDeserialize(test)){
                sender.sendMessage(p.getName());
            }
            return true;
        }
        
        return false;
    }
}
