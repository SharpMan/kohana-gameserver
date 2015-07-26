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
        if (CastInfos.Caster.Fight.GetCell(CastInfos.CellId).HasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightActivableObject) (CastInfos.Caster.Fight.GetCell(CastInfos.CellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).LoadTargets(CastInfos.Targets.get(0));
            return ((FightActivableObject) (CastInfos.Caster.Fight.GetCell(CastInfos.CellId).GetObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).Activate(CastInfos.Targets.get(0));
        }
        return -1;
    }

}
