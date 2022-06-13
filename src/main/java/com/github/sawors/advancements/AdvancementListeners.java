package com.github.sawors.advancements;

import com.github.sawors.DataBase;
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
        final String key = event.getAdvancement().getKey().getKey();
        if(p.getAdvancementProgress(event.getAdvancement()).isDone()){
            p.sendMessage(Component.text("Value : "+DataBase.getAdvancementValue(key)));
        }
        //DataBase.getAdvancementValue(event.getAdvancement().toString());
    }
}
