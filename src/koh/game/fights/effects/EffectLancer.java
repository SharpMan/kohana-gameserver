package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPorteur;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_THROW_CARRIED_CHARACTER;

import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightThrowCharacterMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectLancer extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.caster.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffPorteur)) {
            final Fighter target = castInfos.caster.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).findFirst().get().target;
            if (target != null) {

                target.getFight().sendToField(new GameActionFightThrowCharacterMessage(ACTION_THROW_CARRIED_CHARACTER, castInfos.caster.getID(), target.getID(), castInfos.cellId));
                castInfos.caster.getBuff().getAllBuffs().filter(bf -> bf instanceof BuffStats && bf.castInfos.effectType == StatsEnum.ADD_RANGE && bf.duration == -1)
                        .forEach(buff -> castInfos.caster.getBuff().debuff(buff));
                if(target.isFriendlyWith(castInfos.caster))
                    castInfos.caster.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, castInfos.caster.getID(), castInfos.caster.getID(), 693));
                return target.setCell(target.getFight().getCell(castInfos.cellId));
            }
        }

        return -1;
    }

}
