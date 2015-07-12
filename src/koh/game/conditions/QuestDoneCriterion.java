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
public class QuestDoneCriterion extends Criterion {

    public static String Identifier = "Qf";
    public Integer QuestId;

    @Override
    public String toString() {
        return this.FormatToString("Qf");
    }

    @Override
    public void Build() {
        this.QuestId = Integer.parseInt(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return true;
    }

}
