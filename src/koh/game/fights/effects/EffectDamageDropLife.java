package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamageDropLife;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamageDropLife extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.Targets) {
                Target.getBuff().addBuff(new BuffDamageDropLife(CastInfos, Target));
            }
        } else // Dommage direct
        {
            int effectBase = CastInfos.randomJet(CastInfos.caster);
            MutableInt DamageValue = new MutableInt((CastInfos.caster.currentLife / 100) * effectBase);
            if (EffectDamage.ApplyDamages(CastInfos, CastInfos.caster, DamageValue) == -3) {
                for (Fighter Target : CastInfos.Targets) {
                    if (Target.getID() == CastInfos.caster.getID()) {
                        continue;
                    }
                    if (EffectHeal.ApplyHeal(CastInfos, Target, DamageValue, false) == -3) {
                        return -3;
                    }
                }
                return -3;
            } else {
                for (Fighter Target : CastInfos.Targets) {
                    if (Target.getID() == CastInfos.caster.getID()) {
                        continue;
                    }
                    if (EffectHeal.ApplyHeal(CastInfos, Target, DamageValue, false) == -3) {
                        return -3;
                    }
                }
            }

        }

        return -1;
    }

}
