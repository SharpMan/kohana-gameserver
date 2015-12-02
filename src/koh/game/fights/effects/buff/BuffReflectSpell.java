package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;

/**
 *
 * @author Neo-Craft
 */
public class BuffReflectSpell extends BuffEffect {

    public byte ReflectLevel = 0;

    public BuffReflectSpell(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.ReflectLevel = CastInfos.SpellLevel.grade;
        this.Target.states.addState(this);
    }

    @Override
    public int RemoveEffect() {
        this.Target.states.delState(this);

        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
