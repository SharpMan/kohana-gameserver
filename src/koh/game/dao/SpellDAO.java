package koh.game.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.d2o.entities.SpellBomb;
import koh.game.MySQL;
import koh.game.entities.spells.*;
import koh.game.utils.Settings;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class SpellDAO {

    public static final Map<Integer, SpellLevel> LevelCache = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, Spell> Spells = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, ArrayList<LearnableSpell>> LearnableSpells = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, SpellBomb> Bombs = Collections.synchronizedMap(new HashMap<>());

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from spells", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Spells.put(RS.getInt("id"), new Spell() {
                    {
                        id = RS.getInt("id");
                        typeId = RS.getInt("type_id");
                        iconId = RS.getInt("icon_id");
                        verbose_casttype = RS.getBoolean("verbose_cast");
                        spellLevels = new SpellLevel[RS.getString("spell_levels").split(",").length];
                        for (int i = 0; i < RS.getString("spell_levels").split(",").length; i++) {
                            this.spellLevels[i] = LevelCache.get(Integer.parseInt(RS.getString("spell_levels").split(",")[i]));
                        }
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindBombs() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from spell_bombs", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Bombs.put(RS.getInt("id"), new SpellBomb() {
                    {
                        Id = RS.getInt("id");
                        chainReactionSpellId = RS.getInt("chain_reaction_spellId");
                        explodSpellId = RS.getInt("explod_spell_id");
                        wallId = RS.getInt("wall_id");
                        instantSpellId = RS.getInt("instant_spell_id");
                        comboCoeff = RS.getInt("combo_coeff");
                        wallSpellId = RS.getInt("wall_spell_id");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindLevels() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from spell_levels", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                LevelCache.put(RS.getInt("id"), new SpellLevel() {
                    {
                        this.id = RS.getInt("id");
                        this.spellId = RS.getInt("spell_id");
                        this.grade = RS.getByte("grade");
                        this.spellBreed = RS.getInt("spell_breed");
                        this.ApCost = RS.getInt("ap_cost");
                        this.minRange = RS.getInt("min_range");
                        this.range = RS.getInt("range");
                        this.castInLine = RS.getBoolean("cast_in_line");
                        this.castInDiagonal = RS.getBoolean("cast_in_diagonal");
                        this.castTestLos = RS.getBoolean("cast_test_los");
                        this.criticalHitProbability = RS.getInt("critical_hit_probability");
                        this.criticalFailureProbability = RS.getInt("critical_failure_probability");
                        this.needFreeCell = RS.getBoolean("need_free_cell");
                        this.needTakenCell = RS.getBoolean("need_taken_cell");
                        this.needFreeTrapCell = RS.getBoolean("need_free_trap_cell");
                        this.rangeCanBeBoosted = RS.getBoolean("range_can_be_boosted");
                        this.maxStack = RS.getInt("max_stack");
                        this.maxCastPerTurn = RS.getInt("max_cast_per_turn");
                        this.maxCastPerTarget = RS.getInt("max_cast_per_target");
                        this.minCastInterval = RS.getByte("min_cast_interval");
                        this.initialCooldown = RS.getByte("initial_cooldown");
                        this.globalCooldown = RS.getInt("global_cooldown");
                        this.minPlayerLevel = RS.getInt("min_player_level");
                        this.criticalFailureEndsTurn = RS.getBoolean("critical_failure_ends_turn");
                        this.hideEffects = RS.getBoolean("hide_effects");
                        this.hidden = RS.getBoolean("hidden");

                        if (!RS.getString("states_required").isEmpty()) {
                            this.statesRequired = new int[RS.getString("states_required").split(",").length];
                            for (int i = 0; i < RS.getString("states_required").split(",").length; i++) {
                                this.statesRequired[i] = Integer.parseInt(RS.getString("states_required").split(",")[i]);
                            }
                        } else {
                            this.statesRequired = new int[0];
                        }
                        if (!RS.getString("states_forbidden").isEmpty()) {
                            this.statesForbidden = new int[RS.getString("states_forbidden").split(",").length];
                            for (int i = 0; i < RS.getString("states_forbidden").split(",").length; i++) {
                                this.statesForbidden[i] = Integer.parseInt(RS.getString("states_forbidden").split(",")[i]);
                            }
                        } else {
                            this.statesForbidden = new int[0];
                        }
                        IoBuffer buf = IoBuffer.wrap(RS.getBytes("effects"));
                        this.effects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.effects.length; i++) {
                            this.effects[i] = new EffectInstanceDice(buf);
                            /*if(this.spellId == 5325)
                                System.out.println(this.effects[i]);*/
                        }
                        buf.clear();

                        buf = IoBuffer.wrap(RS.getBytes("critical_effects"));
                        this.criticalEffect = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.criticalEffect.length; i++) {
                            this.criticalEffect[i] = new EffectInstanceDice(buf);
                        }
                        buf.clear();
                        buf = null;
                        
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindLearnables() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from learnable_spells order by obtain_level ASC,spell ASC", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                if (!LearnableSpells.containsKey(RS.getInt("breed_id"))) {
                    LearnableSpells.put(RS.getInt("breed_id"), new ArrayList<>());
                }
                LearnableSpells.get(RS.getInt("breed_id")).add(new LearnableSpell() {
                    {
                        ID = RS.getInt("id");
                        Spell = RS.getInt("spell");
                        ObtainLevel = RS.getInt("obtain_level");
                        BreedID = RS.getInt("breed_id");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
