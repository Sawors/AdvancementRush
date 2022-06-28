package com.github.sawors.discordbot;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.teams.ArTeamManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArDBotManager {
    
    private static Guild discordserver;
    
    private static String discordkey = "";
    
    private static String gamecategory;
    
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
    
    public static String getDiscordkey() {
        return discordkey;
    }
    
    public static void setDiscordkey(String discordkey) {
        ArDBotManager.discordkey = discordkey;
    }
    
    public static Guild getDiscordserver() {
        return discordserver;
    }
    
    public static void setDiscordserver(Guild server) {
        ArDBotManager.discordserver = server;
    }
    
    public static void createGameCategory(){
        Guild discord = getDiscordserver();
        String category = "AR Game "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy : HH:mm:ss"));
        discord.createCategory(category).addRolePermissionOverride(discord.getPublicRole().getIdLong(),Permission.UNKNOWN.getRawValue(),Permission.VIEW_CHANNEL.getRawValue()).queue();
        gamecategory = category;
    }
    
    public static void createTeamRoles(){
        Guild discord = getDiscordserver();
        for(String team : ArTeamManager.getTeamList()){
                discord.createRole()
                    .setColor(Color.decode(ArTeamManager.getTeamColor(team)))
                    .setName(team).queue();
        }
    }
    
    public static void giveTeamRoles(){
        Guild discord = getDiscordserver();
        for(String team : ArTeamManager.getTeamList()){
            for(UUID player : ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team))){
                String id = ArDataBase.getDiscordUser(player.toString());
                List<Role> roles = discord.getRolesByName(team, false);
                if(id != null && roles.size() > 0){
                    discord.addRoleToMember(UserSnowflake.fromId(id), roles.get(0)).queue();
                }
                
            }
        }
    }
    
    public static void createTeamChannels(){
        Guild discord = getDiscordserver();
        for(String team : ArTeamManager.getTeamList()){
            List<Role> roles = discord.getRolesByName(team, false);
            if(roles.size() > 0){
                if(gamecategory.length() > 1){
                    List<Category> categories = discord.getCategoriesByName(gamecategory, false);
                    if(categories.size() > 0){
                        categories.get(0).createVoiceChannel(team).addRolePermissionOverride(Long.parseLong(roles.get(0).getId()), Permission.VOICE_CONNECT.getRawValue(),Permission.MANAGE_CHANNEL.getRawValue()).queue();
                        categories.get(0).createTextChannel(team).addRolePermissionOverride(Long.parseLong(roles.get(0).getId()), Permission.VOICE_CONNECT.getRawValue(),Permission.MANAGE_CHANNEL.getRawValue()).queue();
                    }
                } else {
                    discord.createVoiceChannel(team).addRolePermissionOverride(Long.parseLong(roles.get(0).getId()), Permission.VOICE_CONNECT.getRawValue(),Permission.MANAGE_CHANNEL.getRawValue()).queue();
                    discord.createTextChannel(team).addRolePermissionOverride(Long.parseLong(roles.get(0).getId()), Permission.VOICE_CONNECT.getRawValue(),Permission.MANAGE_CHANNEL.getRawValue()).queue();
    
                }
            }
            
        }
    }
    
    public static void sendUsersToTeamChannels(){
        String generalvocalid = Main.getMainConfig().getString("discord-general-vocal-id");
        Main.logAdmin(generalvocalid);
        if(generalvocalid != null && generalvocalid.length() > 2){
            Guild discord = getDiscordserver();
            VoiceChannel chan = discord.getChannelById(VoiceChannel.class,generalvocalid);
            List<Category> cats = discord.getCategoriesByName(gamecategory, false);
            if(chan != null && cats.size() > 0){
                Main.logAdmin(chan.getName());
                ArrayList<GuildChannel> chns = new ArrayList<>();
                HashMap<String, VoiceChannel> chanmap = new HashMap<>();
                for(GuildChannel chn : cats.get(0).getChannels()){
                    if(chn.getType() == ChannelType.VOICE){
                        Main.logAdmin(chn.getName());
                        chanmap.put(chn.getName(), (VoiceChannel) chn);
                    }
                }
                for(Member mb : chan.getMembers()){
                    String mcplayer = ArDataBase.getMinecraftPlayer(mb.getId());
                    Main.logAdmin(mb.getId());
                    Main.logAdmin(mcplayer);
                    Main.logAdmin(mb.getNickname());
                    if(mcplayer != null){
                        String team = ArTeamManager.getPlayerTeam(UUID.fromString(mcplayer));
                        Main.logAdmin(team);
                        if (chanmap.containsKey(team)) {
                            Main.logAdmin("movin...");
                            discord.moveVoiceMember(mb, chanmap.get(team)).queue();
                        }
                        
                    }
                    
                }
            }
        }
    }
    public static void deleteRoles(){
        Guild discord = getDiscordserver();
        for(String team : ArTeamManager.getTeamList()){
            List<Role> roles = discord.getRolesByName(team, false);
            if(roles.size() > 0){
                for(Role role : roles){
                    role.delete().queue();
                }
            }
        }
    }
    public static void deleteCategory(){
        Guild discord = getDiscordserver();
        if(gamecategory != null){
            List<Category> cats = discord.getCategoriesByName(gamecategory, false);
            if(cats.size() > 0){
                String generalvocalid = Main.getMainConfig().getString("discord-general-vocal-id");
                for(GuildChannel chan : cats.get(0).getChannels()){
                    if(chan.getType().equals(ChannelType.VOICE) && generalvocalid != null){
                        VoiceChannel fallbackchan = discord.getChannelById(VoiceChannel.class,generalvocalid);
                        for(Member mb : ((VoiceChannel) chan).getMembers()){
                            discord.moveVoiceMember(mb, fallbackchan).queue();
                        }
                    }
                    chan.delete().queue();
                }
                cats.get(0).delete().queue();
            }
        }
    }
}
