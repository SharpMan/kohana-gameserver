package koh.game.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import koh.game.MySQL;
import koh.game.entities.environments.Paddock;
import koh.game.utils.Settings;
import koh.game.utils.StringUtil;
import koh.protocol.client.BufUtils;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.guild.GuildEmblem;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.MountInformationsForPaddock;
import koh.protocol.types.game.paddock.PaddockItem;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class PaddockDAO {

    public static Map<Integer, Paddock> Cache = new HashMap<>();

    public static boolean Update(Paddock Item, String[] Columns) {
        try {
            int i = 1;
            String Query = "UPDATE `paddocks_template` set ";
            Query = Arrays.stream(Columns).map((s) -> s + " =?,").reduce(Query, String::concat);
            Query = StringUtil.removeLastChar(Query);
            Query += " WHERE id = ?;";

            PreparedStatement p = MySQL.prepareQuery(Query, MySQL.Connection());

            for (String s : Columns) {
                setValue(p, s, i++, Item);
            }
            setValue(p, "id", i++, Item);

            p.execute();
            MySQL.closePreparedStatement(p);
            Columns = null;

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setValue(PreparedStatement p, String Column, int Seq, Paddock Item) {
        try {
            IoBuffer buf;
            switch (Column) {
                case "id":
                    p.setInt(Seq, Item.Id);
                    break;
                case "abandonned":
                    p.setBoolean(Seq, Item.Abandonned);
                    break;
                case "loocked":
                    p.setBoolean(Seq, Item.Loocked);
                    break;
                case "mounts_informations":
                    buf = SerializeMountsInformations(Item.MountInformations);
                    p.setBytes(Seq, buf.array());
                    buf.clear();
                    break;
                case "items":
                    buf = SerializeItemsInformations(Item.Items);
                    p.setBytes(Seq, buf.array());
                    buf.clear();
                    break;
                case "guild_informations":
                    buf = SerializeGuildInformations(Item.guildInfo);
                    p.setBytes(Seq, buf.array());
                    buf.clear();
                    break;
                case "sell_informations":
                    p.setString(Seq, Item.SelledId + "," + Item.OwnerName);
                    break;
                case "price":
                    p.setInt(Seq, Item.Price);
                    break;
                case "max_outdoor_mount":
                    p.setInt(Seq, Item.MaxOutDoorMount);
                    break;
                case "max_items":
                    p.setInt(Seq, Item.MaxItem);
                    break;

            }
            buf = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IoBuffer SerializeGuildInformations(GuildInformations m) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);
        buff.putInt(m.guildId);
        BufUtils.writeUTF(buff, m.guildName);
        buff.putInt(m.guildEmblem.symbolShape);
        buff.putInt(m.guildEmblem.symbolColor);
        buff.put(m.guildEmblem.backgroundShape);
        buff.putInt(m.guildEmblem.backgroundColor);
        buff.flip();
        return buff;
    }

    public static IoBuffer SerializeMountsInformations(MountInformationsForPaddock[] M) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(M.length);
        for (MountInformationsForPaddock e : M) {
            buff.put(e.modelId);
            BufUtils.writeUTF(buff, e.name);
            BufUtils.writeUTF(buff, e.ownerName);
        }

        buff.flip();
        return buff;
    }

    public static IoBuffer SerializeItemsInformations(PaddockItem[] Items) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(Items.length);
        for (PaddockItem e : Items) {
            buff.putInt(e.cellId);
            buff.putInt(e.objectGID);
            buff.putShort(e.durability.durability);
            buff.putShort(e.durability.durabilityMax);
        }

        buff.flip();
        return buff;
    }

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from paddocks_template", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("map"), new Paddock() {
                    {
                        this.Id = RS.getInt("id");
                        this.Map = RS.getInt("map");
                        this.SubArea = RS.getShort("sub_area");
                        this.Abandonned = RS.getBoolean("abandonned");
                        this.Loocked = RS.getBoolean("loocked");
                        this.Price = RS.getInt("price");
                        this.MaxOutDoorMount = RS.getInt("max_outdoor_mount");
                        this.MaxItem = RS.getInt("max_items");
                        if (RS.getBytes("items") != null) {
                            IoBuffer buf = IoBuffer.wrap(RS.getBytes("items"));
                            this.Items = new PaddockItem[buf.getInt()];
                            for (int i = 0; i < this.Items.length; ++i) {
                                this.Items[i] = new PaddockItem(buf.getInt(), buf.getInt(), new ItemDurability(buf.getShort(), buf.getShort()));
                            }
                            buf.clear();
                        }
                        if (RS.getBytes("mounts_informations") != null) {
                            IoBuffer buf = IoBuffer.wrap(RS.getBytes("mounts_informations"));
                            this.MountInformations = new MountInformationsForPaddock[buf.getInt()];
                            for (int i = 0; i < this.MountInformations.length; ++i) {
                                this.MountInformations[i] = new MountInformationsForPaddock(buf.get(), BufUtils.readUTF(buf), BufUtils.readUTF(buf));
                            }
                            buf.clear();
                        }
                        if (RS.getBytes("guild_informations") != null) {
                            IoBuffer buf = IoBuffer.wrap(RS.getBytes("guild_informations"));
                            this.guildInfo = new GuildInformations(buf.getInt(), BufUtils.readUTF(buf), new GuildEmblem(buf.getInt(), buf.getInt(), buf.get(), buf.getInt()));
                            buf.clear();
                        }
                        if (RS.getString("sell_informations") != null) {
                            this.SelledId = Integer.parseInt(RS.getString("sell_informations").split(",")[0]);
                            this.OwnerName = RS.getString("sell_informations").split(",")[1];
                        }
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

}
