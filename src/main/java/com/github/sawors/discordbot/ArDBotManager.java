package com.github.sawors.discordbot;

import com.github.sawors.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class ArDBotManager {
    
    
    public static JDA initDiscordBot() throws LoginException {
        
        // THIS IS A SAMPLE TOKEN, DO NOT USE IT
        // the token is set in config, NEVER SHARE YOUR TOKEN ON THE INTERNET
        String token = Main.getMainConfig().getString("discord-bot-token");
        if(token != null){
            JDABuilder builder = JDABuilder.createDefault(token);
            // Add Discord bot listeners here
            builder.addEventListeners(new ArDiscordListeners());
    
    
            // Build bot
            return builder.build();
        } else {
            throw new LoginException();
        }
        
    }
}
