package koh.game.actions;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.actors.pnj.NpcMessage;
import koh.game.entities.actors.pnj.NpcReply;
import koh.game.entities.actors.pnj.NpcTemplate;
import koh.game.entities.actors.pnj.replies.TalkReply;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogCreationMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogQuestionMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by Melancholia on 12/4/16.
 */
public class TaxCollectorDialog extends GameAction {

    private static final Logger logger = LogManager.getLogger(NpcDialog.class);

    private static final NpcTemplate NPC_TEMPLATE = DAO.getNpcs().findTemplate(1);

    public TaxCollector NPC;

    public TaxCollectorDialog(TaxCollector Pnj, IGameActor Actor) {
        super(GameActionTypeEnum.TAX_COLLECTOR_DIALOG, Actor);
        this.NPC = Pnj;
    }

    public void changeMessage(int id, int pos) {
        NpcMessage message = null;
        try {
            message = DAO.getNpcs().findMessage(NPC_TEMPLATE.getDialogMessage(id, pos)).getMessage((Player) this.actor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (message == null) {
            PlayerController.sendServerMessage(this.getClient(), "Discours de Pnj Introuvable ...");
            try {
                this.getClient().endGameAction(this.actionType);
            } catch (Exception e) {
            }
            return;
        }
        actor.send(new NpcDialogQuestionMessage(message.getId(), message.getParameters((Player) this.actor), message.getReplies() != null ? message.getReplies() : NPC_TEMPLATE.getReply(id)));
    }

    public void changeMessage(NpcMessage msg) {
        if (msg == null) {
            try {
                this.getClient().endGameAction(this.actionType);
            } catch (Exception e) {
            }
            return;
        }
        actor.send(new NpcDialogQuestionMessage(msg.getId(), msg.getParameters(), msg.getReplies() != null ? msg.getReplies() : NPC_TEMPLATE.getReply(NPC_TEMPLATE.getMessageOffset(msg.getId()))));
    }

    public void reply(int rep) {
        final NpcReply[] stream = DAO.getNpcs().repliesAsStream().filter(x -> x.getReplyID() == rep).toArray(NpcReply[]::new);
        if (!Arrays.stream(stream).anyMatch(x -> x instanceof TalkReply)) {
            this.getClient().endGameAction(GameActionTypeEnum.NPC_DAILOG);
        }
        for (NpcReply x : stream) {
            x.execute(((Player) actor));
        }
        if (stream.length == 0) {
            logger.debug("Undefined reponse ID {} ", rep);
            PlayerController.sendServerMessage(((Player)actor).getClient(), "Ce discours n'est pas encore parametré...");
        }

    }

    @Override
    public void execute() {
        this.actor.send(new NpcDialogCreationMessage(NPC.getMapid(), NPC.getID()));
        this.changeMessage(0, 0);
        super.execute();
    }

    @Override
    public void abort(Object[] Args) {
        super.abort(Args);

    }

    @Override
    public void endExecute() throws Exception {
        actor.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_DIALOG));
        super.endExecute();
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
