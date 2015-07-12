package koh.game.entities.spells;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class SpellLevel {

    public int id, spellId, spellBreed, ApCost, minRange, range;
    public byte grade, initialCooldown, minCastInterval;
    public boolean castInLine, castInDiagonal, castTestLos;
    public int criticalHitProbability, criticalFailureProbability;
    public boolean needFreeCell, needTakenCell, needFreeTrapCell, rangeCanBeBoosted;
    public int maxStack, maxCastPerTurn, maxCastPerTarget, globalCooldown, minPlayerLevel;
    public boolean criticalFailureEndsTurn, hideEffects, hidden;
    public int[] statesRequired, statesForbidden;
    public EffectInstanceDice[] effects, criticalEffect;

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
