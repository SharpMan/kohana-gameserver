package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEndTurn;

/**
 *
 * @author Melancholia
 */
public class EffectEndTurn extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffEndTurn(castInfos, Target));
        }

        return -1;
    }

}