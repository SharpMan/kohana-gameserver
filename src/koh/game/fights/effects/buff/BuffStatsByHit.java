package koh.game.fights.effects.buff;

import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

public class BuffStatsByHit extends BuffEffect {

    private final int JET;

    public BuffStatsByHit(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.JET = castInfos.randomJet(target);
        this.duration++;
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        try {
            if(damageInfos== null || damageValue == null)
                return super.applyEffect(damageValue, damageInfos);
            if (EffectHelper.verifyEffectTrigger(damageInfos.caster, target, damageInfos.spellLevel.getEffects(), damageInfos.effect, false, this.castInfos.effect.triggers, damageInfos.cellId)) {
                final BuffStats buffStats = new BuffStats(new EffectCast(castInfos.effectType, this.castInfos.spellId, this.castInfos.cellId, 0, null, castInfos.caster, null, false, this.castInfos.effectType, JET, null, this.castInfos.effect.duration, this.getId()), this.target);

                buffStats.applyEffect(null, null);

                this.target.getBuff().addBuff(buffStats);
            }
        }
        catch (NullPointerException e){
            logger.error("Wh's null ? 1 {} 2 {} 3 {}",damageInfos.caster == null,damageInfos.spellLevel == null,this.castInfos.effect == null);
        }


        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
