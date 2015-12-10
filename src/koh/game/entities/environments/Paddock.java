package koh.game.entities.environments;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.dao.mysql.MapDAOImpl;
import koh.game.dao.mysql.PaddockDAOImpl;
import koh.protocol.client.BufUtils;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectAddMessage;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectRemoveMessage;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.guild.GuildEmblem;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.MountInformationsForPaddock;
import koh.protocol.types.game.paddock.PaddockAbandonnedInformations;
import koh.protocol.types.game.paddock.PaddockBuyableInformations;
import koh.protocol.types.game.paddock.PaddockInformations;
import koh.protocol.types.game.paddock.PaddockItem;
import koh.protocol.types.game.paddock.PaddockPrivateInformations;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class Paddock {

    @Getter
    private int id, map;
    @Getter
    private short subArea;
    @Getter
    private boolean abandonned;
    @Getter
    private boolean loocked;
    @Getter
    private int price, maxOutDoorMount, maxItem;
    @Getter
    private PaddockItem[] items;
    @Getter
    private MountInformationsForPaddock[] mountInformationsForPaddocks;
    @Getter
    private int selledId;
    @Getter
    private String ownerName;
    @Getter
    private GuildInformations guildInfo;

    public Paddock(ResultSet result) throws SQLException {
        this.id = result.getInt("id");
        this.map = result.getInt("map");
        this.subArea = result.getShort("sub_area");
        this.abandonned = result.getBoolean("abandonned");
        this.loocked = result.getBoolean("loocked");
        this.price = result.getInt("price");
        this.maxOutDoorMount = result.getInt("max_outdoor_mount");
        this.maxItem = result.getInt("max_items");
        if (result.getBytes("items") != null) {
            IoBuffer buf = IoBuffer.wrap(result.getBytes("items"));
            this.items = new PaddockItem[buf.getInt()];
            for (int i = 0; i < this.items.length; ++i) {
                this.items[i] = new PaddockItem(buf.getInt(), buf.getInt(), new ItemDurability(buf.getShort(), buf.getShort()));
            }
            buf.clear();
        }
        if (result.getBytes("mounts_informations") != null) {
            IoBuffer buf = IoBuffer.wrap(result.getBytes("mounts_informations"));
            this.mountInformationsForPaddocks = new MountInformationsForPaddock[buf.getInt()];
            for (int i = 0; i < this.mountInformationsForPaddocks.length; ++i) {
                this.mountInformationsForPaddocks[i] = new MountInformationsForPaddock(buf.get(), BufUtils.readUTF(buf), BufUtils.readUTF(buf));
            }
            buf.clear();
        }
        if (result.getBytes("guild_informations") != null) {
            IoBuffer buf = IoBuffer.wrap(result.getBytes("guild_informations"));
            this.guildInfo = new GuildInformations(buf.getInt(), BufUtils.readUTF(buf), new GuildEmblem(buf.getInt(), buf.getInt(), buf.get(), buf.getInt()));
            buf.clear();
        }
        if (result.getString("sell_informations") != null) {
            this.selledId = Integer.parseInt(result.getString("sell_informations").split(",")[0]);
            this.ownerName = result.getString("sell_informations").split(",")[1];
        }
    }

    public PaddockInformations getInformations() {
        if (this.price == -1 && this.ownerName == null && maxOutDoorMount == 5) {
            return new PaddockInformations(this.maxOutDoorMount, this.maxItem);
        } else if (guildInfo != null) {
            return new PaddockPrivateInformations(this.maxOutDoorMount, this.maxItem, this.price, this.loocked, this.guildInfo.guildId, this.guildInfo);
        } else if (this.isAbandonned()) {
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
