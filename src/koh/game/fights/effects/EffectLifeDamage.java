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
            for (Fighter target : castInfos.targets) {
                int effectBase = castInfos.randomJet(target);
                final MutableInt damageValue = new MutableInt((castInfos.caster.currentLife / 100) * effectBase);
                //damageValue = (-damageValue);
                if (EffectDamage.applyDamages(castInfos, target, damageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
