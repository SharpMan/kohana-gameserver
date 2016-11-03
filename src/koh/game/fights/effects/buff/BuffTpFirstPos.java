package koh.game.fights.effects.buff;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

import koh.game.fights.effects.EffectTeleportSymetricFromCaster;
import koh.game.fights.utils.XelorHandler;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffTpFirstPos extends BuffEffect {

    public BuffTpFirstPos(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (target.getPreviousFirstCellPos().isEmpty()) {
            return -1;
        }
        final FightCell cell = target.getFight().getCell(target.getPreviousFirstCellPos().get(target.getPreviousFirstCellPos().size() - 1));

        if (cell != null && cell.canWalk()) {
            target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

            return target.setCell(cell);
        }else if(cell != null && cell.hasFighter()){
            final Fighter target2 = cell.getFighter();
            final FightCell targetCell = target.getMyCell();
            target2 .getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, target2 .getID(), target.getID(), target.getCellId(), target2 .getCellId()));
            target2 .setCell(null);
            target.setCell(null);

            if (target2.setCell(targetCell, false) == -3 || target.setCell(cell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (target2.onCellChanged() == -3 || target.onCellChanged() == -3) {
                return -3;
            }

            int result = target2 .getMyCell().onObjectAdded(target2 );
            if (result == -3) {
                return result;
            }
            result = target.getMyCell().onObjectAdded(target);
            if (result == -3) {
                return result;
            }
            if(!target.getStates().hasState(FightStateEnum.TÉLÉFRAG) && !target.getStates().hasState(FightStateEnum.TELEFRAG)){
                XelorHandler.boostSynchro(castInfos.caster,castInfos.spellLevel);
            }
            EffectTeleportSymetricFromCaster.applyTelegraph(castInfos,target,target2);
        }

        return -1;

    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }
}
