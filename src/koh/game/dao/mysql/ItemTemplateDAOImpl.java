package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.item.*;
import koh.game.entities.item.actions.*;
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
    private final Map<Integer, Class<? extends ItemAction>> itemActions = new HashMap<Integer, Class<? extends ItemAction>>() {
        {
            put(0, Teleportation.class);
            put(4, Kamas.class);
            put(5, CreateItem.class);
            put(6, LearnJob.class);
            put(7, ReturnToSavePos.class);
            put(8, AddBaseStat.class);
            put(9, LearnSpell.class);
            put(10, GenLife.class);
            put(11, SetAlign.class);
            put(12, SpawnMonsterGroup.class);
            put(13, Restat.class);
            put(14, ForgetSpell.class);
            put(15, TeleportDj.class);
            put(20, AddSpellPoint.class);
            put(21, AddEnergy.class);
            put(22, AddExperience.class);
        }
    };
    //.of(24,morph)
    //.of(25,demorph)
    //.of(26,guildEnnclos)
    //.of(50,traque)
    //.of(51,localistraque)

    private final Class[] actionArgs = new Class[]{String[].class, String.class};

    @Inject
    private DatabaseSource dbSource;

    private int loadAllUsableItems() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_usable_actions", 0)) {
            ResultSet result = conn.getResult();
            ItemType type;
            while (result.next()) {
                if (itemActions.get(result.getInt("action")) == null)
                    continue;
                this.getTemplate(result.getInt("template")).addItemAction(itemActions.get(result.getInt("action")).getDeclaredConstructor(this.actionArgs).newInstance(result.getString("args").split(","), result.getString("criteria")));
                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

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
        logger.info("Loaded {} usable items action", this.loadAllUsableItems());
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
