package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.fighters.MonsterFighter;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;

/**
 * @author Neo-Craft
 */
public class EffectTranspose extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if(castInfos.effectType == StatsEnum.SWITCH_POSITIONS){ //Transko
            castInfos.targets.clear();
            castInfos.getFight().getAliveFighters()
                    .filter(fr -> fr.getStates().hasState(FightStateEnum.Longuevue))
                    .forEach(fr -> castInfos.targets.add(fr));
        }
        for (Fighter target : (Iterable<Fighter>) castInfos.targets.stream()
                .filter(fr -> (fr.getObjectType() != IFightObject.FightObjectType.OBJECT_STATIC)
                        && !fr.getStates().hasState(FightStateEnum.CARRIED)
                        && !fr.getStates().hasState(FightStateEnum.INÉBRANLABLE)
                        && !fr.getStates().hasState(FightStateEnum.ENRACINÉ)
                        && !fr.getStates().hasState(FightStateEnum.INDÉPLAÇABLE))
                ::iterator) {
            if (castInfos.spellId == 445) {
                if (target.getTeam() == castInfos.caster.getTeam()) {
                    continue;
                }
            } else if (castInfos.spellId == 438) {
                if (target.getTeam() != castInfos.caster.getTeam()) {
                    continue;
                }
            }
            else if(target instanceof MonsterFighter && target.asMonster().getGrade().getMonster().isCanSwitchPos()){
                continue;
            }

            final FightCell casterCell = castInfos.caster.getMyCell(), targetCell = target.getMyCell();
            castInfos.caster.getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, castInfos.caster.getID(), target.getID(), target.getCellId(), castInfos.caster.getCellId()));
            castInfos.caster.setCell(null);
            target.setCell(null);

            if (castInfos.caster.setCell(targetCell, false) == -3 || target.setCell(casterCell, false) == -3) {
                return -3;
            }

            //Separated for false Sync wih piège call pushBackEffect
            if (castInfos.caster.onCellChanged() == -3 || target.onCellChanged() == -3) {
                return -3;
            }

            int result = castInfos.caster.getMyCell().onObjectAdded(castInfos.caster);
            if (result == -3) {
                return result;
            }
            result = target.getMyCell().onObjectAdded(target);
            if (result == -3) {
                return result;
            }
        }
        return -1;
    }

}
