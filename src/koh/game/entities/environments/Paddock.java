package koh.game.entities.environments;

import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.dao.mysql.MapDAOImpl;
import koh.game.dao.mysql.PaddockDAOImpl;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectAddMessage;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectRemoveMessage;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.paddock.MountInformationsForPaddock;
import koh.protocol.types.game.paddock.PaddockAbandonnedInformations;
import koh.protocol.types.game.paddock.PaddockBuyableInformations;
import koh.protocol.types.game.paddock.PaddockInformations;
import koh.protocol.types.game.paddock.PaddockItem;
import koh.protocol.types.game.paddock.PaddockPrivateInformations;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class Paddock {

    public int id, map;
    public short subArea;
    public boolean abandonned;
    public boolean loocked;
    public int price, maxOutDoorMount, maxItem;
    public PaddockItem[] items;
    public MountInformationsForPaddock[] mountInformationsForPaddocks;
    public int selledId;
    public String ownerName;
    public GuildInformations guildInfo;

    public PaddockInformations getInformations() {
        if (this.price == -1 && this.ownerName == null && maxOutDoorMount == 5) {
            return new PaddockInformations(this.maxOutDoorMount, this.maxItem);
        } else if (guildInfo != null) {
            return new PaddockPrivateInformations(this.maxOutDoorMount, this.maxItem, this.price, this.loocked, this.guildInfo.guildId, this.guildInfo);
        } else if (this.abandonned) {
            return new PaddockAbandonnedInformations(this.maxOutDoorMount, this.maxItem, this.price, this.loocked, 0);
        } else if (price > 0) {
            return new PaddockBuyableInformations(this.maxOutDoorMount, this.maxItem, this.price, this.loocked);
        } else {
            return new PaddockAbandonnedInformations(this.maxOutDoorMount, this.maxItem, this.price, this.loocked, 0);
        }
    }

    public void addPaddockItem(PaddockItem Item) {
        if (this.items == null) {
            this.items = new PaddockItem[0];
        }
        this.items = ArrayUtils.add(items, Item);
        DAO.getPaddocks().update(this, new String[]{"items"});
        DAO.getMaps().findTemplate(this.map).sendToField(new GameDataPaddockObjectAddMessage(Item));

    }

    public PaddockItem getItem(int cell) {
        return Arrays.stream(this.items).filter(x -> x.cellId == cell).findFirst().orElse(null);
    }

    public void removePaddockItem(int Cell) {
        if (this.items == null) {
            this.items = new PaddockItem[0];
        } else if (getItem(Cell) == null) {
            return;
        }

        this.items = ArrayUtils.removeElement(items, getItem(Cell));
        DAO.getPaddocks().update(this, new String[]{"items"});
        DAO.getMaps().findTemplate(this.map).sendToField(new GameDataPaddockObjectRemoveMessage(Cell));

    }

}
