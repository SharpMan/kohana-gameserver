package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPorter;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.game.fights.fighters.StaticSummonedFighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectPorter extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            if(target instanceof StaticSummonedFighter){
                continue;
            }
            castInfos.caster.getBuff().addBuff(new BuffPorteur(castInfos, target));
            target.getBuff().addBuff(new BuffPorter(castInfos, target));
        }

        return -1;
    }

}
