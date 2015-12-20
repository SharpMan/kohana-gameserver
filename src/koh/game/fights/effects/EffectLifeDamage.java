package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffLifeDamage;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectLifeDamage extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.Targets) {
                Target.getBuff().addBuff(new BuffLifeDamage(CastInfos, Target));
            }
        } else // Dommage direct
        {
            for (Fighter Target : CastInfos.Targets) {
                int effectBase = CastInfos.RandomJet(Target);
                MutableInt DamageValue = new MutableInt((Target.currentLife / 100) * effectBase);
                //DamageValue = (-DamageValue);
                if (EffectDamage.ApplyDamages(CastInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
