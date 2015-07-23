package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import koh.game.actions.InteractiveElementAction;
import koh.game.controllers.MapController;
import koh.game.dao.AreaDAO;
import koh.game.dao.NpcDAO;
import koh.game.dao.PaddockDAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.Path;
import koh.game.fights.Fight;
import koh.game.fights.FightController;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AggressableStatusEnum;
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
import koh.protocol.messages.game.pvp.UpdateMapPlayersAgressableStatusMessage;
import koh.protocol.messages.game.pvp.UpdateSelfAgressableStatusMessage;
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
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusMap extends IWorldEventObserver implements IWorldField {

    public int Id;
    public byte Version;
    public int RelativeId;
    public byte MapType;
    public int SubAreaId;
    public int BottomNeighbourId;
    public int TopNeighbourId;
    public int LeftNeighbourId, RightNeighbourId, ShadowBonusOnEntities;
    public boolean UseLowPassFilter, UseReverb;
    public int PresetId;
    private byte[] CompressedCells, CompressedLayers;
    private String CompressedBlueCells, CompressedRedCells;
    public boolean myInitialized = false;
    private final Map<Integer, IGameActor> myGameActors = Collections.synchronizedMap(new HashMap<>());
    public StatedElement[] ElementsStated = new StatedElement[0];
    public List<InteractiveElementStruct> InteractiveElements = new ArrayList<>();
    private Map<Short, InventoryItem> DroppedItems;
    public List<HouseInformations> Houses = new ArrayList<>();
    public MapPosition Position;
    private int myNextActorId = -1;
    private Map<Integer, MapDoor> Doors;
    private FightController myFightController;

    /*After Initialize */
    public short[] BlueCells, RedCells;
    private DofusCell[] Cells;
    private Layer[] Layers;

    public void SpawnActor(IGameActor Actor) {
        if (!this.myInitialized) {
            this.Init();
        }

        if (Actor instanceof Player) {
            if (((Player) Actor).Client != null) {
                this.registerPlayer((Player) Actor);
            }
        }
        System.out.println(((GameRolePlayActorInformations) Actor.GetGameContextActorInformations(null)).toString());
        this.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) Actor.GetGameContextActorInformations(null)));
        this.myGameActors.put(Actor.ID, Actor);

        //Verif Todo    
        /*if (this.Cells.length <  Actor.CellId) {
         if (this.myCells[Actor.CellId].Walkable) {
         this.myCells[Actor.CellId].AddActor(Actor);
         } else {
         Actor.CellId = this.GetFreeCell();

         this.myCells[Actor.CellId].AddActor(Actor);
         }
         }*/
        this.Cells[Actor.Cell.Id].AddActor(Actor);

    }

    public SubArea GetSubArea() {
        if (this.SubAreaId == 0) {
            return null;
        }
        return AreaDAO.SubAreas.get(this.SubAreaId);
    }

    public Area getArea() {
        return GetSubArea() != null ? GetSubArea().area : AreaDAO.SubAreas.get(0).area;
    }

    public MapCoordinates Cordinates() {
        try {
            return new MapCoordinates(this.Position.posX, this.Position.posY);
        } catch (Exception e) {
            return new MapCoordinates(); //Dj ecT..
        }
    }

    public void DestroyActor(IGameActor Actor) {

        if (Actor instanceof Player) {
            if (((Player) Actor).Client != null) {
                this.unregisterPlayer((Player) Actor);
            }
        }

        this.myGameActors.remove(Actor.ID);
        this.sendToField(new GameContextRemoveElementMessage(Actor.ID));
        this.Cells[Actor.Cell.Id].DelActor(Actor);
    }

    public DofusCell getCell(short c) {
        try {
            return this.Cells[c];
            //return Arrays.stream(Cells).filter(x -> x.Id == c).findFirst().get();
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public StatedElement GetStatedElementById(int id) {
        return Arrays.stream(this.ElementsStated).filter(x -> x.elementId == id).findFirst().orElse(null);
    }

    public InteractiveElementStruct GetInteractiveElementStruct(int id) {
        return this.InteractiveElements.stream().filter(x -> x.elementId == id).findFirst().orElse(null);
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
            useNewSystem = /*dataMap.isUsingNewMovementSystem*/ false; //Todo repase fucking map ?
            cellId = MapPoint.fromCoords(x, y).get_cellId();
            cellData = this.Cells[cellId];
            mov = cellData.Mov() /*&& (((!(this.isInFight)) || (!(cellData.nonWalkableDuringFight)))))*/;

            if (((((((mov) && (useNewSystem))) && (!((previousCellId == -1))))) && (!((previousCellId == cellId))))) {
                previousCellData = Cells[previousCellId];
                dif = Math.abs((Math.abs(cellData.Floor) - Math.abs(previousCellData.Floor)));
                if (((((!((previousCellData.MoveZone == cellData.MoveZone))) && ((dif > 0)))) || ((((((previousCellData.MoveZone == cellData.MoveZone)) && ((cellData.MoveZone == 0)))) && ((dif > TOLERANCE_ELEVATION)))))) {
                    mov = false;
                }
            }
            /*if (!(bAllowTroughEntity))
             {
             cellEntities = EntitiesManager.getInstance().getEntitiesOnCell(cellId, IObstacle);
             if (cellEntities.length)
             {
             for each (o in cellEntities)
             {
             if ((((endCellId == cellId)) && (o.canWalkTo())))
             {
             }
             else
             {
             if (!(o.canWalkThrough()))
             {
             return (false);
             };
             };
             };
             };
             };*/
        } else {
            mov = false;
        }
        return (mov);
    }

    public UpdateMapPlayersAgressableStatusMessage GetAgressableActorsStatus(Player Character) {
        synchronized (this.myGameActors) {
            return new UpdateMapPlayersAgressableStatusMessage(this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> x.ID).toArray(), this.myGameActors.values().stream().filter(x -> x instanceof Player).mapToInt(x -> ((Player) x).PvPEnabled).toArray());
        }
    }

    public MapComplementaryInformationsDataMessage GetMapComplementaryInformationsDataMessage(Player character) {
        return new MapComplementaryInformationsDataMessage(SubAreaId, Id, Houses, this.myGameActors.values().stream().filter(x -> x.CanBeSee(character)).map(x -> (GameRolePlayActorInformations) x.GetGameContextActorInformations(character)).collect(Collectors.toList()), this.toInteractiveElements(character), ElementsStated, new MapObstacle[0], new FightCommonInformations[0]);
    }

    public int getCellSpeed(int cellId) {
        return this.Cells[(short) cellId].Speed;
    }

    public synchronized void Init() {
        if (this.myInitialized) {
            return;
        }
        this.myInitialized = true;
        //this.Cells = MapController.UnCompressCells(this, CompressedCells);
        IoBuffer buf = IoBuffer.wrap(CompressedCells);
        this.Cells = new DofusCell[buf.getInt()];
        for (short i = 0; i < this.Cells.length; i++) {
            this.Cells[i] = new DofusCell(this, i, buf);
        }
        buf.clear();
        buf = IoBuffer.wrap(CompressedLayers);
        this.Layers = new Layer[buf.getInt()];
        for (short i = 0; i < this.Layers.length; i++) {
            this.Layers[i] = new Layer(this, buf);
        }
        buf.clear();
        this.BlueCells = MapController.UnCompressStartingCells(this, CompressedBlueCells);
        this.RedCells = MapController.UnCompressStartingCells(this, CompressedRedCells);
        //StatedElement[]
        //this.ElementsStated = this.ElementsList.stream().map(x -> new StatedElement(x.ID, x.ElementCellID, x.State)).collect(Collectors.toList()).toArray(new StatedElement[this.ElementsList.size()]);
        this.CompressedCells = null;
        this.CompressedLayers = null;
        this.CompressedBlueCells = null;
        this.CompressedRedCells = null;
        if (NpcDAO.Npcs.containsKey(this.Id)) {
            for (Npc npc : NpcDAO.Npcs.get(this.Id)) {
                npc.ID = this.myNextActorId--;
                npc.Cell = this.getCell(npc.CellID);
                this.SpawnActor(npc);
            }
        }
        this.myFightController = new FightController();
        //this.InteractiveElements.forEach(x -> System.out.println(x.toString()));
    }

    public DofusMap(int id, byte v, int r, byte m, int SubAreaId, int bn, int tn, int ln, int rn, int sb, boolean u, boolean ur, int pr, String cc, String sl, byte[] sbb, byte[] rc) {
        this.Id = id;
        this.Version = v;
        this.RelativeId = r;
        this.MapType = m;
        this.SubAreaId = SubAreaId;
        //Todo SubArea Instance
        this.BottomNeighbourId = bn;
        this.TopNeighbourId = tn;
        this.LeftNeighbourId = ln;
        this.RightNeighbourId = rn;
        this.ShadowBonusOnEntities = sb;
        this.UseLowPassFilter = u;
        this.UseReverb = ur;
        this.PresetId = pr;
        this.CompressedCells = sbb;
        this.CompressedLayers = rc;
        this.CompressedBlueCells = cc;
        this.CompressedRedCells = sl;
    }

    public static final ScheduledExecutorService GlobalTimer = Executors.newScheduledThreadPool(20);

    @Override
    public void ActorMoved(Path Path, IGameActor Actor, short newCell, byte newDirection) {
        this.Cells[Actor.Cell.Id].DelActor(Actor);
        this.Cells[newCell].AddActor(Actor);
        if (newDirection != -1) {
            Actor.Direction = newDirection;
        }
    }

    public void OnMouvementConfirmed(Player Actor) {
        if (Actor.Client.onMouvementConfirm != null) {
            Actor.Client.onMouvementConfirm.Apply(Actor);
            Actor.Client.onMouvementConfirm = null;
        }
        if (DroppedItems == null || DroppedItems.isEmpty()) {
            return;
        }
        synchronized (DroppedItems) {
            if (DroppedItems.containsKey(Actor.Cell.Id)) {
                Actor.InventoryCache.Add(DroppedItems.get(Actor.Cell.Id), true);
                DroppedItems.remove(Actor.Cell.Id);
                this.sendToField(new ObjectGroundRemovedMessage(Actor.Cell.Id));
                if (DroppedItems.isEmpty()) {
                    this.DroppedItems = null;
                }
            }
        }
    }

    public synchronized void ClearDroppedItems() {
        if (DroppedItems == null) {
            return;
        }
        synchronized (this.DroppedItems) {
            this.sendToField(new ObjectGroundRemovedMultipleMessage(DroppedItems.keySet().toArray(new Short[DroppedItems.size()])));
            this.DroppedItems.clear();
            this.DroppedItems = null;
        }
    }

    public InteractiveElement[] toInteractiveElements(Player Actor) {
        return this.InteractiveElements.stream().map(Element -> new InteractiveElementWithAgeBonus(Element.elementId,
                Element.elementTypeId,
                Element.Skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, Actor) && StaticElementIsOpened(Element.elementId)).toArray(InteractiveElementSkill[]::new),
                Element.Skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, Actor) && StaticElementIsOpened(Element.elementId))).toArray(InteractiveElementSkill[]::new),
                Element.AgeBonus)
        ).toArray(InteractiveElement[]::new);
    }

    public InteractiveElement toInteractiveElement(Player Actor, int elementId) {
        InteractiveElementStruct Struct = this.GetInteractiveElementStruct(elementId);
        return new InteractiveElementWithAgeBonus(Struct.elementId,
                Struct.elementTypeId,
                Struct.Skills.stream().filter(Skill -> InteractiveElementAction.canDoAction(Skill.skillId, Actor) && StaticElementIsOpened(Struct.elementId)).toArray(InteractiveElementSkill[]::new),
                Struct.Skills.stream().filter(Skill -> !(InteractiveElementAction.canDoAction(Skill.skillId, Actor) && StaticElementIsOpened(Struct.elementId))).toArray(InteractiveElementSkill[]::new),
                Struct.AgeBonus
        );
    }

    public boolean StaticElementIsOpened(int elementId) {
        return !(this.GetStatedElementById(elementId) != null && GetStatedElementById(elementId).elementState > 0);
    }

    public boolean HasActorOnCell(short cell) {
        return this.myGameActors.values().stream().filter(x -> x.Cell != null && x.Cell.Id == cell).count() > 0;
    }

    public Npc GetNpc(int contextualid) {
        return (Npc) this.myGameActors.get(contextualid);
    }

    private final static Random RANDOM = new Random();

    public short getRandomCell() {
        return this.Cells[RANDOM.nextInt(this.Cells.length)].Id;
        //return this.myCells.Keys.FirstOrDefault(x => x == RANDOM.Next(this.myCells.Keys.Count));
    }

    public synchronized DofusCell GetAnyCellWalakable() {
        this.Init();
        return Arrays.stream(this.Cells).filter(x -> x.Mov()).findAny().orElse(this.Cells[0]);
    }

    public short getRandomWalkableCell() {
        short cell = getRandomCell();
        if (Cells[cell].Mov()) {
            return cell;
        } else {
            return getRandomWalkableCell();
        }
    }

    public void addDoor(MapDoor d) {
        if (this.Doors == null) {
            this.Doors = new HashMap();
        }
        this.Doors.put(d.ElementID, d);
    }

    public MapDoor getDoor(int id) {
        return this.Doors.get(id);
    }

    public boolean CellIsOccuped(short cell) {
        if (DroppedItems == null) {
            return false;
        }
        synchronized (this.DroppedItems) {
            return this.DroppedItems != null && this.DroppedItems.containsKey(cell);
        }
    }

    public void AddItem(Short cell, InventoryItem Item) {
        if (this.DroppedItems == null) {
            DroppedItems = Collections.synchronizedMap(new HashMap<>());
        }
        this.DroppedItems.put(cell, Item);
        this.sendToField(new ObjectGroundAddedMessage(cell, Item.TemplateId));
    }

    public ObjectGroundListAddedMessage ObjectsGround() {
        if (this.DroppedItems == null || this.DroppedItems.size() <= 0) {
            return null;
        }
        return new ObjectGroundListAddedMessage(DroppedItems.keySet().toArray(new Short[DroppedItems.size()]), DroppedItems.values().stream().mapToInt(x -> x.TemplateId).toArray());
    }

    public IGameActor GetActor(int target) {
        if (!myGameActors.containsKey(target)) {
            return null;
        }
        return this.myGameActors.get(target);
    }

    public Player GetPlayer(int target) {
        if (!myGameActors.containsKey(target)) {
            return null;
        }
        return (Player) this.myGameActors.get(target);
    }

    public Integer PlayersCount() {
        return (int) this.myGameActors.values().stream().filter(x -> x instanceof Player).count();
    }

    public short NextFightId() {
        return (short) this.myFightController.NextFightId();
    }

    public void AddFight(Fight Fight) {
        this.myFightController.AddFight(Fight);

        this.SendMapFightCountMessage();
    }

    public void RemoveFight(Fight Fight) {
        this.myFightController.RemoveFight(Fight);

        this.SendMapFightCountMessage();
    }

    public void SendMapInfo(WorldClient Client) {
        this.myFightController.SendFightInfos(Client);
        if (PaddockDAO.Cache.containsKey(Id)) {
            Client.Send(new PaddockPropertiesMessage(PaddockDAO.Cache.get(Id).Informations()));
            if (PaddockDAO.Cache.get(Id).Items != null) {
                Client.Send(new GameDataPaddockObjectListAddMessage(PaddockDAO.Cache.get(Id).Items));
            }
        }
        Client.Send(GetAgressableActorsStatus(Client.Character));
        // Client.Send(new UpdateSelfAgressableStatusMessage(Client.Character.PvPEnabled,(int)System.currentTimeMillis() + 360000));
        Message Items = Client.Character.CurrentMap.ObjectsGround();
        if (Items != null) {
            Client.Send(Items);
        }
    }

    public void SendMapFightCountMessage() {
        this.sendToField(new MapFightCountMessage(this.myFightController.FightCount()));
    }

    public Fight GetFight(int FightId) {
        return this.myFightController.GetFight(FightId);
    }

    public List<Fight> GetFights() {
        return this.myFightController.Fights();
    }

    public DofusCell[] GetCells() {
        return this.Cells;
    }

    public DofusCell GetRandomAdjacentFreeCell(short Id) {
        DofusCell RandomCell = this.getCell((short) MapPoint.fromCellId(Id).getNearestFreeCell(this).get_cellId());
        if (RandomCell == null || !RandomCell.Mov()) {
            for (int i = 0; i < 8; i++) {
                RandomCell = this.getCell((short) MapPoint.fromCellId(Id).getNearestFreeCellInDirection(i, this, false).get_cellId());
                if (RandomCell != null && RandomCell.Mov()) {
                    continue;
                }
            }
            if (RandomCell == null) {
                throw new Error("Can't find cell on " + this.Id);
            }
        }
        return RandomCell;
    }

}
