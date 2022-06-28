package com.github.sawors.commands;

import com.github.sawors.database.ArDataBase;
import com.github.sawors.discordbot.ArDBotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArLinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1 && args[0].length() >= 1){
            String key = args[0];
            if(key.equals("check") && sender instanceof Player){
                String target = ((Player) sender).getUniqueId().toString();
                if(args.length >= 2){
                    target = args[1];
                }
                sender.sendMessage(key+" linked to "+ArDataBase.getDiscordUser(target));
            } else {
                ArDBotManager.setDiscordkey(key);
                sender.sendMessage(ChatColor.LIGHT_PURPLE+"Discord key set, now go on your Discord server and send !arlink "+key);
            }
            return true;
        }
        return false;
    }
}
