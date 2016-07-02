package koh.game.fights.fighters;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Melancholia on 7/2/16.
 */
public class SlaveReplacerFighter extends SlaveFighter {

    protected MonsterGrade replacedMonster;

    public SlaveReplacerFighter(Fight fight, MonsterGrade monster, Fighter summoner, MonsterGrade replace) {
        super(fight, monster, summoner);
        this.replacedMonster = replace;
    }


    @Getter //TODO Maybe do it for everyone ?
    protected boolean isDying;

    @Override
    public int tryDie(int casterId, boolean force) {
        final int value = super.tryDie(casterId, force);
        if (value == -2) {
            this.isDying = true;
            final MonsterFighter summon = new SummonedFighter(fight, replacedMonster, summoner);
            summon.setLife((int)(summon.getLife() * 0.5f));
            summon.joinFight();
            summon.getFight().joinFightTeam(summon, summoner.getTeam(), false, this.getCellId(), true);
            fight.sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_SUMMON_CREATURE, summoner.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
            fight.getFightWorker().summonFighter(summon);
            Arrays.stream(replacedMonster.getMonster().getSpellsOnSummons())
                    .mapToObj(sp -> DAO.getSpells().findSpell(sp).getLevelOrNear(1))
                    .forEach(sp -> fight.launchSpell(summon, sp, this.getCellId(), true, true, true,-1));

        }
        return value;
    }

    @Override
    public int tryDieSilencious(int casterId, boolean force) {
        final int value = super.tryDieSilencious(casterId, force);
        if (value == -2) {
            this.isDying = true;
            final MonsterFighter summon = new SummonedFighter(fight, replacedMonster, summoner);
            summon.setLife((int)(summon.getLife() * 0.5f));
            summon.joinFight();
            summon.getFight().joinFightTeam(summon, summoner.getTeam(), false, this.getCellId(), true);
            fight.sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_SUMMON_CREATURE, summoner.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
            fight.getFightWorker().summonFighter(summon);
            Arrays.stream(replacedMonster.getMonster().getSpellsOnSummons())
                    .mapToObj(sp -> DAO.getSpells().findSpell(sp).getLevelOrNear(1))
                    .forEach(sp -> fight.launchSpell(summon, sp, this.getCellId(), true, true, true,-1));

        }
        return value;
    }

}
