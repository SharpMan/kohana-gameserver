package koh.game.entities.environments;

/**
 *
 * @author Neo-Craft
 */
 public class NeighBourStruct {

    public int mapid;
    public int cellid;

    public NeighBourStruct(int Mapid, int Cellid) {
        this.mapid = Mapid;
        this.cellid = Cellid;
    }

    public NeighBourStruct(String[] infos) {
        this.mapid = Integer.parseInt(infos[0]);
        this.cellid = Integer.parseInt(infos[1]);
    }
}

