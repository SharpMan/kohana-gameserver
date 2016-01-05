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
    public int applyEffect(EffectCast CastInfos) {
        MutableInt DamageValue = new MutableInt();
        if (CastInfos.duration > 1) {
            for (Fighter Target : CastInfos.targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.effectType, CastInfos.spellId, (short) 0, 0, CastInfos.effect, CastInfos.caster, null, false, StatsEnum.NONE, 0, CastInfos.spellLevel, CastInfos.duration, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.applyEffect(DamageValue, null);
                Target.getBuff().addBuff(Buff);
            }
        } else {
            for (Fighter Target : CastInfos.targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.effectType, CastInfos.spellId, (short) 0, 0, CastInfos.effect, CastInfos.caster, null, false, StatsEnum.NONE, 0, CastInfos.spellLevel, 0, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.applyEffect(DamageValue, null);

                Target.getBuff().addBuff(Buff);
            }
        }

        return -1;
    }

}
