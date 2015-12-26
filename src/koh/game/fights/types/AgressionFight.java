package koh.game.fights.types;

import java.util.stream.Collectors;
import koh.game.actions.GameFight;
import koh.game.dao.DAO;
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

    public AgressionFight(DofusMap map, WorldClient attacker, WorldClient defender) {
        super(FightTypeEnum.FIGHT_TYPE_AGRESSION, map);
        Fighter AttFighter = new CharacterFighter(this, attacker);
        Fighter DefFighter = new CharacterFighter(this, defender);

        attacker.addGameAction(new GameFight(AttFighter, this));
        defender.addGameAction(new GameFight(DefFighter, this));

        map.sendToField(new GameRolePlayAggressionMessage(attacker.getCharacter().getID(), defender.getCharacter().getID()));
        super.myTeam1.alignmentSide = attacker.getCharacter().getAlignmentSide();
        super.myTeam2.alignmentSide = defender.getCharacter().getAlignmentSide();

        super.initFight(AttFighter, DefFighter);

    }

    @Override
    public synchronized void leaveFight(Fighter fighter) {
        // Un persos quitte le combat
        switch (this.fightState) {
            case STATE_PLACE:
                if (fighter == fighter.getTeam().leader) {
                    break;
                } else {
                    this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, fighter.getTeam().getFightTeamInformations()));

                    this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, fighter.getTeam().id, fighter.getID()));

                    fighter.leaveFight();
                }
                break;
            case STATE_ACTIVE:
                if (fighter.tryDie(fighter.getID(), true) != -3) {
                    fighter.send(leftEndMessage(fighter));
                    this.sendToField(new GameFightLeaveMessage(fighter.getID()));
                    fighter.leaveFight();
                }
                break;
            default:
                throw new Error("Incredible left from fighter " + fighter.getID());
        }
    }

    @Override
    public void endFight(FightTeam Winners, FightTeam Loosers) {
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);

        for (Fighter fighter : (Iterable<Fighter>) Loosers.getFighters()::iterator) {
            super.addNamedParty(fighter, FightOutcomeEnum.RESULT_LOST);
            final short LossedHonor = (short) (FightFormulas.honorPoint(fighter, Winners.getFighters(), Loosers.getFighters(), true) / AntiCheat.deviserBy(getWinners().getFighters(), fighter, false));
            ((CharacterFighter) fighter).character.addHonor(LossedHonor, true);
            ((CharacterFighter) fighter).character.addDishonor(FightFormulas.calculateEarnedDishonor(fighter),true);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) fighter).character.getAlignmentGrade(), DAO.getExps().getLevel(((CharacterFighter) fighter).character.getAlignmentGrade()).getPvP(), DAO.getExps().getLevel(((CharacterFighter) fighter).character.getAlignmentGrade() == 10 ? 10 : ((CharacterFighter) fighter).character.getAlignmentGrade() + 1).getPvP(), ((CharacterFighter) fighter).character.getHonor(), LossedHonor)}));
        }

        for (Fighter fighter : (Iterable<Fighter>) Winners.getFighters()::iterator) {
            super.addNamedParty(fighter, FightOutcomeEnum.RESULT_VICTORY);
            final short LossedHonor = (short) (FightFormulas.honorPoint(fighter, Winners.getFighters(), Loosers.getFighters(), false) / AntiCheat.deviserBy(getEnnemyTeam(getWinners()).getFighters(), fighter, true));
            ((CharacterFighter) fighter).character.addHonor(LossedHonor, true);
            ((CharacterFighter) fighter).character.addDishonor(FightFormulas.calculateEarnedDishonor(fighter),true);
            this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) fighter).character.getAlignmentGrade(), DAO.getExps().getLevel(((CharacterFighter) fighter).character.getAlignmentGrade()).getPvP(), DAO.getExps().getLevel(((CharacterFighter) fighter).character.getAlignmentGrade() == 10 ? 10 : ((CharacterFighter) fighter).character.getAlignmentGrade() + 1).getPvP(), ((CharacterFighter) fighter).character.getHonor(), LossedHonor)}));
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
    public GameFightEndMessage leftEndMessage(Fighter leaver) { //Fixme je ai le call des classes implement comme ça faut trouver une solution
        short lossedHonor = FightFormulas.honorPoint(leaver, this.getEnnemyTeam(leaver.getTeam()).getFighters().filter(x -> x.getSummoner() == null), leaver.getTeam().getFighters().filter(x -> x.getSummoner() == null), true, false);
        ((CharacterFighter) leaver).character.addHonor(lossedHonor, true);
        ((CharacterFighter) leaver).character.addDishonor(FightFormulas.calculateEarnedDishonor(leaver),true);
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime), this.ageBonus, (short) 0, this.Fighters().filter(x -> x.getSummoner() == null).map(x -> new FightResultPlayerListEntry(x.getTeam().id == leaver.getTeam().id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.getID(), x.isAlive(), (byte) x.getLevel(), new FightResultPvpData[]{new FightResultPvpData(((CharacterFighter) x).character.getAlignmentGrade(), DAO.getExps().getLevel(((CharacterFighter) x).character.getAlignmentGrade()).getPvP(), DAO.getExps().getLevel(((CharacterFighter) x).character.getAlignmentGrade() == 10 ? 10 : ((CharacterFighter) x).character.getAlignmentGrade() + 1).getPvP(), ((CharacterFighter) x).character.getHonor(), x.getID() == leaver.getID() ? lossedHonor : 0)})).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }

}
