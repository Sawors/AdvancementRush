package com.github.sawors.teams;

import com.github.sawors.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.util.List;

public class ArTeamDisplay {
    
    //LINES :
    // 9    top 1
    // 8    top 2
    // 7    top 3
    // 6    top 4
    // 5    top 5
    // 1    your team
    
    
    
    public static void updatePlayerScoreboard(Player player, String team){
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        resetScoreboard(sb);
        
        Objective test = sb.getObjective("ar_teams");
        Component title = Component.text(ChatColor.GREEN+""+ChatColor.BOLD+"Advancement  "+ChatColor.DARK_GRAY+"<<");
        if(test == null){
            test = sb.registerNewObjective("ar_teams","none",title);
        }
        test.displayName(title);
        test.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        setDisplayFormat(test);
        if(team != null){
            setSelfTeam(test, team);
        }
        setTopFiveTeams(test);
        player.setScoreboard(sb);
    }
    
    public static void resetScoreboard(Scoreboard scb){
        for(String entry : scb.getEntries()){
            scb.resetScores(entry);
        }
    }
    
    public static void setTopFiveTeams(Objective objective){
        if(objective.getScoreboard() == null){
            return;
        }
        List<String> ranking = ArTeamManager.getTeamsRanking();
        Main.logAdmin(ranking.toString());
        // TODO : config
        int topteamssize = 3;
        for(int i = 0; i<topteamssize; i++){
            if(ranking.size() > i){
                String team = ranking.get(i);
                int points = 0;
                try{
                    points = ArTeamManager.getTeamPoints(team);
                } catch(SQLException ignored){}
    
                String position = String.valueOf(i+1);
                Team displayself = objective.getScoreboard().getTeam(team) == null ? objective.getScoreboard().registerNewTeam(team) : objective.getScoreboard().getTeam(team);
                displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+" "+position+". ").append(getTeamDisplayWithPoints(team, points)));
                StringBuilder identifierunique = new StringBuilder();
                for(int i2 = 0; i2<=i; i2++){
                    identifierunique.append(ChatColor.RESET + "");
                }
                String identifier = identifierunique.toString();
                for(String entry : displayself.getEntries()){
                    displayself.removeEntry(entry);
                }
                displayself.addEntry(identifier);
                objective.getScore(identifier).setScore(9-i);
                //objective.getScore(ChatColor.LIGHT_PURPLE+" "+i+". ").setScore(9-i);
            } else {
                objective.getScore(ChatColor.LIGHT_PURPLE+" "+(i+1)+". ").setScore(9-i);
            }
        }
    }
    
    public static void setSelfTeam(Objective objective, String team){
        if(objective.getScoreboard() == null){
            return;
        }
        int points = 0;
        try{
            points = ArTeamManager.getTeamPoints(team);
        } catch(SQLException ignored){}
        
        int pos = ArTeamManager.getTeamRank(team);
        String position = String.valueOf(pos);
        Team displayself = objective.getScoreboard().getTeam(team+"_") == null ? objective.getScoreboard().registerNewTeam(team+"_") : objective.getScoreboard().getTeam(team+"_");
        displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+" "+position+". ").append(getTeamDisplayWithPoints(team, points)));
        StringBuilder identifierunique = new StringBuilder();
        for(int i2 = 0; i2<=6; i2++){
            identifierunique.append(ChatColor.RESET + "");
        }
        String identifier = identifierunique.toString();
        displayself.addEntry(identifier);
        objective.getScore(identifier).setScore(1);
    }
    
    public static void setDisplayFormat(Objective objective){
        objective.getScore(ChatColor.YELLOW+""+ChatColor.BOLD+"     Rush").setScore(12);
        objective.getScore(ChatColor.GRAY+" "+ChatColor.STRIKETHROUGH+"                   ").setScore(11);
        objective.getScore(ChatColor.GOLD+"    Top Teams").setScore(10);
        objective.getScore(ChatColor.LIGHT_PURPLE+" ...").setScore(4);
        objective.getScore(ChatColor.RESET+" "+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(3);
        objective.getScore(ChatColor.GOLD+"    Your Team").setScore(2);
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(0);
    }
    
    public static Component getTeamDisplayWithPoints(String team, int pts){
        String teamname = team;
        String points = String.valueOf(pts);
        // = total characters per line - fixed amount of chars (format) - points chars
        int maxlength = 20-8-points.length();
        if(team.length() > maxlength){
            StringBuilder newname = new StringBuilder();
            char[] teamchar = team.toCharArray();
            for(int i = 0; i<maxlength-3; i++){
                newname.append(teamchar[i]);
            }
            newname.append('.');
            newname.append('.');
            newname.append('.');
            teamname = newname.toString();
        }
        return Component.text(teamname).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team))).append(Component.text(" : "+ChatColor.WHITE+points));
    }
}
