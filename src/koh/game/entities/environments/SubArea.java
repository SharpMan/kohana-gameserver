package koh.game.entities.environments;

import koh.protocol.messages.game.prism.PrismsListMessage;
import koh.protocol.types.game.prism.PrismSubareaEmptyInfo;

/**
 *
 * @author Neo-Craft
 */
public class SubArea {

    public int Id;
    //public String nameId;
    public Area area;
    public int[] mapIds;
    public int[] shape, customWorldMaptype;
    public int packId, level;
    public boolean isConquestVillage, basicAccountAllowed, displayOnWorldMap;
    public int[] monsters, entranceMapIds, exitMapIds;
    public boolean capturable;

    
    public static PrismsListMessage PrismMessage(){
        return new PrismsListMessage(/*AreaDAOImpl.SubAreas.values().stream().map(x -> new PrismSubareaEmptyInfo(x.Id,1191)).toArray(PrismSubareaEmptyInfo[]::new)*/new PrismSubareaEmptyInfo[0]);
    }
    
}
