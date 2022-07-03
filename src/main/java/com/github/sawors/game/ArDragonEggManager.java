package com.github.sawors.game;

import com.github.sawors.Main;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ArDragonEggManager {
    private static Entity eggholder = null;
    private static Location eggloc = null;
    private static boolean dragoneggbonus = true;
    private static ArDragonEggMode eggbonusmode = ArDragonEggMode.HOLD;
    private static int eggbonusperminute = 10;
    private static int eggfinalbonuspoints = 300;
    private static boolean eggshowholdercoordinates = true;
    private static int eggglowingholdertimer = 30;
    private static BukkitTask eggtimer = null;
    private static boolean eggtimerpaused = false;
    
    protected static Location tryToGetEggLocation(){
        Entity lastknownholder = getLastKnownHolder();
        if(lastknownholder != null && (lastknownholder instanceof ArmorStand || ((Player) lastknownholder).isOnline())){
            if(lastknownholder instanceof Player){
                for(ItemStack item : ((Player)lastknownholder).getInventory().getStorageContents()){
                    if(item != null && item.getType() == Material.DRAGON_EGG){
                        return lastknownholder.getLocation().clone();
                    }
                }
            } else {
                ItemStack item = ((ArmorStand) lastknownholder).getEquipment().getHelmet();
                if(item != null && item.getType() == Material.DRAGON_EGG){
                    return lastknownholder.getLocation().clone();
                }
            }
        }
        
        for(Player p : Bukkit.getOnlinePlayers()){
            for(ItemStack item : p.getInventory().getStorageContents()){
                if(item != null && item.getType() == Material.DRAGON_EGG){
                    eggholder = p;
                    return p.getLocation();
                }
            }
        }
        
        return new Location(ArGameManager.getGameworld(),0,0,0);
    }
    
    protected static void loadEggConfig(FileConfiguration config){
        ConfigurationSection eggsection = config.getConfigurationSection("dragon-egg-bonus");
        if(eggsection != null && eggsection.getBoolean("enabled")){
            try{
                eggbonusmode = ArDragonEggMode.valueOf(Objects.requireNonNull(eggsection.getString("bonus-mode")).toUpperCase(Locale.ROOT));
            } catch (NullPointerException | IllegalArgumentException e){
                Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] could not load egg bonus mode parameter, wrong syntax");
            }
            eggbonusperminute = eggsection.getInt("hold-points-per-minute");
            eggfinalbonuspoints = eggsection.getInt("end-bonus-points");
            eggshowholdercoordinates = eggsection.getBoolean("show-coordinates");
            eggglowingholdertimer = eggsection.getInt("glowing-timer");
        } else {
            dragoneggbonus = false;
        }
    }
    public static boolean isEggEnabled(){
        return dragoneggbonus;
    }
    public static boolean showEggHolderCoordinates(){
        return eggshowholdercoordinates;
    }
    public static ArDragonEggMode getEggBonusMode(){
        return eggbonusmode;
    }
    
    public static Entity getLastKnownHolder(){
        return eggholder;
    }
    
    public static Location getEggLocation(){
        return eggloc;
    }
    
    protected static void setEggHolder(Entity e){
        eggholder = e;
    }
    public static int getFinalBonusPoints(){
        return eggfinalbonuspoints;
    }
    
    protected static void spawnEggEntityHolder(Location loc){
        ArmorStand dropedeggholder = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        dropedeggholder.setInvulnerable(true);
        dropedeggholder.setInvisible(true);
        dropedeggholder.setItem(EquipmentSlot.HEAD, new ItemStack(Material.DRAGON_EGG));
        dropedeggholder.setGravity(false);
        dropedeggholder.setGlowing(true);
        dropedeggholder.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        dropedeggholder.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        dropedeggholder.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        dropedeggholder.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        dropedeggholder.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        dropedeggholder.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        setEggHolder(dropedeggholder);
        new BukkitRunnable(){
            @Override
            public void run() {
                dropedeggholder.setHeadPose(new EulerAngle(0,Math.toRadians(loc.getYaw()+180),0));
            }
        }.runTaskLater(Main.getPlugin(),1);
    }
    
    protected static void cancelEggTimer(){
        if(eggtimer != null){
            eggtimer.cancel();
        }
    }
    
    protected static void pauseEggTimer(){
        eggtimerpaused = true;
    }
    
    protected static void resumeEggTimer(){
        eggtimerpaused = false;
    }
    
    protected static boolean isTimerRunning(){
        return eggtimer != null;
    }
    
    protected static void startEggBonusTimer(){
        if(isEggEnabled() && getEggBonusMode().equals(ArDragonEggMode.HOLD)){
            eggtimer = new BukkitRunnable(){
                int i = 0;
                @Override
                public void run() {
                    eggloc = tryToGetEggLocation();
                    if(!ArGameManager.getGamephase().equals(ArGamePhase.INGAME)){
                        this.cancel();
                        return;
                    }
                    if(i > 0 && i%6 == 0 && !eggtimerpaused && getLastKnownHolder() != null && getLastKnownHolder() instanceof Player){
                        Player p = (Player) getLastKnownHolder();
                        String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
                        if(team != null){
                            ArTeamManager.addPointsToTeam(team, eggbonusperminute);
                            for(UUID pid : ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team))){
                                Player player = Bukkit.getPlayer(pid);
                                if(player != null){
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,.5f,1.2f);
                                    player.sendMessage(Component.text(ChatColor.GOLD+""+ChatColor.BOLD+"> Dragon Egg bonus : "+ChatColor.DARK_GREEN+"+"+eggbonusperminute+"pts"));
                                }
                            }
                        }
                    }
                    i++;
                }
                
            }.runTaskTimer(Main.getPlugin(),0,200);
        }
    }
    
    public static String getEggHolderPositionDisplay(){
        if(eggholder == null || getEggLocation() == null){
            return null;
        }
        Location eggloc = getEggLocation();
        StringBuilder strb = new StringBuilder();
        strb
            .append("X:")
            .append((int) eggloc.getX())
            .append(" Y:")
            .append((int) eggloc.getY())
            .append(" Z:")
            .append((int) eggloc.getZ());
        if(eggloc.getWorld().getName().toLowerCase(Locale.ENGLISH).contains("nether")){
            strb.append(" (Nether)");
        } else if(eggloc.getWorld().getName().toLowerCase(Locale.ENGLISH).contains("end")){
            strb.append(" (End)");
        }
        
        return strb.toString();
    }
    
}
