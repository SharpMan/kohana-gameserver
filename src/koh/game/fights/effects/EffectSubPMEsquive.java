package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.game.fights.effects.buff.BuffSubPMEsquive;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPMEsquive extends EffectBase {




    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.duration >= 1) {
            for (Fighter target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);
                final BuffSubPMEsquive buff = new BuffSubPMEsquive(subInfos, target);
                if (!target.getBuff().buffMaxStackReached(subInfos)) {
                    buff.applyEffect(null, null);
                    target.getBuff().addBuff(buff);
                }
            }
        } else {
            for (Fighter target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, 0, 0);

                final BuffSubPMEsquive buff = new BuffSubPMEsquive(subInfos, target);

                if (!target.getBuff().buffMaxStackReached(subInfos)) {
                    buff.applyEffect(null, null);
                    target.getBuff().addBuff(buff);
                }
            }
        }

        return -1;
    }

}
