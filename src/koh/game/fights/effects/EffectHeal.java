package koh.game.fights.effects;

import koh.game.entities.fight.Challenge;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffHeal;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsGainMessage;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class EffectHeal extends EffectBase {


    private static final Logger logger = LogManager.getLogger(EffectHeal.class);

    public static int applyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal) {
        return applyHeal(CastInfos, Target, Heal, true);
    }

    public static int applyHeal(EffectCast castInfos, Fighter target, MutableInt heal, boolean calculate) {
        if(target.hasState(FightStateEnum.INSOIGNABLE.value)){
            return -1;
        }

        // boost soin etc

        if(castInfos.spellId == 199){
            heal.add(0.01f * heal.getValue() * ( (target.getStats().getTotal(StatsEnum.INTELLIGENCE)  + castInfos.caster.getStats().getTotal(StatsEnum.ADD_HEAL_BONUS))));
        }
        else if (calculate) {
            castInfos.caster.calculheal(heal);
        }
        
        if (target.getBuff().onHealPostJet(castInfos, heal) == -3) {
            return -3; // Fin du combat
        }

        // Si le soin est superieur a sa vie actuelle
        if (target.getLife() + heal.getValue() > target.getMaxLife()) {
            heal.setValue(target.getMaxLife() - target.getLife());
            if (heal.getValue() < 0) {
                logger.debug("TargetMaxlife {} TargettLife {}" ,target.getMaxLife(), target.getLife());
                heal.setValue(0);
            }

        }

        try {
            for (Challenge challenge : target.getFight().getChallenges().values()) {
                challenge.onFighterHealed(target, castInfos, heal.intValue());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // Affectation
        target.setLife(target.getLife() + heal.getValue());

        // Envoi du packet
        if(heal.getValue() != 0)
            target.getFight().sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, castInfos.caster.getID(), target.getID(), Math.abs(heal.getValue())));

        // Le soin entraine la fin du combat ?
        return target.tryDie(castInfos.caster.getID());
    }

    @Override
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(new BuffHeal(castInfos, Target));
            }
        } else // HEAL direct
        {
            for (Fighter Target : castInfos.targets) {
                if (EffectHeal.applyHeal(castInfos, Target, new MutableInt(castInfos.randomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
