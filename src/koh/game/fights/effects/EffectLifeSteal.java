package koh.game.fights.effects;

import koh.game.fights.Fighter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectLifeSteal extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (CastInfos.SpellId == 450 && Target.Team.Id != CastInfos.Caster.Team.Id) { //Folie
                continue;
            }
            MutableInt DamageJet = new MutableInt(CastInfos.RandomJet(Target));

            if (EffectDamage.ApplyDamages(CastInfos, Target, DamageJet) == -3) {
                return -3;
            }

            MutableInt HealJet = new MutableInt(DamageJet.intValue() / 2);

            if (EffectHeal.ApplyHeal(CastInfos, CastInfos.Caster, HealJet) == -3) {
                return -3;
            }
        }

        return -1;
    }

}
