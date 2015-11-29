package koh.game.entities.actors;

import koh.game.dao.mysql.MapDAOImpl;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.DofusMap;
import koh.protocol.client.Message;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayActorInformations;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public abstract class IGameActor {

    public int ID;

    public volatile DofusCell Cell;

    public int Mapid;

    public EntityLook entityLook;

    public abstract EntityLook GetEntityLook();

    public byte Direction = 1;

    public DofusMap getDofusMap() {
        return MapDAOImpl.dofusMaps.get(this.Mapid);
    }

    public GameContextActorInformations GetGameContextActorInformations(Player character) {
        return new GameRolePlayActorInformations(this.ID, this.GetEntityLook(), this.GetEntityDispositionInformations(character));
    }

    public boolean CanBeSee(IGameActor Actor) {
        //Todo: Player Invisibile ?
        return true;
    }

    public void Send(Message Packet) {
        if (this instanceof Player) {
            ((Player) this).Send(Packet);
        }
    }

    public EntityDispositionInformations GetEntityDispositionInformations(Player character) {
        return new EntityDispositionInformations(this.Cell.Id, Direction);
    }

    //public DirectionsEnum Direction;
}
