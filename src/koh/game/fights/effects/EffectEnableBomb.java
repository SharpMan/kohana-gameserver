package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEnableBomb;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.layers.FightBomb;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class EffectEnableBomb extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            if (Target instanceof BombFighter) {
                if (castInfos.duration > 0) {
                    Target.getBuff().addBuff(new BuffEnableBomb(castInfos, Target));
                    continue;
                }
                if (explose(Target, castInfos) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

    public static int explose(Fighter target, EffectCast castInfos) {
        BombFighter[] futureExplosedBombs = new BombFighter[0];
        if (((BombFighter) target).FightBombs != null) {
            int TotalCombo = target.getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            for (FightBomb Bomb : ((BombFighter) target).FightBombs) {
                futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, (BombFighter) ((Bomb.owner[0].getID() == target.getID()) ? Bomb.owner[1] : Bomb.owner[0]));
                TotalCombo += (Bomb.owner[0].getID() == target.getID()) ? Bomb.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES) : Bomb.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            }
            for (BombFighter Bomb : futureExplosedBombs) {
                if (Bomb.FightBombs != null) {
                    for (FightBomb Bomb2 : Bomb.FightBombs) {
                        if (Bomb2.owner[0].getID() != target.getID() && !ArrayUtils.contains(futureExplosedBombs, Bomb2.owner[0])) {
                            futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, Bomb2.owner[0]);
                            TotalCombo += Bomb2.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                        if (Bomb2.owner[1].getID() != target.getID() && !ArrayUtils.contains(futureExplosedBombs, Bomb2.owner[1])) {
                            futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, Bomb2.owner[1]);
                            TotalCombo += Bomb2.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                    }
                }
            }
            castInfos.caster.getFight().sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + TotalCombo + "% dommages d'explosion"}));
            target.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal = TotalCombo;
            for (BombFighter Bomb : futureExplosedBombs) {
                Bomb.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal = TotalCombo;
                Bomb.tryDie(castInfos.caster.getID(), true);
            }
        }
        return target.tryDie(castInfos.caster.getID(), true);
    }

}
