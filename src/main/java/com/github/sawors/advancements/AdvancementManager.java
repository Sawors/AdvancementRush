package com.github.sawors.advancements;

import com.github.sawors.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdvancementManager {
    
    
    public static boolean isRecipe(@NotNull String adv){
        return adv.contains("recipes/");
    }
    
    public static boolean isRecipe(@NotNull Advancement adv){
        return  isRecipe(adv.getKey().getKey());
    }
    
    public static void grantRootAdvancements(Player p){
        ArrayList<NamespacedKey> unlocklist = new ArrayList<>();
        List<String> unlocklist2 = Main.getMainConfig().getStringList("auto-unlock");
        NamespacedKey namekey;
        for(String key : unlocklist2){
            namekey = !key.contains(":") ? NamespacedKey.minecraft(key) : NamespacedKey.fromString(key);
            if(namekey != null && Bukkit.getAdvancement(namekey) != null){
                Advancement ad = Bukkit.getAdvancement(namekey);
                for(String c : p.getAdvancementProgress(Objects.requireNonNull(ad)).getRemainingCriteria()){
                    p.getAdvancementProgress(ad).awardCriteria(c);
                }
            }
        }
    }
}
