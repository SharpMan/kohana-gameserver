package koh.game.dao.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import koh.game.dao.api.PresetDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.preset.PresetBook;
import koh.game.entities.actors.character.preset.PresetEntity;
import koh.protocol.types.game.inventory.preset.Preset;
import koh.protocol.types.game.inventory.preset.PresetItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Melancholia on 7/1/16.
 */
public class PresetDAOImpl extends PresetDAO {

    private static final Logger logger = LogManager.getLogger(PresetDAO.class);

    public PresetDAOImpl() {
        try {
            this.dataSource = DaoManager.createDao(new JdbcConnectionSource("jdbc:sqlite:data/preset.db",
                    null, null), PresetEntity.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final Dao<PresetEntity, Integer> dataSource;

    @Override
    public void insert(PresetEntity entity) {
        try {
            dataSource.create(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(int owner, PresetEntity entity) {
        try {
            final UpdateBuilder<PresetEntity, Integer> builder = dataSource.updateBuilder();
            builder.updateColumnValue("infos",entity.informations);
            builder.where().eq("owner", owner).and().eq("id",entity.id);
            builder.update();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void remove(int owner, byte id) {
        try {
            final DeleteBuilder<PresetEntity, Integer> delete = dataSource.deleteBuilder();
            delete.where().eq("owner", owner).and().eq("id",id);
            delete.delete();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public PresetBook get(Player owner) {
        final PresetBook book = new PresetBook();
        try {
            for (PresetEntity presetEntity : dataSource.queryForEq("owner", owner.getID())) {
                final Preset preset = new Preset();
                final IoBuffer buf = IoBuffer.wrap(presetEntity.informations);
                preset.deserialize(buf);
                buf.clear();
                if(!Arrays.stream(preset.objects).map(o -> o.objUid).allMatch(owner.getInventoryCache()::contains)){
                    if(Arrays.stream(preset.objects).map(o -> o.objUid).noneMatch(owner.getInventoryCache()::contains)){
                        this.remove(owner.getID(),preset.presetId);
                        continue;
                    }
                    preset.objects = Arrays.stream(preset.objects).filter(o -> owner.getInventoryCache().contains(o.objUid)).toArray(PresetItem[]::new);
                    presetEntity.informations = preset.serializeInformations();
                    this.update(owner.getID(), presetEntity);
                }
                book.add(preset,presetEntity);
            }

        } catch (Exception e) {
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
