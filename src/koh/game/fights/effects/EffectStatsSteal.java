package koh.game.fights.effects;

import java.util.HashMap;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDodgePointLossMessage;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectStatsSteal extends EffectBase {

    public static final HashMap<StatsEnum, StatsEnum> TARGET_MALUS = new HashMap<StatsEnum, StatsEnum>() {
        {
            this.put(StatsEnum.STEAL_VITALITY, StatsEnum.SUB_VITALITY);
            this.put(StatsEnum.STEAL_STRENGTH, StatsEnum.SUB_STRENGTH);
            this.put(StatsEnum.STEAL_INTELLIGENCE, StatsEnum.SUB_INTELLIGENCE);
            this.put(StatsEnum.STEAL_AGILITY, StatsEnum.SUB_AGILITY);
            this.put(StatsEnum.STEAL_WISDOM, StatsEnum.SUB_WISDOM);
            this.put(StatsEnum.STEAL_CHANCE, StatsEnum.SUB_CHANCE);
            this.put(StatsEnum.STEAL_PA, StatsEnum.SUB_PA);
            this.put(StatsEnum.STEAL_PM, StatsEnum.SUB_PM);
            this.put(StatsEnum.STEAL_RANGE, StatsEnum.SUB_RANGE);
        }
    };

    public static final HashMap<StatsEnum, StatsEnum> CASTER_BONUS = new HashMap<StatsEnum, StatsEnum>() {
        {
            this.put(StatsEnum.STEAL_VITALITY, StatsEnum.VITALITY);
            this.put(StatsEnum.STEAL_STRENGTH, StatsEnum.STRENGTH);
            this.put(StatsEnum.STEAL_INTELLIGENCE, StatsEnum.INTELLIGENCE);
            this.put(StatsEnum.STEAL_AGILITY, StatsEnum.AGILITY);
            this.put(StatsEnum.STEAL_WISDOM, StatsEnum.WISDOM);
            this.put(StatsEnum.STEAL_CHANCE, StatsEnum.CHANCE);
            this.put(StatsEnum.STEAL_PA, StatsEnum.ACTION_POINTS);
            this.put(StatsEnum.STEAL_PM, StatsEnum.MOVEMENT_POINTS);
            this.put(StatsEnum.STEAL_RANGE, StatsEnum.ADD_RANGE);
        }
    };

    @Override
    public int applyEffect(EffectCast castInfos) {
        StatsEnum malusType = TARGET_MALUS.get(castInfos.effectType);
        StatsEnum bonusType = CASTER_BONUS.get(castInfos.effectType);

        EffectCast malusInfos = new EffectCast(malusType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);
        EffectCast bonusInfos = new EffectCast(bonusType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration <= 0 ? castInfos.duration : castInfos.duration - 1, 0);
        MutableInt damageValue = new MutableInt();

        for (Fighter target : castInfos.targets) {
            if (target == castInfos.caster) {
                continue;
            }

            if(bonusType == StatsEnum.MOVEMENT_POINTS || bonusType == StatsEnum.ACTION_POINTS){
                if(malusInfos.effect == null){ //Old loop
                    malusInfos.effect = bonusInfos.effect = castInfos.effect;
                }
                final int jet = castInfos.randomJet(target);
                malusInfos.damageValue = target.calculDodgeAPMP(castInfos.caster, jet, (bonusType == StatsEnum.MOVEMENT_POINTS), true);
                bonusInfos.damageValue = malusInfos.damageValue;
                malusInfos.effect = bonusInfos.effect = null;
                if (malusInfos.damageValue != jet) {
                    target.getFight().sendToField(new GameActionFightDodgePointLossMessage(bonusType == StatsEnum.MOVEMENT_POINTS ? ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PM : ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PA, castInfos.caster.getID(), target.getID(), jet - castInfos.damageValue));
                    if(malusInfos.damageValue == 0){ //have succesfully esquivate all
                        continue;
                    }
                }
            }

            // Malus a la cible
            BuffStats buffStats = new BuffStats(malusInfos, target);
            if (!target.getBuff().buffMaxStackReached(buffStats)) {
                if (buffStats.applyEffect(damageValue, null) == -3) {
                    return -3;
                }

                target.getBuff().addBuff(buffStats);
            }


            // Bonus au lanceur
            buffStats = new BuffStats(bonusInfos, castInfos.caster);
            if (!castInfos.caster.getBuff().buffMaxStackReached(buffStats)) {
                if (buffStats.applyEffect(damageValue, null) == -3) {
                    return -3;
                }

                castInfos.caster.getBuff().addBuff(buffStats);

            }

        }

        return -1;
    }

}
