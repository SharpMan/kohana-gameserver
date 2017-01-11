package koh.game.entities.item.animal;

import com.google.common.primitives.Ints;
import koh.game.Logs;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.utils.Enumerable;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Neo-Craft
 */
public class PetTemplate {

    private static final Logger logger = LogManager.getLogger(PetTemplate.class);

    @Getter
    private int Id;
    @Getter
    private FoodItem[] foodItems, foodTypes;
    @Getter
    private MonsterBooster[] monsterBoosts;
    @Getter
    private int minDurationBeforeMeal, maxDurationBeforeMeal, hormone;
    @Getter
    private EffectInstance[] possibleEffects, maxEffects;

    public PetTemplate(ResultSet result) throws SQLException {
        this.Id = result.getInt("id");
        if (result.getString("food_items").isEmpty() || result.getString("food_items").equalsIgnoreCase("2239")) {
            this.foodItems = new FoodItem[0];
        } else {
            final ArrayList<FoodItem> foods = new ArrayList<>();
            for (String s : result.getString("food_items").split(",")) {
                if (s.equalsIgnoreCase("2239")) { //Poudre eni
                    continue;
                }
                foods.add(new FoodItem(Integer.parseInt(s.split(";")[0]), Integer.parseInt(s.split(";")[1]), Integer.parseInt(s.split(";")[2]), Integer.parseInt(s.split(";")[3])));
            }
            this.foodItems = foods.stream().toArray(FoodItem[]::new);
            foods.clear();
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
            this.monsterBoosts = new MonsterBooster[0];
        } else {
            this.monsterBoosts = new MonsterBooster[result.getString("monster_food").split(",").length];
            for (int i = 0; i < this.monsterBoosts.length; ++i) {
                this.monsterBoosts[i] = new MonsterBooster(Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[0]), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[1]), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[2]), Enumerable.stringToIntArray(result.getString("monster_food").split(",")[i].split(";")[3], ":"), Integer.parseInt(result.getString("monster_food").split(",")[i].split(";")[4]), result.getString("monster_food").split(",")[i].split(";").length > 5 ? result.getString("monster_food").split(",")[i].split(";")[5] : null);
            }
        }
        this.minDurationBeforeMeal = result.getInt("min_duration_before_meal");
        this.maxDurationBeforeMeal = result.getInt("max_duration_before_meal");
        this.possibleEffects = ItemTemplateDAO.readDiceEffects(result.getBytes("possible_effects"));
        if (result.getBytes("max_effects") != null) {
            this.maxEffects = this.possibleEffects = ItemTemplateDAO.readDiceEffects(result.getBytes("max_effects"));
        }
        this.hormone = result.getInt("total_points");
        if (Logs.DEBUG) {
            this.verify();
        }
    }



    public boolean canEat(ItemTemplate Item) {
        return Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.getItemID()).toArray(), Item.getId()) || Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.getItemID()).toArray(), Item.getTypeId());
    }

    public EffectInstance getEffect(int id) {
        return Arrays.stream(possibleEffects).filter(x -> x.effectId == id).findFirst().orElse(null);
    }

    public EffectInstanceDice getEffectDice(int id) {
        return Arrays.stream(possibleEffects).filter(x -> x.effectId == id).map(e -> ((EffectInstanceDice)e)).findFirst().orElse(null);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void verify() {
        if(this.Id == 15110 || this.Id == 15254) return;
        int[] MonsterEffect = new int[0];
        for (MonsterBooster b : this.monsterBoosts) {
            for (int i : b.stats) {
                MonsterEffect = ArrayUtils.add(MonsterEffect, i);
                if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i)) {
                    System.out.println("Familier" + Id + "undefinied statm " + i);
                    for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                        System.out.println("You must set " + io);
                    }
                } /*else {
                    EffectInstanceDice a = (EffectInstanceDice) Arrays.stream(possibleEffects).filter(x -> x.effectId == i).findFirst().get();
                    int total = (int) a.diceNum >= (int) a.diceSide ? a.diceNum : a.diceSide;
                    if (total != (int) (b.getStatsBoost(i) * (hormone / b.point))) {
                        System.out.println("ErreurEffect stat " + i + " Familier " + id + " StatsNormalMax " + ((int) a.diceNum >= (int) a.diceSide ? a.diceNum : a.diceSide) + " != " + b.getStatsBoost(i)  * (hormone / b.point));
                        int maySet = total / (hormone / b.point);
                        System.out.println("Familier" + ItemDAO.dofusMaps.get(id).nameId + "id " + id + " getMonster " + b.monsterFamily + " you should put " + b.monsterFamily + ";" + b.deathNumber + ";" + b.point + ";" + Enumerable.join(b.stats, ':') + ";" + maySet);
                    }
                }*/

            }
        }
        for (EffectInstance i : this.possibleEffects) {
            if (!Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.getStats()).toArray(), i.effectId) && !Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.getStats()).toArray(), i.effectId) && !Ints.contains(MonsterEffect, i.effectId)) {
                System.out.println("Familier" + Id + " stat not set " + i.effectId);
            }
        }
        for (FoodItem i : this.foodItems) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.getStats())) {
                System.out.println("Familier" + Id + "undefinied stat " + i.getStats());
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }
        for (FoodItem i : this.foodTypes) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.getStats())) {
                System.out.println("Familier" + Id + "undefinied stat " + i.getStats());
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }

    }

}
