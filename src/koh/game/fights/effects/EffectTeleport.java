package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectTeleport extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        return ApplyTeleport(CastInfos);
    }

    public static int ApplyTeleport(EffectCast castInfos) {
        Fighter caster = castInfos.caster;
        FightCell cell = caster.getFight().getCell(castInfos.cellId);

        if (cell != null && cell.canWalk()) {
            caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, caster.getID(), caster.getID(), castInfos.cellId));

            return caster.setCell(cell);
        }

        return -1;
    }

}
