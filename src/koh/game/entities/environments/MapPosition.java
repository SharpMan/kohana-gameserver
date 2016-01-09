package koh.game.entities.environments;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MapPosition {

    @Getter
    private int id;
    @Getter
    private short posX, posY;
    private boolean outdoor;
    private int capabilities;
    private String nameId;
    private boolean showNameOnFingerpost;
    @Getter
    private int subAreaId, worldMap;
    private boolean hasPriorityOnWorldmap;
    
    public MapPosition(){
        
    }


    public MapPosition(ResultSet result) throws SQLException {
        id = result.getInt("id");
        posX = (short) result.getInt("posX");
        posY = (short) result.getInt("posY");
        outdoor = result.getBoolean("outdoor");
        nameId = result.getString("name");
        showNameOnFingerpost = result.getBoolean("show_name_on_finger_post");
        subAreaId = result.getInt("subarea_id");
        worldMap = result.getInt("wold_map");
        hasPriorityOnWorldmap = result.getBoolean("has_priority_on_world_map");
    }
}
