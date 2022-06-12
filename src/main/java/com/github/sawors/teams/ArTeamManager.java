package com.github.sawors.teams;

import org.bukkit.Color;

import java.util.ArrayList;
import java.util.Objects;

public class ArTeamManager {
    
    private static ArrayList<ArTeam> arteams = new ArrayList<>();
    
    public static ArTeam createTeam(String name, Color color) throws IllegalArgumentException{
        ArTeam team = new ArTeam(name, color);
        if(!arteams.contains(team)){
            arteams.add(team);
        } else{
            throw new IllegalArgumentException("team already exists");
        }
        
        return team;
    }
    
    public static void removeTeam(String name) throws IllegalArgumentException{
        boolean found = false;
        for(ArTeam t : arteams){
            if(Objects.equals(t.getName(), name)){
                arteams.remove(t);
                found = true;
            }
        }
        if(!found){
            throw new IllegalArgumentException("Team not found");
        }
    }
    
    public static ArrayList<ArTeam> getTeams(){
        return arteams;
    }
}
