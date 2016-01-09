package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import static koh.protocol.client.enums.StatsEnum.MAXIMIZE_EFFECTS;

/**
 *
 * @author Neo-Craft
 */
public class EffectAlterJet extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.duration > 0) {
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(castInfos.effectType == MAXIMIZE_EFFECTS ? new BuffMaximiseEffects(castInfos, Target) : new BuffMinimizeEffects(castInfos, Target));
            }
        }

        return -1;
    }

}
