package koh.game.network.handlers.game.context;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.GM_CHANNEL;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.messages.game.tinsel.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class TinselHandler {

    @HandlerAttribute(ID = TitlesAndOrnamentsListRequestMessage.MESSAGE_ID)
    public static void HandleTitlesAndOrnamentsListRequestMessage(WorldClient Client, TitlesAndOrnamentsListRequestMessage Message) {
        Client.send(new TitlesAndOrnamentsListMessage(Client.character.getTitles(), Client.character.getOrnaments(), Client.character.getActivableTitle(), Client.character.getActivableOrnament()));
    }

    @HandlerAttribute(ID = TitleSelectRequestMessage.Message_ID)
    public static void HandleTitleSelectRequestMessage(WorldClient Client, TitleSelectRequestMessage Message) {
        if (Client.lastChannelMessage.get(GM_CHANNEL) + 7000L > System.currentTimeMillis()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.lastChannelMessage.get(GM_CHANNEL) + 7000L - System.currentTimeMillis()) / 1000) + ""}));
            Client.send(new TitleSelectErrorMessage((byte) 0));
            return;
        }
        if (ArrayUtils.contains(Client.character.getTitles(), Message.titleId)) {
            Client.character.setActivableTitle((short) Message.titleId);
            Client.character.refreshActor();
            Client.send(new TitleSelectedMessage(Message.titleId));
            
        } else {
            Client.send(new TitleSelectErrorMessage((byte) 0));
            Client.lastChannelMessage.put(GM_CHANNEL, System.currentTimeMillis());
        }
    }

    @HandlerAttribute(ID = OrnamentSelectRequestMessage.Message_ID)
    public static void HandleOrnamentSelectRequestMessage(WorldClient Client, OrnamentSelectRequestMessage Message) {
        if (Client.lastChannelMessage.get(GM_CHANNEL) + 7000L > System.currentTimeMillis()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.lastChannelMessage.get(GM_CHANNEL) + 7000L - System.currentTimeMillis()) / 1000) + ""}));
            Client.send(new OrnamentSelectErrorMessage((byte) 0));
            return;
        }
        if (ArrayUtils.contains(Client.character.getOrnaments(), Message.ornamentId)) {
            Client.character.setActivableOrnament((short) Message.ornamentId);
            Client.character.refreshActor();
            Client.send(new OrnamentSelectedMessage(Message.ornamentId));
        } else {
            Client.send(new OrnamentSelectErrorMessage((byte) 0));
            Client.lastChannelMessage.put(GM_CHANNEL, System.currentTimeMillis());
        }
    }

}
