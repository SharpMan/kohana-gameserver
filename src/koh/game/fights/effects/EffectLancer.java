package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPorteur;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_THROW_CARRIED_CHARACTER;
import koh.protocol.messages.game.actions.fight.GameActionFightThrowCharacterMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectLancer extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        if (CastInfos.caster.buff.getAllBuffs().anyMatch(x -> x instanceof BuffPorteur)) {
            Fighter Target = CastInfos.caster.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).findFirst().get().target;
            if (Target != null) {
                Target.fight.sendToField(new GameActionFightThrowCharacterMessage(ACTION_THROW_CARRIED_CHARACTER, CastInfos.caster.getID(), Target.getID(), CastInfos.CellId));

                return Target.setCell(Target.fight.getCell(CastInfos.CellId));
            }
        }

        return -1;
    }

}
