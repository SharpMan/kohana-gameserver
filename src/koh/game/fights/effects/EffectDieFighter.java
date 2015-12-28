package koh.game.fights.effects;


/**
 *
 * @author Neo-Craft
 */
public class EffectDieFighter extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        CastInfos.Targets.forEach(Target -> Target.tryDie(Target.getID(), true));
        return -1;
    }

}
