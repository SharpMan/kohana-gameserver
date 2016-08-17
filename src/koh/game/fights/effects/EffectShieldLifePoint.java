package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffShieldLifePoint;
import koh.game.fights.fighters.IllusionFighter;

/**
 * Created by Melancholia on 1/3/16.
 */
public class EffectShieldLifePoint extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            if(target instanceof IllusionFighter){
                continue;//Roulette tue clone ...
            }
            final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets,castInfos.spellLevel);
            final BuffShieldLifePoint buffStats = new BuffShieldLifePoint(subInfos, target);
            if (target.getBuff().buffMaxStackReached(buffStats)) {
                continue;
            }
            if (buffStats.applyEffect(null, null) == -3) {
                return -3;
            }
            target.getBuff().addBuff(buffStats);

        }

        return -1;
    }


}
