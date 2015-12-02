package koh.game.entities.environments;

import koh.game.dao.DAO;

/**
 *
 * @author Neo-Craft
 */
public class DofusZaap {
    
    public int Mapid;
    public short Cell;
    public int SubArea;
    
    public DofusMap getMap(){
        return DAO.getMaps().findTemplate(Mapid);
    }
    
}
