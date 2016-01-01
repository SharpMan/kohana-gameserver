package koh.game.fights.types;

import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.environments.DofusMap;
import koh.game.fights.*;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.fight.*;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultExperienceData;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Melancholia on 12/30/15.
 */
public class MonsterFight extends Fight {

    @Getter
    private MonsterGroup monsterGroup;


    public MonsterFight(DofusMap map, WorldClient player, MonsterGroup group) {
        super(FightTypeEnum.FIGHT_TYPE_PvM, map);
        this.monsterGroup = group;
        this.ageBonus = group.getGameRolePlayGroupMonsterInformations().ageBonus;
        this.lootShareLimitMalus = 0;

        logger.debug("PVM_FIGHT Launched : Player={} MapId={}", player.getCharacter().getNickName(), map.getId());

        final Fighter attFighter = new CharacterFighter(this, player);
        final Fighter defFighter = new MonsterFighter(this, group.getMainCreature(), this.nextID(), group);
        player.addGameAction(new GameFight(attFighter, this));

        super.initFight(attFighter, defFighter);

        this.monsterGroup.getMonsters().forEach(mob -> super.joinFightTeam(new MonsterFighter(this, mob, this.nextID(), group), this.myTeam2, false, (short) -1, true));
    }


    @Override
    public void leaveFight(Fighter fighter) {
        switch (this.fightState) {
            case STATE_PLACE: //Ejected
                if (fighter == fighter.getTeam().leader)
                    break;
                this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, fighter.getTeam().getFightTeamInformations()));
                this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, fighter.getTeam().id, fighter.getID()));
                fighter.leaveFight();
                break;
            case STATE_ACTIVE:
                if (fighter.tryDie(fighter.getID(), true) != -3) {
                    if (fighter instanceof CharacterFighter)
                        fighter.send(leftEndMessage((CharacterFighter) fighter));
                    this.sendToField(new GameFightLeaveMessage(fighter.getID()));
                    fighter.leaveFight();
                }
                break;
            default:
                logger.error("Incredible left from fighter {} ", fighter.getID());
        }

    }

    @Override
    public void endFight(FightTeam Winners, FightTeam Loosers) {

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
    public GameFightEndMessage leftEndMessage(CharacterFighter fighter) {
        final MonsterFighter[] deadMobs = this.getEnnemyTeam(this.getTeam1()).getDeadFighters()
                .filter(Fighter -> Fighter instanceof MonsterFighter)
                .map(Fighter -> ((MonsterFighter) Fighter))
                .toArray(MonsterFighter[]::new);
        
        final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter, deadMobs));
        int guildXp = FightFormulas.guildXpEarned(fighter, exp), mountXp = FightFormulas.mountXpEarned(fighter, exp);
        fighter.character.addExperience(exp.get(), false);
        if (this.myResult == null)
            this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);
        else {
            this.myResult.duration = (int) (System.currentTimeMillis() - this.fightTime);
            this.myResult.results.removeIf(res -> ((FightResultPlayerListEntry) res).notOfficial);
        }

        this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), false, (byte) fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
            {
                this.experience = fighter.character.getExperience();
                this.showExperience = true;
                this.experienceLevelFloor = DAO.getExps().getPlayerMinExp(fighter.getLevel());
                this.showExperienceLevelFloor = true;
                this.experienceNextLevelFloor = DAO.getExps().getPlayerMaxExp(fighter.getLevel());
                this.showExperienceNextLevelFloor = fighter.getLevel() < 200;
                this.experienceFightDelta = exp.get();
                this.showExperienceFightDelta = true;
                this.experienceForGuild = guildXp;
                this.showExperienceForGuild = guildXp > 0;
                this.experienceForMount = mountXp;
                this.showExperienceForMount = mountXp > 0;
            }
        }}));
        //Explicaion about this shit , on verifie si on n'a pas attribue a un de vos camarades un resultat car il aurait peut etre abondonne avant toi :)
        fighter.getTeam().getFighters(false)
                .filter(hasAlreadLeft -> !this.myResult.results.stream().noneMatch(shit -> shit instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) shit).id == hasAlreadLeft.getID()))
                .forEach(ally -> {
                    this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, ally.getWave(), new FightLoot(new int[0], 0), ally.getID(), ally.isAlive(), (byte) ally.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
                        {
                            this.experience = 0;
                            this.showExperience = false;
                            this.experienceLevelFloor = 0;
                            this.showExperienceLevelFloor = false;
                            this.experienceNextLevelFloor = 0;
                            this.showExperienceNextLevelFloor = false;
                            this.experienceFightDelta = 0;
                            this.showExperienceFightDelta = false;
                            this.experienceForGuild = 0;
                            this.showExperienceForGuild = false;
                            this.experienceForMount = 0;
                            this.showExperienceForMount = false;

                        }
                    }}, true));
                });
        fighter.getFight().getEnnemyTeam(fighter.getTeam()).getFighters(false)
                .filter(hasAlreadLeft -> !this.myResult.results.stream().noneMatch(shit -> shit instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) shit).id == hasAlreadLeft.getID()))
                .forEach(ennemy -> {
                    this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, ennemy.getWave(), new FightLoot(new int[0], 0), ennemy.getID(), ennemy.isAlive(), (byte) ennemy.getLevel(), new FightResultExperienceData[0], true));
                });
        return this.myResult;
    }

}
