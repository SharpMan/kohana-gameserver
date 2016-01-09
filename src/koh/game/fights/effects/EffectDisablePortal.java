package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layers.FightPortal;

/**
 *
 * @author Neo-Craft
 */
public class EffectDisablePortal extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.getCell().hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightPortal) (castInfos.getCell().getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).disable(castInfos.caster);
        }
        return -1;
    }

}
