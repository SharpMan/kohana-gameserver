package koh.game.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import koh.game.MySQL;
import koh.game.entities.mob.MonsterDrop;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.utils.Settings;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class MonsterDAO {

    public static HashMap<Integer, MonsterTemplate> Cache = new HashMap<>();

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from monster_templates", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new MonsterTemplate() {
                    {
                        Id = RS.getInt("id");
                        gfxId = RS.getInt("gfx_id");
                        race = RS.getInt("race");
                        grades = new MonsterGrade[0];
                        look = RS.getString("look");
                        useSummonSlot = RS.getBoolean("use_summon_slot");
                        useBombSlot = RS.getBoolean("use_bomb_slot");
                        canPlay = RS.getBoolean("can_play");
                        canTackle = RS.getBoolean("can_tackle");
                        isBoss = RS.getBoolean("is_boss");
                        drops = new MonsterDrop[0];
                        subareas = Enumerable.StringToIntArray(RS.getString("subareas"));
                        spells = Enumerable.StringToIntArray(RS.getString("spells"));
                        favoriteSubareaId = RS.getInt("favorite_subarea_id");
                        isMiniBoss = RS.getBoolean("is_mini_boss");
                        isQuestMonster = RS.getBoolean("is_quest_monster");
                        correspondingMiniBossId = RS.getInt("corresponding_mini_boss_id");
                        speedAdjust = RS.getInt("speed_adjust");
                        creatureBoneId = RS.getInt("creature_bone_id");
                        canBePushed = RS.getBoolean("can_be_pushed");
                        fastAnimsFun = RS.getBoolean("fast_anims_fun");
                        canSwitchPos = RS.getBoolean("can_switch_pos");
                        incompatibleIdols = Enumerable.StringToIntArray(RS.getString("incompatable_idols"));
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

    public static int FindDrops() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from monster_drops", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.get(RS.getInt("monster_id")).drops = ArrayUtils.add(Cache.get(RS.getInt("monster_id")).drops, new MonsterDrop() {
                    {
                        dropId = RS.getInt("drop_id");
                        monsterId = RS.getInt("monster_id");
                        objectId = RS.getInt("monster_id");
                        percentDropForGrade1 = RS.getDouble("percent_drop_for_grade1");
                        percentDropForGrade2 = RS.getDouble("percent_drop_for_grade2");
                        percentDropForGrade3 = RS.getDouble("percent_drop_for_grade3");
                        percentDropForGrade4 = RS.getDouble("percent_drop_for_grade4");
                        percentDropForGrade5 = RS.getDouble("percent_drop_for_grade5");
                        DropLimit = RS.getInt("drop_limit");
                        ProspectingLock = RS.getInt("prospecting_lock");
                        hasCriteria = RS.getBoolean("has_criteria");
                        criteria = RS.getString("criteria");
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

    public static int FindGrades() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from monster_grades ORDER by grade ASC", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.get(RS.getInt("monster_id")).grades = ArrayUtils.add(Cache.get(RS.getInt("monster_id")).grades, new MonsterGrade() {
                    {
                        Grade = RS.getByte("grade");
                        monsterId = RS.getInt("monster_id");
                        Level = RS.getInt("level");
                        lifePoints = RS.getInt("life_points");
                        actionPoints = RS.getInt("action_points");
                        movementPoints = RS.getInt("movement_points");
                        paDodge = RS.getInt("pa_dodge");
                        pmDodge = RS.getInt("pm_dodge");
                        Wisdom = RS.getInt("wisdom");
                        tackleEvade = RS.getInt("tackle_evade");
                        tackleBlock = RS.getInt("tackle_block");
                        Strenght = RS.getInt("strength");
                        Chance = RS.getInt("chance");
                        Intelligence = RS.getInt("intelligence");
                        Agility = RS.getInt("agility");
                        earthResistance = RS.getInt("earth_resistance");
                        airResistance = RS.getInt("air_resistance");
                        fireResistance = RS.getInt("fire_resistance");
                        waterResistance = RS.getInt("water_resistance");
                        neutralResistance = RS.getInt("neutral_resistance");
                        gradeXp = RS.getInt("grade_xp");
                        damageReflect = RS.getInt("damage_reflect");
                        hiddenLevel = RS.getInt("hidden_level");
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
