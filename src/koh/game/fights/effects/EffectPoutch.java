package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPoutch;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import lombok.extern.log4j.Log4j2;

/**
 * @author Neo-Craft
 */
@Log4j2
public class EffectPoutch extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {

        if(castInfos.effect.diceNum == 3281 || castInfos.effect.diceNum == 3282){
            for (Fighter target : castInfos.targets) {
                if(castInfos.caster.getSpellsController().canLaunchSpell(DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), target.getID())){
                    castInfos.getFight().launchSpell(castInfos.caster, DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), target.getCellId(), true, true, true,castInfos.spellId);
                }
            }
            return -1;
        }
        else if(castInfos.spellId == 3277){ //Convergence
            for (Fighter target : castInfos.targets) {
                if(target.getSpellsController().canLaunchSpell(DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), target.getID())){
                    castInfos.getFight().launchSpell(target, DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), target.getCellId(), true, true, false,castInfos.spellId);
                }
            }
            return -1;
        }

        if (castInfos.duration > 0)
            for (Fighter target : castInfos.targets)
                target.getBuff().addBuff(new BuffPoutch(castInfos, target));
        else {
            if(castInfos.caster.getSpellsController().canLaunchSpell(DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), castInfos.caster.getID())){
                if(castInfos.effect.diceNum == 5454){
                    System.out.println("side"+castInfos.effect.diceSide+" "+(castInfos.getFight().getFightWorker().round %2));
                    if(castInfos.effect.diceSide == 1 && castInfos.getFight().getFightWorker().round %2 == 0){
                        return -1;
                    }
                    if(castInfos.effect.diceSide == 2 && castInfos.getFight().getFightWorker().round %2 != 0){
                        return -1;
                    }
                }
                castInfos.getFight().launchSpell(castInfos.caster, DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), castInfos.caster.getCellId(), true, true, true,castInfos.spellId);
            }
        }
        log.debug("{} duration {} ",castInfos.effectType,castInfos.duration);
        return -1;
    }

}
