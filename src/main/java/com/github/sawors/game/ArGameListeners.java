package com.github.sawors.game;

import com.github.sawors.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

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
    
    
    
    // CANCEL DAMAGES AND BLOCK INTERACTIONS WHEN NOT INGAME
    @EventHandler
    public static void preventDamages(EntityDamageEvent e){
        if(e.getEntity() instanceof Player && (ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION ) || ArGameManager.getGamephase().equals(ArGamePhase.WINNER_ANNOUNCEMENT ))){
            if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && getWolf() == ((EntityDamageByEntityEvent) e).getDamager()){
                setWolf(e.getEntity());
            }
            e.setCancelled(true);
        }
    }
    @EventHandler
    public static void giveWolfOnInteract(PlayerInteractAtEntityEvent e){
        if(ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION ) ){
            setWolf(e.getRightClicked());
        }
    }
    @EventHandler
    public static void preventBlockBreak(BlockBreakEvent e){
        if((ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION ) || ArGameManager.getGamephase().equals(ArGamePhase.WINNER_ANNOUNCEMENT)) && !e.getPlayer().isOp()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public static void preventBlockPlace(BlockPlaceEvent e){
        if((ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION ) || ArGameManager.getGamephase().equals(ArGamePhase.WINNER_ANNOUNCEMENT)) && !e.getPlayer().isOp()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public static void preventItemPickup(PlayerAttemptPickupItemEvent e){
        if((ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION ) || ArGameManager.getGamephase().equals(ArGamePhase.WINNER_ANNOUNCEMENT)) && !e.getPlayer().isOp()){
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public static void handlePlayerJoinPregame(PlayerJoinEvent event){
        if(ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION)){
            Player p = event.getPlayer();
            World w = ArGameManager.getGameworld();
            p.teleport(new Location(w,0.5,w.getSeaLevel()+ArGameManager.getSpawnheight()+1,0.5));
            p.setHealth(20);
            p.setFoodLevel(20);
            if(Bukkit.getOnlinePlayers().size() == 1){
                setWolf(p);
            }
        }
    }
    
    @EventHandler
    public static void preventFrozenPlayerMovement(PlayerMoveEvent event){
        if(event.hasChangedPosition()){
            if(ArGameManager.isFrozen(event.getPlayer().getUniqueId()) && !(event.getFrom().getY() != event.getTo().getY())){
                event.setCancelled(true);
            }
        }
    }
    
    
    
    
    
    //  WAITING GAME
    private static Entity wolf;
    private static boolean iswolfoncooldown = false;
    private static final int wolfcooldown = 1;
    public static Entity getWolf() {
        return wolf;
    }
    public static void setWolf(Entity newwolf) {
        if(!iswolfoncooldown){
            wolf = newwolf;
            iswolfoncooldown = true;
            new BukkitRunnable(){
                @Override
                public void run(){
                    iswolfoncooldown = false;
                }
            }.runTaskLater(Main.getPlugin(),20L*wolfcooldown);
        }
    }
    
    public static void startWolf(){
        new BukkitRunnable(){
            @Override
            public void run() {
                if(!ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION) || !ArGameManager.isMinigameEnabled()){
                    this.cancel();
                    return;
                }
                if(getWolf() != null && getWolf().getLocation().getY() >= getWolf().getWorld().getSeaLevel()+ArGameManager.getSpawnheight()){
                    getWolf().getWorld().spawnParticle(Particle.REDSTONE,getWolf().getLocation().add(0,2.2,0),4,0,0,0,0, new Particle.DustOptions(Color.RED,1));
                    if(getWolf().isDead() && Bukkit.getOnlinePlayers().size() > 0){
                        setWolf((Entity) Bukkit.getOnlinePlayers().toArray()[(int)((Math.random()*Bukkit.getOnlinePlayers().size())-1)]);
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(),20,1);
    }
}
