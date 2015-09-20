package koh.game.fights.effects.buff;

import koh.game.entities.environments.Pathfinder;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectPush;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffTpFirstPos extends BuffEffect {

    public BuffTpFirstPos(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (Target.previousFirstCellPos.isEmpty()) {
            return -1;
        }
        FightCell cell = Target.Fight.GetCell(Target.previousFirstCellPos.get(Target.previousFirstCellPos.size() - 1));

        if (cell != null) {
            Target.Fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.Caster.ID, Target.ID, cell.Id));

            return Target.SetCell(cell);
        }

        return -1;

    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.CastInfos.Effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.delay);
    }
}
