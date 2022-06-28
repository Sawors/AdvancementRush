package com.github.sawors.discordbot;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ArDiscordListeners extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e){
        String msg = e.getMessage().getContentDisplay();
        if(msg.length() > 0 && msg.toCharArray()[0] == '!'){
            if(msg.toLowerCase(Locale.ROOT).contains("coucou le bot") && !e.getMessage().getAuthor().isBot()){
                e.getChannel().sendMessage("Coucou "+e.getMessage().getAuthor().getAsMention()+" !").queue();
            }else if(msg.toLowerCase(Locale.ROOT).contains("!arlink")){
                StringBuilder key = new StringBuilder();
                char[] msgchar = msg.toCharArray();
                boolean readarg = false;
                for(int i = 0; i<msg.length(); i++){
                    if(readarg){
                        if(msgchar[i] == ' '){
                            break;
                        }
                        key.append(msgchar[i]);
                    }
                    if(!readarg && msgchar[i] == ' '){
                        readarg = true;
                    }
                }
        
                if(key.length() > 0 && ArDBotManager.getDiscordkey().length() > 0 && key.toString().equals(ArDBotManager.getDiscordkey())){
                    ArDBotManager.setDiscordserver(e.getGuild());
                    e.getMessage().delete().queue();
                    Main.logAdmin("[Advancement Rush] Discord bot linked !");
                    e.getChannel().sendMessage("Minecraft server linked !").queue();
                } else {
                    Main.logAdmin("[Advancement Rush] couldn't sync discord server, keys do not match");
                    e.getChannel().sendMessage("couldn't sync minecraft server, keys do not match").queue();
                }
            }else if(msg.toLowerCase(Locale.ROOT).contains("!arpair")){
                StringBuilder key = new StringBuilder();
                char[] msgchar = msg.toCharArray();
                boolean readarg = false;
                for(int i = 0; i<msg.length(); i++){
                    if(readarg){
                        if(msgchar[i] == ' '){
                            break;
                        }
                        key.append(msgchar[i]);
                    }
                    if(!readarg && msgchar[i] == ' '){
                        readarg = true;
                    }
                }
                User author = e.getAuthor();
                if(!author.isBot() && !author.isSystem()){
                    ArDataBase.registerLink(Bukkit.getOfflinePlayer(key.toString()).getUniqueId(), e.getAuthor().getId());
                    e.getChannel().sendMessage("You are now linked to "+Bukkit.getOfflinePlayer(key.toString()).getName()+" (if name is incorrect please check the pseudo you wanted to link)").queue();
                    if(Bukkit.getOfflinePlayer(key.toString()).isOnline()){
                        Player p = Bukkit.getPlayer(Bukkit.getOfflinePlayer(key.toString()).getUniqueId());
                        p.sendMessage(ChatColor.LIGHT_PURPLE+"you are now linked to Discord user "+e.getAuthor().getName());
                    }
                } else {
                    e.getChannel().sendMessage("linking failed my fellow bot, they don't want us in their game :cry:").queue();
                }
            }
        }
    }
}
