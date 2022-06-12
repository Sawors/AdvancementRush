package com.github.sawors;

import com.github.sawors.teams.ArTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class DataBase {
    
    
    
    public static void connectInit(){
        try{
            Connection co = connect();
            
            //create advancement table if it does not exist yet
            
            //  Init teams
            co.createStatement().execute(initTeamsTableQuery());
            
            //  Init advancements
            co.createStatement().execute(initAdvancementsTableQuery());
            co.createStatement().execute("DELETE FROM advancements;");
            co.createStatement().execute(initDBAdvancements());
            co.close();
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
    
    private static String initAdvancementsTableQuery(){
        return "CREATE TABLE IF NOT EXISTS advancements (\n"
                + "	name text UNIQUE,\n"
                + "	value int NOT NULL\n"
                + ");";
    }
    private static String initTeamsTableQuery(){
        return "CREATE TABLE IF NOT EXISTS teams (\n"
                + "	name text UNIQUE,\n"
                + "	color text NOT NULL,\n"
                + "	points int NOT NULL,\n"
                + "	players text NOT NULL\n"
                + ");";
    }
    
    // TODO
    //  players in team are registered in the database, however the link player-team
    //  will have it's own hashmap as HashMap<Player player, String teamname>
    //  -> add player to database and sync hashmap in THE SAME METHOD !
    //  -> init hashmap based on database between each server reset (database persists, hashmap not)
    
    /*
    TEAMS
        |name|color|points|players|
        
    ADVANCEMENTS
        |name|value|
        
     */
    
    
    /*
            |=====================|
            |   TEAMS DATABASE    |
            |=====================|
                      ||
                      ||
    */
    public static void registerTeam(ArTeam team){
        try(Connection co = connect()){
            co.createStatement().execute("INSERT INTO teams(name,color,points,players) VALUES('"+team.getName()+"','"+team.getColorHex()+"',"+team.getPoints()+","+teamMembersSerialize(team)+")");
        } catch (SQLException e){
            e.printStackTrace();
        }
        
    }
    
    public static String teamMembersSerialize(ArTeam tm){
        StringBuilder msg = new StringBuilder();
        msg.append("[");
        ArrayList<Player> members = tm.getMembers();
        
        if(members.size()>=1){
            for(int i = 0; i<members.size(); i++){
                msg.append(members.get(i).getUniqueId());
                if(i!=members.size()-1){
                   msg.append(",");
                }
            }
        }
        msg.append("]");
        return msg.toString();
    }
    
    public static ArrayList<Player> teamMembersDeserialize(String str) throws MalformedParametersException {
        char[] content = str.toCharArray();
        ArrayList<Player> list = new ArrayList<>();
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
            Main.logAdmin("ids -> "+ids);
            for(String conv : ids){
                //Main.logAdmin("player -> "+conv);
                list.add(Bukkit.getPlayer(UUID.fromString(conv)));
            }
            //Main.logAdmin(list.toString());
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
    public static int getAdvancementValue(String advancement){
        try(Connection co = connect()){
            try(Statement statement = co.createStatement()){
                ResultSet rset = statement.executeQuery("SELECT * FROM advancements WHERE name='"+advancement+"';");
                return rset.getInt("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public static String initDBAdvancements(){
        // EDIT THIS TO ADD/MODIFY ADVANCEMENTS
        // TODO
        //  - Add a config file for this
        //  - Add a check for adv validity
        //  - Find a better way to edit adv (maybe set default values here and then allow for overwrite in config ? define them all in the config ?
        return  "INSERT INTO advancements(name,value) VALUES" +
                "('story/root',1)," +
                "('story/mine_stone',1)," +
                "('story/upgrade_tools',1)," +
                "('story/smelt_iron',1)," +
                "('story/obtain_armor',1)," +
                "('story/deflect_arrow',1)," +
                "('story/lava_bucket',10)," +
                "('story/form_obsidian',1)," +
                "('story/enter_the_nether',1)," +
                "('story/cure_zombie_villager',1)," +
                "('story/follow_ender_eye',1)," +
                "('story/enter_the_end',1)," +
                "('story/iron_tools',1)," +
                "('story/mine_diamond',1)," +
                "('story/shiny_gear',1)," +
                "('story/enchant_item',1)," +
                "('adventure/root',1)," +
                "('adventure/kill_a_mob',1)," +
                "('adventure/shoot_arrow',1)," +
                "('adventure/sniper_duel',1)," +
                "('adventure/bullseye',1)," +
                "('adventure/kill_all_mobs',1)," +
                "('adventure/totem_of_undying',1)," +
                "('adventure/throw_trident',1)," +
                "('adventure/very_very_frightening',1)," +
                "('adventure/trade',1)," +
                "('adventure/summon_iron_golem',1)," +
                "('adventure/trade_at_world_height',1)," +
                "('adventure/sleep_in_bed',1)," +
                "('adventure/adventuring_time',1)," +
                "('adventure/walk_on_powder_snow_with_leather_boots',1)," +
                "('adventure/play_jukebox_in_meadows',1)," +
                "('adventure/ol_betsy',1)," +
                "('adventure/whos_the_pillager_now',1)," +
                "('adventure/arbalistic',1)," +
                "('adventure/two_birds_one_arrow',1)," +
                "('adventure/voluntary_exile',1)," +
                "('adventure/hero_of_the_village',1)," +
                "('adventure/honey_block_slide',1)," +
                "('adventure/lightning_rod_with_villager_no_fire',1)," +
                "('adventure/spyglass_at_parrot',1)," +
                "('adventure/spyglass_at_ghast',1)," +
                "('adventure/spyglass_at_dragon',1)," +
                "('adventure/fall_from_world_height',1)," +
                "('adventure/kill_mob_near_sculk_catalyst',1)," +
                "('adventure/avoid_vibration',1)," +
                "('husbandry/root',1)," +
                "('husbandry/breed_an_animal',1)," +
                "('husbandry/bred_all_animals',1)," +
                "('husbandry/tame_an_animal',1)," +
                "('husbandry/complete_catalogue',1)," +
                "('husbandry/plant_seed',1)," +
                "('husbandry/balanced_diet',1)," +
                "('husbandry/obtain_netherite_hoe',1)," +
                "('husbandry/fishy_business',1)," +
                "('husbandry/tactical_fishing',1)," +
                "('husbandry/axolotl_in_a_bucket',1)," +
                "('husbandry/kill_axolotl_target',1)," +
                "('husbandry/safely_harvest_honey',1)," +
                "('husbandry/wax_on',1)," +
                "('husbandry/wax_off',1)," +
                "('husbandry/silk_touch_nest',1)," +
                "('husbandry/make_a_sign_glow',1)," +
                "('husbandry/ride_a_boat_with_a_goat',1)," +
                "('husbandry/tadpole_in_a_bucket',1)," +
                "('husbandry/leash_all_frog_variants',1)," +
                "('husbandry/allay_deliver_item_to_player',1)," +
                "('husbandry/allay_deliver_cake_to_note_block',1)," +
                "('nether/root',1)," +
                "('nether/fast_travel',1)," +
                "('nether/find_fortress',1)," +
                "('nether/obtain_blaze_rod',1)," +
                "('nether/brew_potion',1)," +
                "('nether/all_potions',1)," +
                "('nether/all_effects',1)," +
                "('nether/get_wither_skull',1)," +
                "('nether/summon_wither',1)," +
                "('nether/create_beacon',1)," +
                "('nether/create_full_beacon',1)," +
                "('nether/return_to_sender',1)," +
                "('nether/uneasy_alliance',1)," +
                "('nether/obtain_ancient_debris',1)," +
                "('nether/netherite_armor',1)," +
                "('nether/use_lodestone',1)," +
                "('nether/obtain_crying_obsidian',1)," +
                "('nether/charge_respawn_anchor',1)," +
                "('nether/ride_strider',1)," +
                "('nether/explore_nether',1)," +
                "('nether/ride_strider_in_overworld_lava',1)," +
                "('nether/find_bastion',1)," +
                "('nether/loot_bastion',1)," +
                "('nether/distract_piglin',1)," +
                "('end/root',1)," +
                "('end/kill_dragon',1)," +
                "('end/dragon_egg',1)," +
                "('end/enter_end_gateway',1)," +
                "('end/find_end_city',1)," +
                "('end/elytra',1)," +
                "('end/levitate',1)," +
                "('end/respawn_dragon',1)," +
                "('end/dragon_breath',1)" +
                ";";
    }
    
    
}
