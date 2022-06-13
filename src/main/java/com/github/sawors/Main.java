package com.github.sawors;

import com.github.sawors.advancements.AdvancementListeners;
import com.github.sawors.commands.ArPointCommand;
import com.github.sawors.commands.ArTeamCommand;
import com.github.sawors.commands.ArTestCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    private static Main instance;
    private static File dbfile;
    
    
    
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
        getServer().getPluginManager().registerEvents(new SmallListeners(), this);
        getServer().getPluginManager().registerEvents(new AdvancementListeners(), this);
    
        getServer().getPluginCommand("arteam").setExecutor(new ArTeamCommand());
        getServer().getPluginCommand("arpoint").setExecutor(new ArPointCommand());
        getServer().getPluginCommand("artest").setExecutor(new ArTestCommand());
    
        ArDataBase.connectInit();
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
                p.sendMessage(ChatColor.YELLOW+"[DEBUG] "+ LocalDateTime.now().format(DateTimeFormatter.ISO_TIME)+" : "+msg.content().replaceAll("§e", ""));
            }
        }
    }
    
    public static void logAdmin(String msg){
        logAdmin(Component.text(ChatColor.YELLOW+msg));
    }
}
