package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffTpFirstPos;

/**
 *
 * @author Melancholia
 */
public class EffectTpFirstPos extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffTpFirstPos(castInfos, Target));
        }

        return -1;
    }
}
