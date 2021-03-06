package com.github.sawors.teams;

import com.github.sawors.Main;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.game.ArDragonEggManager;
import com.github.sawors.game.ArGameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        //    Top Teams
        objective.getScore(getCenteringSpacer("Top ??quipes",maxlinelength_final)+ChatColor.GOLD+"Top ??quipes").setScore(topteamsize+5);
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
    
    private static void setSelfTeam(@NotNull Objective objective, String team, int base){
        if(objective.getScoreboard() == null ||team == null){
            return;
        }
        //    Your Team
        objective.getScore(getCenteringSpacer("Votre ??quipe",maxlinelength_final)+ChatColor.GOLD+"Votre ??quipe").setScore(base+1);
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
        
        identifierunique.append(getCenteringSpacer(team.length()+String.valueOf(points).length()+position.length()+4, maxlinelength_final));
        
        String identifier = identifierunique.toString();
        displayself.addEntry(identifier);
        objective.getScore(identifier).setScore(base);
        
    }
    
    private static void setSelfTeamSelection(Objective objective, String team, int base){
        if(objective.getScoreboard() == null){
            return;
        }
        //    Your Team
        objective.getScore(getCenteringSpacer("Votre ??quipe",maxlinelength_final)+ChatColor.GOLD+"Votre ??quipe").setScore(base+1);
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
    
        identifierunique.append(getCenteringSpacer(teamnamelength+4, maxlinelength_final));
        
        String identifier = identifierunique.toString();
        displayself.addEntry(identifier);
        objective.getScore(identifier).setScore(base);
    }
    
    private static void setTimerDisplay(Objective objective){
        if(objective.getScoreboard() == null){
            return;
        }
        objective.getScore(ChatColor.GOLD+" ").setScore(3);
        //        Timer
        objective.getScore(getCenteringSpacer("Temps",maxlinelength_final)+ChatColor.GOLD+"Temps").setScore(2);
        objective.getScore(ChatColor.RED+"      "+ArGameManager.getTimerDisplay()).setScore(1);
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(0);
        
    }
    
    private static void setFinalRankingDisplayFormat(Objective objective, int showlength){
        if(objective.getScoreboard() == null){
            return;
        }
        List<String> ranking = ArTeamManager.getTeamsRanking();
        int rankingsize = ranking.size();
        objective.getScore( getCenteringSpacer("Top ??quipes",maxlinelength_final)+ChatColor.GOLD+"Top ??quipes").setScore(rankingsize+2);
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
        
        // case last team added to scoreboard
        objective.getScore(ChatColor.RESET+" "+ChatColor.RESET+""+ChatColor.GRAY+""+ChatColor.STRIKETHROUGH+"                   ").setScore(0);
    }
    
    protected static TextComponent getTeamDisplay(String team, int pts, boolean showpoints){
        String teamname = team;
        if(team == null){
            return Component.text(ChatColor.WHITE+"Pas d'??quipe");
        }
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
    protected static TextComponent getTeamDisplay(String team, int pts){
        return getTeamDisplay(team,pts,true);
    }
    protected static TextComponent getTeamDisplay(String team){
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
        TextColor color = TextColor.fromHexString(ArTeamManager.getTeamColor(team));
        TextColor rankcolor = TextColor.color(0xFFFF55);
        List<UUID> ids = new ArrayList<>(ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team)));
        StringBuilder playernames = new StringBuilder();
        for(int i = 0; i<ids.size(); i++){
            OfflinePlayer plr = Bukkit.getOfflinePlayer(ids.get(i));
            playernames.append(plr.getName());
            if(i< ids.size()-1){
                playernames.append(" - ");
            }
        }
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
        p.showTitle(Title.title(Component.text(team).color(color), Component.text(points+" points").color(rankcolor), Title.Times.times(Duration.ofMillis(duration*150L),Duration.ofMillis(duration* 500L),Duration.ofMillis(duration*350L))));
        p.sendActionBar(Component.text(ChatColor.BOLD+playernames.toString()).color(color));
    }
    
    protected static String getCenteringSpacer(int textlength, int linelength){
        StringBuilder centeredtext = new StringBuilder();
        for(int i = 0; i<(linelength - textlength)/2; i++){
            centeredtext.append(ChatColor.RESET + " ");
        }
        return centeredtext.toString();
    }
    
    protected static String getCenteringSpacer(String texttocenter, int linelength){
        StringBuilder centeredtext = new StringBuilder();
        for(int i = 0; i<(linelength - texttocenter.length())/2; i++){
            centeredtext.append(ChatColor.RESET + " ");
        }
        return centeredtext.toString();
    }
    
    protected static String getCenteredText(String text, int linelength){
        return getCenteringSpacer(text.length(), linelength)+text;
    }
    
    public static void updateTablist(Player p, String timerdisplay, String posdisplay){
        Component footer = Component.text("");
        if(posdisplay != null){
            //Egg Holder Location :
            footer = footer.append(Component.text(ChatColor.DARK_PURPLE+""+ChatColor.BOLD+"Porteur D'Oeuf : "+ChatColor.LIGHT_PURPLE+ArDragonEggManager.getEggHolderPositionDisplay()));
            if(timerdisplay != null){
                footer = footer.append(Component.text('\n'));
            }
        }
        if(timerdisplay != null){
            footer = footer.append(Component.text(ChatColor.GOLD+ ArGameManager.getTimerDisplay()));
        }
        p.sendPlayerListFooter(footer);
    }
}
