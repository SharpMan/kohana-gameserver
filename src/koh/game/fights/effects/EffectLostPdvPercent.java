package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostPdvPercent extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            EffectCast subInfos = new EffectCast(StatsEnum.SUB_VITALITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            subInfos.damageValue = (int) (((double) Target.getLife() / 100) * castInfos.randomJet(Target));
            subInfos.duration = castInfos.duration;
            BuffStats buffStats = new BuffStats(subInfos, Target);
            if (buffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(buffStats);
        }

        return -1;
    }

}
