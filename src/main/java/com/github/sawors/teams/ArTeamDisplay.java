package com.github.sawors.teams;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ArTeamDisplay {
    
    //LINES :
    // 8    top 1
    // 7    top 2
    // 6    top 3
    // 5    top 4
    // 4    top 5
    // 0    your team
    
    public static void initScoreboard(){
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        resetScoreboard();
        
        Objective test = sb.getObjective("ar_teams");
        Component title = Component.text(ChatColor.DARK_GRAY+"> "+ChatColor.GREEN+""+ChatColor.BOLD+"Advancement "+ChatColor.YELLOW+""+ChatColor.BOLD+"Rush"+ChatColor.DARK_GRAY+" <");
        if(test == null){
            test = sb.registerNewObjective("ar_teams","none",title);
        }
        test.displayName(title);
        test.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        test.getScore(ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                              ").setScore(9);
        test.getScore(ChatColor.GOLD+"         Top Teams").setScore(9);
        test.getScore(ChatColor.LIGHT_PURPLE+" 1. ").setScore(8);
        test.getScore(ChatColor.LIGHT_PURPLE+" 2. ").setScore(7);
        test.getScore(ChatColor.LIGHT_PURPLE+" 3. ").setScore(6);
        test.getScore(ChatColor.LIGHT_PURPLE+" 4. ").setScore(5);
        test.getScore(ChatColor.LIGHT_PURPLE+" 5. ").setScore(4);
        test.getScore(ChatColor.LIGHT_PURPLE+" ...").setScore(3);
        test.getScore(ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                              ").setScore(2);
        test.getScore(ChatColor.GOLD+"         Your Team").setScore(1);
        test.getScore(ChatColor.LIGHT_PURPLE+"→17. "+ChatColor.BLUE+"CockSuckers : "+ChatColor.WHITE+"-12").setScore(0);
    }
    
    public static void resetScoreboard(){
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for(String entry : sb.getEntries()){
            sb.resetScores(entry);
        }
    }
    
    public static void updateTopFiveTeams(){
    
    }
    
    public static void updateSelfTeam(){
    
    }
}
