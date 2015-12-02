package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffDamagePercentReduced;
import koh.game.fights.Fighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamagePercentReduced extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for(Fighter Target : CastInfos.Targets){
            Target.buff.addBuff(new BuffDamagePercentReduced(CastInfos,Target));
        }
         return -1;
    }
    
    
}