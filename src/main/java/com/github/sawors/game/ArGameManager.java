package com.github.sawors.game;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ArGameManager {
    
    private static ArGameMode gamemode = ArGameMode.TIMER;
    private static int duration = 240;
    private static boolean showscores = true;
    private static int hidescorestimer = 30;
    private static int targetvalue = 1000;
    private static int hybridtargetvalue = targetvalue;
    private static int hybridhidescorestimer = hidescorestimer;
    private static boolean dragoneggbonus = true;
    private static ArDragonEggMode eggbonusmode = ArDragonEggMode.HOLD;
    private static int eggbonusperminute = 10;
    private static int eggfinalbonuspoints = 300;
    private static boolean eggshowholdercoordinates = true;
    private static int eggglowingholdertimer = 30;
    private static BukkitTask timerinstance = null;
    private static int timer = 0;
    
    public static void initGameMode(){
        FileConfiguration config = Main.getMainConfig();
        
        try{
            gamemode = ArGameMode.valueOf(Objects.requireNonNull(config.getString("gamemode")).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e){
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] could not load gamemode parameter, wrong syntax");
        }
    
        ConfigurationSection timersection = config.getConfigurationSection("timer-options");
        ConfigurationSection valuesection = config.getConfigurationSection("value-options");
        ConfigurationSection hybridsection = config.getConfigurationSection("hybrid-options");
        if(gamemode == ArGameMode.TIMER && timersection != null){
            duration = timersection.getInt("duration");
            showscores = timersection.getBoolean("show-scores");
            hidescorestimer = timersection.getInt("hide-scores-timer");
        } else if(gamemode == ArGameMode.VALUE && valuesection != null){
            targetvalue = valuesection.getInt("target-value");
        } else if(gamemode == ArGameMode.HYBRID && hybridsection != null){
            hybridtargetvalue = hybridsection.getInt("target-value");
            duration = hybridsection.getInt("duration");
            hybridhidescorestimer = hybridsection.getInt("hide-scores-timer");
        }
        
        ConfigurationSection eggsection = config.getConfigurationSection("dragon-egg-bonus");
        if(eggsection != null && eggsection.getBoolean("enabled")){
            try{
                eggbonusmode = ArDragonEggMode.valueOf(Objects.requireNonNull(eggsection.getString("bonus-mode")).toUpperCase(Locale.ROOT));
            } catch (NullPointerException | IllegalArgumentException e){
                Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] could not load egg bonus mode parameter, wrong syntax");
            }
            eggbonusperminute = eggsection.getInt("hold-points-per-minute");
            eggfinalbonuspoints = eggsection.getInt("end-bonus-points");
            eggshowholdercoordinates = eggsection.getBoolean("show-coordinates");
            eggglowingholdertimer = eggsection.getInt("glowing-timer");
        } else {
            dragoneggbonus = false;
        }
        Main.logAdmin(duration*60+"");
        cancelTimerTask();
    }
    
    public static Location tryToGetEggLocation(){
        Player lastknownholder = Bukkit.getPlayer(Objects.requireNonNull(getLastKnownHolder()));
        if(getLastKnownHolder() != null && lastknownholder != null){
            for(ItemStack item : lastknownholder.getInventory().getStorageContents()){
                if(item != null && item.getType() == Material.DRAGON_EGG){
                    return lastknownholder.getLocation();
                }
            }
        }
    
        for(Player p : Bukkit.getOnlinePlayers()){
            for(ItemStack item : p.getInventory().getStorageContents()){
                if(item != null && item.getType() == Material.DRAGON_EGG){
                    updateLastKnownHolder(p);
                    return p.getLocation();
                }
            }
        }
        
        return new Location(Bukkit.getWorlds().get(0),0,0,0);
    }
    
    public static UUID getLastKnownHolder(){
        try(Connection co = ArDataBase.connect()){
            String query = "SELECT VALUE FROM game WHERE DATA = '"+ArGameData.EGG_HOLDER+"'";
            String output = co.prepareStatement(query).executeQuery().getString("VALUE");
            if(Objects.equals(output, "[]")){
                return null;
            } else {
                return UUID.fromString(output);
            }
            
        } catch(SQLException | IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
    }
    public static void updateLastKnownHolder(Player player){
        try(Connection co = ArDataBase.connect()){
            String id = "";
            if(player == null){
                id = "[]";
            } else {
                id = player.getUniqueId().toString();
            }
            
            String query = "UPDATE game SET VALUE = '"+id+"' WHERE DATA = '"+ArGameData.EGG_HOLDER+"'";
            co.prepareStatement(query).executeQuery();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void startTimer(boolean reset){
        cancelTimerTask();
        if(reset){timer = 0;}
        getNewTimer();
    }
    public static void startTimer(){
        startTimer(true);
    }
    public static void resetTimer(){
        cancelTimerTask();
        timer = 0;
        refreshTimerDisplay();
    }
    public static void refreshTimerDisplay(){
        for(Player p : Bukkit.getOnlinePlayers()){
            int hours = ((duration*60)-timer) / 3600;
            int minutes = (((duration*60)-timer) % 3600) / 60;
            int seconds = ((duration*60)-timer) % 60;
            p.sendPlayerListFooter(Component.text(ChatColor.GOLD+ String.format(Locale.ENGLISH,"%02d:%02d:%02d", hours, minutes, seconds)));
        }
    }
    public static void setGameDuration(int durationinminutes){
        duration = durationinminutes;
    }
    public static void startEndGameSequence(){
        timer=duration*60;
        refreshTimerDisplay();
        for(Player p : Bukkit.getOnlinePlayers()){
            p.showTitle(Title.title(Component.text("TIMER END"),Component.text("")));
            try{
                String team = ArTeamManager.getTeamsRanking().get(0);
                p.showTitle(Title.title(Component.text(ChatColor.GOLD+"WINNER"),Component.text(team+" : "+ArTeamManager.getTeamPoints(team)).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team)))));
            } catch(IndexOutOfBoundsException e){
                Main.logAdmin("no team in first place");
            }
        }
    }
    public static void forceSetTimer(int timerinminutes){
        // do this check first because we cannot know if at this time timer is used, we do this to avoid assigning it by mistake to a negative value for an instant
        if (timerinminutes*60 < 0) {timer = 0;return;}
        timer = timerinminutes*60;
        if(timer >= duration*60){startEndGameSequence();}
        
    }
    public static void updateTimerInDatabase(int time){
        try(Connection co = ArDataBase.connect()){
            String query = "UPDATE game SET VALUE = "+time+" WHERE DATA = '"+ArGameData.TIMER+"'";
            co.prepareStatement(query).executeQuery();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static int loadTimerValue(){
        try(Connection co = ArDataBase.connect()){
            String query = "SELECT VALUE FROM game WHERE DATA = '"+ArGameData.TIMER+"'";
            return Integer.getInteger(co.prepareStatement(query).executeQuery().getString("VALUE"));
        } catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
    }
    public static int getTimerTime(){
        return timer;
    }
    public static void stopTimerCount(){
        cancelTimerTask();
    }
    public static void printTimer(){
        Main.logAdmin("duration : "+duration+" timer : "+timer);
    }
    private static void cancelTimerTask(){
        try{
            timerinstance.cancel();
        }catch (NullPointerException e){
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] no timer currently running, nothing has been cancelled");
        }
    }
    private static void getNewTimer(){
        cancelTimerTask();
        timerinstance = new BukkitRunnable() {
            @Override
            public void run() {
                timer++;
                refreshTimerDisplay();
                if(timer >= duration*60){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            startEndGameSequence();
                        }
                    }.runTask(Main.getPlugin());
                    this.cancel();
                    return;
                }
                
                //do timed events here !!!
                
                
                
            }
        }.runTaskTimer(Main.getPlugin(),0,20);
    }
    
    
    
    public static int getTargetValue(){
        if(gamemode == ArGameMode.HYBRID){
            return hybridtargetvalue;
        } else if(gamemode == ArGameMode.TIMER){
            return targetvalue;
        } else return 0;
    }
    public static ArGameMode getGameMode(){
        return gamemode;
    }
    public static boolean isEggEnabled(){
        return dragoneggbonus;
    }
    public static boolean showEggHolderCoordinates(){
        return eggshowholdercoordinates;
    }
}
