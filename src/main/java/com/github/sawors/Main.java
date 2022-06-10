package com.github.sawors;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    File dbfile = new File(this.getDataFolder()+File.separator+"advancements.db");
    
    @Override
    public void onEnable() {
        // Plugin startup logic
    
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
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
