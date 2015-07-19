package koh.game.fights.effects;

import java.util.Arrays;
import koh.game.controllers.PlayerController;
import koh.game.fights.Fighter;
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
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (Target instanceof BombFighter) {
                BombFighter[] FutureExplosedBombs = new BombFighter[0];
                if (((BombFighter) Target).FightBombs != null) {
                    int TotalCombo = Target.Stats.GetBoost(StatsEnum.Combo_Dammages);
                    for (FightBomb Bomb : ((BombFighter) Target).FightBombs) {
                        FutureExplosedBombs = (BombFighter[]) ArrayUtils.add(FutureExplosedBombs, (BombFighter) ((Bomb.Owner[0].ID == Target.ID) ? Bomb.Owner[1] : Bomb.Owner[0]));
                        TotalCombo += (Bomb.Owner[0].ID == Target.ID) ? Bomb.Owner[1].Stats.GetBoost(StatsEnum.Combo_Dammages) : Bomb.Owner[0].Stats.GetBoost(StatsEnum.Combo_Dammages);
                    }
                    for (BombFighter Bomb : FutureExplosedBombs) {
                        if (Bomb.FightBombs != null) {
                            for (FightBomb Bomb2 : Bomb.FightBombs) {
                                if (Bomb2.Owner[0].ID != Target.ID && !ArrayUtils.contains(FutureExplosedBombs, Bomb2.Owner[0])) {
                                    FutureExplosedBombs = (BombFighter[]) ArrayUtils.add(FutureExplosedBombs, Bomb2.Owner[0]);
                                    TotalCombo += Bomb2.Owner[0].Stats.GetBoost(StatsEnum.Combo_Dammages);
                                }
                                if (Bomb2.Owner[1].ID != Target.ID && !ArrayUtils.contains(FutureExplosedBombs, Bomb2.Owner[1])) {
                                    FutureExplosedBombs = (BombFighter[]) ArrayUtils.add(FutureExplosedBombs, Bomb2.Owner[1]);
                                    TotalCombo += Bomb2.Owner[1].Stats.GetBoost(StatsEnum.Combo_Dammages);
                                }
                            }
                        }
                    }
                    CastInfos.Caster.Fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + TotalCombo + "% dommages d'explosion"}));
                    Target.Stats.GetEffect(StatsEnum.Combo_Dammages).additionnal = TotalCombo;
                    for (BombFighter Bomb : FutureExplosedBombs) {
                        Bomb.Stats.GetEffect(StatsEnum.Combo_Dammages).additionnal = TotalCombo;
                        Bomb.TryDie(CastInfos.Caster.ID, true);
                    }
                }
                Target.TryDie(CastInfos.Caster.ID, true);
            }
        }
        return -1;
    }

}
