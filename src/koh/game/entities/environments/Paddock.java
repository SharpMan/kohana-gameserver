package koh.game.entities.environments;

import java.util.Arrays;
import koh.game.dao.MapDAO;
import koh.game.dao.PaddockDAO;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectAddMessage;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectRemoveMessage;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.mount.ItemDurability;
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

    public int Id, Map;
    public short SubArea;
    public boolean Abandonned;
    public boolean Loocked;
    public int Price, MaxOutDoorMount, MaxItem;
    public PaddockItem[] Items;
    public MountInformationsForPaddock[] MountInformations;
    public int SelledId;
    public String OwnerName;
    public GuildInformations guildInfo;

    public PaddockInformations Informations() {
        if (this.Price == -1 && this.OwnerName == null && MaxOutDoorMount == 5) {
            return new PaddockInformations(this.MaxOutDoorMount, this.MaxItem);
        } else if (guildInfo != null) {
            return new PaddockPrivateInformations(this.MaxOutDoorMount, this.MaxItem, this.Price, this.Loocked, this.guildInfo.guildId, this.guildInfo);
        } else if (this.Abandonned) {
            return new PaddockAbandonnedInformations(this.MaxOutDoorMount, this.MaxItem, this.Price, this.Loocked, 0);
        } else if (Price > 0) {
            return new PaddockBuyableInformations(this.MaxOutDoorMount, this.MaxItem, this.Price, this.Loocked);
        } else {
            return new PaddockAbandonnedInformations(this.MaxOutDoorMount, this.MaxItem, this.Price, this.Loocked, 0);
        }
    }

    public void AddPaddockItem(PaddockItem Item) {
        if (this.Items == null) {
            this.Items = new PaddockItem[0];
        }
        this.Items = ArrayUtils.add(Items, Item);
        PaddockDAO.Update(this, new String[]{"items"});
        MapDAO.Cache.get(this.Map).sendToField(new GameDataPaddockObjectAddMessage(Item));

    }

    public PaddockItem GetItem(int cell) {
        try {
            return Arrays.stream(this.Items).filter(x -> x.cellId == cell).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public void RemovePaddockItem(int Cell) {
        if (this.Items == null) {
            this.Items = new PaddockItem[0];
        } else if (GetItem(Cell) == null) {
            return;
        }

        this.Items = ArrayUtils.removeElement(Items, GetItem(Cell));
        PaddockDAO.Update(this, new String[]{"items"});
        MapDAO.Cache.get(this.Map).sendToField(new GameDataPaddockObjectRemoveMessage(Cell));

    }

}
