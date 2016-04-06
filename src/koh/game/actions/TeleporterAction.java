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
        this.actor.send(new TeleportDestinationsListMessage(TeleporterTypeEnum.TELEPORTER_SUBWAY, mapIds(), subAreaIds(), getCost(), Enumerable.duplicatedKey(mapIds().length, TeleporterTypeEnum.TELEPORTER_SUBWAY)));
    }

    @Override
    public void abort(Object[] args) {
        try {
            if(args.length > 0) {
                int map = (int) args[0];
                final DofusZaap subway = DAO.getMaps().findSubWay(((Player) actor).getCurrentMap().getSubArea().getArea().getId(), map);
                if (subway == null) {
                    return;
                }
                if (((Player) actor).getKamas() < getCostTo(null)) {
                    actor.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 6, new String[0]));
                    return;
                }
                ((Player) actor).getInventoryCache().substractKamas(getCostTo(null));
                ((Player) actor).teleport(map, subway.getCell());
            }

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
        ArrayList<DofusZaap> zaaps = DAO.getMaps().getSubway(((Player) actor).getCurrentMap().getSubArea().getArea().getId());
        if (zaaps == null) {
            return Enumerable.duplicatedKeyInt(39, ((Player) actor).getMapid());
        }
        return zaaps.stream().mapToInt(x -> x.getSubArea()).toArray();
    }

    public int[] mapIds() {
        ArrayList<DofusZaap> zaaps = DAO.getMaps().getSubway(((Player) actor).getCurrentMap().getSubArea().getArea().getId());
        if (zaaps == null) {
            return Enumerable.duplicatedKeyInt(39, ((Player) actor).getMapid());
        }
        return zaaps.stream().mapToInt(x -> x.getMapid()).toArray();
    }

    public int[] getCost() {
        return Enumerable.duplicatedKeyInt(mapIds().length, 20);
    }

    public short getCostTo(DofusMap map) {
        return 20;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }
}
