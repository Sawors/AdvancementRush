package com.github.sawors.discordbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ArDiscordListeners extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e){
        String msg = e.getMessage().getContentDisplay();
        if(msg.toLowerCase(Locale.ROOT).contains("coucou") && !e.getMessage().getAuthor().isBot()){
            e.getChannel().sendMessage("Coucou "+e.getMessage().getAuthor().getAsMention()+" !").queue();
        }
    }
}
