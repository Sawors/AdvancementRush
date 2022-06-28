package com.github.sawors.commands;

import com.github.sawors.ArDataBase;
import com.github.sawors.Main;
import com.github.sawors.UsefulTools;
import com.github.sawors.advancements.AdvancementManager;
import com.github.sawors.game.ArGameManager;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ArTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if(sender instanceof Player){
            final Player p = (Player) sender;
            if(args.length > 0){
                switch(args[0]){
                    case"getcrits":
                        Main.logAdmin(ChatColor.YELLOW+"minecraft:adventure/adventuring_time");
                        for(String crit : Objects.requireNonNull(Bukkit.getAdvancement(Objects.requireNonNull(NamespacedKey.fromString("minecraft:adventure/adventuring_time")))).getCriteria()){
                            Main.logAdmin(crit);
                        }
                        Main.logAdmin(ChatColor.YELLOW+"minecraft:story/mine_stone");
                        for(String crit : Objects.requireNonNull(Bukkit.getAdvancement(Objects.requireNonNull(NamespacedKey.fromString("minecraft:story/mine_stone")))).getCriteria()){
                            Main.logAdmin(crit);
                        }
                        break;
                    case"testcrits":
                        Main.logAdmin(ArDataBase.advancementCriteriaSerialize(NamespacedKey.fromString("minecraft:adventure/adventuring_time"), ((Player)sender).getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString("minecraft:adventure/adventuring_time"))).getRemainingCriteria()));
                        break;
                    case"reversecrits":
                        Main.logAdmin(ArDataBase.advancementCriteriaDeserialize("minecraft:adventure/adventuring_time(minecraft:snowy_slopes,minecraft:old_growth_pine_taiga,minecraft:mushroom_fields,minecraft:taiga,minecraft:deep_ocean,minecraft:eroded_badlands,minecraft:frozen_river,minecraft:sunflower_plains,minecraft:birch_forest,minecraft:windswept_hills,minecraft:wooded_badlands,minecraft:bamboo_jungle,minecraft:savanna_plateau,minecraft:beach,minecraft:dark_forest,minecraft:stony_peaks,minecraft:sparse_jungle,minecraft:lukewarm_ocean,minecraft:river,minecraft:stony_shore,minecraft:snowy_plains,minecraft:snowy_taiga,minecraft:dripstone_caves,minecraft:swamp,minecraft:grove,minecraft:jagged_peaks,minecraft:cold_ocean,minecraft:forest,minecraft:lush_caves,minecraft:deep_cold_ocean,minecraft:ice_spikes,minecraft:frozen_ocean,minecraft:desert,minecraft:windswept_forest,minecraft:ocean,minecraft:jungle,minecraft:old_growth_spruce_taiga,minecraft:snowy_beach,minecraft:windswept_savanna,minecraft:warm_ocean,minecraft:deep_lukewarm_ocean,minecraft:flower_forest,minecraft:frozen_peaks,minecraft:old_growth_birch_forest,minecraft:meadow,minecraft:windswept_gravelly_hills,minecraft:savanna)").toString());
                        break;
                    case"checkcomplete":
                        try {
                            if(ArTeamManager.hasTeamAdvancementCompleted("Sawors2",NamespacedKey.fromString("minecraft:adventure/adventuring_time"))){
                                sender.sendMessage("yes");
                            } else {
                                sender.sendMessage("no");
                            }
                        } catch (
                                SQLException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case"mutelist":
                        ArDataBase.printMuteMap();
                        break;
                    case"places":
                        p.sendMessage(ChatColor.GREEN+"Chat Message");
                        p.sendActionBar(Component.text(ChatColor.RED+"Action Bar"));
                        p.showTitle(Title.title(Component.text(ChatColor.GOLD+"Title"), Component.text(ChatColor.YELLOW+"Subtitle")));
                        final BossBar bar = BossBar.bossBar(Component.text(ChatColor.DARK_AQUA+"Bossbar"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_6);
                        p.showBossBar(bar);
                        new BukkitRunnable(){
                            @Override
                            public void run(){
                                p.hideBossBar(bar);
                            }
                        }.runTaskLater(Main.getPlugin(), 4*20);
                        break;
                    case"compact":
                        sender.sendMessage(Component.text("[BigChungus] ").color(TextColor.fromCSSHexString(UsefulTools.getRandomColorHex())).append(Component.text(ChatColor.WHITE+"Sawors has made the advancement ")).append(Component.text(ChatColor.GREEN+"[Diamonds!]")).append(Component.text(ChatColor.DARK_GREEN+" +10pts")).append(Component.text(ChatColor.GOLD+" "+ChatColor.MAGIC+"A"+ChatColor.GOLD+"+5pts"+ChatColor.MAGIC+"A")));
                        break;
                    case"timer":
                        ArGameManager.startTimer();
                        break;
                    case"win":
                        ArGameManager.startWinnerAnnouncementSequence();
    
                }
            } else {
                sender.sendMessage("Phase 1");
                ArrayList<String> advlist = new ArrayList<>();
                int i = 0;
                for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
                    if(i >= 5){
                        break;
                    }
                    Advancement adv = it.next();
                    if(!AdvancementManager.isRecipe(adv)){
                        advlist.add(adv.getKey().toString());
                        i++;
                    }
                }
                ArTeamManager.setTeamAdvancements("test", ArDataBase.teamAdvancementsSerialize(advlist));
                sender.sendMessage("Phase 2");
                try{
                    ArrayList<String> out = ArDataBase.teamAdvancementsDeserialize(ArTeamManager.getTeamAdvancements("test"));
                    for(String adv : out){
                        AdvancementProgress pgr = ((Player) sender).getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString(adv)));
                        for(String ctr : pgr.getRemainingCriteria()){
                            pgr.awardCriteria(ctr);
                        }
                    }
                } catch (
                        SQLException | NullPointerException e) {
                    e.printStackTrace();
                }
    
            }
            return true;
        }
        
        return false;
    }
}
