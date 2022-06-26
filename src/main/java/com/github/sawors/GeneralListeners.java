package com.github.sawors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GeneralListeners implements Listener {
    
    
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
    public static void discreetConnectionMessages(PlayerJoinEvent e){
        // TODO : config
        e.joinMessage(Component.text(ChatColor.GRAY+"→ "+e.getPlayer().getName()));
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public static void discreetLeavingMessages(PlayerQuitEvent e){
        // TODO : config
        e.quitMessage(Component.text(ChatColor.GRAY+"← "+e.getPlayer().getName()));
    }
    
    @EventHandler
    public static void discreetDeathMessage(PlayerDeathEvent event){
        Component dm = event.deathMessage() != null ? event.deathMessage() : Component.text("");
        event.deathMessage(dm.color(TextColor.color(0xAAAAAA)));
        Main.logAdmin("LAUNCHED");
    }
}
