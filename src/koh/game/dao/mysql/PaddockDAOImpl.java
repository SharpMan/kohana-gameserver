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
    public Paddock find(int id) { return this.paddocks.get(id); }

    @Override
    public boolean update(Paddock item, String[] columns) {
        int i = 0;
        String Query = "UPDATE `paddocks_template` set ";
        Query = Arrays.stream(columns).map((s) -> s + " =?,").reduce(Query, String::concat);
        Query = StringUtil.removeLastChar(Query);
        Query += " WHERE id = ?;";
        try (PreparedStatement p = (PreparedStatement) dbSource.prepareStatement(Query)) {
            for (String s : columns) {
                setValue(p, s, ++i, item);
            }
            setValue(p, "id", ++i, item);
            p.execute();
            //MySQL.closePreparedStatement(p);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }


    private void setValue(PreparedStatement p, String column, int seq, Paddock item) {
        try {
            switch (column) {
                case "id":
                    p.setInt(seq, item.getId());
                    break;
                case "abandonned":
                    p.setBoolean(seq, item.isAbandonned());
                    break;
                case "loocked":
                    p.setBoolean(seq, item.isLoocked());
                    break;
                case "mounts_informations":
                    p.setBytes(seq, serializeMountsInformations(item.getMountInformationsForPaddocks()).array());
                    break;
                case "items":
                    p.setBytes(seq, serializeItemsInformations(item.getItems()).array());
                    break;
                case "guild_informations":
                    p.setBytes(seq, serializeGuildInformations(item.getGuildInfo()).array());
                    break;
                case "sell_informations":
                    p.setString(seq, item.getSelledId() + "," + item.getOwnerName());
                    break;
                case "price":
                    p.setInt(seq, item.getPrice());
                    break;
                case "max_outdoor_mount":
                    p.setInt(seq, item.getMaxOutDoorMount());
                    break;
                case "max_items":
                    p.setInt(seq, item.getMaxItem());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from paddocks_template", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                paddocks.put(result.getInt("map"), new Paddock(result));
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
        logger.info("loaded {} paddocks", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
