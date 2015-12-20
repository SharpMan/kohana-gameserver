package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layer.FightActivableObject;

/**
 *
 * @author Neo-Craft
 */
public class EffectPortalTeleportation extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.caster.getFight().getCell(CastInfos.CellId).HasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.CellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).loadTargets(CastInfos.Targets.get(0));
            return ((FightActivableObject) (CastInfos.caster.getFight().getCell(CastInfos.CellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).activate(CastInfos.Targets.get(0));
        }
        return -1;
    }

}
