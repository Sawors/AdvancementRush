package com.github.sawors.advancements;

import java.util.Map;

public class AdvancementRewards {
    
    private static final Map<String, ArReward> rewards = Map.ofEntries(
            Map.entry("story/root", new ArReward(1)),
            Map.entry("story/mine_stone", new ArReward(1)),
            Map.entry("story/upgrade_tools", new ArReward(1))
            
            
    );
    
}
