package com.github.sawors.advancements;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;

public class AdvancementManager {
    
    
    public static boolean isRecipe(String adv){
        return adv.contains("recipes/");
    }
    
    public static boolean isRecipe(Advancement adv){
        return  isRecipe(adv.getKey().getKey());
    }
    
    public static void grantRootAdvancements(Player p){
        ArrayList<NamespacedKey> unlocklist = new ArrayList<>();
        unlocklist.add(NamespacedKey.minecraft("story/root"));
        unlocklist.add(NamespacedKey.minecraft("nether/root"));
        unlocklist.add(NamespacedKey.minecraft("adventure/root"));
        unlocklist.add(NamespacedKey.minecraft("end/root"));
        unlocklist.add(NamespacedKey.minecraft("husbandry/root"));
        // TODO : config
        //  add config for custom auto-unlock advancements here
        
        
        
        for(NamespacedKey adkey : unlocklist){
            Advancement ad = Bukkit.getAdvancement(adkey);
            for(String c : p.getAdvancementProgress(Objects.requireNonNull(ad)).getRemainingCriteria()){
                p.getAdvancementProgress(ad).awardCriteria(c);
            }
        }
    }
}
