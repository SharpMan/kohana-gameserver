package koh.game.actions;

import java.util.Arrays;

import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.NpcDAOImpl;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcMessage;
import koh.game.entities.actors.npc.NpcReply;
import koh.game.entities.actors.npc.replies.TalkReply;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogCreationMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogQuestionMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;

/**
 *
 * @author Neo-Craft
 */
public class NpcDialog extends GameAction {

    public Npc NPC;

    public NpcDialog(Npc Pnj, IGameActor Actor) {
        super(GameActionTypeEnum.NPC_DAILOG, Actor);
        this.NPC = Pnj;
    }

    public void ChangeMessage(int id, int pos) {
        NpcMessage Message = null;
        try {
            Message = NpcDAOImpl.messages.get(this.NPC.Template().getDialogMessage(id, pos)).GetMessage((Player) this.Actor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Message == null) {
            PlayerController.SendServerMessage(this.GetClient(), "Discours de Pnj Introuvable ...");
            try {
                this.GetClient().EndGameAction(this.ActionType);
            } catch (Exception e) {
            }
            return;
        }
        Actor.Send(new NpcDialogQuestionMessage(Message.Id, Message.GetParameters((Player) this.Actor), Message.Replies != null ? Message.Replies : this.NPC.Template().GetReply(id)));
    }

    public void ChangeMessage(NpcMessage Message) {
        if (Message == null) {
            try {
                this.GetClient().EndGameAction(this.ActionType);
            } catch (Exception e) {
            }
            return;
        }
        Actor.Send(new NpcDialogQuestionMessage(Message.Id, Message.Parameters, Message.Replies != null ? Message.Replies : this.NPC.Template().GetReply(this.NPC.Template().GetMessageOffset(Message.Id))));
    }

    public void Reply(int rep) {
        NpcReply[] Stream = NpcDAOImpl.replies.stream().filter(x -> x.ReplyID == rep).toArray(NpcReply[]::new);
        if (!Arrays.stream(Stream).anyMatch(x -> x instanceof TalkReply)) {
            this.GetClient().EndGameAction(GameActionTypeEnum.NPC_DAILOG);
        }
        for (NpcReply x : Stream) {
            x.Execute(((Player) Actor));
        }
        if (Stream.length == 0) {
            Main.Logs().writeDebug("Undefinied reponse " + rep);
            PlayerController.SendServerMessage(((Player) Actor).Client, "Ce discours n'est pas encore parametré...");
        }

    }

    @Override
    public void Execute() {
        this.Actor.Send(new NpcDialogCreationMessage(NPC.Cell.Map.Id, NPC.ID));
        this.ChangeMessage(0, 0);
        super.Execute();
    }

    @Override
    public void Abort(Object[] Args) {
        super.Abort(Args);

    }

    @Override
    public void EndExecute() throws Exception {
        Actor.Send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_DIALOG));
        super.EndExecute();
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
