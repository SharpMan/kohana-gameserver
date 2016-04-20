package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDammageOcassioned;

/**
 * Created by Melancholia on 4/15/16.
 */
public class EffectSpellPower extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffDammageOcassioned(castInfos, Target));
        }
        return -1;
    }

}
