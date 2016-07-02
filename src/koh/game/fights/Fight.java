package koh.game.fights;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFightSpectator;
import koh.game.actions.GameMapMovement;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.*;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.maps.pathfinding.*;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffAddSpellRange;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffEndTurn;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.fighters.*;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightPortal;
import koh.game.fights.layers.FightTrap;
import koh.game.fights.utils.Algo;
import koh.game.network.WorldClient;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.game.utils.Three;
import koh.protocol.client.Message;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.actions.SequenceEndMessage;
import koh.protocol.messages.game.actions.SequenceStartMessage;
import koh.protocol.messages.game.actions.fight.*;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.*;
import koh.protocol.messages.game.context.fight.*;
import koh.protocol.messages.game.context.fight.character.GameFightShowFighterMessage;
import koh.protocol.messages.game.context.roleplay.CurrentMapMessage;
import koh.protocol.messages.game.context.roleplay.fight.GameRolePlayRemoveChallengeMessage;
import koh.protocol.messages.game.context.roleplay.fight.GameRolePlayShowChallengeMessage;
import koh.protocol.types.game.action.fight.FightDispellableEffectExtendedInformations;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.context.IdentifiedEntityDispositionInformations;
import koh.protocol.types.game.context.fight.*;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeam;
import koh.protocol.types.game.context.roleplay.party.NamedPartyTeamWithOutcome;
import koh.protocol.types.game.data.items.effects.ObjectEffectDice;
import koh.protocol.types.game.idol.Idol;
import koh.utils.Couple;
import koh.utils.Enumerable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static koh.protocol.client.enums.StatsEnum.*;

/**
 * @author Neo-Craft
 */
public abstract class Fight extends IWorldEventObserver implements IWorldField {

    //public static final ImprovedCachedThreadPool BackGroundWorker2 = new ImprovedCachedThreadPool(5, 50, 2);
    public static final ScheduledExecutorService BACK_GROUND_WORKER = Executors.newScheduledThreadPool(50);
    public static final Random RANDOM = new Random();
    public static final StatsEnum[] EFFECT_NOT_SILENCED = new StatsEnum[]{
            StatsEnum.DAMAGE_NEUTRAL, StatsEnum.DAMAGE_EARTH, StatsEnum.DAMAGE_AIR, StatsEnum.DAMAGE_FIRE, StatsEnum.DAMAGE_WATER,
            StatsEnum.STEAL_NEUTRAL, StatsEnum.STEAL_EARTH, StatsEnum.STEAL_AIR, StatsEnum.STEAL_FIRE, StatsEnum.STEAL_WATER, StatsEnum.STEAL_PV_FIX,
            StatsEnum.DAMAGE_LIFE_NEUTRE, StatsEnum.DAMAGE_LIFE_WATER, StatsEnum.DAMAGE_LIFE_TERRE, StatsEnum.DAMAGE_LIFE_AIR, StatsEnum.DAMAGE_LIFE_FEU, StatsEnum.DAMAGE_DROP_LIFE
    };
    @Getter
    protected static final Logger logger = LogManager.getLogger(Fight.class);
    private static final HashMap<Integer, HashMap<Integer, Short[]>> MAP_FIGHTCELLS = new HashMap<>();
    private final Object $mutex_lock = new Object();
    public volatile Boolean hasFinished = false;
    public boolean isSequencing;
    public boolean waitAcknowledgment;
    public SequenceTypeEnum sequence;
    protected FightTeam myTeam1 = new FightTeam((byte) 0, this);
    protected FightTeam myTeam2 = new FightTeam((byte) 1, this);
    protected ArrayList<WorldClient> mySpectators = new ArrayList<WorldClient>();
    protected long myLoopTimeOut = -1;
    protected long myLoopActionTimeOut;
    protected ArrayList<GameAction> myActions = new ArrayList<>();
    protected volatile GameFightEndMessage myResult;
    @Getter
    @Setter
    protected short fightId;
    @Getter
    protected FightState fightState;
    @Getter
    @Setter
    protected FightLoopState fightLoopState;
    @Getter
    protected DofusMap map;
    @Getter
    protected Fighter currentFighter;
    @Getter
    protected long fightTime, creationTime;
    @Getter
    protected FightTypeEnum fightType;
    @Getter
    protected Map<Short, FightCell> fightCells = new HashMap<>();
    protected Map<FightTeam, Map<Short, FightCell>> myFightCells = new HashMap<>();
    protected short ageBonus = 0, lootShareLimitMalus = -1;
    protected Map<String, CancellableScheduledRunnable> myTimers = new HashMap<>();
    @Getter
    protected Map<Fighter, CopyOnWriteArrayList<FightActivableObject>> activableObjects = Collections.synchronizedMap(new HashMap<>());
    @Getter
    protected FightWorker fightWorker = new FightWorker(this);
    @Getter
    protected AtomicInteger nextTriggerUid = new AtomicInteger();
    private SequenceTypeEnum m_lastSequenceAction;
    private int m_sequenceLevel;
    private Stack<SequenceTypeEnum> m_sequences = new Stack<>();
    private AtomicInteger contextualIdProvider = new AtomicInteger(-2);

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

    public FightTeam getEnnemyTeam(FightTeam team) {
        return (team == this.myTeam1 ? this.myTeam2 : this.myTeam1);
    }

    public FightTeam getAllyTeam(FightTeam team) {
        return (team == this.myTeam1 ? this.myTeam1 : this.myTeam2);
    }

    public FightTeam getTeam(int LeaderId) {
        return (this.myTeam1.leaderId == LeaderId ? this.myTeam1 : this.myTeam2);
    }

    public abstract void leaveFight(Fighter Fighter);
    //TODO ActionIdConverter.ACTION_FIGHT_DISABLE_PORTAL

    public abstract void endFight(FightTeam Winners, FightTeam Loosers);

    public abstract int getStartTimer();

    public abstract int getTurnTime();

    private void initCells() {
        // Ajout des cells
        for (DofusCell cell : this.map.getCells()) {
            this.fightCells.put(cell.getId(), new FightCell(cell.getId(), cell.walakableInFight(), cell.los()));
        }
        this.myFightCells.put(myTeam1, new HashMap<>());
        this.myFightCells.put(myTeam2, new HashMap<>());

        if (Fight.MAP_FIGHTCELLS.containsKey(this.map.getId())) {
            // Ajout
            synchronized (Fight.MAP_FIGHTCELLS) {
                for (Short cell : Fight.MAP_FIGHTCELLS.get(this.map.getId()).get(0)) {
                    this.myFightCells.get(this.myTeam1).put(cell, this.fightCells.get(cell));
                }
                for (Short cell : Fight.MAP_FIGHTCELLS.get(this.map.getId()).get(1)) {
                    this.myFightCells.get(this.myTeam2).put(cell, this.fightCells.get(cell));
                }
            }
            return;
        }

        for (Short cellValue : this.map.getRedCells()) {
            final FightCell cell = this.fightCells.get(cellValue);
            if (cell == null || !cell.canWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam1).put(cellValue, cell);
        }

        for (Short cellValue : this.map.getBlueCells()) {
            final FightCell cell = this.fightCells.get(cellValue);
            if (cell == null || !cell.canWalk()) {
                continue;
            }
            this.myFightCells.get(this.myTeam2).put(cellValue, cell);
        }

        if (this.map.getBlueCells().length == 0 || this.map.getRedCells().length == 0) {
            this.myFightCells.get(this.myTeam1).clear();
            this.myFightCells.get(this.myTeam2).clear();
            final Couple<ArrayList<FightCell>, ArrayList<FightCell>> startCells = Algo.genRandomFightPlaces(this);
            for (FightCell cell : startCells.first) {
                this.myFightCells.get(this.myTeam1).put(cell.Id, cell);
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

    public void disconnect(CharacterFighter fighter) {
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 182, new String[]{fighter.getCharacter().getNickName(), Integer.toString(fighter.getTurnRunning())}));
        if (this.currentFighter.getID() == fighter.getID()) {
            this.fightLoopState = fightLoopState.STATE_END_TURN;
        }
        fighter.setTurnReady(true);
    }

    public void launchSpell(Fighter fighter, SpellLevel spelllevel, short cellId, boolean friend) {
        launchSpell(fighter, spelllevel, cellId, friend, false, true, -1);
    }


    public boolean pointLos(int x, int y, boolean bAllowTroughEntity) {
        final FightCell cell = this.getCell(MapPoint.fromCoords(x, y).get_cellId());
        boolean los = cell.isLineOfSight();
        if (!(bAllowTroughEntity)) {
            if (!cell.canGoTrough())
                return false;
        }
        ;
        return (los);
    }

    private static final int TOLERANCE_ELEVATION = 11;

    public boolean pointMov(int x, int y, boolean bAllowTroughEntity, int previousCellId, int endCellId) {
        boolean useNewSystem;
        short cellId;
        DofusCell cellData, previousCellData;
        boolean mov;
        int dif;
        if (MapPoint.isInMap(x, y)) {
            useNewSystem = map.isUsingNewMovementSystem();
            cellId = MapPoint.fromCoords(x, y).get_cellId();
            cellData = map.getCell(cellId);
            mov = ((cellData.mov()) && (!cellData.nonWalkableDuringFight()));
            if (((((((mov) && (useNewSystem))) && (!((previousCellId == -1))))) && (!((previousCellId == cellId))))) {
                previousCellData = map.getCell((short) previousCellId);
                dif = Math.abs((Math.abs(cellData.getFloor()) - Math.abs(previousCellData.getFloor())));
                if (((((!((previousCellData.getMoveZone() == cellData.getMoveZone()))) && ((dif > 0)))) || ((((((previousCellData.getMoveZone() == cellData.getMoveZone())) && ((cellData.getMoveZone() == 0)))) && ((dif > TOLERANCE_ELEVATION)))))) {
                    mov = false;
                }
            }
            if (!(bAllowTroughEntity)) {
                for (IFightObject o : this.getCell(cellId).getObjects()) {
                    if ((((endCellId == cellId)) && (o.canWalk()))) {

                    } else {
                        if (!(o.canGoThrough())) {
                            return (false);
                        }
                    }
                }
            }
        } else {
            mov = false;
        }
        return (mov);
    }

    public double pointWeight(int x, int y) {
        return pointWeight(x, y, true);
    }

    public double pointWeight(int x, int y, boolean bAllowTroughEntity) {
        IFightObject entity;
        double weight = 1;
        int speed = this.getMap().getCellSpeed(MapPoint.fromCoords(x, y).get_cellId());
        if (bAllowTroughEntity) {
            if (speed >= 0) {
                weight = (weight + (5 - speed));
            } else {
                weight = (weight + (11 + Math.abs(speed)));
            }
            entity = this.getCell(MapTools.getCellNumFromXYCoordinates(x, y)).getFighter();
            if (((entity != null) && (!(entity.canGoThrough())))) {
                weight = 20;
            }
        } else {
            if (this.getCell(MapTools.getCellNumFromXYCoordinates(x, y)).getFighter() != null) {
                weight = (weight + 0.3);
            }

            if (this.getCell(MapTools.getCellNumFromXYCoordinates((x + 1), y)).getFighter() != null) {
                weight = (weight + 0.3);
            }

            if (this.getCell(MapTools.getCellNumFromXYCoordinates(x, (y + 1))).getFighter() != null) {
                weight = (weight + 0.3);
            }

            if (this.getCell(MapTools.getCellNumFromXYCoordinates((x - 1), y)).getFighter() != null) {
                weight = (weight + 0.3);
            }

            if (this.getCell(MapTools.getCellNumFromXYCoordinates(x, (y - 1))).getFighter() != null) {
                weight = (weight + 0.3);
            }

            if (this.getCell(MapTools.getCellNumFromXYCoordinates(x, y)).hasGameObject(FightObjectType.OBJECT_TRAP)) { //orglyph
                weight = (weight + 0.2);
            }
            /*if ((this.pointSpecialEffects(x, y) & 2) == 2)
            {
                weight = (weight + 0.2);
            };*/
        }
        return (weight);
    }


    public boolean hasEntity(int x, int y) {
        final short cell = (short) Math.abs(MapPoint.coordToCellId(x, y));
        try {
            for (IFightObject o : this.getCell(cell).getObjects()) {
                if (!o.canGoThrough()) {
                    return true;
                }
            }
            return !this.map.getCell(cell).los() && !this.map.getCell(cell).farmCell();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
        }
        return false;
    }

    private Three<Integer, int[], Integer> getTargetThroughPortal(Fighter Fighter, int param1) {
        return getTargetThroughPortal(Fighter, param1, false);
    }

    private Three<Integer, int[], Integer> getTargetThroughPortal(Fighter fighter, int param1, boolean param2) {
        MapPoint _loc3_ = null;
        int damagetoReturn = 0;
        MapPoint _loc16_;
        FightPortal[] portals = new FightPortal[0];
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).enabled) {
                    portals = ArrayUtils.add(portals, (FightPortal) Object);
                    if (Object.getCellId() == param1) {
                        _loc3_ = Object.getMapPoint();
                    }
                }
            }
        }
        if (portals.length < 2) {
            return new Three<>(param1, new int[0], 0);
        }
        if (_loc3_ == null) {
            return new Three<>(param1, new int[0], 0);
        }
        final int[] _loc10_ = LinkedCellsManager.getLinks(_loc3_, Arrays.stream(portals)/*.filter(x -> x.caster.team == Fighter.team)*/.map(x -> x.getMapPoint()).toArray(MapPoint[]::new));
        final MapPoint _loc11_ = MapPoint.fromCellId(_loc10_[/*_loc10_.length == 0 ? 0 :*/_loc10_.length - 1]);
        final MapPoint _loc12_ = MapPoint.fromCellId(fighter.getCellId());
        if (_loc12_ == null) {
            return new Three<>(param1, new int[0], 0);
        }
        final int _loc13_ = _loc3_.get_x() - _loc12_.get_x() + _loc11_.get_x();
        final int _loc14_ = _loc3_.get_y() - _loc12_.get_y() + _loc11_.get_y();
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
        final int[] portailIds = new int[_loc10_.length];
        FightPortal portal;
        for (int i = 0; i < _loc10_.length; i++) {
            final int ID = _loc10_[i];
            portal = Arrays.stream(portals).filter(y -> y.getCellId() == ID).findFirst().get();
            if (fighter.getTeam() == portal.caster.getTeam()) {
                damagetoReturn += portal.damageValue;
            }
            portailIds[i] = portal.ID;
        }
        return new Three<>((int) _loc16_.get_cellId(), portailIds, damagetoReturn);
    }

    private static final int[] BLACKLISTED_EFFECTS = DAO.getSettings().getIntArray("Effect.BlacklistedByTriggers");
    private static final StatsEnum[] FIRST_EFFECTS = new StatsEnum[]{
            StatsEnum.DISPELL_SPELL,
            StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION2,
            //StatsEnum.CAST_SPELL,
            KILL_TARGET_TO_REPLACE_INVOCATION,
            StatsEnum.KILL,
            StatsEnum.SUMMON,
            StatsEnum.DESENVOUTEMENT,


    };

    public void launchSpell(Fighter fighter, SpellLevel spellLevel, short cellId, boolean friend, boolean fakeLaunch, boolean imTargeted, int spellId) {
        synchronized (fighter.getMutex()) {
            try {
                if (this.fightState != fightState.STATE_ACTIVE) {
                    return;
                }
                if (this.fightLoopState == fightLoopState.STATE_WAIT_READY) {
                    return;
                }
                //System.out.println(spellLevel);
                final short oldCell = cellId;
                if (spellLevel.getSpellId() == 0 && fighter.isPlayer() && fighter.getPlayer().getInventoryCache().hasItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON)) {
                    this.launchWeapon(fighter.asPlayer(), cellId);
                    return;
                }

                boolean isCc = false;
                if (spellLevel.getCriticalHitProbability() != 0 && spellLevel.getCriticalEffect().length > 0) {
                    final int tauxCC = fighter.getStats().getTotal(StatsEnum.ADD_CRITICAL_HIT) + spellLevel.getCriticalHitProbability();
                    //logger.debug("CC: {} TauxCC {} getSpellLevel.criticalHitProbability {} stat {} {} ", isCc, tauxCC, spellLevel.getCriticalHitProbability(), fighter.getStats().getTotal(StatsEnum.ADD_CRITICAL_HIT));

                    if (tauxCC > (Fight.RANDOM.nextDouble() * 100)) {
                        isCc = true;
                    }
                }

                EffectInstanceDice[] spellEffects = isCc || spellLevel.getEffects() == null ? spellLevel.getCriticalEffect() : spellLevel.getEffects();

                Three<Integer, int[], Integer> informations = null;
                if (!fakeLaunch) {

                    if (this.getCell(cellId).hasGameObject(FightObjectType.OBJECT_PORTAL)
                            && !Arrays.stream(spellEffects).anyMatch(effect -> effect.getEffectType().equals(StatsEnum.DISABLE_PORTAL))) {
                        informations = this.getTargetThroughPortal(fighter, cellId, true);
                        cellId = informations.first.shortValue();
                        //this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"DamagePercentBoosted Suite au portails = " + informations.tree}));

                    }
                }

                // La cible si elle existe
                Fighter targetE = this.hasEnnemyInCell(cellId, fighter.getTeam());
                if (friend && targetE == null) {
                    targetE = this.hasFriendInCell(cellId, fighter.getTeam());
                }

                final int targetId = targetE == null ? -1 : targetE.getID();

                // Peut lancer le sort ?
                if (!fakeLaunch && !this.canLaunchSpell(fighter, spellLevel, fighter.getCellId(), oldCell, targetId)) {
                    fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
                    this.startSequence(SequenceTypeEnum.SEQUENCE_SPELL);
                    fighter.send(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, fighter.getID(), fighter.getID(), (short) 0));
                    this.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
                    return;
                }


                if (!fakeLaunch) {
                    this.startSequence(SequenceTypeEnum.SEQUENCE_SPELL);
                    fighter.setUsedAP(fighter.getUsedAP() + spellLevel.getApCost());
                }
                fighter.getSpellsController().actualize(spellLevel, targetId);

                isCc &= !fighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);
                if (isCc && !fakeLaunch && fighter.getStats().getTotal(CAST_SPELL_ON_CRITICAL_HIT) > 0) { //Turquoise
                    fighter.getPlayer().getInventoryCache().getEffects(CAST_SPELL_ON_CRITICAL_HIT.value()).forEach(list -> {
                        list.forEach(effect -> {
                            launchSpell(fighter, DAO.getSpells().findSpell(effect.diceNum).getSpellLevel(effect.diceSide), fighter.getCellId(), true, true, true, -1);
                        });
                    });
                }


                final int maxGroup = Arrays.stream(spellEffects).mapToInt(ef -> ef.group).max().orElse(0);
                if (maxGroup > 0) {
                    final EffectInstanceDice[] effectsUnique = Arrays.stream(spellEffects) //Rekop have a effect without group...
                            .map(ee -> ee.effectId)
                            .distinct()
                            .filter(id -> (Arrays.stream((spellLevel.getEffects() != null ? spellLevel.getEffects() : spellLevel.getCriticalEffect()))
                                    .filter(eee -> (id == eee.effectId)).count() == 1)
                            )
                            .map(e -> Arrays.stream(spellLevel.getEffects() != null ? spellLevel.getEffects() : spellLevel.getCriticalEffect()).filter(ee -> ee.effectId == e).findFirst().get())
                            .toArray(EffectInstanceDice[]::new);

                    Arrays.stream(spellEffects).forEach(e -> e.random = 0); //TODO check 3-4 monsters group spells
                    final int randGroup = spellId == 114 ? RANDOM.nextInt(maxGroup) + 1 : RANDOM.nextInt(maxGroup + 1);
                    spellEffects = Arrays.stream(spellEffects).filter(ef -> ef.group == randGroup).toArray(EffectInstanceDice[]::new);

                    while (spellEffects.length == 0) {
                        final int gr = RANDOM.nextInt(maxGroup);
                        spellEffects = Arrays.stream(isCc || spellLevel.getEffects() == null ? spellLevel.getCriticalEffect() : spellLevel.getEffects()).filter(ef -> ef.group == gr).toArray(EffectInstanceDice[]::new);
                    }
                    Arrays.stream(spellEffects).forEach(x -> x.targetMask = "a,A");

                    for (EffectInstanceDice effect : effectsUnique) {
                        spellEffects = ArrayUtils.add(spellEffects, effect);
                    }

                    //TODO: Ecaflip rekop make all c targetMask in db , ankama mistake ...
                }

                final boolean silentCast = Arrays.stream(spellEffects).allMatch(x -> !ArrayUtils.contains(EFFECT_NOT_SILENCED, x.getEffectType()));

                if (!fakeLaunch) {
                    for (Player player : this.observable$Stream()) {
                        player.send(new GameActionFightSpellCastMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, fighter.getID(), targetId, (spellLevel.getSpellId() == 2763 ? true : (!fighter.isVisibleFor(player) && silentCast)) ? 0 : cellId, (byte) (isCc ? 2 : 1), spellLevel.getSpellId() == 2763 ? true : (!fighter.isVisibleFor(player) || silentCast), spellLevel.getSpellId(), spellLevel.getGrade(), informations == null ? new int[0] : informations.second));
                    }
                }

                final HashMap<EffectInstanceDice, ArrayList<Fighter>> targets = new HashMap<>();
                for (EffectInstanceDice effect : spellEffects) {
                    logger.debug(effect.toString());
                    targets.put(effect, new ArrayList<>());
                    final Fighter[] targetsOnZone = Arrays.stream((new Zone(effect.getZoneShape(), effect.zoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(informations != null ? oldCell : cellId), true), this.map))
                            .getCells(cellId))
                            .map(cell -> this.getCell(cell))
                            .filter(cell -> cell != null && cell.hasGameObject(FightObjectType.OBJECT_FIGHTER, FightObjectType.OBJECT_STATIC))
                            .map(fightCell -> fightCell.getObjectsAsFighter()[0])
                            .toArray(Fighter[]::new);

            /* Explanation about the bottom code
               When a fighter cast spell in a cell where he is not on it .
               Like Mot drainant , Corruption.. Some spell effect need to be applied on himself
             */
                    if ((effect.targetMask.equals("C") || (effect.targetMask.startsWith("C,") && effect.targetMask.split(",").length == 2))
                            && effect.zoneShape() == 80
                            && effect.zoneSize() == 1
                            && Arrays.stream(targetsOnZone).noneMatch(fr -> fr.getID() == fighter.getID())) {
                        if (effect.isValidTarget(fighter, fighter) && (EffectInstanceDice.verifySpellEffectMask(fighter, fighter, effect, cellId))) {
                            targets.get(effect).add(fighter);
                        }
                    } else if (effect.targetMask.equalsIgnoreCase("A,K") && fighter.getCarriedActor() != null) { //Vertigo
                        targets.get(effect).add(fighter.getCarriedActor());
                    }

                    for (Fighter target : targetsOnZone) {
                        logger.debug("EffectId {} {} target {} Triger {} validTarget {} spellMask {}", effect.effectId, StatsEnum.valueOf(effect.effectId), target.getID(), EffectHelper.verifyEffectTrigger(fighter, target, spellEffects, effect, false, effect.triggers, cellId, true), effect.isValidTarget(fighter, target), EffectInstanceDice.verifySpellEffectMask(fighter, target, effect, cellId));
                        if ((ArrayUtils.contains(BLACKLISTED_EFFECTS, effect.effectUid)
                                || EffectHelper.verifyEffectTrigger(fighter, target, spellEffects, effect, false, effect.triggers, cellId, true))
                                && effect.isValidTarget(fighter, target)
                                && EffectInstanceDice.verifySpellEffectMask(fighter, target, effect, cellId)) {
                            if ((effect.targetMask.equals("C") && fighter.getCarrierActorId() == target.getID())
                                    || (effect.targetMask.equals("a,A") && fighter.getCarrierActorId() != 0 & fighter.getID() == target.getID())
                                    || (!imTargeted && target.getID() == fighter.getID())) {
                                continue;
                            }
                                /*if (Fighter instanceof BombFighter && target.states.hasState(FightStateEnum.Kaboom)) {
                                 continue;
                                 }*/
                            logger.debug("Targeet Added!");
                            targets.get(effect).add(target);
                        } else if (effect.effectId == 1160) {
                            logger.debug("Contained {} ", Enumerable.join(Arrays.stream(DAO.getSpells().findSpell(effect.diceNum).getSpellLevel(effect.diceSide).getEffects()).map(e -> e.getEffectType().toString()).toArray(String[]::new)));
                        }
                    }
                }
                double num1 = Fight.RANDOM.nextDouble();
                final double num2 = (double) Arrays.stream(spellEffects).mapToInt(x -> x.random).sum();
                boolean flag = false;
                final short castingCell = fighter.getCellId();
                for (final Iterator<EffectInstanceDice> effectIterator = ((Arrays.stream(spellEffects).sorted((e1, e2) -> Integer.compare(ArrayUtils.indexOf(FIRST_EFFECTS, e2.getEffectType()), ArrayUtils.indexOf(FIRST_EFFECTS, e1.getEffectType())))).iterator()); effectIterator.hasNext(); ) {
                    final EffectInstanceDice effect = effectIterator.next();
                    if (effect.random > 0) {
                        if (!flag) {
                            if (num1 > (double) effect.random / num2) {
                                num1 -= (double) effect.random / num2;
                                continue;
                            } else {
                                flag = true;
                            }
                        } else {
                            continue;
                        }
                    }
                    // Actualisation des morts
                    targets.get(effect).removeIf(fr -> fr.isDead());
                    if (effect.getEffectType() == ADD_BASE_DAMAGE_SPELL) {
                        targets.get(effect).clear();
                        targets.get(effect).add(fighter);
                    }
                    if (effect.getEffectType() == CAST_SPELL && effect.diceNum == 5574 && getCell(cellId).getFighter() != null) {
                        targets.get(effect).add(getCell(cellId).getFighter());
                        effect.delay++;
                    }
                    if (effect.delay > 0) {
                        //TODO: Set ParentBoost UID
                        fighter.getBuff().delayedEffects.add(new Couple<>(new EffectCast(effect.getEffectType(), spellId == -1 ? spellLevel.getSpellId() : spellId, cellId, num1, effect, fighter, targets.get(effect), false, StatsEnum.NONE, 0, spellLevel), effect.delay));
                        targets.get(effect).stream().forEach((target) ->
                                this.sendToField(new GameActionFightDispellableEffectMessage(effect.effectId, fighter.getID(), new FightTriggeredEffect(target.getNextBuffUid().incrementAndGet(), target.getID(), (short) effect.duration, FightDispellableEnum.DISPELLABLE, spellLevel.getSpellId(), effect.effectUid, 0, (short) effect.diceNum, (short) effect.diceSide, (short) effect.value, (short) effect.delay)))
                        );
                        continue;
                    }
                    final EffectCast castInfos = new EffectCast(effect.getEffectType(), spellId == -1 ? spellLevel.getSpellId() : spellId, cellId, num1, effect, fighter, targets.get(effect), false, StatsEnum.NONE, 0, spellLevel);
                    castInfos.targetKnownCellId = cellId;
                    castInfos.oldCell = oldCell;
                    castInfos.casterOldCell = castingCell;
                    castInfos.setCritical(isCc);
                    if (EffectBase.tryApplyEffect(castInfos) == -3) {
                        break;
                    }
                }

                if (!fakeLaunch) {
                    this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -spellLevel.getApCost()));
                }

                if (!fakeLaunch
                        && fighter.getVisibleState() == GameActionFightInvisibilityStateEnum.INVISIBLE
                        /*&& silentCast
                        && spellLevel.getSpellId() != 2763*/) {
                    this.sendToField(new ShowCellMessage(fighter.getID(), fighter.getCellId()));
                }

                if (!fakeLaunch) {
                    this.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
                }
                if (informations != null) {
                    informations.clear();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void launchWeapon(CharacterFighter fighter, short cellId) {
        synchronized (fighter.getMutex()) {
            try {
                // Combat encore en cour ?
                if (this.fightState != fightState.STATE_ACTIVE) {
                    return;
                }
                if (fighter != this.currentFighter) {
                    return;
                }
                if (this.fightLoopState == fightLoopState.STATE_WAIT_READY) {
                    return;
                }
                this.startSequence(SequenceTypeEnum.SEQUENCE_WEAPON);
                final InventoryItem weapon = fighter.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON);
                if (weapon.getTemplate().getTypeId() == 83) { //Pière d'Ame
                    return;
                }
                // La cible si elle existe
                Fighter targetE = this.hasEnnemyInCell(cellId, fighter.getTeam());
                if (targetE == null) {
                    targetE = this.hasFriendInCell(cellId, fighter.getTeam());
                }
                int targetId = targetE == null ? -1 : targetE.getID();

                if (!fighter.getSpellsController().canLaunchWeapon(weapon)) {
                    fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
                    this.endSequence(SequenceTypeEnum.SEQUENCE_WEAPON, false);
                    return;
                }

                //TODO HACHE
                if (!(Pathfunction.goalDistance(map, cellId, fighter.getCellId()) <= weapon.getWeaponTemplate().getRange()
                        && Pathfunction.goalDistance(map, cellId, fighter.getCellId()) >= weapon.getWeaponTemplate().getMinRange()
                        && fighter.getAP() >= weapon.getWeaponTemplate().getApCost())) {
                    fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
                    this.endSequence(SequenceTypeEnum.SEQUENCE_WEAPON, false);
                    return;

                }

                this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -weapon.getWeaponTemplate().getApCost()));

                fighter.setUsedAP(fighter.getUsedAP() + weapon.getWeaponTemplate().getApCost());

                boolean isCc = false;

                int tauxCC = weapon.getWeaponTemplate().getCriticalHitProbability() - fighter.getStats().getTotal(StatsEnum.ADD_CRITICAL_HIT);
                if (tauxCC < 2) {
                    tauxCC = 2;
                }
                if (Fight.RANDOM.nextInt(tauxCC) == 0 && weapon.getWeaponTemplate().getCriticalHitProbability() > 0) {
                    isCc = true;
                }

                isCc &= !fighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);

                ArrayList<Fighter> targets = new ArrayList<>(5);

                for (short Cell : (new Zone(SpellShapeEnum.valueOf(weapon.getItemType().zoneShape()), weapon.getItemType().zoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(cellId), true), this.map)).getCells(cellId)) {
                    final FightCell fightCell = this.getCell(Cell);
                    if (fightCell != null) {
                        if (fightCell.hasGameObject(FightObjectType.OBJECT_FIGHTER) | fightCell.hasGameObject(FightObjectType.OBJECT_STATIC)) {
                            targets.addAll(fightCell.getObjectsAsFighterList());
                        }
                    }
                }

                targets.removeIf(Fighter::isDead);
                targets.remove(fighter);
                final ObjectEffectDice[] effects = weapon.getEffects()
                        .stream()
                        .filter(effect1 -> effect1 instanceof ObjectEffectDice && ArrayUtils.contains(EffectHelper.UN_RANDOMABLES_EFFECTS, effect1.actionId))
                        .map(x -> (ObjectEffectDice) x)
                        .toArray(ObjectEffectDice[]::new);

                double num1 = Fight.RANDOM.nextDouble();

        /*double num2 = Arrays.stream(effects)
                .mapToInt(Effect -> weapon.getTemplate().getEffect(Effect.actionId).random)
                .sum();*/
                boolean flag = false;

                this.sendToField(new GameActionFightCloseCombatMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL, fighter.getID(), targetId, cellId, (byte) (isCc ? 2 : 1), false, weapon.getTemplate().getId()));

                EffectInstanceDice effectParent;
                for (ObjectEffectDice effect : effects) {
                    effectParent = (EffectInstanceDice) (weapon.getTemplate().getEffect(effect.actionId, effect.diceNum) == null ?
                            Arrays.stream(weapon.getTemplate().getPossibleEffects())
                                    .parallel()
                                    .filter(x -> x instanceof EffectInstanceDice)
                                    .map(x -> (EffectInstanceDice) x)
                                    .filter(e -> e.diceNum == effect.diceNum && e.diceSide == effect.diceSide)
                                    .findFirst().get() :
                            weapon.getTemplate().getEffect(effect.actionId, effect.diceNum));
                    logger.debug(effectParent.toString());
            /*if (effectParent.random > 0) {
                if (!flag) {
                    if (num1 > (double) effectParent.random / num2) {
                        num1 -= (double) effectParent.random / num2;
                        continue;
                    } else {
                        flag = true;
                    }
                } else {
                    continue;
                }
            }*/
                    final EffectCast castInfos = new EffectCast(StatsEnum.valueOf(effect.actionId), 0, cellId, num1, effectParent, fighter, targets, true, StatsEnum.NONE, 0, null);
                    castInfos.targetKnownCellId = cellId;
                    if (isCc) {
                        castInfos.fakeValue = -1;
                    }
                    if (EffectBase.tryApplyEffect(castInfos) == -3) {
                        break;
                    }
                }
                fighter.getSpellsController().actualize(weapon, targetId);

                this.sendToField(new GameActionFightPointsVariationMessage(!isCc ? ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT : ActionIdEnum.ACTION_FIGHT_CLOSE_COMBAT_CRITICAL_MISS, fighter.getID(), fighter.getID(), (short) weapon.getWeaponTemplate().getApCost()));
                this.endSequence(SequenceTypeEnum.SEQUENCE_WEAPON, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canLaunchSpell(Fighter fighter, SpellLevel spell, short currentCell, short cellId, int targetId) {
        // Fake caster
        if (fighter != this.currentFighter) {
            return false;
        }

        // Fake cellId
        if (!this.fightCells.containsKey(cellId)) {
            return false;
        }

        // PA manquant ?
        if (fighter.getAP() < spell.getApCost()) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 170, String.valueOf(fighter.getAP()), String.valueOf(spell.getApCost())));
            return false;
        } else if (!this.map.getCell(cellId).walakable() || this.map.getCell(cellId).nonWalkableDuringFight()) {
            return false;
        } else if (spell.isNeedFreeCell() && targetId != -1) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 172));
            return false;
        } else if ((spell.isNeedTakenCell() && targetId == -1)) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 193));
            return false;
        } else if (spell.isNeedFreeTrapCell() && this.fightCells.get(cellId).hasGameObject(FightObjectType.OBJECT_TRAP)) {
            for (FightActivableObject obj : this.fightCells.get(cellId).getObjectsLayer()) {
                if (obj instanceof FightTrap && obj.getCellId() == cellId) {
                    fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 193));
                    return false;
                }
            }
        } else if (Arrays.stream(spell.getStatesForbidden()).anyMatch(x -> fighter.hasState(x))) {
            return false;
        } else if (Arrays.stream(spell.getStatesRequired()).anyMatch(x -> !fighter.hasState(x))) {
            return false;
        } else if (!fighter.getSpellsController().canLaunchSpell(spell, targetId)) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            return false;
        }

        int range = spell.getRange()
                + fighter.getBuff().getAllBuffs()
                .filter(buff -> buff instanceof BuffAddSpellRange && buff.castInfos.effect.diceNum == spell.getSpellId())
                .mapToInt(buff -> buff.castInfos.effect.value)
                .sum();


        if (spell.isRangeCanBeBoosted()) {
            int val1 = range + fighter.getStats().getTotal(StatsEnum.ADD_RANGE);
            if (val1 < spell.getMinRange()) {
                val1 = spell.getMinRange();
            }
            range = Math.min(val1, 280);
        }

        if (range < 0)
            range = 0;

        final Short[] zone = fighter.getCastZone(range, spell, currentCell);

        if ((!ArrayUtils.contains(zone, cellId))) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 171, String.valueOf(spell.getMinRange() != 0 ? spell.getMinRange() : 0), String.valueOf(range), String.valueOf(spell.getRange())));
            return false;
        } else if (spell.isCastTestLos() && !spell.isNeedFreeTrapCell()) {
            final MapPoint target = MapPoint.fromCellId(cellId);
            FightCell cell, lastCell = null;
            boolean result = true;

            for (final Point p : Bresenham.findLine(fighter.getMapPoint().get_x(), fighter.getMapPoint().get_y(), target.get_x(), target.get_y())) {
                if (!(MapPoint.isInMap(p.x, p.y))) {
                } else {
                    cell = this.getCell(MapTools.getCellNumFromXYCoordinates(p.x, p.y));
                    //fighter.send(new ShowCellMessage(0,cell.id));
                    if (lastCell != null && lastCell.hasFighter()) {
                        if (lastCell.getFighter().isVisibleFor(fighter)) {
                            result = false;
                        }
                    }
                    if (!(cell.isLineOfSight())) {
                        result = false;
                    }
                    lastCell = cell;
                }
            }
            if (!result) {
                fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 174));
                return false;
            }
        }
        return true;
    }


    public void toggleLock(Fighter fighter, FightOptionsEnum type) {
        boolean value = fighter.getTeam().isToggled(type) == false;
        fighter.getTeam().toggle(type, value);
        if (this.fightState == fightState.STATE_PLACE) {
            this.map.sendToField(new GameFightOptionStateUpdateMessage(this.fightId, fighter.getTeam().id, type.value, value));
        }

        final Message message;
        switch (type) {
            case FIGHT_OPTION_SET_CLOSED:
                if (value) {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 95);
                } else {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 96);
                }
                break;

            case FIGHT_OPTION_ASK_FOR_HELP:
                if (value) {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 103);
                } else {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 104);
                }
                break;

            case FIGHT_OPTION_SET_TO_PARTY_ONLY:
                if (value) {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 93);
                } else {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 94);
                }
                break;

            case FIGHT_OPTION_SET_SECRET:
                if (value) {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 40);

                    // on kick les spectateurs
                    this.kickSpectators();
                } else {
                    message = new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 39);
                }
                break;
            default:
                return;
        }
        this.sendToField(message);
    }

    public void kickSpectators() {
        this.kickSpectators(false);
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

        this.sendToField(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.getIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
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
        this.startTimer(new CancellableScheduledRunnable(BACK_GROUND_WORKER, 10, 10) {
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
                    if (this.currentFighter instanceof VirtualFighter) {
                        // Lancement de l'IA pour 30 secondes maximum
                        (this.currentFighter.asVirtual()).getMind().runAI();
                    } else if (this.currentFighter.getObjectType() == FightObjectType.OBJECT_STATIC) {
                        this.myLoopActionTimeOut = System.currentTimeMillis() + 750;
                    }
                    // Fin de tour
                    if (this.fightLoopState != fightLoopState.STATE_WAIT_END) {
                        this.fightLoopState = fightLoopState.STATE_END_TURN;
                    }

                    break;

                case STATE_WAIT_END: // Fin du combat
                    synchronized (hasFinished) {
                        if (!hasFinished || this.isActionsFinish() || this.myLoopActionTimeOut < System.currentTimeMillis()) {
                            hasFinished = true;
                            this.endTurn(true);
                            //System.Threading.Thread.Sleep(500);
                            this.myTeam1.endFight();
                            this.myTeam2.endFight();
                            this.endFight(this.getWinners(), this.getEnnemyTeam(this.getWinners()));
                        }
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
        final int beginTurnIndice = this.currentFighter.beginTurn();

        this.endSequence(SequenceTypeEnum.SEQUENCE_TURN_END, false);

        // Mort du joueur ou fin de combat
        if (beginTurnIndice == -3 || beginTurnIndice == -2) {
            return;
        }

        if (this.currentFighter.getLife() < 0) { //shouldn't happen
            currentFighter.tryDie(currentFighter.getID());
            return;
        }

        // Envois debut du tour
        this.sendToField(new GameFightTurnStartMessage(this.currentFighter.getID(), this.getTurnTime() / 100));

        if (this.currentFighter instanceof CharacterFighter) {
            this.currentFighter.send(this.currentFighter.asPlayer().getFighterStatsListMessagePacket());
        }

        this.sendToField((o) -> {
            o.send(new GameFightSynchronizeMessage(this.fighters().filter(x -> !x.isLeft()).map(x -> x.getGameContextActorInformations((Player) o)).toArray(GameFightFighterInformations[]::new)));
        });

        this.currentFighter.send(new GameFightTurnStartPlayingMessage());

        // Timeout du tour
        this.myLoopTimeOut = System.currentTimeMillis() + this.getTurnTime();

        // status en attente de fin de tour
        if ((this.currentFighter instanceof CharacterFighter
                && currentFighter.getPlayer().getClient() == null
                && this.currentFighter.getTeam().getAliveFighters().count() > 1L)
                || this.currentFighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffEndTurn)) {
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

            if (this.currentFighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffEndTurn))
                this.fightLoopState = fightLoopState.STATE_END_TURN;
            else if (this.currentFighter instanceof SlaveFighter) {
                if (this.currentFighter.getTeam().getAliveFighters().count() > 2L
                        && currentFighter.asSlave().summoner instanceof CharacterFighter
                        && currentFighter.asSlave().summoner.getPlayer().getClient() == null)
                    this.fightLoopState = fightLoopState.STATE_END_TURN;
                else
                    this.fightLoopState = fightLoopState.STATE_WAIT_TURN;
            } else
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

        if (this.isSequencing) {
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

    protected void onTackled(Fighter fighter, int movementLength) {
        final ArrayList<Fighter> tacklers = Pathfunction.getEnnemyNearToTakle(this, fighter.getTeam(), fighter.getCellId());

        final int tackledMp = fighter.getTackledMP();
        final int tackledAp = fighter.getTackledAP();
        if (fighter.getMP() - tackledMp < 0) {
            logger.error("Cannot apply tackle : mp tackled ({0}) > available mp ({1})", tackledMp, fighter.getMP());
        } else {
            this.sendToField(new GameActionFightTackledMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_TACKLED, fighter.getID(), tacklers.stream().mapToInt(x -> x.getID()).toArray()));

            fighter.setUsedAP(fighter.getUsedAP() + tackledAp);
            fighter.setUsedMP(fighter.getUsedMP() + tackledMp);

            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -tackledAp));
            this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, fighter.getID(), fighter.getID(), (short) -tackledMp));

            if (movementLength <= fighter.getMP()) {
                return;
            }
        }
    }

    public void affectSpellTo(Fighter caster, Fighter target, int level, int... spells) {
        int bestResult;
        SpellLevel spell;
        for (int spellid : spells) {
            spell = DAO.getSpells().findSpell(spellid).getSpellLevel(level);
            double num1 = Fight.RANDOM.nextDouble();
            double num2 = (double) Arrays.stream(spell.getEffects()).mapToInt(x -> x.random).sum();
            boolean flag = false;
            for (EffectInstanceDice effect : spell.getEffects()) {
                logger.debug(effect.toString());
                logger.debug("*EffectId {} {} target {} Triger {} validTarget {} spellMask {}", effect.effectId, StatsEnum.valueOf(effect.effectId), target.getID(), EffectHelper.verifyEffectTrigger(caster, target, spell.getEffects(), effect, false, effect.triggers, target.getCellId(), true), effect.isValidTarget(caster, target), EffectInstanceDice.verifySpellEffectMask(caster, target, effect, target.getCellId()));

                if (!(effect.isValidTarget(caster, target)
                        && EffectInstanceDice.verifySpellEffectMask(caster, target, effect, target.getCellId())) ||
                        ((effect.targetMask.equals("C") && caster.getCarrierActorId() == target.getID())
                                || (effect.targetMask.equals("a,A") && caster.getCarrierActorId() != 0 & caster.getID() == target.getID()))) {
                    continue;
                }
                if (effect.random > 0) {
                    if (!flag) {
                        if (num1 > (double) effect.random / num2) {
                            num1 -= (double) effect.random / num2;
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
                if (effect.delay > 0) {
                    target.getBuff().delayedEffects.add(new Couple<>(new EffectCast(effect.getEffectType(), spellid, target.getCellId(), num1, effect, caster, targets, false, StatsEnum.NONE, 0, spell), effect.delay));
                    this.sendToField(new GameActionFightDispellableEffectMessage(effect.effectId, caster.getID(), new FightTriggeredEffect(target.getNextBuffUid().incrementAndGet(), target.getID(), (short) effect.duration, FightDispellableEnum.DISPELLABLE, spellid, effect.effectUid, 0, (short) effect.diceNum, (short) effect.diceSide, (short) effect.value, (short) effect.delay)));
                    continue;
                }
                final EffectCast castInfos = new EffectCast(effect.getEffectType(), spellid, target.getCellId(), num1, effect, caster, targets, false, StatsEnum.NONE, 0, spell);
                castInfos.targetKnownCellId = target.getCellId();
                if (EffectBase.tryApplyEffect(castInfos) == -3) {
                    break;
                }
            }
        }
    }

    public synchronized GameMapMovement tryMove(Fighter fighter, koh.game.fights.utils.Path path) {
        if (fighter != this.currentFighter || path.isEmptyRide()) {
            return null;
        }

        // Pas assez de point de mouvement
        if (path.MPCost() > fighter.getMP()) {
            return null;
        }


        this.startSequence(SequenceTypeEnum.SEQUENCE_MOVE);

        if ((fighter.getTackledMP() > 0 || fighter.getTackledAP() > 0) && !this.currentFighter.getStates().hasState(FightStateEnum.ENRACINÉ)) {
            this.onTackled(fighter, path.MPCost());

        }

        //fighter.getPreviousCellPos().add(fighter.getCellId());

        for (int i = 0; i < path.getCellsPath().length; ++i) {
            if (Pathfunction.isStopCell(this, fighter.getTeam(), path.getCellsPath()[i].getId(), fighter)) {
                if (path.getEnd() != path.getCellsPath()[i]) {
                    path.cutPath(i + 1);
                    break;
                }
            }
            if (i != path.getCellsPath().length)
                fighter.getPreviousCellPos().add(path.getCellsPath()[i].getId());
        }

        GameMapMovement gameMapMovement = new GameMapMovement(this, fighter, path.getClientPathKeys());

        this.sendToField(new FieldNotification(new GameMapMovementMessage(gameMapMovement.keyMovements, fighter.getID())) {
            @Override
            public boolean can(Player perso) {
                return fighter.isVisibleFor(perso);
            }
        });

        fighter.usedMP += path.MPCost();
        this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, fighter.getID(), fighter.getID(), (short) -path.MPCost()));

        fighter.setCell(this.getCell(path.getEnd().getId()));
        fighter.setDirection(path.getEndCellDirection());
        this.endSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
        return gameMapMovement;
    }

    public synchronized GameMapMovement tryMove(Fighter fighter, MovementPath path) {
        // Pas a lui de jouer
        if (fighter != this.currentFighter) {
            return null;
        }

        // Pas assez de point de mouvement
        if (path.getMovementLength() > fighter.getMP() || path.getMovementLength() == -1) {
            return null;
        }

        this.startSequence(SequenceTypeEnum.SEQUENCE_MOVE);

        //fighter.getPreviousCellPos().add(fighter.getCellId());

        if ((fighter.getTackledMP() > 0 || fighter.getTackledAP() > 0) && !this.currentFighter.getStates().hasState(FightStateEnum.ENRACINÉ)) {
            this.onTackled(fighter, path.getMovementLength());
            if (path.transitCells.isEmpty() || path.getMovementLength() == 0) {
                this.endSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
                return null;
            }

        }
        final GameMapMovement gameMapMovement = new GameMapMovement(this, fighter, path.serializePath());

        this.sendToField(new FieldNotification(new GameMapMovementMessage(gameMapMovement.keyMovements, fighter.getID())) {
            @Override
            public boolean can(Player perso) {
                return fighter.isVisibleFor(perso);
            }
        });

        path.transitCells.forEach(fighter.getPreviousCellPos()::add);
        if (!path.transitCells.isEmpty() && !fighter.getPreviousCellPos().isEmpty()) {
            fighter.getPreviousCellPos().remove(fighter.getPreviousCellPos().size() - 1);
        }

        fighter.usedMP += path.getMovementLength();
        this.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_USE, fighter.getID(), fighter.getID(), (short) -path.getMovementLength()));

        fighter.setCell(this.getCell(path.getEndCell()));
        fighter.setDirection(path.getEndDirection());
        this.endSequence(SequenceTypeEnum.SEQUENCE_MOVE, false);
        return gameMapMovement;

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
        this.fighters().forEach(fr -> fr.setDirection(this.findPlacementDirection(fr)));
        this.sendToField(new GameFightPlacementSwapPositionsMessage(new IdentifiedEntityDispositionInformations[]{fighter.getIdentifiedEntityDispositionInformations(), fighterTarget.getIdentifiedEntityDispositionInformations()}));
    }

    public void setFighterReady(Fighter fighter) {
        // Si combat deja commencé on arrete
        if (this.fightState != fightState.STATE_PLACE) {
            return;
        }

        fighter.setTurnReady(!fighter.isTurnReady());

        this.sendToField(new GameFightHumanReadyStateMessage(fighter.getID(), fighter.isTurnReady()));

        // Debut du combat si tout le monde ready
        this.tryStartFight();
    }

    public void tryStartFight() {
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
            if (Cell.canWalk()) {
                // Affectation
                fighter.setCell(Cell);
                this.fighters().forEach(x -> x.setDirection(this.findPlacementDirection(x)));
                this.sendToField(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.getIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
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
        this.joinFightTeam(defender, this.myTeam2, true, (short) -1, true);

        // Si un timer pour le lancement du combat
        if (this.getStartTimer() != -1) {
            //FIXME: remove Thread.sleep
            this.startTimer(new CancellableScheduledRunnable(BACK_GROUND_WORKER, (getStartTimer() * 1000)) {
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
        if (this.fightState == fightState.STATE_PLACE)
            this.fightWorker.initTurns();
        if (this.fightState == FightState.STATE_ACTIVE && fighter.getObjectType() != FightObjectType.OBJECT_STATIC)
            this.sendToField(this.getFightTurnListMessage());
    }

    public void sendPlacementInformation(CharacterFighter fighter, boolean update) {
        if (update) {
            fighter.send(new GameContextDestroyMessage());
            fighter.send(new GameContextCreateMessage((byte) 2));
        }
        fighter.send(new GameFightStartingMessage(fightType.value, getTeam1().leaderId, getTeam2().leaderId));
        //TODO FriendUpdateMessage OnContexteChanged

        this.sendGameFightJoinMessage(fighter);
        fighter.send(new GameFightPlacementPossiblePositionsMessage(this.myFightCells.get(myTeam1).keySet().stream().mapToInt(x -> x.intValue()).toArray(), this.myFightCells.get(myTeam2).keySet().stream().mapToInt(x -> x.intValue()).toArray(), fighter.getTeam().id));

        if (!update) {
            CharacterHandler.sendCharacterStatsListMessage(fighter.getCharacter().getClient(), true);
        }
        //TODO iterate
        this.fighters().forEach((Actor) -> {
            fighter.send(new GameFightShowFighterMessage(Actor.getGameContextActorInformations(null)));
        });

        fighter.send(new GameEntitiesDispositionMessage(this.fighters().map(x -> x.getIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
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
            fighter.send(new GameFightStartingMessage(fightType.value, getTeam1().leaderId, getTeam2().leaderId));
            this.sendGameFightJoinMessage(fighter);
            this.fighters().filter(x -> !x.isLeft()).forEach((Actor) -> {
                fighter.send(new GameFightShowFighterMessage(Actor.getGameContextActorInformations(fighter.getCharacter())));

            });
            fighter.send(new GameEntitiesDispositionMessage(this.getAliveFighters().map(x -> x.getIdentifiedEntityDispositionInformations()).toArray(IdentifiedEntityDispositionInformations[]::new)));
            //fighter.getSpellsController().getInitialCooldown().entrySet()
            //       .stream().forEach(f-> System.out.println(f.getKey()+" "+f.getValue()));

            if (fighter.getSummonedCreature().noneMatch(fi -> fi instanceof SlaveFighter)) {
                fighter.send(new GameFightResumeMessage(getFightDispellableEffectExtendedInformations(), getAllGameActionMark(), this.fightWorker.fightTurn, (int) (System.currentTimeMillis() - this.fightTime), getIdols(),
                        fighter.getSpellsController().getInitialCooldown().entrySet()
                                .stream()
                                .map(x -> new GameFightSpellCooldown(x.getKey(), fighter.getSpellsController().minCastInterval(x.getKey()) == 0 ? x.getValue().initialCooldown : fighter.getSpellsController().minCastInterval(x.getKey())))
                                .toArray(GameFightSpellCooldown[]::new),
                        (byte) fighter.getTeam().getAliveFighters()
                                .filter(x -> x.getSummonerID() == fighter.getID() && !(x instanceof BombFighter))
                                .count(),
                        (byte) fighter.getTeam().getAliveFighters()
                                .filter(x -> x.getSummonerID() == fighter.getID() && (x instanceof BombFighter))
                                .count()));
            } else {
                fighter.send(new GameFightResumeWithSlavesMessage(getFightDispellableEffectExtendedInformations(), getAllGameActionMark(), this.fightWorker.fightTurn, (int) (System.currentTimeMillis() - this.fightTime), getIdols(),
                        fighter.getSpellsController().getInitialCooldown().entrySet()
                                .stream()
                                .map(x -> new GameFightSpellCooldown(x.getKey(), fighter.getSpellsController().minCastInterval(x.getKey()) == 0 ? x.getValue().initialCooldown : fighter.getSpellsController().minCastInterval(x.getKey())))
                                .toArray(GameFightSpellCooldown[]::new),
                        (byte) fighter.getTeam().getAliveFighters()
                                .filter(x -> x.getSummonerID() == fighter.getID() && !(x instanceof BombFighter))
                                .count(),
                        (byte) fighter.getTeam().getAliveFighters()
                                .filter(x -> x.getSummonerID() == fighter.getID() && (x instanceof BombFighter))
                                .count(), fighter.getGameFightResumeSlaveInfo()));
            }


            fighter.send(getFightTurnListMessage());
            fighter.send(new GameFightSynchronizeMessage(this.fighters().filter(x -> !x.isLeft()).map(x -> x.getGameContextActorInformations(fighter.getCharacter())).toArray(GameFightFighterInformations[]::new)));

            /*/213.248.126.93 ChallengeInfoMessage Second8 paket
             /213.248.126.93 ChallengeResultMessage Second9 paket*/
            CharacterHandler.sendCharacterStatsListMessage(fighter.getCharacter().getClient(), true);
            if (this.currentFighter.getID() == fighter.getID()) {
                fighter.send(this.currentFighter.asPlayer().getFighterStatsListMessagePacket());
            } else if (this.currentFighter instanceof SlaveFighter && currentFighter.getSummonerID() == fighter.getID()) {
                fighter.send(currentFighter.asSlave().getSwitchContextMessage());
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
            for (FightActivableObject object : objs) {
                if (object.getHiddenGameActionMark() != null)
                    gameActionMarks = ArrayUtils.add(gameActionMarks, object.getHiddenGameActionMark());
            }
        }
        return gameActionMarks;
    }

    public FightDispellableEffectExtendedInformations[] getFightDispellableEffectExtendedInformations() {
        FightDispellableEffectExtendedInformations[] FightDispellableEffectExtendedInformations = new FightDispellableEffectExtendedInformations[0];

        for (Stream<BuffEffect> Buffs : (Iterable<Stream<BuffEffect>>) this.getAliveFighters().map(x -> x.getBuff().getAllBuffs())::iterator) {
            for (BuffEffect Buff : (Iterable<BuffEffect>) Buffs::iterator) {
                FightDispellableEffectExtendedInformations = ArrayUtils.add(FightDispellableEffectExtendedInformations, new FightDispellableEffectExtendedInformations(Buff.castInfos.effectType.value(), Buff.caster.getID(), Buff.getAbstractFightDispellableEffect()));
            }
        }
        /*return Stream.of(this.getAliveFighters()
         .map(x -> x.buff.getAllBuffs()
         .map(Buff -> (new FightDispellableEffectExtendedInformations(Buff.castInfos.effectType.value(), Buff.caster.id, Buff.getAbstractFightDispellableEffect())))
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
        final boolean team1 = this.myTeam1.getFighters().anyMatch(Player -> !Player.isMarkedDead() && (!Player.isLeft()));
        final boolean team2 = this.myTeam2.getFighters().anyMatch(Player -> !Player.isMarkedDead() && (!Player.isLeft()));
        /*for (Fighter player : fighters()) {
         if ((player.team.id == 0) && (!player.dead) && (!player.left)) {
         Team1 = true;
         }
         if ((player.team.id == 1) && (!player.dead) && (!player.left)) {
         Team2 = true;
         }
         }*/
        return !(team1 && team2);
    }

    public synchronized boolean tryEndFight() {
        if (this.getWinners() != null) {
            this.fightLoopState = fightLoopState.STATE_WAIT_END;
            return true;
        }
        return false;
    }

    public boolean startSequence(SequenceTypeEnum sequenceType) {
        this.m_lastSequenceAction = sequenceType;
        ++this.m_sequenceLevel;
        if (this.isSequencing) {
            return false;
        }
        this.isSequencing = true;
        this.sequence = sequenceType;
        this.m_sequences.push(sequenceType);
        this.sendToField(new SequenceStartMessage(sequenceType.value, this.currentFighter.getID())); //TODO not Spectator?
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
        if (!this.isSequencing) {
            return false;
        }
        --this.m_sequenceLevel;
        if (this.m_sequenceLevel > 0 && !force) {
            return false;
        }
        this.isSequencing = false;
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
        this.isSequencing = false;
        this.waitAcknowledgment = false;
        while (this.m_sequences.size() > 0) {
            this.sendToField(new SequenceEndMessage(this.m_lastSequenceAction.value, this.currentFighter.getID(), this.m_sequences.pop().value));
        }
    }

    public void acknowledgeAction() {
        this.waitAcknowledgment = false;
    }

    protected synchronized void endFight() {
        try {
            switch (this.fightState) {
                case STATE_PLACE:
                    this.map.sendToField(new GameRolePlayRemoveChallengeMessage(this.fightId));
                    break;
                case STATE_FINISH:
                    return;
            }

            this.stopTimer("gameLoop");

            this.myTeam1.getFighters().filter(fr -> fr instanceof CharacterFighter).forEach(c -> c.getPlayer().getFightsRegistred().remove(this));
            this.myTeam2.getFighters().filter(fr -> fr instanceof CharacterFighter).forEach(c -> c.getPlayer().getFightsRegistred().remove(this));


            this.sendToField(this.myResult);

            this.creationTime = 0;
            this.fightTime = 0;
            this.ageBonus = 0;
            this.endAllSequences();
            this.fighters().forEach(Fighter::endFight);

            this.kickSpectators(true);

            this.fightCells.values().forEach(FightCell::clear);
            this.fightCells.clear();
            this.myTimers.clear();
            this.fightWorker.dispose();
            this.myTeam1.dispose();
            this.myTeam2.dispose();

            this.myFightCells = null;
            this.fightCells = null;
            this.myTeam1 = null;
            this.myTeam2 = null;
            this.fightWorker = null;
            this.activableObjects.values().forEach(CopyOnWriteArrayList::clear);
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
                    myTimers.values().stream().forEach(CancellableScheduledRunnable::cancel);
                } catch (Exception e) {
                } finally {
                    myTimers.clear();
                    myTimers = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public abstract GameFightEndMessage leftEndMessage(CharacterFighter fighter);

    protected boolean isStarted() {
        return this.fightState != fightState.STATE_INIT && this.fightState != fightState.STATE_PLACE;
    }

    private void kickSpectators(boolean end) {
        synchronized (mySpectators) {
            final ArrayList<WorldClient> spectators = new ArrayList<>(mySpectators.size());
            spectators.addAll(mySpectators);
            spectators.forEach(this::leaveSpectator);
        }
    }

    public void leaveSpectator(WorldClient client) {
        synchronized (this.mySpectators) {
            this.mySpectators.remove(client);
        }
        this.unregisterPlayer(client.getCharacter());
        client.endGameAction(GameActionTypeEnum.FIGHT);
        client.getCharacter().setFight(null);
        client.send(new GameContextDestroyMessage());
        client.send(new GameContextCreateMessage((byte) 1));
        client.send(new CurrentMapMessage(client.getCharacter().getCurrentMap().getId(), "649ae451ca33ec53bbcbcc33becf15f4"));
        client.getCharacter().getCurrentMap().spawnActor(client.getCharacter());
        client.getCharacter().refreshStats(false, true);
    }

    public void joinFightSpectator(WorldClient client) {
        synchronized (this.mySpectators) {
            this.mySpectators.add(client);
        }
        // On enleve l'entitée de la map
        client.getCharacter().destroyFromMap();
        client.send(new GameContextDestroyMessage());
        client.send(new GameContextCreateMessage((byte) 2));

        // On lui ajoute la gameaction
        client.addGameAction(new GameFightSpectator(client, this));

        this.registerPlayer(client.getCharacter());
        client.getCharacter().setFight(this);
        //TODO SEQUENCE

        client.send(new GameFightJoinMessage(false, false, this.isStarted(), (short) 0, this.fightType.value));
        this.fighters().filter(x -> !x.isLeft()).forEach((Actor) -> {
            client.send(new GameFightShowFighterMessage(Actor.getGameContextActorInformations(client.getCharacter())));
        });

        client.send(getFightTurnListMessage());
        client.send(new GameFightSpectateMessage(getFightDispellableEffectExtendedInformations(), getAllGameActionMark(), this.fightWorker.fightTurn, (int) Instant.now().minusMillis(this.fightTime).toEpochMilli(), getIdols()));
        client.send(new GameFightNewRoundMessage(this.fightWorker.round));
            /*/213.248.126.93 ChallengeInfoMessage Second8 paket
             /213.248.126.93 ChallengeResultMessage Second9 paket*/
        CharacterHandler.sendCharacterStatsListMessage(client, true);
        this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 36, client.getCharacter().getNickName()));
        if (this.currentFighter != null) {
            client.send(new GameFightTurnResumeMessage(this.currentFighter.getID(), this.getTurnTime() / 100, (int) (this.myLoopTimeOut - System.currentTimeMillis()) / 100));
        }
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
            if (fighter.getPlayer().getClient() != null && fighter.getPlayer().getClient().getParty() != null
                    && !fighter.getPlayer().getClient().getParty().partyName.isEmpty()
                    && !Arrays.stream(this.myResult.namedPartyTeamsOutcomes)
                    .anyMatch(x -> x.team.partyName.equalsIgnoreCase((fighter.getPlayer().getClient().getParty().partyName)))) {
                this.myResult.namedPartyTeamsOutcomes = ArrayUtils.add(this.myResult.namedPartyTeamsOutcomes, new NamedPartyTeamWithOutcome(new NamedPartyTeam(fighter.getTeam().id, fighter.getPlayer().getClient().getParty().partyName), outcome));
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
        if (!this.fightCells.containsKey(cell)) {
            return false;
        }
        return this.fightCells.get(cell).hasObject(type);
    }

    public boolean canPutObject(short cellId) {
        if (!this.fightCells.containsKey(cellId)) {
            return false;
        }
        return this.fightCells.get(cellId).canPutObject();
    }

    public FightExternalInformations getFightExternalInformations(Player visitor) {
        return new FightExternalInformations(this.fightId, this.fightType.value, (int) this.creationTime, this.canJoinSpectator(), new FightTeamLightInformations[]{myTeam1.getFightTeamLightInformations(visitor), myTeam2.getFightTeamLightInformations(visitor)}, new FightOptionsInformations[]{myTeam1.getFightOptionsInformations(), myTeam2.getFightOptionsInformations()});
    }

    public boolean isCellWalkable(short cellId) {
        if (!this.fightCells.containsKey(cellId)) {
            return false;
        }

        return this.fightCells.get(cellId).canWalk();
    }

    public FightCell getCell(short cellId) {
        if (this.fightCells.containsKey(cellId)) {
            return this.fightCells.get(cellId);
        }
        return null;
    }

    public Fighter hasEnnemyInCell(short cellId, FightTeam Team) {
        if (!this.fightCells.containsKey(cellId)) {
            return null;
        }
        return this.fightCells.get(cellId).hasEnnemy(Team);
    }

    public Fighter hasFriendInCell(short cellId, FightTeam Team) {
        if (!this.fightCells.containsKey(cellId)) {
            return null;
        }
        return this.fightCells.get(cellId).hasFriend(Team);
    }

    public GameFightTurnListMessage getFightTurnListMessage() {
        if (this.isStarted()) {
            return new GameFightTurnListMessage(this.fightWorker.fighters().stream().sequential().filter(x -> !(x instanceof StaticFighter))
                    //.sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false)))
                    .mapToInt(x -> x.getID()).toArray(), this.getDeadFighters().filter(x -> !(x instanceof StaticFighter))
                    .mapToInt(x -> x.getID()).toArray());
        }
        return new GameFightTurnListMessage(this.getAliveFighters().filter(x -> !(x instanceof StaticFighter))
                // .sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false)))
                .mapToInt(x -> x.getID()).toArray(), this.getDeadFighters().filter(x -> !(x instanceof StaticFighter))
                .mapToInt(x -> x.getID()).toArray());
    }

    private synchronized FightCell getFreeSpawnCell(FightTeam team) {
        for (FightCell cell : this.myFightCells.get(team).values()) {
            if (!cell.isWalkable()) {
                logger.info("Cell {} map2 {} ", cell.getId(), map.getId());
            }
            if (cell.canWalk()) {
                return cell;
            }
        }
        if (!this.myFightCells.get(team).isEmpty()) {
            return this.myFightCells.get(team).values().stream().filter(x -> x.getObjectsAsFighter() == null).findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public void actorMoved(Path Path, IGameActor Actor, short newCell, byte newDirection) {
        //((Fighter) actor).setCell(fightCells.get(newCell));
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


    public boolean isCellWalkable(DofusCell cell, boolean throughEntities /*= false*/, DofusCell previousCell /*= null*/) {
        if (!cell.walakable())
            return false;

        if (cell.nonWalkableDuringRP())
            return false;

        // compare the floors
        if (map.isUsingNewMovementSystem() && previousCell != null) {
            int floorDiff = Math.abs(cell.getFloor()) - Math.abs(previousCell.getFloor());

            if (cell.getMoveZone() != previousCell.getMoveZone() ||
                    cell.getMoveZone() == previousCell.getMoveZone() && cell.getMoveZone() == 0 && floorDiff > 11)
                return false;
        }

        if (!throughEntities && getCell(cell.getId()).canWalk())
            return false;

        // todo : LoS => Sure ? LoS may stop a walk ?!

        return true;
    }

    public short getPlacementTimeLeft() {
        if (this.isStarted()) {
            return 0;
        }
        double num = (double) (this.getStartTimer() - (Instant.now().getEpochSecond() - this.creationTime)) * 10;
        if (num < 0.0) {
            num = 0.0;
        }
        return (short) num;
    }

    public enum FightLoopState {

        STATE_WAIT_START, STATE_WAIT_TURN, STATE_WAIT_ACTION, STATE_WAIT_READY, STATE_WAIT_END, STATE_WAIT_AI, STATE_END_TURN, STATE_END_FIGHT,
    }

}
