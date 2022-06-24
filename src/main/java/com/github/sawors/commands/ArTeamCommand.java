package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.UsefulTools;
import com.github.sawors.teams.ArTeamData;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.lang.reflect.MalformedParametersException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ArTeamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1 && sender.isOp()){
            String action = args[0];
            try{
                switch(action){
                    case "create":
                        //      /arteam create [teamname] [color]
                        if(args.length >= 2){
                            String teamname = args[1];
                            String colorhex = "";
                            if(args.length >= 3){
                                colorhex = args[2];
                            } else {
                                colorhex = UsefulTools.getRandomColorHex();
                            }
                            Color color = UsefulTools.stringToColorElseRandom(colorhex);
                            try{
                                ArTeamManager.createTeam(teamname, color);
                                
                                TextComponent p1 = Component.text(ChatColor.YELLOW+"team \"");
                                TextComponent namepart = Component.text(teamname).color(TextColor.color(color.asRGB()));
                                TextComponent p2 = Component.text(ChatColor.YELLOW+"\" successfully created");
                                sender.sendMessage(p1.append(namepart).append(p2));
                                
                            } catch (KeyAlreadyExistsException e){
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"team creation failed, team \""+teamname+"\" already exists");
                            }
                            return true;
                        } else {
                            return false;
                        }
                        
                    case "remove":
                    case "delete":
                        //      /arteam remove [teamname]
                        if(args.length >=2){
                            String name = args[1];
                            try{
                                ArTeamManager.removeTeam(name);
                                sender.sendMessage(ChatColor.YELLOW+"team \""+name+"\" successfully deleted");
                            } catch(NullPointerException e){
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"sorry team \""+name+"\" does not exist (/!\\ names are case sensitive)");
                            }
                            return true;
                        } else {
                            return false;
                        }
                    case "set":
                        //      /arteam set [teamname] [data] [value]
                        if(args.length >=4){
                            String name = args[1];
                            String data = args[2];
                            String value = args[3];
                            if(data.equalsIgnoreCase(ArTeamData.COLOR.toString())){
                                ArTeamManager.setTeamColor(name, value);
                                sender.sendMessage(Component.text(ChatColor.YELLOW+"team \""+name+"\"'s color successfully changed to ").append((Component.text(value).color(TextColor.fromHexString(value)))));
                            } else if(data.equalsIgnoreCase(ArTeamData.POINTS.toString())){
                                ArTeamManager.setTeamPoints(name, Integer.parseInt(value));
                                sender.sendMessage(Component.text(ChatColor.YELLOW+"team \""+name+"\"'s points successfully changed to "+value));
                            } else if(data.equalsIgnoreCase(ArTeamData.PLAYERS.toString())){
                                ArTeamManager.setTeamPlayers(name, value);
                                sender.sendMessage(Component.text(ChatColor.YELLOW+"team \""+name+"\"'s players successfully changed to "+value));
                            }
                            return true;
                        } else {
                            return false;
                        }
                    case "join":
                        //      /arteam join [teamname]
                        if(args.length >=2 && sender instanceof Player){
                            String team = args[1];
                            Player p = (Player) sender;
                            try{
                                ArTeamManager.changePlayerTeam(team, p.getUniqueId());
                                TextComponent p1 = Component.text(ChatColor.YELLOW+"you are now a member of team ");
                                TextComponent namepart = Component.text(team).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team)));
                                if(p.isOnline()) {
                                    ArTeamManager.syncPlayerAllAdvancementsWithTeam(p, team);
                                    ArTeamManager.syncPlayerColorWithTeam(p);
                                } else {
                                    Main.logAdmin("could not sync player "+p.getName()+" with team "+team+" for this player is offline");
                                }
                                sender.sendMessage(p1.append(namepart));
                            } catch (SQLException | MalformedParametersException e){
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"Query to database failed, no further information (is team name correct ?)");
                            } catch (KeyAlreadyExistsException e){
                                sender.sendMessage(ChatColor.GOLD+"You are already in team "+team);
                            }
                            return true;
                        } else {
                            sender.sendMessage("this command must be used by a player");
                            return false;
                        }
                    case "add":
                        //      /arteam add [teamname] [playername]
                        if(args.length >=3){
                            String player = args[1];
                            String team = args[2];
                            try{
                                ArTeamManager.changePlayerTeam(team, Bukkit.getPlayerUniqueId(player));
                                TextComponent p1 = Component.text(ChatColor.YELLOW+player+" is now a member of team ");
                                TextComponent namepart = Component.text(team).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team)));
                                if(Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).isOnline()) {
                                    ArTeamManager.syncPlayerAllAdvancementsWithTeam(Bukkit.getPlayer(player), team);
                                    ArTeamManager.syncPlayerColorWithTeam(Objects.requireNonNull(Bukkit.getPlayer(player)));
                                } else {
                                    Main.logAdmin("could not sync player "+player+" with team "+team+" for this player is offline");
                                }
                                sender.sendMessage(p1.append(namepart));
                            } catch (SQLException | MalformedParametersException | NullPointerException e){
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"Query to database failed, no further information (are team name and player's name correct ?)");
                            } catch (KeyAlreadyExistsException e){
                                sender.sendMessage(ChatColor.GOLD+"Player "+player+" is already in team "+team);
                            }
                            return true;
                        } else {
                            return false;
                        }
                    case "kick":
                        //      /arteam kick [playername] [teamname]
                        if(args.length >=3){
                            String player = args[1];
                            String team = args[2];
                            try{
                                ArTeamManager.removePlayerFromTeam(team,Bukkit.getPlayerUniqueId(player));
                                TextComponent p1 = Component.text(ChatColor.YELLOW+player+" is now removed from team ");
                                TextComponent namepart = Component.text(team).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team)));
                                sender.sendMessage(p1.append(namepart));
                            } catch (SQLException | MalformedParametersException | NullPointerException e){
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"Query to database failed, no further information (are team name and player's name correct ?)");
                            }
                            return true;
                        } else {
                            return false;
                        }
                    case "list":
                        //      /arteam list
                        //      /arteam list [teamname]
                        
                        if(args.length == 1){
                            sender.sendMessage(ChatColor.YELLOW+"=====[Team List]=====");
                            for(String team : ArTeamManager.getTeamList()){
                                sender.sendMessage(Component.text(ChatColor.YELLOW+"- ").append(ArTeamManager.getTeamColoredName(team)).append(Component.text(" : "+ArTeamManager.getTeamPoints(team))));
                            }
                            sender.sendMessage(ChatColor.YELLOW+"===================");
                            return true;
                        } else {
                            try{
                                Main.logAdmin("arg : "+args[1]);
                                ArrayList<UUID> players = ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(args[1]));
                                sender.sendMessage(Component.text(ChatColor.YELLOW+"==[Team ").append(ArTeamManager.getTeamColoredName(args[1])).append(Component.text(ChatColor.YELLOW+" Players]==")));
                                for(UUID pid : players){
                                    try{
                                        sender.sendMessage(ChatColor.YELLOW+"- "+Bukkit.getOfflinePlayer(pid).getName());
                                    } catch(NullPointerException e){
                                        e.printStackTrace();
                                    }
                                }
                                StringBuilder lowbar = new StringBuilder("=================");
                                for(int i = 1; i<= args[1].length(); i++){
                                    lowbar.append('=');
                                }
                                sender.sendMessage(ChatColor.YELLOW+lowbar.toString());
                            } catch(SQLException e){
                                e.printStackTrace();
                            }
                            return true;
                        }
                }
            } catch (
                    ArrayIndexOutOfBoundsException e){
                if(sender instanceof Player){
                    sender.sendMessage(ChatColor.RED+"Command failed, missing argument");
                }
            } catch (
                    SQLException e) {
                e.printStackTrace();
            }
        }
        
        return false;
    }
}
