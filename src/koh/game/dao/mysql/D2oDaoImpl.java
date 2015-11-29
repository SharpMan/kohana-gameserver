package koh.game.dao.mysql;

import koh.d2o.d2oReader;
import koh.d2o.entities.Breed;
import koh.d2o.entities.Effect;
import koh.d2o.entities.Head;
import koh.d2o.entities.SpellState;
import koh.game.dao.api.D2oDAO;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class D2oDaoImpl extends D2oDAO {

    private static d2oReader d2oBreeds, d2oHeads, d2oEffects, d2oStates = null;

    public void loadAll() {
        d2oEffects = new d2oReader("data/Effects.d2o");
        d2oBreeds = new d2oReader("data/Breeds.d2o");
        d2oHeads = new d2oReader("data/Heads.d2o");
        d2oStates = new d2oReader("data/SpellStates.d2o");
    }

    public SpellState getState(FightStateEnum state) {
        return d2oReader.SpellStates.get(state.value);
    }

    public SpellState getState(int state) {
        return d2oReader.SpellStates.get(state);
    }

    public Effect getEffect(int effectid) {
        return d2oReader.Effects.get(effectid);
    }

    public Head getHead(int head) {
        return d2oReader.Heads.get(head);
    }

    public Breed getBreed(int breedId) {
        return d2oReader.Breeds.get(breedId - 1);
    }

    @Override
    public void start() {
        this.loadAll();
    }

    @Override
    public void stop() {

    }
}
