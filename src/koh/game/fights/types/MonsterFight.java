package koh.game.fights.types;

import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.actors.character.shortcut.ScoreType;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.entities.mob.MonsterDrop;
import koh.game.fights.*;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.DroppedItem;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.fight.*;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultExperienceData;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        final Fighter defFighter = new MonsterFighter(this, group.getMainCreature(), this.getNextContextualId(), group);
        player.addGameAction(new GameFight(attFighter, this));

        super.initFight(attFighter, defFighter);

        this.monsterGroup.getMonsters().forEach(mob -> super.joinFightTeam(new MonsterFighter(this, mob, this.getNextContextualId(), group), this.myTeam2, false, (short) -1, true));
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
                        fighter.send(leftEndMessage(fighter.asPlayer()));
                    this.sendToField(new GameFightLeaveMessage(fighter.getID()));
                    fighter.leaveFight();
                }
                break;
            default:
                logger.error("Incredible left from fighter {} ", fighter.getID());
        }

    }

    @Override
    public void endFight(FightTeam winners, FightTeam loosers) {
        final MonsterFighter[] deadMobs = this.myTeam2.getDeadFighters()
                .filter(fighter -> fighter instanceof MonsterFighter && !fighter.hasSummoner())
                .map(Fighter -> ((MonsterFighter) Fighter))
                .toArray(MonsterFighter[]::new);

        if (this.myResult == null)
            this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);
        else {
            this.myResult.duration = (int) (System.currentTimeMillis() - this.fightTime);
            this.myResult.results.removeIf(res -> ((FightResultPlayerListEntry) res).notOfficial);
            //On efface les anciens fakes results et on laisse celui des gens qui ont abondonnee maybe ils ont xp ?
        }
        final int teamPP = winners.getFighters().mapToInt(fr -> fr.getStats().getTotal(StatsEnum.PROSPECTING)).sum();
        final int baseKamas = loosers.equals(this.myTeam2) ? Arrays.stream(deadMobs).mapToInt(mob -> mob.getGrade().getMonster().getKamasWin(Fight.RANDOM)).sum() : 0; //sum min max
        final HashMap<MonsterDrop, Integer> droppedItems = new HashMap<>();

        for (Fighter fighter : (Iterable<Fighter>) winners.getFighters()::iterator) { //In stream.foreach you should use final var that suck
            if (fighter instanceof CharacterFighter) {
                super.addNamedParty(fighter.asPlayer(), FightOutcomeEnum.RESULT_VICTORY);
                final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter.asPlayer(), deadMobs));
                final int guildXp = FightFormulas.guildXpEarned(fighter.asPlayer(), exp), mountXp = FightFormulas.mountXpEarned(fighter.asPlayer(), exp);
                fighter.getPlayer().addExperience(exp.get(), false);
                final List<DroppedItem> loots = new ArrayList<>(7);
                Arrays.stream(deadMobs).forEachOrdered(mob -> {
                    FightFormulas.rollLoot(fighter, mob.getGrade(), teamPP, droppedItems,loots);
                });
                fighter.getPlayer().addScore(ScoreType.PVM_WIN);

                final int kamasWin = FightFormulas.computeKamas(fighter, baseKamas, teamPP);
                final int[] serializedLoots = new int[loots.size() * 2];
                for (int i = 0; i < serializedLoots.length; i += 2) {
                    serializedLoots[i] = loots.get(i / 2).getItem();
                    serializedLoots[i + 1] = loots.get(i / 2).getQuantity();
                }
                loots.forEach(lot -> {
                    final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), lot.getItem(), 63, fighter.getPlayer().getID(), lot.getQuantity(), EffectHelper.generateIntegerEffect(DAO.getItemTemplates().getTemplate(lot.getItem()).getPossibleEffects(), EffectGenerationType.NORMAL, DAO.getItemTemplates().getTemplate(lot.getItem()) instanceof Weapon));
                    if (fighter.getPlayer().getInventoryCache().add(item, true)) {
                        item.setNeedInsert(true);
                    }
                });
                loots.clear();

                this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(serializedLoots, kamasWin), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
                    {
                        this.experience = fighter.getPlayer().getExperience();
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

                fighter.getPlayer().addKamas(kamasWin);

            } else {
                this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[0]));
            }
        }

        for (Fighter fighter : (Iterable<Fighter>) loosers.getFighters()::iterator) {
            if (fighter.isPlayer()) {
                super.addNamedParty(fighter.asPlayer(), FightOutcomeEnum.RESULT_LOST);
                final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter.asPlayer(), deadMobs));
                final int guildXp = FightFormulas.guildXpEarned(fighter.asPlayer(), exp), mountXp = FightFormulas.mountXpEarned(fighter.asPlayer(), exp);
                fighter.getPlayer().addExperience(exp.get(), false);
                fighter.getPlayer().addScore(ScoreType.PVM_LOOSE);

                this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
                    {
                        this.experience = fighter.getPlayer().getExperience();
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
            } else {
                this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[0]));
            }
        }

        droppedItems.clear();

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
        fighter.send(new GameFightJoinMessage(false, !this.isStarted(), this.isStarted(),  this.getPlacementTimeLeft(), this.fightType.value));
    }

    @Override
    public GameFightEndMessage leftEndMessage(CharacterFighter fighter) {
        final MonsterFighter[] deadMobs = this.getEnnemyTeam(fighter.getTeam()).getDeadFighters()
                .filter(Fighter -> Fighter instanceof MonsterFighter)
                .map(Fighter -> ((MonsterFighter) Fighter))
                .toArray(MonsterFighter[]::new);

        final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter, deadMobs));
        final int guildXp = FightFormulas.guildXpEarned(fighter, exp), mountXp = FightFormulas.mountXpEarned(fighter, exp);
        fighter.getCharacter().addExperience(exp.get(), false);
        if (this.myResult == null)
            this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);
        else {
            this.myResult.duration = (int) (System.currentTimeMillis() - this.fightTime);
            this.myResult.results.removeIf(res -> ((FightResultPlayerListEntry) res).notOfficial);
        }

        this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), false, (byte) fighter.getLevel(), new FightResultExperienceData[]{new FightResultExperienceData() {
            {
                this.experience = fighter.getCharacter().getExperience();
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
        fighter.getTeam().getFightersNotSummoned()
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
        fighter.getFight().getEnnemyTeam(fighter.getTeam()).getFightersNotSummoned()
                .filter(hasAlreadLeft -> !this.myResult.results.stream().noneMatch(shit -> shit instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) shit).id == hasAlreadLeft.getID()))
                .forEach(ennemy -> {
                    this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, ennemy.getWave(), new FightLoot(new int[0], 0), ennemy.getID(), ennemy.isAlive(), (byte) ennemy.getLevel(), new FightResultExperienceData[0], true));
                });
        return this.myResult;
    }

}
