package koh.game.fights;

import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.environments.CrossZone;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.environments.cells.IZone;
import koh.game.entities.environments.cells.Lozenge;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight.FightLoopState;
import koh.game.fights.effects.buff.*;
import koh.game.fights.fighters.*;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightGlyph;
import koh.game.fights.layers.FightPortal;
import koh.game.fights.types.AgressionFight;
import koh.protocol.client.Message;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.actions.fight.GameActionFightDeathMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.FightEntityDispositionInformations;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.IdentifiedEntityDispositionInformations;
import koh.protocol.types.game.context.fight.*;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Enumerable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author Neo-Craft
 */
public abstract class Fighter extends IGameActor implements IFightObject {

    public Fighter(Fight fight, Fighter summoner) {
        this.fight = fight;
        this.buff = new FighterBuff();
        this.spellsController = new FighterSpell(buff);
        this.states = new FighterState(this);
        this.summoner = summoner;
        this.mutex = new Object();
    }

    @Getter
    private static final Random RANDOM = new Random();

    protected boolean dead;
    @Setter
    @Getter
    protected boolean left;
    @Getter
    @Setter
    protected FightTeam team;
    @Getter
    @Setter
    protected Fight fight;
    @Getter
    @Setter
    protected FightCell myCell;
    @Getter
    @Setter
    protected int usedAP, usedMP, turnRunning = 19, shieldPoints;
    @Getter
    protected GenericStats stats;
    @Getter
    protected Fighter summoner;
    @Getter
    @Setter
    protected GameActionFightInvisibilityStateEnum visibleState = GameActionFightInvisibilityStateEnum.VISIBLE;
    protected int[] previousPositions = new int[0];
    @Getter
    protected CopyOnWriteArrayList<Short> previousCellPos = new CopyOnWriteArrayList<>(), previousFirstCellPos = new CopyOnWriteArrayList<>();
    private final MapPoint mapPointCache = MapPoint.fromCellId(0);
    @Getter
    protected final byte wave = 0; //Dimension obscure
    @Getter
    protected FighterSpell spellsController;
    @Getter
    protected FighterBuff buff;
    @Getter
    protected FighterState states;
    @Getter
    protected AtomicInteger nextBuffUid = new AtomicInteger();
    protected short nextCell;
    protected byte nextDir;
    @Getter
    @Setter
    protected boolean turnReady = true;
    public Object temperoryLook = new Object();
    @Getter
    @Setter
    private short lastCellSeen;
    @Getter
    private Object mutex;

    /**
     * Virtual Method
     */
    public void endFight() {

    }

    public MapPoint getMapPoint() {
        return mapPointCache;
    }

    public int setCell(FightCell cell) {
        return this.setCell(cell, true);
    }


    public Stream<Fighter> getSummonedCreature() {
        return this.team.getAliveFighters()
                .filter(fighter -> fighter.getSummonerID() == this.getID());
    }

    public int setCell(FightCell cell, boolean runEvent) {
        int addResult;
        if (this.myCell != null) {
            previousCellPos.add(myCell.getId());
            this.myCell.removeObject(this); // On vire le fighter de la cell:
            if (this.myCell.hasGameObject(FightObjectType.OBJECT_PORTAL)) {
                ((FightPortal) this.myCell.getObjects(FightObjectType.OBJECT_PORTAL)[0]).enable(this, true);
            }
            if (this.fight.getFightState() == FightState.STATE_PLACE) {
                if (!ArrayUtils.contains(previousPositions, myCell.Id)) {
                    this.previousPositions = ArrayUtils.add(previousPositions, this.myCell.Id);
                }
            } /*else {
                this.previousCellPos.add(this.myCell.Id);
            }*/
            if (cell != null) {
                myCell.getGlyphStream(gl -> !cell.contains(gl)).forEach(gl -> {
                    //Osef du result ils ne peux pas mourir
                    this.getBuff().getAllBuffs().filter(bf -> bf.getCastInfos().glyphId == gl.ID).forEach(buff -> {
                        buff.removeEffect();
                        fight.sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, buff.castInfos.caster.getID(), getID(), buff.getCastInfos().spellId));
                    });
                });

                if (cell.hasObject(FightObjectType.OBJECT_GLYPHE)) {
                    final FightGlyph[] glyphes = cell.getGlyphes(gl -> !myCell.contains(gl));

                    this.myCell = cell;
                    for (FightGlyph glyph : glyphes) {

                        if ((!previousCellPos.isEmpty() && this.fight.getCell(previousCellPos.get(previousCellPos.size() - 1)).hasGameObject(glyph))
                                || glyph.getLastTurnActivated() == this.fight.getFightWorker().fightTurn) {
                            continue;
                        }
                        glyph.setLastTurnActivated(this.fight.getFightWorker().fightTurn);
                        glyph.targets.add(this);
                        addResult = glyph.loadEnnemyTargetsAndActive(this, BuffActiveType.ACTIVE_ENDMOVE);
                        if (addResult == -3 || addResult == -2) {
                            return addResult;
                        }
                    }
                }
            }

        }


        this.myCell = cell;

        if (this.myCell != null) {
            this.mapPointCache.set_cellId(myCell.Id);
            addResult = this.myCell.addObject(this, runEvent);

            if (addResult == -3 || addResult == -2) {
                return addResult;
            }
        }

        if (runEvent) {
            final int result = onCellChanged();
            if (result != -1) {
                return result;
            }
        }
        return this.tryDie(this.ID);
    }

    public int onCellChanged() {
        if (this.myCell != null) {
            return this.buff.endMove();
        }
        return -1;
    }

    protected void initFighter(GenericStats statsToMerge, int ID) {
        this.stats = new GenericStats();
        this.stats.merge(statsToMerge);

        this.ID = ID;
    }

    public boolean isMarkedDead() {
        return this.isDead(); //TODO delete above dead function
    }

    /*public int GetFighterOutcome() {
     if(this.left)
     return FightOutcomeEnum.RESULT_LOST;
     boolean flag1 = !this.team.hasFighterAlive();
     boolean flag2 = !this.team.hasFighterAlive();
     if (!flag1 && flag2) {
     return FightOutcomeEnum.RESULT_VICTORY;
     }
     return flag1 && !flag2 ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_DRAW;
     }*/
    public void leaveFight() {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            // On le vire de l'equipe
            team.fighterLeave(this);
            fight.tryStartFight();
        }

        this.left = true;

        // On le vire de la cell
        myCell.removeObject(this);
    }

    public int tryDie(int casterId) {
        return tryDie(casterId, false);
    }

    public abstract GameFightFighterLightInformations getGameFightFighterLightInformations();

    public int tryDie(int casterId, boolean force) {
        /*if (force) {
            this.setLife(0);
        }*/
        if (this.getLife() <= 0 || force) {
            this.fight.startSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH);
            //SendGameFightLeaveMessage
            this.fight.sendToField(new GameActionFightDeathMessage(ActionIdEnum.ACTION_CHARACTER_DEATH, casterId, this.ID));

            final Fighter[] aliveFighters = this.team.getAliveFighters()
                    .filter(fighter -> fighter.getSummonerID() == this.ID)
                    .toArray(Fighter[]::new);

            for (final Fighter fighter : aliveFighters) {
                if (fighter instanceof SummonedFighter)
                    ((SummonedFighter) fighter).tryDieSilencious(this.ID, true);
                else if (fighter instanceof SlaveFighter)
                    ((SlaveFighter) fighter).tryDieSilencious(this.ID, true);
                else
                    fighter.tryDie(this.ID, true);
            }

            /*this.team.getAliveFighters()
                    .filter(x -> x.getSummonerID() == this.ID)
                    .forEachOrdered(fighter -> fighter.tryDie(this.ID, true));*/

            if (this.fight.getActivableObjects().containsKey(this)) {
                this.fight.getActivableObjects().get(this).stream().forEach(y -> y.remove());
            }

            for (final Fighter fighter : aliveFighters) {
                fighter.getBuff().getBuffsDec().values().forEach(list -> {
                    for (BuffEffect buff : (Iterable<BuffEffect>) list.parallelStream()::iterator) {
                        if (buff.caster == this)
                            fighter.getBuff().debuff(buff);
                    }
                });
            }

            /*for(Fighter fr : (Iterable<Fighter>) this.fight.getAliveFighters()::iterator){
                fr.getBuff().getBuffsDec().values().forEach(list -> {
                    for(BuffEffect buff : (Iterable<BuffEffect>) list.parallelStream()::iterator){
                        if(buff.caster == this)
                            fr.getBuff().debuff(buff);
                    }
                });
            }*/

            myCell.removeObject(this);

            this.fight.endSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH, false);
            this.dead = true;

            if (!hasSummoner() && this.fight.tryEndFight()) {
                return -3;
            }
            if (this.fight.getCurrentFighter() == this) {
                this.fight.fightLoopState = FightLoopState.STATE_END_TURN;
            }
            return -2;
        }
        return -1;
    }

    public int beginTurn() {
        if (this.fight.getActivableObjects().containsKey(this)) {
            this.fight.getActivableObjects().get(this).stream()
                    .filter((objs) -> (objs instanceof FightPortal))
                    .forEach((obj) -> {
                        ((FightPortal) obj).forceEnable(this);
                    });
            this.fight.getActivableObjects().get(this).stream().filter(obj -> obj instanceof FightGlyph).forEach(y -> y.decrementDuration());
            this.fight.getActivableObjects().get(this).removeIf(fightObject -> fightObject.getObjectType() == FightObjectType.OBJECT_GLYPHE && fightObject.duration <= 0);

        }

        final int buffResult = this.buff.beginTurn();
        if (buffResult != -1) {
            return buffResult;
        }

        this.previousFirstCellPos.add(this.myCell.Id);
        return myCell.beginTurn(this);
    }

    public void middleTurn() {
        this.usedAP = 0;
        this.usedMP = 0;
    }

    public int endTurn() {
        this.fight.getActivableObjects().values().stream().forEach((objects) -> {
            objects.stream().filter((objs) -> (objs instanceof FightPortal)).forEach((obj) ->
                    ((FightPortal) obj).enable(this)
            );
        });
        this.spellsController.endTurn();
        final int buffResult = this.buff.endTurn();
        if (buffResult != -1) {
            return buffResult;
        }
        return myCell.endTurn(this);
    }

    public Stream<FightActivableObject> getActivatedObjects() {
        try {
            return this.fight.getActivableObjects().get(this).stream();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public boolean isDead() {
        return this.getLife() <= 0 || dead;
    }

    public int currentLife, currentLifeMax;

    public void setLife(int value) {
        this.currentLife = value - (this.stats.getTotal(StatsEnum.VITALITY) + this.stats.getTotal(StatsEnum.HEAL)) /*+ this.stats.getTotal(StatsEnum.ADD_VIE)*/;
    }

    public int getLife() {
        return this.currentLife + (this.stats.getTotal(StatsEnum.VITALITY) + this.stats.getTotal(StatsEnum.HEAL));
    }

    public void setLifeMax(int value) {
        this.currentLifeMax = value - (this.stats.getTotal(StatsEnum.VITALITY) + this.stats.getTotal(StatsEnum.HEAL));
    }

    public int getMaxLife() {
        return this.currentLifeMax + this.stats.getTotal(StatsEnum.VITALITY) + this.stats.getTotal(StatsEnum.HEAL);
    }

    public int getLifePercentage() {
        double percentage = ((double) getLife() / (double) this.getMaxLife());
        return (int) (percentage * 100);
    }

    public void showCell(int Cell, boolean team) {
        if (team) {
            this.team.sendToField(new ShowCellMessage(this.ID, Cell));
        } else {
            this.fight.sendToField(new ShowCellMessage(this.ID, Cell));
        }
    }

    public abstract int getLevel();

    public abstract short getMapCell();


    public boolean canBeginTurn() {
        if (this.isDead()) {
            return false; // Mort    
        }
        return true;
    }

    public int getMaxAP() {
        return this.stats.getTotal(StatsEnum.ACTION_POINTS);
    }

    public int getMaxMP() {
        return this.stats.getTotal(StatsEnum.MOVEMENT_POINTS);
    }

    public int getAP() {
        return this.stats.getTotal(StatsEnum.ACTION_POINTS, this instanceof CharacterFighter) - this.usedAP;

    }

    public int getMP() {
        return this.stats.getTotal(StatsEnum.MOVEMENT_POINTS, this instanceof CharacterFighter) - this.usedMP;

    }

    public boolean isAlive() {
        return this.getLife() > 0 && !this.left && !this.dead;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightFighterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions);
    }

    /*public EntityDispositionInformations getEntityDispositionInformations(player character) {
     return new FightEntityDispositionInformations(character != null ? (this.isVisibleFor(character) ? this.getCellId() : -1) : this.getCellId(), this.direction, getCarrierActorId());
     }*/
    public int getAPCancelling() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.WISDOM) / 4) + this.stats.getTotal(StatsEnum.ADD_RETRAIT_PA);
    }

    public int getMPCancelling() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.WISDOM) / 4) + this.stats.getTotal(StatsEnum.ADD_RETRAIT_PM);
    }

    public int getAPDodge() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.WISDOM) / 4) + this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY);
    }

    public int getMPDodge() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.WISDOM) / 4) + this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY);
    }

    public int getTackledMP() { //Sould be implements in summoner,getMonster return 0
        if (this.visibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter tackler : Pathfunction.getEnnemyNear(fight, team, this.getCellId(), false)) {
            if (num1 == 0.0) {
                num1 = this.getTacklePercent(tackler);
            } else {
                num1 *= this.getTacklePercent(tackler);
            }
        }
        if (num1 == 0.0) {
            return 0;
        }
        double num2 = 1.0 - num1;
        if (num2 < 0.0) {
            num2 = 0.0;
        } else if (num2 > 1.0) {
            num2 = 1.0;
        }
        return (int) Math.ceil((double) this.getMP() * num2);
    }

    public Short[] getCastZone(SpellLevel spellLevel, short cell) {
        int num = spellLevel.getRange()
                + this.buff.getAllBuffs()
                .filter(buff -> buff instanceof BuffAddSpellRange && buff.castInfos.effect.diceNum == spellLevel.getSpellId())
                .mapToInt(buff -> buff.castInfos.effect.value)
                .sum();

        if (spellLevel.isRangeCanBeBoosted()) {
            int val1 = num + this.stats.getTotal(StatsEnum.ADD_RANGE);
            if (val1 < spellLevel.getMinRange()) {
                val1 = spellLevel.getMinRange();
            }
            num = Math.min(val1, 280);
        }
        return this.getCastZone(num, spellLevel, cell);
    }

    public Short[] getCastZone(final int num, SpellLevel spellLevel, short cell) {
        IZone shape;
        if (spellLevel.isCastInDiagonal() && spellLevel.isCastInLine()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num) {
                {
                    allDirections = true;
                }
            };
        } else if (spellLevel.isCastInLine()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num);
        } else if (spellLevel.isCastInDiagonal()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num) {
                {
                    diagonal = true;
                }
            };
        } else {
            shape = new Lozenge((byte) spellLevel.getMinRange(), (byte) num, this.fight.getMap());
        }
        return shape.getCells(cell);

    }


    public boolean hasState(int stateId) {
        return this.states.hasState(FightStateEnum.valueOf(stateId));
    }

    public int getTackledAP() {
        if (this.visibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter tackler : Pathfunction.getEnnemyNear(fight, team, this.getCellId())) {
            {
                if (num1 == 0.0) {
                    num1 = this.getTacklePercent(tackler);
                } else {
                    num1 *= this.getTacklePercent(tackler);
                }
            }
        }
        if (num1 == 0.0) {
            return 0;
        }
        double num2 = 1.0 - num1;
        if (num2 < 0.0) {
            num2 = 0.0;
        } else if (num2 > 1.0) {
            num2 = 1.0;
        }
        return (int) Math.ceil((double) this.getAP() * num2);
    }

    protected double getTacklePercent(Fighter tackler) {
        if (tackler.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK) == -2) {
            return 0.0;
        } else {
            return (double) (this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE) + 2) / (2.0 * (double) (tackler.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK) + 2));
        }
    }

    @Override
    public EntityDispositionInformations getEntityDispositionInformations(Player character) {
        return new FightEntityDispositionInformations(character == null || this.isVisibleFor(character) ? this.getCellId() : -1, this.direction, getCarrierActorId());
    }

    public int getCarrierActorId() {
        final Optional<BuffEffect> Option = this.buff.getAllBuffs().filter(x -> x instanceof BuffPorter && x.duration != 0).findFirst();
        return Option.isPresent() ? Option.get().caster.getID() : 0;
    }

    public Fighter getCarriedActor() {
        final Optional<BuffEffect> Option = this.buff.getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).findFirst();
        return Option.isPresent() ? Option.get().target : null;
    }

    public Fighter getCarrierActor() {
        final Optional<BuffEffect> Option = this.buff.getAllBuffs().filter(x -> x instanceof BuffPorter && x.duration != 0).findFirst();
        return Option.isPresent() ? Option.get().caster : null;
    }

    public boolean isVisibleFor(Player character) {
        return this.getVisibleStateFor(character) != GameActionFightInvisibilityStateEnum.INVISIBLE.value;
    }

    public boolean isVisibleFor(Fighter fighter) {
        if (this.team.getAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.summoner.getID() == this.ID)) {
            return true;
        } else if (fighter == null) {
            return this.visibleState != GameActionFightInvisibilityStateEnum.INVISIBLE;
        } else {
            return !fighter.isFriendlyWith(this) || this.visibleState == GameActionFightInvisibilityStateEnum.VISIBLE ? this.visibleState != GameActionFightInvisibilityStateEnum.INVISIBLE : true;
        }
    }


    public int getSummonerID() {
        return this.summoner == null ? 0 : this.summoner.getID();
    }

    public byte getVisibleStateFor(Player character) {
        if (this.team.getAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.summoner.getID() == this.ID)) {
            return GameActionFightInvisibilityStateEnum.VISIBLE.value;
        } else if (character == null || character.getClient() == null || character.getFighter() == null || character.getFight() != this.fight) {
            return this.visibleState.value;
        } else {
            return !character.getFighter().isFriendlyWith(this) || this.visibleState == GameActionFightInvisibilityStateEnum.VISIBLE ? this.visibleState.value : GameActionFightInvisibilityStateEnum.DETECTED.value;
        }
    }

    public boolean isMyFriend(Player character) {
        if (character == null || character.getClient() == null || character.getFighter() == null || character.getFight() != this.fight) {
            return false;
        }
        return character.getFighter().isFriendlyWith(this);
    }

    public GameFightResumeSlaveInfo[] getGameFightResumeSlaveInfo() {
        return this.getSummonedCreature()
                .filter(f -> f instanceof SlaveFighter)
                .map(f -> new GameFightResumeSlaveInfo(f.getID(),
                        f.getSpellsController().getInitialCooldown().entrySet()
                                .stream()
                                .map(x -> new GameFightSpellCooldown(x.getKey(), f.getSpellsController().minCastInterval(x.getKey()) == 0 ? x.getValue().initialCooldown : f.getSpellsController().minCastInterval(x.getKey())))
                                .toArray(GameFightSpellCooldown[]::new)
                        , (byte) f.getTeam().getAliveFighters()
                        .filter(x -> x.getSummonerID() == f.getID() && !(x instanceof BombFighter))
                        .count(),
                        (byte) f.getTeam().getAliveFighters()
                                .filter(x -> x.getSummonerID() == f.getID() && (x instanceof BombFighter))
                                .count())).toArray(GameFightResumeSlaveInfo[]::new);

    }

    public int getInitiative(boolean Base) { //FIXME : Considerate stats ?
        return (int) Math.floor((double) (this.getMaxLife() / 4 + this.stats.getTotal(StatsEnum.INITIATIVE)) * (double) (this.getLife() / this.getMaxLife()));
    }

    public GameFightMinimalStats getGameFightMinimalStats(Player character) {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.getLife(), this.getMaxLife(), (int) this.stats.getBase(StatsEnum.VITALITY), this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT), this.shieldPoints, this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), Math.max(this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), 0), Math.max(this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), 0), this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE), character == null ? this.visibleState.value : this.getVisibleStateFor(character), this.getInitiative(false));
        }
        return new GameFightMinimalStats(this.getLife(), this.getMaxLife(), this.stats.getBase(StatsEnum.VITALITY), this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT), this.shieldPoints, this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), Math.max(this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), 0), Math.max(this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), 0), this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE), character == null ? this.visibleState.value : this.getVisibleStateFor(character));
    }

    public IdentifiedEntityDispositionInformations getIdentifiedEntityDispositionInformations() {
        return new IdentifiedEntityDispositionInformations(this.getCellId(), this.direction, this.ID);
    }

    public abstract FightTeamMemberInformations getFightTeamMemberInformations();

    @Override
    public abstract void send(Message Packet);

    public abstract void joinFight();

    @Override
    public abstract EntityLook getEntityLook();

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_FIGHTER;
    }

    @Override
    public short getCellId() {
        try {
            return this.myCell.Id;
        } catch (NullPointerException e) {
            fight.getLogger().error("map {} cell {} {} ",fight.getMap().getId(), Enumerable.join(fight.myFightCells.get(fight.getTeam1()).keySet().stream().toArray(Short[]::new)),Enumerable.join(fight.myFightCells.get(fight.getTeam2()).keySet().stream().toArray(Short[]::new)));
            //this.fight.getLogger().error(this.toString());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean canWalk() {
        return false;
    }

    @Override
    public boolean canStack() {
        return false;
    }

    public boolean isFriendlyWith(Fighter actor) {
        return actor.team.id == this.team.id;
    }

    public boolean isEnnemyWith(Fighter actor) {
        return !this.isFriendlyWith(actor);
    }

    public void computeDamages(StatsEnum effect, MutableInt jet) {
        switch (effect) {
            case DAMAGE_EARTH:
            case STEAL_EARTH:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100 + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_EARTH_DAMAGES_BONUS)));
                break;
            case DAMAGE_EARTH_PER_PM_PERCENT:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100 + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_EARTH_DAMAGES_BONUS)) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS)));
                break;
            case DAMAGE_NEUTRAL_PER_PM_PERCENT:
                jet.setValue(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS)) * (((double) (this.getMP() / this.getMaxMP())) * 100)));
                break;
            case DAMAGE_FIRE:
            case STEAL_FIRE:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.INTELLIGENCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_FIRE_DAMAGES_BONUS)));
                break;
            case DAMAGE_FIRE_PER_PM_PERCENT:
                jet.setValue(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.INTELLIGENCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_FIRE_DAMAGES_BONUS))) * ((((double) this.getMP() / (double) this.getMaxMP()))));
                break;
            case DAMAGE_AIR:
            case STEAL_AIR:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.AGILITY) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_AIR_DAMAGES_BONUS)));
                break;
            case DAMAGE_AIR_PER_PM_PERCENT:
                jet.setValue(((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.AGILITY) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_AIR_DAMAGES_BONUS))) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
            case DAMAGE_WATER:
            case STEAL_WATER:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.CHANCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_WATER_DAMAGES_BONUS)));
                break;
            case DAMAGE_WATER_PER_PM_PERCENT:
                jet.setValue(((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.CHANCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MULTIPLICATOR)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_WATER_DAMAGES_BONUS))) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
        }

    }

    public static final byte EFFECTSHAPE_DEFAULT_AREA_SIZE = 1;

    public static final byte EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE = 0;

    public static final byte EFFECTSHAPE_DEFAULT_EFFICIENCY = 10;

    public static final byte EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY = 4;

    public double getPortalsSpellEfficiencyBonus(short param1, Fight fight) {
        boolean _loc3_ = false;
        FightPortal[] _loc8_ = new FightPortal[0];
        int _loc9_ = 0;
        FightPortal _loc10_ = null;
        FightPortal _loc11_ = null;
        int _loc12_ = 0;
        double _loc13_ = 0;
        double _loc2_ = 1;

        for (CopyOnWriteArrayList<FightActivableObject> objs : fight.getActivableObjects().values()) {
            for (FightActivableObject Object : objs) {
                if (Object instanceof FightPortal && ((FightPortal) Object).enabled) {
                    _loc8_ = ArrayUtils.add(_loc8_, (FightPortal) Object);
                    if (Object.getCellId() == param1) {
                        _loc3_ = true;
                    }
                }
            }
        }

        if (!_loc3_) {
            return _loc2_;
        }
        final int[] _loc6_ = LinkedCellsManager.getLinks(MapPoint.fromCellId(param1), Arrays.stream(_loc8_).map(x -> x.getMapPoint()).toArray(MapPoint[]::new));
        int _loc7_ = _loc6_.length;
        if (_loc7_ > 1) {
            while (_loc9_ < _loc7_) {
                _loc10_ = _loc8_[_loc9_];
                _loc12_ = Math.max(_loc12_, _loc10_.damageValue);
                if (_loc11_ != null) {
                    _loc13_ = _loc13_ + MapPoint.fromCellId(_loc10_.getCellId()).distanceToCell(MapPoint.fromCellId(_loc11_.getCellId()));
                }
                _loc11_ = _loc10_;
                _loc9_++;
            }
            _loc2_ = 1 + (_loc12_ + _loc8_.length * _loc13_) / 100;
        }
        return _loc2_;
    }

    public void calculBonusDamages(EffectInstanceDice effect, MutableInt jet, short castCell, short targetCell, short truedCell) {

        //double bonus = 0;

        double bonus = getShapeEfficiency(effect.zoneShape(), castCell, targetCell, effect.zoneSize() != -100000 ? effect.zoneSize() : EFFECTSHAPE_DEFAULT_AREA_SIZE, effect.zoneMinSize() != -100000 ? effect.zoneMinSize() : EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE, effect.zoneEfficiencyPercent() != -100000 ? effect.zoneEfficiencyPercent() : EFFECTSHAPE_DEFAULT_EFFICIENCY, effect.zoneMaxEfficiency() != -100000 ? effect.zoneMaxEfficiency() : EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY);

        bonus *= getPortalsSpellEfficiencyBonus(truedCell, this.fight);

        jet.setValue((jet.floatValue() * bonus));
    }

    public static double getShapeEfficiency(int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        if (SpellShapeEnum.valueOf(param1) == null) {
            return getSimpleEfficiency(Pathfunction.getDistance(param2, param3), param4, param5, param6, param7);
        }
        int _loc8_ = 0;

        switch (SpellShapeEnum.valueOf(param1)) {
            case A:
            case a:
            case Z:
            case I:
            case O:
            case semicolon:
            case empty:
            case P:
                return DAMAGE_NOT_BOOSTED;
            case B:
            case V:
            case G:
            case W:
                _loc8_ = Pathfunction.getSquareDistance(param2, param3);
                break;
            case minus:
            case plus:
            case U:
                _loc8_ = Pathfunction.getDistance(param2, param3) / 2;
                break;
            default:
                _loc8_ = Pathfunction.getDistance(param2, param3);
        }
        return getSimpleEfficiency(_loc8_, param4, param5, param6, param7);
    }

    private static final int DAMAGE_NOT_BOOSTED = 1, UNLIMITED_ZONE_SIZE = 50;

    public static double getSimpleEfficiency(int param1, int param2, int param3, int param4, int param5) {
        if (param4 == 0) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param2 <= 0 || param2 >= UNLIMITED_ZONE_SIZE) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param1 > param2) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param4 <= 0) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param3 != 0) {
            if (param1 <= param3) {
                return DAMAGE_NOT_BOOSTED;
            }
            return Math.max(0, DAMAGE_NOT_BOOSTED - 0.01 * Math.min(param1 - param3, param5) * param4);
        }
        return Math.max(0, DAMAGE_NOT_BOOSTED - 0.01 * Math.min(param1, param5) * param4);
    }

    public void calculReduceDamages(StatsEnum effect, MutableInt damages, boolean cc) {
        switch (effect) {
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
            case LIFE_LEFT_TO_THE_ATTACKER_NEUTRAL_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_EARTH:
            case STEAL_EARTH:
            case LIFE_LEFT_TO_THE_ATTACKER_EARTH_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_FIRE:
            case STEAL_FIRE:
            case LIFE_LEFT_TO_THE_ATTACKER_FIRE_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_AIR:
            case STEAL_AIR:
            case LIFE_LEFT_TO_THE_ATTACKER_AIR_DAMAGES:
            case PA_USED_LOST_X_PDV:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_WATER:
            case STEAL_WATER:
            case LIFE_LEFT_TO_THE_ATTACKER_WATER_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;
        }
        if (cc) {
            damages.subtract(this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION));
        }
    }

    /// <summary>
    /// Calcul des PA/PM perdus
    /// </summary>
    /// <param name="caster"></param> TODO: Supprimer les appel variable inutile qui occupent de la mémoire et referencer direct les var
    /// <param name="LostPoint"></param>
    /// <param name="getMP"></param>
    /// <returns></returns>
    public int calculDodgeAPMP(Fighter caster, int lostPoint, boolean MP, boolean isBuff) {
        int realLostPoint = 0;

        if (!MP) {
            int dodgeAPCaster = caster.getAPCancelling() + 1;
            int dodgeAPTarget = this.getAPDodge() + 1;

            for (int i = 0; i < lostPoint; i++) {
                int ActualAP = (isBuff && this.ID == this.fight.getCurrentFighter().ID ? this.getMaxAP() : this.getAP()) - realLostPoint;
                int realAP = getAP();
                if (realAP == 0) {
                    realAP = 1;
                }
                final double percentLastAP = ActualAP / realAP;
                final double chance = 0.5 * (dodgeAPCaster / dodgeAPTarget) * percentLastAP;
                int percentChance = (int) (chance * 100);

                if (percentChance > 100) {
                    percentChance = 90;
                }
                if (percentChance < 10) {
                    percentChance = 10;
                }

                if (RANDOM.nextInt(99) < percentChance) {
                    realLostPoint++;
                }
            }
        } else {
            final int dodgeMPCaster = Math.max(1, caster.getMPCancelling() + 1);
            final int dodgeMPTarget = Math.max(1, this.getMPDodge() + 1);

            for (int i = 0; i < lostPoint; i++) {
                final int actualMP = (isBuff && this.ID == this.fight.getCurrentFighter().ID ? this.getMaxMP() : this.getMP()) - realLostPoint;
                int realMP = getMP();
                if (realMP == 0) {
                    realMP = 1;
                }
                final double percentLastMP = actualMP / realMP;
                final double chance = 0.5 * (dodgeMPCaster / dodgeMPTarget) * percentLastMP;
                int percentChance = (int) (chance * 100);

                if (percentChance > 100) {
                    percentChance = 90;
                }
                if (percentChance < 10) {
                    percentChance = 10;
                }

                if (RANDOM.nextInt(99) < percentChance) {
                    realLostPoint++;
                }
            }
        }

        return realLostPoint;
    }

    public void calculheal(MutableInt heal) {
        heal.setValue((Math.floor(heal.doubleValue() *
                (1 + (this.stats.getTotal(StatsEnum.INTELLIGENCE) / 100f))) + this.stats.getTotal(StatsEnum.ADD_HEAL_BONUS)));
    }

    public int calculArmor(StatsEnum DamageEffect) {
        /*switch (DamageEffect) {
         default:
         return this.stats.getTotal(StatsEnum.ADD_ARMOR);
         }*/
        return this.stats.getTotal(StatsEnum.ADD_ARMOR);
    }

    public int getReflectedDamage() {
        return ((1 + (this.stats.getTotal(StatsEnum.WISDOM) / 100)) * this.stats.getTotal(StatsEnum.DAMAGE_RETURN)) + this.stats.getTotal(StatsEnum.DAMAGE_REFLECTION);
    }

    public abstract List<SpellLevel> getSpells();

    @Override
    public Integer getPriority() {
        return 0;
    }

    @Override
    public boolean canGoThrough() {
        return false;
    }

    public boolean isPlayer() {
        return this instanceof CharacterFighter;
    }

    public CharacterFighter asPlayer() {
        return (CharacterFighter) this;
    }

    public VirtualFighter asVirtual() {
        return (VirtualFighter) this;
    }

    public SummonedFighter asSummon() {
        return (SummonedFighter) this;
    }

    public MonsterFighter asMonster() {
        return (MonsterFighter) this;
    }

    public SlaveFighter asSlave() {
        return (SlaveFighter) this;
    }

    public Player getPlayer() {
        return ((CharacterFighter) this).getCharacter();
    }

    public boolean hasSummoner() {
        return this.summoner != null;
    }

}
