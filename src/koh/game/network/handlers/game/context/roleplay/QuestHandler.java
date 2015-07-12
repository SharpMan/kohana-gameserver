package koh.game.network.handlers.game.context.roleplay;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.messages.game.context.roleplay.quest.QuestListMessage;
import koh.protocol.messages.game.context.roleplay.quest.QuestListRequestMessage;
import koh.protocol.types.context.roleplay.quest.QuestActiveInformations;

/**
 *
 * @author Neo-Craft
 */
public class QuestHandler {

    @HandlerAttribute(ID = QuestListRequestMessage.MESSAGE_ID)
    public static void HandleQuestListRequestMessage(WorldClient Client, Message message) {
        Client.SequenceMessage(new QuestListMessage(new int[0], new int[0], new QuestActiveInformations[0],new int[0]));

    }

}
