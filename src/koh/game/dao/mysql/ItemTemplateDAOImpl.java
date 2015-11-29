package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.item.animal.Pets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import koh.game.Logs;

import static koh.game.MySQL.executeQuery;
import koh.game.entities.item.*;
import static koh.game.entities.item.InventoryItem.DeserializeEffects;
import koh.game.entities.item.animal.*;
import koh.game.entities.spells.*;
import koh.game.utils.StringUtil;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class ItemTemplateDAOImpl extends ItemTemplateDAO {

    private static final Logger logger = LogManager.getLogger(ItemTemplateDAO.class);

    private final Map<Integer, ItemTemplate> itemTemplates = new ConcurrentHashMap<>(1000);
    private final Map<Integer, ItemSet> itemSets = new ConcurrentHashMap<>(50);
    private final Map<Integer, Pets> pets = new ConcurrentHashMap<>(50);
    private final Map<Integer, ItemType> itemTypes = new ConcurrentHashMap<>(50);

    private volatile int nextId;
    private volatile int nextStorageId;
    private volatile int nextPetId = 1;
    private volatile int NextMountsID = 1;

    @Inject
    private DatabaseSource dbSource;

    @Override
    public void initInventoryCache(int player, Map<Integer, InventoryItem> cache, String table) {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from " + table + " where owner =" + player + ";")) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                List<ObjectEffect> effects = DeserializeEffects(result.getBytes("effects"));
                cache.put(result.getInt("id"), InventoryItem.Instance(
                        result.getInt("id"),
                        result.getInt("template"),
                        result.getInt("position"),
                        result.getInt("owner"),
                        result.getInt("stack"),
                        effects
                ));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private void distinctItems() {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `character_items` WHERE owner = ?;")) {
            PreparedStatement pStatement = conn.getStatement();
            pStatement.setInt(1, -1);
            pStatement.execute();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private void initNextId() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT id FROM `character_items` ORDER BY id DESC LIMIT 1;")) {
            ResultSet result = conn.getResult();
            if (!result.first())
                nextId = 0;
            else
                nextId = result.getInt("id");
            ++nextId;

            try(Statement statement = conn.getConnection().createStatement()) {
                ResultSet storageResult = statement.executeQuery("SELECT id FROM `storage_items` ORDER BY id DESC LIMIT 1;");

                if (!storageResult.first())
                    nextStorageId = 0;
                else
                    nextStorageId = storageResult.getInt("id");
                ++nextStorageId;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean create(InventoryItem item, boolean clear, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `" + table + "` VALUES (?,?,?,?,?,?);")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.ID);
            pStatement.setInt(2, item.GetOwner());
            pStatement.setInt(3, item.TemplateId);
            pStatement.setInt(4, item.GetPosition());
            pStatement.setInt(5, item.GetQuantity());
            pStatement.setBytes(6, item.SerializeEffectInstanceDice().array());

            item.NeedInsert = false;
            item.ColumsToUpdate = null;

            pStatement.execute();

            //TODO better Dispose/totalClear pattern
            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean save(InventoryItem item, boolean clear, String table) {
        int i = 1;
        String query = "UPDATE `" + table + "` set ";
        query = item.ColumsToUpdate.stream().map((s) -> s + " =?,").reduce(query, String::concat);
        query = StringUtil.removeLastChar(query);
        query += " WHERE id = ?;";

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement(query)) {
            PreparedStatement pStatement = conn.getStatement();

            item.ColumsToUpdate.add("id");
            for (String columnName : item.ColumsToUpdate) {
                setValue(pStatement, columnName, i++, item);
            }

            item.ColumsToUpdate.clear();
            item.ColumsToUpdate = null;

            pStatement.execute();

            //TODO better Dispose/totalClear pattern
            /*if (Clear) {
                Item.totalClear();
            }*/

            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    private static void setValue(PreparedStatement p, String Column, int Seq, InventoryItem Item) {
        try {
            switch (Column) {
                case "id":
                    p.setInt(Seq, Item.ID);
                    break;
                case "owner":
                    p.setInt(Seq, Item.GetOwner());
                    break;
                case "stack":
                    p.setInt(Seq, Item.GetQuantity());
                    break;
                case "position":
                    p.setInt(Seq, Item.GetPosition());
                    break;
                case "effects":
                    p.setBytes(Seq, Item.SerializeEffectInstanceDice().array());
                    //p.setBlob(Seq, new SerialBlob(Item.SerializeEffectInstanceDice()));
                    break;

            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean delete(InventoryItem item, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `" + table + "` WHERE id = ?;")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.ID);
            pStatement.execute();

            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }

        return false;
    }

    private int loadAllItemTypes() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_types", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemTypes.put(result.getInt("id"), new ItemType() {
                    {
                        SuperType = result.getInt("super_type_id");
                        plural = result.getBoolean("plural");
                        gender = result.getInt("gender");
                        rawZone = result.getString("raw_zone");
                        needUseConfirm = result.getBoolean("need_use_confirm");
                        mimickable = result.getBoolean("mimickable");
                    }
                });
                ++i;
            }
        }catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllItemSets() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_sets", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemSets.put(result.getInt("id"), new ItemSet() {
                    {
                        id = result.getInt("id");

                        this.items = new int[result.getString("items").split(",").length];
                        for (int i = 0; i < result.getString("items").split(",").length; i++) {
                            this.items[i] = Integer.parseInt(result.getString("items").split(",")[i]);
                        }
                        this.bonusIsSecret = result.getBoolean("bonus_is_secret");
                        IoBuffer buf = IoBuffer.wrap(result.getBytes("effects"));
                        this.effects = new EffectInstance[buf.getInt()][];
                        for (int i = 0; i < this.effects.length; ++i) {
                            this.effects[i] = ItemTemplateDAO.readEffectInstance(buf);
                        }
                    }
                });
                ++i;
            }
        }catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllPets() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_pets", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                pets.put(result.getInt("id"), new Pets() {
                    {
                        this.Id = result.getInt("id");
                        if (result.getString("food_items").isEmpty() || result.getString("food_items").equalsIgnoreCase("2239")) {
                            this.foodItems = new FoodItem[0];
                        } else {
                            ArrayList<FoodItem> Foods = new ArrayList<>();
                            for (String s : result.getString("food_items").split(",")) {
                                if (s.equalsIgnoreCase("2239")) {
                                    continue;
                                }
                                Foods.add(new FoodItem(Integer.parseInt(s.split(";")[0]), Integer.parseInt(s.split(";")[1]), Integer.parseInt(s.split(";")[2]), Integer.parseInt(s.split(";")[3])));
                            }
                            this.foodItems = Foods.stream().toArray(FoodItem[]::new);
                            Foods.clear();
                            Foods = null;
                        }
                        if (result.getString("food_types").isEmpty()) {
                            this.foodTypes = new FoodItem[0];
                        } else {
                            this.foodTypes = new FoodItem[result.getString("food_types").split(",").length];
                            for (int i = 0; i < this.foodTypes.length; ++i) {
                                this.foodTypes[i] = new FoodItem(Integer.parseInt(result.getString("food_types").split(",")[i].split(";")[0]), Integer.parseInt(result.getString("food_types").split(",")[i].split(";")[1]), Integer.parseInt(result.getString("food_types").split(",")[i].split(";")[2]), Integer.parseInt(result.getString("food_types").split(",")[i].split(";")[3]));
                            }
                        }
                        if (result.getString("monster_food").isEmpty()) {
                            this.MonsterBoosts = new MonsterBooster[0];
                        } else {
                            this.MonsterBoosts = new MonsterBooster[result.getString("monster_food").split(",").length];
                            for (int i = 0; i < this.MonsterBoosts.length; ++i) {
                                this.MonsterBoosts[i] = new MonsterBooster(Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[0]), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[1]), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[2]), Enumerable.StringToIntArray(result.getString("monster_food").split(",")[i].split(";")[3], ":"), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[4]), result.getString("monster_food").split(",")[i].split(";").length > 5 ? result.getString("monster_food").split(",")[i].split(";")[5] : null);
                            }
                        }
                        this.minDurationBeforeMeal = result.getInt("min_duration_before_meal");
                        this.maxDurationBeforeMeal = result.getInt("max_duration_before_meal");
                        IoBuffer buf = IoBuffer.wrap(result.getBytes("possible_effects"));
                        this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.possibleEffects.length; i++) {
                            this.possibleEffects[i] = new EffectInstanceDice(buf);
                        }
                        buf.clear();
                        if (result.getBytes("max_effects") != null) {
                            buf = IoBuffer.wrap(result.getBytes("max_effects"));
                            this.maxEffects = new EffectInstanceDice[buf.getInt()];
                            for (int i = 0; i < this.maxEffects.length; i++) {
                                this.maxEffects[i] = new EffectInstanceDice(buf);
                            }
                            buf.clear();
                        }
                        this.Hormone = result.getInt("total_points");
                        if (Logs.DEBUG) {
                            this.Verif();
                        }
                    }
                });
                ++i;
            }
        }catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemTemplates.put(result.getInt("id"), new ItemTemplate() {
                    {
                        id = result.getInt("id");
                        this.nameId = result.getString("name");
                        this.TypeId = result.getInt("type_id");
                        this.iconId = result.getInt("icon_id");
                        this.level = result.getInt("level");
                        this.realWeight = result.getInt("real_weight");
                        this.cursed = result.getBoolean("cursed");
                        this.useAnimationId = result.getInt("use_animation_id");
                        this.usable = result.getBoolean("usable");
                        this.targetable = result.getBoolean("targetable");
                        this.exchangeable = result.getBoolean("exchangeable");
                        this.price = result.getFloat("price");
                        this.twoHanded = result.getBoolean("two_handed");
                        this.etheral = result.getBoolean("etheral");
                        this.itemSetId = result.getInt("item_set_id");
                        this.criteria = result.getString("criteria");
                        this.criteriaTarget = result.getString("criteria_target");
                        this.hideEffects = result.getBoolean("hide_effects");
                        this.enhanceable = result.getBoolean("enhanceable");
                        this.nonUsableOnAnother = result.getBoolean("non_usable_on_another");
                        this.appearanceId = result.getShort("apprance_id");
                        this.secretRecipe = result.getBoolean("secret_recipe");
                        this.recipeSlots = result.getInt("recipe_slots");

                        this.recipeIds = parseIds(result.getString("recipe_ids"));

                        this.dropMonsterIds = parseIds(result.getString("drop_monster_ids"));

                        this.bonusIsSecret = result.getBoolean("bonus_is_secret");

                        {
                            IoBuffer buf = IoBuffer.wrap(result.getBytes("possible_effects"));
                            this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                            for (int i = 0; i < this.possibleEffects.length; i++) {
                                this.possibleEffects[i] = new EffectInstanceDice(buf);
                            }
                        }

                        this.favoriteSubAreas = parseIds(result.getString("favorite_sub_areas"));

                        this.favoriteSubAreasBonus = result.getInt("favorite_sub_areas_bonus");

                    }
                });
                ++i;
            }
        }catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private static int[] parseIds(String recipe_ids) {
        if (!recipe_ids.trim().isEmpty()) {
            String[] recipes = recipe_ids.split(",");
            int[] ids = new int[recipes.length];
            for (int i = 0; i < recipes.length; i++)
                ids[i] = Integer.parseInt(recipes[i]);
            return ids;
        } else {
            return new int[0];
        }
    }

    private int loadAllWeapons() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from item_templates_weapons", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                itemTemplates.put(result.getInt("id"), new Weapon() {
                    {
                        id = result.getInt("id");
                        this.nameId = result.getString("name");
                        this.TypeId = result.getInt("type_id");
                        this.iconId = result.getInt("icon_id");
                        this.level = result.getInt("level");
                        this.realWeight = result.getInt("real_weight");
                        this.cursed = result.getBoolean("cursed");
                        this.useAnimationId = result.getInt("use_animation_id");
                        this.usable = result.getBoolean("usable");
                        this.targetable = result.getBoolean("targetable");
                        this.exchangeable = result.getBoolean("exchangeable");
                        this.price = result.getFloat("price");
                        this.twoHanded = result.getBoolean("two_handed");
                        this.etheral = result.getBoolean("etheral");
                        this.itemSetId = result.getInt("item_set_id");
                        this.criteria = result.getString("criteria");
                        this.criteriaTarget = result.getString("criteria_target");
                        this.hideEffects = result.getBoolean("hide_effects");
                        this.enhanceable = result.getBoolean("enhanceable");
                        this.nonUsableOnAnother = result.getBoolean("non_usable_on_another");
                        this.appearanceId = result.getShort("apprance_id");
                        this.secretRecipe = result.getBoolean("secret_recipe");
                        this.recipeSlots = result.getInt("recipe_slots");

                        this.recipeIds = parseIds(result.getString("recipe_ids"));

                        this.dropMonsterIds = parseIds(result.getString("drop_monster_ids"));

                        this.bonusIsSecret = result.getBoolean("bonus_is_secret");

                        {
                            IoBuffer buf = IoBuffer.wrap(result.getBytes("possible_effects"));
                            this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                            for (int i = 0; i < this.possibleEffects.length; i++) {
                                this.possibleEffects[i] = new EffectInstanceDice(buf);
                            }
                        }

                        this.favoriteSubAreas = parseIds(result.getString("favorite_sub_areas"));

                        this.favoriteSubAreasBonus = result.getInt("favorite_sub_areas_bonus");
                        this.range = result.getInt("range");
                        this.criticalHitBonus = result.getInt("range");
                        this.minRange = result.getInt("range");
                        this.maxCastPerTurn = result.getInt("range");
                        this.criticalFailureProbability = result.getInt("range");
                        this.criticalHitProbability = result.getInt("range");
                        this.castInDiagonal = result.getBoolean("cast_in_diagonal");
                        this.apCost = result.getInt("ap_cost");
                        this.castInLine = result.getBoolean("cast_in_line");
                        this.castTestLos = result.getBoolean("cast_test_los");
                    }
                });
                ++i;
            }
        }catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        this.initNextId();
        this.distinctItems();

        logger.info("Loaded {} template types", this.loadAllItemTypes());
        logger.info("Loaded {} template sets", this.loadAllItemSets());
        logger.info("Loaded {} template pets", this.loadAllPets());
        logger.info("Loaded {} templates", this.loadAll());
        logger.info("Loaded {} template weapons", this.loadAllWeapons());
    }

    @Override
    public void stop() {

    }
}
