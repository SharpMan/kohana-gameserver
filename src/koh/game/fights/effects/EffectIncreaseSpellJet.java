package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffIncreaseSpellJet;

/**
 *
 * @author Neo-Craft
 */
public class EffectIncreaseSpellJet extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.caster == null) {
            return -1;
        }
        CastInfos.caster.getBuff().addBuff(new BuffIncreaseSpellJet(CastInfos, CastInfos.caster));

        return -1;
    }

}
