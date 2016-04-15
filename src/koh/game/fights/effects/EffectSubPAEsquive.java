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
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);
                subInfos.glyphId = castInfos.glyphId;
                final BuffSubPAEsquive buff = new BuffSubPAEsquive(subInfos, Target);
                buff.applyEffect(DamageValue, null);
                Target.getBuff().addBuff(buff);
            }
        } else {
            for (Fighter Target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, 0, 0);
                subInfos.glyphId = castInfos.glyphId;
                BuffSubPAEsquive Buff = new BuffSubPAEsquive(subInfos, Target);
                Buff.applyEffect(DamageValue, null);

                Target.getBuff().addBuff(Buff);
            }
        }

        return -1;
    }

}
