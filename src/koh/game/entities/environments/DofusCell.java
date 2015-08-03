package koh.game.entities.environments;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusCell {

    public DofusMap Map;
    public short Id;
    public short Floor;
    public int LosMov = 3;
    public byte Speed;
    public int MapChangeData;
    public int MoveZone;

    private final Map<Integer, IGameActor> myActors = Collections.synchronizedMap(new HashMap<>());
    public DofusTrigger myAction = null;

    public DofusCell(DofusMap map, short id, IoBuffer buf) {
        this.Map = map;
        this.Id = id;
        this.Floor = buf.getShort();
        this.LosMov = buf.getUnsigned();
        this.Speed = buf.get();
        this.MapChangeData = buf.getUnsigned();
        this.MoveZone = buf.getUnsigned();
    }

    public void AddActor(IGameActor Actor) {
        this.myActors.put(Actor.ID, Actor);

        // on affecte la cell
        Actor.Cell = this;

        if (Actor instanceof Player && myAction != null) {
            ((Player) Actor).Client.onMouvementConfirm = myAction;
        }
    }

    public void DelActor(IGameActor Actor) {
        this.myActors.remove(Actor.ID);
    }
    
    public Collection<IGameActor> getActors(){
        return this.myActors.values();
    }
    
    public boolean hasActor(){
        return !this.myActors.isEmpty();
    }

    public DofusCell(DofusMap map, short id, short Floor, byte LosMov, byte Speed, int MapChangeData, int MoveZone) {
        this.Map = map;
        this.Id = id;
        this.Floor = Floor;
        this.LosMov = LosMov;
        this.Speed = Speed;
        this.MapChangeData = MapChangeData;
        this.MoveZone = MoveZone;
    }

    public boolean AffectMapChange() {
        return this.MapChangeData != 0;
    }

    public boolean Los() {
        return (this.LosMov & 2) >> 1 == 1;
    }

    public boolean Mov() {
        return (this.LosMov & 1) == 1 && !this.NonWalkableDuringFight() && !this.FarmCell();
    }

    public boolean Walakable() {
        return (this.LosMov & 1) == 1;
    }

    public boolean NonWalkableDuringFight() {
        return (this.LosMov & 4) >> 2 == 1;
    }

    public boolean NonWalkableDuringRP() {
        return (this.LosMov & 128) >> 7 == 1;
    }

    public boolean FarmCell() {
        return (this.LosMov & 32) >> 5 == 1;
    }

    public boolean Visible() {
        return (this.LosMov & 64) >> 6 == 1;
    }

    public boolean Red() {
        return (this.LosMov & 8) >> 3 == 1;
    }

    public boolean Blue() {
        return (this.LosMov & 16) >> 4 == 1;
    }

}
