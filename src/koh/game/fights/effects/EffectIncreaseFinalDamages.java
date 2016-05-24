package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffIncreaseFinalDamage;

/**
 * Created by Melancholia on 5/22/16.
 */
public class EffectIncreaseFinalDamages extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            target.getBuff().addBuff(new BuffIncreaseFinalDamage(castInfos, target));
        }
        return -1;
    }

}
