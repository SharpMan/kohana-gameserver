package koh.game.network.handlers.game.context;

import java.util.Arrays;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.GM_CHANNEL;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.GameContextRefreshEntityLookMessage;
import koh.protocol.messages.game.context.roleplay.GameRolePlayShowActorMessage;
import koh.protocol.messages.messages.game.tinsel.*;
import koh.protocol.types.game.context.roleplay.GameRolePlayActorInformations;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class TinselHandler {

    @HandlerAttribute(ID = TitlesAndOrnamentsListRequestMessage.MESSAGE_ID)
    public static void HandleTitlesAndOrnamentsListRequestMessage(WorldClient Client, TitlesAndOrnamentsListRequestMessage Message) {
        Client.Send(new TitlesAndOrnamentsListMessage(Client.Character.Titles, Client.Character.Ornaments, Client.Character.activableTitle, Client.Character.activableOrnament));
    }

    @HandlerAttribute(ID = TitleSelectRequestMessage.Message_ID)
    public static void HandleTitleSelectRequestMessage(WorldClient Client, TitleSelectRequestMessage Message) {
        if (Client.LastChannelMessage.get(GM_CHANNEL) + 7000L > System.currentTimeMillis()) {
            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.LastChannelMessage.get(GM_CHANNEL) + 7000L - System.currentTimeMillis()) / 1000) + ""}));
            Client.Send(new TitleSelectErrorMessage((byte) 0));
            return;
        }
        if (ArrayUtils.contains(Client.Character.Titles, Message.titleId)) {
            Client.Character.activableTitle = (short) Message.titleId;
            Client.Character.RefreshActor();
            Client.Send(new TitleSelectedMessage(Message.titleId));
            
        } else {
            Client.Send(new TitleSelectErrorMessage((byte) 0));
            Client.LastChannelMessage.put(GM_CHANNEL, System.currentTimeMillis());
        }
    }

    @HandlerAttribute(ID = OrnamentSelectRequestMessage.Message_ID)
    public static void HandleOrnamentSelectRequestMessage(WorldClient Client, OrnamentSelectRequestMessage Message) {
        if (Client.LastChannelMessage.get(GM_CHANNEL) + 7000L > System.currentTimeMillis()) {
            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.LastChannelMessage.get(GM_CHANNEL) + 7000L - System.currentTimeMillis()) / 1000) + ""}));
            Client.Send(new OrnamentSelectErrorMessage((byte) 0));
            return;
        }
        if (ArrayUtils.contains(Client.Character.Ornaments, Message.ornamentId)) {
            Client.Character.activableOrnament = (short) Message.ornamentId;
            Client.Character.RefreshActor();
            Client.Send(new OrnamentSelectedMessage(Message.ornamentId));
        } else {
            Client.Send(new OrnamentSelectErrorMessage((byte) 0));
            Client.LastChannelMessage.put(GM_CHANNEL, System.currentTimeMillis());
        }
    }

}
