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
public class SpecializationCriterion extends Criterion {

    public static String Identifier = "Pr";

    @Override
    public String toString() {
        return this.FormatToString("Pr");
    }

    @Override
    public void Build() {
        
    }

    @Override
    public boolean eval(Player character) {
         return true;
    }
}
