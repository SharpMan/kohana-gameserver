package koh.game.actions;

import koh.game.dao.mysql.MapDAOImpl;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.MapPosition;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.TeleporterTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.utils.Enumerable;
import koh.protocol.messages.game.interactive.zaap.ZaapListMessage;

/**
 *
 * @author Neo-Craft
 */
public class ZaapAction extends GameAction {

    public ZaapAction(IGameActor Actor) {
        super(GameActionTypeEnum.ZAAP, Actor);
    }

    @Override
    public void Execute() {
        //Stream<Entry<Integer, DofusZaap>> zaaps = MapDAOImpl.zaaps.entrySet().stream().filter(x -> x.getValue().Mapid != ((Player) Actor).CurrentMap.Id);
        this.Actor.Send(new ZaapListMessage(TeleporterTypeEnum.TELEPORTER_ZAAP, mapIds(), subAreaIds(), Costs(), Enumerable.DuplicatedKey(MapDAOImpl.zaaps.size() - 1, TeleporterTypeEnum.TELEPORTER_ZAAP), ((Player) Actor).CurrentMap.Id));
    }

    @Override
    public void Abort(Object[] Args) {
        try {
            int map = (int) Args[0];
            if (!MapDAOImpl.zaaps.containsKey(map)) {
                return;
            }
            if (((Player) Actor).Kamas < GetCostTo(MapDAOImpl.zaaps.get(map).Map())) {
                Actor.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 6, new String[0]));
                return;
            }
            ((Player) Actor).InventoryCache.SubstractKamas(GetCostTo(MapDAOImpl.zaaps.get(map).Map()));
            ((Player) Actor).teleport(map, MapDAOImpl.zaaps.get(map).Cell);

            this.EndExecute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void EndExecute() throws Exception {
        Actor.Send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_TELEPORTER));
        super.EndExecute();
    }

    public int[] subAreaIds() {
        return MapDAOImpl.zaaps.entrySet().stream().filter(x -> x.getValue().Mapid != ((Player) Actor).CurrentMap.Id).mapToInt(x -> x.getValue().Map().SubAreaId).toArray();
    }

    public int[] mapIds() {
        return MapDAOImpl.zaaps.entrySet().stream().filter(x -> x.getValue().Mapid != ((Player) Actor).CurrentMap.Id).mapToInt(x -> x.getKey()).toArray();
    }

    public int[] Costs() {
        int[] Cost = new int[MapDAOImpl.zaaps.size() - 1];
        int i = 0;
        for (DofusMap zaap : MapDAOImpl.zaaps.entrySet().stream().filter(x -> x.getValue().Mapid != ((Player) Actor).CurrentMap.Id).map(x -> x.getValue().Map()).toArray(DofusMap[]::new)) {
            Cost[i] = GetCostTo(zaap);
            i++;
        }
        return Cost;
    }

    public short GetCostTo(DofusMap map) {
        MapPosition position1 = map.Position;
        MapPosition position2 = ((Player) Actor).CurrentMap.Position;
        return (short) Math.floor(Math.sqrt((double) ((position2.posX - position1.posX) * (position2.posX - position1.posX) + (position2.posY - position1.posY) * (position2.posY - position1.posY))) * 10.0);
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
