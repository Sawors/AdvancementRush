package com.github.sawors.teams;

import com.github.sawors.Main;
import com.github.sawors.game.ArGameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.List;

public class ArTeamDisplay {
    
    
    private static int topteamsize = 0;
    private static boolean showpoints = true;
    //this to set default value for line length, thus final
    private static final int maxlinelength_final = 19;
    private static int maxlinelength = maxlinelength_final;
    private static String spacer = " ";
    
    public static void initDisplay(){
        FileConfiguration config = Main.getMainConfig();
        //load scoreboard length
        int rksize = config.getInt("ranking-size");
        if(rksize >= 0 && rksize <= 7){
            topteamsize = rksize;
        }
        
        showpoints = ArGameManager.showScores();
        
    }
    
    public static void updatePlayerDisplay(Player player, String team){
        showpoints = ArGameManager.showScores();
        if(!showpoints){
            spacer = "     ";
            maxlinelength = maxlinelength_final-(spacer.length()/2);
        } else {
            maxlinelength = maxlinelength_final;
        }
        
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        resetScoreboard(sb);
        
        Objective objective = sb.getObjective("ar_teams");
        Component title = Component.text(ChatColor.GREEN+""+ChatColor.BOLD+" Advancement  "+ChatColor.DARK_GRAY+"<<");
        if(objective == null){
            objective = sb.registerNewObjective("ar_teams","none",title);
        }
        objective.displayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore(ChatColor.YELLOW+""+ChatColor.BOLD+"      Rush").setScore(20);
        objective.getScore(ChatColor.GRAY+" "+ChatColor.STRIKETHROUGH+"                   ").setScore(19);
    
        player.sendPlayerListHeader(Component.text(ChatColor.GOLD+""+ChatColor.STRIKETHROUGH+"============"+ChatColor.GREEN+"  "+ChatColor.BOLD+"Advancement "+ChatColor.YELLOW+""+ChatColor.BOLD+"Rush  "+ChatColor.GOLD+""+ChatColor.STRIKETHROUGH+"============"));
        
        switch(ArGameManager.getGamephase()){
            case TEAM_SELECTION:
                setDisplayTeamSelectionFormat(objective, team);
                break;
            case INGAME:
                setDisplayInGameFormat(objective, team);
                break;
            case ENDGAME:
                setDisplayEndGameFormat(objective, team);
                break;
            case WINNER_ANNOUNCEMENT:
                int index = ArTeamManager.getTeamsRanking().size()-ArGameManager.getFinalRankingShowLength();
                if(index >= 0){
                    showPlayerTeamScoreTitle(player,ArTeamManager.getTeamsRanking().get(index));
                }
                setDisplayWinnerAnnouncementFormat(objective, ArGameManager.getFinalRankingShowLength());
                break;
        }
        
        player.setScoreboard(sb);
    }
    
    
    
    public static void resetScoreboard(Scoreboard scb){
        for(String entry : scb.getEntries()){
            scb.resetScores(entry);
        }
    }
    private static void setDisplayTeamSelectionFormat(Objective objective, String team){
        setSelfTeamSelection(objective, team, 1);
    }
    private static void setDisplayInGameFormat(Objective objective, String team){
        if(team != null){
            setSelfTeam(objective, team,1);
        }
        if(topteamsize > 0){
            setTopTeams(objective);
        }
    }
    private static void setDisplayEndGameFormat(Objective objective, String team){
        setSelfTeam(objective,team,5);
        setTimerDisplay(objective);
    }
    private static void setDisplayWinnerAnnouncementFormat(Objective objective, int showlength){
        setFinalRankingDisplayFormat(objective, showlength);
    }
    
    
    private static void setTopTeams(Objective objective){
        if(objective.getScoreboard() == null || !ArGameManager.showRanks()){
            return;
        }
        List<String> ranking = ArTeamManager.getTeamsRanking();
        Main.logAdmin(ranking.toString());
        objective.getScore(ChatColor.GOLD+"    Top Teams").setScore(topteamsize+5);
        for(int i = 0; i<topteamsize; i++){
            if(ranking.size() > i){
                String team = ranking.get(i);
                int points = 0;
                points = ArTeamManager.getTeamPoints(team);
    
                String position = String.valueOf(i+1);
                Team displayself = objective.getScoreboard().getTeam(team) == null ? objective.getScoreboard().registerNewTeam(team) : objective.getScoreboard().getTeam(team);
                if(showpoints){
                    displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+spacer+position+". ").append(getTeamDisplay(team, points)));
                } else {
                    displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+spacer+position+". ").append(getTeamDisplay(team)));
                }
                StringBuilder identifierunique = new StringBuilder();
                for(int i2 = 0; i2<=i; i2++){
                    identifierunique.append(ChatColor.RESET + "");
                }
                String identifier = identifierunique.toString();
                for(String entry : displayself.getEntries()){
                    displayself.removeEntry(entry);
                }
                displayself.addEntry(identifier);
                objective.getScore(identifier).setScore(topteamsize+4-i);
                //objective.getScore(ChatColor.LIGHT_PURPLE+" "+i+". ").setScore(9-i);
            } else {
                objective.getScore(ChatColor.LIGHT_PURPLE+spacer+(i+1)+". ").setScore(topteamsize+4-i);
            }
        }
        objective.getScore(ChatColor.LIGHT_PURPLE+"           ...").setScore(4);
        objective.getScore(" ").setScore(3);
        //objective.getScore(ChatColor.RESET+" "+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(3);
    }
    
    private static void setSelfTeam(Objective objective, String team, int base){
        if(objective.getScoreboard() == null){
            return;
        }
    
        objective.getScore(ChatColor.GOLD+"    Your Team").setScore(base+1);
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(base-1);
        
        int points = ArTeamManager.getTeamPoints(team);
    
        Team displayself = objective.getScoreboard().getTeam(team+"_") == null ? objective.getScoreboard().registerNewTeam(team+"_") : objective.getScoreboard().getTeam(team+"_");
        String position = ArGameManager.showRanks() ? ArTeamManager.getTeamRank(team)+". " : "";
        
        if(showpoints){
            displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+spacer+position).append(getTeamDisplay(team, points, true)));
        } else {
            displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+spacer+position).append(getTeamDisplay(team)));
        }
        StringBuilder identifierunique = new StringBuilder();
        for(int i2 = 0; i2<=6; i2++){
            identifierunique.append(ChatColor.RESET + "");
        }
        String identifier = identifierunique.toString();
        displayself.addEntry(identifier);
        objective.getScore(identifier).setScore(base);
        
    }
    
    private static void setSelfTeamSelection(Objective objective, String team, int base){
        if(objective.getScoreboard() == null){
            return;
        }
    
        objective.getScore(ChatColor.GOLD+"    Your Team").setScore(base+1);
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(base-1);
    
        int points = 0;
        points = ArTeamManager.getTeamPoints(team);
    
        int pos = ArTeamManager.getTeamRank(team);
        String position = String.valueOf(pos);
        Team displayself = objective.getScoreboard().getTeam(team+"_") == null ? objective.getScoreboard().registerNewTeam(team+"_") : objective.getScoreboard().getTeam(team+"_");
        TextComponent teamnamecolored = Component.text("> ").append(getTeamDisplay(team)).append(Component.text(" <"));
        displayself.suffix(teamnamecolored);
        StringBuilder identifierunique = new StringBuilder();
        int teamnamelength = getTeamDisplay(team).content().length();
    
        //TODO : center better the team name
        for(int i2 = 0; i2<(((maxlinelength_final-teamnamelength)-3)/2); i2++){
            identifierunique.append(ChatColor.RESET + " ");
            Main.logAdmin(i2+"");
        }
        String identifier = identifierunique.toString();
        displayself.addEntry(identifier);
        objective.getScore(identifier).setScore(base);
    }
    
    private static void setTimerDisplay(Objective objective){
        if(objective.getScoreboard() == null){
            return;
        }
        objective.getScore(ChatColor.GOLD+" ").setScore(3);
        objective.getScore(ChatColor.GOLD+"        Timer").setScore(2);
        objective.getScore(ChatColor.RED+"      "+ArGameManager.getTimerDisplay()).setScore(1);
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(0);
        
    }
    
    private static void setFinalRankingDisplayFormat(Objective objective, int showlength){
        if(objective.getScoreboard() == null){
            return;
        }
        List<String> ranking = ArTeamManager.getTeamsRanking();
        Main.logAdmin(ranking.toString());
        int rankingsize = ranking.size();
        objective.getScore(ChatColor.GOLD+"    Top Teams").setScore(rankingsize+2);
        Main.logAdmin(showlength+"");
        for(int i = 0; i<showlength; i++){
            String team = ranking.get(rankingsize-i-1);
            int points = ArTeamManager.getTeamPoints(team);
            int pos = ranking.indexOf(team)+1;
            String position = String.valueOf(pos);
            Team displayself = objective.getScoreboard().getTeam(team) == null ? objective.getScoreboard().registerNewTeam(team) : objective.getScoreboard().getTeam(team);
            displayself.suffix(Component.text(ChatColor.LIGHT_PURPLE+spacer+position+". ").append(getTeamDisplay(team, points)));
            StringBuilder identifierunique = new StringBuilder();
            for(int i2 = 0; i2<=i; i2++){
                identifierunique.append(ChatColor.RESET + "");
            }
            String identifier = identifierunique.toString();
            for(String entry : displayself.getEntries()){
                displayself.removeEntry(entry);
            }
            displayself.addEntry(identifier);
            objective.getScore(identifier).setScore(rankingsize-pos+1);
        }
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(0);
    }
    
    private static TextComponent getTeamDisplay(String team, int pts, boolean showpoints){
        String teamname = team;
        String points = String.valueOf(pts);
        // = total characters per line - fixed amount of chars (format) - points chars
        int maxlength = showpoints ? maxlinelength-8-points.length() : maxlinelength-8;
        if(!ArGameManager.showRanks()){
            maxlength += 2;
        }
        if(team.length() > maxlength){
            StringBuilder newname = new StringBuilder();
            char[] teamchar = team.toCharArray();
            for(int i = 0; i<maxlength-2; i++){
                newname.append(teamchar[i]);
            }
            newname.append('.');
            newname.append('.');
            newname.append('.');
            teamname = newname.toString();
        }
        
        TextComponent pointsdisplay = showpoints ? Component.text(" : "+ChatColor.WHITE+points) : Component.text("");
        
        return Component.text(teamname).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team))).append(pointsdisplay);
    }
    private static TextComponent getTeamDisplay(String team, int pts){
        return getTeamDisplay(team,pts,true);
    }
    private static TextComponent getTeamDisplay(String team){
        return getTeamDisplay(team,0,false);
    }
    
    public static void setTopTeamSize(int size){
        topteamsize = size;
    }
    public static void setShowPoints(boolean show){
        showpoints = show;
    }
    private static void showPlayerTeamScoreTitle(Player p, String team){
        int points = ArTeamManager.getTeamPoints(team);
        int duration = ArGameManager.getRankTitleDuration();
        String color = ArTeamManager.getTeamColor(team);
        TextColor rankcolor = TextColor.color(0xFFFF55);
        int rank = ArTeamManager.getTeamRank(team);
        switch(rank){
            case 3:
                rankcolor = TextColor.color(0x6A3805);
                break;
            case 2:
                rankcolor = TextColor.color(0xB4B4B4);
                break;
            case 1:
                rankcolor = TextColor.color(0xFFAA00);
                break;
        }
        p.showTitle(Title.title(Component.text(team).color(TextColor.fromHexString(color)), Component.text(points+" points").color(rankcolor), Title.Times.times(Duration.ofMillis(duration*150L),Duration.ofMillis(duration* 500L),Duration.ofMillis(duration*350L))));
    }
}
