package com.github.sawors.teams;

import org.bukkit.Color;

import java.util.ArrayList;
import java.util.UUID;

public class ArTeam {
    
    private ArrayList<UUID> members = new ArrayList<>();
    private String name = "no_name";
    private Color color;
    private int points;
    
    
    public void addMember(UUID p){
        members.add(p);
    }
    public ArrayList<UUID> getMembers(){
        return new ArrayList<>(this.members);
    }
    public void removeAllMembers(){
        members = new ArrayList<>();
    }
    
    // TODO :
    //  add a team ID column to DB to allow the safe modification of team name
    //  (touching to a team name is for the moment dangerous as it is it's unique identifier)
    public String getName() {
        return name;
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
    public void addPoints(int pts) {
        this.points += pts;
    }
    public void setPoints(int pts) {
        this.points = pts;
    }
    
    public ArTeam(String name, Color color){
        this.name = name;
        this.color = color;
        this.points = 0;
        this.members = new ArrayList<>();
    }
    
    
}
