package koh.game.fights.effects;

import koh.game.Main;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffHeal;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsGainMessage;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectHeal extends EffectBase {

    public static int ApplyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal) {
        return ApplyHeal(CastInfos, Target, Heal, true);
    }

    public static int ApplyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal, boolean Calculate) {
        Fighter Caster = CastInfos.Caster;

        // Boost soin etc
        if (Calculate) {
            Caster.CalculHeal(Heal);
        }
        
        if (Target.Buffs.OnHealPostJet(CastInfos, Heal) == -3) {
            return -3; // Fin du combat
        }

        // Si le soin est superieur a sa vie actuelle
        if (Target.Life() + Heal.getValue() > Target.MaxLife()) {
            Heal.setValue(Target.MaxLife() - Target.Life());
            if (Heal.getValue() < 0) {
                Main.Logs().writeError("TargetMaxlife" + Target.MaxLife() + " TargettLife" + Target.Life());
            }
        }

        // Affectation
        Target.setLife(Target.Life() + Heal.getValue());

        // Envoi du packet
        Target.Fight.sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, Caster.ID, Target.ID, Math.abs(Heal.getValue())));

        // Le soin entraine la fin du combat ?
        return Target.TryDie(Caster.ID);
    }

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.Targets) {
                Target.Buffs.AddBuff(new BuffHeal(CastInfos, Target));
            }
        } else // Heal direct
        {
            for (Fighter Target : CastInfos.Targets) {
                if (EffectHeal.ApplyHeal(CastInfos, Target, new MutableInt(CastInfos.RandomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
