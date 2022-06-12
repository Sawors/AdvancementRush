package com.github.sawors.advancements;

import org.bukkit.scheduler.BukkitRunnable;

public class ArReward {
    private int value;
    private BukkitRunnable reward;
    
    ArReward(int points, BukkitRunnable bonus){
    
    }
    ArReward(int points){
    
    }
    
    public void setValue(int val){
        value = val;
    }
    
    public void setReward(BukkitRunnable rwrd){
        reward = rwrd;
    }
    
    public int getValue(){
        return value;
    }
    
    public BukkitRunnable getReward(){
        return reward;
    }
    
    
}
