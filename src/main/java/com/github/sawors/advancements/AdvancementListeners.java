package com.github.sawors.advancements;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class AdvancementListeners implements Listener {
    
    @EventHandler(priority = EventPriority.LOW)
    public static void playerCriteriaProgress(PlayerAdvancementCriterionGrantEvent event){
        Advancement adv = event.getAdvancement();
        if(adv.getKey().getKey().contains("recipes/")){
            return;
        }
        Main.logAdmin("you're fired!");
        Player p = event.getPlayer();
        try{
            String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            if(!p.getAdvancementProgress(event.getAdvancement()).isDone()){
                ArTeamManager.syncTeamAdvancement(team, adv, p);
            }
        } catch(SQLException | NullPointerException e){
            e.printStackTrace();
        }
        
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void onPlayerCompleteAdvancement(PlayerAdvancementDoneEvent event){
        Advancement adv = event.getAdvancement();
        if(adv.getKey().getKey().contains("recipes/")){
            return;
        }
        Player p = event.getPlayer();
        try{
            String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            String advname = event.getAdvancement().getKey().getKey();
        
            if(p.getAdvancementProgress(event.getAdvancement()).isDone()){
                int value = ArDataBase.getAdvancementValue(advname);
                if(value != 0){
                    if (ArDataBase.isAdvancementMuted(advname, team)) {
                        event.message(null);
                    } else {
                        // VALUE ADD
                        p.sendMessage(Component.text("Value : "+value));
                        ArTeamManager.addPointsToTeam(team, value);
                        ArDataBase.muteAdvancement(advname,team);
                        ArTeamManager.syncTeamAdvancement(team, adv, p);
                        ArDataBase.unmuteAdvancement(advname,team);
                    }
                }
            }
        } catch(SQLException | NullPointerException e){
            e.printStackTrace();
        }
        
    }
    
    @EventHandler
    public static void addRootsAdvancementsOnPlayerConnect(PlayerJoinEvent e){
        
        // TODO :
        //  Add a section to the config "base advancements" to add more advancement to be unlocked from the start
        
        Player p = e.getPlayer();
        ArrayList<String> unlocklist = new ArrayList<>();
        unlocklist.add("story/root");
        unlocklist.add("nether/root");
        unlocklist.add("adventure/root");
        unlocklist.add("end/root");
        unlocklist.add("husbandry/root");
        
        for(String adkey : unlocklist){
            Advancement ad = Bukkit.getAdvancement(NamespacedKey.minecraft(adkey));
            for(String c : p.getAdvancementProgress(Objects.requireNonNull(ad)).getRemainingCriteria()){
                p.getAdvancementProgress(ad).awardCriteria(c);
            }
        }
        
        try{
            ArTeamManager.syncPlayerAllAdvancementsWithTeam(p,ArTeamManager.getPlayerTeam(p.getUniqueId()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
