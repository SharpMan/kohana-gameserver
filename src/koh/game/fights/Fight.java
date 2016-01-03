package koh.game.fights;

import java.time.Instant;

import koh.game.dao.DAO;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightGlyph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameAction;
import koh.game.actions.GameMapMovement;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.entities.environments.IWorldField;
import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.Path;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffEndTurn;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.fighters.*;
import koh.game.fights.layer.FightPortal;
import koh.game.network.WorldClient;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.game.utils.Three;
import koh.protocol.client.Message;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.FightOptionsEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.StatsEnum;
import static koh.protocol.client.enums.StatsEnum.ADD_BASE_DAMAGE_SPELL;
import static koh.protocol.client.enums.StatsEnum.CAST_SPELL_ON_CRITICAL_HIT;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightTackledMessage;
import koh.protocol.messages.game.actions.SequenceEndMessage;
import koh.protocol.messages.game.actions.SequenceStartMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightCloseCombatMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellableEffectMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightPointsVariationMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightSpellCastMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.GameEntitiesDispositionMessage;
import koh.protocol.messages.game.context.GameMapMovementMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.messages.game.context.fight.GameFightEndMessage;
import koh.protocol.messages.game.context.fight.GameFightHumanReadyStateMessage;
import koh.protocol.messages.game.context.fight.GameFightNewRoundMessage;
import koh.protocol.messages.game.context.fight.GameFightOptionStateUpdateMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementPossiblePositionsMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsMessage;
import koh.protocol.messages.game.context.fight.GameFightResumeMessage;
import koh.protocol.messages.game.context.fight.GameFightStartMessage;
import koh.protocol.messages.game.context.fight.GameFightStartingMessage;
import koh.protocol.messages.game.context.fight.GameFightSynchronizeMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnEndMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnListMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnReadyRequestMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnResumeMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnStartMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnStartPlayingMessage;
import koh.protocol.messages.game.context.fight.GameFightUpdateTeamMessage;
import koh.protocol.messages.game.context.fight.character.GameFightShowFighterMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayRemoveChallengeMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayShowChallengeMessage;
import koh.protocol.types.game.action.fight.FightDispellableEffectExtendedInformations;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.context.IdentifiedEntityDispositionInformations;
import koh.protocol.types.game.context.fight.FightCommonInformations;
import koh.protocol.types.game.context.fight.FightOptionsInformations;
import koh.protocol.types.game.context.fight.FightTeamInformations;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import koh.protocol.types.game.context.fight.GameFightSpellCooldown;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeam;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeamWithOutcome;
import koh.protocol.types.game.data.items.effects.ObjectEffectDice;
import koh.protocol.types.game.idol.Idol;
import koh.utils.Couple;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public abstract class Fight extends IWorldEventObserver implements IWorldField {

    //public static final ImprovedCachedThreadPool BackGroundWorker2 = new ImprovedCachedThreadPool(5, 50, 2);
    public static final ScheduledExecutorService BackGroundWorker = Executors.newScheduledThreadPool(50);
    protected static final Logger logger = LogManager.getLogger(Fight.class);

    public static final Random RANDOM = new Random();
    private static final HashMap<Integer, HashMap<Integer, Short[]>> MAP_FIGHTCELLS = new HashMap<>();
    protected FightTeam myTeam1 = new FightTeam((byte) 0, this);
    protected FightTeam myTeam2 = new FightTeam((byte) 1, this);

    protected long myLoopTimeOut = -1;
    protected long myLoopActionTimeOut;
    protected int myNextID = -1000;
    protected ArrayList<GameAction> myActions = new ArrayList<>();
    protected volatile GameFightEndMessage myResult;

    public enum FightLoopState {

        STATE_WAIT_START, STATE_WAIT_TURN, STATE_WAIT_ACTION, STATE_WAIT_READY, STATE_WAIT_END, STATE_WAIT_AI, STATE_END_TURN, STATE_END_FIGHT,
    }

    @Getter @Setter
    protected short fightId;
    @Getter
    protected FightState fightState;
    @Getter @Setter
    protected FightLoopState fightLoopState;
    @Getter
    protected DofusMap map;
    @Getter
    protected Fighter currentFighter;
    @Getter
    protected long fightTime, creationTime;
    @Getter
    protected FightTypeEnum fightType;
    protected Map<Short, FightCell> myCells = new HashMap<>();
    protected Map<FightTeam, Map<Short, FightCell>> myFightCells = new HashMap<>();
    protected short ageBonus = -1, lootShareLimitMalus = -1;

    protected Map<String, CancellableScheduledRunnable> myTimers = new HashMap<>();
    @Getter
    protected Map<Fighter, CopyOnWriteArrayList<FightActivableObject>> activableObjects = Collections.synchronizedMap(new HashMap<>());
    private final Object $mutex_lock = new Object();
    @Getter
    protected FightWorker fightWorker = new FightWorker(this);
    @Getter
    protected AtomicInteger nextTriggerUid = new AtomicInteger();

    public synchronized int nextID() {
        return this.myNextID--;
    }

    public Fight(FightTypeEnum type, DofusMap map) {
        this.fightState = fightState.STATE_PLACE;
        this.fightTime = -1;
        this.creationTime = Instant.now().getEpochSecond();
        this.fightType = type;
        this.map = map;
        this.fightId = this.map.nextFightId();
        this.initCells();
    }

    public FighterRefusedReasonEnum canJoin(FightTeam Team, Player Character) {
        if (Team.canJoin(Character) != FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
            return Team.canJoin(Character);
        } else if (this.getFreeSpawnCell(Team) == null) {
            return FighterRefusedReasonEnum.TEAM_FULL;
        } else {
            return FighterRefusedReasonEnum.FIGHTER_ACCEPTED;
        }
    }

    public boolean canJoinSpectator() {
        return this.fightState == fightState.STATE_ACTIVE && !this.myTeam1.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET) && !this.myTeam2.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET);
    }

    public FightTeam getEnnemyTeam(FightTeam Team) {
        return (Team == this.myTeam1 ? this.myTeam2 : this.myTeam1);
    }

    public FightTeam getTeam(int LeaderId) {
        return (this.myTeam1.LeaderId == LeaderId ? this.myTeam1 : this.myTeam2);
    }

    public abstract void leaveFight(Fighter Fighter);

    public abstract void endFight(FightTeam Winners, FightTeam Loosers);

    public abstract int getStartTimer();

    public abstract int getTurnTime();

    public boolean hasFinished = false;

    private void initCells() {
        // Ajout des cells
        for (DofusCell Cell : this.map.getCells()) {
            this.myCells.put(Cell.getId(), new FightCell(Cell.getId(), Cell.mov(), Cell.los()));
        }
        this.myFightCells.put(myTeam1, new HashMap<>());
        this.myFightCells.put(myTeam2, new HashMap<>());

        if (Fight.MAP_FIGHTCELLS.containsKey(this.map.getId())) {
            // Ajout
            synchronized (Fight.MAP_FIGHTCELLS) {
                for (Short Cell : Fight.MAP_FIGHTCELLS.get(this.map.getId()).get(0)) {
                    this.myFightCells.get(this.myTeam1).put(Cell, this.myCells.get(Cell));
                }
                for (Short Cell : Fight.MAP_FIGHTCELLS.get(this.map.getId()).get(1)) {
                    this.myFightCells.get(this.myTeam2).put(Cell, this.myCells.get(Cell));
                }
            }
            return;
        }

        for (Short CellValue : this.map.getRedCells()) {
            FightCell Cell = this.myCells.get(CellValue);
            if (Cell == null || !Cell.CanWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam1).put(CellValue, Cell);
        }

        for (Short CellValue : this.map.getBlueCells()) {
            FightCell Cell = this.myCells.get(CellValue);
            if (Cell == null || !Cell.CanWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam2).put(CellValue, Cell);
        }

        if (this.map.getBlueCells().length == 0 || this.map.getRedCells().length == 0) {
            this.myFightCells.get(this.myTeam1).clear();
            this.myFightCells.get(this.myTeam2).clear();
            Couple<ArrayList<FightCell>, ArrayList<FightCell>> startCells = Algo.genRandomFightPlaces(this);
            for (FightCell Cell : startCells.first) {
                this.myFightCells.get(this.myTeam1).put(Cell.Id, Cell);
            }
            for (FightCell Cell : startCells.second) {
                this.myFightCells.get(this.myTeam2).put(Cell.Id, Cell);
            }
            synchronized (Fight.MAP_FIGHTCELLS) {
                Fight.MAP_FIGHTCELLS.put(this.map.getId(), new HashMap<>());
                Fight.MAP_FIGHTCELLS.get(this.map.getId()).put(0, this.myFightCells.get(this.myTeam1).keySet().toArray(new Short[this.myFightCells.get(this.myTeam1).size()]));
                Fight.MAP_FIGHTCELLS.get(this.map.getId()).put(1, this.myFightCells.get(this.myTeam2).keySet().toArray(new Short[this.myFightCells.get(this.myTeam2).size()]));
            }
        }

    }

    public static final StatsEnum[] EFFECT_NOT_SILENCED = new StatsEnum[]{
        StatsEnum.Damage_Neutral, StatsEnum.Damage_Earth, StatsEnum.Damage_Air, StatsEnum.Damage_Fire, StatsEnum.Damage_Water,
        StatsEnum.Steal_Neutral, StatsEnum.Steal_Earth, StatsEnum.Steal_Air, StatsEnum.Steal_Fire, StatsEnum.Steal_Water, StatsEnum.Steal_PV_Fix,
        StatsEnum.DamageLifeNeutre, StatsEnum.DamageLifeEau, StatsEnum.DamageLifeTerre, StatsEnum.DamageLifeAir, StatsEnum.DamageLifeFeu, StatsEnum.DamageDropLife
    };

    public void disconnect(CharacterFighter fighter) {
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 182, new String[]{fighter.getCharacter().getNickName(), Integer.toString(fighter.getTurnRunning())}));
        if (this.currentFighter.getID() == fighter.getID()) {
            this.fightLoopState = fightLoopState.STATE_END_TURN;
        }
        fighter.setTurnReady(true);
    }

    public void launchSpell(Fighter Fighter, SpellLevel SpellLevel, short CellId, boolean friend) {
        launchSpell(Fighter, SpellLevel, CellId, friend, false, true);
    }
    //TODO ActionIdConverter.ACTION_FIGHT_DISABLE_PORTAL

    private Three<Integer, int[], Integer> getTargetThroughPortal(Fighter Fighter, int param1) {
        return getTargetThroughPortal(Fighter, param1, false);
    }

    private Three<Integer, int[], Integer> getTargetThroughPortal(Fighter Fighter, int param1, boolean param2) {
        MapPoint _loc3_ = null;
        int damagetoReturn = 0;
        MapPoint _loc16_;
        FightPortal[] Portails = new FightPortal[0];
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled) {
                    Portails = ArrayUtils.add(Portails, (FightPortal) Object);
                    if (Object.getCellId() == param1) {
                        _loc3_ = Object.getMapPoint();
                    }
                }
            }
        }
        if (Portails.length < 2) {
            return new Three<>(param1, new int[0], 0);
        }
        if (_loc3_ == null) {
            return new Three<>(param1, new int[0], 0);
        }
        final int[] _loc10_ = LinkedCellsManager.getLinks(_loc3_, Arrays.stream(Portails)/*.filter(x -> x.caster.team == Fighter.team)*/.map(x -> x.getMapPoint()).toArray(MapPoint[]::new));
        MapPoint _loc11_ = MapPoint.fromCellId(_loc10_[/*_loc10_.length == 0 ? 0 :*/_loc10_.length - 1]);
        MapPoint _loc12_ = MapPoint.fromCellId(Fighter.getCellId());
        if (_loc12_ == null) {
            return new Three<>(param1, new int[0], 0);
        }
        int _loc13_ = _loc3_.get_x() - _loc12_.get_x() + _loc11_.get_x();
        int _loc14_ = _loc3_.get_y() - _loc12_.get_y() + _loc11_.get_y();
        if (!MapPoint.isInMap(_loc13_, _loc14_)) {
            return /*AtouinConstants.MAP_CELLS_COUNT + 1*/ new Three<>(561, new int[0], 0);
        }
        _loc16_ = MapPoint.fromCoords(_loc13_, _loc14_);
        /* if (param2) {
         _loc17_ = new int[]{_loc12_.get_cellId(), _loc3_.get_cellId()};
         //LinkedCellsManager.getInstance().drawLinks("spellEntryLink",_loc17_,10,TARGET_COLOR.color,1);
         if (_loc16_.get_cellId() < 560) {
         _loc18_ = new int[]{_loc11_.get_cellId(), _loc16_.get_cellId()};
         //LinkedCellsManager.getInstance().drawLinks("spellExitLink",_loc18_,6,TARGET_COLOR.color,1);
         }
         }
         for (int i : _loc10_) {
         damagetoReturn += Arrays.stream(Portails).filter(y -> y.getCellId() == i).findFirst().get().damageValue;
         }*/
        int[] PortailIds = new int[_loc10_.length];
        FightPortal Portal;
        for (int i = 0; i < _loc10_.length; i++) {
            final int ID = _loc10_[i];
            Portal = Arrays.stream(Portails).filter(y -> y.getCellId() == ID).findFirst().get();
            damagetoReturn += Portal.damageValue;
            PortailIds[i] = Portal.ID;
        }
        return new Three<>((int) _loc16_.get_cellId(), PortailIds, damagetoReturn);
    }

    public void launchSpell(Fighter fighter, SpellLevel spellLevel, short cellId, boolean friend, boolean fakeLaunch, boolean imTargeted) {
        if (this.fightState != fightState.STATE_ACTIVE) {
            return;
        }
        short oldCell = cellId;
        if (spellLevel.getId() == 10461 && fighter.isPlayer() && fighter.getPlayer().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON) != null) {
            this.launchWeapon(fighter.asPlayer(), cellId);
            return;
        }

        // La cible si elle existe
        Fighter TargetE = this.hasEnnemyInCell(cellId, fighter.getTeam());
        if (friend && TargetE == null) { //FIXME: relook this line
            TargetE = this.hasFriendInCell(cellId, fighter.getTeam());
        }

        int TargetId = TargetE == null ? -1 : TargetE.getID();
        // Peut lancer le sort ?
        if (!fakeLaunch && !this.canLaunchSpell(fighter, spellLevel, fighter.getCellId(), cellId, TargetId)) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            this.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
            return;
        }
        if (!fakeLaunch) {
            this.startSequence(SequenceTypeEnum.SEQUENCE_SPELL);

            fighter.setUsedAP(fighter.getUsedAP() + spellLevel.getApCost());
            fighter.getSpellsController().actualize(spellLevel, TargetId);
        }

        boolean IsCc = false;
        if (spellLevel.getCriticalHitProbability() != 0 && spellLevel.getCriticalEffect().length > 0) {
            int TauxCC = spellLevel.getCriticalHitProbability() - fighter.getStats().getTotal(StatsEnum.Add_CriticalHit);
            if (TauxCC < 2) {
                TauxCC = 2;
            }
            if (Fight.RANDOM.nextInt(TauxCC) == 0) {
                IsCc = true;
            }
            logger.debug("CC: " + IsCc + " TauxCC " + TauxCC + " getSpellLevel.criticalHitProbability " + spellLevel.getCriticalHitProbability());
        }
        IsCc &= !fighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);
        if (IsCc && fighter.getStats().getTotal(CAST_SPELL_ON_CRITICAL_HIT) > 0) { //Tutu

        }

        EffectInstanceDice[] Effects = IsCc ? spellLevel.getCriticalEffect() : spellLevel.getEffects();
        if (Effects == null) {
            Effects = spellLevel.getCriticalEffect();
        }

        boolean silentCast = Arrays.stream(Effects).allMatch(x -> !ArrayUtils.contains(EFFECT_NOT_SILENCED, x.EffectType()));

        if (!fakeLaunch) {
            Three<Integer, int[], Integer> Informations = null;
            if (this.getCell(cellId).HasGameObject(FightObjectType.OBJECT_PORTAL) && !Arrays.stream(Effects).anyMatch(Effect -> Effect.EffectType().equals(StatsEnum.DISABLE_PORTAL))) {
                Informations = this.getTargetThroughPortal(fighter, cellId, true);
                cellId = Informations.first.shortValue();
                //this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"DamagePercentBoosted Suite au portails = " + DamagePercentBoosted}));

            }
            for (Player player : this.Observable$stream()) {
                player.send(new GameActionFightSpellCastMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, fighter.getID(), TargetId, cellId, (byte) (IsCc ? 2 : 1), spellLevel.getSpellId() == 2763 ? true : (!fighter.isVisibleFor(player) || silentCast), spellLevel.getSpellId(), spellLevel.getGrade(), Informations == null ? new int[0] : Informations.second));
            }
            if (Informations != null) {
                Informations.Clear();
            }
        }

        HashMap<EffectInstanceDice, ArrayList<Fighter>> Targets = new HashMap<>();
        for (Iterator<EffectInstanceDice> EffectI = ((Arrays.stream(Effects).sorted((e2, e1) -> (e1.EffectType() == StatsEnum.DISPELL_SPELL ? 0 : (e2.EffectType() == StatsEnum.DISPELL_SPELL ? -1 : 1))).iterator())); EffectI.hasNext();) {
            final EffectInstanceDice Effect = EffectI.next();
            System.out.println(Effect.toString());
            Targets.put(Effect, new ArrayList<>());
            for (short Cell : (new Zone(Effect.ZoneShape(), Effect.ZoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(cellId), true), this.map)).getCells(cellId)) {
                FightCell FightCell = this.getCell(Cell);
                if (FightCell != null && FightCell.HasGameObject(FightObjectType.OBJECT_PORTAL)) {

                }
                if (FightCell != null) {
                    if (FightCell.HasGameObject(FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
                        for (Fighter Target : FightCell.GetObjectsAsFighter()) {
                            if (Effect.targetMask.equals("C") && (Effect.category() == 0)) {
                                Targets.get(Effect).add(fighter);
                                break;
                            }
                            logger.debug(EffectHelper.verifyEffectTrigger(fighter, Target, Effects, Effect, false, Effect.triggers, cellId));
                            logger.debug(Effect.isValidTarget(fighter, Target));
                            logger.debug(EffectInstanceDice.verifySpellEffectMask(fighter, Target, Effect));

                            if (EffectHelper.verifyEffectTrigger(fighter, Target, Effects, Effect, false, Effect.triggers, cellId) && Effect.isValidTarget(fighter, Target) && EffectInstanceDice.verifySpellEffectMask(fighter, Target, Effect)) {
                                if (Effect.targetMask.equals("C") && fighter.getCarriedActor() == Target.getID()) {
                                    continue;
                                } else if (Effect.targetMask.equals("a,A") && fighter.getCarriedActor() != 0 & fighter.getID() == Target.getID()) {
                                    continue;
                                }
                                /*if (Fighter instanceof BombFighter && target.states.hasState(FightStateEnum.Kaboom)) {
                                 continue;
                                 }*/
                                if (!imTargeted && Target.getID() == fighter.getID()) {
                                    continue;
                                }
                                /*if(Effect.category() == EffectHelper.DAMAGE_EFFECT_CATEGORY && !EffectInstanceDice.verifySpellEffectMask(Fighter, target, Effect)){
                                 continue;
                                 }*/
                                logger.debug("Targeet Aded!");
                                Targets.get(Effect).add(Target);
                            }
                        }
                    }
                }
            }
        }
        double num1 = Fight.RANDOM.nextDouble();
        double num2 = (double) Arrays.stream(Effects).mapToInt(x -> x.random).sum();
        boolean flag = false;
        for (EffectInstanceDice Effect : Effects) {
            if (Effect.random > 0) {
                if (!flag) {
                    if (num1 > (double) Effect.random / num2) {
                        num1 -= (double) Effect.random / num2;
                        continue;
                    } else {
                        flag = true;
                    }
                } else {
                    continue;
                }
            }
            // Actualisation des morts
            Targets.get(Effect).removeIf(F -> F.isDead());
            if (Effect.EffectType() == ADD_BASE_DAMAGE_SPELL) {
                Targets.get(Effect).clear();
                Targets.get(Effect).add(fighter);
            }
            if (Effect.delay > 0) {
                //TODO: Set ParentBoost UID
                fighter.getBuff().delayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), spellLevel.getSpellId(), cellId, num1, Effect, fighter, Targets.get(Effect), false, StatsEnum.NONE, 0, spellLevel), Effect.delay));
                Targets.get(Effect).stream().forEach((Target) -> {
                    this.sendToField(new GameActionFightDispellableEffectMessage(Effect.effectId, fighter.getID(), new FightTriggeredEffect(Target.getNextBuffUid().incrementAndGet(), Target.getID(), (short) Effect.duration, FightDispellableEnum.DISPELLABLE, spellLevel.getSpellId(), Effect.effectUid, 0, (short) Effect.diceNum, (short) Effect.diceSide, (short) Effect.value, (short) Effect.delay)));
                });

                /*for (Fighter target : targets.get(Effect)) {
                 target.buff.delayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), getSpellLevel.spellId, getCellId, num1, Effect, Fighter, new ArrayList<Fighter>() {
                 {
                 add(target);
                 }
                 }, false, StatsEnum.NONE, 0, getSpellLevel), Effect.delay));
                 }*/
                continue;
            }
            EffectCast CastInfos = new EffectCast(Effect.EffectType(), spellLevel.getSpellId(), cellId, num1, Effect, fighter, Targets.get(Effect), false, StatsEnum.NONE, 0, spellLevel);
            CastInfos.targetKnownCellId = cellId;
            CastInfos.oldCell = oldCell;
            if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                break;
            }
        }

        if (!fakeLaunch) {
            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -spellLevel.getApCost()));
        }

        if (!fakeLaunch && fighter.getVisibleState() == GameActionFightInvisibilityStateEnum.INVISIBLE && silentCast && spellLevel.getSpellId() != 2763) {
            this.sendToField(new ShowCellMessage(fighter.getID(), fighter.getCellId()));
        }

        if (!fakeLaunch) {
            this.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
        }
    }

    public void launchWeapon(CharacterFighter fighter, short cellId) {
        // Combat encore en cour ?
        if (this.fightState != fightState.STATE_ACTIVE) {
            return;
        }
        if (fighter != this.currentFighter) {
            return;
        }
        InventoryItem Weapon = fighter.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON);
        if (Weapon.getTemplate().getTypeId() == 83) { //Pière d'Ame
            return;
        }
        // La cible si elle existe
        Fighter targetE = this.hasEnnemyInCell(cellId, fighter.getTeam());
        if (targetE == null) {
            targetE = this.hasFriendInCell(cellId, fighter.getTeam());
        }
        int TargetId = targetE == null ? -1 : targetE.getID();

        if (!(Pathfinder.getGoalDistance(map, cellId, fighter.getCellId()) <= Weapon.getWeaponTemplate().getRange() && Pathfinder.getGoalDistance(map, cellId, fighter.getCellId()) >= Weapon.getWeaponTemplate().getMinRange() && fighter.getAP() >= Weapon.getWeaponTemplate().getApCost())) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            return;
        }

        this.startSequence(SequenceTypeEnum.SEQUENCE_WEAPON);

        fighter.setUsedAP(fighter.getUsedAP() + Weapon.getWeaponTemplate().getApCost());

        boolean IsCc = false;

        int TauxCC = Weapon.getWeaponTemplate().getCriticalHitProbability() - fighter.getStats().getTotal(StatsEnum.Add_CriticalHit);
        if (TauxCC < 2) {
            TauxCC = 2;
        }
        if (Fight.RANDOM.nextInt(TauxCC) == 0) {
            IsCc = true;
        }

        IsCc &= !fighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);

        ArrayList<Fighter> Targets = new ArrayList<>(4);

        for (short Cell : (new Zone(SpellShapeEnum.valueOf(Weapon.getItemType().zoneShape()), Weapon.getItemType().zoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(cellId), true), this.map)).getCells(cellId)) {
            FightCell FightCell = this.getCell(Cell);
            if (FightCell != null) {
                if (FightCell.HasGameObject(FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
                    Targets.addAll(FightCell.GetObjectsAsFighterList());
                }
            }
        }

        Targets.removeIf(F -> F.isDead());
        Targets.remove(fighter);
        ObjectEffectDice[] Effects = Weapon.getEffects$Notify().stream().filter(Effect -> Effect instanceof ObjectEffectDice && ArrayUtils.contains(EffectHelper.unRandomablesEffects, Effect.actionId)).map(x -> (ObjectEffectDice) x).toArray(ObjectEffectDice[]::new);

        double num1 = Fight.RANDOM.nextDouble();

        double num2 = (double) Arrays.stream(Effects).mapToInt(Effect -> ((EffectInstanceDice) Weapon.getTemplate().getEffect(Effect.actionId)).random).sum();
        boolean flag = false;

        this.sendToField(new GameActionFightCloseCombatMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, fighter.getID(), TargetId, cellId, (byte) (IsCc ? 2 : 1), false, Weapon.getTemplate().getId()));

        EffectInstanceDice EffectFather;
        for (ObjectEffectDice Effect : Effects) {
            EffectFather = (EffectInstanceDice) Weapon.getTemplate().getEffect(Effect.actionId);
            if (EffectFather.random > 0) {
                if (!flag) {
                    if (num1 > (double) EffectFather.random / num2) {
                        num1 -= (double) EffectFather.random / num2;
                        continue;
                    } else {
                        flag = true;
                    }
                } else {
                    continue;
                }
            }
            EffectCast CastInfos = new EffectCast(StatsEnum.valueOf(Effect.actionId), 0, cellId, num1, EffectFather, fighter, Targets, true, StatsEnum.NONE, 0, null);
            CastInfos.targetKnownCellId = cellId;
            if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                break;
            }
        }

        this.sendToField(new GameActionFightPointsVariationMessage(!IsCc ? ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT : ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT_CRITICAL_MISS, fighter.getID(), fighter.getID(), (short) Weapon.getWeaponTemplate().getApCost()));
        this.endSequence(SequenceTypeEnum.SEQUENCE_WEAPON, false);
    }

    public boolean canLaunchSpell(Fighter fighter, SpellLevel spell, short currentCell, short cellId, int targetId) {
        // Fake caster
        if (fighter != this.currentFighter) {
            return false;
        }

        // Fake cellId
        if (!this.myCells.containsKey(cellId)) {
            return false;
        }

        // PA manquant ?
        if (fighter.getAP() < spell.getApCost()) {
            return false;
        }

        //Todo check po PO
        if (!this.map.getCell(cellId).walakable() || this.map.getCell(cellId).nonWalkableDuringFight()) {
            return false;
        }
        //targetId == -1
        return (!spell.isNeedFreeCell() || targetId == -1)
                && (!spell.isNeedTakenCell() || targetId != -1)
                && !Arrays.stream(spell.getStatesForbidden()).anyMatch(x -> fighter.hasState(x))
                && !Arrays.stream(spell.getStatesRequired()).anyMatch(x -> !fighter.hasState(x))
                && ArrayUtils.contains(fighter.getCastZone(spell), cellId)
                && fighter.getSpellsController().canLaunchSpell(spell, targetId);

    }

    public void toggleLock(Fighter fighter, FightOptionsEnum type) {
        boolean Value = fighter.getTeam().isToggled(type) == false;
        fighter.getTeam().toggle(type, Value);
        if (this.fightState == fightState.STATE_PLACE) {
            this.map.sendToField(new GameFightOptionStateUpdateMessage(this.fightId, fighter.getTeam().id, type.value, Value));
        }

        Message Message = null;
        switch (type) {
            case FIGHT_OPTION_SET_CLOSED:
                if (Value) {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 95);
                } else {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 96);
                }
                break;

            case FIGHT_OPTION_ASK_FOR_HELP:
                if (Value) {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 103);
                } else {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 104);
                }
                break;

            case FIGHT_OPTION_SET_TO_PARTY_ONLY:
                if (Value) {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 93);
                } else {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 94);
                }
                break;

            case FIGHT_OPTION_SET_SECRET:
                if (Value) {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 40);

                    // on kick les spectateurs
                    this.kickSpectators();
                } else {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 39);
                }
                break;
        }
        this.sendToField(Message);
    }

    public void kickSpectators() {

    }

    public synchronized void startFight() {
        // Si combat deja lancé
        if (this.fightState != fightState.STATE_PLACE) {
            return;
        }

        this.map.sendToField(new GameRolePlayRemoveChallengeMessage(this.fightId));

        // Preparation du lancement
        this.fightState = fightState.STATE_INIT;

        //TODO : CHALLENGE
        // Arret du timer
        this.stopTimer("startTimer");
        this.fightTime = System.currentTimeMillis();

        // initialize des tours
        this.fightWorker.initTurns();

        this.sendToField(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
        this.sendToField(new GameFightStartMessage(new Idol[0]));
        // Liste des tours
        //this.sendToField(new GameFightTurnListMessage(this.fightWorker.fighters().stream().filter(x -> x.isAlive()).mapToInt(x -> x.id).toArray(), this.fightWorker.fighters().stream().filter(x -> !x.isAlive()).mapToInt(x -> x.id).toArray()));
        this.sendToField(getFightTurnListMessage());
        this.sendToField(new GameFightSynchronizeMessage(this.fighters().map(x -> x.getGameContextActorInformations(null)).toArray(GameFightFighterInformations[]::new)));

        // reset du ready
        this.setAllUnReady();
        // En attente de lancement
        this.fightLoopState = fightLoopState.STATE_WAIT_START;

        // Lancement du gameLoop 10 ms d'interval.
        this.startTimer(new CancellableScheduledRunnable(BackGroundWorker, 10, 10) {
            @Override
            public void run() {
                gameLoop();
            }
        }, "gameLoop");
    }

    private void gameLoop() {
        try {
            // Switch sur le status et verify fin de tour
            switch (this.fightLoopState) {
                case STATE_WAIT_START: // En attente de lancement
                    this.fightState = fightState.STATE_ACTIVE;
                    this.fightLoopState = fightLoopState.STATE_WAIT_READY;
                    this.beginTurn();
                    break;

                case STATE_WAIT_TURN: // Fin du tour par force a cause du timeout
                    if (this.myLoopTimeOut < System.currentTimeMillis()) {
                        if (this.isActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                            this.endTurn(); // Fin du tour
                        }
                    }
                    break;

                case STATE_END_TURN: // Fin du tour par le joueur
                    if (this.isActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                        this.endTurn(); // Fin du tour
                    }
                    break;

                case STATE_WAIT_READY: // En attente des joueurs x ...
                    if (this.isAllTurnReady()) {
                        this.middleTurn();
                        this.beginTurn();
                    } else if (this.myLoopTimeOut + 5000 < System.currentTimeMillis()) {
                        this.sendToField(new TextInformationMessage((byte) 1, 29, new String[]{StringUtils.join(this.getAliveFighters().filter(x -> !x.isTurnReady() && x instanceof CharacterFighter).map(y -> ((CharacterFighter) y).getCharacter().getNickName()).toArray(String[]::new), ", ")}));
                        this.middleTurn();
                        this.beginTurn();
                    }
                    break;

                case STATE_WAIT_AI: // Artificial intelligence
                     /*if (this.currentFighter istanceof VirtualFighter)
                     {
                     // Lancement de l'IA pour 30 secondes maximum
                     (this.currentFighter as VirtualFighter).Mind.runAI();
                     //new AIProcessor(this, this.currentFighter).applyIA(Environment.TickCount + 30000);
                     }
                     else*/
                    if (this.currentFighter.getObjectType() == FightObjectType.OBJECT_STATIC) {
                        this.myLoopActionTimeOut = System.currentTimeMillis() + 750;
                    }
                    // Fin de tour
                    if (this.fightLoopState != fightLoopState.STATE_WAIT_END) {
                        this.fightLoopState = fightLoopState.STATE_END_TURN;
                    }
                    break;

                case STATE_WAIT_END: // Fin du combat
                    if (!hasFinished || this.isActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                        this.endTurn(true);
                        //System.Threading.Thread.Sleep(500);
                        this.myTeam1.endFight();
                        this.myTeam2.endFight();
                        this.endFight(this.getWinners(), this.getEnnemyTeam(this.getWinners()));
                        hasFinished = true;
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /// <summary>
    /// Veifie si toute les actions son terminé
    /// </summary>
    /// <returns></returns>
    public boolean isActionsFinish() {
        return this.myActions.isEmpty();
    }

    public synchronized void beginTurn() {
        // Mise a jour du combattant
        this.currentFighter = this.fightWorker.getNextFighter();

        this.startSequence(SequenceTypeEnum.SEQUENCE_TURN_END);

        // Activation des buffs et fightObjects
        int BeginTurnIndice = this.currentFighter.beginTurn();

        this.endSequence(SequenceTypeEnum.SEQUENCE_TURN_END, false);

        // Mort du joueur ou fin de combat
        if (BeginTurnIndice == -3 || BeginTurnIndice == -2) {
            return;
        }

        // Envois debut du tour
        this.sendToField(new GameFightTurnStartMessage(this.currentFighter.getID(), this.getTurnTime() / 100));

        if (this.currentFighter instanceof CharacterFighter) {
            this.currentFighter.send(((CharacterFighter) this.currentFighter).FighterStatsListMessagePacket());
        }

        this.sendToField((o) -> {
            ((Player) o).send(new GameFightSynchronizeMessage(this.fighters().map(x -> x.getGameContextActorInformations((Player) o)).toArray(GameFightFighterInformations[]::new)));
        });

        this.currentFighter.send(new GameFightTurnStartPlayingMessage());

        // Timeout du tour
        this.myLoopTimeOut = System.currentTimeMillis() + this.getTurnTime();

        // status en attente de fin de tour
        if ((this.currentFighter instanceof CharacterFighter && ((CharacterFighter) currentFighter).getCharacter().getClient() == null && this.currentFighter.getTeam().getAliveFighters().count() > 1L) || this.currentFighter.getBuff().getAllBuffs().anyMatch(x-> x instanceof BuffEndTurn)) {
            this.fightLoopState = fightLoopState.STATE_END_TURN;
        } else {
            this.fightLoopState = fightLoopState.STATE_WAIT_TURN;
        }

            //Chalenge
            /*if (this instanceof MonsterFight && this.currentFighter instanceof CharacterFighter)
         {
         foreach (var Challenge in Challanges)
         {
         Challenge.beginTurn(this.currentFighter);
         }
         }*/
        // Monstre passe le tour
        if (this.currentFighter instanceof VirtualFighter || this.currentFighter instanceof StaticFighter) {
            this.fightLoopState = fightLoopState.STATE_WAIT_AI;
        }
    }

    public void endTurn() {
        endTurn(false);
    }

    public void endTurn(boolean finish) {
        this.startSequence(SequenceTypeEnum.SEQUENCE_TURN_END);
        // Fin du tour, activation des buffs, pieges etc
        if (this.currentFighter.endTurn() == -3) {
            return;
        } else if (activableObjects.containsKey(currentFighter)) {
            activableObjects.get(currentFighter).stream().filter(x -> x instanceof FightGlyph).forEach(y -> y.decrementDuration());
            activableObjects.get(currentFighter).removeIf(fightObject -> fightObject.getObjectType() == FightObjectType.OBJECT_GLYPHE && fightObject.Duration <= 0);
        }
        this.endSequence(SequenceTypeEnum.SEQUENCE_TURN_END, false);
        // Combat fini a la fin de son tour

        /* if (this instanceof MonsterFight && this.currentFighter  instanceof CharacterFighter)
         {
         this.Challanges.ForEach(x => x.endTurn(this.currentFighter));
         }*/
        // Tout le monde doit se synchro
        this.setAllUnReady();
        if (!finish) // En attente des joueurs
        {
            this.fightLoopState = fightLoopState.STATE_WAIT_READY;
        }

        if (this.IsSequencing) {
            this.endSequence(this.sequence, true);
        }
        if (this.waitAcknowledgment) {
            this.acknowledgeAction();
        }

        // Tour fini
        this.sendToField(new GameFightTurnEndMessage(this.currentFighter.getID()));
        if (!finish) {
            this.sendToField(new GameFightTurnReadyRequestMessage(this.currentFighter.getID()));
        }
    }

    public void middleTurn() {
        this.currentFighter.middleTurn();

    }

    protected void onTackled(Fighter fighter, MovementPath path) {
        ArrayList<Fighter> tacklers = Pathfinder.getEnnemyNearToTakle(this, fighter.getTeam(), fighter.getCellId());

        int tackledMp = fighter.getTackledMP();
        int tackledAp = fighter.getTackledAP();
        if (fighter.getMP() - tackledMp < 0) {
            logger.error("Cannot apply tackle : mp tackled ({0}) > available mp ({1})", tackledMp, fighter.getMP());
        } else {
            this.sendToField(new GameActionFightTackledMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_TACKLED, fighter.getID(), tacklers.stream().mapToInt(x -> x.getID()).toArray()));

            fighter.setUsedAP(fighter.getUsedAP() + tackledAp);
            fighter.setUsedMP(fighter.getUsedAP() + tackledMp);

            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -tackledAp));
            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, fighter.getID(), fighter.getID(), (short) -tackledMp));

            if (path.movementLength <= fighter.getMP()) {
                return;
            }
            path.cutPath(fighter.getMP() + 1);
        }
    }

    public void affectSpellTo(Fighter caster, Fighter target, int level, int... spells) {
        SpellLevel spell;
        for (int spellid : spells) {
            spell =  DAO.getSpells().findSpell(spellid).getSpellLevel(level);
            double num1 = Fight.RANDOM.nextDouble();
            double num2 = (double) Arrays.stream(spell.getEffects()).mapToInt(x -> x.random).sum();
            boolean flag = false;
            for (EffectInstanceDice Effect : spell.getEffects()) {
                if (Effect.random > 0) {
                    if (!flag) {
                        if (num1 > (double) Effect.random / num2) {
                            num1 -= (double) Effect.random / num2;
                            continue;
                        } else {
                            flag = true;
                        }
                    } else {
                        continue;
                    }
                }
                ArrayList<Fighter> targets = new ArrayList<Fighter>() {
                    {
                        add(target);
                    }
                };
                if (Effect.delay > 0) {
                    target.getBuff().delayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), spellid, target.getCellId(), num1, Effect, caster, targets, false, StatsEnum.NONE, 0, spell), Effect.delay));
                    this.sendToField(new GameActionFightDispellableEffectMessage(Effect.effectId, caster.getID(), new FightTriggeredEffect(target.getNextBuffUid().incrementAndGet(), target.getID(), (short) Effect.duration, FightDispellableEnum.DISPELLABLE, spellid, Effect.effectUid, 0, (short) Effect.diceNum, (short) Effect.diceSide, (short) Effect.value, (short) Effect.delay)));
                    continue;
                }
                EffectCast CastInfos = new EffectCast(Effect.EffectType(), spellid, target.getCellId(), num1, Effect, caster, targets, false, StatsEnum.NONE, 0, spell);
                CastInfos.targetKnownCellId = target.getCellId();
                if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                    break;
                }
            }
        }
    }

    public synchronized GameMapMovement tryMove(Fighter fighter, MovementPath path) {
        // Pas a lui de jouer
        if (fighter != this.currentFighter) {
            return null;
        }

        // Pas assez de point de mouvement
        if (path.movementLength > fighter.getMP() || path.movementLength == -1) {
            System.out.println(path.movementLength > fighter.getMP());
            System.out.println(path.movementLength + " " + fighter.getMP());
            return null;
        }

        this.startSequence(SequenceTypeEnum.SEQUENCE_MOVE);

        if ((fighter.getTackledMP() > 0 || fighter.getTackledAP() > 0) && !this.currentFighter.getStates().hasState(FightStateEnum.Enraciné)) {
            this.onTackled(fighter, path);
            if (path.transitCells.isEmpty() || path.movementLength == 0) {
                this.endSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
                return null;
            }

        }
        GameMapMovement GameMovement = new GameMapMovement(this, fighter, path.serializePath());

        this.sendToField(new FieldNotification(new GameMapMovementMessage(path.serializePath(), fighter.getID())) {
            @Override
            public boolean can(Player perso) {
                return fighter.isVisibleFor(perso);
            }
        });

        fighter.usedMP += path.movementLength;
        this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, fighter.getID(), fighter.getID(), (short) -path.movementLength));

        fighter.setCell(this.getCell(path.getEndCell()));
        this.endSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
        return GameMovement;

    }

    public byte findPlacementDirection(Fighter fighter) {
        if (this.fightState != fightState.STATE_PLACE) {
            throw new Error("State != Placement, cannot give placement direction");
        }
        FightTeam fightTeam = fighter.getTeam() == this.myTeam1 ? this.myTeam2 : this.myTeam1;
        Couple<Short, Integer> bestPos = null; //@Param1 = cellid,@Param2 = Distance
        for (Fighter fightActor : (Iterable<Fighter>) fightTeam.getFighters()::iterator) {
            MapPoint point = fightActor.getMapPoint();
            if (bestPos == null) {
                bestPos = new Couple<>(fightActor.getCellId(), fighter.getMapPoint().distanceToCell(point));
            } else if (fighter.getMapPoint().distanceToCell(point) < bestPos.second) {
                bestPos = new Couple<>(fightActor.getCellId(), fighter.getMapPoint().distanceToCell(point));
            }
        }
        if (bestPos == null) {
            return fighter.getDirection();
        } else {
            return fighter.getMapPoint().advancedOrientationTo(MapPoint.fromCellId(bestPos.first), false);
        }
    }

    public void swapPosition(Fighter fighter, Fighter fighterTarget) {
        FightCell cell = fighter.getMyCell();
        FightCell cell2 = fighterTarget.getMyCell();
        fighter.setCell(cell2);
        fighterTarget.setCell(cell);
        this.fighters().forEach(x -> x.setDirection(this.findPlacementDirection(x)));
        this.sendToField(new GameFightPlacementSwapPositionsMessage(new IdentifiedEntityDispositionInformations[]{fighter.GetIdentifiedEntityDispositionInformations(), fighterTarget.GetIdentifiedEntityDispositionInformations()}));
    }

    public void SetFighterReady(Fighter fighter) {
        // Si combat deja commencé on arrete
        if (this.fightState != fightState.STATE_PLACE) {
            return;
        }

        fighter.setTurnReady(!fighter.isTurnReady());

        this.sendToField(new GameFightHumanReadyStateMessage(fighter.getID(), fighter.isTurnReady()));

        // Debut du combat si tout le monde ready
        if (this.isAllTurnReady() && this.fightType != FightTypeEnum.FIGHT_TYPE_PvT) {
            this.startFight();
        }
    }

    private boolean isAllTurnReady() {
        return this.getAliveFighters().allMatch(Fighter -> Fighter.isTurnReady());
    }

    private void setAllUnReady() {
        this.fighters()
                .filter(fr -> fr instanceof CharacterFighter && ((CharacterFighter) fr).getCharacter().getClient() != null)
                .forEach(fr -> fr.setTurnReady(false));
        /*foreach (var Fighter in this.fighters.Where(Fighter => Fighter is DoubleFighter))
         Fighter.turnReady = true;*/
    }

    public void setFighterPlace(Fighter fighter, short cellId) {
        // Deja pret ?
        if (fighter.isTurnReady()) {
            return;
        }

        FightCell Cell = this.myFightCells.get(fighter.getTeam()).get(cellId);

        // Existante ?
        if (Cell != null) {
            // Aucun persos dessus ?
            if (Cell.CanWalk()) {
                // Affectation
                fighter.setCell(Cell);
                this.fighters().forEach(x -> x.setDirection(this.findPlacementDirection(x)));
                this.sendToField(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
            }
        }
    }

    protected void initFight(Fighter attacker, Fighter defender) {
        // Les leaders d'equipes
        this.myTeam1.setLeader(attacker);
        this.myTeam2.setLeader(defender);

        // On despawn avant la vue du flag de combat
        attacker.joinFight();
        defender.joinFight();

        // Flags de combat
        this.sendFightFlagInfos();

        // Rejoins les combats
        this.joinFightTeam(attacker, this.myTeam1, true, (short) -1, true);
        this.joinFightTeam(defender, this.myTeam2, true, (short) - 1, true);

        // Si un timer pour le lancement du combat
        if (this.getStartTimer() != -1) {
            //FIXME: remove Thread.sleep
            this.startTimer(new CancellableScheduledRunnable(BackGroundWorker, (getStartTimer() * 1000)) {
                @Override
                public void run() {
                    try {
                        Thread.sleep(getStartTimer() * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    startFight();
                }
            }, "startTimer");
        }
    }

    public void sendFightFlagInfos(WorldClient client) {
        if (this.fightState != fightState.STATE_PLACE) {
            return;
        }
        if (this.myTeam1.bladePosition == -1) {
            if (this.myTeam1.leader.getMapCell() != this.myTeam2.leader.getMapCell()) {
                this.myTeam1.bladePosition = this.myTeam1.leader.getMapCell();
                this.myTeam2.bladePosition = this.myTeam2.leader.getMapCell();
            } else {
                this.myTeam1.bladePosition = this.map.getRandomAdjacentFreeCell(this.myTeam2.leader.getMapCell()).getId();
                this.myTeam2.bladePosition = this.myTeam2.leader.getMapCell();
            }
        }
        if (client == null) {
            this.map.sendToField(new GameRolePlayShowChallengeMessage(getFightCommonInformations()));
        } else {
            client.send(new GameRolePlayShowChallengeMessage(getFightCommonInformations()));
        }
    }

    public void sendFightFlagInfos() {
        this.sendFightFlagInfos(null);
    }

    public void onTeamOptionsChanged(FightTeam team, FightOptionsEnum option) {
        this.sendToField(new GameFightOptionStateUpdateMessage(this.fightId, team.id, option.value, team.isToggled(option)));
        if (this.fightState == fightState.STATE_PLACE) {
            this.map.sendToField(new GameFightOptionStateUpdateMessage(this.fightId, team.id, option.value, team.isToggled(option)));
        }
    }

    //int fightId, byte fightType, FightTeamInformations[] fightTeams, int[] fightTeamsPositions, FightOptionsInformations[] fightTeamsOption
    public FightCommonInformations getFightCommonInformations() {
        return new FightCommonInformations(this.fightId, this.fightType.value, new FightTeamInformations[]{this.myTeam1.getFightTeamInformations(), this.myTeam2.getFightTeamInformations()}, new int[]{this.myTeam1.bladePosition, this.myTeam2.bladePosition}, new FightOptionsInformations[]{this.myTeam1.getFightOptionsInformations(), this.myTeam2.getFightOptionsInformations()});
    }

    public void joinFightTeam(Fighter fighter, FightTeam team, boolean leader, short cell, boolean sendInfos) {
        if (!leader) {
            fighter.joinFight();
        }

        // Ajout a la team
        team.fighterJoin(fighter);

        // On envois l'ajout du joueur a la team sur la map BLADE
        if (this.fightState == fightState.STATE_PLACE) {
            this.map.sendToField(new GameFightUpdateTeamMessage(this.fightId, team.getFightTeamInformations()));
        }

        // cell de combat
        if (cell == -1) {
            fighter.setCell(this.getFreeSpawnCell(team));
        } else {
            fighter.setCell(this.getCell(cell));
        }

        if (fighter instanceof CharacterFighter) {
            this.sendPlacementInformation((CharacterFighter) fighter, true);
        }

        if (sendInfos) {
            this.sendToField(new FieldNotification(new GameFightShowFighterMessage(fighter.getGameContextActorInformations(null))) {
                @Override
                public boolean can(Player perso) {
                    return perso.getID() != fighter.getID();
                }
            });
        }

        // this.sendToField(this.getFightTurnListMessage());
    }

    public void sendPlacementInformation(CharacterFighter fighter, boolean update) {
        if (update) {
            fighter.send(new GameContextDestroyMessage());
            fighter.send(new GameContextCreateMessage((byte) 2));
        }
        fighter.send(new GameFightStartingMessage(fightType.value, getTeam1().LeaderId, getTeam2().LeaderId));
        //TODO FriendUpdateMessage OnContexteChanged

        this.sendGameFightJoinMessage(fighter);
        fighter.send(new GameFightPlacementPossiblePositionsMessage(this.myFightCells.get(myTeam1).keySet().stream().mapToInt(x -> x.intValue()).toArray(), this.myFightCells.get(myTeam2).keySet().stream().mapToInt(x -> x.intValue()).toArray(), fighter.getTeam().id));

        if (!update) {
            CharacterHandler.SendCharacterStatsListMessage(fighter.getCharacter().getClient());
        }
        this.fighters().forEach((Actor) -> {
            fighter.send(new GameFightShowFighterMessage(Actor.getGameContextActorInformations(null)));
        });

        fighter.send(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
        fighter.send(new GameFightUpdateTeamMessage(this.fightId, this.getTeam1().getFightTeamInformations()));
        fighter.send(new GameFightUpdateTeamMessage(this.fightId, this.getTeam2().getFightTeamInformations()));
        if (update) {
            this.sendToField(new FieldNotification(new GameFightUpdateTeamMessage(this.fightId, fighter.getTeam().getFightTeamInformations())) {
                @Override
                public boolean can(Player Actor) {
                    return Actor.getID() != fighter.getID();
                }
            });
        }
        this.fighters()
                .filter(fr -> !(fr instanceof VirtualFighter))
                .forEach(fr -> fighter.send(new GameFightHumanReadyStateMessage(fr.getID(), fr.isTurnReady())));
    }

    public void onReconnect(CharacterFighter fighter) {
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 184, new String[]{fighter.getCharacter().getNickName()}));

        if (this.fightState == fightState.STATE_PLACE) {
            this.sendPlacementInformation(fighter, false);
        } else {
            fighter.send(new GameFightStartingMessage(fightType.value, getTeam1().LeaderId, getTeam2().LeaderId));
            this.sendGameFightJoinMessage(fighter);
            this.fighters().forEach((Actor) -> {
                fighter.send(new GameFightShowFighterMessage(Actor.getGameContextActorInformations(fighter.getCharacter())));
            });
            fighter.send(new GameEntitiesDispositionMessage(this.getAliveFighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
            fighter.send(new GameFightResumeMessage(getFightDispellableEffectExtendedInformations(), getAllGameActionMark(), this.fightWorker.fightTurn, (int) (System.currentTimeMillis() - this.fightTime), getIdols(),
                    fighter.getSpellsController().myinitialCooldown.entrySet()
                            .stream()
                            .map(x -> new GameFightSpellCooldown(x.getKey(), x.getValue().initialCooldown))
                            .toArray(GameFightSpellCooldown[]::new),
                    (byte) fighter.getTeam().getAliveFighters()
                                       .filter(x -> x.getSummonerID() == fighter.getID() && !(x instanceof BombFighter))
                                        .count(),
                    (byte) fighter.getTeam().getAliveFighters()
                                       .filter(x -> x.getSummonerID() == fighter.getID() && (x instanceof BombFighter))
                            .count()));
            fighter.send(getFightTurnListMessage());
            fighter.send(new GameFightSynchronizeMessage(this.fighters().map(x -> x.getGameContextActorInformations(fighter.getCharacter())).toArray(GameFightFighterInformations[]::new)));

            /*/213.248.126.93 ChallengeInfoMessage Second8 paket
             /213.248.126.93 ChallengeResultMessage Second9 paket*/
            CharacterHandler.SendCharacterStatsListMessage(fighter.getCharacter().getClient());
            if (this.currentFighter.getID() == fighter.getID()) {
                fighter.send(this.currentFighter.asPlayer().FighterStatsListMessagePacket());
            }
            /*Fighter.send(new GameFightUpdateTeamMessage(this.fightId, this.getTeam1().getFightTeamInformations()));
             Fighter.send(new GameFightUpdateTeamMessage(this.fightId, this.getTeam2().getFightTeamInformations()));*/

            fighter.send(new GameFightNewRoundMessage(this.fightWorker.round));

            fighter.send(new GameFightTurnResumeMessage(this.currentFighter.getID(), this.getTurnTime() / 100, (int) (this.myLoopTimeOut - System.currentTimeMillis()) / 100));
        }
    }

    public GameActionMark[] getAllGameActionMark() {
        GameActionMark[] gameActionMarks = new GameActionMark[0];
        for (CopyOnWriteArrayList<FightActivableObject> objs : this.activableObjects.values()) {
            for (FightActivableObject Object : objs) {
                gameActionMarks = ArrayUtils.add(gameActionMarks, Object.getHiddenGameActionMark());
            }
        }
        return gameActionMarks;
    }

    public FightDispellableEffectExtendedInformations[] getFightDispellableEffectExtendedInformations() {
        FightDispellableEffectExtendedInformations[] FightDispellableEffectExtendedInformations = new FightDispellableEffectExtendedInformations[0];

        for (Stream<BuffEffect> Buffs : (Iterable<Stream<BuffEffect>>) this.getAliveFighters().map(x -> x.getBuff().getAllBuffs())::iterator) {
            for (BuffEffect Buff : (Iterable<BuffEffect>) Buffs::iterator) {
                FightDispellableEffectExtendedInformations = ArrayUtils.add(FightDispellableEffectExtendedInformations, new FightDispellableEffectExtendedInformations(Buff.CastInfos.EffectType.value(), Buff.caster.getID(), Buff.getAbstractFightDispellableEffect()));
            }
        }
        /*return Stream.of(this.getAliveFighters()
         .map(x -> x.buff.getAllBuffs()
         .map(Buff -> (new FightDispellableEffectExtendedInformations(Buff.CastInfos.EffectType.value(), Buff.caster.id, Buff.getAbstractFightDispellableEffect())))
         )).toArray(FightDispellableEffectExtendedInformations[]::new);*/
        return FightDispellableEffectExtendedInformations;
    }

    public byte summonCount() {
        return 0;
    }

    public byte bombCount() {
        return 0;
    }

    public Idol[] getIdols() {
        return new Idol[0];
    }

    protected abstract void sendGameFightJoinMessage(Fighter fighter);

    public boolean onlyOneTeam() {
        boolean Team1 = this.myTeam1.getFighters().anyMatch(Player -> !Player.isMarkedDead() && (!Player.isLeft()));
        boolean Team2 = this.myTeam2.getFighters().anyMatch(Player -> !Player.isMarkedDead() && (!Player.isLeft()));
        /*for (Fighter player : fighters()) {
         if ((player.team.id == 0) && (!player.dead) && (!player.left)) {
         Team1 = true;
         }
         if ((player.team.id == 1) && (!player.dead) && (!player.left)) {
         Team2 = true;
         }
         }*/
        return !(Team1 && Team2);
    }

    public synchronized boolean tryEndFight() {
        if (this.getWinners() != null) {
            this.fightLoopState = fightLoopState.STATE_WAIT_END;
            return true;
        }
        return false;
    }

    private SequenceTypeEnum m_lastSequenceAction;
    private int m_sequenceLevel;
    public boolean IsSequencing;
    public boolean waitAcknowledgment;
    public SequenceTypeEnum sequence;
    private Stack<SequenceTypeEnum> m_sequences = new Stack<>();
    private AtomicInteger contextualIdProvider = new AtomicInteger(-2);

    public boolean startSequence(SequenceTypeEnum sequenceType) {
        this.m_lastSequenceAction = sequenceType;
        ++this.m_sequenceLevel;
        if (this.IsSequencing) {
            return false;
        }
        this.IsSequencing = true;
        this.sequence = sequenceType;
        this.m_sequences.push(sequenceType);
        this.sendToField(new SequenceStartMessage(sequenceType.value,this.currentFighter.getID())); //TODO not Spectator?
        return true;
    }

    public int getNextContextualId() {
        int id = contextualIdProvider.decrementAndGet();
        while (anyFighterMatchId(id)) {
            id = contextualIdProvider.decrementAndGet();
        }
        return id;
    }

    public boolean anyFighterMatchId(final int id) {
        return this.fighters().anyMatch(Fighter -> Fighter.getID() == id);
    }

    public boolean isSequence(SequenceTypeEnum sequenceType) {
        return this.m_sequences.contains(sequenceType);
    }

    public boolean endSequence(SequenceTypeEnum sequenceType) {
        return endSequence(sequenceType, false);
    }

    public boolean endSequence(SequenceTypeEnum sequenceType, boolean force) {
        if (!this.IsSequencing) {
            return false;
        }
        --this.m_sequenceLevel;
        if (this.m_sequenceLevel > 0 && !force) {
            return false;
        }
        this.IsSequencing = false;
        this.waitAcknowledgment = true;
        SequenceTypeEnum sequenceTypeEnum = this.m_sequences.pop();
        if (sequenceTypeEnum != sequenceType) {
            logger.debug("Popped sequence different ({0} != {1})", sequenceTypeEnum.value, sequenceType.value);
        }
        this.sendToField(new SequenceEndMessage(this.m_lastSequenceAction.value, this.currentFighter.getID(), sequenceType.value));
        return true;
    }

    public void endAllSequences() {
        this.m_sequenceLevel = 0;
        this.IsSequencing = false;
        this.waitAcknowledgment = false;
        while (this.m_sequences.size() > 0) {
            this.sendToField(new SequenceEndMessage(this.m_lastSequenceAction.value, this.currentFighter.getID(), this.m_sequences.pop().value));
        }
    }

    public void acknowledgeAction() {
        this.waitAcknowledgment = false;
    }

    protected synchronized void endFight() {
        switch (this.fightState) {
            case STATE_PLACE:
                this.map.sendToField(new GameRolePlayRemoveChallengeMessage(this.fightId));
                break;
            case STATE_FINISH:
                return;
        }

        this.stopTimer("gameLoop");

        this.sendToField(this.myResult);

        this.creationTime = 0;
        this.fightTime = 0;
        this.ageBonus = 0;
        this.endAllSequences();
        this.fighters().forEach(x -> x.endFight());

        this.kickSpectators(true);

        this.myCells.values().stream().forEach((c) -> {
            c.Clear();
        });
        this.myCells.clear();
        this.myTimers.clear();
        this.fightWorker.dispose();
        this.myTeam1.dispose();
        this.myTeam2.dispose();

        this.myFightCells = null;
        this.myCells = null;
        this.myTeam1 = null;
        this.myTeam2 = null;
        this.fightWorker = null;
        this.activableObjects.values().forEach(x -> x.clear());
        this.activableObjects.clear();
        this.activableObjects = null;
        this.myTimers = null;
        this.m_sequences.clear();
        this.m_sequences = null;
        this.contextualIdProvider = null;

        //this.Glyphes = null;
        this.map.removeFight(this);
        this.fightState = fightState.STATE_FINISH;
        this.fightLoopState = fightLoopState.STATE_END_FIGHT;
        if (myTimers != null) {
            try {
                myTimers.values().stream().forEach((CR) -> {
                    CR.cancel();
                });
            } catch (Exception e) {
            } finally {
                myTimers.clear();
                myTimers = null;
            }
        }

    }

    public abstract GameFightEndMessage leftEndMessage(CharacterFighter fighter);

    protected boolean isStarted() {
        return this.fightState != fightState.STATE_INIT && this.fightState != fightState.STATE_PLACE;
    }

    private void kickSpectators(boolean End) {

    }

    public Fighter getFighterOnCell(int cellId) {
        return this.getAliveFighters().filter(Fighter -> Fighter.getCellId() == cellId).findFirst().orElse(null);
    }

    public FightTeam getWinners() {
        if (!this.myTeam1.hasFighterAlive()) {
            return this.myTeam2;
        } else if (!this.myTeam2.hasFighterAlive()) {
            return this.myTeam1;
        }

        return null;
    }

    public FightTeam getLoosers() {
        return this.getEnnemyTeam(this.getWinners());
    }

    /// <summary>
    ///  initialize des tours
    /// </summary>
    /*public void RemakeTurns() {
     this.fightWorker.RemakeTurns(this.fighters());
     }*/
    public synchronized void addNamedParty(CharacterFighter fighter, int outcome) {
        if (fighter instanceof CharacterFighter) {
            if (((CharacterFighter) fighter).getCharacter().getClient() != null && ((CharacterFighter) fighter).getCharacter().getClient().getParty() != null
                    && !((CharacterFighter) fighter).getCharacter().getClient().getParty().partyName.isEmpty()
                    && !Arrays.stream(this.myResult.namedPartyTeamsOutcomes)
                              .anyMatch(x -> x.team.partyName.equalsIgnoreCase(((CharacterFighter) fighter).getCharacter().getClient().getParty().partyName))) {
                this.myResult.namedPartyTeamsOutcomes = ArrayUtils.add(this.myResult.namedPartyTeamsOutcomes, new NamedPartyTeamWithOutcome(new NamedPartyTeam(fighter.getTeam().id, ((CharacterFighter) fighter).getCharacter().getClient().getParty().partyName), outcome));
            }
        }
    }

    public void addActivableObject(Fighter caster, FightActivableObject obj) {
        if (!activableObjects.containsKey(caster)) {
            activableObjects.put(caster, new CopyOnWriteArrayList<>());
        }
        activableObjects.get(caster).add(obj);
    }

    //Dont rename vulnerble
    public Stream<Fighter> fighters() {
        return Stream.concat(this.myTeam1.getFighters(), this.myTeam2.getFighters());
    }

    public Stream<Fighter> getAliveFighters() {
        return Stream.concat(this.myTeam1.getAliveFighters(), this.myTeam2.getAliveFighters());
    }

    public Stream<Fighter> getDeadFighters() {
        //Need Performance Test return Stream.of(this.myTeam1.getDeadFighters(), this.myTeam2.getDeadFighters()).flatMap(x -> x);
        return Stream.concat(this.myTeam1.getDeadFighters(), this.myTeam2.getDeadFighters());
    }

    public FightTeam getTeam1() { //red
        return myTeam1;
    }

    public FightTeam getTeam2() {
        return myTeam2;
    }

    public Fighter getFighter(int FighterId) {
        return this.fighters().filter(x -> x.getID() == FighterId).findFirst().orElse(null);
    }

    public boolean hasObjectOnCell(FightObjectType type, short cell) {
        if (!this.myCells.containsKey(cell)) {
            return false;
        }
        return this.myCells.get(cell).HasObject(type);
    }

    public boolean canPutObject(short cellId) {
        if (!this.myCells.containsKey(cellId)) {
            return false;
        }
        return this.myCells.get(cellId).CanPutObject();
    }

    public boolean isCellWalkable(short CellId) {
        if (!this.myCells.containsKey(CellId)) {
            return false;
        }

        return this.myCells.get(CellId).CanWalk();
    }

    public FightCell getCell(short CellId) {
        if (this.myCells.containsKey(CellId)) {
            return this.myCells.get(CellId);
        }
        return null;
    }

    public Fighter hasEnnemyInCell(short CellId, FightTeam Team) {
        if (CellId == -1) {
            return null;
        }
        return this.myCells.get(CellId).HasEnnemy(Team);
    }

    public Fighter hasFriendInCell(short CellId, FightTeam Team) {
        if (CellId == -1) {
            return null;
        }
        return this.myCells.get(CellId).HasFriend(Team);
    }

    public GameFightTurnListMessage getFightTurnListMessage() {
        return new GameFightTurnListMessage(this.getAliveFighters().filter(x -> !(x instanceof StaticFighter))
                .sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false)))
                .mapToInt(x -> x.getID()).toArray(), this.getDeadFighters().filter(x -> !(x instanceof StaticFighter))
                .mapToInt(x -> x.getID()).toArray());
    }

    private synchronized FightCell getFreeSpawnCell(FightTeam Team) {
        for (FightCell Cell : this.myFightCells.get(Team).values()) {
            if (Cell.CanWalk()) {
                return Cell;
            }
        }
        if (!this.myFightCells.get(Team).isEmpty()) {
            return this.myFightCells.get(Team).values().stream().filter(x -> x.GetObjectsAsFighter() == null).findFirst().get();
        }
        return null;
    }

    @Override
    public void actorMoved(Path Path, IGameActor Actor, short newCell, byte newDirection) {
        //((Fighter) actor).setCell(myCells.get(newCell));
        Actor.setDirection(newDirection);
    }

    public void stopTimer(String Name) {
        synchronized ($mutex_lock) {
            try {
                if (this.myTimers.containsKey(Name)) {
                    myTimers.get(Name).cancel();
                    this.myTimers.remove(Name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startTimer(CancellableScheduledRunnable CR, String Name) {
        synchronized ($mutex_lock) {
            try {
                this.myTimers.put(Name, CR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlacementTimeLeft() {
        if (this.isStarted()) {
            return 0;
        }
        double num = (double) (this.getStartTimer() - (Instant.now().getEpochSecond() - this.creationTime)) * 10;
        if (num < 0.0) {
            num = 0.0;
        }
        return (int) num;
    }

}
