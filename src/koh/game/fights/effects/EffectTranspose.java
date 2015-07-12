package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectTranspose extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (CastInfos.SpellId == 445) {
                if (Target.Team == CastInfos.Caster.Team) {
                    continue;
                }
            } else if (CastInfos.SpellId == 438) {
                if (Target.Team != CastInfos.Caster.Team) {
                    continue;
                }
            }
            FightCell CasterCell = CastInfos.Caster.myCell, TargetCell = Target.myCell;
            CastInfos.Caster.Fight.sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, CastInfos.Caster.ID, Target.ID, Target.CellId(), CastInfos.Caster.CellId()));
            CastInfos.Caster.SetCell(null);
            Target.SetCell(null);

            if (CastInfos.Caster.SetCell(TargetCell, false) == -3 || Target.SetCell(CasterCell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (CastInfos.Caster.OnCellChanged() == -3 || Target.OnCellChanged() == -3) {
                return -3;
            }

            CastInfos.Caster.myCell.onObjectAdded(CastInfos.Caster);
            Target.myCell.onObjectAdded(Target);

        }
        return -1;
    }

}
