package koh.game.fights.effects;

import koh.game.fights.Fighter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 6/7/16.
 */
public class EffectDamageBasedLifeLeft extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        final float effectBase = castInfos.randomJet(castInfos.caster) / 100f;
        final MutableInt damageValue = new MutableInt((castInfos.caster.getMaxLife() - castInfos.caster.getLife()) * effectBase);
        for (Fighter target : castInfos.targets) {
            if (EffectDamage.applyDamages(castInfos, target, damageValue) == -3) {
                return -3;
            }
        }
        return -1;
    }

}
