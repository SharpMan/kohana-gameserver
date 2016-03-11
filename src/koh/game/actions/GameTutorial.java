package koh.game.actions;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcTemplate;
import koh.game.utils.TutorialText;
import koh.protocol.messages.game.chat.ChatServerMessage;
import koh.protocol.messages.game.context.roleplay.GameRolePlayShowActorMessage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static koh.protocol.client.enums.ChatActivableChannelsEnum.CHANNEL_GLOBAL;

/**
 * Created by Melancholia on 3/6/16.
 */
public class GameTutorial extends GameAction {

    private final Player character;
    private Future future;

    private final static NpcTemplate npc = DAO.getNpcs().findTemplate(309);
    private final static ExecutorService executor = Executors.newFixedThreadPool(45);

    public GameTutorial(Player actor) {
        super(GameActionTypeEnum.TUTORIAL, actor);
        this.character = actor;
    }

    private void say(String message) {
        character.send(new ChatServerMessage(CHANNEL_GLOBAL, message, (int) Instant.now().getEpochSecond(), "az", -100, "Ybaul, Greffière", character.getAccount().id));
    }

    @Override
    public void execute() {
        character.send(new GameRolePlayShowActorMessage(npc.getProjection((short) 415, (byte) 1)));
        this.future = executor.submit((Runnable) () -> {
            try {

                for (Map.Entry<Integer, String> text : TutorialText.getTexts()) {
                    say(text.getValue());
                    Thread.sleep(text.getKey());
                }
                character.getClient().endGameAction(GameActionTypeEnum.TUTORIAL);
                return;
            } catch (InterruptedException e) {
            }
        });
        if(future.isDone()){
            super.execute();
        }
    }

    @Override
    public void endExecute() {
        try {
            this.future.cancel(true);
        } catch (Exception e) {
        }
        character.setOnTutorial(false);
        character.teleport(115083777, 474);
        try {
            super.endExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public void abort(Object[] Args) {
        try {
            character.setOnTutorial(false);
            this.future.cancel(true);
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
