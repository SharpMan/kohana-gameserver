package koh.game.entities.mob;

import koh.game.dao.DAO;
import koh.game.entities.actors.character.GenericStats;
import koh.protocol.client.enums.StatsEnum;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MonsterGrade {

    public byte Grade;
    public int monsterId, Level, lifePoints, actionPoints, movementPoints, paDodge, pmDodge;
    public int Wisdom, tackleEvade, tackleBlock, Strenght, Chance, Intelligence, Agility, earthResistance, airResistance, fireResistance, waterResistance, neutralResistance;
    public int gradeXp, damageReflect, hiddenLevel;

    private GenericStats myStats;

    public MonsterGrade(ResultSet result) throws SQLException {
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

    private void parseStats() {
        this.myStats = new GenericStats();

        this.myStats.addBase(StatsEnum.Vitality, this.lifePoints);
        this.myStats.addBase(StatsEnum.ActionPoints, this.actionPoints);
        this.myStats.addBase(StatsEnum.MovementPoints, this.monsterId);
        this.myStats.addBase(StatsEnum.DodgePALostProbability, this.paDodge);
        this.myStats.addBase(StatsEnum.DodgePMLostProbability, this.pmDodge);
        this.myStats.addBase(StatsEnum.Wisdom, this.Wisdom);

        this.myStats.addBase(StatsEnum.Add_TackleEvade, this.tackleEvade);
        this.myStats.addBase(StatsEnum.Add_TackleBlock, this.tackleBlock);

        if (!this.getMonster().isUseBombSlot() && !this.getMonster().isUseSummonSlot() && this.Strenght == 0 && this.Chance == 0 && this.Intelligence == 0 && this.Agility == 0) {
            int Bonus;
            switch (this.Grade) {
                case 1:
                    Bonus = 80;
                    break;
                case 2:
                    Bonus = 85;
                    break;
                case 3:
                    Bonus = 90;
                    break;
                case 4:
                    Bonus = 95;
                    break;
                case 5:
                default:
                    Bonus = 100;
            }
            this.myStats.addBase(StatsEnum.Strength, Bonus);
            this.myStats.addBase(StatsEnum.Chance, Bonus);
            this.myStats.addBase(StatsEnum.Intelligence, Bonus);
            this.myStats.addBase(StatsEnum.Agility, this.getMonster().isCanTackle() ? Bonus : 0);
        } else {
            this.myStats.addBase(StatsEnum.Strength, this.Strenght);
            this.myStats.addBase(StatsEnum.Chance, this.Chance);
            this.myStats.addBase(StatsEnum.Intelligence, this.Intelligence);
            this.myStats.addBase(StatsEnum.Agility, this.Agility);
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
