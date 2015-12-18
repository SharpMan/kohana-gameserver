package koh.game.network.handlers.game.context;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.SMILEY_CHANNEL;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.chat.smiley.*;

/**
 *
 * @author Neo-Craft
 */
public class SmileyHandler {

    @HandlerAttribute(ID = MoodSmileyRequestMessage.MESSAGE_ID)
    public static void HandleMoodSmileyRequestMessage(WorldClient Client, MoodSmileyRequestMessage message) {
        //1 Error
        //2 FloodMod
        Client.character.setMoodSmiley(message.smileyId);
        Client.send(new MoodSmileyResultMessage((byte) 0, message.smileyId));
    }

    @HandlerAttribute(ID = ChatSmileyRequestMessage.MESSAGE_ID)
    public static void HandleChatSmileyRequestMessage(WorldClient Client, ChatSmileyRequestMessage Message) {
        if (Client.lastChannelMessage.get(SMILEY_CHANNEL) + 5000L > System.currentTimeMillis()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.lastChannelMessage.get(SMILEY_CHANNEL) + 5000L - System.currentTimeMillis()) / 1000) + ""}));
            return;
        }
        Client.character.getCurrentMap().sendToField(new ChatSmileyMessage(Client.character.getID(), Message.smileyId, 0));
        Client.lastChannelMessage.put(SMILEY_CHANNEL, System.currentTimeMillis());
    }

}
