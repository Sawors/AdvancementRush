package com.github.sawors.teams;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.lang.reflect.MalformedParametersException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class ArTeamManager {
    
    
    // |====================================[GIT GUD]=====================================|
    // |                    Reminder for the newbie I am in SQL :                         |
    // | -> Set  : INSERT into [table]([column]) VALUES([value])                          |
    // | -> Get  : SELECT [column] FROM [table] // WHERE [condition]=[something]          |
    // | -> Edit : UPDATE [table] SET [column] = [value] // WHERE [condition]=[something] |
    // | -> Del  : DELETE FROM [table] WHERE [condition]=[something]                      |
    // |==================================================================================|
    
    public static void createTeam(String name, Color color) throws KeyAlreadyExistsException {
        ArTeam tm = new ArTeam(name, color);
        ArDataBase.registerTeam(tm);
    }
    public static void createTeam(ArTeam team) throws KeyAlreadyExistsException {
        ArDataBase.registerTeam(team);
    }
    
    public static void removeTeam(String name) throws NullPointerException{
        ArDataBase.deleteTeam(name);
    }
    
    public static void setTeamColor(String teamname, String colorhex){
        try{
            if(ArDataBase.doesTeamExist(teamname) && java.awt.Color.getColor(colorhex) != null){
                setTeamData(ArTeamData.NAME, teamname, colorhex);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void setTeamPoints(String teamname, int points){
        try{
            if(ArDataBase.doesTeamExist(teamname)){
                setTeamData(ArTeamData.POINTS, teamname, String.valueOf(points));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void setTeamPlayers(String teamname, String players){
        try{
            if(ArDataBase.doesTeamExist(teamname)){
                setTeamData(ArTeamData.PLAYERS, teamname, players);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void setTeamPlayers(String teamname, ArrayList<UUID> players){
        try{
            if(ArDataBase.doesTeamExist(teamname)){
                setTeamData(ArTeamData.PLAYERS, teamname, ArDataBase.teamMembersSerialize(players));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static String getTeamColor(String teamname) throws SQLException{
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.COLOR.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            return rs.getString(target);
        }
    }
    public static int getTeamPoints(String teamname) throws SQLException{
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.POINTS.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            return rs.getInt(target);
        }
    }
    public static String getTeamPlayers(String teamname) throws SQLException{
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.PLAYERS.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if(rs.isClosed()){
                return "";
            }
            return rs.getString(target);
        }
    }
    
    public static boolean playerHasTeam(UUID player){
        return false;
    }
    
    public static void addPlayerToTeam(String teamname, UUID playerid) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
        // add player to team if not in it
        if (!output.contains(playerid)) {
            output.add(playerid);
        } else {
            //ArDataBase.setPlayerTeamLink(playerid, teamname);
            throw new KeyAlreadyExistsException("this player is already in this team");
           
        }
        setTeamPlayers(teamname, ArDataBase.teamMembersSerialize(output));
        Main.logAdmin(ArDataBase.teamMembersSerialize(output));
        //ArDataBase.setPlayerTeamLink(playerid, teamname);
    }
    public static void changePlayerTeam(String newteam, UUID playerid) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        try{
            ArTeamManager.removePlayerFromTeam(getPlayerTeam(playerid), playerid);
        } catch (Exception e){
            //catch everything NOT RECOMMENDED
            Main.logAdmin("player has no team");
        }
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(newteam));
        // add player to team if not in it
        if (!output.contains(playerid)) {
            output.add(playerid);
        } else {
            //ArDataBase.setPlayerTeamLink(playerid, newteam);
            throw new KeyAlreadyExistsException("this player is already in this team");
        
        }
        setTeamPlayers(newteam, ArDataBase.teamMembersSerialize(output));
        Main.logAdmin(ArDataBase.teamMembersSerialize(output));
        //ArDataBase.setPlayerTeamLink(playerid, newteam);
    }
    
    public static void removePlayerFromTeam(String teamname, UUID playerid) throws SQLException, MalformedParametersException {
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
        output.remove(playerid);
        setTeamPlayers(teamname, ArDataBase.teamMembersSerialize(output));
        //ArDataBase.setPlayerTeamLink(playerid, "");
    }
    public static void addPointsToTeam(String teamname, int points) throws SQLException, MalformedParametersException {
        setTeamPoints(teamname, getTeamPoints(teamname)+points);
    }
    
    private static void setTeamData(ArTeamData datatype, String teamname, String data){
        try(Connection co = ArDataBase.connect()){
            //yes this "if" is ridiculous but used to avoid database errors
            String query;
            if(datatype == ArTeamData.POINTS){
                query = "UPDATE teams SET "+datatype+"="+data+" WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            } else {
                query = "UPDATE teams SET "+datatype+"='"+data+"' WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            }
            Main.logAdmin("update query -> "+query);
            co.createStatement().execute(query);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> getTeamList(){
        ArrayList<String> teamlist = new ArrayList<>();
        try(Connection co = ArDataBase.connect()){
            PreparedStatement statement = co.prepareStatement("SELECT NAME FROM teams");
            ResultSet rset = statement.executeQuery();
            while(rset.next()){
                teamlist.add(rset.getString("NAME"));
            }
        }catch (SQLException e){
            //e.printStackTrace();
        }
        return teamlist;
    }
    
    public static String getPlayerTeam(UUID id) throws SQLException, NullPointerException{
        try(Connection co = ArDataBase.connect()){
            PreparedStatement statement = co.prepareStatement("SELECT NAME FROM teams WHERE PLAYERS LIKE '%"+id+"%'");
            Main.logAdmin("SELECT NAME FROM teams WHERE PLAYERS LIKE '%"+id+"%'");
            ResultSet rset = statement.executeQuery();
            if(!rset.isClosed() && rset.getString("NAME") != null){
                return rset.getString("NAME");
            } else {
                throw new NullPointerException("player is not in a team");
            }
        }
    }
    
    public static void syncTeamAdvancement(String teamname, Advancement adv, Player source){
        try{
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
            for(UUID id : players){
                if(!source.getUniqueId().equals(id)){
                    try{
                        Main.logAdmin("idsource : "+source.getUniqueId());
                        Main.logAdmin("id : "+id);
                        Main.logAdmin("size : "+source.getAdvancementProgress(adv).getAwardedCriteria().size());
                        for(String crit : source.getAdvancementProgress(adv).getAwardedCriteria()){
                                Main.logAdmin("crit : "+crit);
                                Player p = Bukkit.getPlayer(id);
                                if(p != null){
                                    p.getAdvancementProgress(adv).awardCriteria(crit);
                                }
                        }
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void syncPlayerAdvancementWithTeam(Player target, String teamsource, Advancement adv){
        try {
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamsource));
            
            for (UUID id : players) {
                if (id != target.getUniqueId() && Bukkit.getPlayer(id) != null && Objects.requireNonNull(Bukkit.getPlayer(id)).isOnline()) {
                    AdvancementProgress referenceprogress = Objects.requireNonNull(Bukkit.getPlayer(id)).getAdvancementProgress(adv);
                    for(String crit : referenceprogress.getAwardedCriteria()){
                        target.getAdvancementProgress(adv).awardCriteria(crit);
                    }
                    return;
                }
            }
            
        } catch (SQLException | NullPointerException e){
            e.printStackTrace();
        }
    }
    
    
    // TODO :
    //  Maybe as all sync methods are pretty slow we might try to make them asynchronous, however I don't know how
    public static void syncPlayerAllAdvancementsWithTeam(Player target, String teamsource){
        try {
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamsource));
            ArrayList<String> mutelist = new ArrayList<>();
            for (UUID id : players) {
                if (id != target.getUniqueId() && Bukkit.getPlayer(id) != null && Objects.requireNonNull(Bukkit.getPlayer(id)).isOnline()) {
                    for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
                        Advancement adv = it.next();
                        AdvancementProgress referenceprogress = Objects.requireNonNull(Bukkit.getPlayer(id)).getAdvancementProgress(adv);
                        if(target.getAdvancementProgress(adv) != referenceprogress){
                            ArDataBase.muteAdvancement(adv.getKey().getKey(), teamsource);
                            mutelist.add(adv.getKey().getKey());
                            for(String crit : referenceprogress.getAwardedCriteria()){
                                target.getAdvancementProgress(adv).awardCriteria(crit);
                            }
                        }
                    }
                    for(String advname : mutelist){
                        ArDataBase.unmuteAdvancement(advname, teamsource);
                    }
                    
                    return;
                }
            }
        
        } catch (SQLException | NullPointerException e){
            e.printStackTrace();
        }
    }
    
    //  TODO :
    //      Maybe log all advancements to the database pretty much like we've done with players in teams.
    //      I believe this could provide us a more stable way to register advancement completion and synchronization
    
    public static void syncAllTeamAdvancements(String teamname){
    
    }
}
