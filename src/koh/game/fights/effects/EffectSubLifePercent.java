package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;

/**
 * Created by Melancholia on 1/28/16.
 */
public class EffectSubLifePercent extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        int toReturn = -1;
        for (Fighter target : castInfos.targets) {
            if(castInfos.duration > 0){
                final EffectCast subInfos = new EffectCast(StatsEnum.SUB_VITALITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, castInfos.targets, castInfos.spellLevel);
                subInfos.damageValue = (int)(target.getLife() * castInfos.randomJet(target) / 100f);
                subInfos.duration = castInfos.duration;
                final BuffStats buffstats = new BuffStats(subInfos, target);
                if (buffstats.applyEffect(null, null) == -3) {
                    return -3;
                }
                target.getBuff().addBuff(buffstats);
                continue;
            }
            final int value = (int)(target.getLife() * castInfos.randomJet(target) / 100f);
            target.setLife(value);
            toReturn = target.tryDie(castInfos.caster.getID());
            if (toReturn != -1) {
                return toReturn;
            }
            target.getFight().sendToField(new GameActionFightLifePointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, castInfos.caster.getID(), target.getID(), value, 0));

        }
        return toReturn;
    }
}
