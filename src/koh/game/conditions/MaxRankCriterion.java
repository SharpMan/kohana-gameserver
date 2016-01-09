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
public class MaxRankCriterion extends Criterion {

    public static String Identifier = "P¨Q";

    @Override
    public String toString() {
        return this.FormatToString("P¨Q");
    }

    @Override
    public void Build() {
        
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }
}
