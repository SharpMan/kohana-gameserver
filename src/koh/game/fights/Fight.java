package koh.game.fights;

import java.time.Instant;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightGlyph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import koh.commons.CancellableExecutorRunnable;
import koh.game.Main;
import koh.game.actions.GameAction;
import koh.game.actions.GameMapMovement;
import koh.game.dao.SpellDAO;
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
import koh.protocol.messages.game.context.GameContextQuitMessage;
import koh.protocol.messages.game.context.GameEntitiesDispositionMessage;
import koh.protocol.messages.game.context.GameMapMovementMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.messages.game.context.fight.GameFightEndMessage;
import koh.protocol.messages.game.context.fight.GameFightHumanReadyStateMessage;
import koh.protocol.messages.game.context.fight.GameFightJoinMessage;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Neo-Craft
 */
public abstract class Fight extends IWorldEventObserver implements IWorldField {

    //public static final ImprovedCachedThreadPool BackGroundWorker2 = new ImprovedCachedThreadPool(5, 50, 2);
    public static final ScheduledExecutorService BackGroundWorker = Executors.newScheduledThreadPool(50);

    public static final Random RANDOM = new Random();
    private static final HashMap<Integer, HashMap<Integer, Short[]>> MAP_FIGHTCELLS = new HashMap<>();
    protected FightTeam myTeam1 = new FightTeam((byte) 0, this);
    protected FightTeam myTeam2 = new FightTeam((byte) 1, this);

    protected long myLoopTimeOut = -1;
    protected long myLoopActionTimeOut;
    protected int myNextID = -1000;
    protected ArrayList<GameAction> myActions = new ArrayList<>();
    protected GameFightEndMessage myResult;

    public enum FightLoopState {

        STATE_WAIT_START, STATE_WAIT_TURN, STATE_WAIT_ACTION, STATE_WAIT_READY, STATE_WAIT_END, STATE_WAIT_AI, STATE_END_TURN, STATE_END_FIGHT,
    }

    public short FightId;
    public FightState FightState;
    public FightLoopState FightLoopState;
    public DofusMap Map;
    public Fighter CurrentFighter;
    public long FightTime, CreationTime;
    public FightTypeEnum FightType;
    protected Map<Short, FightCell> myCells = new HashMap<>();
    protected Map<FightTeam, Map<Short, FightCell>> myFightCells = new HashMap<>();
    protected short AgeBonus = -1, lootShareLimitMalus = -1;

    protected Map<String, CancellableExecutorRunnable> myTimers = new HashMap<>();
    public Map<Fighter, CopyOnWriteArrayList<FightActivableObject>> m_activableObjects = Collections.synchronizedMap(new HashMap<>());
    private final Object $mutex_lock = new Object();
    public FightWorker myWorker = new FightWorker(this);
    public AtomicInteger NextTriggerUid = new AtomicInteger();

    public synchronized int NextID() {
        return this.myNextID--;
    }

    public Fight(FightTypeEnum Type, DofusMap Map) {
        this.FightState = FightState.STATE_PLACE;
        this.FightTime = -1;
        this.CreationTime = Instant.now().getEpochSecond();
        this.FightType = Type;
        this.Map = Map;
        this.FightId = this.Map.NextFightId();
        this.InitCells();
    }

    public FighterRefusedReasonEnum CanJoin(FightTeam Team, Player Character) {
        if (Team.CanJoin(Character) != FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
            return Team.CanJoin(Character);
        } else if (this.GetFreeSpawnCell(Team) == null) {
            return FighterRefusedReasonEnum.TEAM_FULL;
        } else {
            return FighterRefusedReasonEnum.FIGHTER_ACCEPTED;
        }
    }

    public boolean CanJoinSpectator() {
        return this.FightState == FightState.STATE_ACTIVE && !this.myTeam1.IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_SECRET) && !this.myTeam2.IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_SECRET);
    }

    public FightTeam GetEnnemyTeam(FightTeam Team) {
        return (Team == this.myTeam1 ? this.myTeam2 : this.myTeam1);
    }

    public FightTeam GetTeam(int LeaderId) {
        return (this.myTeam1.LeaderId == LeaderId ? this.myTeam1 : this.myTeam2);
    }

    public abstract void LeaveFight(Fighter Fighter);

    public abstract void EndFight(FightTeam Winners, FightTeam Loosers);

    public abstract int GetStartTimer();

    public abstract int GetTurnTime();

    public boolean HasFinished = false;

    private void InitCells() {
        // Ajout des cells
        for (DofusCell Cell : this.Map.GetCells()) {
            this.myCells.put(Cell.Id, new FightCell(Cell.Id, Cell.Mov(), Cell.Los()));
        }
        this.myFightCells.put(myTeam1, new HashMap<>());
        this.myFightCells.put(myTeam2, new HashMap<>());

        if (Fight.MAP_FIGHTCELLS.containsKey(this.Map.Id)) {
            // Ajout
            synchronized (Fight.MAP_FIGHTCELLS) {
                for (Short Cell : Fight.MAP_FIGHTCELLS.get(this.Map.Id).get(0)) {
                    this.myFightCells.get(this.myTeam1).put(Cell, this.myCells.get(Cell));
                }
                for (Short Cell : Fight.MAP_FIGHTCELLS.get(this.Map.Id).get(1)) {
                    this.myFightCells.get(this.myTeam2).put(Cell, this.myCells.get(Cell));
                }
            }
            return;
        }

        for (Short CellValue : this.Map.RedCells) {
            FightCell Cell = this.myCells.get(CellValue);
            if (Cell == null || !Cell.CanWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam1).put(CellValue, Cell);
        }

        for (Short CellValue : this.Map.BlueCells) {
            FightCell Cell = this.myCells.get(CellValue);
            if (Cell == null || !Cell.CanWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam2).put(CellValue, Cell);
        }

        if (this.Map.BlueCells.length == 0 || this.Map.RedCells.length == 0) {
            this.myFightCells.get(this.myTeam1).clear();
            this.myFightCells.get(this.myTeam2).clear();
            Couple<ArrayList<FightCell>, ArrayList<FightCell>> startCells = Algo.GenRandomFightPlaces(this);
            for (FightCell Cell : startCells.first) {
                this.myFightCells.get(this.myTeam1).put(Cell.Id, Cell);
            }
            for (FightCell Cell : startCells.second) {
                this.myFightCells.get(this.myTeam2).put(Cell.Id, Cell);
            }
            synchronized (Fight.MAP_FIGHTCELLS) {
                Fight.MAP_FIGHTCELLS.put(this.Map.Id, new HashMap<>());
                Fight.MAP_FIGHTCELLS.get(this.Map.Id).put(0, this.myFightCells.get(this.myTeam1).keySet().toArray(new Short[this.myFightCells.get(this.myTeam1).size()]));
                Fight.MAP_FIGHTCELLS.get(this.Map.Id).put(1, this.myFightCells.get(this.myTeam2).keySet().toArray(new Short[this.myFightCells.get(this.myTeam2).size()]));
            }
        }

    }

    public static final StatsEnum[] EffectNotSilenced = new StatsEnum[]{
        StatsEnum.Damage_Neutral, StatsEnum.Damage_Earth, StatsEnum.Damage_Air, StatsEnum.Damage_Fire, StatsEnum.Damage_Water,
        StatsEnum.Steal_Neutral, StatsEnum.Steal_Earth, StatsEnum.Steal_Air, StatsEnum.Steal_Fire, StatsEnum.Steal_Water, StatsEnum.Steal_PV_Fix,
        StatsEnum.DamageLifeNeutre, StatsEnum.DamageLifeEau, StatsEnum.DamageLifeTerre, StatsEnum.DamageLifeAir, StatsEnum.DamageLifeFeu, StatsEnum.DamageDropLife
    };

    public void Disconnect(CharacterFighter Fighter) {
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 182, new String[]{Fighter.Character.NickName, Integer.toString(Fighter.TurnRunning)}));
        if (this.CurrentFighter.ID == Fighter.ID) {
            this.FightLoopState = FightLoopState.STATE_END_TURN;
        }
        Fighter.TurnReady = true;
    }

    public void LaunchSpell(Fighter Fighter, SpellLevel SpellLevel, short CellId, boolean friend) {
        LaunchSpell(Fighter, SpellLevel, CellId, friend, false, true);
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
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.m_activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled) {
                    Portails = ArrayUtils.add(Portails, (FightPortal) Object);
                    if (Object.CellId() == param1) {
                        _loc3_ = Object.MapPoint();
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
        final int[] _loc10_ = LinkedCellsManager.getLinks(_loc3_, Arrays.stream(Portails)/*.filter(x -> x.m_caster.Team == Fighter.Team)*/.map(x -> x.MapPoint()).toArray(MapPoint[]::new));
        MapPoint _loc11_ = MapPoint.fromCellId(_loc10_[/*_loc10_.length == 0 ? 0 :*/_loc10_.length - 1]);
        MapPoint _loc12_ = MapPoint.fromCellId(Fighter.CellId());
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
         damagetoReturn += Arrays.stream(Portails).filter(y -> y.CellId() == i).findFirst().get().damageValue;
         }*/
        int[] PortailIds = new int[_loc10_.length];
        FightPortal Portal;
        for (int i = 0; i < _loc10_.length; i++) {
            final int ID = _loc10_[i];
            Portal = Arrays.stream(Portails).filter(y -> y.CellId() == ID).findFirst().get();
            damagetoReturn += Portal.damageValue;
            PortailIds[i] = Portal.ID;
        }
        return new Three<>((int) _loc16_.get_cellId(), PortailIds, damagetoReturn);
    }

    public void LaunchSpell(Fighter Fighter, SpellLevel SpellLevel, short CellId, boolean friend, boolean fakeLaunch, boolean imTargeted) {
        if (this.FightState != FightState.STATE_ACTIVE) {
            return;
        }
        short oldCell = CellId;
        if (SpellLevel.id == 10461 && Fighter instanceof CharacterFighter && ((CharacterFighter) Fighter).Character.InventoryCache.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON) != null) {
            this.LaunchWeapon(((CharacterFighter) Fighter), CellId);
            return;
        }

        // La cible si elle existe
        Fighter TargetE = this.HasEnnemyInCell(CellId, Fighter.Team);
        if (friend && TargetE == null) {
            TargetE = this.HasFriendInCell(CellId, Fighter.Team);
        }
        int TargetId = TargetE == null ? -1 : TargetE.ID;

        // Peut lancer le sort ?
        if (!fakeLaunch && !this.CanLaunchSpell(Fighter, SpellLevel, Fighter.CellId(), CellId, TargetId)) {
            Fighter.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            this.EndSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
            return;
        }
        if (!fakeLaunch) {
            this.StartSequence(SequenceTypeEnum.SEQUENCE_SPELL);

            Fighter.UsedAP += SpellLevel.ApCost;
            Fighter.SpellsController.Actualise(SpellLevel, TargetId);
        }

        boolean IsCc = false;
        if (SpellLevel.criticalHitProbability != 0 && SpellLevel.criticalEffect.length > 0) {
            int TauxCC = SpellLevel.criticalHitProbability - Fighter.Stats.GetTotal(StatsEnum.Add_CriticalHit);
            if (TauxCC < 2) {
                TauxCC = 2;
            }
            if (Fight.RANDOM.nextInt(TauxCC) == 0) {
                IsCc = true;
            }
        }
        IsCc &= !Fighter.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);

        EffectInstanceDice[] Effects = IsCc ? SpellLevel.criticalEffect : SpellLevel.effects;
        if (Effects == null) {
            Effects = SpellLevel.criticalEffect;
        }

        boolean silentCast = Arrays.stream(Effects).allMatch(x -> !ArrayUtils.contains(EffectNotSilenced, x.EffectType()));

        if (!fakeLaunch) {
            Three<Integer, int[], Integer> Informations = null;
            if (this.GetCell(CellId).HasGameObject(FightObjectType.OBJECT_PORTAL) && !Arrays.stream(Effects).anyMatch(Effect -> Effect.EffectType().equals(StatsEnum.DISABLE_PORTAL))) {
                Informations = this.getTargetThroughPortal(Fighter, CellId, true);
                CellId = Informations.first.shortValue();
                //this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"DamagePercentBoosted Suite au portails = " + DamagePercentBoosted}));

            }
            for (Player player : this.Observable$stream()) {
                player.Send(new GameActionFightSpellCastMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, Fighter.ID, TargetId, CellId, (byte) (IsCc ? 2 : 1), SpellLevel.spellId == 2763 ? true : (!Fighter.IsVisibleFor(player) || silentCast), SpellLevel.spellId, SpellLevel.grade, Informations == null ? new int[0] : Informations.second));
            }
            if (Informations != null) {
                Informations.Clear();
            }
        }

        HashMap<EffectInstanceDice, ArrayList<Fighter>> Targets = new HashMap<>();
        for (EffectInstanceDice Effect : Effects) {
            Effect.parseZone();
            System.out.println(Effect.toString());
            Targets.put(Effect, new ArrayList<>());
            for (short Cell : (new Zone(Effect.ZoneShape(), Effect.ZoneSize(), MapPoint.fromCellId(Fighter.CellId()).advancedOrientationTo(MapPoint.fromCellId(CellId), true),this.Map)).GetCells(CellId)) {
                FightCell FightCell = this.GetCell(Cell);
                if (FightCell != null && FightCell.HasGameObject(FightObjectType.OBJECT_PORTAL)) {

                }
                if (FightCell != null) {
                    if (FightCell.HasGameObject(FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
                        for (Fighter Target : FightCell.GetObjectsAsFighter()) {
                            if (Effect.IsValidTarget(Fighter, Target) && EffectInstanceDice.verifySpellEffectMask(Fighter, Target, Effect)) {
                                if (Effect.targetMask.equals("C") && Fighter.GetCarriedActor() == Target.ID) {
                                    continue;
                                } else if (Effect.targetMask.equals("a,A") && Fighter.GetCarriedActor() != 0 & Fighter.ID == Target.ID) {
                                    continue;
                                }
                                if (Fighter instanceof BombFighter && Target.States.HasState(FightStateEnum.Kaboom)) {
                                    continue;
                                }
                                if (!imTargeted && Target.ID == Fighter.ID) {
                                    continue;
                                }
                                /*if(Effect.category() == EffectHelper.DAMAGE_EFFECT_CATEGORY && !EffectInstanceDice.verifySpellEffectMask(Fighter, Target, Effect)){
                                 continue;
                                 }*/
                                Main.Logs().writeDebug("Targeet Aded!");
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
            Targets.get(Effect).removeIf(F -> F.Dead());
            if (Effect.EffectType() == ADD_BASE_DAMAGE_SPELL) {
                Targets.get(Effect).clear();
                Targets.get(Effect).add(Fighter);
            }
            if (Effect.delay > 0) {
                //TODO: Set ParentBoost UID
                Fighter.Buffs.DelayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), SpellLevel.spellId, CellId, num1, Effect, Fighter, Targets.get(Effect), false, StatsEnum.NONE, 0, SpellLevel), Effect.delay));
                Targets.get(Effect).stream().forEach((Target) -> {
                    this.sendToField(new GameActionFightDispellableEffectMessage(Effect.effectId, Fighter.ID, new FightTriggeredEffect(Target.NextBuffUid.incrementAndGet(), Target.ID, (short) Effect.duration, FightDispellableEnum.DISPELLABLE, SpellLevel.spellId, Effect.effectUid, 0, (short) Effect.diceNum, (short) Effect.diceSide, (short) Effect.value, (short) Effect.delay)));
                });

                /*for (Fighter Target : Targets.get(Effect)) {
                 Target.Buffs.DelayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), SpellLevel.spellId, CellId, num1, Effect, Fighter, new ArrayList<Fighter>() {
                 {
                 add(Target);
                 }
                 }, false, StatsEnum.NONE, 0, SpellLevel), Effect.delay));
                 }*/
                continue;
            }
            EffectCast CastInfos = new EffectCast(Effect.EffectType(), SpellLevel.spellId, CellId, num1, Effect, Fighter, Targets.get(Effect), false, StatsEnum.NONE, 0, SpellLevel);
            CastInfos.targetKnownCellId = CellId;
            CastInfos.oldCell = oldCell;
            if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                break;
            }
        }

        if (!fakeLaunch) {
            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, Fighter.ID, Fighter.ID, (short) -SpellLevel.ApCost));
        }

        if (!fakeLaunch && Fighter.VisibleState == GameActionFightInvisibilityStateEnum.INVISIBLE && silentCast && SpellLevel.spellId != 2763) {
            this.sendToField(new ShowCellMessage(Fighter.ID, Fighter.CellId()));
        }

        if (!fakeLaunch) {
            this.EndSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
        }
    }

    public void LaunchWeapon(CharacterFighter Fighter, short CellId) {
        // Combat encore en cour ?
        if (this.FightState != FightState.STATE_ACTIVE) {
            return;
        }
        if (Fighter != this.CurrentFighter) {
            return;
        }
        InventoryItem Weapon = Fighter.Character.InventoryCache.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON);
        if (Weapon.Template().TypeId == 83) { //Pière d'Ame
            return;
        }
        // La cible si elle existe
        Fighter TargetE = this.HasEnnemyInCell(CellId, Fighter.Team);
        if (TargetE == null) {
            TargetE = this.HasFriendInCell(CellId, Fighter.Team);
        }
        int TargetId = TargetE == null ? -1 : TargetE.ID;

        if (!(Pathfinder.GoalDistance(Map, CellId, Fighter.CellId()) <= Weapon.WeaponTemplate().range && Pathfinder.GoalDistance(Map, CellId, Fighter.CellId()) >= Weapon.WeaponTemplate().minRange && Fighter.AP() >= Weapon.WeaponTemplate().apCost)) {
            Fighter.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            return;
        }

        this.StartSequence(SequenceTypeEnum.SEQUENCE_WEAPON);

        Fighter.UsedAP += Weapon.WeaponTemplate().apCost;

        boolean IsCc = false;

        int TauxCC = Weapon.WeaponTemplate().criticalHitProbability - Fighter.Stats.GetTotal(StatsEnum.Add_CriticalHit);
        if (TauxCC < 2) {
            TauxCC = 2;
        }
        if (Fight.RANDOM.nextInt(TauxCC) == 0) {
            IsCc = true;
        }

        IsCc &= !Fighter.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);

        ArrayList<Fighter> Targets = new ArrayList<>(4);

        for (short Cell : (new Zone(SpellShapeEnum.valueOf(Weapon.ItemType().zoneShape()), Weapon.ItemType().zoneSize(), MapPoint.fromCellId(Fighter.CellId()).advancedOrientationTo(MapPoint.fromCellId(CellId), true),this.Map)).GetCells(CellId)) {
            FightCell FightCell = this.GetCell(Cell);
            if (FightCell != null) {
                if (FightCell.HasGameObject(FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
                    Targets.addAll(FightCell.GetObjectsAsFighterList());
                }
            }
        }

        Targets.removeIf(F -> F.Dead());
        Targets.remove(Fighter);
        ObjectEffectDice[] Effects = Weapon.getEffects().stream().filter(Effect -> Effect instanceof ObjectEffectDice && ArrayUtils.contains(EffectHelper.unRandomablesEffects, Effect.actionId)).map(x -> (ObjectEffectDice) x).toArray(ObjectEffectDice[]::new);

        double num1 = Fight.RANDOM.nextDouble();

        double num2 = (double) Arrays.stream(Effects).mapToInt(Effect -> ((EffectInstanceDice) Weapon.Template().GetEffect(Effect.actionId)).random).sum();
        boolean flag = false;

        this.sendToField(new GameActionFightCloseCombatMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, Fighter.ID, TargetId, CellId, (byte) (IsCc ? 2 : 1), false, Weapon.Template().id));

        EffectInstanceDice EffectFather;
        for (ObjectEffectDice Effect : Effects) {
            EffectFather = (EffectInstanceDice) Weapon.Template().GetEffect(Effect.actionId);
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
            EffectCast CastInfos = new EffectCast(StatsEnum.valueOf(Effect.actionId), 0, CellId, num1, EffectFather, Fighter, Targets, true, StatsEnum.NONE, 0, null);
            CastInfos.targetKnownCellId = CellId;
            if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                break;
            }
        }

        this.sendToField(new GameActionFightPointsVariationMessage(!IsCc ? ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT : ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT_CRITICAL_MISS, Fighter.ID, Fighter.ID, (short) Weapon.WeaponTemplate().apCost));
        this.EndSequence(SequenceTypeEnum.SEQUENCE_WEAPON, false);
    }

    public boolean CanLaunchSpell(Fighter Fighter, SpellLevel Spell, short CurrentCell, short CellId, int TargetId) {
        // Fake caster
        if (Fighter != this.CurrentFighter) {
            return false;
        }

        // Fake cellId
        if (!this.myCells.containsKey(CellId)) {
            return false;
        }

        // PA manquant ?
        if (Fighter.AP() < Spell.ApCost) {
            return false;
        }

        //Todo check po PO
        if (!this.Map.getCell(CellId).Walakable() || this.Map.getCell(CellId).NonWalkableDuringFight()) {
            return false;
        }
        //TargetId == -1
        return (!Spell.needFreeCell || TargetId == -1) && (!Spell.needTakenCell || TargetId != -1)
                && !Arrays.stream(Spell.statesForbidden).anyMatch(x -> Fighter.HasState(x))
                && !Arrays.stream(Spell.statesRequired).anyMatch(x -> !Fighter.HasState(x))
                && ArrayUtils.contains(Fighter.GetCastZone(Spell), CellId)
                && Fighter.SpellsController.CanLaunchSpell(Spell, TargetId);

    }

    public void ToggleLock(Fighter Fighter, FightOptionsEnum Type) {
        boolean Value = Fighter.Team.IsToggle(Type) == false;
        Fighter.Team.Toggle(Type, Value);
        if (this.FightState == FightState.STATE_PLACE) {
            this.Map.sendToField(new GameFightOptionStateUpdateMessage(this.FightId, Fighter.Team.Id, Type.value, Value));
        }

        Message Message = null;
        switch (Type) {
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
                    this.KickSpectators();
                } else {
                    Message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 39);
                }
                break;
        }
        this.sendToField(Message);
    }

    public void KickSpectators() {

    }

    public synchronized void StartFight() {
        // Si combat deja lancé
        if (this.FightState != FightState.STATE_PLACE) {
            return;
        }

        this.Map.sendToField(new GameRolePlayRemoveChallengeMessage(this.FightId));

        // Preparation du lancement
        this.FightState = FightState.STATE_INIT;

        //TODO : CHALLENGE
        // Arret du timer
        this.StopTimer("StartTimer");
        this.FightTime = System.currentTimeMillis();

        // Init des tours
        this.myWorker.InitTurns();

        this.sendToField(new GameEntitiesDispositionMessage(this.Fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
        this.sendToField(new GameFightStartMessage(new Idol[0]));
        // Liste des tours
        //this.sendToField(new GameFightTurnListMessage(this.myWorker.Fighters().stream().filter(x -> x.IsAlive()).mapToInt(x -> x.ID).toArray(), this.myWorker.Fighters().stream().filter(x -> !x.IsAlive()).mapToInt(x -> x.ID).toArray()));
        this.sendToField(FightTurnListMessage());
        this.sendToField(new GameFightSynchronizeMessage(this.Fighters().map(x -> x.GetGameContextActorInformations(null)).toArray(GameFightFighterInformations[]::new)));

        // Reset du ready
        this.SetAllUnReady();
        // En attente de lancement
        this.FightLoopState = FightLoopState.STATE_WAIT_START;

        // Lancement du gameLoop 10 ms d'interval.
        this.StartTimer(new CancellableExecutorRunnable(BackGroundWorker, 10, 10) {
            @Override
            public void run() {
                GameLoop();
            }
        }, "GameLoop");
    }

    private void GameLoop() {
        try {
            // Switch sur le status et verif fin de tour
            switch (this.FightLoopState) {
                case STATE_WAIT_START: // En attente de lancement
                    this.FightState = FightState.STATE_ACTIVE;
                    this.FightLoopState = FightLoopState.STATE_WAIT_READY;
                    this.BeginTurn();
                    break;

                case STATE_WAIT_TURN: // Fin du tour par force a cause du timeout
                    if (this.myLoopTimeOut < System.currentTimeMillis()) {
                        if (this.IsActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                            this.EndTurn(); // Fin du tour
                        }
                    }
                    break;

                case STATE_END_TURN: // Fin du tour par le joueur
                    if (this.IsActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                        this.EndTurn(); // Fin du tour
                    }
                    break;

                case STATE_WAIT_READY: // En attente des joueurs x ...
                    if (this.IsAllTurnReady()) {
                        this.MiddleTurn();
                        this.BeginTurn();
                    } else if (this.myLoopTimeOut + 5000 < System.currentTimeMillis()) {
                        this.sendToField(new TextInformationMessage((byte) 1, 29, new String[]{StringUtils.join(this.AliveFighters().filter(x -> !x.TurnReady && x instanceof CharacterFighter).map(y -> ((CharacterFighter) y).Character.NickName).toArray(String[]::new), ", ")}));
                        this.MiddleTurn();
                        this.BeginTurn();
                    }
                    break;

                case STATE_WAIT_AI: // Artificial intelligence
                     /*if (this.CurrentFighter istanceof VirtualFighter)
                     {
                     // Lancement de l'IA pour 30 secondes maximum
                     (this.CurrentFighter as VirtualFighter).Mind.runAI();
                     //new AIProcessor(this, this.CurrentFighter).applyIA(Environment.TickCount + 30000); 
                     }
                     else*/
                    if (this.CurrentFighter.ObjectType() == FightObjectType.OBJECT_STATIC) {
                        this.myLoopActionTimeOut = System.currentTimeMillis() + 750;
                    }
                    // Fin de tour
                    if (this.FightLoopState != FightLoopState.STATE_WAIT_END) {
                        this.FightLoopState = FightLoopState.STATE_END_TURN;
                    }
                    break;

                case STATE_WAIT_END: // Fin du combat
                    if (!HasFinished || this.IsActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                        this.EndTurn(true);
                        //System.Threading.Thread.Sleep(500);
                        this.myTeam1.EndFight();
                        this.myTeam2.EndFight();
                        this.EndFight(this.GetWinners(), this.GetEnnemyTeam(this.GetWinners()));
                        HasFinished = true;
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
    public boolean IsActionsFinish() {
        return this.myActions.isEmpty();
    }

    public synchronized void BeginTurn() {
        // Mise a jour du combattant
        this.CurrentFighter = this.myWorker.GetNextFighter();

        this.StartSequence(SequenceTypeEnum.SEQUENCE_TURN_END);

        // Activation des buffs et fightObjects
        int BeginTurnIndice = this.CurrentFighter.BeginTurn();

        this.EndSequence(SequenceTypeEnum.SEQUENCE_TURN_END, false);

        // Mort du joueur ou fin de combat
        if (BeginTurnIndice == -3 || BeginTurnIndice == -2) {
            return;
        }

        // Envois debut du tour
        this.sendToField(new GameFightTurnStartMessage(this.CurrentFighter.ID, this.GetTurnTime() / 100));

        if (this.CurrentFighter instanceof CharacterFighter) {
            this.CurrentFighter.Send(((CharacterFighter) this.CurrentFighter).FighterStatsListMessagePacket());
        }

        this.observers.stream().forEach((o) -> {
            ((Player) o).Send(new GameFightSynchronizeMessage(this.Fighters().map(x -> x.GetGameContextActorInformations((Player) o)).toArray(GameFightFighterInformations[]::new)));
        });

        this.CurrentFighter.Send(new GameFightTurnStartPlayingMessage());

        // Timeout du tour
        this.myLoopTimeOut = System.currentTimeMillis() + this.GetTurnTime();

        // Status en attente de fin de tour
        if (this.CurrentFighter instanceof CharacterFighter && ((CharacterFighter) CurrentFighter).Character.Client == null && this.CurrentFighter.Team.GetAliveFighters().count() > 1L) {
            this.FightLoopState = FightLoopState.STATE_END_TURN;
        } else {
            this.FightLoopState = FightLoopState.STATE_WAIT_TURN;
        }

            //Chalenge
            /*if (this instanceof MonsterFight && this.CurrentFighter instanceof CharacterFighter)
         {
         foreach (var Challenge in Challanges)
         {
         Challenge.BeginTurn(this.CurrentFighter);
         }
         }*/
        // Monstre passe le tour
        if (/*this.CurrentFighter instanceof VirtualFighter*/this.CurrentFighter instanceof StaticFighter) {
            this.FightLoopState = FightLoopState.STATE_WAIT_AI;
        }
    }

    public void EndTurn() {
        EndTurn(false);
    }

    public void EndTurn(boolean Finish) {
        this.StartSequence(SequenceTypeEnum.SEQUENCE_TURN_END);
        // Fin du tour, activation des buffs, pieges etc
        if (this.CurrentFighter.EndTurn() == -3) {
            return;
        } else if (m_activableObjects.containsKey(CurrentFighter)) {
            m_activableObjects.get(CurrentFighter).stream().filter(x -> x instanceof FightGlyph).forEach(y -> y.DecrementDuration());
            m_activableObjects.get(CurrentFighter).removeIf(fightObject -> fightObject.ObjectType() == FightObjectType.OBJECT_GLYPHE && fightObject.Duration <= 0);
        }
        this.EndSequence(SequenceTypeEnum.SEQUENCE_TURN_END, false);
        // Combat fini a la fin de son tour

        /* if (this instanceof MonsterFight && this.CurrentFighter  instanceof CharacterFighter)
         {
         this.Challanges.ForEach(x => x.EndTurn(this.CurrentFighter));
         }*/
        // Tout le monde doit se synchro
        this.SetAllUnReady();
        if (!Finish) // En attente des joueurs
        {
            this.FightLoopState = FightLoopState.STATE_WAIT_READY;
        }

        if (this.IsSequencing) {
            this.EndSequence(this.Sequence, true);
        }
        if (this.WaitAcknowledgment) {
            this.AcknowledgeAction();
        }

        // Tour fini
        this.sendToField(new GameFightTurnEndMessage(this.CurrentFighter.ID));
        if (!Finish) {
            this.sendToField(new GameFightTurnReadyRequestMessage(this.CurrentFighter.ID));
        }
    }

    public void MiddleTurn() {
        this.CurrentFighter.MiddleTurn();

    }

    protected void OnTackled(Fighter Fighter, MovementPath path) {
        ArrayList<Fighter> tacklers = Pathfinder.GetEnnemyNearToTakle(this, Fighter.Team, Fighter.CellId());

        int tackledMp = Fighter.GetTackledMP();
        int tackledAp = Fighter.GetTackledAP();
        if (Fighter.MP() - tackledMp < 0) {
            Main.Logs().writeError(String.format("Cannot apply tackle : mp tackled ({0}) > available mp ({1})", tackledMp, Fighter.MP()));
        } else {
            this.sendToField(new GameActionFightTackledMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_TACKLED, Fighter.ID, tacklers.stream().mapToInt(x -> x.ID).toArray()));

            Fighter.UsedAP += tackledAp;
            Fighter.UsedMP += tackledMp;

            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, Fighter.ID, Fighter.ID, (short) -tackledAp));
            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, Fighter.ID, Fighter.ID, (short) -tackledMp));

            if (path.MovementLength <= Fighter.MP()) {
                return;
            }
            path.CutPath(Fighter.MP() + 1);
        }
    }

    public void AffectSpellTo(Fighter Caster, Fighter Target, int Level, int... Spells) {
        SpellLevel Spell;
        for (int Spellid : Spells) {
            Spell = SpellDAO.Spells.get(Spellid).SpellLevel(Level);
            double num1 = Fight.RANDOM.nextDouble();
            double num2 = (double) Arrays.stream(Spell.effects).mapToInt(x -> x.random).sum();
            boolean flag = false;
            for (EffectInstanceDice Effect : Spell.effects) {
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
                ArrayList<Fighter> target = new ArrayList<Fighter>() {
                    {
                        add(Target);
                    }
                };
                if (Effect.delay > 0) {
                    Target.Buffs.DelayedEffects.add(new Couple<>(new EffectCast(Effect.EffectType(), Spellid, Target.CellId(), num1, Effect, Caster, target, false, StatsEnum.NONE, 0, Spell), Effect.delay));
                    this.sendToField(new GameActionFightDispellableEffectMessage(Effect.effectId, Caster.ID, new FightTriggeredEffect(Target.NextBuffUid.incrementAndGet(), Target.ID, (short) Effect.duration, FightDispellableEnum.DISPELLABLE, Spellid, Effect.effectUid, 0, (short) Effect.diceNum, (short) Effect.diceSide, (short) Effect.value, (short) Effect.delay)));
                    continue;
                }
                EffectCast CastInfos = new EffectCast(Effect.EffectType(), Spellid, Target.CellId(), num1, Effect, Caster, target, false, StatsEnum.NONE, 0, Spell);
                CastInfos.targetKnownCellId = Target.CellId();
                if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                    break;
                }
            }
        }
    }

    public synchronized GameMapMovement TryMove(Fighter Fighter, MovementPath Path) {
        // Pas a lui de jouer
        if (Fighter != this.CurrentFighter) {
            return null;
        }

        // Pas assez de point de mouvement
        if (Path.MovementLength > Fighter.MP() || Path.MovementLength == -1) {
            System.out.println(Path.MovementLength > Fighter.MP());
            System.out.println(Path.MovementLength + " " + Fighter.MP());
            return null;
        }

        this.StartSequence(SequenceTypeEnum.SEQUENCE_MOVE);

        if ((Fighter.GetTackledMP() > 0 || Fighter.GetTackledAP() > 0) && !this.CurrentFighter.States.HasState(FightStateEnum.Enraciné)) {
            this.OnTackled(Fighter, Path);
            if (Path.TransitCells.isEmpty() || Path.MovementLength == 0) {
                this.EndSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
                return null;
            }

        }
        GameMapMovement GameMovement = new GameMapMovement(this, Fighter, Path.SerializePath());

        this.sendToField(new FieldNotification(new GameMapMovementMessage(Path.SerializePath(), Fighter.ID)) {
            @Override
            public boolean can(Player perso) {
                return Fighter.IsVisibleFor(perso);
            }
        });

        Fighter.UsedMP += Path.MovementLength;
        this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, Fighter.ID, Fighter.ID, (short) -Path.MovementLength));

        Fighter.SetCell(this.GetCell(Path.EndCell()));
        this.EndSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
        return GameMovement;

    }

    public byte FindPlacementDirection(Fighter fighter) {
        if (this.FightState != FightState.STATE_PLACE) {
            throw new Error("State != Placement, cannot give placement direction");
        }
        FightTeam fightTeam = fighter.Team == this.myTeam1 ? this.myTeam2 : this.myTeam1;
        Couple<Short, Integer> bestPos = null; //@Param1 = Cellid,@Param2 = Distance
        for (Fighter fightActor : (Iterable<Fighter>) fightTeam.GetFighters()::iterator) {
            MapPoint point = fightActor.MapPoint();
            if (bestPos == null) {
                bestPos = new Couple<>(fightActor.CellId(), fighter.MapPoint().distanceToCell(point));
            } else if (fighter.MapPoint().distanceToCell(point) < bestPos.second) {
                bestPos = new Couple<>(fightActor.CellId(), fighter.MapPoint().distanceToCell(point));
            }
        }
        if (bestPos == null) {
            return fighter.Direction;
        } else {
            return fighter.MapPoint().advancedOrientationTo(MapPoint.fromCellId(bestPos.first), false);
        }
    }

    public void SwapPosition(Fighter Fighter, Fighter FighterTarget) {
        FightCell Cell = Fighter.myCell;
        FightCell Cell2 = FighterTarget.myCell;
        Fighter.SetCell(Cell2);
        FighterTarget.SetCell(Cell);
        this.Fighters().forEach(x -> x.Direction = this.FindPlacementDirection(x));
        this.sendToField(new GameFightPlacementSwapPositionsMessage(new IdentifiedEntityDispositionInformations[]{Fighter.GetIdentifiedEntityDispositionInformations(), FighterTarget.GetIdentifiedEntityDispositionInformations()}));
    }

    public void SetFighterReady(Fighter Fighter) {
        // Si combat deja commencé on arrete
        if (this.FightState != FightState.STATE_PLACE) {
            return;
        }

        Fighter.TurnReady = Fighter.TurnReady == false;

        this.sendToField(new GameFightHumanReadyStateMessage(Fighter.ID, Fighter.TurnReady));

        // Debut du combat si tout le monde ready
        if (this.IsAllTurnReady() && this.FightType != FightTypeEnum.FIGHT_TYPE_PvT) {
            this.StartFight();
        }
    }

    private boolean IsAllTurnReady() {
        return this.AliveFighters().allMatch(Fighter -> Fighter.TurnReady);
    }

    private void SetAllUnReady() {
        this.Fighters().filter(x -> x instanceof CharacterFighter && ((CharacterFighter) x).Character.Client != null).forEach(x -> x.TurnReady = false);
        /*foreach (var Fighter in this.Fighters.Where(Fighter => Fighter is DoubleFighter))
         Fighter.TurnReady = true;*/
    }

    public void SetFighterPlace(Fighter Fighter, short CellId) {
        // Deja pret ?
        if (Fighter.TurnReady) {
            return;
        }

        FightCell Cell = this.myFightCells.get(Fighter.Team).get(CellId);

        // Existante ?
        if (Cell != null) {
            // Aucun persos dessus ?
            if (Cell.CanWalk()) {
                // Affectation
                Fighter.SetCell(Cell);
                this.Fighters().forEach(x -> x.Direction = this.FindPlacementDirection(x));
                this.sendToField(new GameEntitiesDispositionMessage(this.Fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
            }
        }
    }

    protected void InitFight(Fighter Attacker, Fighter Defender) {
        // Les leaders d'equipes
        this.myTeam1.SetLeader(Attacker);
        this.myTeam2.SetLeader(Defender);

        // On despawn avant la vue du flag de combat
        Attacker.JoinFight();
        Defender.JoinFight();

        // Flags de combat
        this.SendFightFlagInfos();

        // Rejoins les combats
        this.JoinFightTeam(Attacker, this.myTeam1, true, (short) -1, true);
        this.JoinFightTeam(Defender, this.myTeam2, true, (short) - 1, true);

        // Si un timer pour le lancement du combat
        if (this.GetStartTimer() != -1) {
            this.StartTimer(new CancellableExecutorRunnable(BackGroundWorker, (GetStartTimer() * 1000)) {
                @Override
                public void run() {
                    try {
                        Thread.sleep(GetStartTimer() * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    StartFight();
                }
            }, "StartTimer");
        }
    }

    public void SendFightFlagInfos(WorldClient Client) {
        if (this.FightState != FightState.STATE_PLACE) {
            return;
        }
        if (this.myTeam1.BladePosition == -1) {
            if (this.myTeam1.Leader.MapCell() != this.myTeam2.Leader.MapCell()) {
                this.myTeam1.BladePosition = this.myTeam1.Leader.MapCell();
                this.myTeam2.BladePosition = this.myTeam2.Leader.MapCell();
            } else {
                this.myTeam1.BladePosition = this.Map.GetRandomAdjacentFreeCell(this.myTeam2.Leader.MapCell()).Id;
                this.myTeam2.BladePosition = this.myTeam2.Leader.MapCell();
            }
        }
        if (Client == null) {
            this.Map.sendToField(new GameRolePlayShowChallengeMessage(GetFightCommonInformations()));
        } else {
            Client.Send(new GameRolePlayShowChallengeMessage(GetFightCommonInformations()));
        }
    }

    public void SendFightFlagInfos() {
        this.SendFightFlagInfos(null);
    }

    public void OnTeamOptionsChanged(FightTeam team, FightOptionsEnum option) {
        this.sendToField(new GameFightOptionStateUpdateMessage(this.FightId, team.Id, option.value, team.IsToggle(option)));
        if (this.FightState == FightState.STATE_PLACE) {
            this.Map.sendToField(new GameFightOptionStateUpdateMessage(this.FightId, team.Id, option.value, team.IsToggle(option)));
        }
    }

    //int fightId, byte fightType, FightTeamInformations[] fightTeams, int[] fightTeamsPositions, FightOptionsInformations[] fightTeamsOption
    public FightCommonInformations GetFightCommonInformations() {
        return new FightCommonInformations(this.FightId, this.FightType.value, new FightTeamInformations[]{this.myTeam1.GetFightTeamInformations(), this.myTeam2.GetFightTeamInformations()}, new int[]{this.myTeam1.BladePosition, this.myTeam2.BladePosition}, new FightOptionsInformations[]{this.myTeam1.GetFightOptionsInformations(), this.myTeam2.GetFightOptionsInformations()});
    }

    public void JoinFightTeam(Fighter Fighter, FightTeam Team, boolean Leader, short cell, boolean sendInfos) {
        if (!Leader) {
            Fighter.JoinFight();
        }

        // Ajout a la team
        Team.FighterJoin(Fighter);

        // On envois l'ajout du joueur a la team sur la map BLADE
        if (this.FightState == FightState.STATE_PLACE) {
            this.Map.sendToField(new GameFightUpdateTeamMessage(this.FightId, Team.GetFightTeamInformations()));
        }

        // Cell de combat
        if (cell == -1) {
            Fighter.SetCell(this.GetFreeSpawnCell(Team));
        } else {
            Fighter.SetCell(this.GetCell(cell));
        }

        if (Fighter instanceof CharacterFighter) {
            this.SendPlacementInformation((CharacterFighter) Fighter, true);
        }

        if (sendInfos) {
            this.sendToField(new FieldNotification(new GameFightShowFighterMessage(Fighter.GetGameContextActorInformations(null))) {
                @Override
                public boolean can(Player perso) {
                    return perso.ID != Fighter.ID;
                }
            });
        }

        // this.sendToField(this.FightTurnListMessage());
    }

    public void SendPlacementInformation(CharacterFighter Fighter, boolean Update) {
        if (Update) {
            Fighter.Send(new GameContextDestroyMessage());
            Fighter.Send(new GameContextCreateMessage((byte) 2));
        }
        Fighter.Send(new GameFightStartingMessage(FightType.value, GetTeam1().LeaderId, GetTeam2().LeaderId));
        //TODO FriendUpdateMessage OnContexteChanged

        this.SendGameFightJoinMessage(Fighter);
        Fighter.Send(new GameFightPlacementPossiblePositionsMessage(this.myFightCells.get(myTeam1).keySet().stream().mapToInt(x -> x.intValue()).toArray(), this.myFightCells.get(myTeam2).keySet().stream().mapToInt(x -> x.intValue()).toArray(), Fighter.Team.Id));

        if (!Update) {
            CharacterHandler.SendCharacterStatsListMessage(Fighter.Character.Client);
        }
        this.Fighters().forEach((Actor) -> {
            Fighter.Send(new GameFightShowFighterMessage(Actor.GetGameContextActorInformations(null)));
        });

        Fighter.Send(new GameEntitiesDispositionMessage(this.Fighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
        Fighter.Send(new GameFightUpdateTeamMessage(this.FightId, this.GetTeam1().GetFightTeamInformations()));
        Fighter.Send(new GameFightUpdateTeamMessage(this.FightId, this.GetTeam2().GetFightTeamInformations()));
        if (Update) {
            this.sendToField(new FieldNotification(new GameFightUpdateTeamMessage(this.FightId, Fighter.Team.GetFightTeamInformations())) {
                @Override
                public boolean can(Player Actor) {
                    return Actor.ID != Fighter.ID;
                }
            });
        }
        this.Fighters().forEach(x -> Fighter.Send(new GameFightHumanReadyStateMessage(x.ID, x.TurnReady)));
    }

    public void onReconnect(CharacterFighter Fighter) {
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 184, new String[]{Fighter.Character.NickName}));

        if (this.FightState == FightState.STATE_PLACE) {
            this.SendPlacementInformation(Fighter, false);
        } else {
            Fighter.Send(new GameFightStartingMessage(FightType.value, GetTeam1().LeaderId, GetTeam2().LeaderId));
            this.SendGameFightJoinMessage(Fighter);
            this.Fighters().forEach((Actor) -> {
                Fighter.Send(new GameFightShowFighterMessage(Actor.GetGameContextActorInformations(Fighter.Character)));
            });
            Fighter.Send(new GameEntitiesDispositionMessage(this.AliveFighters().map(x -> x.GetIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
            Fighter.Send(new GameFightResumeMessage(GetFightDispellableEffectExtendedInformations(), GetAllGameActionMark(), this.myWorker.FightTurn, (int) (System.currentTimeMillis() - this.FightTime), Idols(), Fighter.SpellsController.myinitialCooldown.entrySet().stream().map(x -> new GameFightSpellCooldown(x.getKey(), x.getValue().initialCooldown)).toArray(GameFightSpellCooldown[]::new), (byte) Fighter.Team.GetAliveFighters().filter(x -> x.Summoner() == Fighter.ID && !(x instanceof BombFighter)).count(), (byte) Fighter.Team.GetAliveFighters().filter(x -> x.Summoner() == Fighter.ID && (x instanceof BombFighter)).count()));
            Fighter.Send(FightTurnListMessage());
            Fighter.Send(new GameFightSynchronizeMessage(this.Fighters().map(x -> x.GetGameContextActorInformations(Fighter.Character)).toArray(GameFightFighterInformations[]::new)));

            /*/213.248.126.93 ChallengeInfoMessage Second8 paket
             /213.248.126.93 ChallengeResultMessage Second9 paket*/
            CharacterHandler.SendCharacterStatsListMessage(Fighter.Character.Client);
            if (this.CurrentFighter.ID == Fighter.ID) {
                Fighter.Send(((CharacterFighter) this.CurrentFighter).FighterStatsListMessagePacket());
            }
            /*Fighter.Send(new GameFightUpdateTeamMessage(this.FightId, this.GetTeam1().GetFightTeamInformations()));
             Fighter.Send(new GameFightUpdateTeamMessage(this.FightId, this.GetTeam2().GetFightTeamInformations()));*/

            Fighter.Send(new GameFightNewRoundMessage(this.myWorker.Round));

            Fighter.Send(new GameFightTurnResumeMessage(this.CurrentFighter.ID, this.GetTurnTime() / 100, (int) (this.myLoopTimeOut - System.currentTimeMillis()) / 100));
        }
    }

    public GameActionMark[] GetAllGameActionMark() {
        GameActionMark[] GameActionMarks = new GameActionMark[0];
        for (CopyOnWriteArrayList<FightActivableObject> objs : this.m_activableObjects.values()) {
            for (FightActivableObject Object : objs) {
                GameActionMarks = ArrayUtils.add(GameActionMarks, Object.GetHiddenGameActionMark());
            }
        }
        return GameActionMarks;
    }

    public FightDispellableEffectExtendedInformations[] GetFightDispellableEffectExtendedInformations() {
        FightDispellableEffectExtendedInformations[] FightDispellableEffectExtendedInformations = new FightDispellableEffectExtendedInformations[0];

        for (Stream<BuffEffect> Buffs : (Iterable<Stream<BuffEffect>>) this.AliveFighters().map(x -> x.Buffs.GetAllBuffs())::iterator) {
            for (BuffEffect Buff : (Iterable<BuffEffect>) Buffs::iterator) {
                FightDispellableEffectExtendedInformations = ArrayUtils.add(FightDispellableEffectExtendedInformations, new FightDispellableEffectExtendedInformations(Buff.CastInfos.EffectType.value(), Buff.Caster.ID, Buff.GetAbstractFightDispellableEffect()));
            }
        }
        /*return Stream.of(this.AliveFighters()
         .map(x -> x.Buffs.GetAllBuffs()
         .map(Buff -> (new FightDispellableEffectExtendedInformations(Buff.CastInfos.EffectType.value(), Buff.Caster.ID, Buff.GetAbstractFightDispellableEffect())))
         )).toArray(FightDispellableEffectExtendedInformations[]::new);*/
        return FightDispellableEffectExtendedInformations;
    }

    public byte SummonCount() {
        return 0;
    }

    public byte BombCount() {
        return 0;
    }

    public Idol[] Idols() {
        return new Idol[0];
    }

    protected abstract void SendGameFightJoinMessage(Fighter fighter);

    public boolean OnlyOneTeam() {
        boolean Team1 = this.myTeam1.GetFighters().anyMatch(Player -> !Player.Dead && (!Player.Left));
        boolean Team2 = this.myTeam2.GetFighters().anyMatch(Player -> !Player.Dead && (!Player.Left));
        /*for (Fighter Player : Fighters()) {
         if ((Player.Team.Id == 0) && (!Player.Dead) && (!Player.Left)) {
         Team1 = true;
         }
         if ((Player.Team.Id == 1) && (!Player.Dead) && (!Player.Left)) {
         Team2 = true;
         }
         }*/
        return !(Team1 && Team2);
    }

    public synchronized boolean TryEndFight() {
        if (this.GetWinners() != null) {
            this.FightLoopState = FightLoopState.STATE_WAIT_END;
            return true;
        }
        return false;
    }

    private SequenceTypeEnum m_lastSequenceAction;
    private int m_sequenceLevel;
    public boolean IsSequencing;
    public boolean WaitAcknowledgment;
    public SequenceTypeEnum Sequence;
    private Stack<SequenceTypeEnum> m_sequences = new Stack<>();
    private AtomicInteger contextualIdProvider = new AtomicInteger();

    public boolean StartSequence(SequenceTypeEnum sequenceType) {
        this.m_lastSequenceAction = sequenceType;
        ++this.m_sequenceLevel;
        if (this.IsSequencing) {
            return false;
        }
        this.IsSequencing = true;
        this.Sequence = sequenceType;
        this.m_sequences.push(sequenceType);
        this.sendToField(new SequenceStartMessage(sequenceType.value, this.CurrentFighter.ID)); //TODO not Spectator?
        return true;
    }

    public int GetNextContextualId() {
        int id = contextualIdProvider.decrementAndGet();
        while (AnyFighterMatchId(id)) {
            id = contextualIdProvider.decrementAndGet();
        }
        return id;
    }

    public boolean AnyFighterMatchId(final int id) {
        return this.Fighters().anyMatch(Fighter -> Fighter.ID == id);
    }

    public boolean isSequence(SequenceTypeEnum sequenceType) {
        return this.m_sequences.contains(sequenceType);
    }

    public boolean EndSequence(SequenceTypeEnum sequenceType) {
        return EndSequence(sequenceType, false);
    }

    public boolean EndSequence(SequenceTypeEnum sequenceType, boolean force) {
        if (!this.IsSequencing) {
            return false;
        }
        --this.m_sequenceLevel;
        if (this.m_sequenceLevel > 0 && !force) {
            return false;
        }
        this.IsSequencing = false;
        this.WaitAcknowledgment = true;
        SequenceTypeEnum sequenceTypeEnum = this.m_sequences.pop();
        if (sequenceTypeEnum != sequenceType) {
            Main.Logs().writeDebug(String.format("Popped Sequence different ({0} != {1})", sequenceTypeEnum.value, sequenceType.value));
        }
        this.sendToField(new SequenceEndMessage(this.m_lastSequenceAction.value, this.CurrentFighter.ID, sequenceType.value));
        return true;
    }

    public void EndAllSequences() {
        this.m_sequenceLevel = 0;
        this.IsSequencing = false;
        this.WaitAcknowledgment = false;
        while (this.m_sequences.size() > 0) {
            this.sendToField(new SequenceEndMessage(this.m_lastSequenceAction.value, this.CurrentFighter.ID, this.m_sequences.pop().value));
        }
    }

    public void AcknowledgeAction() {
        this.WaitAcknowledgment = false;
    }

    protected synchronized void EndFight() {
        switch (this.FightState) {
            case STATE_PLACE:
                this.Map.sendToField(new GameRolePlayRemoveChallengeMessage(this.FightId));
                break;
            case STATE_FINISH:
                return;
        }

        this.StopTimer("GameLoop");

        this.sendToField(this.myResult);

        this.CreationTime = 0;
        this.FightTime = 0;
        this.EndAllSequences();
        this.Fighters().forEach(x -> x.EndFight());

        this.KickSpectators(true);

        this.myCells.values().stream().forEach((c) -> {
            c.Clear();
        });
        this.myCells.clear();
        this.myTimers.clear();
        this.myWorker.Dispose();
        this.myTeam1.Dispose();
        this.myTeam2.Dispose();

        this.myFightCells = null;
        this.myCells = null;
        this.myTeam1 = null;
        this.myTeam2 = null;
        this.myWorker = null;
        this.m_activableObjects.values().forEach(x -> x.clear());
        this.m_activableObjects.clear();
        this.m_activableObjects = null;
        this.myTimers = null;
        this.m_sequences.clear();
        this.m_sequences = null;
        this.contextualIdProvider = null;

        //this.Glyphes = null;
        this.Map.RemoveFight(this);
        this.FightState = FightState.STATE_FINISH;
        this.FightLoopState = FightLoopState.STATE_END_FIGHT;
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

    public abstract GameFightEndMessage LeftEndMessage(Fighter fighter);

    protected boolean IsStarted() {
        return this.FightState != FightState.STATE_INIT && this.FightState != FightState.STATE_PLACE;
    }

    private void KickSpectators(boolean End) {

    }

    public Fighter GetFighterOnCell(int cellId) {
        return this.AliveFighters().filter(Fighter -> Fighter.CellId() == cellId).findFirst().orElse(null);
    }

    public FightTeam GetWinners() {
        if (!this.myTeam1.HasFighterAlive()) {
            return this.myTeam2;
        } else if (!this.myTeam2.HasFighterAlive()) {
            return this.myTeam1;
        }

        return null;
    }

    public FightTeam GetLoosers() {
        return this.GetEnnemyTeam(this.GetWinners());
    }

    /// <summary>
    ///  Init des tours
    /// </summary>
    /*public void RemakeTurns() {
     this.myWorker.RemakeTurns(this.Fighters());
     }*/
    public synchronized void AddNamedParty(Fighter Fighter, int Outcome) {
        if (Fighter instanceof CharacterFighter) {
            if (((CharacterFighter) Fighter).Character.Client != null && ((CharacterFighter) Fighter).Character.Client.GetParty() != null && !((CharacterFighter) Fighter).Character.Client.GetParty().PartyName.isEmpty() && !Arrays.stream(this.myResult.namedPartyTeamsOutcomes).anyMatch(x -> x.team.partyName.equalsIgnoreCase(((CharacterFighter) Fighter).Character.Client.GetParty().PartyName))) {
                this.myResult.namedPartyTeamsOutcomes = ArrayUtils.add(this.myResult.namedPartyTeamsOutcomes, new NamedPartyTeamWithOutcome(new NamedPartyTeam(Fighter.Team.Id, ((CharacterFighter) Fighter).Character.Client.GetParty().PartyName), Outcome));
            }
        }
    }

    public void AddActivableObject(Fighter caster, FightActivableObject obj) {
        if (!m_activableObjects.containsKey(caster)) {
            m_activableObjects.put(caster, new CopyOnWriteArrayList<>());
        }
        m_activableObjects.get(caster).add(obj);
    }

    public Stream<Fighter> Fighters() {
        return Stream.concat(this.myTeam1.GetFighters(), this.myTeam2.GetFighters());
    }

    public Stream<Fighter> AliveFighters() {
        return Stream.concat(this.myTeam1.GetAliveFighters(), this.myTeam2.GetAliveFighters());
    }

    public Stream<Fighter> DeadFighters() {
        //Need Performance Test return Stream.of(this.myTeam1.GetDeadFighters(), this.myTeam2.GetDeadFighters()).flatMap(x -> x);
        return Stream.concat(this.myTeam1.GetDeadFighters(), this.myTeam2.GetDeadFighters());
    }

    public FightTeam GetTeam1() { //Red
        return myTeam1;
    }

    public FightTeam GetTeam2() {
        return myTeam2;
    }

    public Fighter GetFighter(int FighterId) {
        return this.Fighters().filter(x -> x.ID == FighterId).findFirst().orElse(null);
    }

    public boolean HasObjectOnCell(FightObjectType type, short cell) {
        if (!this.myCells.containsKey(cell)) {
            return false;
        }
        return this.myCells.get(cell).HasObject(type);
    }

    public boolean CanPutObject(short cellId) {
        if (!this.myCells.containsKey(cellId)) {
            return false;
        }
        return this.myCells.get(cellId).CanPutObject();
    }

    public boolean IsCellWalkable(short CellId) {
        if (!this.myCells.containsKey(CellId)) {
            return false;
        }

        return this.myCells.get(CellId).CanWalk();
    }

    public FightCell GetCell(short CellId) {
        if (this.myCells.containsKey(CellId)) {
            return this.myCells.get(CellId);
        }
        return null;
    }

    public Fighter HasEnnemyInCell(short CellId, FightTeam Team) {
        if (CellId == -1) {
            return null;
        }
        return this.myCells.get(CellId).HasEnnemy(Team);
    }

    public Fighter HasFriendInCell(short CellId, FightTeam Team) {
        if (CellId == -1) {
            return null;
        }
        return this.myCells.get(CellId).HasFriend(Team);
    }

    public GameFightTurnListMessage FightTurnListMessage() {
        return new GameFightTurnListMessage(this.AliveFighters().filter(x -> !(x instanceof StaticFighter)).sorted((e1, e2) -> Integer.compare(e2.Initiative(false), e1.Initiative(false))).mapToInt(x -> x.ID).toArray(), this.DeadFighters().filter(x -> !(x instanceof StaticFighter)).mapToInt(x -> x.ID).toArray());
    }

    private synchronized FightCell GetFreeSpawnCell(FightTeam Team) {
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
    public void ActorMoved(Path Path, IGameActor Actor, short newCell, byte newDirection) {
        //((Fighter) Actor).SetCell(myCells.get(newCell));
        Actor.Direction = newDirection;
    }

    public void StopTimer(String Name) {
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

    public void StartTimer(CancellableExecutorRunnable CR, String Name) {
        synchronized ($mutex_lock) {
            try {
                this.myTimers.put(Name, CR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int GetPlacementTimeLeft() {
        if (this.IsStarted()) {
            return 0;
        }
        double num = (double) (this.GetStartTimer() - (Instant.now().getEpochSecond() - this.CreationTime)) * 10;
        if (num < 0.0) {
            num = 0.0;
        }
        return (int) num;
    }

}
