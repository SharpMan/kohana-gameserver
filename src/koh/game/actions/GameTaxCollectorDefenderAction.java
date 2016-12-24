package koh.game.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import lombok.Getter;

/**
 * Created by Melancholia on 12/15/16.
 */
public class GameTaxCollectorDefenderAction extends GameAction {

    @Getter
    private final TaxCollector taxCollector;

    public GameTaxCollectorDefenderAction(Player actor, TaxCollector taxCollector) {
        super(GameActionTypeEnum.DEFEND_TAX_COLLECTOR, actor);
        this.taxCollector = taxCollector;
    }


    @Override
    public void abort(Object[] Args) {
        try {
            taxCollector.getCurrent_fight().getDefenders().remove(actor);
        } catch (Exception e) {
        } finally {
            try {
                super.endExecute();
            } catch (Exception e) {
            }
        }
    }



    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }
}
