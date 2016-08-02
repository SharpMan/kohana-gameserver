package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffCastSpellOnPortal;
import koh.game.fights.effects.buff.BuffCastSpell;

import java.util.Arrays;

/**
 * Created by Melancholia on 1/3/16.
 */
public class EffectCastSpell extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        //TODO return -2 -3


        if (castInfos.duration >0 && castInfos.effect.triggers.equalsIgnoreCase("P")) {
            castInfos.caster.getBuff().addBuff(new BuffCastSpellOnPortal(castInfos,castInfos.caster));
            return -1;
        }

        if(castInfos.duration > 0){
            for (Fighter target : castInfos.targets)
                target.getBuff().addBuff(new BuffCastSpell(castInfos, target));
            return -1;
        }

        return castSpell(castInfos);

        /*if (castInfos.effect.targetMask.equalsIgnoreCase("a,A,U")) //Arbre sadi daamn the mask
            castInfos.getFight().launchSpell(castInfos.caster, spell, castInfos.cellId, true, true, false);
        else*/

    }

    public static int castSpell(EffectCast castInfos){
        final SpellLevel spell = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide);



        if(castInfos.spellId == 199 || castInfos.effect.diceNum == 5570){ //don & trembl
            for (Fighter target : castInfos.targets) {
                castInfos.getFight().launchSpell(target, spell, target.getCellId(), true, true, true,castInfos.spellId);
            }
            return -1;
        }
        if(castInfos.spellId== 89){ //paradox
            for (Fighter target : castInfos.targets) {
                castInfos.getFight().launchSpell(castInfos.caster, spell, target.getCellId(), true, true, true,castInfos.spellId);
            }
            return -1;
        }

        if(castInfos.effect.targetMask.equalsIgnoreCase("A,E263") && !castInfos.targets.isEmpty()){ //Etat infecte
            final Fighter target = castInfos.targets.get(0);
            target.getTeam().getAliveFighters().filter(fr -> fr != target).forEach(fr ->{
                target.getFight().affectSpellTo(castInfos.caster, fr, castInfos.effect.diceSide, castInfos.effect.diceNum);
            });
            return -1;
        }


        for (Fighter target : castInfos.targets) {
            target.getFight().affectSpellTo(castInfos.caster, target, castInfos.effect.diceSide, castInfos.effect.diceNum);
        }

        return -1;
    }

}
