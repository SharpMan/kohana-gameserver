/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class PanoplieBonusCriterion extends Criterion {

    public static String Identifier = "Pk";
    public Integer PanoplieCount;

    @Override
    public String toString() {
        return this.FormatToString("Pk");
    }

    @Override
    public void Build() {
        this.PanoplieCount = Integer.parseInt(Literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.inventoryCache.getItemSetCount(), this.PanoplieCount);
    }
}
