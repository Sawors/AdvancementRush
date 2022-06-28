package com.github.sawors.game;

import com.github.sawors.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Objects;


public class ArGameListeners implements Listener {
    
    
    /*// FANCY TRAIL FOR THE BEST PLAYER (ME OFC)
    @EventHandler
    public static void saworsTrailMaker(PlayerMoveEvent event){
        
        if(event.hasChangedPosition() && event.getPlayer().isOp()){
        
            Particle p = Particle.SPIT;
            if(event.getPlayer().getName().equals("Sawors")){
                p = Particle.ELECTRIC_SPARK;
            } else if(event.getPlayer().getName().equals("LesBoulesDeGyro")){
                p = Particle.BUBBLE_POP;
            }
            
            
            
            
            Location locp = event.getFrom().clone();
            final Particle finalp = p;
            new BukkitRunnable(){
                int i = 20;
                @Override
                public void run(){
                    if(i <= 0){
                        this.cancel();
                        return;
                    }
                    locp.getWorld().spawnParticle(finalp, locp.clone().add(0,0.8,0),2,.1,.3,.1,.1);
                    i--;
                }
            }.runTaskTimer(Main.getPlugin(),2,1);
        }
    }*/
    
    @EventHandler(priority = EventPriority.LOW)
    public static void playerJoinServer(PlayerJoinEvent e){
        if(Objects.equals(Main.getMainConfig().getBoolean("discreet-connections"), true)){
            e.joinMessage(Component.text(ChatColor.GRAY+"-> "+e.getPlayer().getName()));
        }
        if(ArGameManager.getGameMode() == ArGameMode.HYBRID || ArGameManager.getGameMode() == ArGameMode.TIMER){
            ArGameManager.refreshTimerDisplay();
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public static void playerLeaveServer(PlayerQuitEvent e){
        if(Objects.equals(Main.getMainConfig().getBoolean("discreet-connections"), true)){
            e.quitMessage(Component.text(ChatColor.GRAY+"<- "+e.getPlayer().getName()));
        }
        
    }
    
    @EventHandler
    public static void discreetDeathMessage(PlayerDeathEvent event){
        if(Objects.equals(Main.getMainConfig().getBoolean("discreet-death-messages"), true)){
            Component dm = event.deathMessage() != null ? event.deathMessage() : Component.text("");
            event.deathMessage(dm.color(TextColor.color(0x555555)));
        }
    }
    
    @EventHandler
    public static void chatSoundEffects(AsyncChatEvent event){
        if(ArGameManager.getGamephase().equals(ArGamePhase.WINNER_ANNOUNCEMENT) && ((TextComponent)event.message()).content().toLowerCase(Locale.ENGLISH).contains("gg")){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE,1,1.5f);
            }
        }
    }
}
