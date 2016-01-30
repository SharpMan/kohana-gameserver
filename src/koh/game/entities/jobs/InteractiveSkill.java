package koh.game.entities.jobs;

import koh.utils.Enumerable;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveSkill {

    @Getter
    private int ID;
    @Getter
    private String type;
    @Getter
    private byte parentJobId;
    @Getter
    private boolean isForgemagus;
    @Getter
    private int[] modifiableItemTypeId,craftableItemIds;
    @Getter
    private int gatheredRessourceItem,interactiveId,elementActionId,cursor,levelMin;
    @Getter
    private String useAnimation; //TODO: use it in jobs
    @Getter
    private boolean isRepair,availableInHouse, clientDisplay;


    public InteractiveSkill(ResultSet result) throws SQLException {
        ID = result.getInt("id");
        type = result.getString("name");
        parentJobId = result.getByte("parent_job");
        isForgemagus = result.getBoolean("is_forgemagus");
        modifiableItemTypeId = Enumerable.stringToIntArray(result.getString("modifiable_item_ids"));
        gatheredRessourceItem = result.getInt("gathered_ressource_item");
        craftableItemIds = Enumerable.stringToIntArray(result.getString("craftable_item_ids"));
        interactiveId = result.getInt("interactive_id");
        useAnimation = result.getString("use_animation");
        elementActionId = result.getInt("element_action_id");
        isRepair = result.getBoolean("is_repair");
        cursor = result.getInt("cursor");
        availableInHouse = result.getBoolean("available_in_house");
        clientDisplay = result.getBoolean("client_display");
        levelMin = result.getInt("level_min");
    }
}
