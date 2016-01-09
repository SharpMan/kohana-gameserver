package koh.game.fights.effects;

import koh.game.fights.IFightObject;
import koh.game.fights.layers.FightActivableObject;

/**
 *
 * @author Neo-Craft
 */
public class EffectPortalTeleportation extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.caster.getFight().getCell(castInfos.cellId).hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            ((FightActivableObject) (castInfos.caster.getFight().getCell(castInfos.cellId).getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).loadTargets(castInfos.targets.get(0));
            return ((FightActivableObject) (castInfos.caster.getFight().getCell(castInfos.cellId).getObjects(IFightObject.FightObjectType.OBJECT_PORTAL)[0])).activate(castInfos.targets.get(0));
        }
        return -1;
    }

}
