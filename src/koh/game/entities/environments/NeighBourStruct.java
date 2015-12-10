package koh.game.entities.environments;

import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
 public class NeighBourStruct {

    @Getter
    private int mapid;
    @Getter
    private int cellid;

    public NeighBourStruct(int Mapid, int Cellid) {
        this.mapid = Mapid;
        this.cellid = Cellid;
    }

    public NeighBourStruct(String[] infos) {
        this.mapid = Integer.parseInt(infos[0]);
        this.cellid = Integer.parseInt(infos[1]);
    }
}

