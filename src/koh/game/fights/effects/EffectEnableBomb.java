package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.fighters.BombFighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectEnableBomb extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (Target instanceof BombFighter) {
                Target.TryDie(CastInfos.Caster.ID, true);
            }
        }
        return -1;
    }

}
