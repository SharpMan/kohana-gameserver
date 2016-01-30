package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffHealDamageIncured;

/**
 * Created by Melancholia on 1/29/16.
 */
public class EffectHealDamageIncured extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffHealDamageIncured(castInfos, Target));
        }
        return -1;
    }
}
