package koh.game.fights.types;

import java.util.stream.Collectors;
import koh.game.actions.GameFight;
import koh.game.dao.ExpDAO;
import koh.game.entities.environments.DofusMap;
import koh.game.fights.AntiCheat;
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
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayAggressionMessage;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import koh.protocol.types.game.context.fight.FightResultPvpData;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeamWithOutcome;

/**
 *
 * @author Neo-Craft
 */
public class AgressionFight extends Fight {

    public AgressionFight(DofusMap Map, WorldClient Attacker, WorldClient Defender) {
        super(FightTypeEnum.FIGHT_TYPE_AGRESSION, Map);
        Fighter AttFighter = new CharacterFighter(this, Attacker);
        Fighter DefFighter = new CharacterFighter(this, Defender);

        Attacker.AddGameAction(new GameFight(AttFighter, this));
        Defender.AddGameAction(new GameFight(DefFighter, this));

        Map.sendToField(new GameRolePlayAggressionMessage(Attacker.Character.ID, Defender.Character.ID));
        super.myTeam1.AlignmentSide = Attacker.Character.AlignmentSide;
        super.myTeam2.AlignmentSide = Defender.Character.AlignmentSide;

        super.InitFight(AttFighter, DefFighter);

    }

    @Override
    public synchronized void LeaveFight(Fighter Fighter) {
        // Un persos quitte le combat
        switch (this.FightState) {
            case STATE_ACTIVE:
                if (Fighter.TryDie(Fighter.ID, true) != -3) {
                    Fighter.Send(LeftEndMessage(Fighter));
                    this.sendToField(new GameFightLeaveMessage(Fighter.ID));
                    Fighter.LeaveFight();
                }
                break;
            default:
                throw new Error("Incredible left from fighter " + Fighter.ID);
        }
    }

    @Override
    public void EndFight(FightTeam Winners, FightTeam Loosers) {
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.FightTime, this.AgeBonus, this.lootShareLimitMalus);

        for (Fighter Fighter : (Iterable<Fighter>) Loosers.GetFighters()::iterator) {
            super.AddNamedParty(Fighter, FightOutcomeEnum.RESULT_LOST);
            final short LossedHonor = (short) (FightFormulas.HonorPoint(Fighter, Winners.GetFighters(), Loosers.GetFighters(), true) / AntiCheat.DeviserBy(GetWinners().GetFighters(), Fighter, false));
            ((CharacterFighter) Fighter).Character.addHonor(LossedHonor, true);
            ((CharacterFighter) Fighter).Character.Dishonor += FightFormulas.CalculateEarnedDishonor(Fighter);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.IsAlive(), (byte) Fighter.Level(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) Fighter).Character.AlignmentGrade, ExpDAO.GetFloorByLevel(((CharacterFighter) Fighter).Character.AlignmentGrade).PvP, ExpDAO.GetFloorByLevel(((CharacterFighter) Fighter).Character.AlignmentGrade == 10 ? 10 : ((CharacterFighter) Fighter).Character.AlignmentGrade + 1).PvP, ((CharacterFighter) Fighter).Character.Honor, LossedHonor)}));
        }

        for (Fighter Fighter : (Iterable<Fighter>) Winners.GetFighters()::iterator) {
            super.AddNamedParty(Fighter, FightOutcomeEnum.RESULT_VICTORY);
            final short LossedHonor = (short) (FightFormulas.HonorPoint(Fighter, Winners.GetFighters(), Loosers.GetFighters(), false) / AntiCheat.DeviserBy(GetEnnemyTeam(GetWinners()).GetFighters(), Fighter, true));
            ((CharacterFighter) Fighter).Character.addHonor(LossedHonor, true);
            ((CharacterFighter) Fighter).Character.Dishonor += FightFormulas.CalculateEarnedDishonor(Fighter);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.IsAlive(), (byte) Fighter.Level(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) Fighter).Character.AlignmentGrade, ExpDAO.GetFloorByLevel(((CharacterFighter) Fighter).Character.AlignmentGrade).PvP, ExpDAO.GetFloorByLevel(((CharacterFighter) Fighter).Character.AlignmentGrade == 10 ? 10 : ((CharacterFighter) Fighter).Character.AlignmentGrade + 1).PvP, ((CharacterFighter) Fighter).Character.Honor, LossedHonor)}));
        }
        super.EndFight();
    }

    @Override
    public int GetStartTimer() {
        return 30;
    }

    @Override
    public int GetTurnTime() {
        return 45000;
    }

    @Override
    protected void SendGameFightJoinMessage(Fighter fighter) {
        //boolean canBeCancelled, boolean canSayReady, boolean isFightStarted, short timeMaxBeforeFightStart, byte fightType
        fighter.Send(new GameFightJoinMessage(true, !this.IsStarted(), this.IsStarted(), (short) this.GetPlacementTimeLeft(), this.FightType.value));
    }

    @Override
    public GameFightEndMessage LeftEndMessage(Fighter Leaver) { //Fixme je ai le call des classes implement comme ça faut trouver une solution
        short LossedHonor = FightFormulas.HonorPoint(Leaver, this.GetEnnemyTeam(Leaver.Team).GetFighters().filter(x -> x.Summoner == null), Leaver.Team.GetFighters().filter(x -> x.Summoner == null), true, false);
        ((CharacterFighter) Leaver).Character.addHonor(LossedHonor, true);
        ((CharacterFighter) Leaver).Character.Dishonor += FightFormulas.CalculateEarnedDishonor(Leaver);
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.FightTime), this.AgeBonus, (short) 0, this.Fighters().filter(x -> x.Summoner == null).map(x -> new FightResultPlayerListEntry(x.Team.Id == Leaver.Team.Id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.ID, x.IsAlive(), (byte) x.Level(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) x).Character.AlignmentGrade, ExpDAO.GetFloorByLevel(((CharacterFighter) x).Character.AlignmentGrade).PvP, ExpDAO.GetFloorByLevel(((CharacterFighter) x).Character.AlignmentGrade == 10 ? 10 : ((CharacterFighter) x).Character.AlignmentGrade + 1).PvP, ((CharacterFighter) x).Character.Honor, x.ID == Leaver.ID ? LossedHonor : 0)})).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

}
