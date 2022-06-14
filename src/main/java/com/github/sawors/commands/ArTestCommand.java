package com.github.sawors.commands;

import com.github.sawors.teams.ArTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;

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
            try {
                sender.sendMessage(ArTeamManager.getPlayerTeam(((Player) sender).getUniqueId()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (NullPointerException e2){
                sender.sendMessage(ChatColor.RED+"this player has no team");
            }
            Player p = (Player) sender;
            for(String c : p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).getRemainingCriteria()){
            p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).awardCriteria(c);
            }
            return true;
        }
        
        return false;
    }
}
