package com.github.sawors;

import com.github.sawors.advancements.AdvancementListeners;
import com.github.sawors.commands.*;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.discordbot.ArDBotManager;
import com.github.sawors.game.ArGameListeners;
import com.github.sawors.game.ArGameManager;
import com.github.sawors.game.ArGamePhase;
import com.github.sawors.teams.ArTeamDisplay;
import com.github.sawors.teams.TeamListeners;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    private static Main instance;
    private static File dbfile;
    private static HashSet<String> ignorednamespaces = new HashSet<>();
    
    //
    // Discord bot
    private static JDA jda;
    public static JDA getJDA(){
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
        getServer().getPluginManager().registerEvents(new ArGameListeners(), this);
        getServer().getPluginManager().registerEvents(new AdvancementListeners(), this);
        getServer().getPluginManager().registerEvents(new TeamListeners(), this);
    
        getServer().getPluginCommand("arteam").setExecutor(new ArTeamCommand());
        getServer().getPluginCommand("artest").setExecutor(new ArTestCommand());
        getServer().getPluginCommand("arnick").setExecutor(new ArNickCommand());
        getServer().getPluginCommand("arunnick").setExecutor(new ArUnNickCommand());
        getServer().getPluginCommand("artime").setExecutor(new ArTimeCommand());
        getServer().getPluginCommand("arlink").setExecutor(new ArLinkCommand());
        getServer().getPluginCommand("arme").setExecutor(new ArMeCommand());
        getServer().getPluginCommand("arvoc").setExecutor(new ArVocCommand());
        
        //initMainConfig();
        this.saveDefaultConfig();
        // ALWAYS init this first
        ArDataBase.connectInit();
        
        
        // then we can safely use the database
        ArDataBase.initNoSyncList();
        
        //load ignored namespaces
        ignorednamespaces.addAll(getMainConfig().getStringList("ignored-namespaces"));
        
        //init gamemode manager
        ArGameManager.initGameMode();
        
        if(getMainConfig().getBoolean("show-points")){
            ArTeamDisplay.setShowPoints(getMainConfig().getBoolean("show-points"));
        }
    
        //set gamerules and difficulty
        for(World w : Bukkit.getWorlds()){
            w.setGameRule(GameRule.DO_FIRE_TICK, !getMainConfig().getBoolean("disable-fire-spread"));
            w.setGameRule(GameRule.KEEP_INVENTORY, getMainConfig().getBoolean("keep-inventory"));
            w.setGameRule(GameRule.NATURAL_REGENERATION, !getMainConfig().getBoolean("no-natural-health-regen"));
            if(getMainConfig().getBoolean("difficulty-hard")){w.setDifficulty(Difficulty.HARD);} else {w.setDifficulty(Difficulty.NORMAL);}
        }
        
    
    
        // Discord bot init
        try {
            jda = ArDBotManager.initDiscordBot();
            Main.logAdmin("Bot on");
        } catch (LoginException e) {
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] Discord bot couldn't start : wrong token, disabling Discord bot...");
        }
    
        //init display LAST
        ArTeamDisplay.initDisplay();
        
        String serverid = Main.getMainConfig().getString("discord-server-id");
        final int timoutdelay = 20;
        new BukkitRunnable(){
            int tries = 0;
            @Override
            public void run() {
                if(serverid != null && jda.getGuildById(serverid) != null){
                    try{
                        Guild server = jda.getGuildById(serverid);
                        if(server != null){
                            ArDBotManager.setDiscordserver(server);
                            Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Discord server "+ArDBotManager.getDiscordserver().getName()+" correctly linked !");
                            this.cancel();
                            return;
                        }
                    } catch (NumberFormatException e){
                        Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Could not link to Discord automatically from the ID provided in the config, new try in 1 second...");
                        e.printStackTrace();
                    }
                } else {
                    Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Could not link to Discord automatically from the ID provided in the config, new try in 1 second...");
                }
                if(tries == timoutdelay){
                    Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Could not link to Discord automatically from the ID provided in the config, you must use /arlink <key> to link to Discord");
                    this.cancel();
                    return;
                }
                tries++;
            }
        }.runTaskTimer(Main.getPlugin(), 40, 20);
    
    
        // THIS OTHERWISE I REFUSE TO WORK ANYMORE
        ArGameListeners.startWolf();
    
        ArGameManager.setGamephase(ArGamePhase.TEAM_SELECTION);
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        new BukkitRunnable(){
    
            @Override
            public void run() {
                ArDBotManager.deleteCategory();
                ArDBotManager.deleteRoles();
            }
        }.runTaskAsynchronously(getPlugin());
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
    
    public static boolean isNamespaceIgnored(String namespace){
        return ignorednamespaces.contains(namespace);
    }
    public static boolean isNamespaceIgnored(NamespacedKey key){
        return isNamespaceIgnored(key.namespace());
    }
}
