package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEnableBomb;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.layer.FightBomb;
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
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            if (Target instanceof BombFighter) {
                if (CastInfos.duration > 0) {
                    Target.getBuff().addBuff(new BuffEnableBomb(CastInfos, Target));
                    continue;
                }
                if (Explose(Target, CastInfos) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

    public static int Explose(Fighter Target, EffectCast CastInfos) {
        BombFighter[] futureExplosedBombs = new BombFighter[0];
        if (((BombFighter) Target).FightBombs != null) {
            int TotalCombo = Target.getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            for (FightBomb Bomb : ((BombFighter) Target).FightBombs) {
                futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, (BombFighter) ((Bomb.owner[0].getID() == Target.getID()) ? Bomb.owner[1] : Bomb.owner[0]));
                TotalCombo += (Bomb.owner[0].getID() == Target.getID()) ? Bomb.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES) : Bomb.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
            }
            for (BombFighter Bomb : futureExplosedBombs) {
                if (Bomb.FightBombs != null) {
                    for (FightBomb Bomb2 : Bomb.FightBombs) {
                        if (Bomb2.owner[0].getID() != Target.getID() && !ArrayUtils.contains(futureExplosedBombs, Bomb2.owner[0])) {
                            futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, Bomb2.owner[0]);
                            TotalCombo += Bomb2.owner[0].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                        if (Bomb2.owner[1].getID() != Target.getID() && !ArrayUtils.contains(futureExplosedBombs, Bomb2.owner[1])) {
                            futureExplosedBombs = (BombFighter[]) ArrayUtils.add(futureExplosedBombs, Bomb2.owner[1]);
                            TotalCombo += Bomb2.owner[1].getStats().getBoost(StatsEnum.COMBO_DAMMAGES);
                        }
                    }
                }
            }
            CastInfos.caster.getFight().sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + TotalCombo + "% dommages d'explosion"}));
            Target.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal = TotalCombo;
            for (BombFighter Bomb : futureExplosedBombs) {
                Bomb.getStats().getEffect(StatsEnum.COMBO_DAMMAGES).additionnal = TotalCombo;
                Bomb.tryDie(CastInfos.caster.getID(), true);
            }
        }
        return Target.tryDie(CastInfos.caster.getID(), true);
    }

}
