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
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.duration > 0) {
            // L'effet est un poison
            CastInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.targets) {
                Target.getBuff().addBuff(new BuffLifeDamage(CastInfos, Target));
            }
        } else // Dommage direct
        {
            for (Fighter Target : CastInfos.targets) {
                int effectBase = CastInfos.randomJet(Target);
                MutableInt DamageValue = new MutableInt((Target.currentLife / 100) * effectBase);
                //damageValue = (-damageValue);
                if (EffectDamage.applyDamages(CastInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
