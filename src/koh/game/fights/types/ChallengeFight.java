package koh.game.fights.types;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import koh.game.actions.GameFight;
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

        Attacker.AddGameAction(new GameFight(AttFighter, this));
        Defender.AddGameAction(new GameFight(DefFighter, this));

        super.InitFight(AttFighter, DefFighter);

    }

    @Override
    public void EndFight(FightTeam Winners, FightTeam Loosers) {

        if(this.FightTime == -1){
            this.myResult = new GameFightEndMessage(0, this.AgeBonus, this.lootShareLimitMalus);
            super.EndFight();
            return;
        }
        
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.FightTime, this.AgeBonus, this.lootShareLimitMalus);
        
         for (Fighter Fighter : (Iterable<Fighter>) Winners.GetFighters()::iterator) {
            super.AddNamedParty(Fighter, FightOutcomeEnum.RESULT_VICTORY);
            AtomicReference<Long> xpTotal = new AtomicReference<>();
            xpTotal.set(FightFormulas.XPDefie(Fighter, Winners.GetFighters(), Loosers.GetFighters()));
            long GuildXp = FightFormulas.GuildXpEarned((CharacterFighter) Fighter, xpTotal), MountXp = FightFormulas.MountXpEarned((CharacterFighter) Fighter, xpTotal);
            ((CharacterFighter) Fighter).Character.AddExperience(xpTotal.get(), false);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.IsAlive(), (byte) Fighter.Level(), new FightResultExperienceData[]{new FightResultExperienceData() {
                {
                    this.experience = ((CharacterFighter) Fighter).Character.Experience;
                    this.showExperience = true;
                    this.experienceLevelFloor = ExpDAOImpl.persoXpMin(Fighter.Level());
                    this.showExperienceLevelFloor = true;
                    this.experienceNextLevelFloor = ExpDAOImpl.persoXpMax(Fighter.Level());
                    this.showExperienceNextLevelFloor = Fighter.Level() < 200;
                    this.experienceFightDelta = xpTotal.get().intValue();
                    this.showExperienceFightDelta = true;
                    this.experienceForGuild = (int) GuildXp;
                    this.showExperienceForGuild = GuildXp > 0;
                    this.experienceForMount = (int) MountXp;
                    this.showExperienceForMount = MountXp > 0;

                }
            }}));
        }

        for (Fighter Fighter : (Iterable<Fighter>) Loosers.GetFighters()::iterator) {
            super.AddNamedParty(Fighter, FightOutcomeEnum.RESULT_LOST);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.IsAlive(), (byte) Fighter.Level(), new FightResultExperienceData[0]));
        }

       

        super.EndFight();
    }

    @Override
    public GameFightEndMessage LeftEndMessage(Fighter f) {
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.FightTime), this.AgeBonus, this.lootShareLimitMalus, this.Fighters().filter(x -> x.Summoner == null).map(x -> new FightResultPlayerListEntry(x.Team.Id == f.Team.Id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.ID, FightTime== -1 ? true : x.IsAlive(), (byte) x.Level(), new FightResultExperienceData[0])).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

    @Override
    public synchronized void LeaveFight(Fighter Fighter) {
        // Un persos quitte le combat
        switch (this.FightState) {
            case STATE_PLACE:
                if (Fighter == Fighter.Team.Leader) {
                    Fighter.Team.GetFighters().forEach(TeamFighter -> TeamFighter.setLife(0));
                    Fighter.Left = true;
                    this.EndFight(this.GetEnnemyTeam(Fighter.Team), Fighter.Team);
                } else {
                    this.Map.sendToField(new GameFightUpdateTeamMessage(this.FightId, Fighter.Team.GetFightTeamInformations()));

                    this.sendToField(new GameFightRemoveTeamMemberMessage(this.FightId, Fighter.Team.Id, Fighter.ID));

                    Fighter.LeaveFight();

                    //Fighter.Send(new GameLeaveMessage());
                }
                break;
            case STATE_ACTIVE:
                if (Fighter.TryDie(Fighter.ID, true) != -3) {
                    Fighter.Send(LeftEndMessage(Fighter));
                    this.sendToField(new GameFightLeaveMessage(Fighter.ID));
                    Fighter.LeaveFight();
                }
                break;
        }
    }

    @Override
    public int GetStartTimer() {
        return -1;
    }

    @Override
    public int GetTurnTime() {
        return 30000;
    }

    @Override
    protected void SendGameFightJoinMessage(Fighter fighter) {
        //boolean canBeCancelled, boolean canSayReady, boolean isFightStarted, short timeMaxBeforeFightStart, byte fightType
        fighter.Send(new GameFightJoinMessage(true, !IsStarted(), this.IsStarted(), (short) 0, this.FightType.value));
    }

}
