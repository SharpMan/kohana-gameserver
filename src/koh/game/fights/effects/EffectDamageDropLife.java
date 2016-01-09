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
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(new BuffDamageDropLife(castInfos, Target));
            }
        } else // Dommage direct
        {
            int effectBase = castInfos.randomJet(castInfos.caster);
            MutableInt DamageValue = new MutableInt((castInfos.caster.currentLife / 100) * effectBase);
            if (EffectDamage.applyDamages(castInfos, castInfos.caster, DamageValue) == -3) {
                for (Fighter Target : castInfos.targets) {
                    if (Target.getID() == castInfos.caster.getID()) {
                        continue;
                    }
                    if (EffectHeal.applyHeal(castInfos, Target, DamageValue, false) == -3) {
                        return -3;
                    }
                }
                return -3;
            } else {
                for (Fighter Target : castInfos.targets) {
                    if (Target.getID() == castInfos.caster.getID()) {
                        continue;
                    }
                    if (EffectHeal.applyHeal(castInfos, Target, DamageValue, false) == -3) {
                        return -3;
                    }
                }
            }

        }

        return -1;
    }

}
