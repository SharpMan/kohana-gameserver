package koh.game.actions;

import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.DofusZaap;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.TeleporterTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.utils.Enumerable;
import koh.protocol.messages.game.interactive.zaap.TeleportDestinationsListMessage;

import java.util.ArrayList;

/**
 *
 * @author Neo-Craft
 */
public class TeleporterAction extends GameAction {

    public TeleporterAction(IGameActor Actor) {
        super(GameActionTypeEnum.ZAAP, Actor);
    }

    @Override
    public void execute() {
        this.actor.send(new TeleportDestinationsListMessage(TeleporterTypeEnum.TELEPORTER_SUBWAY, mapIds(), subAreaIds(), getCost(), Enumerable.DuplicatedKey(mapIds().length, TeleporterTypeEnum.TELEPORTER_SUBWAY)));
    }

    @Override
    public void abort(Object[] Args) {
        try {
            int map = (int) Args[0];
            DofusZaap subway = DAO.getMaps().findSubWay(((Player) actor).currentMap.getSubArea().area.id, map);
            if (subway == null) {
                return;
            }
            if (((Player) actor).kamas < getCostTo(null)) {
                actor.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 6, new String[0]));
                return;
            }
            ((Player) actor).inventoryCache.substractKamas(getCostTo(null));
            ((Player) actor).teleport(map, subway.Cell);

            this.endExecute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endExecute() throws Exception {
        actor.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_TELEPORTER));
        super.endExecute();
    }

    public int[] subAreaIds() {
        ArrayList<DofusZaap> zaaps = DAO.getMaps().getSubway(((Player) actor).currentMap.getSubArea().area.id);
        if (zaaps == null) {
            return Enumerable.DuplicatedKeyInt(39, ((Player) actor).mapid);
        }
        return zaaps.stream().mapToInt(x -> x.SubArea).toArray();
    }

    public int[] mapIds() {
        ArrayList<DofusZaap> zaaps = DAO.getMaps().getSubway(((Player) actor).currentMap.getSubArea().area.id);
        if (zaaps == null) {
            return Enumerable.DuplicatedKeyInt(39, ((Player) actor).mapid);
        }
        return zaaps.stream().mapToInt(x -> x.Mapid).toArray();
    }

    public int[] getCost() {
        return Enumerable.DuplicatedKeyInt(mapIds().length, 20);
    }

    public short getCostTo(DofusMap map) {
        return 20;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }
}
