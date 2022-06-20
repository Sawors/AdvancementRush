package com.github.sawors.advancements;

import org.bukkit.advancement.Advancement;

public class AdvancementManager {
    
    
    public static boolean isRecipe(String adv){
        return adv.contains("recipes/");
    }
    
    public static boolean isRecipe(Advancement adv){
        return  isRecipe(adv.getKey().getKey());
    }
    
    public static String getAdvancementWithoutKey(String advancement){
        String nokeyadv = "";
        StringBuilder strb = new StringBuilder();
        if(advancement.contains(":")){
            int start = 0;
            char[] nokeychar = advancement.toCharArray();
            for(int i = 0; i<nokeychar.length; i++){
                if(nokeychar[i] == ':'){
                    start = i+1;
                }
            }
            for(int i = start; i<nokeychar.length; i++){
                strb.append(nokeychar[i]);
            }
            return strb.toString();
        } else {
            return advancement;
        }
    }
}
