package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layer.FightActivableObject;

/**
 *
 * @author Neo-Craft
 */
public class EffectPortalTeleportation extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.caster.getFight().getCell(CastInfos.cellId).HasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.cellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).loadTargets(CastInfos.targets.get(0));
            return ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.cellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).activate(CastInfos.targets.get(0));
        }
        return -1;
    }

}
