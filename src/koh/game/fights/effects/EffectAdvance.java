package koh.game.fights.effects;

import koh.game.entities.environments.Pathfunction;
import koh.game.fights.Fighter;

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
            if ((castInfos.effect.diceNum == 3356 && !Pathfunction.inLine(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId()))
                    || Pathfunction.goalDistance(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId()) == 1) {
                continue;
            }

            direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId());


            if (EffectPush.applyPush(castInfos, target, direction, castInfos.randomJet(target)) == -3) {
                return -3;
            }

        }
        return -1;
    }
}
