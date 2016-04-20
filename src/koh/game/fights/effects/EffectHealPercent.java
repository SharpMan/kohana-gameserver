package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffHealPercent;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsGainMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectHealPercent extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(new BuffHealPercent(castInfos, Target));
            }
        } else // HEAL direct
        {
            for (Fighter Target : castInfos.targets) {
                if (EffectHealPercent.ApplyHealPercent(castInfos, Target, castInfos.randomJet(Target)) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int ApplyHealPercent(EffectCast CastInfos, Fighter Target, int heal) {
        Fighter Caster = CastInfos.caster;

        // boost soin etc
        heal = heal * (Target.getLife() / 100);

        // Si le soin est superieur a sa vie actuelle
        if ((Target.getLife() + heal) > Target.getMaxLife()) {
            heal = Target.getMaxLife() - Target.getLife();
        }

        if (heal < 0) {
            heal = 0;
        }

        // Affectation
        Target.setLife(Target.getLife() + heal);

        // Envoi du packet
        if (heal != 0) {
            Target.getFight().sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, Caster.getID(), Target.getID(), heal));
        }

        // Le soin entraine la fin du combat ?
        return Target.tryDie(Caster.getID());
    }

}
