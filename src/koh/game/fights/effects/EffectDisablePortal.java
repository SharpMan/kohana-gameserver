package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layers.FightPortal;

/**
 *
 * @author Neo-Craft
 */
public class EffectDisablePortal extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.getCell().hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightPortal) (CastInfos.getCell().getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).disable(CastInfos.caster);
        }
        return -1;
    }

}
