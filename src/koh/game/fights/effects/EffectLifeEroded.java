package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffErosion;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifeAndShieldPointsLostMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReduceDamagesMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReflectDamagesMessage;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;

/**
 * Created by Melancholia on 1/8/16.
 */
public class EffectLifeEroded extends EffectBase {


    private static final HashMap<StatsEnum, StatsEnum> DAMAGE_EQ = new HashMap<StatsEnum, StatsEnum>() {
        {
            this.put(StatsEnum.LIFE_PERCENT_ERODED_NEUTRAL , StatsEnum.DAMAGE_NEUTRAL);
            this.put(StatsEnum.LIFE_PERCENT_ERODED_AIR     , StatsEnum.DAMAGE_AIR);
            this.put(StatsEnum.LIFE_PERCENT_ERODED_FIRE    , StatsEnum.DAMAGE_FIRE);
            this.put(StatsEnum.LIFE_PERCENT_ERODED_WATER   , StatsEnum.DAMAGE_WATER);
            this.put(StatsEnum.LIFE_PERCENT_ERODED_EARTH   , StatsEnum.DAMAGE_EARTH);
        }
    };

    @Override
    public int applyEffect(EffectCast castInfos) {
        int toReturn;
        for (Fighter target : castInfos.targets) {
            synchronized (target.temperoryLook) {
                final int totalErosion = target.getBuff()
                        .getAllBuffs()
                        .filter(buff -> buff instanceof BuffErosion)
                        .mapToInt(buff -> ((BuffErosion) buff).getScore())
                        .sum();
                final MutableInt damageJet = new MutableInt((totalErosion * castInfos.randomJet(target)) / 100.00f);

                if (castInfos.caster.hasState(FightStateEnum.PACIFISTE.value) && !castInfos.isGlyph) { //Une glyphe qui punit .. beh
                    damageJet.setValue(0);
                }

                // Calcul resistances
                target.computeReducedDamage(DAMAGE_EQ.get(castInfos.effectType), damageJet, castInfos.isCritical());
                // Reduction des dommages grace a l'armure
                if (damageJet.intValue() > 0) {
                    // Si ce n'est pas des dommages direct on ne reduit pas
                    if (!castInfos.isPoison && !castInfos.isReflect) {
                        // Calcul de l'armure par rapport a l'effet
                        int Armor = target.calculArmor(DAMAGE_EQ.get(castInfos.effectType));
                        // Si il reduit un minimum
                        if (Armor != 0) {
                            // XX Reduit les dommages de X

                            target.getFight().sendToField(new GameActionFightReduceDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_MODERATOR, target.getID(), target.getID(), Armor));

                            // On reduit
                            damageJet.setValue(damageJet.intValue() - Armor);

                            // Si on suprimme totalement les dommages
                            if (damageJet.intValue() < 0) {
                                damageJet.setValue(0);
                            }
                        }
                    }
                }
                // Application des buffs apres le calcul totaux et l'armure
                if (!castInfos.isPoison && !castInfos.isReflect) {
                    if (castInfos.caster.getBuff().onAttackAfterJet(castInfos, damageJet) == -3) {
                        return -3; // Fin du combat
                    }
                    if (target.getBuff().onAttackedAfterjet(castInfos, damageJet) == -3) {
                        return -3; // Fin du combat
                    }
                }

                // S'il subit des dommages
                if (damageJet.getValue() > 0) {
                    // Si c'est pas un poison ou un renvoi on applique le renvoie
                    if (!castInfos.isPoison && !castInfos.isReflect) {
                        MutableInt reflectDamage = new MutableInt(target.getReflectedDamage());

                        // Si du renvoi
                        if (reflectDamage.intValue() > 0 && target.getID() != castInfos.caster.getID()) {
                            target.getFight().sendToField(new GameActionFightReflectDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_REFLECTOR, target.getID(), castInfos.caster.getID()));

                            // Trop de renvois
                            if (reflectDamage.getValue() > damageJet.getValue()) {
                                reflectDamage.setValue(damageJet.getValue());
                            }

                            EffectCast subInfos = new EffectCast(StatsEnum.DAMAGE_BRUT, 0, (short) 0, 0, null, target, null, false, StatsEnum.NONE, 0, null);
                            subInfos.isReflect = true;

                            // Si le renvoi de dommage entraine la fin de combat on stop
                            if (EffectDamage.applyDamages(subInfos, castInfos.caster, reflectDamage) == -3) {
                                return -3;
                            }

                            // Dommage renvoyé
                            damageJet.add(-reflectDamage.intValue());
                        }
                    }
                }
                // Peu pas etre en dessous de 0
                if (damageJet.getValue() < 0) {
                    damageJet.setValue(0);
                }

                // Dommages superieur a la vie de la cible
                // Dommages superieur a la vie de la cible
                if (damageJet.getValue() > target.getLife() + target.getShieldPoints()) {
                    damageJet.setValue(target.getLife() + target.getShieldPoints());
                }

                // On verifie les point bouclier d'abord
                if (target.getShieldPoints() > 0) {
                    if (target.getShieldPoints() > damageJet.intValue()) {
                        target.setShieldPoints(target.getShieldPoints() - damageJet.getValue());
                        target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(DAMAGE_EQ.get(castInfos.effectType).value(), castInfos.caster.getID(), target.getID(), 0, 0, damageJet.intValue()));
                    } else {
                        int lifePointRemaining = damageJet.toInteger() - target.getShieldPoints();
                        target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(DAMAGE_EQ.get(castInfos.effectType).value(), castInfos.caster.getID(), target.getID(), lifePointRemaining, 0, target.getShieldPoints()));
                        target.setLife(target.getLife() - lifePointRemaining);
                        target.setShieldPoints(0);
                    }
                    toReturn = target.tryDie(castInfos.caster.getID());
                    if (toReturn != -1) {
                        return toReturn;
                    }
                    continue;
                }
                // Deduit la vie
                target.setLife(target.getLife() - damageJet.intValue());

                // Enois du packet combat subit des dommages
                if (damageJet.intValue() != 0) {
                    target.getFight().sendToField(new GameActionFightLifePointsLostMessage(DAMAGE_EQ.get(castInfos.effectType).value(), castInfos.caster.getID(), target.getID(), damageJet.intValue(), 0));
                }
                toReturn = target.tryDie(castInfos.caster.getID());
                if (toReturn != -1) {
                    return toReturn;
                }
            }
        } ;
        return -1;
    }


}
