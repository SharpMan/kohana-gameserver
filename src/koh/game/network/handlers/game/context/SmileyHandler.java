package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
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
    public static void HandleMoodSmileyRequestMessage(WorldClient client, MoodSmileyRequestMessage message) {
        if(client.getCharacter() == null) {
            client.forceClose();
            return;
        }
        //1 Error
        //2 FloodMod
        client.getCharacter().setMoodSmiley(message.smileyId);
        client.send(new MoodSmileyResultMessage((byte) 0, message.smileyId));
    }

    @HandlerAttribute(ID = ChatSmileyRequestMessage.MESSAGE_ID)
    public static void handleChatSmileyRequestMessage(WorldClient client, ChatSmileyRequestMessage message) {
        if(client.getCharacter() == null) {
            client.forceClose();
            return;
        }
        if (client.getLastChannelMessage().get(SMILEY_CHANNEL) + 5000L > System.currentTimeMillis()) {
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((client.getLastChannelMessage().get(SMILEY_CHANNEL) + 5000L - System.currentTimeMillis()) / 1000) + ""}));
            return;
        }
        if(client.isGameAction(GameActionTypeEnum.FIGHT))
            client.getCharacter().getFight().sendToField(new ChatSmileyMessage(client.getCharacter().getID(), message.smileyId, client.getCharacter().getOwner()));
        else
            client.getCharacter().getCurrentMap().sendToField(new ChatSmileyMessage(client.getCharacter().getID(), message.smileyId, client.getCharacter().getOwner()));

        client.getLastChannelMessage().put(SMILEY_CHANNEL, System.currentTimeMillis());
    }

}
