package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.PresetDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.preset.PresetBook;
import koh.game.entities.actors.character.preset.PresetEntity;
import koh.game.entities.item.InventoryItem;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.inventory.preset.Preset;
import koh.protocol.types.game.inventory.preset.PresetItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static koh.game.entities.item.InventoryItem.deserializeEffects;

/**
 * Created by Melancholia on 7/1/16.
 */
public class PresetDAOImpl extends PresetDAO {

    private static final Logger logger = LogManager.getLogger(PresetDAO.class);

    @Inject
    private DatabaseSource dbSource;


    @Override
    public void insert(PresetEntity entity) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `preset` VALUES (?,?,?);")) {
                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, entity.getOwner());
                pStatement.setInt(2, entity.getId());
                pStatement.setBytes(3, entity.getInformations());
                pStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(int owner, PresetEntity entity) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `preset` SET `infos` = ? WHERE `owner` = ? AND `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setBytes(1,entity.getInformations());
                pStatement.setInt(2,entity.getOwner());
                pStatement.setInt(3,entity.getId());
                pStatement.executeUpdate();

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void remove(int owner, byte id) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `preset` WHERE `owner` = ? AND `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1,owner);
                pStatement.setInt(2,id);
                pStatement.execute();

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public PresetBook get(Player owner) {
        final PresetBook book = new PresetBook();
        try {
            try (ConnectionResult conn = dbSource.executeQuery("SELECT * from `preset` where owner =" + owner.getID() + ";")) {
                final ResultSet result = conn.getResult();
                while (result.next()) {
                    final Preset preset = new Preset();
                    final PresetEntity entity = new PresetEntity(result.getInt("owner"),result.getInt("id"),result.getBytes("infos"),preset);
                    final IoBuffer buf = IoBuffer.wrap(entity.getInformations());
                    preset.deserialize(buf);
                    buf.clear();
                    if(!Arrays.stream(preset.objects).map(o -> o.objUid).allMatch(owner.getInventoryCache()::contains)){
                        if(Arrays.stream(preset.objects).map(o -> o.objUid).noneMatch(owner.getInventoryCache()::contains)){
                            this.remove(owner.getID(),preset.presetId);
                            continue;
                        }
                        preset.objects = Arrays.stream(preset.objects).filter(o -> owner.getInventoryCache().contains(o.objUid)).toArray(PresetItem[]::new);
                        entity.setInformations(preset.serializeInformations());
                        this.update(owner.getID(), entity);
                    }
                    book.add(preset,entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                logger.warn(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        finally {
            return book;
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
