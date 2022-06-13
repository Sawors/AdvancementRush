package com.github.sawors.advancements;

import com.github.sawors.DataBase;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Objects;

public class AdvancementListeners implements Listener {
    
    @EventHandler
    public static void onPlayerUnlockAdvancement(PlayerAdvancementDoneEvent event){
        if(event.getAdvancement().getKey().getKey().contains("recipes/")){
            return;
        }
        
        Player p = event.getPlayer();
        final String key = event.getAdvancement().getKey().getKey();
        if(p.getAdvancementProgress(event.getAdvancement()).isDone()){
            p.sendMessage(Component.text("Value : "+DataBase.getAdvancementValue(key)));
        }
        //DataBase.getAdvancementValue(event.getAdvancement().toString());
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
    }
}
