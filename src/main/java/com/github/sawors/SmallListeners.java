package com.github.sawors;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SmallListeners implements Listener {
    
    
    // FANCY TRAIL FOR THE BEST PLAYER (ME OFC)
    @EventHandler
    public static void saworsTrailMaker(PlayerMoveEvent event){
        
        boolean active = false;
        
        if(active && event.hasChangedPosition() && event.getPlayer().isOp()){
        
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
    }
}
