package com.github.sawors.commands;

import com.github.sawors.teams.ArTeam;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.logging.Level;

public class ArTeamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1){
            String action = args[0];
            try{
                sender.sendMessage("y1");
                switch(action){
                    case "create":
                        //ArTeamManager.createTeam(args[1], Color.fromRGB((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
                        return true;
                    case "remove":
                        //ArTeamManager.removeTeam(args[1]);
                        return true;
                    case "set":
                        break;
                    case "join":
                        break;
                    case "add":
                        break;
                    case "kick":
                        break;
                    case "list":
                        sender.sendMessage("yep");
                        ArrayList<ArTeam> tms = ArTeamManager.getTeams();
                        sender.sendMessage(tms.toString());
                        TextComponent msg = Component.text("");
                        if(tms.size() > 0){
                            for(ArTeam t : tms){
                                msg.append(Component.text(t.getName()+"\n").color(TextColor.color(t.getColor().asRGB())));
                            }
                        } else{
                            msg = Component.text("there is no team existing yet");
                        }
                        if(sender instanceof Player){
                            sender.sendMessage(msg);
                        } else {
                            Bukkit.getLogger().log(Level.INFO, msg.content());
                        }
                        return true;
                }
            } catch (ArrayIndexOutOfBoundsException e){
                if(sender instanceof Player){
                    sender.sendMessage(ChatColor.RED+"Command failed, missing argument");
                }
            }
        }
        
        return false;
    }
}
