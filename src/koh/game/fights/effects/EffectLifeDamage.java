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
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(new BuffLifeDamage(castInfos, Target));
            }
        } else // Dommage direct
        {
            for (Fighter Target : castInfos.targets) {
                int effectBase = castInfos.randomJet(Target);
                final MutableInt DamageValue = new MutableInt((Target.currentLife / 100) * effectBase);
                //damageValue = (-damageValue);
                if (EffectDamage.applyDamages(castInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
