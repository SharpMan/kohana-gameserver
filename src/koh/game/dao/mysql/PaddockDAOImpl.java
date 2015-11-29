package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.PaddockDAO;
import koh.game.entities.environments.Paddock;
import koh.game.utils.Settings;
import koh.game.utils.StringUtil;
import koh.game.utils.sql.ConnectionResult;
import koh.protocol.client.BufUtils;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.guild.GuildEmblem;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.MountInformationsForPaddock;
import koh.protocol.types.game.paddock.PaddockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class PaddockDAOImpl extends PaddockDAO {

    public static Map<Integer, Paddock> paddocks = new HashMap<>(1500);

    private static final Logger logger = LogManager.getLogger(PaddockDAOImpl.class);

    @Inject
    private DatabaseSource dbSource;

    public boolean update(Paddock Item, String[] Columns) {
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

    private void setValue(PreparedStatement p, String Column, int Seq, Paddock Item) {
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



    private int loadAll() {
            int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from paddocks_template", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                paddocks.put(result.getInt("map"), new Paddock() {
                    {
                        this.Id = result.getInt("id");
                        this.Map = result.getInt("map");
                        this.SubArea = result.getShort("sub_area");
                        this.Abandonned = result.getBoolean("abandonned");
                        this.Loocked = result.getBoolean("loocked");
                        this.Price = result.getInt("price");
                        this.MaxOutDoorMount = result.getInt("max_outdoor_mount");
                        this.MaxItem = result.getInt("max_items");
                        if (result.getBytes("items") != null) {
                            IoBuffer buf = IoBuffer.wrap(result.getBytes("items"));
                            this.Items = new PaddockItem[buf.getInt()];
                            for (int i = 0; i < this.Items.length; ++i) {
                                this.Items[i] = new PaddockItem(buf.getInt(), buf.getInt(), new ItemDurability(buf.getShort(), buf.getShort()));
                            }
                            buf.clear();
                        }
                        if (result.getBytes("mounts_informations") != null) {
                            IoBuffer buf = IoBuffer.wrap(result.getBytes("mounts_informations"));
                            this.MountInformations = new MountInformationsForPaddock[buf.getInt()];
                            for (int i = 0; i < this.MountInformations.length; ++i) {
                                this.MountInformations[i] = new MountInformationsForPaddock(buf.get(), BufUtils.readUTF(buf), BufUtils.readUTF(buf));
                            }
                            buf.clear();
                        }
                        if (result.getBytes("guild_informations") != null) {
                            IoBuffer buf = IoBuffer.wrap(result.getBytes("guild_informations"));
                            this.guildInfo = new GuildInformations(buf.getInt(), BufUtils.readUTF(buf), new GuildEmblem(buf.getInt(), buf.getInt(), buf.get(), buf.getInt()));
                            buf.clear();
                        }
                        if (result.getString("sell_informations") != null) {
                            this.SelledId = Integer.parseInt(result.getString("sell_informations").split(",")[0]);
                            this.OwnerName = result.getString("sell_informations").split(",")[1];
                        }
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        logger.info("loaded {} paddocks");
    }

    @Override
    public void stop() {

    }
}
