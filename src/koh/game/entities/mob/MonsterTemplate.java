package koh.game.entities.mob;

import koh.look.EntityLookParser;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Enumerable;
import lombok.Getter;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Neo-Craft
 */
public class MonsterTemplate {

    @Getter
    private int Id;
    @Getter
    private String nameId,look;
    @Getter
    private int gfxId, race;
    @Getter
    private final ArrayList<MonsterGrade> grades = new ArrayList<>(6);
    @Getter
    private boolean useSummonSlot, useBombSlot, canPlay,canTackle, isBoss;
    @Getter
    private final ArrayList<MonsterDrop> drops = new ArrayList<>(5);
    @Getter
    private int[] subareas, spells;
    @Getter
    private int favoriteSubareaId;
    @Getter
    private boolean isMiniBoss, isQuestMonster;
    @Getter
    private int correspondingMiniBossId, speedAdjust, creatureBoneId;
    @Getter
    private boolean canBePushed, fastAnimsFun, canSwitchPos;
    @Getter
    private int[] incompatibleIdols;

    private EntityLook myEntityLook;

    public MonsterTemplate(ResultSet result) throws SQLException {
        Id = result.getInt("id");
        gfxId = result.getInt("gfx_id");
        race = result.getInt("race");
        look = result.getString("look");
        useSummonSlot = result.getBoolean("use_summon_slot");
        useBombSlot = result.getBoolean("use_bomb_slot");
        canPlay = result.getBoolean("can_play");
        canTackle = result.getBoolean("can_tackle");
        isBoss = result.getBoolean("is_boss");
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



    public MonsterGrade getRandomGrade(SecureRandom rand){
        return this.grades.get(rand.nextInt(this.grades.size()));
    }

    public MonsterGrade getLevelOrNear(int Level) {
        int Near = 10000;
        MonsterGrade objNear = null;
        for (MonsterGrade objLevel : grades) {
            if (objLevel.getGrade() == Level) {
                return objLevel;
            } else {
                int Diff = Math.abs(objLevel.getGrade() - Level);
                if (Near > Diff) {
                    Near = Diff;
                    objNear = objLevel;
                }
            }
        }
        return objNear;
    }


    public EntityLook getEntityLook() {
        if (myEntityLook == null) {
            myEntityLook = EntityLookParser.fromString(this.look);
        }
        return myEntityLook;
    }

}
