package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffIncreaseSpellJet;

/**
 *
 * @author Neo-Craft
 */
public class EffectIncreaseSpellJet extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.Caster == null) {
            return -1;
        }
        CastInfos.Caster.buff.addBuff(new BuffIncreaseSpellJet(CastInfos, CastInfos.Caster));

        return -1;
    }

}
