package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.utils.XelorHandler;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;

/**
 *
 * @author Melancholia
 */
public class EffectTeleportSymetricMySelf extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        FightCell cell;
        int toReturn = -1;
        for (Fighter target : castInfos.targets) {
            try {
                cell = castInfos.caster.getFight().getCell(castInfos.caster.getMapPoint().pointSymetry(target.getMapPoint()).get_cellId());
            }
            catch (NullPointerException e) { continue; }
            if (cell != null && cell.canWalk()) {
                castInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), castInfos.caster.getID(), cell.Id));

                toReturn = castInfos.caster.setCell(cell);
            }
            else if(cell.hasFighter()) { //TELEFRAG
                final Fighter target2 = cell.getFighter();

                final FightCell targetCell = castInfos.caster.getMyCell();
                target2.getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, target2 .getID(), castInfos.caster.getID(), castInfos.caster.getCellId(), target2 .getCellId()));
                target2.setCell(null);
                castInfos.caster.setCell(null);

                if (target2.setCell(targetCell, false) == -3 || castInfos.caster.setCell(cell, false) == -3) {
                    return -3;
                }

                //Separated for false Sync wih piège call pushBackEffect
                if (target2 .onCellChanged() == -3 || castInfos.caster.onCellChanged() == -3) {
                    return -3;
                }

                int result = target2 .getMyCell().onObjectAdded(target2 );
                if (result == -3) {
                    return result;
                }
                result = castInfos.caster.getMyCell().onObjectAdded(castInfos.caster);
                if (result == -3) {
                    return result;
                }
                 if(castInfos.spellId == 88){
                     if(target2 instanceof SummonedFighter && target2.asSummon().getGrade().getMonsterId() == 3958) { // Synchro
                         final SpellLevel spell = DAO.getSpells().findSpell(5435).getLevelOrNear(target2.asSummon().getGrade().getLevel());
                         castInfos.getFight().launchSpell(target2, spell, target2.getCellId(), true, true, true, castInfos.spellId);
                         continue;
                     }
                    EffectTeleportSymetricFromCaster.applyTelegraph(castInfos,castInfos.caster,target2);
                    XelorHandler.boostSynchro(castInfos.caster,castInfos.spellLevel);
                     final EffectCast castInfos2 = new EffectCast(cooldown.getEffectType(), castInfos.spellId, target.getCellId(), 0, cooldown, castInfos.caster, new ArrayList<Fighter>() {{ this.add(castInfos.caster); }}, false, StatsEnum.NONE, 0, castInfos.spellLevel);
                     castInfos2.targetKnownCellId = target.getCellId();
                     if (EffectBase.tryApplyEffect(castInfos2) == -3) {
                         break;
                     }
                }
            }
            if (toReturn != -1) {
                break;
            }
        }
        return toReturn;
    }



    private static final EffectInstanceDice cooldown = new EffectInstanceDice(new EffectInstance(131900, 1045,0, "a", 0, 0,0, "P1,", 0, "I", false, true, true) , 0, 88,0);

}
