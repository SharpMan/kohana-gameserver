package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.PaddockDAO;
import koh.game.entities.environments.Paddock;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Neo-Craft
 */
public class PaddockDAOImpl extends PaddockDAO {

    private static final Logger logger = LogManager.getLogger(PaddockDAOImpl.class);
    private final Map<Integer, Paddock> paddocks = new HashMap<>(1500);
    @Inject
    private DatabaseSource dbSource;

    @Override
    public boolean update(Paddock item, String[] columns) {
        int i = 1;
        String Query = "UPDATE `paddocks_template` set ";
        Query = Arrays.stream(columns).map((s) -> s + " =?,").reduce(Query, String::concat);
        Query = StringUtil.removeLastChar(Query);
        Query += " WHERE id = ?;";
        try (PreparedStatement p = (PreparedStatement) dbSource.prepareStatement(Query)) {
            for (String s : columns) {
                setValue(p, s, i++, item);
            }
            setValue(p, "id", i++, item);
            p.execute();
            //MySQL.closePreparedStatement(p);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return false;
        }
        columns = null;
        return true;
    }


    private void setValue(PreparedStatement p, String column, int seq, Paddock item) {
        try {
            IoBuffer buf;
            switch (column) {
                case "id":
                    p.setInt(seq, item.id);
                    break;
                case "abandonned":
                    p.setBoolean(seq, item.abandonned);
                    break;
                case "loocked":
                    p.setBoolean(seq, item.loocked);
                    break;
                case "mounts_informations":
                    buf = serializeMountsInformations(item.mountInformationsForPaddocks);
                    p.setBytes(seq, buf.array());
                    buf.clear();
                    break;
                case "items":
                    buf = serializeItemsInformations(item.items);
                    p.setBytes(seq, buf.array());
                    buf.clear();
                    break;
                case "guild_informations":
                    buf = serializeGuildInformations(item.guildInfo);
                    p.setBytes(seq, buf.array());
                    buf.clear();
                    break;
                case "sell_informations":
                    p.setString(seq, item.selledId + "," + item.ownerName);
                    break;
                case "price":
                    p.setInt(seq, item.price);
                    break;
                case "max_outdoor_mount":
                    p.setInt(seq, item.maxOutDoorMount);
                    break;
                case "max_items":
                    p.setInt(seq, item.maxItem);
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
