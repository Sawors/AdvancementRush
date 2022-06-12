package com.github.sawors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

public class SmallListeners implements Listener {
    Team team;
    
    @EventHandler
    public static void onPlayerConnect(PlayerJoinEvent event){
        
        Player p = event.getPlayer();
        /*for(String c : p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).getRemainingCriteria()){
            p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).awardCriteria(c);
        }*/
    }
}
