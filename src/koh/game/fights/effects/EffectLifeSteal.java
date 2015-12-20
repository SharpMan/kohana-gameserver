package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffLifeSteal;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectLifeSteal extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            CastInfos.Targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffLifeSteal(CastInfos, Target));
            });
        } else {
            for (Fighter Target : CastInfos.Targets) {
                if (CastInfos.SpellId == 450 && Target.getTeam().id != CastInfos.caster.getTeam().id) { //Folie
                    continue;
                }

                if (ApplyLifeSteal(CastInfos, Target, new MutableInt(CastInfos.RandomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int ApplyLifeSteal(EffectCast CastInfos, Fighter Target, MutableInt DamageJet) {
        if (EffectDamage.ApplyDamages(CastInfos, Target, DamageJet) == -3) {
            return -3;
        }

        MutableInt HealJet = new MutableInt(DamageJet.intValue() / 2);

        if (EffectHeal.ApplyHeal(CastInfos, CastInfos.caster, HealJet) == -3) {
            return -3;
        }
        return -1;
    }

}
