package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.utils.XelorHandler;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

/**
 * Created by Melancholia on 1/4/16.
 */
public class EffectTeleportSymetricImpactcell extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        boolean synchroBoosted = false;
        for (Fighter target : castInfos.targets) {
            final MapPoint point = target.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.cellId));
            if (point == null) {
                return -1;
            }
            //target.getFight().sendToField(new ShowCellMessage(target.getID(), point.get_cellId()));
            //target.getFight().sendToField(new ShowCellMessage(target.getID(),(target.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.caster.getCellId()))).get_cellId()));
            final FightCell cell = castInfos.caster.getFight().getCell(point.get_cellId());

            if (cell != null) {
                if (cell.canWalk()) {
                    castInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

                    final int result = target.setCell(cell);
                    if (result != -1) {
                        return result;
                    }
                    if(castInfos.spellId != 89) {
                        if (target.getStates().hasState(FightStateEnum.TÉLÉFRAG) || target.getStates().hasState(FightStateEnum.TELEFRAG)) {
                            unTelefrag(castInfos, target);
                        }
                    }
                } else if (cell.hasFighter()) { //TELEFRAG
                    final Fighter target2 = cell.getFighter();
                    final MapPoint point2 = target2.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.cellId));
                    if (point2 != null) {
                        if(!target.getPreviousCellPos().isEmpty() &&
                                castInfos.targets.contains(target2) &&
                                castInfos.targets.indexOf(target) > castInfos.targets.indexOf(target2) &&
                                target.getCellId() == point2.get_cellId() &&
                                target.getPreviousCellPos().get(target.getPreviousCellPos().size() -1) == target2.getCellId()){
                            continue;
                        }
                    }

                    final FightCell targetCell = target.getMyCell();
                    target2.getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, target2.getID(), target.getID(), target.getCellId(), target2.getCellId()));
                    target2.setCell(null);
                    target.setCell(null);

                    if (target2.setCell(targetCell, false) == -3 || target.setCell(cell, false) == -3) {
                        return -3;
                    }

                    //Separated for false Sync wih piège call pushBackEffect
                    if (target2.onCellChanged() == -3 || target.onCellChanged() == -3) {
                        return -3;
                    }

                    int result = target2.getMyCell().onObjectAdded(target2);
                    if (result == -3) {
                        return result;
                    }
                    result = target.getMyCell().onObjectAdded(target);
                    if (result == -3) {
                        return result;
                    }
                    if (castInfos.spellId == 96) { //dust temporal
                        if(target.getStates().hasState(FightStateEnum.TÉLÉFRAG) || target.getStates().hasState(FightStateEnum.TELEFRAG) ){
                            unTelefrag(castInfos,target);
                            continue;
                        }
                        else {
                            applyTelegraph(castInfos, target, target2);
                            if (!synchroBoosted) {
                                XelorHandler.boostSynchro(castInfos.caster, castInfos.spellLevel);
                                synchroBoosted = true;
                            }
                        }

                    }
                    if(castInfos.spellId == 89){
                        if(!target.getStates().hasState(FightStateEnum.TÉLÉFRAG) && !target.getStates().hasState(FightStateEnum.TELEFRAG) ){
                            applyTelegraph(castInfos, target, target2);
                            if (!synchroBoosted) {
                                XelorHandler.boostSynchro(castInfos.caster, castInfos.spellLevel);
                                synchroBoosted = true;
                            }
                        }
                    }
                }

            }
        }
        return -1;
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
    }

    public static void unTelefrag(EffectCast parentCastInfos,Fighter... fighters){
        for (Fighter fighter : fighters) {
            final EffectCast castInfos;
            if(fighter.getStates().hasState(FightStateEnum.TÉLÉFRAG)){
                castInfos = new EffectCast(UN_TELEFRAG.getEffectType(), parentCastInfos.spellId, fighter.getCellId(), 0, UN_TELEFRAG, fighter, new ArrayList<Fighter>() {{ this.add(fighter); }}, false, StatsEnum.NONE, 0, parentCastInfos.spellLevel);
            }
            else {
                castInfos = new EffectCast(UN_TELEFRAG2.getEffectType(), parentCastInfos.spellId, fighter.getCellId(), 0, UN_TELEFRAG2, fighter, new ArrayList<Fighter>() {{ this.add(fighter); }}, false, StatsEnum.NONE, 0, parentCastInfos.spellLevel);
            }
            castInfos.targetKnownCellId = fighter.getCellId();
            if (EffectBase.tryApplyEffect(castInfos) == -3) {
                break;
            }
        }
    }



    static final EffectInstanceDice UN_TELEFRAG = Arrays.stream(DAO.getSpells().findLevel(24071).getEffects())
            .filter(e -> e.value == 244 && e.effectId == 951)
            .findFirst()
            .get();

    static final EffectInstanceDice UN_TELEFRAG2 = Arrays.stream(DAO.getSpells().findLevel(24071).getEffects())
            .filter(e -> e.value == 251 && e.effectId == 951)
            .findFirst()
            .get();

    static final EffectInstanceDice TELEFRAG = Arrays.stream(DAO.getSpells().findLevel(24073).getEffects())
            .filter(e -> e.value == 244)
            .findFirst()
            .get();

    static final EffectInstanceDice TELEFRAG2 = Arrays.stream(DAO.getSpells().findLevel(24073).getEffects())
            .filter(e -> e.value == 251)
            .findFirst()
            .get();

}
