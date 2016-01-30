package koh.game.entities.mob;

import koh.game.dao.DAO;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.spells.SpellLevel;
import koh.protocol.client.enums.StatsEnum;
import koh.utils.Enumerable;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Neo-Craft
 */
public class MonsterGrade {


    private ArrayList<SpellLevel> spells;
    @Getter
    private byte grade;
    @Getter
    private int monsterId, level, lifePoints, actionPoints, movementPoints, paDodge, pmDodge;
    @Getter
    private int wisdom, tackleEvade, tackleBlock, strenght, chance, intelligence, agility, earthResistance, airResistance, fireResistance, waterResistance, neutralResistance;
    @Getter
    private int gradeXp, damageReflect, hiddenLevel;
    private GenericStats myStats;


    public MonsterGrade(ResultSet result) throws SQLException {
        grade = result.getByte("grade");
        monsterId = result.getInt("monster_id");
        level = result.getInt("level");
        lifePoints = result.getInt("life_points");
        actionPoints = result.getInt("action_points");
        movementPoints = result.getInt("movement_points");
        paDodge = result.getInt("pa_dodge");
        pmDodge = result.getInt("pm_dodge");
        wisdom = result.getInt("wisdom");
        tackleEvade = result.getInt("tackle_evade");
        tackleBlock = result.getInt("tackle_block");
        strenght = result.getInt("strength");
        chance = result.getInt("chance");
        intelligence = result.getInt("intelligence");
        agility = result.getInt("agility");
        earthResistance = result.getInt("earth_resistance");
        airResistance = result.getInt("air_resistance");
        fireResistance = result.getInt("fire_resistance");
        waterResistance = result.getInt("water_resistance");
        neutralResistance = result.getInt("neutral_resistance");
        gradeXp = result.getInt("grade_xp");
        damageReflect = result.getInt("damage_reflect");
        hiddenLevel = result.getInt("hidden_level");
    }

    public List<SpellLevel> getSpells(){
        if(this.spells == null){
            this.spells = new ArrayList<>(5);
        }
        Arrays.stream(this.getMonster().getSpells())
                .mapToObj(id -> DAO.getSpells().findSpell(id).getLevelOrNear(this.grade))
                //.filter(fr -> fr != null)
                .forEach(spell ->  this.spells.add(spell) );
        return spells;
    }

    private void parseStats() {
        this.myStats = new GenericStats();

        this.myStats.addBase(StatsEnum.VITALITY, this.lifePoints);
        this.myStats.addBase(StatsEnum.ACTION_POINTS, this.actionPoints < 0 ? 0 : this.actionPoints );
        this.myStats.addBase(StatsEnum.MOVEMENT_POINTS, this.movementPoints < 0 ? 0 : this.movementPoints);
        this.myStats.addBase(StatsEnum.DODGE_PA_LOST_PROBABILITY, this.paDodge < 0 ? 0 : paDodge);
        this.myStats.addBase(StatsEnum.DODGE_PM_LOST_PROBABILITY, this.pmDodge < 0 ? 0 : pmDodge);
        this.myStats.addBase(StatsEnum.WISDOM, this.wisdom);

        this.myStats.addBase(StatsEnum.ADD_TACKLE_EVADE, this.tackleEvade);
        this.myStats.addBase(StatsEnum.ADD_TACKLE_BLOCK, this.tackleBlock);

        if (!this.getMonster().isUseBombSlot() && !this.getMonster().isUseSummonSlot() && this.strenght == 0 && this.chance == 0 && this.intelligence == 0 && this.agility == 0) {
            int bonus;
            switch (this.grade) {
                case 1:
                    bonus = 80;
                    break;
                case 2:
                    bonus = 85;
                    break;
                case 3:
                    bonus = 90;
                    break;
                case 4:
                    bonus = 95;
                    break;
                case 5:
                default:
                    bonus = 100;
            }
            this.myStats.addBase(StatsEnum.STRENGTH, bonus);
            this.myStats.addBase(StatsEnum.CHANCE, bonus);
            this.myStats.addBase(StatsEnum.INTELLIGENCE, bonus);
            this.myStats.addBase(StatsEnum.AGILITY, this.getMonster().isCanTackle() ? bonus : 0);
        } else {
            this.myStats.addBase(StatsEnum.STRENGTH, this.strenght);
            this.myStats.addBase(StatsEnum.CHANCE, this.chance);
            this.myStats.addBase(StatsEnum.INTELLIGENCE, this.intelligence);
            this.myStats.addBase(StatsEnum.AGILITY, this.agility);
        }

        this.myStats.addBase(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT, this.earthResistance);
        this.myStats.addBase(StatsEnum.AIR_ELEMENT_RESIST_PERCENT, this.airResistance);
        this.myStats.addBase(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT, this.fireResistance);
        this.myStats.addBase(StatsEnum.WATER_ELEMENT_RESIST_PERCENT, this.waterResistance);
        this.myStats.addBase(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT, this.neutralResistance);
        this.myStats.addBase(StatsEnum.DAMAGE_REFLECTION, this.damageReflect);

    }

    public MonsterTemplate getMonster() {
        return DAO.getMonsters().find(this.monsterId);
    }

    public GenericStats getStats() {
        if (this.myStats == null) {
            this.parseStats();
        }
        return myStats;
    }


}
