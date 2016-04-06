package koh.game.actions;

import java.util.Arrays;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcMessage;
import koh.game.entities.actors.pnj.NpcReply;
import koh.game.entities.actors.pnj.replies.TalkReply;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogCreationMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogQuestionMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class NpcDialog extends GameAction {

    private static final Logger logger = LogManager.getLogger(NpcDialog.class);

    public Npc NPC;

    public NpcDialog(Npc Pnj, IGameActor Actor) {
        super(GameActionTypeEnum.NPC_DAILOG, Actor);
        this.NPC = Pnj;
    }

    public void changeMessage(int id, int pos) {
        NpcMessage Message = null;
        try {
            Message = DAO.getNpcs().findMessage(this.NPC.getTemplate().getDialogMessage(id, pos)).getMessage((Player) this.actor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Message == null) {
            PlayerController.sendServerMessage(this.getClient(), "Discours de Pnj Introuvable ...");
            try {
                this.getClient().endGameAction(this.actionType);
            } catch (Exception e) {
            }
            return;
        }
        actor.send(new NpcDialogQuestionMessage(Message.getId(), Message.getParameters((Player) this.actor), Message.getReplies() != null ? Message.getReplies() : this.NPC.getTemplate().getReply(id)));
    }

    public void changeMessage(NpcMessage Message) {
        if (Message == null) {
            try {
                this.getClient().endGameAction(this.actionType);
            } catch (Exception e) {
            }
            return;
        }
        actor.send(new NpcDialogQuestionMessage(Message.getId(), Message.getParameters(), Message.getReplies() != null ? Message.getReplies() : this.NPC.getTemplate().getReply(this.NPC.getTemplate().getMessageOffset(Message.getId()))));
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
        this.actor.send(new NpcDialogCreationMessage(NPC.getCell().getMap().getId(), NPC.getID()));
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
