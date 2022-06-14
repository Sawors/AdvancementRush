package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if(sender instanceof Player){
            /*String name = "UwUBois";
            ArTeam team = new ArTeam(name, Color.LIME);
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
            }*/
            ArDataBase.printPlayerTeamLink();
            return true;
        }
        
        return false;
    }
}
