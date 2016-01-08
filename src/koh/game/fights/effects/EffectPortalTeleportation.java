package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layers.FightActivableObject;

/**
 *
 * @author Neo-Craft
 */
public class EffectPortalTeleportation extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.caster.getFight().getCell(CastInfos.cellId).hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.cellId).getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).loadTargets(CastInfos.targets.get(0));
            return ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.cellId).getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).activate(CastInfos.targets.get(0));
        }
        return -1;
    }

}
