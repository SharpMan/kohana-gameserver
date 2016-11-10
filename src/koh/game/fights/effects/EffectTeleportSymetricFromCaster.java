package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.utils.XelorHandler;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Melancholiax
 */
public class EffectTeleportSymetricFromCaster extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        int toReturn = -1;
        FightCell cell;
        if(castInfos.targets == null){
            return  toReturn;
        }
        for (Fighter target : castInfos.targets) {
            try {
                cell = castInfos.caster.getFight().getCell(target.getMapPoint().pointSymetry(castInfos.caster.getMapPoint()).get_cellId());
            }
            catch (NullPointerException e){
                continue;
            }
            if (cell != null) {
                if(cell.canWalk()) {
                    target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

                    toReturn = target.setCell(cell);
                    if (toReturn != -1) {
                        break;
                    }
                }else if(cell.hasFighter()) { //TELEFRAG
                    final Fighter target2 = cell.getFighter();
                    final FightCell targetCell = target.getMyCell();
                    target2 .getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, target2 .getID(), target.getID(), target.getCellId(), target2 .getCellId()));
                    target2 .setCell(null);
                    target.setCell(null);

                    if (target2.setCell(targetCell, false) == -3 || target.setCell(cell, false) == -3) {
                        return -3;
                    }

                    //Separated for false Sync wih piège call pushBackEffect
                    if (target2 .onCellChanged() == -3 || target.onCellChanged() == -3) {
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

                    if(castInfos.spellId == 91){ //Frape de xelor
                        applyTelegraph(castInfos,target,target2);
                        XelorHandler.boostSynchro(castInfos.caster, castInfos.spellLevel);
                    }

                }
            }

        }
        return toReturn;
    }

    public static void applyTelegraph(EffectCast castInfos, Fighter... fighters){
        for (Fighter fighter : fighters) {
            if(fighter.getStates().hasState(FightStateEnum.TÉLÉFRAG) || fighter.getStates().hasState(FightStateEnum.TELEFRAG) ){
                continue;
            }
            if( fighter instanceof SummonedFighter && fighter.asSummon().getGrade().getMonsterId() == 3958 && ArrayUtils.indexOf(fighters,fighter) == 0) { // Synchro
                final SpellLevel spell = DAO.getSpells().findSpell(5435).getLevelOrNear(fighter.asSummon().getGrade().getLevel());
                castInfos.getFight().launchSpell(fighter, spell, fighter.getCellId(), true, true, true, castInfos.spellId);
                continue;
            }
            final BuffEffect buff = new BuffState(new EffectCast(StatsEnum.ADD_STATE, castInfos.spellId, castInfos.cellId, 0, castInfos.caster.isEnnemyWith(fighter) ? TELEFRAG2 : TELEFRAG, castInfos.caster, new ArrayList<Fighter>(1) {{ this.add(fighter); }}, false, StatsEnum.NONE, 0, castInfos.spellLevel), fighter);
            fighter.getBuff().addBuff(buff);
            buff.applyEffect(null,null);
        }
        //new ArrayList<Fighter>() {{ this.add(fighter); }}
    }



    static final EffectInstanceDice TELEFRAG = Arrays.stream(DAO.getSpells().findLevel(24073).getEffects())
            .filter(e -> e.value == 244)
            .findFirst()
            .get();

    static final EffectInstanceDice TELEFRAG2 = Arrays.stream(DAO.getSpells().findLevel(24073).getEffects())
            .filter(e -> e.value == 251)
            .findFirst()
            .get();



}
