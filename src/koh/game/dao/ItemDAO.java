package koh.game.dao;

import koh.game.entities.item.animal.Pets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import koh.game.Logs;
import koh.game.MySQL;
import static koh.game.MySQL.executeQuery;
import koh.game.entities.item.*;
import static koh.game.entities.item.InventoryItem.DeserializeEffects;
import koh.game.entities.item.animal.*;
import koh.game.entities.spells.*;
import koh.game.utils.Settings;
import koh.game.utils.StringUtil;
import koh.utils.Enumerable;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class ItemDAO {

    public static final Map<Integer, ItemTemplate> Cache = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, ItemSet> Sets = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, Pets> Pets = Collections.synchronizedMap(new HashMap<>());
    //@Param1 = ItemType @Param2 = ItemSuperType
    public static final Map<Integer, Integer> SuperTypes = Collections.synchronizedMap(new HashMap<>());
    public static volatile int NextID;
    public static volatile int NextStorageID;
    public static volatile int NextPetsID = 1;
    public static volatile int NextMountsID = 1;

    public static void InitInventoryCache(int player, Map<Integer, InventoryItem> Cache, String table) {
        synchronized (Cache) {
            try {
                ResultSet RS = MySQL.executeQuery("SELECT * from " + table + " where owner =" + player + ";", Settings.GetStringElement("Database.Name"), 0);
                List<EffectInstance> Effects;
                while (RS.next()) {
                    Effects = DeserializeEffects(RS.getBytes("effects"));
                    Cache.put(RS.getInt("id"), InventoryItem.Instance(RS.getInt("id"), RS.getInt("template"), RS.getInt("position"), RS.getInt("owner"), RS.getInt("stack"), Effects));
                    Effects = null;
                }
                MySQL.closeResultSet(RS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static void DistinctItems() {
        try {
            //TestDAO.SetMaxEffects(15254, new EffectInstance[]{new EffectInstanceDice(new EffectInstance(0, 128, 0, "", 0, 0, 0, false, "C", 0, "", 0), 0, 1, 0)});
            PreparedStatement p = MySQL.prepareQuery("DELETE from `character_items` WHERE owner = ?;", MySQL.Connection());
            p.setInt(1, -1);
            p.execute();

            MySQL.closePreparedStatement(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void InitializeNextIdentifiant() {
        try {
            ResultSet RS = executeQuery("SELECT id FROM `character_items` ORDER BY id DESC LIMIT 1;", Settings.GetStringElement("Database.Name"));
            if (!RS.first()) {
                NextID = 0;
            }
            NextID = RS.getInt("id");
            NextID++;
            MySQL.closeResultSet(RS);
            RS = executeQuery("SELECT id FROM `storage_items` ORDER BY id DESC LIMIT 1;", Settings.GetStringElement("Database.Name"));
            if (!RS.first()) {
                NextStorageID = 0;
            }
            NextStorageID = RS.getInt("id");
            NextStorageID++;
            MySQL.closeResultSet(RS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean Insert(InventoryItem Item, boolean Clear, String Table) {
        try {
            PreparedStatement p = MySQL.prepareQuery("INSERT INTO `" + Table + "` VALUES (?,?,?,?,?,?);", MySQL.Connection());

            p.setInt(1, Item.ID);
            p.setInt(2, Item.GetOwner());
            p.setInt(3, Item.TemplateId);
            p.setInt(4, Item.GetPosition());
            p.setInt(5, Item.GetQuantity());
            p.setBytes(6, Item.SerializeEffectInstanceDice().array());
            Item.NeedInsert = false;
            Item.ColumsToUpdate = null;
            p.execute();
            MySQL.closePreparedStatement(p);
            if (Clear) {
                Item.totalClear();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean Update(InventoryItem Item, boolean Clear, String Table) {
        try {
            int i = 1;
            String Query = "UPDATE `" + Table + "` set ";
            Query = Item.ColumsToUpdate.stream().map((s) -> s + " =?,").reduce(Query, String::concat);
            Query = StringUtil.removeLastChar(Query);
            Query += " WHERE id = ?;";

            PreparedStatement p = MySQL.prepareQuery(Query, MySQL.Connection());

            Item.ColumsToUpdate.add("id");
            for (String s : Item.ColumsToUpdate) {
                setValue(p, s, i++, Item);
            }
            p.execute();
            MySQL.closePreparedStatement(p);
            Item.ColumsToUpdate.clear();
            Item.ColumsToUpdate = null;
            if (Clear) {
                Item.totalClear();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setValue(PreparedStatement p, String Column, int Seq, InventoryItem Item) {
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
            e.printStackTrace();
        }
    }

    public static boolean Remove(InventoryItem Item, String Table) {
        try {
            PreparedStatement p = MySQL.prepareQuery("DELETE from `" + Table + "` WHERE id = ?;", MySQL.Connection());
            p.setInt(1, Item.ID);
            p.execute();

            MySQL.closePreparedStatement(p);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int FindItemTypes() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from item_types", Settings.GetStringElement("Database.Name"), 0);
            while (RS.next()) {
                SuperTypes.put(RS.getInt("id"), RS.getInt("super_type_id"));
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindItemSets() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from item_sets", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Sets.put(RS.getInt("id"), new ItemSet() {
                    {
                        id = RS.getInt("id");

                        this.items = new int[RS.getString("items").split(",").length];
                        for (int i = 0; i < RS.getString("items").split(",").length; i++) {
                            this.items[i] = Integer.parseInt(RS.getString("items").split(",")[i]);
                        }
                        this.bonusIsSecret = RS.getBoolean("bonus_is_secret");
                        IoBuffer buf = IoBuffer.wrap(RS.getBytes("effects"));
                        this.effects = new EffectInstance[buf.getInt()][];
                        for (int i = 0; i < this.effects.length; ++i) {
                            this.effects[i] = ReadInstance(buf);
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

    public static int FindPets() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from item_pets", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Pets.put(RS.getInt("id"), new Pets() {
                    {
                        this.Id = RS.getInt("id");
                        if (RS.getString("food_items").isEmpty() || RS.getString("food_items").equalsIgnoreCase("2239")) {
                            this.foodItems = new FoodItem[0];
                        } else {
                            ArrayList<FoodItem> Foods = new ArrayList<>();
                            for (String s : RS.getString("food_items").split(",")) {
                                if (s.equalsIgnoreCase("2239")) {
                                    continue;
                                }
                                Foods.add(new FoodItem(Integer.parseInt(s.split(";")[0]), Integer.parseInt(s.split(";")[1]), Integer.parseInt(s.split(";")[2]), Integer.parseInt(s.split(";")[3])));
                            }
                            this.foodItems = Foods.stream().toArray(FoodItem[]::new);
                            Foods.clear();
                            Foods = null;
                        }
                        if (RS.getString("food_types").isEmpty()) {
                            this.foodTypes = new FoodItem[0];
                        } else {
                            this.foodTypes = new FoodItem[RS.getString("food_types").split(",").length];
                            for (int i = 0; i < this.foodTypes.length; ++i) {
                                this.foodTypes[i] = new FoodItem(Integer.parseInt(RS.getString("food_types").split(",")[i].split(";")[0]), Integer.parseInt(RS.getString("food_types").split(",")[i].split(";")[1]), Integer.parseInt(RS.getString("food_types").split(",")[i].split(";")[2]), Integer.parseInt(RS.getString("food_types").split(",")[i].split(";")[3]));
                            }
                        }
                        if (RS.getString("monster_food").isEmpty()) {
                            this.MonsterBoosts = new MonsterBooster[0];
                        } else {
                            this.MonsterBoosts = new MonsterBooster[RS.getString("monster_food").split(",").length];
                            for (int i = 0; i < this.MonsterBoosts.length; ++i) {
                                this.MonsterBoosts[i] = new MonsterBooster(Integer.parseInt(RS.getString("monster_food").split(",")[i].split(";")[0]), Integer.parseInt(RS.getString("monster_food").split(",")[i].split(";")[1]), Integer.parseInt(RS.getString("monster_food").split(",")[i].split(";")[2]), Enumerable.StringToIntArray(RS.getString("monster_food").split(",")[i].split(";")[3], ":"), Integer.parseInt(RS.getString("monster_food").split(",")[i].split(";")[4]));
                            }
                        }
                        this.minDurationBeforeMeal = RS.getInt("min_duration_before_meal");
                        this.maxDurationBeforeMeal = RS.getInt("max_duration_before_meal");
                        IoBuffer buf = IoBuffer.wrap(RS.getBytes("possible_effects"));
                        this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.possibleEffects.length; i++) {
                            this.possibleEffects[i] = new EffectInstanceDice(buf);
                        }
                        buf.clear();
                        if (RS.getBytes("max_effects") != null) {
                            buf = IoBuffer.wrap(RS.getBytes("max_effects"));
                            this.maxEffects = new EffectInstanceDice[buf.getInt()];
                            for (int i = 0; i < this.maxEffects.length; i++) {
                                this.maxEffects[i] = new EffectInstanceDice(buf);
                            }
                            buf.clear();
                        }
                        this.Hormone = RS.getInt("total_points");
                        if (Logs.DEBUG) {
                            this.Verif();
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

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from item_templates", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new ItemTemplate() {
                    {
                        id = RS.getInt("id");
                        this.nameId = RS.getString("name");
                        this.TypeId = RS.getInt("type_id");
                        this.iconId = RS.getInt("icon_id");
                        this.level = RS.getInt("level");
                        this.realWeight = RS.getInt("real_weight");
                        this.cursed = RS.getBoolean("cursed");
                        this.useAnimationId = RS.getInt("use_animation_id");
                        this.usable = RS.getBoolean("usable");
                        this.targetable = RS.getBoolean("targetable");
                        this.exchangeable = RS.getBoolean("exchangeable");
                        this.price = RS.getFloat("price");
                        this.twoHanded = RS.getBoolean("two_handed");
                        this.etheral = RS.getBoolean("etheral");
                        this.itemSetId = RS.getInt("item_set_id");
                        this.criteria = RS.getString("criteria");
                        this.criteriaTarget = RS.getString("criteria_target");
                        this.hideEffects = RS.getBoolean("hide_effects");
                        this.enhanceable = RS.getBoolean("enhanceable");
                        this.nonUsableOnAnother = RS.getBoolean("non_usable_on_another");
                        this.appearanceId = RS.getShort("apprance_id");
                        this.secretRecipe = RS.getBoolean("secret_recipe");
                        this.recipeSlots = RS.getInt("recipe_slots");
                        if (!RS.getString("recipe_ids").isEmpty()) {
                            this.recipeIds = new int[RS.getString("recipe_ids").split(",").length];
                            for (int i = 0; i < RS.getString("recipe_ids").split(",").length; i++) {
                                this.recipeIds[i] = Integer.parseInt(RS.getString("recipe_ids").split(",")[i]);
                            }
                        } else {
                            this.recipeIds = new int[0];
                        }
                        if (!RS.getString("drop_monster_ids").isEmpty()) {
                            this.dropMonsterIds = new int[RS.getString("drop_monster_ids").split(",").length];
                            for (int i = 0; i < RS.getString("drop_monster_ids").split(",").length; i++) {
                                this.dropMonsterIds[i] = Integer.parseInt(RS.getString("drop_monster_ids").split(",")[i]);
                            }
                        } else {
                            this.dropMonsterIds = new int[0];
                        }
                        this.bonusIsSecret = RS.getBoolean("bonus_is_secret");
                        IoBuffer buf = IoBuffer.wrap(RS.getBytes("possible_effects"));
                        this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.possibleEffects.length; i++) {
                            this.possibleEffects[i] = new EffectInstanceDice(buf);
                        }
                        buf.clear();
                        if (!RS.getString("favorite_sub_areas").isEmpty()) {
                            this.favoriteSubAreas = new int[RS.getString("favorite_sub_areas").split(",").length];
                            for (int i = 0; i < RS.getString("favorite_sub_areas").split(",").length; i++) {
                                this.favoriteSubAreas[i] = Integer.parseInt(RS.getString("favorite_sub_areas").split(",")[i]);
                            }
                        } else {
                            this.favoriteSubAreas = new int[0];
                        }

                        this.favoriteSubAreasBonus = RS.getInt("favorite_sub_areas_bonus");
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

    public static int FindWeapons() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from item_templates_weapons", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new Weapon() {
                    {
                        id = RS.getInt("id");
                        this.nameId = RS.getString("name");
                        this.TypeId = RS.getInt("type_id");
                        this.iconId = RS.getInt("icon_id");
                        this.level = RS.getInt("level");
                        this.realWeight = RS.getInt("real_weight");
                        this.cursed = RS.getBoolean("cursed");
                        this.useAnimationId = RS.getInt("use_animation_id");
                        this.usable = RS.getBoolean("usable");
                        this.targetable = RS.getBoolean("targetable");
                        this.exchangeable = RS.getBoolean("exchangeable");
                        this.price = RS.getFloat("price");
                        this.twoHanded = RS.getBoolean("two_handed");
                        this.etheral = RS.getBoolean("etheral");
                        this.itemSetId = RS.getInt("item_set_id");
                        this.criteria = RS.getString("criteria");
                        this.criteriaTarget = RS.getString("criteria_target");
                        this.hideEffects = RS.getBoolean("hide_effects");
                        this.enhanceable = RS.getBoolean("enhanceable");
                        this.nonUsableOnAnother = RS.getBoolean("non_usable_on_another");
                        this.appearanceId = RS.getShort("apprance_id");
                        this.secretRecipe = RS.getBoolean("secret_recipe");
                        this.recipeSlots = RS.getInt("recipe_slots");
                        if (!RS.getString("recipe_ids").isEmpty()) {
                            this.recipeIds = new int[RS.getString("recipe_ids").split(",").length];
                            for (int i = 0; i < RS.getString("recipe_ids").split(",").length; i++) {
                                this.recipeIds[i] = Integer.parseInt(RS.getString("recipe_ids").split(",")[i]);
                            }
                        } else {
                            this.recipeIds = new int[0];
                        }
                        if (!RS.getString("drop_monster_ids").isEmpty()) {
                            this.dropMonsterIds = new int[RS.getString("drop_monster_ids").split(",").length];
                            for (int i = 0; i < RS.getString("drop_monster_ids").split(",").length; i++) {
                                this.dropMonsterIds[i] = Integer.parseInt(RS.getString("drop_monster_ids").split(",")[i]);
                            }
                        } else {
                            this.dropMonsterIds = new int[0];
                        }
                        this.bonusIsSecret = RS.getBoolean("bonus_is_secret");
                        IoBuffer buf = IoBuffer.wrap(RS.getBytes("possible_effects"));
                        this.possibleEffects = new EffectInstanceDice[buf.getInt()];
                        for (int i = 0; i < this.possibleEffects.length; i++) {
                            this.possibleEffects[i] = new EffectInstanceDice(buf);
                        }
                        buf.clear();
                        if (!RS.getString("favorite_sub_areas").isEmpty()) {
                            this.favoriteSubAreas = new int[RS.getString("favorite_sub_areas").split(",").length];
                            for (int i = 0; i < RS.getString("favorite_sub_areas").split(",").length; i++) {
                                this.favoriteSubAreas[i] = Integer.parseInt(RS.getString("favorite_sub_areas").split(",")[i]);
                            }
                        } else {
                            this.favoriteSubAreas = new int[0];
                        }

                        this.favoriteSubAreasBonus = RS.getInt("favorite_sub_areas_bonus");
                        this.range = RS.getInt("range");
                        this.criticalHitBonus = RS.getInt("range");
                        this.minRange = RS.getInt("range");
                        this.maxCastPerTurn = RS.getInt("range");
                        this.criticalFailureProbability = RS.getInt("range");
                        this.criticalHitProbability = RS.getInt("range");
                        this.castInDiagonal = RS.getBoolean("cast_in_diagonal");
                        this.apCost = RS.getInt("ap_cost");
                        this.castInLine = RS.getBoolean("cast_in_line");
                        this.castTestLos = RS.getBoolean("cast_test_los");

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

    public static EffectInstance[] ReadInstance(IoBuffer buf) {
        EffectInstance[] possibleEffectstype = new EffectInstance[buf.getInt()];
        for (int i = 0; i < possibleEffectstype.length; ++i) {
            int classID = buf.getInt();
            switch (classID) {
                case 1:
                    possibleEffectstype[i] = new EffectInstance(buf);
                    break;
                case 3:
                    possibleEffectstype[i] = new EffectInstanceDice(buf);
                    break;
                case 2:
                    possibleEffectstype[i] = new EffectInstanceInteger(buf);
                    break;
                case -1431655766:
                    break;
                default:
                    System.out.println("class" + classID);
                    //throw new Error("Unknown classDI " + classID);
                    break;
            }
        }
        return possibleEffectstype;
    }

}
