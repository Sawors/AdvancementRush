package com.github.sawors;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListeners implements Listener {
    
    @EventHandler
    public static void onPlayerUnlockAdvancement(PlayerAdvancementDoneEvent event){
        if(event.getAdvancement().getKey().getKey().contains("recipes/")){
            return;
        }
        
        Player p = event.getPlayer();
        if(p.getAdvancementProgress(event.getAdvancement()).getRemainingCriteria().size()<1){
            p.sendMessage(Component.text(DataBase.getAdvancementValue(event.getAdvancement().getKey().getKey())));
        }
        //DataBase.getAdvancementValue(event.getAdvancement().toString());
    }
}
