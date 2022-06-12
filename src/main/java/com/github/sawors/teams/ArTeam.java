package com.github.sawors.teams;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ArTeam {
    
    private ArrayList<Player> members = new ArrayList<>();
    private String name = "no_name";
    private Color color;
    private int points;
    
    
    public void addMember(Player p){
        members.add(p);
    }
    public ArrayList<Player> getMembers(){
        return this.members;
    }
    public void removeAllMembers(){
        members = new ArrayList<>();
    }
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
    public Color getColor() {
        return color;
    }
    public String getColorHex() {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        hex=hex.toUpperCase();
        return hex;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    
    public int getPoints() {
        return points;
    }
    
    public ArTeam(String name, Color color, int points, ArrayList<Player> players){
        this.name = name;
        this.color = color;
        this.points = points;
        this.members = players;
    }
    
    
}
