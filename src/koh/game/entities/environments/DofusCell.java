package koh.game.entities.environments;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import lombok.Getter;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusCell {

    @Getter
    private  DofusMap map;
    @Getter
    private  short id;
    @Getter
    private  short floor;
    public int losMov = 3;
    @Getter
    private  byte speed;
    public int mapChangeData;
    @Getter
    private int moveZone;

    private final Map<Integer, IGameActor> myActors = Collections.synchronizedMap(new HashMap<>());
    public DofusTrigger myAction = null;

    public DofusCell(DofusMap map, short id, IoBuffer buf) {
        this.map = map;
        this.id = id;
        this.floor = buf.getShort();
        this.losMov = buf.getUnsigned();
        this.speed = buf.get();
        this.mapChangeData = buf.getUnsigned();
        this.moveZone = buf.getUnsigned();
    }

    public void addActor(IGameActor actor) {
        this.myActors.put(actor.getID(), actor);

        // on affecte la cell
        actor.setActorCell(this);

        if (actor instanceof Player && myAction != null) {
            ((Player) actor).getClient().onMouvementConfirm = myAction;
        }
    }

    public void delActor(IGameActor actor) {
        this.myActors.remove(actor.getID());
    }
    
    public Collection<IGameActor> getActors(){
        return this.myActors.values();
    }
    
    public boolean hasActor(){
        return !this.myActors.isEmpty();
    }

    public DofusCell(DofusMap map, short id, short Floor, byte LosMov, byte Speed, int MapChangeData, int MoveZone) {
        this.map = map;
        this.id = id;
        this.floor = Floor;
        this.losMov = LosMov;
        this.speed = Speed;
        this.mapChangeData = MapChangeData;
        this.moveZone = MoveZone;
    }

    public boolean affectMapChange() {
        return this.mapChangeData != 0;
    }

    public boolean los() {
        return (this.losMov & 2) >> 1 == 1;
    }

    public boolean mov() {
        return (this.losMov & 1) == 1 && !this.nonWalkableDuringFight() && !this.farmCell();
    }

    public boolean walakable() {
        return (this.losMov & 1) == 1;
    }

    public boolean nonWalkableDuringFight() {
        return (this.losMov & 4) >> 2 == 1;
    }

    public boolean nonWalkableDuringRP() {
        return (this.losMov & 128) >> 7 == 1;
    }

    public boolean farmCell() {
        return (this.losMov & 32) >> 5 == 1;
    }

    public boolean visible() {
        return (this.losMov & 64) >> 6 == 1;
    }

    public boolean red() {
        return (this.losMov & 8) >> 3 == 1;
    }

    public boolean blue() {
        return (this.losMov & 16) >> 4 == 1;
    }

}
