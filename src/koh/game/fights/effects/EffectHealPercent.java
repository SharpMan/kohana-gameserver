package koh.game.fights.effects;

import koh.game.fights.effects.buff.BuffHealPercent;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
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
                if (EffectHealPercent.applyHealPercent(castInfos, Target, castInfos.randomJet(Target)) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int applyHealPercent(EffectCast castInfos, Fighter target, int heal) {
        if(target.hasState(FightStateEnum.INSOIGNABLE.value)){
            return -1;
        }
        final Fighter caster = castInfos.caster;

        // boost soin etc
        heal = heal * (castInfos.spellId == 131 ? target.getLife() : target.getMaxLife()) / 100;
        heal += caster.getStats().getTotal(StatsEnum.ADD_HEAL_BONUS);
        heal *= caster.getPortalsSpellEfficiencyHealBonus(castInfos.oldCell, caster.getFight());

        // Si le soin est superieur a sa vie actuelle
        if ((target.getLife() + heal) > target.getMaxLife()) {
            heal = target.getMaxLife() - target.getLife();
        }

        if (heal < 0) {
            heal = 0;
        }

        // Affectation
        target.setLife(target.getLife() + heal);

        // Envoi du packet
        if (heal != 0) {
            target.getFight().sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, caster.getID(), target.getID(), heal));
        }

        // Le soin entraine la fin du combat ?
        return target.tryDie(caster.getID());
    }

}
