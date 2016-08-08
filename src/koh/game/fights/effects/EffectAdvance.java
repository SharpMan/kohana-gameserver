package koh.game.fights.effects;

import koh.game.entities.environments.Pathfunction;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.SpellIDEnum;

import java.util.Arrays;

/**
 * Created by Melancholia on 8/6/16.
 */
public class EffectAdvance extends EffectBase {

    public int applyEffect(EffectCast castInfos) {
        byte direction = 0;
        for (Fighter target : castInfos.targets.stream()/*.filter(tarrget ->
                !tarrget.getStates().hasState(FightStateEnum.CARRIED)
                        && (tarrget.getObjectType() != IFightObject.FightObjectType.OBJECT_STATIC)
                        && !tarrget.getStates().hasState(FightStateEnum.INÉBRANLABLE)
                        && !tarrget.getStates().hasState(FightStateEnum.ENRACINÉ)
                        && !tarrget.getStates().hasState(FightStateEnum.INDÉPLAÇABLE))*/
                .toArray(Fighter[]::new)) {

                    final Fighter pp = castInfos.caster;
                    castInfos.caster = target;
                    target = pp;
                    castInfos.targets.remove(0);
                    direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId());


            if (EffectPush.applyPush(castInfos, target, direction, castInfos.randomJet(target)) == -3) {
                return -3;
            }

        }
        return -1;
    }
}
