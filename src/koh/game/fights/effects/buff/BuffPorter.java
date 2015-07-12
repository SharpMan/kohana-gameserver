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

/**
 *
 * @author Neo-Craft
 */
public class BuffPorter extends BuffEffect {

    public BuffPorter(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDMOVE, BuffDecrementType.TYPE_ENDMOVE);
        this.Duration = -1;
        Target.States.FakeState(FightStateEnum.Porté, true);
        this.CastInfos.EffectType = StatsEnum.Add_State;
        Target.SetCell(this.Caster.myCell);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (this.Caster.CellId() != this.Target.CellId()) {
            if (!isCalledBy("koh.game.fights.effects.EffectLancer", 6)) {
                this.Caster.Fight.sendToField(new GameActionFightDropCharacterMessage(ACTION_CARRY_CHARACTER, this.Caster.ID, this.Target.ID, (short) this.Target.CellId()));
            }
            this.Caster.Fight.sendToField(new GameActionFightDispellSpellMessage(406, this.Caster.ID, this.Target.ID, this.CastInfos.SpellId));
            Main.Logs().writeDebug("3bada " + this.Caster.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).count());
            Main.Logs().writeDebug("3bada " + this.Target.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorter && x.Duration != 0).count());
            Main.Logs().writeDebug("3bada " + this.Caster.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorteur).count());
            Main.Logs().writeDebug("3bada " + this.Target.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorter).count());
            this.Caster.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).forEach(x -> {
                {
                    x.RemoveEffect();
                    this.Caster.Fight.sendToField(new GameActionFightDispellEffectMessage(514, this.Caster.ID, this.Caster.ID, x.GetId()));
                }
            });
            this.Target.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorter && x.Duration != 0).forEach(x -> {
                {
                    x.RemoveEffect();
                    this.Caster.Fight.sendToField(new GameActionFightDispellEffectMessage(514, this.Caster.ID, this.Target.ID, x.GetId()));
                }
            });
            this.Duration = 0;
        }
        return -1;
    }

    @Override
    public int RemoveEffect() {
        Target.States.FakeState(FightStateEnum.Porté, false);

        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporaryBoostStateEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, (short)/*this.CastInfos.GetEffectUID()*/ 3, this.CastInfos.ParentUID, (short) 1, (short) 8);
    }

    public static boolean isCalledBy(String Comparant, int... indexes) {
        for (int i : indexes) {
            if (sun.reflect.Reflection.getCallerClass(i).toString().equalsIgnoreCase("class " + Comparant)) {
                return true;
            }
        }
        return false;
    }

}
