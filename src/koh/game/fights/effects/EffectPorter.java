package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPorter;
import koh.game.fights.effects.buff.BuffPorteur;

/**
 *
 * @author Neo-Craft
 */
public class EffectPorter extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            castInfos.caster.getBuff().addBuff(new BuffPorteur(castInfos, Target));
            Target.getBuff().addBuff(new BuffPorter(castInfos, Target));
        }

        return -1;
    }

}
