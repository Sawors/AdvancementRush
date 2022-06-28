package com.github.sawors.commands;

import com.github.sawors.Main;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.discordbot.ArDBotManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArMeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(ArDBotManager.getDiscordserver() == null){
            sender.sendMessage(ChatColor.RED+"this game is not yet linked to a Discord server");
            return true;
        }
        if(sender instanceof Player && args.length == 1){
           
            Guild serv = ArDBotManager.getDiscordserver();
            Main.logAdmin(serv.getMembers().toString());
            List<Member> userfound = serv.getMembersByName(args[0],true);
            Main.logAdmin(userfound.toString());
            if(userfound.size() > 0){
                ArDataBase.registerLink(((Player) sender).getUniqueId(), userfound.get(0).getId());
                return true;
            }
        } else if(args.length >= 2){
            ArDataBase.registerLink(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), args[1]);
            return true;
        }
        return false;
    }
}
