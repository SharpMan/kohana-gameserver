package koh.game.fights.effects;


/**
 *
 * @author Neo-Craft
 */
public class EffectDieFighter extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        castInfos.targets.forEach(target -> target.tryDie(target.getID(), true));
        return -1;
    }

}
