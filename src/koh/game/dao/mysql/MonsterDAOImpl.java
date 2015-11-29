package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MonsterDAO;
import koh.game.entities.mob.MonsterDrop;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Neo-Craft
 */
public class MonsterDAOImpl extends MonsterDAO {

    private static final Logger logger = LogManager.getLogger(MonsterDAOImpl.class);
    private static HashMap<Integer, MonsterTemplate> templates = new HashMap<>(3000);
    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.put(result.getInt("id"), new MonsterTemplate() {
                    {
                        Id = result.getInt("id");
                        gfxId = result.getInt("gfx_id");
                        race = result.getInt("race");
                        grades = new MonsterGrade[0];
                        look = result.getString("look");
                        useSummonSlot = result.getBoolean("use_summon_slot");
                        useBombSlot = result.getBoolean("use_bomb_slot");
                        canPlay = result.getBoolean("can_play");
                        canTackle = result.getBoolean("can_tackle");
                        isBoss = result.getBoolean("is_boss");
                        drops = new MonsterDrop[0];
                        subareas = Enumerable.StringToIntArray(result.getString("subareas"));
                        spells = Enumerable.StringToIntArray(result.getString("spells"));
                        favoriteSubareaId = result.getInt("favorite_subarea_id");
                        isMiniBoss = result.getBoolean("is_mini_boss");
                        isQuestMonster = result.getBoolean("is_quest_monster");
                        correspondingMiniBossId = result.getInt("corresponding_mini_boss_id");
                        speedAdjust = result.getInt("speed_adjust");
                        creatureBoneId = result.getInt("creature_bone_id");
                        canBePushed = result.getBoolean("can_be_pushed");
                        fastAnimsFun = result.getBoolean("fast_anims_fun");
                        canSwitchPos = result.getBoolean("can_switch_pos");
                        incompatibleIdols = Enumerable.StringToIntArray(result.getString("incompatable_idols"));
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

    private int loadAllDrops() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_drops", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.get(result.getInt("monster_id")).drops = ArrayUtils.add(templates.get(result.getInt("monster_id")).drops, new MonsterDrop() {
                    {
                        dropId = result.getInt("drop_id");
                        monsterId = result.getInt("monster_id");
                        objectId = result.getInt("monster_id");
                        percentDropForGrade1 = result.getDouble("percent_drop_for_grade1");
                        percentDropForGrade2 = result.getDouble("percent_drop_for_grade2");
                        percentDropForGrade3 = result.getDouble("percent_drop_for_grade3");
                        percentDropForGrade4 = result.getDouble("percent_drop_for_grade4");
                        percentDropForGrade5 = result.getDouble("percent_drop_for_grade5");
                        DropLimit = result.getInt("drop_limit");
                        ProspectingLock = result.getInt("prospecting_lock");
                        hasCriteria = result.getBoolean("has_criteria");
                        criteria = result.getString("criteria");
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

    private int loadAllGrades() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_grades ORDER by grade ASC", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.get(result.getInt("monster_id")).grades = ArrayUtils.add(templates.get(result.getInt("monster_id")).grades, new MonsterGrade() {
                    {
                        Grade = result.getByte("grade");
                        monsterId = result.getInt("monster_id");
                        Level = result.getInt("level");
                        lifePoints = result.getInt("life_points");
                        actionPoints = result.getInt("action_points");
                        movementPoints = result.getInt("movement_points");
                        paDodge = result.getInt("pa_dodge");
                        pmDodge = result.getInt("pm_dodge");
                        Wisdom = result.getInt("wisdom");
                        tackleEvade = result.getInt("tackle_evade");
                        tackleBlock = result.getInt("tackle_block");
                        Strenght = result.getInt("strength");
                        Chance = result.getInt("chance");
                        Intelligence = result.getInt("intelligence");
                        Agility = result.getInt("agility");
                        earthResistance = result.getInt("earth_resistance");
                        airResistance = result.getInt("air_resistance");
                        fireResistance = result.getInt("fire_resistance");
                        waterResistance = result.getInt("water_resistance");
                        neutralResistance = result.getInt("neutral_resistance");
                        gradeXp = result.getInt("grade_xp");
                        damageReflect = result.getInt("damage_reflect");
                        hiddenLevel = result.getInt("hidden_level");
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
    public void start() {
        logger.info("Loaded {} template monster", this.loadAll());
        logger.info("Loaded {} template grades", this.loadAllGrades());
        logger.info("Loaded {} template drops", this.loadAllGrades());
    }

    @Override
    public void stop() {

    }
}
