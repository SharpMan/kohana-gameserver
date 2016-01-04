package koh.game.entities.mob;

import koh.game.dao.DAO;
import koh.game.entities.actors.character.GenericStats;
import koh.protocol.client.enums.StatsEnum;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MonsterGrade {

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

    private void parseStats() {
        this.myStats = new GenericStats();

        this.myStats.addBase(StatsEnum.Vitality, this.lifePoints);
        this.myStats.addBase(StatsEnum.ActionPoints, this.actionPoints);
        this.myStats.addBase(StatsEnum.MovementPoints, this.monsterId);
        this.myStats.addBase(StatsEnum.DodgePALostProbability, this.paDodge);
        this.myStats.addBase(StatsEnum.DodgePMLostProbability, this.pmDodge);
        this.myStats.addBase(StatsEnum.Wisdom, this.wisdom);

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
            this.myStats.addBase(StatsEnum.Strength, bonus);
            this.myStats.addBase(StatsEnum.Chance, bonus);
            this.myStats.addBase(StatsEnum.Intelligence, bonus);
            this.myStats.addBase(StatsEnum.Agility, this.getMonster().isCanTackle() ? bonus : 0);
        } else {
            this.myStats.addBase(StatsEnum.Strength, this.strenght);
            this.myStats.addBase(StatsEnum.Chance, this.chance);
            this.myStats.addBase(StatsEnum.Intelligence, this.intelligence);
            this.myStats.addBase(StatsEnum.Agility, this.agility);
        }

        this.myStats.addBase(StatsEnum.EarthElementResistPercent, this.earthResistance);
        this.myStats.addBase(StatsEnum.AirElementResistPercent, this.airResistance);
        this.myStats.addBase(StatsEnum.FireElementResistPercent, this.fireResistance);
        this.myStats.addBase(StatsEnum.WaterElementResistPercent, this.waterResistance);
        this.myStats.addBase(StatsEnum.NeutralElementResistPercent, this.neutralResistance);
        this.myStats.addBase(StatsEnum.DamageReflection, this.damageReflect);

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
