package koh.game.fights.effects;

import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightModifyEffectsDurationMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDispellEffectDuration extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            final short jet = castInfos.randomJet(target);
            if(castInfos.spellId == 108){ //Esprit felin
                final int result = decrementEffectDurationExceptMe(target,jet);
                if(result != -1){
                    return result;
                }
            }
            else {
                final int result = target.getBuff().decrementEffectDuration(jet);
                if(result != -1){
                    return result;
                }
            }

            target.getFight().sendToField(new GameActionFightModifyEffectsDurationMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, castInfos.caster.getID(), target.getID(), (short) -jet));
        }

        return -1;
    }

    public int decrementEffectDurationExceptMe(Fighter t, int duration) {
        for (BuffEffect buff : t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (buff.isDebuffable() && isNotBad(buff) && buff.decrementDuration(duration) <= 0) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect buff : t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_ENDTURN)) {
            if (buff.isDebuffable() && isNotBad(buff) && buff.decrementDuration(duration) <= 0) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect buff : t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_ENDMOVE)) {
            if (buff.isDebuffable()  && isNotBad(buff) &&  buff.decrementDuration(duration) <= 0) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_BEGINTURN).removeIf(buff -> buff.isDebuffable()  && isNotBad(buff) &&  buff.duration <= 0);
        t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_ENDTURN).removeIf(buff -> buff.isDebuffable()  && isNotBad(buff) &&  buff.duration <= 0);
        t.getBuff().getBuffsDec().get(BuffDecrementType.TYPE_ENDMOVE).removeIf(buff -> buff.isDebuffable()  && isNotBad(buff) &&  buff.duration <= 0);


        t.getBuff().getBuffsAct().values().stream().forEach((buffList) ->
            buffList.removeIf(buff -> buff.isDebuffable()   && isNotBad(buff) &&  buff.duration <= 0)
        );

        return -1;
    }


    private static boolean isNotBad(BuffEffect buff){
        return (buff.getCastInfos().effect.category() != EffectHelper.DAMAGE_EFFECT_CATEGORY)
                || !(buff instanceof BuffStats && GenericStats.getOPPOSITE_STATS().containsKey(buff.castInfos.effectType));
    }

}
