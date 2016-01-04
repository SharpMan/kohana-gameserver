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
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            CastInfos.targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffLifeSteal(CastInfos, Target));
            });
        } else {
            for (Fighter Target : CastInfos.targets) {
                if (CastInfos.SpellId == 450 && Target.getTeam().id != CastInfos.caster.getTeam().id) { //Folie
                    continue;
                }

                if (applyLifeSteal(CastInfos, Target, new MutableInt(CastInfos.randomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int applyLifeSteal(EffectCast CastInfos, Fighter Target, MutableInt DamageJet) {
        //CastInfos.EffectType = StatsEnum.DamageBrut;

        if (EffectDamage.applyDamages(CastInfos, Target, DamageJet) == -3) {
            return -3;
        }

        MutableInt healJet = new MutableInt(DamageJet.intValue() / 2);

        if (EffectHeal.applyHeal(CastInfos, CastInfos.caster, healJet,false) == -3) {
            return -3;
        }
        return -1;
    }

}
