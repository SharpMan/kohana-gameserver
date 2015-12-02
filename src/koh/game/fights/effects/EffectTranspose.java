package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectTranspose extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets.stream().filter(target -> /*!(target instanceof StaticFighter) &&*/ !target.states.hasState(FightStateEnum.Porté) && !target.states.hasState(FightStateEnum.Inébranlable) && !target.states.hasState(FightStateEnum.Enraciné) && !target.states.hasState(FightStateEnum.Indéplaçable)).toArray(Fighter[]::new)) {
            if (CastInfos.SpellId == 445) {
                if (Target.team == CastInfos.Caster.team) {
                    continue;
                }
            } else if (CastInfos.SpellId == 438) {
                if (Target.team != CastInfos.Caster.team) {
                    continue;
                }
            }
            FightCell CasterCell = CastInfos.Caster.myCell, TargetCell = Target.myCell;
            CastInfos.Caster.fight.sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, CastInfos.Caster.ID, Target.ID, Target.getCellId(), CastInfos.Caster.getCellId()));
            CastInfos.Caster.setCell(null);
            Target.setCell(null);

            if (CastInfos.Caster.setCell(TargetCell, false) == -3 || Target.setCell(CasterCell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (CastInfos.Caster.onCellChanged() == -3 || Target.onCellChanged() == -3) {
                return -3;
            }

            int Result = CastInfos.Caster.myCell.onObjectAdded(CastInfos.Caster);
            if (Result == -3) {
                return Result;
            }
            Result = Target.myCell.onObjectAdded(Target);
            if (Result == -3) {
                return Result;
            }
        }
        return -1;
    }

}
