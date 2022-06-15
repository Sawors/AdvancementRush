package com.github.sawors;

import com.github.sawors.teams.ArTeam;
import com.github.sawors.teams.ArTeamData;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ArDataBase {
    
    // |====================================[GIT GUD]=====================================|
    // |                     Reminder for the newbie I'm in SQL :                         |
    // | -> Set  : INSERT into [table]([column]) VALUES([value])                          |
    // | -> Get  : SELECT [column] FROM [table] // WHERE [condition]=[something]          |
    // | -> Edit : UPDATE [table] SET [column] = [value] // WHERE [condition]=[something] |
    // | -> Del  : DELETE FROM [table] WHERE [condition]=[something]                      |
    // |==================================================================================|
    
    
    //                 <team, advancementsname>
    private static HashMap<String, ArrayList<String>> advancementmutemap = new HashMap<>();
    
    public static void muteAdvancement(String advancement, String team){
        ArrayList<String> check = new ArrayList<>();
        if(advancementmutemap.containsKey(team)){
            check = advancementmutemap.get(team);
        }
        if(!check.contains(advancement)){
            check.add(advancement);
        }
        advancementmutemap.put(team, check);
    }
    public static void unmuteAdvancement(String advancement, String team){
        if(advancementmutemap.containsKey(team)){
            ArrayList<String> check = advancementmutemap.get(team);
            check.remove(advancement);
            advancementmutemap.put(team, check);
        }
    }
    public static boolean isAdvancementMuted(String advancement, String team){
        return advancementmutemap.containsKey(team) && advancementmutemap.get(team).contains(advancement);
    }
    /*private static HashMap<UUID, String> playerteams = new HashMap<>();
    public static void setPlayerTeamLink(UUID playerid, String teamname){
        playerteams.put(playerid, teamname);
    }
    public static String getPlayerTeam(UUID playerid) throws MalformedParametersException, NullPointerException{
        if(playerteams.containsKey(playerid)){
            String teamname = playerteams.get(playerid);
            if(teamname.length() > 1){
                return teamname;
            } else {
                throw new NullPointerException("Player has no team");
            }
        } else {
            throw new MalformedParametersException("Player not found (severe error)");
        }
    }
    public static void printPlayerTeamLink(){
        Main.logAdmin(playerteams.toString());
    }*/
    
    
    public static void connectInit(){
        try(Connection co = connect()){
            //  Init teams
            co.createStatement().execute(initTeamsTableQuery());
            
            //  Init advancements
            co.createStatement().execute(initAdvancementsTableQuery());
            co.createStatement().execute("DELETE FROM advancements;");
            co.createStatement().execute(initDBAdvancements());
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Connection connect(){
        Connection co;
        try{
            String target = "jdbc:sqlite:"+Main.getDbFile().getCanonicalFile();
            co = DriverManager.getConnection(target);
            //Main.logAdmin("[AdvancementRush] Connection to database established : "+target);
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
        return "CREATE TABLE IF NOT EXISTS teams (\n"
                + "	"+ArTeamData.NAME+" text UNIQUE,\n"
                + "	"+ArTeamData.COLOR+" text NOT NULL,\n"
                + "	"+ArTeamData.POINTS+" int NOT NULL,\n"
                + "	"+ArTeamData.PLAYERS+" text NOT NULL\n"
                + ");";
    }
    
    public static void registerTeam(ArTeam team) throws KeyAlreadyExistsException{
        try(Connection co = connect()){
            String query = "INSERT INTO teams("+ArTeamData.NAME+","+ArTeamData.COLOR+","+ArTeamData.POINTS+","+ArTeamData.PLAYERS+") VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+",'"+teamMembersSerialize(team)+"')";
            if(!doesTeamExist(team.getName())){
                co.createStatement().execute(query);
            } else{
                throw new KeyAlreadyExistsException("this team is already registered");
            }
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void deleteTeam(String teamname) throws NullPointerException {
        try(Connection co = connect()){
            String query = "DELETE FROM teams WHERE "+ArTeamData.NAME+"='"+teamname+"'";
            Main.logAdmin(query);
            if(doesTeamExist(teamname)){
                co.createStatement().execute(query);
            } else{
                throw new NullPointerException("sorry, there is no team with name "+teamname);
            }
        
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static String teamMembersSerialize(ArrayList<UUID> members){
        StringBuilder msg = new StringBuilder();
        msg.append("[");
        
        if(members.size()>=1){
            for(int i = 0; i<members.size(); i++){
                msg.append(members.get(i));
                if(i!=members.size()-1){
                   msg.append(",");
                }
            }
        }
        msg.append("]");
        return msg.toString();
    }
    
    public static String teamMembersSerialize(ArTeam team){
        return teamMembersSerialize(team.getMembers());
    }
    
    public static ArrayList<UUID> teamMembersDeserialize(String str) throws MalformedParametersException {
        char[] content = str.toCharArray();
        ArrayList<UUID> list = new ArrayList<>();
        if(content[0] == '[' && content[content.length-1] == ']'){
            ArrayList<String> ids = new ArrayList<>();
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
    
    
    /*
            |=====================|
            |ADVANCEMENTS DATABASE|
            |=====================|
                      ||
                      ||
    */
    private static String initAdvancementsTableQuery(){
        return "CREATE TABLE IF NOT EXISTS advancements (\n"
                + "	name text UNIQUE,\n"
                + "	value int NOT NULL\n"
                + ");";
    }
    
    public static int getAdvancementValue(String advancement){
        try(Connection co = connect()){
            PreparedStatement statement = co.prepareStatement("SELECT * FROM advancements WHERE name=?;");
            statement.setString(1,advancement);
            //co.createStatement();
            return statement.executeQuery().getInt("value");
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
    }
    
    public static String initDBAdvancements(){
        // EDIT THIS TO ADD/MODIFY ADVANCEMENTS
        // TODO
        //  - Add a config file for this
        //  - Add a check for adv validity
        //  - Find a better way to edit adv (maybe set default values here and then allow for overwrite in config ? define them all directly in the config ?
        return  "INSERT INTO advancements(name,value) VALUES" +
                "('story/mine_stone',5)," +
                "('story/upgrade_tools',5)," +
                "('story/smelt_iron',5)," +
                "('story/obtain_armor',5)," +
                "('story/deflect_arrow',10)," +
                "('story/lava_bucket',5)," +
                "('story/form_obsidian',15)," +
                "('story/enter_the_nether',10)," +
                "('story/cure_zombie_villager',125)," +
                "('story/follow_ender_eye',25)," +
                "('story/enter_the_end',25)," +
                "('story/iron_tools',5)," +
                "('story/mine_diamond',10)," +
                "('story/shiny_gear',15)," +
                "('story/enchant_item',15)," +
                "('adventure/kill_a_mob',5)," +
                "('adventure/shoot_arrow',5)," +
                "('adventure/sniper_duel',50)," +
                "('adventure/bullseye',100)," +
                "('adventure/kill_all_mobs',650)," +
                "('adventure/totem_of_undying',50)," +
                "('adventure/throw_trident',75)," +
                "('adventure/very_very_frightening',30)," +
                "('adventure/trade',10)," +
                "('adventure/summon_iron_golem',25)," +
                "('adventure/trade_at_world_height',75)," +
                "('adventure/sleep_in_bed',5)," +
                "('adventure/adventuring_time',650)," +
                "('adventure/walk_on_powder_snow_with_leather_boots',25)," +
                "('adventure/play_jukebox_in_meadows',75)," +
                "('adventure/ol_betsy',15)," +
                "('adventure/whos_the_pillager_now',35)," +
                "('adventure/arbalistic',100)," +
                "('adventure/two_birds_one_arrow',75)," +
                "('adventure/voluntary_exile',30)," +
                "('adventure/hero_of_the_village',150)," +
                "('adventure/honey_block_slide',35)," +
                "('adventure/lightning_rod_with_villager_no_fire',20)," +
                "('adventure/spyglass_at_parrot',15)," +
                "('adventure/spyglass_at_ghast',10)," +
                "('adventure/spyglass_at_dragon',20)," +
                "('adventure/fall_from_world_height',75)," +
                "('husbandry/breed_an_animal',5)," +
                "('husbandry/bred_all_animals',150)," +
                "('husbandry/tame_an_animal',15)," +
                "('husbandry/complete_catalogue',75)," +
                "('husbandry/plant_seed',5)," +
                "('husbandry/balanced_diet',125)," +
                "('husbandry/obtain_netherite_hoe',69)," +
                "('husbandry/fishy_business',10)," +
                "('husbandry/tactical_fishing',5)," +
                "('husbandry/axolotl_in_a_bucket',15)," +
                "('husbandry/kill_axolotl_target',20)," +
                "('husbandry/safely_harvest_honey',15)," +
                "('husbandry/wax_on',15)," +
                "('husbandry/wax_off',10)," +
                "('husbandry/silk_touch_nest',50)," +
                "('husbandry/make_a_sign_glow',10)," +
                "('husbandry/ride_a_boat_with_a_goat',15)," +
                "('nether/fast_travel',20)," +
                "('nether/find_fortress',15)," +
                "('nether/obtain_blaze_rod',20)," +
                "('nether/brew_potion',20)," +
                "('nether/all_potions',450)," +
                "('nether/all_effects',1250)," +
                "('nether/get_wither_skull',75)," +
                "('nether/summon_wither',400)," +
                "('nether/create_beacon',150)," +
                "('nether/create_full_beacon',450)," +
                "('nether/return_to_sender',50)," +
                "('nether/uneasy_alliance',200)," +
                "('nether/obtain_ancient_debris',35)," +
                "('nether/netherite_armor',150)," +
                "('nether/use_lodestone',-5)," +
                "('nether/obtain_crying_obsidian',15)," +
                "('nether/charge_respawn_anchor',35)," +
                "('nether/ride_strider',20)," +
                "('nether/explore_nether',75)," +
                "('nether/ride_strider_in_overworld_lava',50)," +
                "('nether/find_bastion',75)," +
                "('nether/loot_bastion',25)," +
                "('nether/distract_piglin',15)," +
                "('end/kill_dragon',400)," +
                "('end/dragon_egg',300)," +
                "('end/enter_end_gateway',20)," +
                "('end/find_end_city',200)," +
                "('end/elytra',350)," +
                "('end/levitate',300)," +
                "('end/respawn_dragon',100)," +
                "('end/dragon_breath',30)" +
                ";";
    }
    
    
}
