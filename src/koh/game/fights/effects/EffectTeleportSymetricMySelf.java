package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholia
 */
public class EffectTeleportSymetricMySelf extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        FightCell cell;
        for (Fighter target : castInfos.targets) {
            cell = castInfos.caster.getFight().getCell(castInfos.caster.getMapPoint().pointSymetry(target.getMapPoint()).get_cellId());

            if (cell != null && cell.canWalk()) {
                castInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), castInfos.caster.getID(), cell.Id));

                return castInfos.caster.setCell(cell);
            }
        }
        return -1;
    }

}
