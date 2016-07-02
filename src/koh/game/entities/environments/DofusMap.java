package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.InteractiveElementAction;
import koh.game.controllers.MapController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.Path;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.FightController;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.messages.game.context.GameContextRemoveElementMessage;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectListAddMessage;
import koh.protocol.messages.game.context.roleplay.GameRolePlayShowActorMessage;
import koh.protocol.messages.game.context.roleplay.MapComplementaryInformationsDataMessage;
import koh.protocol.messages.game.context.roleplay.MapFightCountMessage;
import koh.protocol.messages.game.context.roleplay.objects.ObjectGroundAddedMessage;
import koh.protocol.messages.game.context.roleplay.objects.ObjectGroundListAddedMessage;
import koh.protocol.messages.game.context.roleplay.objects.ObjectGroundRemovedMessage;
import koh.protocol.messages.game.context.roleplay.objects.ObjectGroundRemovedMultipleMessage;
import koh.protocol.messages.game.context.roleplay.paddock.PaddockPropertiesMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.game.pvp.UpdateMapPlayersAgressableStatusMessage;
import koh.protocol.types.game.context.MapCoordinates;
import koh.protocol.types.game.context.fight.FightCommonInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayActorInformations;
import koh.protocol.types.game.interactive.MapObstacle;
import koh.protocol.types.game.interactive.StatedElement;
import koh.protocol.types.game.house.HouseInformations;
import koh.protocol.types.game.interactive.InteractiveElement;
import koh.protocol.types.game.interactive.InteractiveElementSkill;
import koh.protocol.types.game.interactive.InteractiveElementWithAgeBonus;
import koh.utils.Enumerable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusMap extends IWorldEventObserver implements IWorldField {

    @Getter
    private int id;
    private byte version,mapType;
    @Getter
    private int relativeId,subAreaId;
    @Getter
    public int bottomNeighbourId,topNeighbourId, leftNeighbourId, rightNeighbourId, shadowBonusOnEntities;
    @Getter @Setter
    private NeighBourStruct[] newNeighbour;
    public boolean useLowPassFilter, useReverb;
    private int presetId;
    private byte[] compressedCells, compressedLayers;
    @Getter
    private boolean myInitialized = false;
    @Getter
    private final Map<Integer, IGameActor> myGameActors = new ConcurrentHashMap<>();
    @Getter @Setter
    private StatedElement[] elementsStated = new StatedElement[0];
    @Getter
    private final List<InteractiveElementStruct> interactiveElements = new ArrayList<>();
    private Map<Short, InventoryItem> droppedItems;
    @Getter
    private final List<HouseInformations> houses = new ArrayList<>();
    @Getter @Setter
    public MapPosition position;
    @Getter
    private final MutableInt myNextActorId = new MutableInt(0);
    @Getter
    private final ArrayList<MonsterGroup> monsters = new ArrayList<>(4);
    private Map<Integer, MapDoor> doors;
    private FightController myFightController;
    /*After loadAll */
    @Getter
    private short[] blueCells, redCells;
    private DofusCell[] cells;
    private Layer[] layers;

    public void spawnActor(IGameActor actor) {

        if (!this.myInitialized) {
            this.initialize();
        }

        if (actor instanceof Player) {
            if (((Player) actor).getClient() != null) {
                this.registerPlayer((Player) actor);
                this.onPlayerSpawned((Player) actor);
            }
        }

        this.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) actor.getGameContextActorInformations(null)));

        this.myGameActors.put(actor.getID(), actor);

        //verify Todo
        /*if (this.cells.length <  actor.getCellId) {
         if (this.fightCells[actor.getCellId].Walkable) {
         this.fightCells[actor.getCellId].addActor(actor);
         } else {
         actor.getCellId = this.GetFreeCell();

         this.fightCells[actor.getCellId].addActor(actor);
         }
         }*/
        this.cells[actor.getCell().getId()].addActor(actor);

    }

    public void onPlayerSpawned(Player actor) {
        /*if (this.id == 115083777 && !actor.getNickName().startsWith("Melan")) {
            actor.send(new PopupWarningMessage((byte) 5, "MØ", "Pour consulter les vendeurs , parlez à Hal San !"));
        }*/
    }

    public SubArea getSubArea() {
        if (this.subAreaId == 0) {
            return null;
        }
        return DAO.getAreas().getSubArea(this.subAreaId);
    }

    public Area getArea() {
        return getSubArea() != null ? getSubArea().getArea() : DAO.getAreas().getSubArea(0).getArea();
    }

    public MapCoordinates coordinates() {
        try {
            return new MapCoordinates(this.position.getPosX(), this.position.getPosY());
        } catch (Exception e) {
            return new MapCoordinates(); //Dj ecT..
        }
    }

    public void destroyActor(IGameActor actor) {

        if (actor instanceof Player) {
            if (((Player) actor).getClient() != null) {
                this.unregisterPlayer((Player) actor);
            }
        }

        this.myGameActors.remove(actor.getID());
        this.sendToField(new GameContextRemoveElementMessage(actor.getID()));
        this.cells[actor.getCell().getId()].delActor(actor);
    }

    public DofusCell getCell(short c) {
        try {
            return this.cells[c];
            //return Arrays.stream(cells).filter(x -> x.id == c).findFirst().get();
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public StatedElement getStatedElementById(int id) {
        return Arrays.stream(this.elementsStated).filter(x -> x.elementId == id).findFirst().orElse(null);
    }

    public InteractiveElementStruct getInteractiveElementStruct(int id) {
        return this.interactiveElements.stream().filter(x -> x.elementId == id).findFirst().orElse(null);
    }

    private static final byte TOLERANCE_ELEVATION = 11;

    public boolean pointMov(int x, int y, boolean bAllowTroughEntity, int previousCellId, int endCellId) {
        boolean useNewSystem = false;
        int cellId = 0;
        DofusCell cellData = null;
        boolean mov = false;
        DofusCell previousCellData = null;
        int dif = 0;
        //var cellEntities:Array; Obstacle glyphe ect lol ?
        //var o:IObstacle;
        if (MapPoint.isInMap(x, y)) {
            useNewSystem =  this.usingNewMovementSystem;
            cellId = MapPoint.fromCoords(x, y).get_cellId();
            cellData = this.cells[cellId];
            mov = cellData.mov() && !cellData.nonWalkableDuringFight()/*&& (((!(this.isInFight)) || (!(cellData.nonWalkableDuringFight)))))*/;

            if (((((((mov) && (useNewSystem))) && (!((previousCellId == -1))))) && (!((previousCellId == cellId))))) {
                previousCellData = cells[previousCellId];
                dif = Math.abs((Math.abs(cellData.getFloor()) - Math.abs(previousCellData.getFloor())));
                if (((((!((previousCellData.getMoveZone() == cellData.getMoveZone()))) && ((dif > 0)))) || ((((((previousCellData.getMoveZone() == cellData.getMoveZone())) && ((cellData.getMoveZone() == 0)))) && ((dif > TOLERANCE_ELEVATION)))))) {
                    mov = false;
                }
            }

            if (!(bAllowTroughEntity)) {
                if (this.getCell((short) cellId).hasActor()) {
                    for (IGameActor o : this.getCell((short) cellId).getActors()) {
                        if ((((endCellId == cellId)) /*&& (o.canWalkTo())*/)) {
                        } else {
                            /*if (!(o.canWalkThrough())) {
                             return (false);
                             };*/
                        };
                    };
                };
            };
        } else {
            mov = false;
        }
        return (mov);
    }

    public UpdateMapPlayersAgressableStatusMessage getAgressableActorsStatus(Player player) {
        synchronized (this.myGameActors) {
            return new UpdateMapPlayersAgressableStatusMessage(this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> x.getID()).toArray(), this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> ((Player) x).getPvPEnabled()).toArray());
        }
    }

    public MapComplementaryInformationsDataMessage getFakedMapComplementaryInformationsDataMessage(Player character) {
        return new MapComplementaryInformationsDataMessage(subAreaId, id, houses, new ArrayList<GameRolePlayActorInformations>() {{ add( (GameRolePlayActorInformations) character.getGameContextActorInformations(null)); }}, this.toInteractiveElements(character), elementsStated, new MapObstacle[0], new FightCommonInformations[0]);
    }

    public MapComplementaryInformationsDataMessage getMapComplementaryInformationsDataMessage(Player character) {
        return new MapComplementaryInformationsDataMessage(subAreaId, id, houses, this.myGameActors.values().stream().filter(x -> x.canBeSeen(character)).map(x -> (GameRolePlayActorInformations) x.getGameContextActorInformations(character)).collect(Collectors.toList()), this.toInteractiveElements(character), elementsStated, new MapObstacle[0], new FightCommonInformations[0]);
    }

    public int getCellSpeed(int cellId) {
        return this.cells[(short) cellId].getSpeed();
    }

    @Getter
    private boolean usingNewMovementSystem = false;

    
    public DofusMap init$Return(){
        this.initialize();
        return this;
    }

    public synchronized void initialize() {
        if (this.myInitialized) {
            return;
        }
        this.myInitialized = true;
        //this.cells = MapController.unCompressCells(this, compressedCells);
        IoBuffer buf = IoBuffer.wrap(compressedCells);
        this.cells = new DofusCell[buf.getInt()];
        int _oldMvtSystem = -1;
        for (short i = 0; i < this.cells.length; i++) {
            this.cells[i] = new DofusCell(this, i, buf);
            if(_oldMvtSystem == -1){
                _oldMvtSystem = this.cells[i].getMoveZone();
            }
            if(this.cells[i].getMoveZone() != _oldMvtSystem){
               this.usingNewMovementSystem = true;
            }
        }
        buf.clear();
        buf = IoBuffer.wrap(compressedLayers);
        this.layers = new Layer[buf.getInt()];
        for (short i = 0; i < this.layers.length; i++) {
            this.layers[i] = new Layer(this, buf);
        }
        buf.clear();
      /*  this.blueCells = MapController.unCompressStartingCells(this, compressedBlueCells);
        this.redCells = MapController.unCompressStartingCells(this, compressedRedCells);*/
        //StatedElement[]
        //this.elementsStated = this.ElementsList.stream().map(x -> new StatedElement(x.id, x.ElementCellID, x.State)).collect(Collectors.toList()).toArray(new StatedElement[this.ElementsList.size()]);
        this.compressedCells = null;
        this.compressedLayers = null;
        /*this.compressedBlueCells = null;
        this.compressedRedCells = null;*/
        //throw un nullP mieux que referencer une liste on 111000 map object
        try {
            for (Npc npc : DAO.getNpcs().forMap(this.id)) {
                    npc.setID(getNextActorId());
                    npc.setActorCell(this.getCell(npc.getCellID()));
                    this.spawnActor(npc);
            }
        } catch(NullPointerException ignored) {}

        this.monsters.stream()
                .filter(gr -> !gr.isFix())
                .forEach(gr -> {
                    gr.getGameRolePlayGroupMonsterInformations().disposition.cellId = this.getRandomWalkableCell();
                    gr.setActorCell(this.getCell(gr.getGameContextActorInformations(null).disposition.cellId));
                });
        this.monsters.stream()
                .filter(gr -> gr.isFix())
                .forEach(gr -> gr.setActorCell(this.getCell(gr.getFixedCell())));

        this.monsters.forEach(this::spawnActor);

        this.myFightController = new FightController();
        //this.interactiveElements.forEach(x -> System.out.println(x.toString()));
    }

    public synchronized void removeSpawn(final MonsterGroup grp){
        this.destroyActor(grp);
        final int timeReq = grp.getGameRolePlayGroupMonsterInformations().staticInfos.underlings.length * 20; //To wait 20s * MonsterIngroup
        if(!grp.isFix()){
            this.monsters.remove(grp);
            final MonsterGroup newGrp = DAO.getMapMonsters().genMonsterGroup(this.getSubArea(),this);
            this.addMonster(grp);
            new CancellableScheduledRunnable(this.getArea().getBackGroundWorker(), timeReq * 1000){
                @Override
                public void run() {
                    spawnActor(newGrp);
                }
            };
        }
        else{
            new CancellableScheduledRunnable(this.getArea().getBackGroundWorker(), timeReq * 1000){
                @Override
                public void run() {
                    spawnActor(grp);
                }
            };
        }
    }

    public synchronized int getNextActorId(){
        this.myNextActorId.decrement();
        return this.myNextActorId.intValue();
    }

    public DofusMap(int id, byte v, int r, byte m, int SubAreaId, int bn, int tn, int ln, int rn, int sb, boolean u, boolean ur, int pr, String cc, String sl, byte[] sbb, byte[] rc) {
        this.id = id;
        this.version = v;
        this.relativeId = r;
        this.mapType = m;
        this.subAreaId = SubAreaId;
        //Todo subArea getInstance
        this.bottomNeighbourId = bn;
        this.topNeighbourId = tn;
        this.leftNeighbourId = ln;
        this.rightNeighbourId = rn;
        this.shadowBonusOnEntities = sb;
        this.useLowPassFilter = u;
        this.useReverb = ur;
        this.presetId = pr;
        this.compressedCells = sbb;
        this.compressedLayers = rc;
        this.blueCells = Enumerable.stringToShortArray(cc);
        this.redCells =  Enumerable.stringToShortArray(sl);
    }

    public static final ScheduledExecutorService GlobalTimer = Executors.newScheduledThreadPool(20);

    @Override
    public void actorMoved(Path path, IGameActor actor, short newCell, byte newDirection) {
        this.cells[actor.getCell().getId()].delActor(actor);
        this.cells[newCell].addActor(actor);
        if (newDirection != -1) {
            actor.setDirection(newDirection);
        }
    }

    public void onMouvementConfirmed(Player actor) {
        if (actor.getClient().getOnMouvementConfirm() != null) {
            actor.getClient().getOnMouvementConfirm().apply(actor);
            actor.getClient().setOnMouvementConfirm(null);
        }
        if (droppedItems == null || droppedItems.isEmpty()) {
            return;
        }
        synchronized (droppedItems) {
            if (droppedItems.containsKey(actor.getCell().getId())) {
                actor.getInventoryCache().add(droppedItems.get(actor.getCell().getId()), true);
                droppedItems.remove(actor.getCell().getId());
                this.sendToField(new ObjectGroundRemovedMessage(actor.getCell().getId()));
                if (droppedItems.isEmpty()) {
                    this.droppedItems = null;
                }
            }
        }
    }

    public synchronized void clearDroppedItems() {
        if (droppedItems == null) {
            return;
        }
        synchronized (this.droppedItems) {
            this.sendToField(new ObjectGroundRemovedMultipleMessage(droppedItems.keySet().toArray(new Short[droppedItems.size()])));
            this.droppedItems.clear();
            this.droppedItems = null;
        }
    }

    public InteractiveElement[] toInteractiveElements(Player actor) {
        return this.interactiveElements.stream().map(Element -> new InteractiveElementWithAgeBonus(Element.elementId,
                Element.elementTypeId,
                Element.skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, actor) && staticElementIsOpened(Element.elementId)).toArray(InteractiveElementSkill[]::new),
                Element.skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, actor) && staticElementIsOpened(Element.elementId))).toArray(InteractiveElementSkill[]::new),
                Element.ageBonus)
        ).toArray(InteractiveElement[]::new);
    }

    public InteractiveElement toInteractiveElement(Player actor, int elementId) {
        InteractiveElementStruct Struct = this.getInteractiveElementStruct(elementId);
        return new InteractiveElementWithAgeBonus(Struct.elementId,
                Struct.elementTypeId,
                Struct.skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, actor) && staticElementIsOpened(Struct.elementId)).toArray(InteractiveElementSkill[]::new),
                Struct.skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, actor) && staticElementIsOpened(Struct.elementId))).toArray(InteractiveElementSkill[]::new),
                Struct.ageBonus
        );
    }

    public boolean staticElementIsOpened(int elementId) {
        return !(this.getStatedElementById(elementId) != null && getStatedElementById(elementId).elementState > 0);
    }

    public boolean hasActorOnCell(short cell) {
        return this.myGameActors.values().stream().filter(x -> x.getCell() != null && x.getCell().getId() == cell).count() > 0;
    }

    public Npc getNpc(int contextualid) {
        return (Npc) this.myGameActors.get(contextualid);
    }

    private final static Random RANDOM = new Random();

    public short getRandomCell() {
        return this.cells[RANDOM.nextInt(this.cells.length)].getId();
        //return this.fightCells.Keys.FirstOrDefault(x => x == RANDOM.Next(this.fightCells.Keys.Count));
    }

    public synchronized DofusCell getAnyCellWalakable() {
        this.initialize();
        return Arrays.stream(this.cells).filter(x -> x.mov()).findAny().orElse(this.cells[0]);
    }

    public short getRandomWalkableCell() {
        short cell = getRandomCell();
        if (cells[cell].mov()) {
            return cell;
        } else {
            return getRandomWalkableCell();
        }
    }

    public short getRandomWalkableCellInFight() {
        short cell = getRandomCell();
        if (cells[cell].walakableInFight()) {
            return cell;
        } else {
            return getRandomWalkableCellInFight();
        }
    }


    public void addDoor(MapDoor d) {
        if (this.doors == null) {
            this.doors = new HashMap();
        }
        this.doors.put(d.getElementID(), d);
    }

    public MapDoor getDoor(int id) {
        if (this.doors == null) {
            return null;
        }
        return this.doors.get(id);
    }

    public boolean cellIsOccuped(short cell) {
        if (droppedItems == null) {
            return false;
        }
        synchronized (this.droppedItems) {
            return this.droppedItems != null && this.droppedItems.containsKey(cell);
        }
    }

    public void addItem(Short cell, InventoryItem item) {
        if (this.droppedItems == null) {
            droppedItems = Collections.synchronizedMap(new HashMap<>());
        }
        this.droppedItems.put(cell, item);
        this.sendToField(new ObjectGroundAddedMessage(cell, item.getTemplateId()));
    }

    public ObjectGroundListAddedMessage objectsGround() {
        if (this.droppedItems == null || this.droppedItems.size() <= 0) {
            return null;
        }
        return new ObjectGroundListAddedMessage(droppedItems.keySet().toArray(new Short[droppedItems.size()]), droppedItems.values().stream().mapToInt(x -> x.getTemplateId()).toArray());
    }

    public IGameActor getActor(int target) {
        if (!myGameActors.containsKey(target)) {
            return null;
        }
        return this.myGameActors.get(target);
    }

    public Player getPlayer(int target) {
        if (!myGameActors.containsKey(target)) {
            return null;
        }
        return (Player) this.myGameActors.get(target);
    }

    public Integer playersCount() {
        return (int) this.myGameActors.values().stream().filter(x -> x instanceof Player).count();
    }

    public short nextFightId() {
        return (short) this.myFightController.nextFightId();
    }

    public void addFight(Fight fight) {
        this.myFightController.addFight(fight);

        this.sendMapFightCountMessage();
    }

    public void removeFight(Fight fight) {
        this.myFightController.removeFight(fight);

        this.sendMapFightCountMessage();
    }

    public void addMonster(MonsterGroup gr){
        gr.setID(gr.getGameRolePlayGroupMonsterInformations().contextualId);
        this.monsters.add(gr);
    }

    public void sendMapInfo(WorldClient client) {
        this.myFightController.sendFightInfos(client);
        //FIXME : Dafuk am certain some maps have 2 paddocks
        Paddock paddock = DAO.getPaddocks().find(id);
        if (paddock != null) {
            client.send(new PaddockPropertiesMessage(paddock.getInformations()));
            if (paddock.getItems() != null) {
                client.send(new GameDataPaddockObjectListAddMessage(paddock.getItems()));
            }
        }
        client.send(getAgressableActorsStatus(client.getCharacter()));
        // client.send(new UpdateSelfAgressableStatusMessage(client.character.PvPEnabled,(int)System.currentTimeMillis() + 360000));
        Message objectsGround = client.getCharacter().getCurrentMap().objectsGround();
        if (objectsGround != null) {
            client.send(objectsGround);
        }
    }

    public void sendMapFightCountMessage() {
        this.sendToField(new MapFightCountMessage(this.myFightController.fightCount()));
    }

    public Fight getFight(int FightId) {
        return this.myFightController.getFight(FightId);
    }

    public List<Fight> getFights() {
        return this.myFightController.getFights();
    }

    public DofusCell[] getCells() {
        return this.cells;
    }

    public DofusCell getRandomAdjacentFreeCell(short Id) {
        DofusCell RandomCell = this.getCell((short) MapPoint.fromCellId(Id).getNearestFreeCell(this).get_cellId());
        if (RandomCell == null || !RandomCell.mov()) {
            for (int i = 0; i < 8; i++) {
                RandomCell = this.getCell((short) MapPoint.fromCellId(Id).getNearestFreeCellInDirection(i, this, false).get_cellId());
                if (RandomCell != null && RandomCell.mov()) {
                    continue;
                }
            }
            if (RandomCell == null) {
                throw new Error("Can't find cell on " + this.id);
            }
        }
        return RandomCell;
    }

    public String posToString(){
        if(this.position != null){
            return this.position.getPosX() +" " + this.position.getPosY();
        }
        return "Undefined mapid " + this.id;
    }

    public short getBestCellBetween(short cellId, short nextCell, List<Short> closes) {
        short bestCell = -1;
        int bestDist = 1000;
        for (int i = 1; i < 8; i += 2)
        {
            short cell = Pathfunction.computeNextCell(cellId, (byte) ((i + 2) % 8));


            if (Arrays.stream(cells).anyMatch(c -> c.getId() == cell))
                if (!closes.contains(cell))
                {
                    int dist = Pathfunction.goalDistanceScore(this, cell, nextCell);
                    if (dist <= bestDist)
                    {
                        bestCell = cell;
                        bestDist = dist;
                    }
                }
        }
        return bestCell;
    }

    public boolean isChangeZone(short cell1, short cell2)
    {
        final DofusCell cellData1 = this.getCell(cell1);
        final DofusCell cellData2 = this.getCell(cell2);
        int dif = Math.abs((Math.abs(cellData1.getFloor()) - Math.abs(cellData2.getFloor())));
        if (((!((cellData1.getMoveZone() == cellData2.getMoveZone()))) && ((dif == 0))))
        {
            return (true);
        }
        return (false);
    }

}
