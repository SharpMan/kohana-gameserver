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
        Fighter attFighter = new CharacterFighter(this, attacker);
        Fighter defFighter = new CharacterFighter(this, defender);

        attacker.addGameAction(new GameFight(attFighter, this));
        defender.addGameAction(new GameFight(defFighter, this));

        map.sendToField(new GameRolePlayAggressionMessage(attacker.getCharacter().getID(), defender.getCharacter().getID()));
        super.myTeam1.alignmentSide = attacker.getCharacter().getAlignmentSide();
        super.myTeam2.alignmentSide = defender.getCharacter().getAlignmentSide();

        super.initFight(attFighter, defFighter);

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
                    fighter.send(leftEndMessage((CharacterFighter)fighter));
                    this.sendToField(new GameFightLeaveMessage(fighter.getID()));
                    fighter.leaveFight();
                }
                break;
            default:
                logger.error("Incredible left from fighter {} " , fighter.getID());
        }
    }

    @Override
    public void endFight(FightTeam winners, FightTeam loosers) {
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);

        for (Fighter fighter : (Iterable<Fighter>) loosers.getFighters()::iterator) {
            super.addNamedParty((CharacterFighter)fighter, FightOutcomeEnum.RESULT_LOST);
            final short loosedHonor = (short) (FightFormulas.honorPoint(fighter, winners.getFighters(), loosers.getFighters(), true) / AntiCheat.deviserBy(getWinners().getFighters(), fighter, false));
            fighter.getPlayer().addHonor(loosedHonor, true);
            fighter.getPlayer().addDishonor(FightFormulas.calculateEarnedDishonor(fighter),true);
            this.myResult.results.add(
                    new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST,
                            fighter.getWave(),
                            new FightLoot(new int[0], 0),
                            fighter.getID(),
                            fighter.isAlive(),
                            (byte) fighter.getLevel(),
                            new FightResultPvpData[]{
                                    new FightResultPvpData(fighter.getPlayer().getAlignmentGrade(),
                                            DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade()).getPvP(),
                                            DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade() == 10 ? 10 : fighter.getPlayer().getAlignmentGrade() + 1).getPvP(),
                                            fighter.getPlayer().getHonor(),
                                            loosedHonor)}));
        }

        for (Fighter fighter : (Iterable<Fighter>) winners.getFighters()::iterator) {
            super.addNamedParty((CharacterFighter)fighter, FightOutcomeEnum.RESULT_VICTORY);
            final short loosedHonor = (short) (FightFormulas.honorPoint(fighter, winners.getFighters(), loosers.getFighters(), false) / AntiCheat.deviserBy(getEnnemyTeam(getWinners()).getFighters(), fighter, true));
            fighter.getPlayer().addHonor(loosedHonor, true);
            fighter.getPlayer().addDishonor(FightFormulas.calculateEarnedDishonor(fighter),true);
            this.myResult.results.add(
                    new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY,
                            fighter.getWave(),
                            new FightLoot(new int[0], 0),
                            fighter.getID(),
                            fighter.isAlive(),
                            (byte) fighter.getLevel(),
                            new FightResultPvpData[]{
                                    new FightResultPvpData(fighter.getPlayer().getAlignmentGrade(),
                                            DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade()).getPvP(),
                                            DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade() == 10 ? 10 : fighter.getPlayer().getAlignmentGrade() + 1).getPvP(),
                                            fighter.getPlayer().getHonor(),
                                            loosedHonor)}));
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
    public GameFightEndMessage leftEndMessage(CharacterFighter leaver) {
        short lossedHonor = FightFormulas.honorPoint(leaver, this.getEnnemyTeam(leaver.getTeam()).getFightersNotSummoned(), leaver.getTeam().getFighters().filter(x -> !x.hasSummoner()), true, false);
        leaver.getCharacter().addHonor(lossedHonor, true);
        leaver.getCharacter().addDishonor(FightFormulas.calculateEarnedDishonor(leaver),true);
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime),
                this.ageBonus,
                (short) 0,
                this.fighters()
                        .filter(x -> x.getSummoner() == null)
                        .map(fighter -> new FightResultPlayerListEntry(
                                fighter.getTeam().id == leaver.getTeam().id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY,
                                (byte) 0,
                                new FightLoot(new int[0], 0),
                                fighter.getID(),
                                fighter.isAlive(),
                                (byte) fighter.getLevel(),
                                new FightResultPvpData[]{
                                        new FightResultPvpData(fighter.getPlayer().getAlignmentGrade(),
                                                DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade()).getPvP(),
                                                DAO.getExps().getLevel(fighter.getPlayer().getAlignmentGrade() == 10 ? 10 : fighter.getPlayer().getAlignmentGrade() + 1).getPvP(),
                                                fighter.getPlayer().getHonor(),
                                                fighter.getID() == leaver.getID() ? lossedHonor : 0)}))
                        .collect(Collectors.toList()),
                new NamedPartyTeamWithOutcome[0]);
    }

}
