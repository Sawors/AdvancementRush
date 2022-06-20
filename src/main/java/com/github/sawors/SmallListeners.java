package com.github.sawors;

import org.bukkit.event.Listener;

public class SmallListeners implements Listener {
    
    
    // FANCY TRAIL FOR THE BEST PLAYER (ME OFC)
    /*@EventHandler
    public static void saworsTrailMaker(PlayerMoveEvent event){
        if(event.hasChangedPosition() && event.getPlayer().getName().equals("Sawors")){
            Location locp = event.getFrom().clone();
            new BukkitRunnable(){
                int i = 20;
                @Override
                public void run(){
                    if(i <= 0){
                        this.cancel();
                        return;
                    }
                    locp.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, locp.clone().add(0,0.8,0),2,.1,.3,.1,.1);
                    i--;
                }
            }.runTaskTimer(Main.getPlugin(),2,1);
        }
    }*/
}
