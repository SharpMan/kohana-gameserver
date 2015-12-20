package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPMEsquive;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPMEsquive extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.Duration > 1) {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, CastInfos.Duration, 0);
                BuffSubPMEsquive Buff = new BuffSubPMEsquive(SubInfos, Target);
                Buff.applyEffect(null, null);
                Target.getBuff().addBuff(Buff);
            }
        } else {
            for (Fighter Target : CastInfos.Targets) {
                EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, (short) 0, 0, CastInfos.Effect, CastInfos.caster, null, false, StatsEnum.NONE, 0, CastInfos.SpellLevel, 0, 0);

                BuffSubPMEsquive Buff = new BuffSubPMEsquive(SubInfos, Target);
                Buff.applyEffect(null, null);

                Target.getBuff().addBuff(Buff);
            }
        }

        return -1;
    }

}
