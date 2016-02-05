package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectVitalityPercent extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            final EffectCast subInfos = new EffectCast(StatsEnum.VITALITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            subInfos.damageValue = (int) (((double) Target.getMaxLife() / 100) * castInfos.randomJet(Target));
            subInfos.duration = castInfos.duration;
            final BuffStats buffstats = new BuffStats(subInfos, Target);
            if (buffstats.applyEffect(null, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(buffstats);
        }

        return -1;
    }

}
