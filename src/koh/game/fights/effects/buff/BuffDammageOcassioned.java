package koh.game.fights.effects.buff;

import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;
import static koh.protocol.client.enums.StatsEnum.PA_USED_LOST_X_PDV_2;


/**
 * @author Neo-Craft
 */
public class BuffDammageOcassioned extends BuffEffect {

    private final int JET;

    public BuffDammageOcassioned(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.JET = CastInfos.randomJet(Target);
        if(castInfos.spellId == 103){ // Chance eca
            this.duration++;
            //this.decrementType = BuffDecrementType.TYPE_BEGINTURN;
        }
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast damageInfos) {
        if (damageInfos.effectType != PA_USED_LOST_X_PDV_2 && EffectHelper.verifyEffectTrigger(damageInfos.caster, target, this.castInfos.spellLevel.getEffects(), damageInfos.effect, damageInfos.isCAC, this.castInfos.effect.triggers, damageInfos.cellId)) {
            DamageValue.setValue((DamageValue.intValue() * this.JET) / 100);
        }
        return super.applyEffect(DamageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}
