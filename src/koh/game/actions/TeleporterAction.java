package koh.game.actions;

import koh.game.dao.MapDAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.DofusMap;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.TeleporterTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.utils.Enumerable;
import messages.game.interactive.zaap.TeleportDestinationsListMessage;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class TeleporterAction extends GameAction {

    public TeleporterAction(IGameActor Actor) {
        super(GameActionTypeEnum.ZAAP, Actor);
    }

    @Override
    public void Execute() {
        this.Actor.Send(new TeleportDestinationsListMessage(TeleporterTypeEnum.TELEPORTER_SUBWAY, mapIds(), subAreaIds(), Costs(), Enumerable.DuplicatedKey(mapIds().length, TeleporterTypeEnum.TELEPORTER_SUBWAY)));
    }

    @Override
    public void Abort(Object[] Args) {
        try {
            int map = (int) Args[0];
            if (MapDAO.GetSubWay(((Player) Actor).CurrentMap.GetSubArea().area.id, map) == null) {
                return;
            }
            if (((Player) Actor).Kamas < GetCostTo(null)) {
                Actor.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 6, new String[0]));
                return;
            }
            ((Player) Actor).InventoryCache.SubstractKamas(GetCostTo(null));
            ((Player) Actor).teleport(map, MapDAO.GetSubWay(((Player) Actor).CurrentMap.GetSubArea().area.id, map).Cell);

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
        if (!MapDAO.SubWays.containsKey(((Player) Actor).CurrentMap.GetSubArea().area.id)) {
            return Enumerable.DuplicatedKeyInt(39, ((Player) Actor).Mapid);
        }
        return MapDAO.SubWays.get(((Player) Actor).CurrentMap.GetSubArea().area.id).stream().mapToInt(x -> x.SubArea).toArray();
    }

    public int[] mapIds() {
        if (!MapDAO.SubWays.containsKey(((Player) Actor).CurrentMap.GetSubArea().area.id)) {
            return Enumerable.DuplicatedKeyInt(39, ((Player) Actor).Mapid);
        }
        return MapDAO.SubWays.get(((Player) Actor).CurrentMap.GetSubArea().area.id).stream().mapToInt(x -> x.Mapid).toArray();
    }

    public int[] Costs() {
        return Enumerable.DuplicatedKeyInt(mapIds().length, 20);
    }

    public short GetCostTo(DofusMap map) {
        return 20;
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }
}
