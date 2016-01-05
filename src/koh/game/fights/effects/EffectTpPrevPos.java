package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholia
 */
public class EffectTpPrevPos extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        int toReturn = -1;
        for (Fighter Target : CastInfos.targets) {
            if (Target.getPreviousCellPos().isEmpty()) {
                continue;
            }
            FightCell cell = Target.getFight().getCell(Target.getPreviousCellPos().get(Target.getPreviousCellPos().size() - 1));

            if (cell != null && cell.canWalk()) {
                Target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.caster.getID(), Target.getID(), cell.Id));

                toReturn = Target.setCell(cell);
            }
            if (toReturn != -1) {
                break;
            }
        }

        return toReturn;
    }

}
