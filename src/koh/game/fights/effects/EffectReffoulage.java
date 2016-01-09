package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffReffoulage;

/**
 *
 * @author Neo-Craft
 */
public class EffectReffoulage extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
       for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffReffoulage(castInfos, Target));
        }

        return -1;
    }

    
}
