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
                if (Target.team == CastInfos.caster.team) {
                    continue;
                }
            } else if (CastInfos.SpellId == 438) {
                if (Target.team != CastInfos.caster.team) {
                    continue;
                }
            }
            FightCell CasterCell = CastInfos.caster.myCell, TargetCell = Target.myCell;
            CastInfos.caster.fight.sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, CastInfos.caster.getID(), Target.getID(), Target.getCellId(), CastInfos.caster.getCellId()));
            CastInfos.caster.setCell(null);
            Target.setCell(null);

            if (CastInfos.caster.setCell(TargetCell, false) == -3 || Target.setCell(CasterCell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (CastInfos.caster.onCellChanged() == -3 || Target.onCellChanged() == -3) {
                return -3;
            }

            int Result = CastInfos.caster.myCell.onObjectAdded(CastInfos.caster);
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
