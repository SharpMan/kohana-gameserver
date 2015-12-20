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
        for (Fighter target : CastInfos.Targets.stream().filter(target -> /*!(target instanceof StaticFighter) &&*/ !target.getStates().hasState(FightStateEnum.Porté) && !target.getStates().hasState(FightStateEnum.Inébranlable) && !target.getStates().hasState(FightStateEnum.Enraciné) && !target.getStates().hasState(FightStateEnum.Indéplaçable)).toArray(Fighter[]::new)) {
            if (CastInfos.SpellId == 445) {
                if (target.getTeam() == CastInfos.caster.getTeam()) {
                    continue;
                }
            } else if (CastInfos.SpellId == 438) {
                if (target.getTeam() != CastInfos.caster.getTeam()) {
                    continue;
                }
            }
            FightCell CasterCell = CastInfos.caster.getMyCell(), TargetCell = target.getMyCell();
            CastInfos.caster.getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, CastInfos.caster.getID(), target.getID(), target.getCellId(), CastInfos.caster.getCellId()));
            CastInfos.caster.setCell(null);
            target.setCell(null);

            if (CastInfos.caster.setCell(TargetCell, false) == -3 || target.setCell(CasterCell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (CastInfos.caster.onCellChanged() == -3 || target.onCellChanged() == -3) {
                return -3;
            }

            int Result = CastInfos.caster.getMyCell().onObjectAdded(CastInfos.caster);
            if (Result == -3) {
                return Result;
            }
            Result = target.getMyCell().onObjectAdded(target);
            if (Result == -3) {
                return Result;
            }
        }
        return -1;
    }

}
