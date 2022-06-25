package com.github.sawors.teams;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TeamListeners implements Listener {
    
    @EventHandler
    public static void chatHandleEvent(AsyncChatEvent event){
        Player p = event.getPlayer();
        ArTeamManager.syncPlayerColorWithTeam(p);
    }
    
    @EventHandler
    public static void updateOnJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
        if(team != null){
            ArTeamManager.syncPlayerColorWithTeam(p);
        }
        ArTeamDisplay.updatePlayerScoreboard(p, team);
    }
}
