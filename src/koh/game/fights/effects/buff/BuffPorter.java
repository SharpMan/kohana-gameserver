package koh.game.fights.effects.buff;

import koh.game.Main;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CARRY_CHARACTER;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellEffectMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightDropCharacterMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostStateEffect;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class BuffPorter extends BuffEffect {

    //NOTE: This class's code is daamned even the author didn't undestand it

    private static final Logger logger = LogManager.getLogger(BuffPorter.class);

    public BuffPorter(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDMOVE, BuffDecrementType.TYPE_ENDMOVE);
        this.Duration = -1;
        Target.states.fakeState(FightStateEnum.Porté, true);
        this.CastInfos.EffectType = StatsEnum.Add_State;
        Target.setCell(this.Caster.myCell);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (this.Caster.getCellId() != this.Target.getCellId()) {
            if (!isCalledBy("koh.game.fights.effects.EffectLancer", 6)) {
                this.Caster.fight.sendToField(new GameActionFightDropCharacterMessage(ACTION_CARRY_CHARACTER, this.Caster.ID, this.Target.ID, (short) this.Target.getCellId()));
            }
            this.Caster.fight.sendToField(new GameActionFightDispellSpellMessage(406, this.Caster.ID, this.Target.ID, this.CastInfos.SpellId));
            logger.debug("3bada " + this.Caster.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).count());
            logger.debug("3bada " + this.Target.buff.getAllBuffs().filter(x -> x instanceof BuffPorter && x.Duration != 0).count());
            logger.debug("3bada " + this.Caster.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur).count());
            logger.debug("3bada " + this.Target.buff.getAllBuffs().filter(x -> x instanceof BuffPorter).count());
            this.Caster.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).forEach(x -> {
                {
                    x.removeEffect();
                    this.Caster.fight.sendToField(new GameActionFightDispellEffectMessage(514, this.Caster.ID, this.Caster.ID, x.GetId()));
                }
            });
            this.Target.buff.getAllBuffs().filter(x -> x instanceof BuffPorter && x.Duration != 0).forEach(x -> {
                {
                    x.removeEffect();
                    this.Caster.fight.sendToField(new GameActionFightDispellEffectMessage(514, this.Caster.ID, this.Target.ID, x.GetId()));
                }
            });
            this.Duration = 0;
        }
        return -1;
    }

    @Override
    public int removeEffect() {
        Target.states.fakeState(FightStateEnum.Porté, false);

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostStateEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, (short)/*this.CastInfos.GetEffectUID()*/ 3, this.CastInfos.ParentUID, (short) 1, (short) 8);
    }

    public static boolean isCalledBy(String Comparant, int... indexes) { //Do not modif long story
        for (int i : indexes) {
            if (sun.reflect.Reflection.getCallerClass(i).toString().equalsIgnoreCase("class " + Comparant)) {
                return true;
            }
        }
        return false;
    }

}
