package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPAEsquive;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPAEsquive extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        MutableInt DamageValue = new MutableInt();
        if (castInfos.duration > 1) {
            for (Fighter Target : castInfos.targets) {
                EffectCast SubInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.applyEffect(DamageValue, null);
                Target.getBuff().addBuff(Buff);
            }
        } else {
            for (Fighter Target : castInfos.targets) {
                EffectCast SubInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, 0, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.applyEffect(DamageValue, null);

                Target.getBuff().addBuff(Buff);
            }
        }

        return -1;
    }

}
