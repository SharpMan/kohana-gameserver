package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffWeaponDamageBonus;

/**
 * Created by Melancholia on 4/14/16.
 */
public class EffectWeaponDamageBonus extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffWeaponDamageBonus(castInfos, Target));
        }
        return -1;
    }

}