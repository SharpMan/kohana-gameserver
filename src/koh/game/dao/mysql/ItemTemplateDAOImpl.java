package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.item.animal.PetTemplate;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import koh.game.Logs;

import static koh.game.MySQL.executeQuery;
import koh.game.entities.item.*;
import koh.game.entities.item.animal.*;
import koh.game.entities.spells.*;
import koh.game.utils.sql.ConnectionResult;
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
                pets.put(result.getInt("id"), new PetTemplate() {
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
                        this.typeId = result.getInt("type_id");
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
                        this.typeId = result.getInt("type_id");
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
