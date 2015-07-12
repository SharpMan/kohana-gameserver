package koh.game.dao;

import koh.d2o.d2oReader;
import koh.d2o.entities.Breed;
import koh.d2o.entities.Effect;
import koh.d2o.entities.Head;
import koh.d2o.entities.SpellState;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class D2oDao {

    private static d2oReader d2oBreeds, d2oHeads, d2oEffects, d2oStates = null;

    public static void Initialize() {
        d2oEffects = new d2oReader("data/Effects.d2o");
        d2oBreeds = new d2oReader("data/Breeds.d2o");
        d2oHeads = new d2oReader("data/Heads.d2o");
        d2oStates = new d2oReader("data/SpellStates.d2o");
    }

    public static SpellState getState(FightStateEnum state) {
        return d2oReader.SpellStates.get(state.value);
    }

    public static SpellState getState(int state) {
        return d2oReader.SpellStates.get(state);
    }

    public static Effect getEffect(int effectid) {
        return d2oReader.Effects.get(effectid);
    }

    public static Head getHead(int head) {
        return d2oReader.Heads.get(head);
    }

    public static Breed getBreed(int breedId) {
        return d2oReader.Breeds.get(breedId - 1);
    }

}
