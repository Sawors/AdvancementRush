package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import com.github.sawors.teams.ArTeam;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.UUID;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if(sender instanceof Player){
            ArTeam team = new ArTeam("TeamName", Color.LIME);
            UUID id = ((Player) sender).getUniqueId();
            team.addMember(id);
            team.addMember(id);
            team.addMember(id);
            team.addMember(id);
            //String test = DataBase.teamMembersSerialize(team);
            
            try{
                ArDataBase.registerTeam(team);
            } catch(KeyAlreadyExistsException e){
                sender.sendMessage(ChatColor.RED+"sorry, a team already exists with this name ("+team.getName()+")");
                return true;
            }
            //sender.sendMessage(ChatColor.GOLD+"INSERT INTO teams(name,color,points,players) VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+",'"+test+"')");
            /*for(UUID p : DataBase.teamMembersDeserialize(test)){
                sender.sendMessage(Objects.requireNonNull(Bukkit.getPlayer(p)).getName());
            }*/
            return true;
        }
        
        return false;
    }
}
