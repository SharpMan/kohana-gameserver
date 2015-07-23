package koh.game.fights.effects.buff;

import koh.game.entities.environments.Pathfinder;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectPush;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffReffoulage extends BuffEffect {

    public BuffReffoulage(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_POST_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (Target.CellId() != DamageInfos.targetKnownCellId || Pathfinder.GoalDistance(Target.Fight.Map, DamageInfos.Caster.CellId(), Target.CellId()) > 1) {
            return -1;
        }
        
        DamageValue.setValue(0);

        EffectCast SubInfos = new EffectCast(StatsEnum.Push_Back, 0, (short) 0, 0, null, DamageInfos.Caster, null, false, StatsEnum.NONE, 0, null);
        byte Direction = Pathfinder.GetDirection(Target.Fight.Map, DamageInfos.Caster.CellId(), Target.CellId());

        // Application du push
        return EffectPush.ApplyPush(SubInfos, this.Target, Direction, 1);
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.CastInfos.Effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.delay);
    }

}