package com.github.sawors.game;

import com.github.sawors.Main;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;

public class ArDragonEggListeners implements Listener {
    
    @EventHandler
    public static void pickupEggOnInteract(PlayerInteractAtEntityEvent event){
        Player p = event.getPlayer();
        Entity e = event.getRightClicked();
        if(e instanceof ArmorStand && ((ArmorStand)e).getEquipment().getHelmet() != null && ((ArmorStand)e).getEquipment().getHelmet().getType().equals(Material.DRAGON_EGG)){
            if(p.getInventory().firstEmpty() == -1){
                p.showTitle(Title.title(Component.text(""),Component.text(ChatColor.RED+"Your inventory is full, can't pickup the egg")));
                event.setCancelled(true);
            } else {
                e.remove();
                if(ArDragonEggManager.getLastKnownHolder() == null || !ArDragonEggManager.isTimerRunning()){
                    ArDragonEggManager.startEggBonusTimer();
                }
                if(p.getInventory().getItemInMainHand().getType().isAir()){
                    p.getInventory().setItemInMainHand(new ItemStack(Material.DRAGON_EGG));
                } else {
                    p.getInventory().addItem(new ItemStack(Material.DRAGON_EGG));
                }
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,1,1.25f);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM,.5f,.85f);
                p.showTitle(Title.title(Component.text(""),Component.text(ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"You are now the Egg Holder"), Title.Times.times(Duration.ofMillis(250),Duration.ofMillis(1000),Duration.ofMillis(250))));
                ArDragonEggManager.setEggHolder(p);
                String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
                if(team != null){
                    Bukkit.broadcast(Component.text(ChatColor.DARK_PURPLE+"Team "+ChatColor.BOLD).append(ArTeamManager.getTeamColoredName(team)).append(Component.text(ChatColor.DARK_PURPLE+" has picked up the Dragon Egg")));
                }
            }
        }
    }
    
    @EventHandler
    public static void placeEggBlockPlace(BlockPlaceEvent event){
        if(event.getBlock().getType().equals(Material.DRAGON_EGG)){
            event.setCancelled(true);
            event.getPlayer().getInventory().remove(Material.DRAGON_EGG);
            Location loc = event.getBlock().getLocation().add(0.5,-1.5,0.5);
            loc.setYaw(event.getPlayer().getLocation().getYaw());
            ArDragonEggManager.spawnEggEntityHolder(loc);
        }
    }
    
    @EventHandler
    public static void replaceEggItemByHolderArmorstand(ItemSpawnEvent event){
        if(event.getEntity().getItemStack().getType().equals(Material.DRAGON_EGG)){
            Location baseloc = event.getEntity().getLocation();
            Block b = baseloc.getBlock();
            Block checkb = b;
            for(int i = 0; i>= -baseloc.getWorld().getMaxHeight(); i--){
                checkb = baseloc.clone().add(0,i,0).getBlock();
                if(!checkb.getType().isAir()){
                   b = checkb;
                   break;
                }
            }
            Main.logAdmin(b.getType());
            ArDragonEggManager.spawnEggEntityHolder(b.getLocation().add(.5,-.5,.5));
            event.getEntity().remove();
        }
    }
    
    @EventHandler
    public static void preventEggInChest(InventoryClickEvent event){
        if(event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.DRAGON_EGG) &&!(event.getInventory().getType().equals(InventoryType.CRAFTING) || event.getInventory().getType().equals(InventoryType.WORKBENCH))){
            event.setCancelled(true);
        }
    }
    
    
}
