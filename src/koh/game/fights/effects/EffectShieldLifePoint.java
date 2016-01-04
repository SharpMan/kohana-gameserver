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
            EffectCast subInfos = new EffectCast(castInfos.EffectType, castInfos.SpellId, castInfos.CellId, castInfos.Chance, castInfos.effect, castInfos.caster, castInfos.targets,castInfos.SpellLevel);
            BuffShieldLifePoint buffStats = new BuffShieldLifePoint(subInfos, target);
            if (buffStats.applyEffect(null, null) == -3) {
                return -3;
            }
            target.getBuff().addBuff(buffStats);

        }

        return -1;
    }


}
