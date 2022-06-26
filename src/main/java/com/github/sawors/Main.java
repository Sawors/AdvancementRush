package com.github.sawors;

import com.github.sawors.advancements.AdvancementListeners;
import com.github.sawors.commands.ArNickCommand;
import com.github.sawors.commands.ArTeamCommand;
import com.github.sawors.commands.ArTestCommand;
import com.github.sawors.commands.ArUnNickCommand;
import com.github.sawors.discordbot.DiscordBotManager;
import com.github.sawors.teams.ArTeamDisplay;
import com.github.sawors.teams.TeamListeners;
import net.dv8tion.jda.api.JDA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    private static Main instance;
    private static File dbfile;
    
    //
    // Discord bot
    private static JDA jda;
    private JDA getJDA(){
        return jda;
    }
    
    //
    // startup
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        //Initialisation of the Database
        dbfile = new File(instance.getDataFolder()+File.separator+"database.db");
        this.getDataFolder().mkdirs();
        try {
            dbfile.createNewFile();
            Bukkit.getLogger().log(Level.INFO, "[AdvancementRush] Advancements database for plugin stored under : "+File.separator+dbfile);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        getServer().getPluginManager().registerEvents(new GeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new AdvancementListeners(), this);
        getServer().getPluginManager().registerEvents(new TeamListeners(), this);
    
        getServer().getPluginCommand("arteam").setExecutor(new ArTeamCommand());
        getServer().getPluginCommand("artest").setExecutor(new ArTestCommand());
        getServer().getPluginCommand("arnick").setExecutor(new ArNickCommand());
        getServer().getPluginCommand("arunnick").setExecutor(new ArUnNickCommand());
    
        
        //initMainConfig();
        this.saveDefaultConfig();
        
        
        
        
        // ALWAYS init this first
        ArDataBase.connectInit();
        
        // then we can safely use the database
        ArDataBase.initNoSyncList();
        
        // Discord bot init
        try {
            jda = DiscordBotManager.initDiscordBot();
            Main.logAdmin("Bot on");
        } catch (LoginException e) {
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] Discord bot couldn't start : wrong token, disabling Discord bot...");
        }
        
        //load scoreboard appearance from config
        int rksize = getMainConfig().getInt("ranking-size");
        if(rksize >= 0 && rksize <= 7){
            ArTeamDisplay.setTopTeamSize(rksize);
        }
        if(getMainConfig().getBoolean("show-points")){
            ArTeamDisplay.setShowPoints(getMainConfig().getBoolean("show-points"));
        }
        
        
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public static Plugin getPlugin() {
        return instance;
    }
    
    public static File getDbFile(){
        return dbfile;
    }
    
    public static void logAdmin(TextComponent msg){
        Bukkit.getLogger().log(Level.INFO, "[AdvancementRush] "+msg.content().replaceAll("§e", ""));
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.isOp()){
                p.sendMessage(ChatColor.YELLOW+"[DEBUG] "+UsefulTools.getTimeText()+" : "+msg.content().replaceAll("§e", ""));
            }
        }
    }
    
    public static void logAdmin(String msg){
        logAdmin(Component.text(ChatColor.YELLOW+msg));
    }
    
    public static FileConfiguration getMainConfig(){
        return getPlugin().getConfig();
    }
}
