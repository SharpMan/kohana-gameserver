package koh.game.fights.fighters;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDeathMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;

/**
 * Created by Melancholia on 1/29/16.
 */
public class SummonedReplacerFighter extends SummonedFighter {


    protected MonsterGrade replacedMonster;

    public SummonedReplacerFighter(Fight fight, MonsterGrade monster, Fighter summoner, MonsterGrade replace) {
        super(fight, monster, summoner);
        this.replacedMonster = replace;
    }


    @Getter //TODO Maybe do it for everyone ?
    protected boolean isDying;

    @Override
    public void computeDamages(StatsEnum effect, MutableInt jet) {
        if(grade.getMonsterId() == 116) {
            final double firstJet = jet.doubleValue();
           // System.out.println(firstJet+" "+this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)+" "+ this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR));
            jet.setValue(jet.doubleValue() * summoner.getLevel() * (0.04f));
            jet.add(firstJet);

            jet.setValue(jet.getValue() * 0.01f * (100 + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)));
            return;
        }
        super.computeDamages(effect,jet);
    }

    private int primaryDie(int casterId, boolean force) {
        /*if (force) {
            this.setLife(0);
        }*/
        if (this.getLife() <= 0 || force) {
            this.fight.startSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH);
            //SendGameFightLeaveMessage
            this.fight.sendToField(new GameActionFightDeathMessage(ActionIdEnum.ACTION_CHARACTER_DEATH, casterId, this.ID));

            final Fighter[] aliveFighters = this.team.getAliveFighters()
                    .filter(fighter -> fighter.getSummonerID() == this.ID)
                    .toArray(Fighter[]::new);

            for (final Fighter fighter : aliveFighters) {
                if (fighter instanceof SummonedFighter)
                    ((SummonedFighter) fighter).tryDieSilencious(this.ID, true);
                else if (fighter instanceof SlaveFighter)
                    ((SlaveFighter) fighter).tryDieSilencious(this.ID, true);
                else
                    fighter.tryDie(this.ID, true);
            }

            /*this.team.getAliveFighters()
                    .filter(x -> x.getSummonerID() == this.ID)
                    .forEachOrdered(fighter -> fighter.tryDie(this.ID, true));*/

            if (this.fight.getActivableObjects().containsKey(this)) {
                this.fight.getActivableObjects().get(this).stream().forEach(y -> y.remove());
            }

            for (final Fighter fighter : aliveFighters) {
                fighter.getBuff().getBuffsDec().values().forEach(list -> {
                    for (BuffEffect buff : (Iterable<BuffEffect>) list.parallelStream()::iterator) {
                        if (buff.caster == this)
                            fighter.getBuff().debuff(buff);
                    }
                });
            }

            for(Fighter fr : fight.getTeam1().getMyFighters()){
                if(fr.isAlive()){
                    fr.getBuff().getBuffsDec().values().forEach(list -> {
                        for(BuffEffect buff : (Iterable<BuffEffect>) list.stream()::iterator){
                            if(buff.caster == this && fr != this)
                                fr.getBuff().debuff(buff);
                        }
                    });
                }
            }

            for(Fighter fr : fight.getTeam2().getMyFighters()){
                if(fr.isAlive()){
                    fr.getBuff().getBuffsDec().values().forEach(list -> {
                        for(BuffEffect buff : (Iterable<BuffEffect>) list.stream()::iterator){
                            if(buff.caster == this & fr != this)
                                fr.getBuff().debuff(buff);
                        }
                    });
                }
            }

            /*for(Fighter fr : (Iterable<Fighter>) this.fight.getAliveFighters()::iterator){
                fr.getBuff().getBuffsDec().values().forEach(list -> {
                    for(BuffEffect buff : (Iterable<BuffEffect>) list.stream()::iterator){
                        if(buff.caster == this)
                            fr.getBuff().debuff(buff);
                    }
                });
            }*/

            myCell.removeObject(this);

            this.fight.endSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH, false);
            this.dead = true;

            if (!hasSummoner() && this.fight.tryEndFight()) {
                return -3;
            }
            if (this.fight.getCurrentFighter() == this) {
                this.fight.setFightLoopState(Fight.FightLoopState.STATE_END_TURN);
            }
            return -2;
        }
        return -1;
    }

    @Override
    public int tryDie(int casterId, boolean force) {
        final int value = this.primaryDie(casterId, force);
        if(value == -2 || value == -3){
            if(this.grade.getMonster().isUseSummonSlot()) {
                this.summoner.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base++;
                if (summoner instanceof CharacterFighter) {
                    summoner.send(summoner.asPlayer().getCharacterStatsListMessagePacket());
                }
            }
        }
        if (value == -2) {
            this.isDying = true;
            final MonsterFighter summon = new SummonedFighter(fight, replacedMonster, summoner);
            summon.setLife((int)(summon.getLife() * 0.5f));
            summon.joinFight();
            if (summon.setCell(this.getMyCell(), false) == -3){
                return -3;
            }
            summon.getFight().joinFightTeam(summon, summoner.getTeam(), false, this.getCellId(), true);
            fight.sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_SUMMON_CREATURE, summoner.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
            fight.getFightWorker().summonFighter(summon);
            Arrays.stream(replacedMonster.getMonster().getSpellsOnSummons())
                    .mapToObj(sp -> DAO.getSpells().findSpell(sp).getLevelOrNear(1))
                    .forEach(sp -> fight.launchSpell(summon, sp, this.getCellId(), true, true, true,-1));
            final int result = summon.getMyCell().onObjectAdded(summon);
            if (result == -3) {
                return result;
            }

        }
        return value;
    }
}
