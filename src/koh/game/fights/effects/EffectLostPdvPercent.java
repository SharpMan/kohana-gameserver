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
        for (Fighter target : castInfos.targets) {
            final EffectCast subInfos = new EffectCast(StatsEnum.SUB_VITALITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            subInfos.damageValue = (int) (((double) target.getLife() / 100) * castInfos.randomJet(target));
            subInfos.duration = castInfos.duration;
            final BuffStats buffStats = new BuffStats(subInfos, target);
            if (buffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            target.getBuff().addBuff(buffStats);
        }

        return -1;
    }

}
