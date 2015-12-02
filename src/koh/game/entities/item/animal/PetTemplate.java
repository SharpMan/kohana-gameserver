package koh.game.entities.item.animal;

import com.google.common.primitives.Ints;
import java.util.Arrays;

import koh.game.entities.item.ItemTemplate;
import koh.game.entities.spells.EffectInstance;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class PetTemplate {

    public int Id;
    public FoodItem[] foodItems, foodTypes;
    public MonsterBooster[] monsterBoosts;
    public int minDurationBeforeMeal, maxDurationBeforeMeal, Hormone;
    public EffectInstance[] possibleEffects, maxEffects;

    public boolean canEat(ItemTemplate Item) {
        return Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.itemID).toArray(), Item.id) || Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.itemID).toArray(), Item.typeId);
    }

    public EffectInstance getEffect(int id) {
       return Arrays.stream(possibleEffects).filter(x -> x.effectId == id).findFirst().orElse(null);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void verify() {
        int[] MonsterEffect = new int[0];
        for (MonsterBooster b : this.monsterBoosts) {
            for (int i : b.Stats) {
                MonsterEffect = ArrayUtils.add(MonsterEffect, i);
                if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i)) {
                    System.out.println("Familier" + Id + "undefinied statm " + i);
                    for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                        System.out.println("You must set " + io);
                    }
                } /*else {
                    EffectInstanceDice a = (EffectInstanceDice) Arrays.stream(possibleEffects).filter(x -> x.effectId == i).findFirst().get();
                    int total = (int) a.diceNum >= (int) a.diceSide ? a.diceNum : a.diceSide;
                    if (total != (int) (b.getStatsBoost(i) * (Hormone / b.point))) {
                        System.out.println("ErreurEffect stat " + i + " Familier " + id + " StatsNormalMax " + ((int) a.diceNum >= (int) a.diceSide ? a.diceNum : a.diceSide) + " != " + b.getStatsBoost(i)  * (Hormone / b.point));
                        int maySet = total / (Hormone / b.point);
                        System.out.println("Familier" + ItemDAO.dofusMaps.get(id).nameId + "id " + id + " Monster " + b.MonsterFamily + " you should put " + b.MonsterFamily + ";" + b.DeathNumber + ";" + b.point + ";" + Enumerable.Join(b.stats, ':') + ";" + maySet);
                    }
                }*/

            }
        }
        for (EffectInstance i : this.possibleEffects) {
            if (!Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.stats).toArray(), i.effectId) && !Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.stats).toArray(), i.effectId) && !Ints.contains(MonsterEffect, i.effectId)) {
                System.out.println("Familier" + Id + " stat not set " + i.effectId);
            }
        }
        for (FoodItem i : this.foodItems) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.stats)) {
                System.out.println("Familier" + Id + "undefinied stat " + i.stats);
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }
        for (FoodItem i : this.foodTypes) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.stats)) {
                System.out.println("Familier" + Id + "undefinied stat " + i.stats);
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }

    }

}
