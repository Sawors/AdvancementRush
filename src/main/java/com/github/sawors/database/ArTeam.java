package com.github.sawors.database;

import java.util.Set;
import java.util.UUID;

public class ArTeam {
    
    private String name;
    private int points;
    private Set<UUID> players;
    // maybe change this to a Set of AdvancementProgress ?
    private Set<String> advancements;
    
    
    
    
    public int getPoints() {
        return points;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    
    public Set<UUID> getPlayers() {
        return players;
    }
    public void setPlayers(Set<UUID> players) {
        this.players = players;
    }
    
    public Set<String> getAdvancements() {
        return advancements;
    }
    public void setAdvancements(Set<String> advancements) {
        this.advancements = advancements;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
