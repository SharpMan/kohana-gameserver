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

    private double getCoefficient(){ //Steamer
        switch (this.grade.getLevel()){
            case 1:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.3;
                    case 3288:
                        return 0.2;
                    case 3289:
                        return 0.25;
                }
                break;
            case 2:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.32;
                    case 3288:
                        return 0.22;
                    case 3289:
                        return 0.27;
                }
                break;
            case 3:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.34;
                    case 3288:
                        return 0.24;
                    case 3289:
                        return 0.29;
                }
                break;
            case 4:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.36;
                    case 3288:
                        return 0.24;
                    case 3289:
                        return 0.29;
                }
                break;
            case 5:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.38;
                    case 3288:
                        return 0.28;
                    case 3289:
                        return 0.33;
                }
                break;
            case 6:
                switch (this.grade.getMonsterId()){
                    case 3287:
                        return 0.4;
                    case 3288:
                        return 0.3;
                    case 3289:
                        return 0.35;
                }
                break;
        }
        return 0.3;
    }

    public void adjustStats() {
        if(this.grade.getMonsterId() == 3287 || this.grade.getMonsterId() == 3288 || this.grade.getMonsterId() == 3289){
            this.stats.addBase(StatsEnum.VITALITY, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getLife() * this.getCoefficient())));
            this.stats.addBase(StatsEnum.INTELLIGENCE, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getStats().getTotal(StatsEnum.INTELLIGENCE) * this.getCoefficient())));
            this.stats.addBase(StatsEnum.CHANCE, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getStats().getTotal(StatsEnum.CHANCE) * this.getCoefficient())));
            this.stats.addBase(StatsEnum.STRENGTH, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getStats().getTotal(StatsEnum.STRENGTH) * this.getCoefficient())));
            this.stats.addBase(StatsEnum.AGILITY, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getStats().getTotal(StatsEnum.AGILITY) * this.getCoefficient())));
            this.stats.addBase(StatsEnum.WISDOM, (int) Math.floor((((summoner.getLevel() -1) * 5 +55) * this.getCoefficient()) + (summoner.getPlayer().getStats().getTotal(StatsEnum.WISDOM) * this.getCoefficient())));

        }
        else {
            this.stats.addBase(StatsEnum.VITALITY, (short) ((double) this.stats.getEffect(StatsEnum.VITALITY).base * (/*1.0 +*/ (double) this.summoner.getLevel() / 100.0)));
            this.stats.addBase(StatsEnum.INTELLIGENCE, (short) ((double) this.stats.getEffect(StatsEnum.INTELLIGENCE).base * ((double) this.summoner.getLevel() / 100.0)));
            this.stats.addBase(StatsEnum.CHANCE, (short) ((double) this.stats.getEffect(StatsEnum.CHANCE).base * ( (double) this.summoner.getLevel() / 100.0)));
            this.stats.addBase(StatsEnum.STRENGTH, (short) ((double) this.stats.getEffect(StatsEnum.STRENGTH).base * ((double) this.summoner.getLevel() / 100.0)));
            this.stats.addBase(StatsEnum.AGILITY, (short) ((double) this.stats.getEffect(StatsEnum.AGILITY).base * ( (double) this.summoner.getLevel() / 100.0)));
            this.stats.addBase(StatsEnum.WISDOM, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * ( (double) this.summoner.getLevel() / 100.0)));
        }
    }

    public int tryDieSilencious(int casterId, boolean force) {
        final int result = super.tryDie(casterId,force);
        if(result == -2 || result == -3){
            if(this.grade.getMonster().isUseSummonSlot()) {
                this.summoner.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base++;
            }
        }
        return  result;
    }


    @Override
    public int tryDie(int casterId, boolean force) {
       final int result = super.tryDie(casterId,force);
        if(result == -2 || result == -3){
            if(this.grade.getMonster().isUseSummonSlot()) {
                this.summoner.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base++;
                if (summoner instanceof CharacterFighter) {
                    summoner.send(summoner.asPlayer().getCharacterStatsListMessagePacket());
                    //summoner.send(((CharacterFighter) summoner).getFighterStatsListMessagePacket());
                }
            }
            //5435
        }
        return  result;
    }
}
