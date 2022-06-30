package com.github.sawors.commands;

import com.github.sawors.game.ArGameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ArTimeCommand extends ArGameManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1){
            switch(args[0]){
                case"start":
                    ArGameManager.startTimer();
                    break;
                case"pause":
                case"stop":
                    ArGameManager.stopTimerCount();
                    break;
                case"reset":
                    ArGameManager.resetTimer();
                    break;
                case"resume":
                    ArGameManager.startTimer(false);
                    break;
            }
            if(args.length >=2){
                try{
                    int value = Integer.parseInt(args[1]);
                    switch(args[0]){
                        case"add":
                            ArGameManager.forceSetTimer(ArGameManager.getTimerTime()+value);
                            ArGameManager.refreshTimerDisplay();
                            break;
                        case"set":
                            ArGameManager.forceSetTimer(value);
                            ArGameManager.refreshTimerDisplay();
                            break;
                        case"duration":
                            ArGameManager.setGameDuration(value);
                            ArGameManager.refreshTimerDisplay();
                            break;
                            
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        } else return false;
    }
}
