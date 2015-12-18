package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layer.FightPortal;

/**
 *
 * @author Neo-Craft
 */
public class EffectDisablePortal extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.Cell().HasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightPortal) (CastInfos.Cell().GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).disable(CastInfos.caster);
        }
        return -1;
    }

}
