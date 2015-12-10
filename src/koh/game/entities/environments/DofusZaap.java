package koh.game.entities.environments;

import koh.game.dao.DAO;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class DofusZaap {

    @Getter
    private int mapid;
    @Getter
    private short cell;
    @Getter
    private int subArea;

    public DofusZaap(ResultSet result) throws SQLException {
        this.mapid = result.getInt("mapid");
        this.cell = result.getShort("cell");
        this.subArea = result.getInt("subarea");
    }

    public DofusMap getMap(){
        return DAO.getMaps().findTemplate(mapid);
    }
    
}
