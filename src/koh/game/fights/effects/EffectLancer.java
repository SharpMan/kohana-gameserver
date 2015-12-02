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
        if (CastInfos.Caster.buff.getAllBuffs().anyMatch(x -> x instanceof BuffPorteur)) {
            Fighter Target = CastInfos.Caster.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).findFirst().get().Target;
            if (Target != null) {
                Target.fight.sendToField(new GameActionFightThrowCharacterMessage(ACTION_THROW_CARRIED_CHARACTER, CastInfos.Caster.ID, Target.ID, CastInfos.CellId));

                return Target.setCell(Target.fight.getCell(CastInfos.CellId));
            }
        }

        return -1;
    }

}
