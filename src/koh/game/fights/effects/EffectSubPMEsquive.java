package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPMEsquive;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPMEsquive extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.Duration > 1) {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.Caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, CastInfos.Duration, 0);
                BuffSubPMEsquive Buff = new BuffSubPMEsquive(SubInfos, Target);
                Buff.ApplyEffect(null, null);
                Target.Buffs.AddBuff(Buff);
            }
        } else {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.Caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, 0, 0);

                BuffSubPMEsquive Buff = new BuffSubPMEsquive(SubInfos, Target);
                Buff.ApplyEffect(null, null);

                Target.Buffs.AddBuff(Buff);
            }
        }

        return -1;
    }

}
