package com.github.sawors;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Criterias;

import java.util.Objects;

public class SmallListeners implements Listener {
    
    @EventHandler
    public static void onPlayerConnect(PlayerJoinEvent event){
        Player p = event.getPlayer();
        Bukkit.broadcast(Component.text(ChatColor.GOLD+"OMG "+p.getName()+" a rejoint !!!!"));
        for(String c : p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).getRemainingCriteria()){
            p.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(NamespacedKey.minecraft("story/upgrade_tools")))).awardCriteria(c);
        }
    }
}
