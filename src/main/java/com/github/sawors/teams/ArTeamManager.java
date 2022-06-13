package com.github.sawors.teams;

import com.github.sawors.DataBase;
import org.bukkit.Color;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ArTeamManager {
    
    private static ArrayList<ArTeam> arteams = new ArrayList<>();
    private static HashMap<UUID, ArTeam> playerteamlink = new HashMap<>();
    
    
    // TODO
    //  -> separate the "load team" from the actual team creation : creating a team is a DB operation, loading one is an ArrayList/HashMap operation
    //  -> do we really need to use ArrayLists/Hashmaps ? I think using only the database might be even more efficient than trying to always sync DB with Java Collections
    //      > if we use only DB we should IMPERATIVELY handle ALL the team operations with methods (getters and setters)
    
    @Deprecated
    public static ArTeam createTeam(String name, Color color) throws IllegalArgumentException{
        ArTeam team = new ArTeam(name, color,0,new ArrayList<>());
        IllegalArgumentException exc = new IllegalArgumentException("team already exists");
        try{
            registerTeam(team);
        } catch(SQLException e){
            throw exc;
        }
        if(!arteams.contains(team)){
            arteams.add(team);
        } else{
            throw exc;
        }
        return team;
    }
    
    @Deprecated
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
    
    @Deprecated
    public static ArrayList<ArTeam> getTeams(){
        return arteams;
    }
    
    @Deprecated
    private static void registerTeam(ArTeam team) throws SQLException{
        try(Connection co = DataBase.connect()){
            co.createStatement().execute("INSERT INTO teams(name,color,points,players) VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+",'"+DataBase.teamMembersSerialize(team)+"')");
        }
    }
}
