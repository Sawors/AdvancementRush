package com.github.sawors.advancements;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.github.sawors.Main;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.game.ArGameManager;
import com.github.sawors.game.ArGamePhase;
import com.github.sawors.teams.ArTeamManager;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.MalformedParametersException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class AdvancementListeners implements Listener {
    
    @EventHandler(priority = EventPriority.LOW)
    public static void playerCriteriaProgress(PlayerAdvancementCriterionGrantEvent event){
        if(!ArGameManager.getGamephase().equals(ArGamePhase.INGAME)){
            event.setCancelled(true);
            return;
        }
        Advancement adv = event.getAdvancement();
        if(AdvancementManager.isRecipe(adv) || adv.getKey().getKey().contains("/root") || Main.isIgnored(adv.getKey())){
            return;
        }
        Player p = event.getPlayer();
        try{
            String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            if(!p.getAdvancementProgress(event.getAdvancement()).isDone() && !ArDataBase.isAdvancementMuted(adv.getKey(),team) && team != null && ArDataBase.shouldSync(adv)){
                ArTeamManager.addCriterionToTeam(team,adv.getKey(),event.getCriterion());
                ArTeamManager.syncTeamAdvancement(team, adv);
                Main.logAdmin(ChatColor.RED+event.getCriterion());
            }
        } catch(SQLException | NullPointerException e){
            e.printStackTrace();
        }
        
    }
    
    
    //  THIS LISTENER IS USED FOR :
    //      - Granting points to teams
    //      - Muting advancements for other players when not in team
    //      - Checking and granting bonus for the first team to earn an advancement
    //      - Checking and granting other bonuses
    
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void onPlayerCompleteAdvancement(PlayerAdvancementDoneEvent event){
        Advancement adv = event.getAdvancement();
        Component advmessage = event.message();
    
        if(AdvancementManager.isRecipe(adv) || adv.getKey().getKey().contains("/root") || Main.isIgnored(adv.getKey())){
            event.message(null);
            return;
        }
        
        
        Player p = event.getPlayer();
        if(!ArTeamManager.doesPlayerHaveTeam(p)){
            if(advmessage != null){
                p.sendMessage(advmessage);
                event.message(null);
                return;
            }
        }
        try{
            String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            NamespacedKey advname = event.getAdvancement().getKey();
        
            if(team != null && p.getAdvancementProgress(event.getAdvancement()).isDone() && !ArTeamManager.hasTeamAdvancementCompleted(team, advname)){
                int value = ArDataBase.getAdvancementValue(advname);
                if(value != 0){
                    if (ArDataBase.isAdvancementMuted(advname, team)) {
                        event.message(null);
                    } else {
                        // THE ADVANCEMENT IS VALIDATED FOR THE TEAM, NOW ADD VALUE AND CHECK FOR BONUSES
                        
                        // add advancement to team in database + sync
                        ArDataBase.muteAdvancement(advname,team);
                        ArTeamManager.addAdvancementToTeam(team, adv.getKey());
                        ArTeamManager.syncTeamAdvancement(team, adv);
                        ArDataBase.unmuteAdvancement(advname,team);
    
                        // give points to team
                        ArTeamManager.addPointsToTeam(team, value);
                        
                        //  TODO :
                        //      add team's points display on HoverEvent for all team's name occurrence in chat (maybe team's members list too ?)
                        
                        // change event message to display team name
                        Component msg = event.message();
                        String valuesign = "+";
                        if(value <= 0){
                            valuesign = "";
                        }
                        event.message(Component.text("["+team+"] ").color(TextColor.fromCSSHexString(ArTeamManager.getTeamColor(team))).append(msg.color(TextColor.color(0xFFFFFF))).append(Component.text(" "+valuesign+value+"pts").color(TextColor.color(0x00AA00))));
    
                        // Play unlock sound
                        playUnlockSoundForTeam(team);
                        
                        // check for "pioneer" bonus
                        if(ArTeamManager.isTeamFirstOnAdvancement(team, event.getAdvancement())){
                            // team is effectively first, giving "pioneer" bonus
                            
                            final int pioneerbonus = Main.getMainConfig().getInt("pioneer-bonus");
                            
                            final AdvancementDisplay display = event.getAdvancement().getDisplay();
                            // Play bonus sound
                            playBonusSoundForTeam(team);
                            ArTeamManager.addPointsToTeam(team, pioneerbonus);
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    Bukkit.broadcast(
                                            Component.text(ChatColor.DARK_PURPLE+""+ChatColor.BOLD+"Team ")
                                                    .append(
                                                            ArTeamManager.getTeamColoredName(team)
                                                    )
                                                    .append(
                                                            Component.text(ChatColor.DARK_PURPLE+""+ChatColor.BOLD+" is the first team to unlock "+ChatColor.GOLD+"[")
                                                    )
                                                    .append(
                                                            display.title().color(TextColor.color(0xFFAA00))
                                                    )
                                                    .append(
                                                            Component.text(ChatColor.GOLD+"] "+ChatColor.RESET+""+ChatColor.DARK_PURPLE+"(Bonus +"+pioneerbonus+"pts)")
                                                    )
    
                                    );
                                }
                            }.runTaskLater(Main.getPlugin(), 1);
                        }
                    }
                }else {
                    event.message(null);
                }
            } else {
                event.message(null);
            }
        } catch(SQLException | NullPointerException e){
            e.printStackTrace();
        }
        
    }
    
    
    
    @EventHandler
    public static void addRootsAndSync(PlayerJoinEvent e){
        
        Player p = e.getPlayer();
        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
        if (team != null) {
            ArTeamManager.syncPlayerAllAdvancementsWithTeam(p,team);
        } else {
            Main.logAdmin("player "+p.getName()+" has no team, couldn't sync advancements");
        }
        
        AdvancementManager.grantRootAdvancements(p);
        
    }
    
    private static void playBonusSoundForTeam(String team){
        try{
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team));
            for(UUID id : players){
                Player soundtarget = Bukkit.getPlayer(id);
                if( soundtarget != null && soundtarget.isOnline()){
                    new BukkitRunnable(){
                        int i = 1;
                        @Override
                        public void run() {
                            
                            if(i == 4){
                                soundtarget.playSound(soundtarget.getLocation(),"advancementrush:info.first_bonus",1,1);
                                soundtarget.playSound(soundtarget.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1.2f);
                            } else {
                                soundtarget.playSound(soundtarget.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1+(0.4f*i));
                            }
                            
                            i++;
                            if(i > 4){
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(Main.getPlugin(),0,2);
                }
            }
        } catch (
                MalformedParametersException e){
            e.printStackTrace();
        }
    }
    
    
    // TODO :
    //  add a little sound when a criterion is obtained ?
    //  send a message in chat to player when a criterion is obtained (+ progress/total at the end or progress as "%" ?)?
    
    private static void playUnlockSoundForTeam(String team){
        try{
            ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team));
            for(UUID id : players){
                Player soundtarget = Bukkit.getPlayer(id);
                if( soundtarget != null && soundtarget.isOnline()){
                    new BukkitRunnable(){
                        int i = 1;
                        @Override
                        public void run() {
                            if(i == 2){
                                soundtarget.playSound(soundtarget.getLocation(),"advancementrush:info.advancement_unlocked",1,1);
                                soundtarget.playSound(soundtarget.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                            } else {
                                soundtarget.playSound(soundtarget.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
                            }
                            i++;
                            if(i > 2){
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(Main.getPlugin(),0,2);
                }
            }
        } catch (
                MalformedParametersException e){
            e.printStackTrace();
        }
    }
}
