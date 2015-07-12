package koh.game.fights.effects;


/**
 *
 * @author Neo-Craft
 */
public class EffectDieFighter extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        CastInfos.Targets.forEach(Target -> Target.TryDie(Target.ID, true));
        return -1;
    }

}
