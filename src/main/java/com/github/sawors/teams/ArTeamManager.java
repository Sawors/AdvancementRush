package com.github.sawors.teams;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.UsefulTools;
import com.github.sawors.advancements.AdvancementManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import java.util.*;

public class ArTeamManager {
    
    
    // |====================================[GIT GUD]=====================================|
    // |                    Reminder for the newbie I am in SQL :                         |
    // | -> Set  : INSERT into [table]([column]) VALUES([value])                          |
    // | -> Get  : SELECT [column] FROM [table] // WHERE [condition]=[something]          |
    // | -> Edit : UPDATE [table] SET [column] = [value] // WHERE [condition]=[something] |
    // | -> Del  : DELETE FROM [table] WHERE [condition]=[something]                      |
    // |==================================================================================|
    
    public static void createTeam(String name, Color color) throws KeyAlreadyExistsException {
        ArDataBase.registerTeam(name, UsefulTools.getColorHex(color), 0, new ArrayList<>());
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
    
    public static String getTeamColor(String teamname){
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.COLOR.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            return rs.getString(target);
        } catch(SQLException e){
            return UsefulTools.getColorHex(Color.WHITE);
        }
    }
    public static int getTeamPoints(String teamname) {
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.POINTS.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            return rs.getInt(target);
        } catch (SQLException e){
            e.printStackTrace();
            return 0;
        }
    }
    public static String getTeamPlayers(String teamname) {
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.PLAYERS.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if(rs.isClosed()){
                return "[]";
            }
            return rs.getString(target);
        } catch (SQLException e){
            return "[]";
        }
    }
    
    public static void addPlayerToTeam(String teamname, UUID playerid) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
        // add player to team if not in it
        if (!output.contains(playerid)) {
            output.add(playerid);
        } else {
            throw new KeyAlreadyExistsException("this player is already in this team");
           
        }
        setTeamPlayers(teamname, ArDataBase.teamMembersSerialize(output));
        Player p = Bukkit.getPlayer(playerid);
        if(p != null && p.isOnline()) {
            ArTeamManager.syncPlayerAllAdvancementsWithTeam(p, teamname);
            ArTeamManager.syncPlayerColorWithTeam(p);
            ArTeamDisplay.updatePlayerDisplay(p, teamname);
        } else {
            Main.logAdmin("could not sync player "+p.getName()+" with team "+teamname+" for this player is offline");
        }
    }
    public static void changePlayerTeam(String newteam, UUID playerid) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        String team = getPlayerTeam(playerid);
        if(team != null){
            ArTeamManager.removePlayerFromTeam(team, playerid);
        } else {
            Main.logAdmin("Player "+Bukkit.getOfflinePlayer(playerid).getName()+" has no team");
        }
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(newteam));
        // add player to team if not in it
        try{
            addPlayerToTeam(newteam,playerid);
        } catch (KeyAlreadyExistsException e){
            e.printStackTrace();
        }
    }
    
    public static void removePlayerFromTeam(String teamname, UUID playerid) throws SQLException, MalformedParametersException {
        ArrayList<UUID> output = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
        output.remove(playerid);
        setTeamPlayers(teamname, ArDataBase.teamMembersSerialize(output));
        //ArDataBase.setPlayerTeamLink(playerid, "");
    }
    public static void addPointsToTeam(String teamname, int points) throws SQLException, MalformedParametersException {
        setTeamPoints(teamname, getTeamPoints(teamname)+points);
        for(Player p : Bukkit.getOnlinePlayers()){
            String team = getPlayerTeam(p.getUniqueId());
            if(team != null){
                ArTeamDisplay.updatePlayerDisplay(p, teamname);
            }
        }
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
    
    public static String getPlayerTeam(UUID id){
        try(Connection co = ArDataBase.connect()){
            PreparedStatement statement = co.prepareStatement("SELECT NAME FROM teams WHERE PLAYERS LIKE '%"+id+"%'");
            ResultSet rset = statement.executeQuery();
            if(!rset.isClosed() && rset.getString("NAME") != null){
                return rset.getString("NAME");
            } else {
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    public static void setTeamAdvancements(String teamname, String advancements){
        try{
            if(ArDataBase.doesTeamExist(teamname)){
                setTeamData(ArTeamData.ADVANCEMENTS, teamname, advancements);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static String getTeamAdvancements(String teamname) throws SQLException{
        try(Connection co = ArDataBase.connect()){
            String target = ArTeamData.ADVANCEMENTS.toString();
            String query = "SELECT "+target+" FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if(rs.isClosed()){
                return "";
            }
            return rs.getString(target);
        }
    }
    
    public static ArrayList<String> getTeamsWithAdvancement(NamespacedKey adv) throws SQLException, NullPointerException{
        try(Connection co = ArDataBase.connect()){
            ArrayList<String> list = new ArrayList<>();
            String stmt = "SELECT NAME FROM teams WHERE "+ArTeamData.ADVANCEMENTS+" LIKE '%"+adv.toString()+"(%'";
            PreparedStatement statement = co.prepareStatement(stmt);
            ResultSet rset = statement.executeQuery();
            if(!rset.isClosed() && rset.getString("NAME") != null){
                while(rset.next()){
                    list.add(rset.getString("NAME"));
                }
                return list;
            } else {
                throw new NullPointerException("no team with advancement "+adv);
            }
        }
    }
    
    public static void addAdvancementToTeam(String teamname, NamespacedKey advancement) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        ArrayList<String> output = new ArrayList<>();
        if(!getTeamAdvancements(teamname).contains("[]")){
            output = ArDataBase.teamAdvancementsDeserialize(getTeamAdvancements(teamname));
        }
        // add advancement to team if not in it
        
        if (!output.contains(advancement.toString())) {
            output.add(ArDataBase.advancementCriteriaSerialize(advancement, Bukkit.getAdvancement(advancement).getCriteria()));
        } else {
            throw new KeyAlreadyExistsException("this advancement is already unlocked for this team");
        }
        setTeamAdvancements(teamname, ArDataBase.teamAdvancementsSerialize(output));
    }
    public static void removeAdvancementFromTeam(String teamname, NamespacedKey advancement) throws SQLException, MalformedParametersException {
        ArrayList<String> output = ArDataBase.teamAdvancementsDeserialize(getTeamAdvancements(teamname));
        output.removeIf(adv -> adv.contains(advancement.toString()));
        setTeamAdvancements(teamname, ArDataBase.teamAdvancementsSerialize(output));
    }
    public static boolean hasTeamAdvancement(String team, NamespacedKey advancement) throws SQLException{
        boolean result = false;
        try{
            result = getTeamsWithAdvancement(advancement).contains(team);
        } catch(NullPointerException ignored){
        }
        return result;
    }
    
    public static boolean hasTeamAdvancementCompleted(String team, NamespacedKey advancement) throws SQLException{
        try{
            if(getTeamsWithAdvancement(advancement).contains(team)){
                for(String adv : ArDataBase.teamAdvancementsDeserialize(getTeamAdvancements(team))) {
                    if(adv.contains(advancement.toString())){
                        ArrayList<String> criteria = ArDataBase.advancementCriteriaDeserialize(adv);
                        try{
                            for(String refcrit : Objects.requireNonNull(Bukkit.getAdvancement(advancement)).getCriteria()){
                                if(!criteria.contains(refcrit)){
                                    return false;
                                }
                            }
                        } catch (NullPointerException e){
                            return false;
                        }
                        return true;
                    }
                }
            } else {
                return false;
            }
        } catch(NullPointerException ignored){
        }
        return false;
    }
    
    public static void addCriterionToTeam(String teamname, NamespacedKey advancement, String criterion) throws SQLException, MalformedParametersException, KeyAlreadyExistsException{
        ArrayList<String> advs = new ArrayList<>();
        if(!getTeamAdvancements(teamname).contains("[]")){
            advs = ArDataBase.teamAdvancementsDeserialize(getTeamAdvancements(teamname));
        }
        
        if(!hasTeamAdvancement(teamname, advancement)){
            ArrayList<String> crits = new ArrayList<>();
            crits.add(criterion);
            String newadv = ArDataBase.advancementCriteriaSerialize(advancement, crits);
            advs.add(newadv);
            setTeamAdvancements(teamname, ArDataBase.teamAdvancementsSerialize(advs));
        } else {
            for(String checkadv : advs){
                if(checkadv.contains(advancement.toString())){
                    ArrayList<String> crits = ArDataBase.advancementCriteriaDeserialize(checkadv);
            
            
                    // add criterion to team if not in it
            
                    if (!crits.contains(criterion)) {
                        crits.add(criterion);
                    } else {
                        throw new KeyAlreadyExistsException("criterion "+criterion+" is already unlocked for this team");
                    }
            
                    String newadv = ArDataBase.advancementCriteriaSerialize(advancement, crits);
                    advs.remove(checkadv);
                    advs.add(newadv);
                    setTeamAdvancements(teamname, ArDataBase.teamAdvancementsSerialize(advs));
                    break;
                }
            }
        }
        
    }
    
    public static boolean isTeamFirstOnAdvancement(String team, Advancement advancement){
        try{
            ArrayList<String> teamlist = getTeamsWithAdvancement(advancement.getKey());
            return teamlist.contains(team) && teamlist.size() <= 1;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    
    //Sync an advancement for the whole team
    public static void syncTeamAdvancement(String teamname, Advancement adv){
        ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(getTeamPlayers(teamname));
        for(UUID id : players){
            syncPlayerAdvancementWithTeam(Bukkit.getPlayer(id),teamname,adv);
        }
    }
    
    //Sync a player's advancement with his team
    public static void syncPlayerAdvancementWithTeam(Player target, String teamsource, Advancement adv){
        ArDataBase.muteAdvancement(adv.getKey(), teamsource);
        try {
            AdvancementProgress targetprogress = target.getAdvancementProgress(adv);
            if(target.isOnline() && !AdvancementManager.isRecipe(adv)){
                ArDataBase.muteAdvancement(adv.getKey(),teamsource);
                //delete every criteria
                for(String crit : targetprogress.getAwardedCriteria()){
                    targetprogress.revokeCriteria(crit);
                }
                //add back team's criteria
                if(hasTeamAdvancement(teamsource, adv.getKey()) && ArDataBase.shouldSync(adv)){
                    for(String advcheck : ArDataBase.teamAdvancementsDeserialize(getTeamAdvancements(teamsource))){
                        if(advcheck.contains(adv.getKey().getKey())){
                            for(String crit : ArDataBase.advancementCriteriaDeserialize(advcheck)){
                                targetprogress.awardCriteria(crit);
                            }
                        }
                    }
                }
                ArDataBase.unmuteAdvancement(adv.getKey(),teamsource);
            }
        
        } catch (SQLException | NullPointerException e){
            e.printStackTrace();
        } catch (KeyAlreadyExistsException exception){
            Main.logAdmin(exception.getMessage());
        }finally {
            ArDataBase.unmuteAdvancement(adv.getKey(), teamsource);
        }
    }
    
    
    // TODO :
    //  Maybe as all sync methods are pretty heavy we might try to make them asynchronous, however I don't know how
    //sync every player's advancement with his team
    public static void syncPlayerAllAdvancementsWithTeam(Player target, String teamsource){
        try {
            for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
                Advancement adv = it.next();
                if(!AdvancementManager.isRecipe(adv)){
                    syncPlayerAdvancementWithTeam(target,teamsource,adv);
                }
                
            }
        
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    
    public static void syncPlayerColorWithTeam(Player p){
        Component pname = p.displayName();
        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
        if(team != null){
            p.displayName(pname.color(TextColor.fromCSSHexString(ArTeamManager.getTeamColor(team))));
            p.playerListName(p.playerListName().color(TextColor.fromCSSHexString(ArTeamManager.getTeamColor(team))));
        } else {
            Main.logAdmin("Player "+p.getName()+" has no team");
            p.displayName(pname.color(TextColor.color(0xFFFFFF)));
            p.playerListName(pname.color(TextColor.color(0xFFFFFF)));
        }
    }
    
    public static Component getTeamColoredName(String team){
        return Component.text(team).color(TextColor.color(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)).asRGB()));
    }
    
    public static void playSoundForTeam(String team, Sound sound, float pitch){
        try{
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team));
            for(UUID id : players){
                Player soundtarget = Bukkit.getPlayer(id);
                if( soundtarget != null && soundtarget.isOnline()){
                    soundtarget.playSound(soundtarget.getLocation(), sound,1,pitch);
                }
            }
        } catch (
                MalformedParametersException e){
            e.printStackTrace();
        }
    }
    
    public static List<String> getTeamsRanking(){
        ArrayList<String> ranking = new ArrayList<>();
        try(Connection co = ArDataBase.connect()){
            String query = "SELECT "+ArTeamData.NAME+
                    " FROM teams" +
                    " ORDER BY "+ArTeamData.POINTS+" DESC;";
            PreparedStatement stmt = co.prepareStatement(query);
            ResultSet rset = stmt.executeQuery();
            while(rset.next()){
                ranking.add(rset.getString(ArTeamData.NAME.toString()));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Collections.unmodifiableList(ranking);
    }
    
    public static int getTeamRank(String teamname){
        return getTeamsRanking().indexOf(teamname)+1;
    }
    
    public static boolean doesPlayerHaveTeam(Player p){
        return doesPlayerHaveTeam(p.getUniqueId());
    }
    public static boolean doesPlayerHaveTeam(UUID pid){
        return getPlayerTeam(pid) != null;
    }
    
    public static boolean doesTeamExist(String team){
        try(Connection co = ArDataBase.connect()){
            String query = "SELECT "+ArTeamData.NAME+" FROM teams WHERE "+ArTeamData.NAME+"='"+team+"'";
            PreparedStatement statement = co.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            Main.logAdmin(!rs.isClosed()+" "+rs.getString(ArTeamData.NAME.toString()));
            if(!rs.isClosed() && !Objects.equals(rs.getString(ArTeamData.NAME.toString()), "")){
                return true;
            }
        } catch (SQLException e){
            return false;
        }
        return false;
    }
}
