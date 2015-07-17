package koh.game.entities.mob;

import koh.game.dao.MonsterDAO;
import koh.game.entities.actors.character.GenericStats;
import koh.protocol.client.enums.StatsEnum;

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

    private void ParseStats() {
        this.myStats = new GenericStats();

        this.myStats.AddBase(StatsEnum.Vitality, this.lifePoints);
        this.myStats.AddBase(StatsEnum.ActionPoints, this.actionPoints);
        this.myStats.AddBase(StatsEnum.MovementPoints, this.monsterId);
        this.myStats.AddBase(StatsEnum.DodgePALostProbability, this.paDodge);
        this.myStats.AddBase(StatsEnum.DodgePMLostProbability, this.pmDodge);
        this.myStats.AddBase(StatsEnum.Wisdom, this.Wisdom);

        this.myStats.AddBase(StatsEnum.Add_TackleEvade, this.tackleEvade);
        this.myStats.AddBase(StatsEnum.Add_TackleBlock, this.tackleBlock);

        if (!this.Monster().useBombSlot && !this.Monster().useSummonSlot && this.Strenght == 0 && this.Chance == 0 && this.Intelligence == 0 && this.Agility == 0) {
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
            this.myStats.AddBase(StatsEnum.Strength, Bonus);
            this.myStats.AddBase(StatsEnum.Chance, Bonus);
            this.myStats.AddBase(StatsEnum.Intelligence, Bonus);
            this.myStats.AddBase(StatsEnum.Agility, this.Monster().canTackle ? Bonus : 0);
        } else {
            this.myStats.AddBase(StatsEnum.Strength, this.Strenght);
            this.myStats.AddBase(StatsEnum.Chance, this.Chance);
            this.myStats.AddBase(StatsEnum.Intelligence, this.Intelligence);
            this.myStats.AddBase(StatsEnum.Agility, this.Agility);
        }

        this.myStats.AddBase(StatsEnum.EarthElementResistPercent, this.earthResistance);
        this.myStats.AddBase(StatsEnum.AirElementResistPercent, this.airResistance);
        this.myStats.AddBase(StatsEnum.FireElementResistPercent, this.fireResistance);
        this.myStats.AddBase(StatsEnum.WaterElementResistPercent, this.waterResistance);
        this.myStats.AddBase(StatsEnum.NeutralElementResistPercent, this.neutralResistance);
        this.myStats.AddBase(StatsEnum.DamageReflection, this.damageReflect);

    }

    public MonsterTemplate Monster() {
        return MonsterDAO.Cache.get(this.monsterId);
    }

    public GenericStats GetStats() {
        if (this.myStats == null) {
            this.ParseStats();
        }
        return myStats;
    }
    

}
