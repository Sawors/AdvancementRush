package com.github.sawors.rewards;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.checkerframework.checker.units.qual.A;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AdvancementRewards {
    
    private static final Map<String, ArReward> rewards = Map.ofEntries(
            Map.entry("story/root", new ArReward(1)),
            Map.entry("story/mine_stone", new ArReward(1)),
            Map.entry("story/upgrade_tools", new ArReward(1))
            
            
    );
    
}
