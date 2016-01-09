package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffIncreaseSpellJet;

/**
 *
 * @author Neo-Craft
 */
public class EffectIncreaseSpellJet extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.caster == null) {
            return -1;
        }
        castInfos.caster.getBuff().addBuff(new BuffIncreaseSpellJet(castInfos, castInfos.caster));

        return -1;
    }

}
