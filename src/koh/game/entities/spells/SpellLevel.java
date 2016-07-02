package koh.game.entities.spells;

import koh.utils.Enumerable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Neo-Craft
 */
public class SpellLevel {

    @Getter
    private int id, spellId, spellBreed, ApCost, minRange, range;
    @Getter
    private byte grade, initialCooldown, minCastInterval;
    @Getter
    private boolean castInLine, castInDiagonal, castTestLos;
    @Getter
    private int criticalHitProbability, criticalFailureProbability;
    @Getter
    private boolean needFreeCell, needTakenCell, needFreeTrapCell, rangeCanBeBoosted;
    @Getter
    private int maxStack, maxCastPerTurn, maxCastPerTarget, globalCooldown, minPlayerLevel;
    @Getter
    private boolean criticalFailureEndsTurn, hideEffects, hidden;
    @Getter
    private int[] statesRequired, statesForbidden;
    @Getter @Setter
    private EffectInstanceDice[] effects, criticalEffect;

    public SpellLevel(ResultSet result) throws SQLException {
        this.id = result.getInt("id");
        this.spellId = result.getInt("spell_id");
        this.grade = result.getByte("grade");
        this.spellBreed = result.getInt("spell_breed");
        this.ApCost = result.getInt("ap_cost");
        this.minRange = result.getInt("min_range");
        this.range = result.getInt("range");
        this.castInLine = result.getBoolean("cast_in_line");
        this.castInDiagonal = result.getBoolean("cast_in_diagonal");
        this.castTestLos = result.getBoolean("cast_test_los");
        this.criticalHitProbability = result.getInt("critical_hit_probability");
        this.criticalFailureProbability = result.getInt("critical_failure_probability");
        this.needFreeCell = result.getBoolean("need_free_cell");
        this.needTakenCell = result.getBoolean("need_taken_cell");
        this.needFreeTrapCell = result.getBoolean("need_free_trap_cell");
        this.rangeCanBeBoosted = result.getBoolean("range_can_be_boosted");
        this.maxStack = result.getInt("max_stack");
        this.maxCastPerTurn = result.getInt("max_cast_per_turn");
        this.maxCastPerTarget = result.getInt("max_cast_per_target");
        this.minCastInterval = result.getByte("min_cast_interval");
        this.initialCooldown = result.getByte("initial_cooldown");
        this.globalCooldown = result.getInt("global_cooldown");
        this.minPlayerLevel = result.getInt("min_player_level");
        this.criticalFailureEndsTurn = result.getBoolean("critical_failure_ends_turn");
        this.hideEffects = result.getBoolean("hide_effects");
        this.hidden = result.getBoolean("hidden");
        this.statesRequired = Enumerable.stringToIntArray(result.getString("states_required"));
        this.statesForbidden = Enumerable.stringToIntArray(result.getString("states_forbidden"));

        {
            IoBuffer buf = IoBuffer.wrap(result.getBytes("effects"));
            this.effects = new EffectInstanceDice[buf.getInt()];
            for (int i = 0; i < this.effects.length; i++) {
                this.effects[i] = new EffectInstanceDice(buf);
                if (this.spellId == 126) {//To patch in DAO After
                    this.effects[i].targetMask = "a";
                }
                /*if(this.emoteId == 5589 && this.grade == 6){
                    System.out.println(this.effects[i].toString());
                }*/
                            /* if(this.effects[i].effectId == 165)
                             System.out.println("hn"+this.emoteId);*/
            }
            buf.clear();

            buf = IoBuffer.wrap(result.getBytes("critical_effects"));
            this.criticalEffect = new EffectInstanceDice[buf.getInt()];
            for (int i = 0; i < this.criticalEffect.length; i++) {
                this.criticalEffect[i] = new EffectInstanceDice(buf);
            }
        }

    }


    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
