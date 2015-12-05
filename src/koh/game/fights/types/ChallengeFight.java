package koh.game.fights.types;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.dao.mysql.ExpDAOImpl;
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

    public ChallengeFight(DofusMap Map, WorldClient Attacker, WorldClient Defender) {
        super(FightTypeEnum.FIGHT_TYPE_CHALLENGE, Map);
        Fighter AttFighter = new CharacterFighter(this, Attacker);
        Fighter DefFighter = new CharacterFighter(this, Defender);

        Attacker.addGameAction(new GameFight(AttFighter, this));
        Defender.addGameAction(new GameFight(DefFighter, this));

        super.initFight(AttFighter, DefFighter);

    }

    @Override
    public void endFight(FightTeam Winners, FightTeam Loosers) {

        if(this.fightTime == -1){
            this.myResult = new GameFightEndMessage(0, this.AgeBonus, this.lootShareLimitMalus);
            super.endFight();
            return;
        }
        
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.AgeBonus, this.lootShareLimitMalus);
        
         for (Fighter Fighter : (Iterable<Fighter>) Winners.getFighters()::iterator) {
            super.addNamedParty(Fighter, FightOutcomeEnum.RESULT_VICTORY);
            AtomicReference<Long> xpTotal = new AtomicReference<>();
            xpTotal.set(FightFormulas.XPDefie(Fighter, Winners.getFighters(), Loosers.getFighters()));
            long GuildXp = FightFormulas.guildXpEarned((CharacterFighter) Fighter, xpTotal), MountXp = FightFormulas.mountXpEarned((CharacterFighter) Fighter, xpTotal);
            ((CharacterFighter) Fighter).Character.addExperience(xpTotal.get(), false);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.isAlive(), (byte) Fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
                {
                    this.experience = ((CharacterFighter) Fighter).Character.experience;
                    this.showExperience = true;
                    this.experienceLevelFloor = DAO.getExps().getPlayerMinExp(Fighter.getLevel());
                    this.showExperienceLevelFloor = true;
                    this.experienceNextLevelFloor = DAO.getExps().getPlayerMaxExp(Fighter.getLevel());
                    this.showExperienceNextLevelFloor = Fighter.getLevel() < 200;
                    this.experienceFightDelta = xpTotal.get().intValue();
                    this.showExperienceFightDelta = true;
                    this.experienceForGuild = (int) GuildXp;
                    this.showExperienceForGuild = GuildXp > 0;
                    this.experienceForMount = (int) MountXp;
                    this.showExperienceForMount = MountXp > 0;

                }
            }}));
        }

        for (Fighter Fighter : (Iterable<Fighter>) Loosers.getFighters()::iterator) {
            super.addNamedParty(Fighter, FightOutcomeEnum.RESULT_LOST);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.isAlive(), (byte) Fighter.getLevel(), new FightResultExperienceData[0]));
        }

       

        super.endFight();
    }

    @Override
    public GameFightEndMessage leftEndMessage(Fighter f) {
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime), this.AgeBonus, this.lootShareLimitMalus, this.Fighters().filter(x -> x.summoner == null).map(x -> new FightResultPlayerListEntry(x.team.Id == f.team.Id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.ID, fightTime == -1 ? true : x.isAlive(), (byte) x.getLevel(), new FightResultExperienceData[0])).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

    @Override
    public synchronized void leaveFight(Fighter Fighter) {
        // Un persos quitte le combat
        switch (this.fightState) {
            case STATE_PLACE:
                if (Fighter == Fighter.team.Leader) {
                    Fighter.team.getFighters().forEach(TeamFighter -> TeamFighter.setLife(0));
                    Fighter.left = true;
                    this.endFight(this.getEnnemyTeam(Fighter.team), Fighter.team);
                } else {
                    this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, Fighter.team.getFightTeamInformations()));

                    this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, Fighter.team.Id, Fighter.ID));

                    Fighter.leaveFight();

                    //Fighter.send(new GameLeaveMessage());
                }
                break;
            case STATE_ACTIVE:
                if (Fighter.tryDie(Fighter.ID, true) != -3) {
                    Fighter.send(leftEndMessage(Fighter));
                    this.sendToField(new GameFightLeaveMessage(Fighter.ID));
                    Fighter.leaveFight();
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
