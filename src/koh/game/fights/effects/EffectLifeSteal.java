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
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            castInfos.targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffLifeSteal(castInfos, Target));
            });
        } else {
            for (Fighter Target : castInfos.targets) {
                if (castInfos.spellId == 450 && Target.getTeam().id != castInfos.caster.getTeam().id) { //Folie
                    continue;
                }

                if (applyLifeSteal(castInfos, Target, new MutableInt(castInfos.randomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int applyLifeSteal(EffectCast CastInfos, Fighter Target, MutableInt DamageJet) {
        //castInfos.effectType = StatsEnum.DAMAGE_BRUT;

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
