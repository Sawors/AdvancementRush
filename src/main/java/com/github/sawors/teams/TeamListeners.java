package com.github.sawors.teams;

import com.github.sawors.database.ArDataBase;
import com.github.sawors.game.ArGameManager;
import com.github.sawors.game.ArGamePhase;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class TeamListeners implements Listener {
    
    @EventHandler
    public static void chatHandleEvent(AsyncChatEvent event){
        Player p = event.getPlayer();
        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
        String color = "#808080";
        
        // this is a very ugly way to cast a Component to TextComponent but I don't know if normal cast would work
        TextComponent msg = (TextComponent) event.originalMessage();
        char firstchar = 'a';
        if(msg.content().length() > 0){
            firstchar = msg.content().charAt(0);
        }
        if (ArTeamManager.doesPlayerHaveTeam(p)) {
            team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            color = ArTeamManager.getTeamColor(team);
            TextColor tcolor = TextColor.fromHexString(color);
            ArrayList<Player> targets = new ArrayList<>();
            for(UUID id : ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team))){
                Player checkonline = Bukkit.getPlayer(id);
                if(checkonline != null && checkonline.isOnline()){
                    targets.add(checkonline);
                }
            }
    
            TextComponent channel;
            Audience aud;
            if(ArGameManager.getGamephase().equals(ArGamePhase.TEAM_SELECTION)){
                aud = Audience.audience(Bukkit.getOnlinePlayers());
                channel = Component.text(ChatColor.DARK_GRAY+""+ChatColor.BOLD+""+"[G] ");
            } else {
                //TEAM
                channel = firstchar == '!' ? Component.text(ChatColor.DARK_GRAY+""+ChatColor.BOLD+""+"[G] ") : Component.text(ChatColor.BOLD+"[ÉQUIPE] ");
                aud = firstchar == '!' ? Audience.audience(Bukkit.getOnlinePlayers()) : Audience.audience(targets);
            }
            final TextComponent channel_f = channel;
            ChatRenderer cht = (source, sourceDisplayName, message, viewer) -> channel_f.append(sourceDisplayName.append(Component.text(": "))).color(tcolor).append(Component.text(msg.content().replaceFirst("!", "")).color(TextColor.color(0xFFFFFF)));
    
            try{
                Set<Audience> vwrs = event.viewers();
                vwrs.clear();
                vwrs.add(aud);
            } catch (UnsupportedOperationException e){
                e.printStackTrace();
            }
            event.renderer(cht);
        }
        //
    }
    
    @EventHandler
    public static void updateColorOnJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
        if(ArTeamManager.doesPlayerHaveTeam(p)){
            ArTeamManager.syncPlayerColorWithTeam(p);
        }
        ArTeamDisplay.updatePlayerDisplay(p, team);
    }
}
