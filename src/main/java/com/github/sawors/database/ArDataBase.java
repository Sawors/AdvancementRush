package com.github.sawors.database;

import com.github.sawors.Main;
import com.github.sawors.advancements.AdvancementManager;
import com.github.sawors.discordbot.ArDBotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class ArDataBase {
    
    // |====================================[GIT GUD]=====================================|
    // |                     Reminder for the newbie I'm in SQL :                         |
    // | -> Set  : INSERT into [table]([column]) VALUES([value])                          |
    // | -> Get  : SELECT [column] FROM [table] // WHERE [condition]=[something]          |
    // | -> Edit : UPDATE [table] SET [column] = [value] // WHERE [condition]=[something] |
    // | -> Del  : DELETE FROM [table] WHERE [condition]=[something]                      |
    // |==================================================================================|
    
    
    //                 <team, advancementsname>
    private static HashMap<String, ArrayList<NamespacedKey>> advancementmutemap = new HashMap<>();
    private static HashSet<Advancement> nosynclist = new HashSet<>();
    
    public static void muteAdvancement(NamespacedKey advancement, String team){
        ArrayList<NamespacedKey> check = new ArrayList<>();
        if(advancementmutemap.containsKey(team)){
            check = advancementmutemap.get(team);
        }
        if(!check.contains(advancement)){
            check.add(advancement);
        }
        advancementmutemap.put(team, check);
    }
    public static void unmuteAdvancement(NamespacedKey advancement, String team){
        if(advancementmutemap.containsKey(team)){
            ArrayList<NamespacedKey> check = advancementmutemap.get(team);
            check.remove(advancement);
            advancementmutemap.put(team, check);
        }
    }
    public static boolean isAdvancementMuted(NamespacedKey advancement, String team){
        return advancementmutemap.containsKey(team) && advancementmutemap.get(team).contains(advancement);
    }
    
    public static void printMuteMap(){
        Main.logAdmin(ChatColor.RED+"Mute Map : \n"+advancementmutemap.toString());
    }
    
    public static void setNoSync(Advancement adv){
        nosynclist.add(adv);
    }
    public static boolean shouldSync(Advancement adv){
        return !nosynclist.contains(adv);
    }
    
    
    // TODO
    //  Making this async ?
    public static void initNoSyncList(){
        int ignored = 0;
        for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
            Advancement adv = it.next();
            if(getAdvancementValue(adv.getKey()) == 0 && !AdvancementManager.isRecipe(adv)){
                //Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Ignoring advancement "+adv.getKey()+" : value = 0 (or not referenced)");
                setNoSync(adv);
                ignored++;
            }
        }
        Bukkit.getLogger().log(Level.INFO, "[Advancement Rush] Ignoring "+ignored+" advancements : value = 0 (or not referenced)");
        
    }
    
    public static void connectInit(){
        try(Connection co = connect()){
            //  Init teams
            co.createStatement().execute(initTeamsTableQuery());
            
            //  Init advancements
            co.createStatement().execute(initAdvancementsTableQuery());
            co.createStatement().execute("DELETE FROM advancements;");
            co.createStatement().execute(initDBAdvancements());
            co.createStatement().execute(initGameTableQuery());
            co.createStatement().execute(initDiscordTableQuery());
            //co.createStatement().execute("INSERT INTO game(DATA,VALUE) VALUES "+"("+ArGameData.TIMER +",0), ("+ArGameData.EGG_HOLDER+",[])");
    
            ArDBotManager.setDiscordkey(Main.getMainConfig().getString("discord-link-key"));
            
            ConfigurationSection section = Main.getMainConfig().getConfigurationSection("advancements-values");
            if(section != null){
                Map<String,Object> map = section.getValues(false);
                HashMap<NamespacedKey, Integer> editadv = new HashMap<>();
                for(int i = 0; i<map.size();i++){
                    editadv.put(NamespacedKey.fromString(String.valueOf(map.keySet().toArray()[i])), Integer.valueOf(String.valueOf(map.values().toArray()[i])));
                }
                for(NamespacedKey key : editadv.keySet()){
                    Integer value = editadv.get(key);
                    String query = "REPLACE INTO advancements (name, value) VALUES ('"+key+"',"+value+");";
                    //String query = "INSERT INTO advancements(name,value) VALUES ('"+key.toString()+"', "+value+")"+" ON CONFLICT(name) DO UPDATE SET value="+value+";";
                    co.createStatement().execute(query);
                }
               
            }
        } catch (SQLException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    protected static Connection connect(){
        Connection co;
        try{
            String target = "jdbc:sqlite:"+Main.getDbFile().getCanonicalFile();
            co = DriverManager.getConnection(target);
            return co;
        } catch (
                IOException |
                SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean doesTeamExist(String teamname) throws SQLException {
        try(Connection co = connect()){
            PreparedStatement smtcheck = co.prepareStatement("SELECT COUNT(*) from teams WHERE "+ArTeamData.NAME+" = '"+teamname+"';");
            return smtcheck.executeQuery().getInt(1) != 0;
        }
    }
    // TEAMS
    //    |name|color|points|players|
    // ADVANCEMENTS
    //    |name|value|
    
    
    //        |=====================|
    //        |   TEAMS DATABASE    |
    //        |=====================|
    //                  ||
    //                  ||
    
    private static String initTeamsTableQuery(){
        return "CREATE TABLE IF NOT EXISTS teams ("
                +ArTeamData.NAME+" text NOT NULL UNIQUE,"
                +ArTeamData.COLOR+" text NOT NULL,"
                +ArTeamData.POINTS+" int NOT NULL,"
                +ArTeamData.PLAYERS+" text NOT NULL DEFAULT '[]',"
                +ArTeamData.ADVANCEMENTS+" text NOT NULL DEFAULT '[]'"
                +");";
    }
    
    public static void registerTeam(String name, String colorhex, int points, Set<UUID> members) throws KeyAlreadyExistsException{
        try(Connection co = connect()){
            String query = "INSERT INTO teams("+ArTeamData.NAME+","+ArTeamData.COLOR+","+ArTeamData.POINTS+","+ArTeamData.PLAYERS+") VALUES('"+name+"','"+colorhex+"',"+points+",'"+teamMembersSerialize(members)+"')";
            if(!doesTeamExist(name)){
                co.createStatement().execute(query);
            } else{
                throw new KeyAlreadyExistsException("this team is already registered");
            }
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    protected static void deleteTeam(String teamname) throws NullPointerException {
        try(Connection co = connect()){
            String query = "DELETE FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            if(doesTeamExist(teamname)){
                co.createStatement().execute(query);
            } else{
                throw new NullPointerException("sorry, there is no team with name "+teamname);
            }
        
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static String teamMembersSerialize(Set<UUID> members){
        StringBuilder msg = new StringBuilder();
        msg.append("[");
        List<UUID> mblist = new ArrayList<>(members);
        
        if(mblist.size()>=1){
            for(int i = 0; i<mblist.size(); i++){
                msg.append(mblist.get(i));
                if(i!=mblist.size()-1){
                   msg.append(",");
                }
            }
        }
        msg.append("]");
        return msg.toString();
    }
    
    public static Set<UUID> teamMembersDeserialize(String str) throws MalformedParametersException {
        char[] content = str.toCharArray();
        Set<UUID> list = new HashSet<>();
        if(content[0] == '[' && content[content.length-1] == ']'){
            Set<String> ids = new HashSet<>();
            StringBuilder uuid = new StringBuilder();
            for(int i = 1; i<content.length; i++){
                char evalchar = content[i];
                if(evalchar == ',' || evalchar == ']'){
                    ids.add(uuid.toString());
                    uuid = new StringBuilder();
                } else {
                    uuid.append(content[i]);
                }
            }
            try{
                for(String conv : ids){
                    list.add(UUID.fromString(conv));
                }
            } catch(IllegalArgumentException e){
                //e.printStackTrace();
            }
        } else{
            throw new MalformedParametersException("Can't recognize input as player list (missing \"[\" \"]\")");
        }
        return list;
    }
    
    // FORMAT :
    //      namespace:branch/advancement
    //      -> minecraft:end/elytra
    //
    //      namespace:branch/advancement(criteria)
    //      -> minecraft:adventure/adventuring_time(badlands)
    
    
    public static String advancementCriteriaSerialize(NamespacedKey adv, Collection<String> crits){
        StringBuilder output = new StringBuilder();
        output.append(adv.toString());
        output.append('(');
    
        for(String it : crits){
            output.append(it);
            output.append(',');
        }
        output.deleteCharAt(output.length()-1);
        output.append(')');
        return output.toString();
    }
    public static Set<String> advancementCriteriaDeserialize(String advancementwithcrits){
        char[] content = advancementwithcrits.toCharArray();
        Set<String> list = new HashSet<>();
        if(Character.isLetterOrDigit(content[0]) && content[content.length-1] == ')'){
            Set<String> crits = new HashSet<>();
            StringBuilder critunique = new StringBuilder();
            int criteriastart = 0;
            for(int i = 0; i<content.length; i++){
                char evalchar = content[i];
                if(evalchar == '('){
                    criteriastart = i+1;
                }
            }
            for(int i = criteriastart; i<content.length; i++){
                char evalchar = content[i];
                if(evalchar == ',' || evalchar == ')'){
                    crits.add(critunique.toString());
                    critunique = new StringBuilder();
                } else {
                    critunique.append(content[i]);
                }
            }
            try{
                list.addAll(crits);
            } catch(IllegalArgumentException e){
                //e.printStackTrace();
            }
        } else{
            throw new MalformedParametersException("Can't recognize input as criteria list (missing \"[\" \"]\")");
        }
        return list;
    }
    
    public static String teamAdvancementsSerialize(Set<String> adv){
        StringBuilder msg = new StringBuilder();
        msg.append("[");
        List<String> advlist = new ArrayList<>(adv);
        
        if(advlist.size()>=1){
            for(int i = 0; i<advlist.size(); i++){
                msg.append(advlist.get(i));
                //append the separator ","
                if(i!=advlist.size()-1){
                    msg.append(",");
                }
            }
        }
        msg.append("]");
        return msg.toString();
    }
    
    public static Set<String> teamAdvancementsDeserialize(String str) throws MalformedParametersException {
        char[] content = str.toCharArray();
        Set<String> list = new HashSet<>();
        if(content[0] == '[' && content[content.length-1] == ']'){
            Set<String> advs = new HashSet<>();
            StringBuilder advunique = new StringBuilder();
            for(int i = 1; i<content.length; i++){
                char evalchar = content[i];
                if((evalchar == ',' && content[i-1] == ')') || evalchar == ']'){
                    advs.add(advunique.toString());
                    advunique = new StringBuilder();
                } else {
                    advunique.append(content[i]);
                }
            }
            try{
                list.addAll(advs);
            } catch(IllegalArgumentException e){
                //e.printStackTrace();
            }
        } else{
            throw new MalformedParametersException("Can't recognize input as advancement list (missing \"[\" \"]\")");
        }
        return list;
    }
    
    
    //        |=====================|
    //        |    GAME DATABASE    |
    //        |=====================|
    //                  ||
    //                  ||
    private static String initGameTableQuery(){
        return "CREATE TABLE IF NOT EXISTS game (\n"
                + "	DATA text UNIQUE,"
                + "	VALUE text NOT NULL"
                + ");";
    }
    
    //        |=====================|
    //        |   DISCORD DATABASE  |
    //        |=====================|
    //                  ||
    //                  ||
    private static String initDiscordTableQuery(){
        return "CREATE TABLE IF NOT EXISTS 'discordlink' (\n"
                + "	'MCUUID' text UNIQUE,"
                + "	'DISCORDID' text UNIQUE"
                + ");";
    }
    public static void registerLink(UUID playerid, String discordid) throws KeyAlreadyExistsException{
        try(Connection co = connect()){
            String query = "INSERT INTO 'discordlink'('MCUUID','DISCORDID') VALUES('"+playerid.toString()+"','"+discordid+"')";
            co.createStatement().execute(query);
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static void deleteLink(String id) throws NullPointerException {
        try(Connection co = connect()){
            String category = "DISCORDID";
            if(id.contains("-")){
                category = "MCUUID";
            }
            String query = "DELETE FROM 'discordlink' WHERE '"+category+"'='"+id+"'";
            co.createStatement().execute(query);
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    protected static String getMinecraftPlayer(String discordid){
        try(Connection co = connect()){
            String query = "SELECT MCUUID FROM discordlink WHERE DISCORDID='"+discordid+"'";
            ResultSet rset = co.prepareStatement(query).executeQuery();
            if(!rset.isClosed()){
                return rset.getString("MCUUID");
            }
            return null;
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    protected static String getDiscordUser(String mcuuid){
        try(Connection co = connect()){
            String query = "SELECT DISCORDID FROM discordlink WHERE MCUUID='"+mcuuid+"'";
            ResultSet rset = co.prepareStatement(query).executeQuery();
            if(!rset.isClosed()){
                return rset.getString("DISCORDID");
            }else {
                return null;
            }
            
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    //        |=====================|
    //        |ADVANCEMENTS DATABASE|
    //        |=====================|
    //                  ||
    //                  ||
    
    private static String initAdvancementsTableQuery(){
        return "CREATE TABLE IF NOT EXISTS advancements (\n"
                + "	name text UNIQUE,\n"
                + "	value int NOT NULL\n"
                + ");";
    }
    
    public static int getAdvancementValue(NamespacedKey advancement){
        try(Connection co = connect()){
            PreparedStatement statement = co.prepareStatement("SELECT * FROM advancements WHERE name=?;");
            statement.setString(1,advancement.toString());
            //co.createStatement();
            return statement.executeQuery().getInt("value");
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
    }
    
    private static String initDBAdvancements(){
        // DEFAULT VALUES, DO NOT MODIFY HERE, USE THE CONFIG FOR THAT
        return  "INSERT INTO advancements(name,value) VALUES" +
                "('minecraft:story/mine_stone',5)," +
                "('minecraft:story/upgrade_tools',5)," +
                "('minecraft:story/smelt_iron',5)," +
                "('minecraft:story/obtain_armor',5)," +
                "('minecraft:story/deflect_arrow',10)," +
                "('minecraft:story/lava_bucket',5)," +
                "('minecraft:story/form_obsidian',15)," +
                "('minecraft:story/enter_the_nether',10)," +
                "('minecraft:story/cure_zombie_villager',125)," +
                "('minecraft:story/follow_ender_eye',25)," +
                "('minecraft:story/enter_the_end',25)," +
                "('minecraft:story/iron_tools',5)," +
                "('minecraft:story/mine_diamond',10)," +
                "('minecraft:story/shiny_gear',15)," +
                "('minecraft:story/enchant_item',15)," +
                "('minecraft:adventure/kill_a_mob',5)," +
                "('minecraft:adventure/shoot_arrow',5)," +
                "('minecraft:adventure/sniper_duel',50)," +
                "('minecraft:adventure/bullseye',100)," +
                "('minecraft:adventure/kill_all_mobs',650)," +
                "('minecraft:adventure/totem_of_undying',50)," +
                "('minecraft:adventure/throw_trident',75)," +
                "('minecraft:adventure/very_very_frightening',30)," +
                "('minecraft:adventure/trade',10)," +
                "('minecraft:adventure/summon_iron_golem',25)," +
                "('minecraft:adventure/trade_at_world_height',75)," +
                "('minecraft:adventure/sleep_in_bed',5)," +
                "('minecraft:adventure/adventuring_time',650)," +
                "('minecraft:adventure/walk_on_powder_snow_with_leather_boots',25)," +
                "('minecraft:adventure/play_jukebox_in_meadows',75)," +
                "('minecraft:adventure/ol_betsy',15)," +
                "('minecraft:adventure/whos_the_pillager_now',35)," +
                "('minecraft:adventure/arbalistic',100)," +
                "('minecraft:adventure/two_birds_one_arrow',75)," +
                "('minecraft:adventure/voluntary_exile',30)," +
                "('minecraft:adventure/hero_of_the_village',150)," +
                "('minecraft:adventure/honey_block_slide',35)," +
                "('minecraft:adventure/lightning_rod_with_villager_no_fire',20)," +
                "('minecraft:adventure/spyglass_at_parrot',15)," +
                "('minecraft:adventure/spyglass_at_ghast',10)," +
                "('minecraft:adventure/spyglass_at_dragon',20)," +
                "('minecraft:adventure/fall_from_world_height',75)," +
                "('minecraft:husbandry/breed_an_animal',5)," +
                "('minecraft:husbandry/bred_all_animals',150)," +
                "('minecraft:husbandry/tame_an_animal',15)," +
                "('minecraft:husbandry/complete_catalogue',75)," +
                "('minecraft:husbandry/plant_seed',5)," +
                "('minecraft:husbandry/balanced_diet',125)," +
                "('minecraft:husbandry/obtain_netherite_hoe',69)," +
                "('minecraft:husbandry/fishy_business',10)," +
                "('minecraft:husbandry/tactical_fishing',5)," +
                "('minecraft:husbandry/axolotl_in_a_bucket',15)," +
                "('minecraft:husbandry/kill_axolotl_target',20)," +
                "('minecraft:husbandry/safely_harvest_honey',15)," +
                "('minecraft:husbandry/wax_on',15)," +
                "('minecraft:husbandry/wax_off',10)," +
                "('minecraft:husbandry/silk_touch_nest',50)," +
                "('minecraft:husbandry/make_a_sign_glow',10)," +
                "('minecraft:husbandry/ride_a_boat_with_a_goat',15)," +
                "('minecraft:nether/fast_travel',20)," +
                "('minecraft:nether/find_fortress',15)," +
                "('minecraft:nether/obtain_blaze_rod',20)," +
                "('minecraft:nether/brew_potion',20)," +
                "('minecraft:nether/all_potions',450)," +
                "('minecraft:nether/all_effects',1250)," +
                "('minecraft:nether/get_wither_skull',75)," +
                "('minecraft:nether/summon_wither',400)," +
                "('minecraft:nether/create_beacon',150)," +
                "('minecraft:nether/create_full_beacon',450)," +
                "('minecraft:nether/return_to_sender',50)," +
                "('minecraft:nether/uneasy_alliance',200)," +
                "('minecraft:nether/obtain_ancient_debris',35)," +
                "('minecraft:nether/netherite_armor',150)," +
                "('minecraft:nether/use_lodestone',-5)," +
                "('minecraft:nether/obtain_crying_obsidian',15)," +
                "('minecraft:nether/charge_respawn_anchor',35)," +
                "('minecraft:nether/ride_strider',20)," +
                "('minecraft:nether/explore_nether',75)," +
                "('minecraft:nether/ride_strider_in_overworld_lava',50)," +
                "('minecraft:nether/find_bastion',75)," +
                "('minecraft:nether/loot_bastion',25)," +
                "('minecraft:nether/distract_piglin',15)," +
                "('minecraft:end/kill_dragon',400)," +
                "('minecraft:end/dragon_egg',300)," +
                "('minecraft:end/enter_end_gateway',20)," +
                "('minecraft:end/find_end_city',200)," +
                "('minecraft:end/elytra',350)," +
                "('minecraft:end/levitate',300)," +
                "('minecraft:end/respawn_dragon',100)," +
                "('minecraft:end/dragon_breath',30)" +
                ";";
    }
    
    
}
