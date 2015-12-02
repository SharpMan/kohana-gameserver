package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import koh.d2o.entities.SpellBomb;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.SpellDAO;
import koh.game.entities.spells.*;
import koh.game.utils.sql.ConnectionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class SpellDAOImpl extends SpellDAO {

    private final Map<Integer, SpellLevel> levels = Collections.synchronizedMap(new HashMap<>(16000));
    private final Map<Integer, Spell> spells = Collections.synchronizedMap(new HashMap<>(5000));
    private final Map<Integer, ArrayList<LearnableSpell>> learnableSpells = Collections.synchronizedMap(new HashMap<>(1179));
    private final Map<Integer, SpellBomb> bombs = Collections.synchronizedMap(new HashMap<>(5));

    private static final Logger logger = LogManager.getLogger(AreaDAOImpl.class);

    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from spells", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                spells.put(result.getInt("id"), new Spell() {
                    {
                        id = result.getInt("id");
                        typeId = result.getInt("type_id");
                        iconId = result.getInt("icon_id");
                        verbose_casttype = result.getBoolean("verbose_cast");
                        spellLevels = new SpellLevel[result.getString("spell_levels").split(",").length];
                        for (int i = 0; i < result.getString("spell_levels").split(",").length; i++) {
                            this.spellLevels[i] = levels.get(Integer.parseInt(result.getString("spell_levels").split(",")[i]));

                        }
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllBombs() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from spell_bombs", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                bombs.put(result.getInt("id"), new SpellBomb() {
                    {
                        Id = result.getInt("id");
                        chainReactionSpellId = result.getInt("chain_reaction_spellId");
                        explodSpellId = result.getInt("explod_spell_id");
                        wallId = result.getInt("wall_id");
                        instantSpellId = result.getInt("instant_spell_id");
                        comboCoeff = result.getInt("combo_coeff");
                        wallSpellId = result.getInt("wall_spell_id");
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadLevels() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from spell_levels", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                levels.put(result.getInt("id"), new SpellLevel() {
                    {
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

                        if (!result.getString("states_required").isEmpty()) {
                            this.statesRequired = new int[result.getString("states_required").split(",").length];
                            for (int i = 0; i < result.getString("states_required").split(",").length; i++) {
                                this.statesRequired[i] = Integer.parseInt(result.getString("states_required").split(",")[i]);
                            }
                        } else {
                            this.statesRequired = new int[0];
                        }
                        if (!result.getString("states_forbidden").isEmpty()) {
                            this.statesForbidden = new int[result.getString("states_forbidden").split(",").length];
                            for (int i = 0; i < result.getString("states_forbidden").split(",").length; i++) {
                                this.statesForbidden[i] = Integer.parseInt(result.getString("states_forbidden").split(",")[i]);
                            }
                        } else {
                            this.statesForbidden = new int[0];
                        }
                        IoBuffer buf = IoBuffer.wrap(result.getBytes("effects"));
                        this.effects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.effects.length; i++) {
                            this.effects[i] = new EffectInstanceDice(buf);
                            if (this.spellId == 126) {//To patch in DAO After
                                this.effects[i].targetMask = "a";
                            }
                            /* if(this.effects[i].effectId == 165)
                             System.out.println("hn"+this.spellId);*/
                        }
                        buf.clear();

                        buf = IoBuffer.wrap(result.getBytes("critical_effects"));
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
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

   private int loadAllLearnables() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from learnable_spells order by obtain_level ASC,spell ASC", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                if (!learnableSpells.containsKey(result.getInt("breed_id"))) {
                    learnableSpells.put(result.getInt("breed_id"), new ArrayList<>());
                }
                learnableSpells.get(result.getInt("breed_id")).add(new LearnableSpell() {
                    {
                        ID = result.getInt("id");
                        Spell = result.getInt("spell");
                        obtainLevel = result.getInt("obtain_level");
                        BreedID = result.getInt("breed_id");
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public Spell findSpell(int id)
    {
        return this.spells.get(id);
    }

    @Override
    public ArrayList<LearnableSpell> findLearnableSpell(int id) {
        return this.learnableSpells.get(id);
    }

    @Override
    public void start() {
        logger.info("loaded {} spell levels",this.loadLevels());
        logger.info("loaded {} spells",this.loadAll());
        logger.info("loaded {} spell bombs",this.loadAllBombs());
        logger.info("loaded {} learnable spells",this.loadAllLearnables());
    }

    @Override
    public void stop() {

    }


}
