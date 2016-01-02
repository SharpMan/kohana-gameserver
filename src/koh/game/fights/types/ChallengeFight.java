package koh.game.fights.types;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.entities.environments.DofusMap;
import koh.game.fights.Fight;
import koh.game.fights.FightFormulas;
import koh.game.fights.FightTeam;
import koh.game.fights.FightTypeEnum;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.fight.FightOutcomeEnum;
import koh.protocol.messages.game.context.fight.GameFightEndMessage;
import koh.protocol.messages.game.context.fight.GameFightJoinMessage;
import koh.protocol.messages.game.context.fight.GameFightLeaveMessage;
import koh.protocol.messages.game.context.fight.GameFightRemoveTeamMemberMessage;
import koh.protocol.messages.game.context.fight.GameFightUpdateTeamMessage;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultExperienceData;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeamWithOutcome;

/**
 *
 * @author Neo-Craft
 */
public class ChallengeFight extends Fight {

    public ChallengeFight(DofusMap Map, WorldClient attacker, WorldClient defender) {
        super(FightTypeEnum.FIGHT_TYPE_CHALLENGE, Map);
        Fighter AttFighter = new CharacterFighter(this, attacker);
        Fighter DefFighter = new CharacterFighter(this, defender);

        attacker.addGameAction(new GameFight(AttFighter, this));
        defender.addGameAction(new GameFight(DefFighter, this));

        super.initFight(AttFighter, DefFighter);

    }

    @Override
    public void endFight(FightTeam winners, FightTeam loosers) {

        if(this.fightTime == -1){
            this.myResult = new GameFightEndMessage(0, this.ageBonus, this.lootShareLimitMalus);
            super.endFight();
            return;
        }
        
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);
        
         for (Fighter fighter : (Iterable<Fighter>) winners.getFighters()::iterator) {
             super.addNamedParty(((CharacterFighter) fighter), FightOutcomeEnum.RESULT_VICTORY);
             final AtomicInteger xpTotal = new AtomicInteger(FightFormulas.XPDefie(fighter, winners.getFighters(), loosers.getFighters()));

            int guildXp = FightFormulas.guildXpEarned((CharacterFighter) fighter, xpTotal), mountXpountXp = FightFormulas.mountXpEarned((CharacterFighter) fighter, xpTotal);
            ((CharacterFighter) fighter).character.addExperience(xpTotal.get(), false);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
                {
                    this.experience = ((CharacterFighter) fighter).character.getExperience();
                    this.showExperience = true;
                    this.experienceLevelFloor = DAO.getExps().getPlayerMinExp(fighter.getLevel());
                    this.showExperienceLevelFloor = true;
                    this.experienceNextLevelFloor = DAO.getExps().getPlayerMaxExp(fighter.getLevel());
                    this.showExperienceNextLevelFloor = fighter.getLevel() < 200;
                    this.experienceFightDelta = xpTotal.get();
                    this.showExperienceFightDelta = true;
                    this.experienceForGuild =  guildXp;
                    this.showExperienceForGuild = guildXp > 0;
                    this.experienceForMount = mountXpountXp;
                    this.showExperienceForMount = mountXpountXp > 0;

                }
            }}));
        }

        for (Fighter fighter : (Iterable<Fighter>) loosers.getFighters()::iterator) {
            super.addNamedParty(((CharacterFighter) fighter), FightOutcomeEnum.RESULT_LOST);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[0]));
        }

       

        super.endFight();
    }

    @Override
    public GameFightEndMessage leftEndMessage(CharacterFighter f) {
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime), this.ageBonus, this.lootShareLimitMalus, this.fighters().filter(x -> x.getSummoner() == null).map(x -> new FightResultPlayerListEntry(x.getTeam().id == f.getTeam().id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.getID(), fightTime == -1 ? true : x.isAlive(), (byte) x.getLevel(), new FightResultExperienceData[0])).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

    @Override
    public synchronized void leaveFight(Fighter fighter) {
        // Un persos quitte le combat
        switch (this.fightState) {
            case STATE_PLACE:
                if (fighter == fighter.getTeam().leader) {
                    fighter.getTeam().getFighters().forEach(TeamFighter -> TeamFighter.setLife(0));
                    fighter.setLeft(true);
                    this.endFight(this.getEnnemyTeam(fighter.getTeam()), fighter.getTeam());
                } else {
                    this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, fighter.getTeam().getFightTeamInformations()));

                    this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, fighter.getTeam().id, fighter.getID()));

                    fighter.leaveFight();

                    //Fighter.send(new GameLeaveMessage());
                }
                break;
            case STATE_ACTIVE:
                if (fighter.tryDie(fighter.getID(), true) != -3) {
                    fighter.send(leftEndMessage((CharacterFighter)fighter));
                    this.sendToField(new GameFightLeaveMessage(fighter.getID()));
                    fighter.leaveFight();
                }
                break;
        }
    }

    @Override
    public int getStartTimer() {
        return -1;
    }

    @Override
    public int getTurnTime() {
        return 30000;
    }

    @Override
    protected void sendGameFightJoinMessage(Fighter fighter) {
        //boolean canBeCancelled, boolean canSayReady, boolean isFightStarted, short timeMaxBeforeFightStart, byte fightType
        fighter.send(new GameFightJoinMessage(true, !isStarted(), this.isStarted(), (short) 0, this.fightType.value));
    }

}
