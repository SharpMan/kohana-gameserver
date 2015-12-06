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
import koh.game.actions.InteractiveElementAction;
import koh.game.controllers.MapController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.Path;
import koh.game.fights.Fight;
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
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusMap extends IWorldEventObserver implements IWorldField {

    public int id;
    public byte version;
    public int relativeId;
    public byte mapType;
    public int subAreaId;
    public int bottomNeighbourId;
    public int topNeighbourId;
    public int leftNeighbourId, rightNeighbourId, shadowBonusOnEntities;
    public NeighBourStruct[] newNeighbour;
    public boolean useLowPassFilter, useReverb;
    public int presetId;
    private byte[] compressedCells, compressedLayers;
    private String compressedBlueCells, compressedRedCells;
    public boolean myInitialized = false;
    private final Map<Integer, IGameActor> myGameActors = new ConcurrentHashMap<>();
    public StatedElement[] elementsStated = new StatedElement[0];
    public List<InteractiveElementStruct> interactiveElements = new ArrayList<>();
    private Map<Short, InventoryItem> droppedItems;
    public List<HouseInformations> houses = new ArrayList<>();
    public MapPosition position;
    private int myNextActorId = -1;
    private Map<Integer, MapDoor> doors;
    private FightController myFightController;
    /*After loadAll */
    public short[] blueCells, redCells;
    private DofusCell[] cells;
    private Layer[] layers;

    public void spawnActor(IGameActor actor) {
        if (!this.myInitialized) {
            this.Init();
        }

        if (actor instanceof Player) {
            if (((Player) actor).client != null) {
                this.registerPlayer((Player) actor);
                this.onPlayerSpawned((Player) actor);
            }
        }
        this.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) actor.getGameContextActorInformations(null)));

        this.myGameActors.put(actor.ID, actor);

        //verify Todo
        /*if (this.cells.length <  actor.getCellId) {
         if (this.myCells[actor.getCellId].Walkable) {
         this.myCells[actor.getCellId].addActor(actor);
         } else {
         actor.getCellId = this.GetFreeCell();

         this.myCells[actor.getCellId].addActor(actor);
         }
         }*/
        this.cells[actor.cell.id].addActor(actor);

    }

    public void onPlayerSpawned(Player actor) {
        if (this.id == 115083777) {
            actor.send(new PopupWarningMessage((byte) 5, "MØ", "Pour consulter les vendeurs , parlez à Hal San !"));
        }
    }

    public SubArea getSubArea() {
        if (this.subAreaId == 0) {
            return null;
        }
        return DAO.getAreas().getSubArea(this.subAreaId);
    }

    public Area getArea() {
        return getSubArea() != null ? getSubArea().area : DAO.getAreas().getSubArea(0).area;
    }

    public MapCoordinates coordinates() {
        try {
            return new MapCoordinates(this.position.posX, this.position.posY);
        } catch (Exception e) {
            return new MapCoordinates(); //Dj ecT..
        }
    }

    public void destroyActor(IGameActor actor) {

        if (actor instanceof Player) {
            if (((Player) actor).client != null) {
                this.unregisterPlayer((Player) actor);
            }
        }

        this.myGameActors.remove(actor.ID);
        this.sendToField(new GameContextRemoveElementMessage(actor.ID));
        this.cells[actor.cell.id].delActor(actor);
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
            useNewSystem =  this.isUsingNewMovementSystem;
            cellId = MapPoint.fromCoords(x, y).get_cellId();
            cellData = this.cells[cellId];
            mov = cellData.mov() && !cellData.nonWalkableDuringFight()/*&& (((!(this.isInFight)) || (!(cellData.nonWalkableDuringFight)))))*/;

            if (((((((mov) && (useNewSystem))) && (!((previousCellId == -1))))) && (!((previousCellId == cellId))))) {
                previousCellData = cells[previousCellId];
                dif = Math.abs((Math.abs(cellData.floor) - Math.abs(previousCellData.floor)));
                if (((((!((previousCellData.moveZone == cellData.moveZone))) && ((dif > 0)))) || ((((((previousCellData.moveZone == cellData.moveZone)) && ((cellData.moveZone == 0)))) && ((dif > TOLERANCE_ELEVATION)))))) {
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
            return new UpdateMapPlayersAgressableStatusMessage(this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> x.ID).toArray(), this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> ((Player) x).PvPEnabled).toArray());
        }
    }

    public MapComplementaryInformationsDataMessage getMapComplementaryInformationsDataMessage(Player character) {
        return new MapComplementaryInformationsDataMessage(subAreaId, id, houses, this.myGameActors.values().stream().filter(x -> x.canBeSee(character)).map(x -> (GameRolePlayActorInformations) x.getGameContextActorInformations(character)).collect(Collectors.toList()), this.toInteractiveElements(character), elementsStated, new MapObstacle[0], new FightCommonInformations[0]);
    }

    public int getCellSpeed(int cellId) {
        return this.cells[(short) cellId].speed;
    }
    
    private boolean isUsingNewMovementSystem = false;

    public synchronized void Init() {
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
                _oldMvtSystem = this.cells[i].moveZone;
            }
            if(this.cells[i].moveZone != _oldMvtSystem){
               this.isUsingNewMovementSystem = true;
            }
        }
        buf.clear();
        buf = IoBuffer.wrap(compressedLayers);
        this.layers = new Layer[buf.getInt()];
        for (short i = 0; i < this.layers.length; i++) {
            this.layers[i] = new Layer(this, buf);
        }
        buf.clear();
        this.blueCells = MapController.unCompressStartingCells(this, compressedBlueCells);
        this.redCells = MapController.unCompressStartingCells(this, compressedRedCells);
        //StatedElement[]
        //this.elementsStated = this.ElementsList.stream().map(x -> new StatedElement(x.id, x.ElementCellID, x.State)).collect(Collectors.toList()).toArray(new StatedElement[this.ElementsList.size()]);
        this.compressedCells = null;
        this.compressedLayers = null;
        this.compressedBlueCells = null;
        this.compressedRedCells = null;
        //throw un nullP mieux que referencer une liste on 111000 map object
        try {
            for (Npc npc : DAO.getNpcs().forMap(this.id)) {
                    npc.ID = this.myNextActorId--;
                    npc.cell = this.getCell(npc.cellID);
                    this.spawnActor(npc);
            }
        } catch(NullPointerException ignored) {}
        this.myFightController = new FightController();
        //this.interactiveElements.forEach(x -> System.out.println(x.toString()));
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
        this.compressedBlueCells = cc;
        this.compressedRedCells = sl;
    }

    public static final ScheduledExecutorService GlobalTimer = Executors.newScheduledThreadPool(20);

    @Override
    public void actorMoved(Path path, IGameActor actor, short newCell, byte newDirection) {
        this.cells[actor.cell.id].delActor(actor);
        this.cells[newCell].addActor(actor);
        if (newDirection != -1) {
            actor.direction = newDirection;
        }
    }

    public void onMouvementConfirmed(Player Actor) {
        if (Actor.client.onMouvementConfirm != null) {
            Actor.client.onMouvementConfirm.apply(Actor);
            Actor.client.onMouvementConfirm = null;
        }
        if (droppedItems == null || droppedItems.isEmpty()) {
            return;
        }
        synchronized (droppedItems) {
            if (droppedItems.containsKey(Actor.cell.id)) {
                Actor.inventoryCache.add(droppedItems.get(Actor.cell.id), true);
                droppedItems.remove(Actor.cell.id);
                this.sendToField(new ObjectGroundRemovedMessage(Actor.cell.id));
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

    public InteractiveElement[] toInteractiveElements(Player Actor) {
        return this.interactiveElements.stream().map(Element -> new InteractiveElementWithAgeBonus(Element.elementId,
                Element.elementTypeId,
                Element.skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, Actor) && staticElementIsOpened(Element.elementId)).toArray(InteractiveElementSkill[]::new),
                Element.skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, Actor) && staticElementIsOpened(Element.elementId))).toArray(InteractiveElementSkill[]::new),
                Element.ageBonus)
        ).toArray(InteractiveElement[]::new);
    }

    public InteractiveElement toInteractiveElement(Player Actor, int elementId) {
        InteractiveElementStruct Struct = this.getInteractiveElementStruct(elementId);
        return new InteractiveElementWithAgeBonus(Struct.elementId,
                Struct.elementTypeId,
                Struct.skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, Actor) && staticElementIsOpened(Struct.elementId)).toArray(InteractiveElementSkill[]::new),
                Struct.skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, Actor) && staticElementIsOpened(Struct.elementId))).toArray(InteractiveElementSkill[]::new),
                Struct.ageBonus
        );
    }

    public boolean staticElementIsOpened(int elementId) {
        return !(this.getStatedElementById(elementId) != null && getStatedElementById(elementId).elementState > 0);
    }

    public boolean hasActorOnCell(short cell) {
        return this.myGameActors.values().stream().filter(x -> x.cell != null && x.cell.id == cell).count() > 0;
    }

    public Npc getNpc(int contextualid) {
        return (Npc) this.myGameActors.get(contextualid);
    }

    private final static Random RANDOM = new Random();

    public short getRandomCell() {
        return this.cells[RANDOM.nextInt(this.cells.length)].id;
        //return this.myCells.Keys.FirstOrDefault(x => x == RANDOM.Next(this.myCells.Keys.Count));
    }

    public synchronized DofusCell getAnyCellWalakable() {
        this.Init();
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

    public void addDoor(MapDoor d) {
        if (this.doors == null) {
            this.doors = new HashMap();
        }
        this.doors.put(d.elementID, d);
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
        this.sendToField(new ObjectGroundAddedMessage(cell, item.templateId));
    }

    public ObjectGroundListAddedMessage objectsGround() {
        if (this.droppedItems == null || this.droppedItems.size() <= 0) {
            return null;
        }
        return new ObjectGroundListAddedMessage(droppedItems.keySet().toArray(new Short[droppedItems.size()]), droppedItems.values().stream().mapToInt(x -> x.templateId).toArray());
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


    public void sendMapInfo(WorldClient client) {
        this.myFightController.sendFightInfos(client);
        //FIXME : Dafuk am certain some maps have 2 paddocks
        Paddock paddock = DAO.getPaddocks().find(id);
        if (paddock != null) {
            client.send(new PaddockPropertiesMessage(paddock.getInformations()));
            if (paddock.items != null) {
                client.send(new GameDataPaddockObjectListAddMessage(paddock.items));
            }
        }
        client.send(getAgressableActorsStatus(client.character));
        // client.send(new UpdateSelfAgressableStatusMessage(client.character.PvPEnabled,(int)System.currentTimeMillis() + 360000));
        Message Items = client.character.currentMap.objectsGround();
        if (Items != null) {
            client.send(Items);
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

}
