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
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from spells", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                spells.put(result.getInt("id"), new Spell(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return spells.size();
    }

    private int loadAllBombs() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from spell_bombs", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                bombs.put(result.getInt("id"), new SpellBomb() { //d2o no refact then
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
                levels.put(result.getInt("id"), new SpellLevel(result));
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
                learnableSpells.get(result.getInt("breed_id")).add(new LearnableSpell(result));
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public SpellBomb findBomb(int id) { return this.bombs.get(id); }

    @Override
    public SpellLevel findLevel(int id) { return this.levels.get(id); }

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
