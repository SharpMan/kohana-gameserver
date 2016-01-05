package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;

/**
 *
 * @author Neo-Craft
 */
public class BuffReflectSpell extends BuffEffect {

    public byte reflectLevel = 0;

    public BuffReflectSpell(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.reflectLevel = CastInfos.spellLevel.getGrade();
        this.target.getStates().addState(this);
    }

    @Override
    public int removeEffect() {
        this.target.getStates().delState(this);

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
