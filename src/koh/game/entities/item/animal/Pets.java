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
public class Pets {

    public int Id;
    public FoodItem[] foodItems, foodTypes;
    public MonsterBooster[] MonsterBoosts;
    public int minDurationBeforeMeal, maxDurationBeforeMeal, Hormone;
    public EffectInstance[] possibleEffects, maxEffects;

    public boolean canEat(ItemTemplate Item) {
        return Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.ItemID).toArray(), Item.id) || Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.ItemID).toArray(), Item.TypeId);
    }

    public EffectInstance getEffect(int id) {
       return Arrays.stream(possibleEffects).filter(x -> x.effectId == id).findFirst().orElse(null);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void Verif() {
        int[] MonsterEffect = new int[0];
        for (MonsterBooster b : this.MonsterBoosts) {
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
                    if (total != (int) (b.getStatsBoost(i) * (Hormone / b.Point))) {
                        System.out.println("ErreurEffect stat " + i + " Familier " + Id + " StatsNormalMax " + ((int) a.diceNum >= (int) a.diceSide ? a.diceNum : a.diceSide) + " != " + b.getStatsBoost(i)  * (Hormone / b.Point));
                        int maySet = total / (Hormone / b.Point);
                        System.out.println("Familier" + ItemDAO.Cache.get(Id).nameId + "ID " + Id + " Monster " + b.MonsterFamily + " you should put " + b.MonsterFamily + ";" + b.DeathNumber + ";" + b.Point + ";" + Enumerable.Join(b.Stats, ':') + ";" + maySet);
                    }
                }*/

            }
        }
        for (EffectInstance i : this.possibleEffects) {
            if (!Ints.contains(Arrays.stream(foodItems).mapToInt(x -> x.Stats).toArray(), i.effectId) && !Ints.contains(Arrays.stream(foodTypes).mapToInt(x -> x.Stats).toArray(), i.effectId) && !Ints.contains(MonsterEffect, i.effectId)) {
                System.out.println("Familier" + Id + " stat not set " + i.effectId);
            }
        }
        for (FoodItem i : this.foodItems) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.Stats)) {
                System.out.println("Familier" + Id + "undefinied stat " + i.Stats);
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }
        for (FoodItem i : this.foodTypes) {
            if (!Ints.contains(Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray(), i.Stats)) {
                System.out.println("Familier" + Id + "undefinied stat " + i.Stats);
                for (int io : Arrays.stream(possibleEffects).mapToInt(x -> x.effectId).toArray()) {
                    System.out.println("You must set " + io);
                }
            }
        }

    }

}
