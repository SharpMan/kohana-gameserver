package koh.game.entities.actors;

import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import lombok.Builder;
import lombok.Setter;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;

/**
 * Created by Melancholia on 12/28/15.
 */
@Builder
public class MonsterGroup extends IGameActor {

    @Getter
    private GameRolePlayGroupMonsterInformations gameRolePlayGroupMonsterInformations;

    @Getter @Setter private boolean fix;
    @Getter @Setter private short fixedCell;

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return this.gameRolePlayGroupMonsterInformations;
    }




    @Override
    protected EntityLook getEntityLook() {
        return this.gameRolePlayGroupMonsterInformations.look;
    }
}
