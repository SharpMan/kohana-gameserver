package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.protocol.messages.game.context.roleplay.spell.SpellForgetUIMessage;

/**
 * Created by Melancholia on 12/25/16.
 */
public class GameSpellUI extends GameAction {

    private final Player player;

    public GameSpellUI(Player actor) {
        super(GameActionTypeEnum.SPELL_UI, actor);
        this.player = actor;
    }

    @Override
    public void execute() {
        player.send(new SpellForgetUIMessage(true));
    }


    @Override
    public void endExecute() {
        player.send(new SpellForgetUIMessage(false));
        try {
            super.endExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }
}
