package koh.game.entities.actors;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import lombok.Builder;
import lombok.Setter;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Stream;

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

    public MonsterTemplate getMainCreatureTemplate(){
        return DAO.getMonsters().find(this.gameRolePlayGroupMonsterInformations.staticInfos.mainCreatureLightInfos.creatureGenericId);
    }


    public MonsterGrade getMainCreature(){
        return DAO.getMonsters().find(this.gameRolePlayGroupMonsterInformations.staticInfos.mainCreatureLightInfos.creatureGenericId).getGrade(this.gameRolePlayGroupMonsterInformations.staticInfos.mainCreatureLightInfos.grade);
    }

    public Stream<MonsterGrade> getMonsters(){
        return Arrays.stream(this.gameRolePlayGroupMonsterInformations.staticInfos.underlings)
                .map(mob -> DAO.getMonsters().find(mob.creatureGenericId).getGrade(mob.grade));
    }


    @Override
    protected EntityLook getEntityLook() {
        return this.gameRolePlayGroupMonsterInformations.look;
    }
}
