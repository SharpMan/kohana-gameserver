package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholiax
 */
public class EffectTeleportSymetric extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        int toReturn = -1;
        FightCell cell;
        for (Fighter Target : CastInfos.Targets) {
            cell = CastInfos.caster.fight.getCell(Target.getMapPoint().pointSymetry(CastInfos.caster.getMapPoint()).get_cellId());

            if (cell != null && cell.IsWalkable()) {
                Target.fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.caster.getID(), Target.getID(), cell.Id));

                toReturn = Target.setCell(cell);
                if (toReturn != -1) {
                    break;
                }
            }
        }
        return toReturn;
    }

}
