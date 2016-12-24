package koh.game.actions;

import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.actors.pnj.NpcTemplate;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogCreationMessage;
import koh.protocol.messages.game.context.roleplay.npc.TaxCollectorDialogQuestionBasicMessage;
import koh.protocol.messages.game.context.roleplay.npc.TaxCollectorDialogQuestionExtendedMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Melancholia on 12/4/16.
 */
public class TaxCollectorDialog extends GameAction {

    private static final Logger logger = LogManager.getLogger(TaxCollectorDialog.class);

    private static final NpcTemplate NPC_TEMPLATE = DAO.getNpcs().findTemplate(1);

    public TaxCollector NPC;

    public TaxCollectorDialog(TaxCollector Pnj, IGameActor Actor) {
        super(GameActionTypeEnum.TAX_COLLECTOR_DIALOG, Actor);
        this.NPC = Pnj;
    }

    @Override
    public void execute() {
        this.actor.send(new NpcDialogCreationMessage(NPC.getMapid(), NPC.getID()));
        if(this.getClient().getCharacter().getGuild() == NPC.getGuild()){
            this.actor.send(new TaxCollectorDialogQuestionExtendedMessage(NPC.getGuild().getBasicGuildInformations(), NPC.getGuild().getEntity().pods, NPC.getGuild().getEntity().prospecting, NPC.getGuild().getEntity().wisdom, (byte)NPC.getGuild().getEntity().maxTaxCollectors, NPC.getAttacksCount(), NPC.getKamas(), NPC.getExperience(), NPC.getBagSize(), NPC.getItemValues()));
        }
        else
            this.actor.send(new TaxCollectorDialogQuestionBasicMessage(NPC.getGuild().getBasicGuildInformations()));
        //this.changeMessage(0, 0);
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
    public boolean canSubAction(GameActionTypeEnum actionType) {
        if(actionType == GameActionTypeEnum.BASIC_REQUEST ||
                actionType == GameActionTypeEnum.GROUP ||
                actionType == GameActionTypeEnum.INTERACTIVE_ELEMENT ||
                actionType == GameActionTypeEnum.ZAAP ||
                actionType == GameActionTypeEnum.MAP_MOVEMENT ||
                actionType == GameActionTypeEnum.CHANGE_MAP){
            return true;
        }
        return false;
    }

}
