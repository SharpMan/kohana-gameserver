package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffHealPercent;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsGainMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectHealPercent extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.Targets) {
                Target.Buffs.AddBuff(new BuffHealPercent(CastInfos, Target));
            }
        } else // Heal direct
        {
            for (Fighter Target : CastInfos.Targets) {
                if (EffectHealPercent.ApplyHealPercent(CastInfos, Target, CastInfos.RandomJet(Target)) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int ApplyHealPercent(EffectCast CastInfos, Fighter Target, int Heal) {
        Fighter Caster = CastInfos.Caster;

        // Boost soin etc
        Heal = Heal * (Target.Life() / 100);

        // Si le soin est superieur a sa vie actuelle
        if ((Target.Life() + Heal) > Target.MaxLife()) {
            Heal = Target.MaxLife() - Target.Life();
        }

        if (Heal < 0) {
            Heal = 0;
        }

        // Affectation
        Target.setLife(Target.Life() + Heal);

        // Envoi du packet
        if (Heal != 0) {
            Target.Fight.sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, Caster.ID, Target.ID, Heal));
        }

        // Le soin entraine la fin du combat ?
        return Target.TryDie(Caster.ID);
    }

}
