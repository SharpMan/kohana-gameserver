package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.fighters.DoubleFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.layers.FightPortal;
import koh.game.utils.Three;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

/**
 * Created by Melancholia on 1/27/16.
 */
public class EffectSummonDouble extends EffectBase {
    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.getFight().getCell(castInfos.cellId).hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            final Three<Integer, int[], Integer> portalQuery =  (castInfos.getFight().getTargetThroughPortal(castInfos.caster,
                    castInfos.cellId,
                    true,
                    castInfos.getFight().getCell(castInfos.cellId).getObjects().stream()
                            .filter(x -> x.getObjectType() == IFightObject.FightObjectType.OBJECT_PORTAL)
                            .findFirst()
                            .map(f -> (FightPortal) f)
                            .get().caster.getTeam()
            ));
            castInfos.cellId = portalQuery.first.shortValue();
        }
        if (castInfos.caster.getFight().isCellWalkable(castInfos.cellId)) {
            DoubleFighter summon = new DoubleFighter(castInfos.caster.getFight(),castInfos.caster.asPlayer());
            summon.joinFight();
            summon.getFight().joinFightTeam(summon, castInfos.caster.getTeam(), false, castInfos.cellId, true);
            castInfos.caster.getFight().sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_CHARACTER_ADD_DOUBLE, castInfos.caster.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
            castInfos.caster.getFight().getFightWorker().summonFighter(summon);
        }
        return -1;
    }
}
