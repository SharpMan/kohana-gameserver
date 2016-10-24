package koh.game.fights.types;

import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.fight.*;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.game.entities.mob.MonsterDrop;
import koh.game.fights.*;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.DroppedItem;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.fight.*;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultExperienceData;
import koh.protocol.types.game.context.fight.FightResultFighterListEntry;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Melancholia on 12/30/15.
 */
public class MonsterFight extends Fight {

    @Getter
    private MonsterGroup monsterGroup;


    static final Random rnd = new Random();

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
        while (this.challenges.size() < 1 + (rnd.nextBoolean() ? 1 : 0)){
            final int key = DAO.getChallenges().pop();
            if(!Challenge.canBeUsed(this, myTeam1, key)){
                continue;
            }
            try {
                final Challenge chall = DAO.getChallenges().find(key).getDeclaredConstructor(CHALLENGE_CONSTRUCTOR).newInstance(this, myTeam1);
                this.challenges.put(key, myTeam1,chall);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        }
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

    final private boolean isRazielle = DAO.getSettings().getIntElement("World.ID") == 2;

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
        final double butin = (this.challenges.cellSet().stream()
                .filter(c -> c.getColumnKey() == winners && !c.getValue().isFailed())
                .mapToInt(c -> Challenge.getXPBonus(c.getRowKey()))
                .sum() * 0.01) + 1;


        for (Fighter fighter : (Iterable<Fighter>) winners.getFighters()::iterator) { //In stream.foreach you should use final var that suck
            if (fighter instanceof CharacterFighter) {
                super.addNamedParty(fighter.asPlayer(), FightOutcomeEnum.RESULT_VICTORY);
                final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter.asPlayer(), deadMobs,butin));
                final int guildXp = FightFormulas.guildXpEarned(fighter.asPlayer(), exp), mountXp = FightFormulas.mountXpEarned(fighter.asPlayer(), exp);
                final int oldLevel = fighter.getPlayer().getLevel();
                fighter.getPlayer().addExperience(exp.get(), false);
                final int upRanked = fighter.getPlayer().getLevel() - oldLevel;
                final List<DroppedItem> loots = new ArrayList<>(7);
                Arrays.stream(deadMobs).forEachOrdered(mob -> {
                    FightFormulas.rollLoot(fighter, mob.getGrade(), teamPP, droppedItems,loots,butin);
                });
                loots.add(new DroppedItem(11503, Arrays.stream(deadMobs).mapToInt(MonsterFighter::getLevel).sum() / 3));
                fighter.getPlayer().addScore(ScoreType.PVM_WIN);


                int kamasWin = FightFormulas.computeKamas(fighter, baseKamas, teamPP);
                if(isRazielle && upRanked > 0){
                    try {
                        if (100 > oldLevel && fighter.getPlayer().getLevel() >= 100) {
                            kamasWin += 10000;
                        } else if (fighter.getPlayer().getLevel() >= 200) {
                            kamasWin += 25000;
                        }
                        for (int i = oldLevel + 1; i <= (oldLevel + upRanked); ++i) {
                            //System.out.println(i);
                            if (i % 20 == 0) {
                                kamasWin += 50000;
                            } else
                                kamasWin += 150;
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
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

                if(fighter.getPlayer().getInventoryCache().hasItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS)){
                    final InventoryItem pet = fighter.getPlayer().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS);
                    if(pet != null && pet instanceof PetsInventoryItem){
                        ((PetsInventoryItem)pet).eat(fighter.getPlayer(),deadMobs);
                    }
                }

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
                this.myResult.results.add(new FightResultFighterListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive()));
                //this.myResult.results.add(new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive(), (byte) fighter.getLevel(), new FightResultExperienceData[0]));
            }
        }

        for (Fighter fighter : (Iterable<Fighter>) loosers.getFighters()::iterator) {
            if (fighter.isPlayer()) {
                super.addNamedParty(fighter.asPlayer(), FightOutcomeEnum.RESULT_LOST);
                final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter.asPlayer(), deadMobs, 1));
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

                this.myResult.results.add(new FightResultFighterListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive()));
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

        final AtomicInteger exp = new AtomicInteger(FightFormulas.computeXpWin(fighter, deadMobs,1));
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
