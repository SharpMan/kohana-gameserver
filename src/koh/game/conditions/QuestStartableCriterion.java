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
public class QuestStartableCriterion extends Criterion {

    public static String Identifier = "Qc";
    public Integer QuestId;

    @Override
    public String toString() {
        return this.FormatToString("Qc");
    }

    @Override
    public void Build() {
        this.QuestId = Integer.parseInt(Literal);
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
