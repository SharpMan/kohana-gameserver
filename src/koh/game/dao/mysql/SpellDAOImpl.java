package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.google.inject.Inject;
import koh.d2o.entities.SpellBomb;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.SpellDAO;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.spells.*;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.client.enums.SpellTargetType;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.ArrayUtils;
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

                if(result.getInt("spell_id") == 5590) { //sadi
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        effect.targetMask = "a";
                    }
                }
                if(result.getInt("spell_id") == 5492){ //synchro
                    levels.get(result.getInt("id")).setMaxCastPerTurn(1);
                }
                if(result.getInt("spell_id") == 90) { //fuite
                    EffectInstanceDice[] effects = levels.get(result.getInt("id")).getEffects();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        if(effect.effectId == 1160){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }else if(effect.effectId == 1100){
                            effect.targetMask = "a";
                        }
                    }
                    levels.get(result.getInt("id")).setEffects(effects);
                    effects = levels.get(result.getInt("id")).getCriticalEffect();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getCriticalEffect()) {
                        if(effect.effectId == 1160){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }else if(effect.effectId == 1100){
                            effect.targetMask = "a";
                        }
                    }
                    levels.get(result.getInt("id")).setCriticalEffect(effects);
                }

                //rauleback
                if(result.getInt("spell_id") == 424) { //telepor | momi
                    EffectInstanceDice[] effects = levels.get(result.getInt("id")).getEffects();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        if(effect.effectId != 1100){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setEffects(effects);
                    effects = levels.get(result.getInt("id")).getCriticalEffect();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getCriticalEffect()) {
                        if(effect.effectId != 1100){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setCriticalEffect(effects);
                }

                if(result.getInt("spell_id") == 88 || result.getInt("spell_id") == 99) { //telepor | momi
                    EffectInstanceDice[] effects = levels.get(result.getInt("id")).getEffects();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        if(effect.effectId == 1160){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setEffects(effects);
                    effects = levels.get(result.getInt("id")).getCriticalEffect();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getCriticalEffect()) {
                        if(effect.effectId == 1160){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setCriticalEffect(effects);
                }

                if(result.getInt("spell_id") == 87) { //demotivation
                    EffectInstanceDice[] effects = levels.get(result.getInt("id")).getEffects();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        if(effect.effectId == 1160 && effect.diceNum == 5429){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setEffects(effects);
                    effects = levels.get(result.getInt("id")).getCriticalEffect();
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getCriticalEffect()) {
                        if(effect.effectId == 1160 && effect.diceNum == 5429){
                            effects = ArrayUtils.removeElement(effects,effect);
                        }
                    }
                    levels.get(result.getInt("id")).setCriticalEffect(effects);
                }

                if(result.getInt("id") == 3030){ //untrucalacon
                    final EffectInstanceDice[] effects = new EffectInstanceDice[]{
                      Arrays.stream(levels.get(result.getInt("id")).getEffects()).filter(e -> e.effectId == 1099).findFirst().get(),
                      new EffectInstanceDice(new EffectInstance(131900, 950,0, "a", 1, 0,0, "C63,", 0, "I", false, true, true) , 7, 0,0)
                    };
                    levels.get(406).setEffects(effects);
                    levels.get(406).setCriticalEffect(effects);
                    levels.get(407).setEffects(effects);
                    levels.get(407).setCriticalEffect(effects);
                    levels.get(408).setEffects(effects);
                    levels.get(408).setCriticalEffect(effects);
                    levels.get(409).setEffects(effects);
                    levels.get(409).setCriticalEffect(effects);
                    levels.get(410).setEffects(effects);
                    levels.get(410).setCriticalEffect(effects);
                    levels.get(3030).setEffects(effects);
                    levels.get(3030).setCriticalEffect(effects);
                }


                /*if(result.getInt("spell_id") == 99) { //5486
                    System.out.println(result.getInt("id"));
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        System.out.println(effect.toString());
                    }
                }*/

                //5570
                /*if(result.getInt("spell_id") == 1071) {
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        System.out.println(effect.toString());
                    }
                }*/


                //A,K,
              /* Arrays.stream(levels.get(result.getInt("id")).getEffects())
                       .filter(x -> x.effectId == 666)
                       .forEach(x -> {
                           try {
                               System.out.println(result.getInt("spell_id"));
                           } catch (SQLException e) {
                               e.printStackTrace();
                           }
                       });
                if(result.getInt("spell_id") == 114){
                    for (EffectInstanceDice effect : levels.get(result.getInt("id")).getEffects()) {
                        System.out.println(effect.toString());
                        /*if(effect.effectId != 406){
                            levels.get(result.getInt("id")).setEffects(ArrayUtils.removeElement(levels.get(result.getInt("id")).getEffects(),effect));
                            levels.get(result.getInt("id")).setCriticalEffect(ArrayUtils.removeElement(levels.get(result.getInt("id")).getCriticalEffect(),effect));

                            effect.delay = 0;
                            System.out.println(effect.toString());
                        }*/
                    }
                    /*try (ConnectionStatement<PreparedStatement> connn = dbSource.prepareStatement("UPDATE `spell_levels` set effects= ?,critical_effects =?  WHERE id = ?;")) {
                        PreparedStatement pStatemente =  connn.getStatement();
                        pStatemente.setBytes(1, EffectHelper.serializeEffectInstanceDice(levels.get(result.getInt("id")).getEffects()).array());
                        pStatemente.setBytes(2, EffectHelper.serializeEffectInstanceDice(levels.get(result.getInt("id")).getCriticalEffect()).array());
                        pStatemente.setInt(3,result.getInt("id"));

                        pStatemente.execute();
                    }

                    catch (Exception e) {
                        logger.error(e);
                        logger.warn(e.getMessage());
                    }
                }
                i++;
            }*/
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
