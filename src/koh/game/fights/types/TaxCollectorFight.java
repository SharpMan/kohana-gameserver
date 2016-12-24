package koh.game.fights.types;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.fight.Challenge;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.entities.kolissium.ArenaBattle;
import koh.game.entities.kolissium.KolizeumExecutor;
import koh.game.fights.*;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.TaxCollectorFighter;
import koh.game.fights.utils.AntiCheat;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TaxCollectorStateEnum;
import koh.protocol.messages.game.context.fight.*;
import koh.protocol.messages.game.guild.tax.GuildFightPlayersEnemiesListMessage;
import koh.protocol.messages.game.guild.tax.GuildFightPlayersEnemyRemoveMessage;
import koh.protocol.messages.game.guild.tax.TaxCollectorAttackedResultMessage;
import koh.protocol.types.game.character.CharacterMinimalPlusLookInformation;
import koh.protocol.types.game.context.fight.FightLoot;
import koh.protocol.types.game.context.fight.FightResultExperienceData;
import koh.protocol.types.game.context.fight.FightResultFighterListEntry;
import koh.protocol.types.game.context.fight.FightResultPlayerListEntry;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeamWithOutcome;
import koh.utils.Couple;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Melancholia on 12/9/16.
 */
public class TaxCollectorFight extends Fight {

    @Getter
    private final TaxCollector taxCollector;
    @Getter
    private final CopyOnWriteArrayList<Player> defenders;

    private final static Map<Integer, Couple<Integer, Short>> lastPositions = new HashMap<>();

    public TaxCollectorFight(DofusMap currentMap, WorldClient player, final TaxCollector taxCollector) {
        super(FightTypeEnum.FIGHT_TYPE_PvT, currentMap);
        this.taxCollector = taxCollector;
        final Fighter attFighter = new CharacterFighter(this, player);
        final Fighter defFighter = new TaxCollectorFighter(this,taxCollector);
        player.addGameAction(new GameFight(attFighter, this));
        this.defenders = new CopyOnWriteArrayList<Player>();
        this.startTimer(new CancellableScheduledRunnable(BACK_GROUND_WORKER, 40000) {
            @Override
            public void run() {
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                taxCollector.setState(TaxCollectorStateEnum.STATE_FIGHTING);
                for (Player defender : defenders) {
                    try{
                        if(defender.getClient().isGameAction(GameActionTypeEnum.FIGHT))
                            continue;
                        defender.getClient().abortGameActions();
                        //defender.getClient().endGameAction(GameActionTypeEnum.DEFEND_TAX_COLLECTOR);
                        /*if(!defender.getClient().canGameAction(GameActionTypeEnum.FIGHT))
                            continue;*/
                        lastPositions.put(defender.getID(), new Couple<>(defender.getCurrentMap().getId(), defender.getCell().getId()));
                        defender.teleport(map.getId(), 0);
                        final Fighter fighter = new CharacterFighter(taxCollector.getCurrent_fight(), defender.getClient());
                        final GameAction fightAction = new GameFight(fighter, taxCollector.getCurrent_fight());

                        defender.getClient().addGameAction(fightAction);
                        joinFightTeam(fighter, myTeam2, false, (short) -1, true);
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
        }, "defenderTimer");

        super.initFight(attFighter, defFighter);
    }

    @Override
    public void leaveFight(Fighter fighter) {
        switch (this.fightState) {
            case STATE_PLACE: //Ejected
                if (fighter == fighter.getTeam().leader)
                    break;
                this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, fighter.getTeam().getFightTeamInformations()));
                this.sendToField(new GameFightRemoveTeamMemberMessage(this.fightId, fighter.getTeam().id, fighter.getID()));
                defenders.stream().forEach(pl -> pl.send(new GuildFightPlayersEnemyRemoveMessage(taxCollector.getIden(), fighter.getID())));
                fighter.leaveFight();
                break;
            case STATE_ACTIVE:
                if (fighter.tryDie(fighter.getID(), true) != -3) {
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
    public void joinFightTeam(Fighter fighter, FightTeam team, boolean leader, short cell, boolean sendInfos) {
        super.joinFightTeam(fighter,team,leader,cell,sendInfos);
        if(fightState == FightState.STATE_PLACE && team == this.myTeam1){
            defenders.stream().forEach(pl -> pl.send(new GuildFightPlayersEnemiesListMessage(myTeam1.getFighters()
                    .filter(fighter22 -> fighter22 instanceof CharacterFighter)
                    .map(fighter1 -> fighter1.getPlayer().toBaseInformations())
                    .toArray(CharacterMinimalPlusLookInformation[]::new),
                    taxCollector.getIden())
            ));

        }
    }

    public boolean isFull(FightTeam team){
        if ((this.getFightState() != FightState.STATE_PLACE)
                || ((team.getMyFighters().size()+ defenders.size()) >= 8)
                || (this.getFreeSpawnCell(team) == null)) {
            return true;
        }
        return false;
    }

    @Override
    public void endFight(FightTeam winners, FightTeam loosers) {
        this.myResult = new GameFightEndMessage(System.currentTimeMillis() - this.fightTime, this.ageBonus, this.lootShareLimitMalus);

        try {
            final double butin = Math.abs(this.challenges.cellSet().stream()
                    .filter(c -> c.getColumnKey() == winners && !c.getValue().isFailed())
                    .mapToInt(c -> Challenge.getXPBonus(c.getRowKey()))
                    .sum() * 0.01
            ) + 1;
            final boolean destruction = myTeam2 == loosers;
            final int honorShared = (int) Math.floor(taxCollector.getHonor() / winners.getFighters().count());
            final int experienceShared = (int) Math.floor(taxCollector.getExperience() / winners.getFighters().count());
            final int kamasShared = (int) Math.floor(taxCollector.getKamas() / winners.getFighters().count());
            final int teamPP = winners.getFighters().mapToInt(fr -> fr.getStats().getTotal(StatsEnum.PROSPECTING)).sum();
            final ArrayList<Integer> items = new ArrayList<>(taxCollector.getGatheredItem().size() * 2);
            taxCollector.getGatheredItem().forEach((e,v) -> {
                IntStream.rangeClosed(1, v).forEach(i -> items.add(e));
            });

            Collections.shuffle(items);
            int num2 = 0;
            for (Fighter fighter : (Iterable<Fighter>) winners.getFighters().sorted((e1, e2) -> Integer.compare(e2.getStats().getTotal(StatsEnum.PROSPECTING), e1.getStats().getTotal(StatsEnum.PROSPECTING)))::iterator) {
                if (fighter instanceof TaxCollectorFighter) {
                    this.myResult.results.add(new FightResultFighterListEntry(FightOutcomeEnum.RESULT_VICTORY, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive()));
                    this.map.spawnActor(taxCollector);
                    taxCollector.getGuild().sendToField(new TaxCollectorAttackedResultMessage(false,taxCollector.toTaxCollectorBasicInformations(),taxCollector.getGuild().getBasicGuildInformations()));
                    taxCollector.incrementVictory();
                    DAO.getTaxCollectors().updateSummmary(taxCollector);
                    continue;
                }
                super.addNamedParty((CharacterFighter) fighter, FightOutcomeEnum.RESULT_VICTORY);

                if (fighter.isLeft())
                    continue;
                int kamas = destruction ? kamasShared : 0;
                fighter.getPlayer().addKamas(kamas);
                final int[] serializedLoots;
                if(destruction){
                    fighter.getPlayer().addHonor(honorShared,true);
                    fighter.getPlayer().addExperience(experienceShared);
                    final List<DroppedItem> loots = new ArrayList<>(7);
                    final int num3 = (int) Math.ceil((double)items.size() * ((double) fighter.getStats().getTotal(StatsEnum.PROSPECTING) / (double) teamPP)) + num2;

                    while (num2 < num3 && num2 < items.size())
                    {
                        int id  = items.get(num2);
                        final Optional<DroppedItem> item = loots.stream()
                                .filter(dr -> dr.getItem() == id)
                                .findFirst();
                        if (item.isPresent()) {
                            item.get().accumulateQuantity();
                        } else
                            loots.add(new DroppedItem(id , 1));

                        num2++;
                    }

                    serializedLoots = new int[loots.size() * 2];
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

                }
                else{
                    serializedLoots = new int[0];
                }


                this.myResult.results.add(
                        new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_VICTORY,
                                fighter.getWave(),
                                new FightLoot(serializedLoots, kamas),
                                fighter.getID(),
                                fighter.isAlive(),
                                (byte) fighter.getLevel(),
                                new FightResultExperienceData[]{
                                    new FightResultExperienceData(fighter.getPlayer().getExperience(), true, DAO.getExps().getPlayerMinExp(fighter.getLevel()), true, DAO.getExps().getPlayerMaxExp(fighter.getLevel()), true, destruction ? experienceShared : 0, fighter.getLevel() < 200, 0, false, 0, false, false, (byte) 0)}));

            }

            for (Fighter fighter : (Iterable<Fighter>) loosers.getFighters()::iterator) {
                if (fighter instanceof TaxCollectorFighter) {
                    this.myResult.results.add(new FightResultFighterListEntry(FightOutcomeEnum.RESULT_LOST, fighter.getWave(), new FightLoot(new int[0], 0), fighter.getID(), fighter.isAlive()));
                    taxCollector.getGuild().getTaxCollectors().remove(taxCollector);
                    DAO.getTaxCollectors().remove(taxCollector.getIden());
                    taxCollector.getGuild().sendToField(new TaxCollectorAttackedResultMessage(true,taxCollector.toTaxCollectorBasicInformations(),taxCollector.getGuild().getBasicGuildInformations()));
                    continue;
                }
                super.addNamedParty(fighter.asPlayer(), FightOutcomeEnum.RESULT_LOST);

                if (fighter.isLeft())
                    continue;

                this.myResult.results.add(
                        new FightResultPlayerListEntry(FightOutcomeEnum.RESULT_LOST,
                                fighter.getWave(),
                                new FightLoot(new int[0], 0),
                                fighter.getID(),
                                fighter.isAlive(),
                                (byte) fighter.getLevel(),
                                new FightResultExperienceData[0]/*{
                                    new FightResultExperienceData(fighter.getPlayer().getKolizeumRate().getScreenRating(), true, 0, true, 4000, true, cote, true, 0, false, 0, false, false, (byte) 0)}*/
                        ));
            }

            taxCollector.setCurrent_fight(null);
            taxCollector.setState(TaxCollectorStateEnum.STATE_COLLECTING);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        super.endFight();
    }

    public static void teleportLastPosition(Player p) {
        p.fightTeleportation(lastPositions.get(p.getID()).first, lastPositions.get(p.getID()).second);
    }

    @Override
    public int getStartTimer() {
        return 60;
    }

    @Override
    public int getTurnTime() {
        return 45000;
    }

    @Override
    protected void sendGameFightJoinMessage(Fighter fighter) {
        fighter.send(new GameFightJoinMessage(false, !this.isStarted(), this.isStarted(),  this.getPlacementTimeLeft(), this.fightType.value));
    }

    @Override
    public GameFightEndMessage leftEndMessage(CharacterFighter f) {
        return new GameFightEndMessage((int) (System.currentTimeMillis() - this.fightTime), this.ageBonus, this.lootShareLimitMalus, this.fighters().filter(x -> x.getSummoner() == null).map(x -> new FightResultPlayerListEntry(x.getTeam().id == f.getTeam().id ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_VICTORY, (byte) 0, new FightLoot(new int[0], 0), x.getID(), fightTime == -1 ? true : x.isAlive(), (byte) x.getLevel(), new FightResultExperienceData[0])).collect(Collectors.toList()), new NamedPartyTeamWithOutcome[0]);
    }
}




