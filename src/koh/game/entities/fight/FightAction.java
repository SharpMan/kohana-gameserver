package koh.game.entities.fight;

import lombok.Getter;

/**
 * Created by Melancholia on 8/28/16.
 */
public class FightAction {

    @Getter
    private final FightActionType action;
    @Getter
    private final String param;

    public FightAction(FightActionType action, String param) {
        this.action = action;
        this.param = param;
    }
}
