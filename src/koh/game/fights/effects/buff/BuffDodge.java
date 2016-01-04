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
public class BuffDodge extends BuffEffect {

    public BuffDodge(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_POST_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (target.getCellId() != DamageInfos.targetKnownCellId || Pathfinder.getGoalDistance(target.getFight().getMap(), DamageInfos.caster.getCellId(), target.getCellId()) > 1) {
            return -1;
        }
        
        DamageValue.setValue(0);

        EffectCast SubInfos = new EffectCast(StatsEnum.PUSH_BACK, 0, (short) 0, 0, null, DamageInfos.caster, null, false, StatsEnum.NONE, 0, null);
        byte Direction = Pathfinder.getDirection(target.getFight().getMap(), DamageInfos.caster.getCellId(), target.getCellId());

        // Application du push
        return EffectPush.ApplyPush(SubInfos, this.target, Direction, 1);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.CastInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.effect.effectUid, 0, (short) this.CastInfos.effect.diceNum, (short) this.CastInfos.effect.diceSide, (short) this.CastInfos.effect.value, (short) this.CastInfos.effect.delay);
    }

}
