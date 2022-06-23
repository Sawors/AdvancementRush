package com.github.sawors;

import org.bukkit.Color;

public class UsefulTools {
    
    
    public static Color stringToColorElseRandom(String potentialcolor){
        int hexrgb;
        StringBuilder preint = new StringBuilder("0x");
        if((potentialcolor.charAt(0) == '#') && potentialcolor.length() >= 7){
            for(int i = 1; i<7; i++){
                char check = potentialcolor.charAt(i);
                if(Character.isLetterOrDigit(check)){
                    preint.append(check);
                }
            }
        }
        try{
            hexrgb = Integer.decode(preint.toString());
        } catch (NumberFormatException e){
            hexrgb = (int) (Math.random()*0xFFFFFF);
        }
        return Color.fromRGB(hexrgb);
    }
    
    public static Color getRandomColor(){
        return Color.fromRGB(((int)(Math.random()*255)),((int)(Math.random()*255)),((int)(Math.random()*255)));
    }
    public static String getRandomColorHex(){
        return "#" +
            Integer.toHexString((int) (Math.random() * 255)) +
            Integer.toHexString((int) (Math.random() * 255)) +
            Integer.toHexString((int) (Math.random() * 255));
    }
    
    public static String getColorHex(Color color) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        hex=hex.toUpperCase();
        return hex;
    }
}
