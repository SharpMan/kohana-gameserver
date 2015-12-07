package koh.game.entities.environments;

import koh.protocol.messages.game.prism.PrismsListMessage;
import koh.protocol.types.game.prism.PrismSubareaEmptyInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Neo-Craft
 */
@Builder
public class SubArea {

    @Getter
    private int id;
    //public String nameId;
    @Getter
    private Area area;
    @Getter
    private int[] mapIds, shape, customWorldMaptype;
    @Getter
    private int packId, level;
    @Getter
    private boolean isConquestVillage, basicAccountAllowed, displayOnWorldMap;
    @Getter
    private int[] monsters, entranceMapIds, exitMapIds;
    @Getter
    private boolean capturable;


    public static PrismsListMessage getPrismMessage() {
        return new PrismsListMessage(/*AreaDAOImpl.subAreas.values().stream().map(x -> new PrismSubareaEmptyInfo(x.id,1191)).toArray(PrismSubareaEmptyInfo[]::new)*/new PrismSubareaEmptyInfo[0]);
    }

}
