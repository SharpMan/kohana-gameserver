package koh.game.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import koh.game.MySQL;
import koh.game.entities.jobs.*;
import koh.game.utils.Settings;
import koh.utils.Enumerable;

/**
 *
 * @author Neo-Craft
 */
public class JobDAO {
    
    public static HashMap<Integer, JobGatheringInfos> GatheringJobs = new HashMap<>();
    public static HashMap<Integer, InteractiveSkill> Skills = new HashMap<>();
    
    public static int FindAllGatheringInfos() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from job_gathering", Settings.GetStringElement("Database.Name"), 0);
            
            while (RS.next()) {
                GatheringJobs.put(RS.getInt("start_level"), new JobGatheringInfos() {
                    {
                        this.GatheringByLevel.put(0, this.toCouple(RS.getString("level0")));
                        this.GatheringByLevel.put(20, this.toCouple(RS.getString("level20")));
                        this.GatheringByLevel.put(40, this.toCouple(RS.getString("level40")));
                        this.GatheringByLevel.put(60, this.toCouple(RS.getString("level60")));
                        this.GatheringByLevel.put(80, this.toCouple(RS.getString("level80")));
                        this.GatheringByLevel.put(100, this.toCouple(RS.getString("level100")));
                        this.GatheringByLevel.put(120, this.toCouple(RS.getString("level120")));
                        this.GatheringByLevel.put(140, this.toCouple(RS.getString("level140")));
                        this.GatheringByLevel.put(160, this.toCouple(RS.getString("level160")));
                        this.GatheringByLevel.put(180, this.toCouple(RS.getString("level180")));
                        this.GatheringByLevel.put(200, this.toCouple(RS.getString("level200")));
                        this.bonusMin = Integer.parseInt(RS.getString("bonus").split("-")[0]);
                        this.bonusMax = Integer.parseInt(RS.getString("bonus").split("-")[1]);
                        this.xpEarned = RS.getInt("xp_earned");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public static int FindAllSkills() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_interactive_skills", Settings.GetStringElement("Database.Name"), 0);
            
            while (RS.next()) {
                Skills.put(RS.getInt("id"), new InteractiveSkill() {
                    {
                        ID = RS.getInt("id");
                        Type = RS.getString("name");
                        parentJobId = RS.getByte("parent_job");
                        isForgemagus = RS.getBoolean("is_forgemagus");
                        modifiableItemTypeId = Enumerable.StringToIntArray(RS.getString("modifiable_item_ids"));
                        gatheredRessourceItem = RS.getInt("gathered_ressource_item");
                        craftableItemIds = Enumerable.StringToIntArray(RS.getString("craftable_item_ids"));
                        interactiveId = RS.getInt("interactive_id");
                        useAnimation = RS.getString("use_animation");
                        elementActionId = RS.getInt("element_action_id");
                        isRepair = RS.getBoolean("is_repair");
                        cursor = RS.getInt("cursor");
                        availableInHouse = RS.getBoolean("available_in_house");
                        clientDisplay = RS.getBoolean("client_display");
                        levelMin = RS.getInt("level_min");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
}
