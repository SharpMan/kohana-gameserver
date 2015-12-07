package koh.game.fights.fighters;

import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public abstract class StaticFighter extends Fighter {

    public MonsterGrade Grade;

    public StaticFighter(koh.game.fights.Fight Fight, Fighter Summoner) {
        super(Fight, Summoner);
    }
    
    public void AdjustStats() {
        this.stats.addBase(StatsEnum.Vitality, (short) ((double) this.stats.getEffect(StatsEnum.Vitality).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.Intelligence, (short) ((double) this.stats.getEffect(StatsEnum.Intelligence).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.Chance, (short) ((double) this.stats.getEffect(StatsEnum.Chance).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.Strength, (short) ((double) this.stats.getEffect(StatsEnum.Strength).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.Agility, (short) ((double) this.stats.getEffect(StatsEnum.Agility).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.Wisdom, (short) ((double) this.stats.getEffect(StatsEnum.Wisdom).Base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
    }

    @Override
    public int getMaxAP() {
        return 0;
    }

    @Override
    public int getMaxMP() {
        return 0;
    }

    @Override
    public int getAP() {
        return 0;

    }

    @Override
    public int getMP() {
        return 0;
    }

    private boolean firstTurn = true;

    public void onBeginTurn() {
        if (firstTurn) {
            this.fight.affectSpellTo(this, this, this.Grade.Grade, this.Grade.getMonster().spells);
            this.firstTurn = false;
        }
    }

    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_STATIC;
    }

}
