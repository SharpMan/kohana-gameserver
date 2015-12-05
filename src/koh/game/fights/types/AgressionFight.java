package koh.game.fights.types;

import java.util.stream.Collectors;
import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.dao.mysql.ExpDAOImpl;
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
import koh.protocol.messages.game.context.fight.GameFightRemoveTeamMemberMessage;
import koh.protocol.messages.game.context.fight.GameFightUpdateTeamMessage;
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

        Attacker.addGameAction(new GameFight(AttFighter, this));
        Defender.addGameAction(new GameFight(DefFighter, this));

        Map.sendToField(new GameRolePlayAggressionMessage(Attacker.character.ID, Defender.character.ID));
        super.myTeam1.alignmentSide = Attacker.character.alignmentSide;
        super.myTeam2.alignmentSide = Defender.character.alignmentSide;

        super.initFight(AttFighter, DefFighter);

    }

    @Override
    public synchronized void leaveFight(Fighter Fighter) {
        // Un persos quitte le combat
        switch (this.fightState) {
            case STATE_PLACE:
                if (Fighter == Fighter.team.Leader) {
                    break;
                } else {
                    this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, Fighter.team.getFightTeamInformations()));

                    this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, Fighter.team.Id, Fighter.ID));

                    Fighter.leaveFight();
                }
                break;
            case STATE_ACTIVE:
                if (Fighter.tryDie(Fighter.ID, true) != -3) {
                    Fighter.send(leftEndMessage(Fighter));
                    this.sendToField(new GameFightLeaveMessage(Fighter.ID));
                    Fighter.leaveFight();
                }
                break;
            default:
                throw new Error("Incredible left from fighter " + Fighter.ID);
        }
    }

    @Override
    public void endFight(FightTeam Winners, FightTeam Loosers) {
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.AgeBonus, this.lootShareLimitMalus);

        for (Fighter Fighter : (Iterable<Fighter>) Loosers.getFighters()::iterator) {
            super.addNamedParty(Fighter, FightOutcomeEnum.RESULT_LOST);
            final short LossedHonor = (short) (FightFormulas.honorPoint(Fighter, Winners.getFighters(), Loosers.getFighters(), true) / AntiCheat.deviserBy(getWinners().getFighters(), Fighter, false));
            ((CharacterFighter) Fighter).Character.addHonor(LossedHonor, true);
            ((CharacterFighter) Fighter).Character.dishonor += FightFormulas.calculateEarnedDishonor(Fighter);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.isAlive(), (byte) Fighter.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) Fighter).Character.alignmentGrade, DAO.getExps().getLevel(((CharacterFighter) Fighter).Character.alignmentGrade).PvP, DAO.getExps().getLevel(((CharacterFighter) Fighter).Character.alignmentGrade == 10 ? 10 : ((CharacterFighter) Fighter).Character.alignmentGrade + 1).PvP, ((CharacterFighter) Fighter).Character.honor, LossedHonor)}));
        }

        for (Fighter Fighter : (Iterable<Fighter>) Winners.getFighters()::iterator) {
            super.addNamedParty(Fighter, FightOutcomeEnum.RESULT_VICTORY);
            final short LossedHonor = (short) (FightFormulas.honorPoint(Fighter, Winners.getFighters(), Loosers.getFighters(), false) / AntiCheat.deviserBy(getEnnemyTeam(getWinners()).getFighters(), Fighter, true));
            ((CharacterFighter) Fighter).Character.addHonor(LossedHonor, true);
            ((CharacterFighter) Fighter).Character.dishonor += FightFormulas.calculateEarnedDishonor(Fighter);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, Fighter.wave, new FightLoot(new int[0], 0), Fighter.ID, Fighter.isAlive(), (byte) Fighter.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) Fighter).Character.alignmentGrade, DAO.getExps().getLevel(((CharacterFighter) Fighter).Character.alignmentGrade).PvP, DAO.getExps().getLevel(((CharacterFighter) Fighter).Character.alignmentGrade == 10 ? 10 : ((CharacterFighter) Fighter).Character.alignmentGrade + 1).PvP, ((CharacterFighter) Fighter).Character.honor, LossedHonor)}));
        }
        super.endFight();
    }

    @Override
    public int getStartTimer() {
        return 30;
    }

    @Override
    public int getTurnTime() {
        return 45000;
    }

    @Override
    protected void sendGameFightJoinMessage(Fighter fighter) {
        //boolean canBeCancelled, boolean canSayReady, boolean isFightStarted, short timeMaxBeforeFightStart, byte fightType
        fighter.send(new GameFightJoinMessage(true, !this.isStarted(), this.isStarted(), (short) this.getPlacementTimeLeft(), this.fightType.value));
    }

    @Override
    public GameFightEndMessage leftEndMessage(Fighter Leaver) { //Fixme je ai le call des classes implement comme ça faut trouver une solution
        short LossedHonor = FightFormulas.honorPoint(Leaver, this.getEnnemyTeam(Leaver.team).getFighters().filter(x -> x.summoner == null), Leaver.team.getFighters().filter(x -> x.summoner == null), true, false);
        ((CharacterFighter) Leaver).Character.addHonor(LossedHonor, true);
        ((CharacterFighter) Leaver).Character.dishonor += FightFormulas.calculateEarnedDishonor(Leaver);
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime), this.AgeBonus, (short) 0, this.Fighters().filter(x -> x.summoner == null).map(x -> new FightResultPlayerListEntry(x.team.Id == Leaver.team.Id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.ID, x.isAlive(), (byte) x.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) x).Character.alignmentGrade, DAO.getExps().getLevel(((CharacterFighter) x).Character.alignmentGrade).PvP, DAO.getExps().getLevel(((CharacterFighter) x).Character.alignmentGrade == 10 ? 10 : ((CharacterFighter) x).Character.alignmentGrade + 1).PvP, ((CharacterFighter) x).Character.honor, x.ID == Leaver.ID ? LossedHonor : 0)})).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

}
