package koh.game.entities.environments;

import koh.game.dao.mysql.MapDAO;

/**
 *
 * @author Neo-Craft
 */
public class DofusZaap {
    
    public int Mapid;
    public short Cell;
    public int SubArea;
    
    public DofusMap Map(){
        return MapDAO.Cache.get(Mapid);
    }
    
}
