package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffDamagePercentReduced;
import koh.game.fights.Fighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamagePercentReduced extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for(Fighter Target : castInfos.targets){
            Target.getBuff().addBuff(new BuffDamagePercentReduced(castInfos,Target));
        }
         return -1;
    }
    
    
}