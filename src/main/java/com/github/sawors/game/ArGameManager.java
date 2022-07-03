package com.github.sawors.game;

import com.github.sawors.Main;
import com.github.sawors.UsefulTools;
import com.github.sawors.database.ArDataBase;
import com.github.sawors.discordbot.ArDBotManager;
import com.github.sawors.teams.ArTeamDisplay;
import com.github.sawors.teams.ArTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;

public class ArGameManager extends ArDataBase{
    
    private static ArGameMode gamemode = ArGameMode.TIMER;
    private static int duration = 240;
    private static boolean showscores = true;
    private static int hiderankstimer = 30;
    private static int targetvalue = 1000;
    
    private static BukkitTask timerinstance = null;
    private static int timer = 0;
    private static ArGamePhase gamephase = ArGamePhase.TEAM_SELECTION;
    private static boolean showranks = true;
    private static int finalrankshowlength = 1;
    private static int ranktitleduration = 4;
    private static Location originloc;
    private static World gameworld;
    private static int spawnheight = 64;
    private static int platformradius = 24;
    private static int spreadradius = 256;
    private static boolean enableminigame = true;
    private static boolean randomspread = false;
    private static Set<UUID> frozenplayers = new HashSet<>();
    private static boolean ranksrevelation = true;
    private static boolean groupback = true;
    private static int groupdelay = 15;
    private static String groupdestination = "LOBBY";
    private static boolean startdiscordsplit = true;
    private static boolean enddiscordgroup = true;
    private static boolean deathkeepstuff = true;
    private static boolean deathpositionmessage = true;
    
    public static void initGameMode(){
        gameworld = Bukkit.getServer().getWorlds().get(0);
        loadConfigValues(Main.getMainConfig());
        cancelTimerTask();
        setGamephase(ArGamePhase.TEAM_SELECTION);
        
    }
    
    public static void loadConfigValues(FileConfiguration config){
        try{
            gamemode = ArGameMode.valueOf(Objects.requireNonNull(config.getString("gamemode")).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e){
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] could not load gamemode parameter, wrong syntax");
        }
        ConfigurationSection timersection = config.getConfigurationSection("timer-options");
        ConfigurationSection valuesection = config.getConfigurationSection("value-options");
        ConfigurationSection hybridsection = config.getConfigurationSection("hybrid-options");
        if(gamemode == ArGameMode.TIMER && timersection != null){
            duration = timersection.getInt("duration");
            showscores = timersection.getBoolean("show-scores");
            hiderankstimer = timersection.getInt("hide-ranks-timer");
        } else if(gamemode == ArGameMode.VALUE && valuesection != null){
            targetvalue = valuesection.getInt("target-value");
        } else if(gamemode == ArGameMode.HYBRID && hybridsection != null){
            targetvalue = hybridsection.getInt("target-value");
            duration = hybridsection.getInt("duration");
            hiderankstimer = hybridsection.getInt("hide-ranks-timer");
            showscores = hybridsection.getBoolean("show-scores");
        }
        enableminigame = Main.getMainConfig().getBoolean("enable-waiting-room-minigame");
        platformradius = config.getInt("spawn-platform-radius");
        int getspawnheight = config.getInt("spawn-platform-height");
        if(getspawnheight > 0){
            spawnheight = getspawnheight;
        }
        randomspread = config.getBoolean("random-spread");
        spreadradius = config.getInt("spread-teams-radius");
        if(spreadradius < -1){
            spreadradius = 0;
        }
        groupdelay = config.getInt("end-auto-move-delay");
        if(groupdelay < 0){
            groupdelay = 0;
        }
        ranksrevelation = config.getBoolean("ranks-revelation");
        ConfigurationSection groupbacksection = config.getConfigurationSection("group-back");
        if(groupbacksection != null){
            groupback = groupbacksection.getBoolean("enabled");
            groupdelay = groupbacksection.getInt("delay");
            groupdestination = groupbacksection.getString("tp-destination");
            if(groupdestination == null || (!groupdestination.equalsIgnoreCase("LOBBY") && !groupdestination.equalsIgnoreCase("WORLD"))){
                Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] could not set group-back destination, using default value LOBBY...");
            }
        }
        startdiscordsplit = config.getBoolean("start-auto-move");
        enddiscordgroup = config.getBoolean("end-auto-move");
        
        deathkeepstuff = config.getBoolean("death-keep-basic-equipment");
        deathpositionmessage = config.getBoolean("death-tell-coordinates");
        
        // Maybe messy to keep it here
        ArDragonEggManager.loadEggConfig(config);
    }
    
    protected static boolean doKeepStuffOnDeath(){
        return deathkeepstuff;
    }
    protected static boolean doDeathCoordinatesMessage(){
        return deathpositionmessage;
    }
    
    public static Set<UUID> getFrozenPlayers(){
        return frozenplayers;
    }
    public static boolean isFrozen(UUID pid){
        return frozenplayers.contains(pid);
    }
    public static void freezePlayer(UUID pid){
        Player p = Bukkit.getPlayer(pid);
        if(p != null){
            frozenplayers.add(p.getUniqueId());
        }
    }
    public static void unFreezePlayer(UUID pid){
        frozenplayers.remove(pid);
    }
    
    protected static void startTimer(boolean reset){
        cancelTimerTask();
        if(reset){timer = 0;}
        setGamephase(ArGamePhase.INGAME);
        getNewTimer();
    }
    protected static void startTimer(){
        startTimer(true);
    }
    protected static void resetTimer(){
        setGamephase(ArGamePhase.TEAM_SELECTION);
        cancelTimerTask();
        timer = 0;
        refreshTimerTablistDisplay();
    }
    public static void refreshTimerTablistDisplay(){
        for(Player p : Bukkit.getOnlinePlayers()){
            ArTeamDisplay.updateTablist(p,getTimerDisplay(),ArDragonEggManager.getEggHolderPositionDisplay());
        }
    }
    public static String getTimerDisplay(){
        int hours = ((duration*60)-timer) / 3600;
        int minutes = (((duration*60)-timer) % 3600) / 60;
        int seconds = ((duration*60)-timer) % 60;
        return String.format(Locale.ENGLISH,"%02d:%02d:%02d", hours, minutes, seconds);
    }
    public static void setGameDuration(int durationinminutes){
        duration = durationinminutes;
    }
    public static void forceSetTimer(int timerinminutes){
        // do this check first because we cannot know if at this time timer is used, we do this to avoid assigning it by mistake to a negative value for an instant
        if (timerinminutes*60 < 0) {timer = 0;return;}
        timer = timerinminutes*60;
        if(timer >= duration*60){
            startWinnerAnnouncementSequence();}
        
    }
    /*public static void updateTimerInDatabase(int time){
        try(Connection co = ArDataBase.connect()){
            String query = "UPDATE game SET VALUE = "+time+" WHERE DATA = '"+ArGameData.TIMER+"'";
            co.prepareStatement(query).executeQuery();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static int loadTimerValue(){
        try(Connection co = ArDataBase.connect()){
            String query = "SELECT VALUE FROM game WHERE DATA = '"+ArGameData.TIMER+"'";
            return Integer.getInteger(co.prepareStatement(query).executeQuery().getString("VALUE"));
        } catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
    }*/
    public static int getTimerTime(){
        return timer;
    }
    public static void stopTimerCount(){
        cancelTimerTask();
    }
    private static void cancelTimerTask(){
        try{
            timerinstance.cancel();
        }catch (NullPointerException e){
            Bukkit.getLogger().log(Level.WARNING, "[Advancement Rush] no timer currently running, nothing has been cancelled");
        }
    }
    private static void getNewTimer(){
        cancelTimerTask();
        timerinstance = new BukkitRunnable() {
            @Override
            public void run() {
                timer++;
                refreshTimerTablistDisplay();
                int remainingseconds = (duration*60)-timer;
                if(timer >= duration*60){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            startWinnerAnnouncementSequence();
                        }
                    }.runTask(Main.getPlugin());
                    this.cancel();
                    return;
                }
                
                //do timed events here !!!
                
                
                // 30 minutes in announcement
                if(duration > 2*60 && timer == 30*60){
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW+"-> "+"30 minutes"));
                    // 25% timer announcement
                } else if(timer == duration*60*0.25){
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW+"-> "+getRemainingTimeDisplay()));
                    // 50% timer announcement
                }else if(timer == duration*60*0.5){
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW+"-> "+getRemainingTimeDisplay()));
                    // 75% timer announcement
                }else if(timer == duration*60*0.75){
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW+"-> "+getRemainingTimeDisplay()));
                }
                
                if(getEndGameTime() != 0 && remainingseconds == getEndGameTime()*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,.1f, 1.5f);
                    }
                    Bukkit.broadcast(Component.text(ChatColor.RED+"-> "+getRemainingTimeDisplay()));
                    Bukkit.broadcast(Component.text(ChatColor.RED+""+ChatColor.MAGIC+"ED"+ChatColor.RED+" "+ChatColor.BOLD+"team ranking is now hidden ! "+ChatColor.MAGIC+"ED"));
                }
                
                // endgame announcement
                // GAMEPHASE ENDGAME SET AND ANNOUNCEMENT
                if(getEndGameTime() != 0 && remainingseconds <= getEndGameTime()*60){
                    setGamephase(ArGamePhase.ENDGAME);
                    //must create a method to handle endgame
                    showranks = false;
                    for(Player p : Bukkit.getOnlinePlayers()){
                        ArTeamDisplay.updatePlayerDisplay(p, ArTeamManager.getPlayerTeam(p.getUniqueId()));
                        if(remainingseconds <= 10){
                            p.showTitle(Title.title(Component.text(ChatColor.RED+String.valueOf(remainingseconds)), Component.text(" ")));
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1, 0.8f+(0.05f*(10-remainingseconds)));
                        }
                    }
                }
                
                
            }
        }.runTaskTimer(Main.getPlugin(),0,20);
    }
    
    public static String getRemainingTimeDisplay(){
        int hours = ((duration*60)-timer) / 3600;
        int minutes = (((duration*60)-timer) % 3600) / 60;
        int seconds = ((duration*60)-timer) % 60;
        if(hours >= 1){
            String plural = minutes == 1 ? "" : "s";
            if(minutes < 10){
                return hours+" hour"+plural+" remaining";
            }
            return hours+" hour"+plural+" "+minutes+" minute"+plural+" remaining ";
        } else if(minutes >= 1){
            String plural = minutes == 1 ? "" : "s";
            return minutes+" minute"+plural+" remaining";
        } else {
            return seconds+" seconds remaining";
        }
    }
    
    
    
    public static int getTargetValue(){
        if(gamemode != ArGameMode.VALUE){
            return targetvalue;
        } else return 0;
    }
    public static ArGameMode getGameMode(){
        return gamemode;
    }
    
    
    public static ArGamePhase getGamephase() {
        return gamephase;
    }
    
    
    
    //
    //  GAME PHASES
    //
    protected static void setGamephase(ArGamePhase gamephase) {
        //TODO : /!\ MOVE ALL ACTIONS TRIGGERED ON GAME PHASE CHANGE HERE
        switch(gamephase){
            case TEAM_SELECTION:
               
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for(World w : Bukkit.getWorlds()){
                            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            w.setFullTime(0);
                            w.setDifficulty(Difficulty.PEACEFUL);
                        }
                    }
                }.      // I do this delay otherwise gamerules are not loaded
                        runTaskLater(Main.getPlugin(), 60);
                
                
            case INGAME:
                for(World w : Bukkit.getWorlds()){
                    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                    w.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                    if(Main.getMainConfig().getBoolean("difficulty-hard")){w.setDifficulty(Difficulty.HARD);} else {w.setDifficulty(Difficulty.NORMAL);}
                }
                
            case ENDGAME:
            case WINNER_ANNOUNCEMENT:
        }
        ArGameManager.gamephase = gamephase;
        for(Player p : Bukkit.getOnlinePlayers()){
            ArTeamDisplay.updatePlayerDisplay(p, ArTeamManager.getPlayerTeam(p.getUniqueId()));
        }
    }
    
    public static boolean showScores(){
        return showscores;
    }
    
    public static boolean showRanks(){
        return showranks;
    }
    public static int getEndGameTime(){
        return hiderankstimer;
    }
    
    public static int getFinalRankingShowLength(){
        return finalrankshowlength;
    }
    
    public static int getRankTitleDuration(){
        return ranktitleduration;
    }
    
    protected static void generateSpawnLobby(Material material){
        if(material.isBlock() || material.isAir() && getPlatformRadius() > 0){
            World w = getGameworld();
            Material locmat;
            int y = w.getSeaLevel()+getSpawnheight();
            originloc = new Location(w,0,y,0);
            originloc.setY(y);
            int cageradius = getPlatformRadius();
            
            for(int i = -cageradius; i<cageradius; i++){
                //FLOOR
                for(int z = -cageradius+1; z<=cageradius-1; z++){
                    w.getBlockAt(i,y,z).setType(material);
                }
                
                for(int y2 = 0; y2<9; y2++){
                    if(y2 == 0 && !material.isAir()){
                        locmat = Material.RED_STAINED_GLASS;
                    } else {
                        locmat = material;
                    }
                    //NORTH-SOUTH
                    w.getBlockAt(i,y+y2,-cageradius).setType(locmat);
                    w.getBlockAt(-i,y+y2,cageradius).setType(locmat);
    
                    //EAST-WEST
                    w.getBlockAt(-cageradius,y+y2,i+1).setType(locmat);
                    w.getBlockAt(cageradius,y+y2,-i-1).setType(locmat);
                }
            }
        }
    }
    
    protected static void spreadSpawnTeams(){
        List<String> teams = ArTeamManager.getTeamList();
        randomspread = false;
        World w = getGameworld();
        int spawnradius = getSpreadradius();
        Location baseloc = new Location(w, 0.5,0,0.5);
        double angle = Math.toRadians(360f/teams.size());
        Vector spawnlocvec = new Vector(spawnradius,0,0);
        Main.logAdmin(spawnradius);
        if(spawnradius == -1){
            Bukkit.getLogger().log(Level.INFO,"[Advancement Rush] Spawn radius set to -1 (auto) in config, calculating it for "+teams.size()+" teams...");
            double radius = (Math.sin(90-(angle/2))*Bukkit.getServer().getViewDistance()*16)/(Math.sin(angle));
            spawnlocvec.setX(radius);
            Bukkit.getLogger().log(Level.INFO,"[Advancement Rush] Spawn radius = "+String.format("%.2f", radius));
        }
        final float inteamspawnradius = 1;
        final int spawnheight = getSpawnheight();
        for (String team : teams) {
        
            if (randomspread) {
                spawnlocvec.setX(Math.random() * 2 * spawnradius);
                spawnlocvec.setZ(Math.random() * 2 * spawnradius);
            } else {
                spawnlocvec.rotateAroundY(angle);
            }
            spawnlocvec.setY(getSpawnheight());
            Set<UUID> players = ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team));
            Vector inteamspread = new Vector(inteamspawnradius, 0, 0);
            double inteamangle = Math.toRadians(360f / players.size());
            final Location spawnloc = w.getHighestBlockAt(baseloc.clone().add(spawnlocvec)).getLocation().add(0.5, spawnheight, 0.5);
            new BukkitRunnable() {
                Location teamspawncenter = spawnloc.clone().add(0, -spawnheight+0.75, 0);
                Vector rotation = new Vector(inteamspawnradius+1.5, 0, 0);
                Vector nrotation = new Vector(-(inteamspawnradius+1.5), 0, 0);
                @Override
                public void run() {
                    if(!getGamephase().equals(ArGamePhase.TEAM_SELECTION)){
                        this.cancel();
                        return;
                    }
                    rotation.rotateAroundY(Math.toRadians(9));
                    nrotation.rotateAroundY(Math.toRadians(9));
                    w.spawnParticle(Particle.REDSTONE,teamspawncenter.clone().add(rotation),1,0,0,0, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                    w.spawnParticle(Particle.REDSTONE,teamspawncenter.clone().add(nrotation),1,0,0,0, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                }
            }.runTaskTimer(Main.getPlugin(), 0, 1);
            if(players.size() <= 1){
                inteamspread = new Vector(0,0,0);
            }
            for (UUID id : players) {
                spawnlocvec.rotateAroundY(inteamangle);
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 1, false, false, false));
                    preparePlayerForGame(p);
                    p.teleport(spawnloc.add(inteamspread));
                }
            }
        }
    }
    
    public static boolean isMinigameEnabled(){
        return enableminigame;
    }
    public static World getGameworld() {
        return gameworld;
    }
    
    public static void setGameworld(World gameworld) {
        ArGameManager.gameworld = gameworld;
    }
    
    public static int getSpawnheight() {
        return spawnheight;
    }
    
    public static void setSpawnheight(int spawnheight) {
        ArGameManager.spawnheight = spawnheight;
    }
    
    public static int getPlatformRadius() {
        return platformradius;
    }
    
    public static void setPlatformradius(int platformradius) {
        ArGameManager.platformradius = platformradius;
    }
    
    public static int getSpreadradius() {
        return spreadradius;
    }
    
    public static void setSpreadradius(int spreadradius) {
        ArGameManager.spreadradius = spreadradius;
    }
    
    public static void setEnableminigame(boolean enableminigame) {
        ArGameManager.enableminigame = enableminigame;
    }
    public static boolean discordEndGameGroup(){
        return enddiscordgroup;
    }
    
    
    
    
    public static void startGame(){
        //
        //  NEVER SWITCH GAME PHASES LIKE THAT, ONLY DOING THIS HERE FOR THE DAMAGE CANCELLATION !!
        //  TODO : Add a gamephase for player freeze (drop sequence + endgame ?) or find a better way to effectively deny their movements and actions
        gamephase = ArGamePhase.TEAM_SELECTION;
        Main.logAdmin(
                "Game Started :"+
                        "\n Teams : "+ArTeamManager.getTeamList()+
                        "\n Players : "+Bukkit.getOnlinePlayers()+
                        "\n Duration : "+getTimerDisplay()+
                        "\n World name : "+getGameworld().getName()+
                        "\n Game mode : "+getGameMode()+
                        "\n Dragon Egg : "+ArDragonEggManager.isEggEnabled()+
                        "\n Dragon Egg mode : "+ArDragonEggManager.getEggBonusMode()
        );
        ArDragonEggManager.setEggHolder(null);
        new BukkitRunnable(){
            final int imax = 8;
            int i = 1;
            @Override
            public void run() {
                
                
                
                
                switch(i){
                    case 1:
                        ArDBotManager.createGameCategory();
                        break;
                    case 2:
                        ArDBotManager.createTeamRoles();
                        break;
                    case 3:
                        ArDBotManager.createTeamChannel();
                        break;
                    case 4:
                        ArDBotManager.giveTeamRoles();
                        break;
                    case 5:
                        if(startdiscordsplit){
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    ArDBotManager.sendUsersToTeamChannels();
                                }
                            }.runTaskAsynchronously(Main.getPlugin());
                        }
                        this.cancel();
                        return;
                }
                if(i>= imax){
                    this.cancel();
                    return;
                }
                i++;
            }
        }.runTaskTimer(Main.getPlugin(),0,40);
        Set<Player> tocheck = new HashSet<>();
        for(String team : ArTeamManager.getTeamList()){
            for(UUID id : ArDataBase.teamMembersDeserialize(ArTeamManager.getTeamPlayers(team))){
                Player p = Bukkit.getPlayer(id);
                if(p != null && p.isOnline()){
                    tocheck.add(p);
                }
            }
        }
        Set<Player> freezelist = new HashSet<>();
        Main.logAdmin(tocheck.toString());
        generateSpawnLobby(Material.AIR);
        new BukkitRunnable(){
            // seconds before timeout (*4 to translate it to iterations)
            int timeout = 10*4;
            int testnb = 0;
            boolean allplayersvalidated = false;
            Set<Player> checked = new HashSet<>();
            @SuppressWarnings( "deprecation" )
            @Override
            public void run() {
                if(allplayersvalidated){
                    new BukkitRunnable(){
                        // timer shown right before the game start (in seconds)
                        final int pregametimer = 5;
                        int i = -1;
                        String countdown;
                        float pitch = 1;
                        @Override
                        public void run() {
                            if(i == -1){
                                countdown = "Game starts in...";
                                pitch = .5f;
                            } else{
                                countdown = String.valueOf(pregametimer-i);
                                pitch = 1.15f;
                            }
    
    
                            if(i<pregametimer){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,pitch);
                                    p.showTitle(Title.title(Component.text(ChatColor.RED+countdown), Component.text(""), Title.Times.times(Duration.ofMillis(400),Duration.ofMillis(500),Duration.ofMillis(100))));
                                }
                            } else {
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1f);
                                    p.showTitle(Title.title(Component.text(""), Component.text(ChatColor.GOLD+"GO GO GO GO !"), Title.Times.times(Duration.ofMillis(200),Duration.ofMillis(1000),Duration.ofMillis(800))));
                                }
                            }
                            
                            i++;
                            if(i > pregametimer){
                                //ACTUALLY STARTING THE GAME !
                                setGamephase(ArGamePhase.INGAME);
                                for(Player p : freezelist){
                                    unFreezePlayer(p.getUniqueId());
                                    p.setBedSpawnLocation(p.getLocation(), true);
                                }
                                startTimer(true);
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(Main.getPlugin(),10,20);
                    this.cancel();
                    return;
                } else {
                    if(checked.size() >= tocheck.size()){
                        allplayersvalidated = true;
                    } else {
                        for(Player p : tocheck){
                            if(p.isOnline() && p.isOnGround()){
                                checked.add(p);
                                freezelist.add(p);
                                freezePlayer(p.getUniqueId());
                                Main.logAdmin("Player "+p.getName()+" validated ("+checked.size()+"/"+tocheck.size()+")");
                            }
                        }
                    }
                    
                }
                
                
                
                if(testnb >= timeout){
                    this.cancel();
                    Bukkit.getLogger().log(Level.SEVERE, "[Advancement Rush] COULD NOT START THE GAME, PLAYERS NOT ON GROUND");
                }
            }
        }.runTaskTimer(Main.getPlugin(),10,5);
        spreadSpawnTeams();
    }
    protected static void startWinnerAnnouncementSequence(){
        stopTimerCount();
        ArDragonEggManager.cancelEggTimer();
        if(ArDragonEggManager.getEggBonusMode().equals(ArDragonEggMode.END) && ArDragonEggManager.getLastKnownHolder() instanceof Player){
            Player p = (Player) ArDragonEggManager.getLastKnownHolder();
            String team = ArTeamManager.getPlayerTeam(p.getUniqueId());
            if(team != null){
                ArTeamManager.addPointsToTeam(team, ArDragonEggManager.getFinalBonusPoints());
                for(Player pl : Bukkit.getOnlinePlayers()){
                    pl.playSound(pl.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,0.5f,0.8f);
                }
                Bukkit.broadcast(Component.text(ChatColor.GOLD+"Team ").append(ArTeamManager.getTeamColoredName(team)).append(Component.text(ChatColor.GOLD+" has earned the points of the Dragon Egg holder "+ChatColor.DARK_GREEN+"+"+ArDragonEggManager.getFinalBonusPoints()+"pts")));
            }
        }
        //resetTimer();
        new BukkitRunnable(){
            Title title;
            Sound sound;
            float pitch = 1;
            int count = 1;
            // maxcount = 5 -> 10 seconds before ranks reveal (maxcount*2)
            int maxcount = 5;
            @Override
            public void run() {
                switch(count){
                    case 1:
                        sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                        pitch = 0.85f;
                        title = Title.title(Component.text(ChatColor.GOLD+"Game Finished !"), Component.text(ChatColor.YELLOW+"gg !"), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(250)));
                        break;
                    case 2:
                        pitch = 1;
                        sound = Sound.ENTITY_PLAYER_LEVELUP;
                        title = Title.title(Component.text(ChatColor.GOLD+"Announcing Winners"), Component.text(""), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(250)));
                        break;
                    case 3:
                        pitch = 1;
                        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        title = Title.title(Component.text(ChatColor.GREEN+"..."), Component.text(""), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(250)));
                        break;
                    case 4:
                        pitch = 1f;
                        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        title = Title.title(Component.text(ChatColor.GREEN+"..."), Component.text(""), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(250)));
                        break;
                    case 5:
                        pitch = 1.2f;
                        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        title = Title.title(Component.text(ChatColor.GOLD+"..."), Component.text(""), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(250)));
                        break;
                }
                if(count <= 5){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(pitch > 0){
                            p.playSound(p.getLocation(), sound,1,pitch);
                        }
                        p.showTitle(title);
                    }
                }
                if(count > maxcount){
                    announceWinners();
                    this.cancel();
                    return;
                }
                count++;
            }
        }.runTaskTimer(Main.getPlugin(), 0,40);
    }
    
    private static void announceWinners(){
        showscores = true;
        showranks = true;
        setGamephase(ArGamePhase.WINNER_ANNOUNCEMENT);
        refreshTimerTablistDisplay();
        int imax = ArTeamManager.getTeamsRanking().size();
        new BukkitRunnable(){
            int i = 1;
            @Override
            public void run() {
                if(i>imax){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            // group discord
                            ArDBotManager.deleteCategory();
                            ArDBotManager.deleteRoles();
                            if(Objects.equals(groupdestination, "LOBBY")){
                                generateSpawnLobby(Material.BARRIER);
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    preparePlayerForGame(p);
                                    p.teleport(new Location(gameworld, 0,gameworld.getSeaLevel()+spawnheight+2,0));
                                }
                            }
                        }
                    }.runTaskLater(Main.getPlugin(), groupdelay*20L);
                    this.cancel();
                    return;
                }
                for(Player p : Bukkit.getOnlinePlayers()){
                    try{
                        new BukkitRunnable(){
                            final int times = ranktitleduration;
                            int timesi = 0;
                        
                            @Override
                            public void run() {
                                new BukkitRunnable(){
                                    final int soundnb = i;
                                    int locali = 0;
                                    @Override
                                    public void run() {
                                        if(locali >= soundnb){
                                            this.cancel();
                                            return;
                                        }
                                        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f,1+(.01f*i));
                                        locali++;
                                    }
                                }.runTaskTimer(Main.getPlugin(), 0, 1);
                                new BukkitRunnable(){
                                    final int soundnb = i;
                                    int locali = 0;
                                    @Override
                                    public void run() {
                                        if(locali >= soundnb){
                                            this.cancel();
                                            return;
                                        }
                                        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f,1+(.01f*i));
                                        locali++;
                                    }
                                }.runTaskTimer(Main.getPlugin(), 0, 2);
                                timesi++;
                                if(timesi >= times){
                                    this.cancel();
                                }
                            
                            }
                        
                        }.runTaskTimer(Main.getPlugin(),0,10);
                        String team = "not_used";
                        finalrankshowlength = i;
                        // 1st announcement effects
                        if(i==ArTeamManager.getTeamsRanking().size()){
                            p.spawnParticle(Particle.REDSTONE,p.getLocation().add(0,1,0),128,2,2,2,.5, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                            p.spawnParticle(Particle.SOUL_FIRE_FLAME,p.getLocation().add(0,1,0),128,2,2,2,.25);
                            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 2f,1);
                        }
                        // 2nd announcement effects
                        else if(i==ArTeamManager.getTeamsRanking().size()-1){
                            p.spawnParticle(Particle.REDSTONE,p.getLocation().add(0,1,0),128,2,2,2,.5, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f,1);
                            p.spawnParticle(Particle.END_ROD,p.getLocation().add(0,1,0),128,2,2,2,.1);
                        }
                        // 3rd announcement effects
                        else if(i==ArTeamManager.getTeamsRanking().size()-2){
                            p.spawnParticle(Particle.REDSTONE,p.getLocation().add(0,1,0),128,2,2,2,.5, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                            p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_DEATH, 2f,1);
                            p.spawnParticle(Particle.SPELL,p.getLocation().add(0,1,0),128,2,2,2,.25);
                        }
                        //other announcements effect
                        else {
                            p.spawnParticle(Particle.REDSTONE,p.getLocation().add(0,1,0),128,2,2,2,.5, new Particle.DustOptions(UsefulTools.stringToColorElseRandom(ArTeamManager.getTeamColor(team)),1));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 2f,1);
                        }
                        ArTeamDisplay.updatePlayerDisplay(p,team);
                        //p.showTitle(Title.title(Component.text(ChatColor.GOLD+"WINNER"),Component.text(team+" : "+ArTeamManager.getTeamPoints(team)).color(TextColor.fromHexString(ArTeamManager.getTeamColor(team)))));
                    } catch(IndexOutOfBoundsException e){
                        Bukkit.getLogger().log(Level.WARNING,"no team in ranking");
                    }
                }
                i++;
            }
        }.runTaskTimer(Main.getPlugin(),ranktitleduration/4,ranktitleduration* 20L);
    }
    
    
    
    
    
    
    
    
    private static void preparePlayerForGame(Player p){
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setLevel(0);
        p.setExp(0);
        p.setTotalExperience(0);
        p.setBedSpawnLocation(p.getLocation(),true);
        p.setCustomNameVisible(true);
        p.setInvulnerable(false);
        p.getOpenInventory().close();
        p.getInventory().clear();
    }
}
