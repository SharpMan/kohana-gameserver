package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.item.ItemSet;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.item.ItemType;
import koh.game.entities.item.Weapon;
import koh.game.entities.item.animal.PetTemplate;
import koh.game.utils.sql.ConnectionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Neo-Craft
 */
public class ItemTemplateDAOImpl extends ItemTemplateDAO {

    private static final Logger logger = LogManager.getLogger(ItemTemplateDAO.class);

    private final Map<Integer, ItemTemplate> itemTemplates = new HashMap<>(11000);
    private final Map<Integer, ItemSet> itemSets = new HashMap<>(377);
    private final Map<Integer, PetTemplate> pets = new HashMap<>(114);
    private final Map<Integer, ItemType> itemTypes = new HashMap<>(171);

    @Inject
    private DatabaseSource dbSource;

    private int loadAllItemTypes() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_types", 0)) {
            ResultSet result = conn.getResult();
            ItemType type;
            while (result.next()) {
                type = ItemType.builder()
                        .superType(result.getInt("super_type_id"))
                        .plural(result.getBoolean("plural"))
                        .gender(result.getInt("gender"))
                        .rawZone(result.getString("raw_zone"))
                        .needUseConfirm(result.getBoolean("need_use_confirm"))
                        .mimickable(result.getBoolean("mimickable"))
                        .build();
                itemTypes.put(result.getInt("id"), type);
                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllItemSets() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_sets", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                itemSets.put(result.getInt("id"), new ItemSet(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return itemSets.size();
    }

    private int loadAllPets() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_pets", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                pets.put(result.getInt("id"), new PetTemplate(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return pets.size();
    }

    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemTemplates.put(result.getInt("id"), new ItemTemplate(result));
                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllWeapons() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_templates_weapons", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemTemplates.put(result.getInt("id"), new Weapon(result));
                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        logger.info("Loaded {} template types", this.loadAllItemTypes());
        logger.info("Loaded {} template sets", this.loadAllItemSets());
        logger.info("Loaded {} template pets", this.loadAllPets());
        logger.info("Loaded {} templates", this.loadAll());
        logger.info("Loaded {} template weapons", this.loadAllWeapons());
    }

    @Override
    public void stop() {

    }

    @Override
    public ItemTemplate getTemplate(int id) {
        return itemTemplates.get(id);
    }

    @Override
    public ItemSet getSet(int id) {
        return itemSets.get(id);
    }

    @Override
    public PetTemplate getPetTemplate(int id) {
        return pets.get(id);
    }

    @Override
    public ItemType getType(int id) {
        return itemTypes.get(id);
    }
}
