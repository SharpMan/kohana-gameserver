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
            EffectCast SubInfos = new EffectCast(StatsEnum.VITALITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            SubInfos.damageValue = (int) (((double) Target.getLife() / 100) * castInfos.randomJet(Target));
            SubInfos.duration = castInfos.duration;
            BuffStats BuffStats = new BuffStats(SubInfos, Target);
            if (BuffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(BuffStats);
        }

        return -1;
    }

}
