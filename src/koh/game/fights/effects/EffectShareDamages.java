package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffShareDamages;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Melancholia on 7/22/16.
 */
public class EffectShareDamages extends EffectBase {

    private static final AtomicLong idCreator = new AtomicLong(1);

    @Override
    public int applyEffect(EffectCast castInfos) {
        final long uid = idCreator.incrementAndGet();
        for (Fighter target : castInfos.targets) {
            final BuffShareDamages buff = new BuffShareDamages(castInfos, target,uid);
            if (!target.getBuff().buffMaxStackReached(buff)) {
                target.getBuff().addBuff(buff);
                if (buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }

        }
        return -1;
    }

}
