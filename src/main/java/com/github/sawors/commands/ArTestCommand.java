package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.advancements.AdvancementManager;
import com.github.sawors.teams.ArTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if(sender instanceof Player){
            if(args.length > 0){
                switch(args[0]){
                    case"getcrits":
                        Main.logAdmin(ChatColor.YELLOW+"minecraft:adventure/adventuring_time");
                        for(String crit : Objects.requireNonNull(Bukkit.getAdvancement(Objects.requireNonNull(NamespacedKey.fromString("minecraft:adventure/adventuring_time")))).getCriteria()){
                            Main.logAdmin(crit);
                        }
                        Main.logAdmin(ChatColor.YELLOW+"minecraft:story/mine_stone");
                        for(String crit : Objects.requireNonNull(Bukkit.getAdvancement(Objects.requireNonNull(NamespacedKey.fromString("minecraft:story/mine_stone")))).getCriteria()){
                            Main.logAdmin(crit);
                        }
                }
            } else {
                sender.sendMessage("Phase 1");
                ArrayList<String> advlist = new ArrayList<>();
                int i = 0;
                for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
                    if(i >= 5){
                        break;
                    }
                    Advancement adv = it.next();
                    if(!AdvancementManager.isRecipe(adv)){
                        advlist.add(adv.getKey().toString());
                        i++;
                    }
                }
                ArTeamManager.setTeamAdvancements("test", ArDataBase.teamAdvancementsSerialize(advlist));
                sender.sendMessage("Phase 2");
                try{
                    ArrayList<String> out = ArDataBase.teamAdvancementsDeserialize(ArTeamManager.getTeamAdvancements("test"));
                    for(String adv : out){
                        AdvancementProgress pgr = ((Player) sender).getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString(adv)));
                        for(String ctr : pgr.getRemainingCriteria()){
                            pgr.awardCriteria(ctr);
                        }
                    }
                } catch (
                        SQLException | NullPointerException e) {
                    e.printStackTrace();
                }
    
            }
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
            
            return true;
        }
        
        return false;
    }
}
