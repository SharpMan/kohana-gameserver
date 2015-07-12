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
    public int ApplyEffect(EffectCast CastInfos) {
        MutableInt DamageValue = new MutableInt();
        if (CastInfos.Duration > 1) {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.Caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, CastInfos.Duration, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.ApplyEffect(DamageValue, null);
                Target.Buffs.AddBuff(Buff);
            }
        } else {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.Caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, 0, 0);

                BuffSubPAEsquive Buff = new BuffSubPAEsquive(SubInfos, Target);
                Buff.ApplyEffect(DamageValue, null);

                Target.Buffs.AddBuff(Buff);
            }
        }

        return -1;
    }

}
