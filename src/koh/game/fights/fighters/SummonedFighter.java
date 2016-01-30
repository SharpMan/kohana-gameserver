package koh.game.fights.fighters;

import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.look.EntityLook;

import java.util.List;

/**
 *
 * @author Neo-Craft
 */
public class SummonedFighter extends MonsterFighter {


    public SummonedFighter(Fight fight, MonsterGrade monster, Fighter summoner) {
        super(fight,monster,fight.getNextContextualId(),null);
        this.summoner = summoner;
        this.adjustStats();
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }

    public void adjustStats() {
        this.stats.addBase(StatsEnum.VITALITY, (short) ((double) this.stats.getEffect(StatsEnum.VITALITY).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.INTELLIGENCE, (short) ((double) this.stats.getEffect(StatsEnum.INTELLIGENCE).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.CHANCE, (short) ((double) this.stats.getEffect(StatsEnum.CHANCE).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.STRENGTH, (short) ((double) this.stats.getEffect(StatsEnum.STRENGTH).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.AGILITY, (short) ((double) this.stats.getEffect(StatsEnum.AGILITY).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.WISDOM, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
    }


}
