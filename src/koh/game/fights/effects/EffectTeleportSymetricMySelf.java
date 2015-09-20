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
    public int ApplyEffect(EffectCast CastInfos) {
        FightCell cell;
        for (Fighter Target : CastInfos.Targets) {
            cell = CastInfos.Caster.Fight.GetCell(CastInfos.Caster.MapPoint().pointSymetry(Target.MapPoint()).get_cellId());

            if (cell != null && cell.IsWalkable()) {
                CastInfos.Caster.Fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.Caster.ID, CastInfos.Caster.ID, cell.Id));

                return Target.SetCell(cell);
            }
        }
        return -1;
    }

}
