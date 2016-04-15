package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEnableBomb;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.layers.FightBomb;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import org.apache.commons.lang3.ArrayUtils;

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
        if (((BombFighter) target).fightBombs != null) {
            int totalCombo = target.getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            for (FightBomb bomb : ((BombFighter) target).fightBombs) {
                futureExplosedBombs = ArrayUtils.add(futureExplosedBombs, ((bomb.owner[0].getID() == target.getID()) ? bomb.owner[1] : bomb.owner[0]));
                totalCombo += (bomb.owner[0].getID() == target.getID()) ? bomb.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES) : bomb.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            }
            for (BombFighter bomb : futureExplosedBombs) {
                if (bomb.fightBombs != null) {
                    for (FightBomb bomb2 : bomb.fightBombs) {
                        if (bomb2.owner[0].getID() != target.getID() && !ArrayUtils.contains(futureExplosedBombs, bomb2.owner[0])) {
                            futureExplosedBombs = ArrayUtils.add(futureExplosedBombs, bomb2.owner[0]);
                            totalCombo += bomb2.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                        if (bomb2.owner[1].getID() != target.getID() && !ArrayUtils.contains(futureExplosedBombs, bomb2.owner[1])) {
                            futureExplosedBombs = ArrayUtils.add(futureExplosedBombs, bomb2.owner[1]);
                            totalCombo += bomb2.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                    }
                }
            }
            castInfos.caster.getFight().sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + totalCombo + "% dommages d'explosion"}));
            target.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal += totalCombo;
            for (BombFighter bomb : futureExplosedBombs) {
                bomb.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal += totalCombo;
                bomb.tryDie(castInfos.caster.getID(), true);
            }
        }
        return target.tryDie(castInfos.caster.getID(), true);
    }

}
