package koh.game.fights.effects;

import koh.game.fights.fighters.DoubleFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

/**
 * Created by Melancholia on 1/27/16.
 */
public class EffectSummonDouble extends EffectBase {
    @Override
    public int applyEffect(EffectCast castInfos) {
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
