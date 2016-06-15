package koh.game.network.handlers.character;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.PlayerInst;
import koh.game.entities.command.PlayerCommand;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.MapPosition;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.item.Weapon;
import koh.game.entities.kolissium.ArenaParty;
import koh.game.network.ChatChannel;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.chat.ChatClientMultiMessage;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.*;

import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.chat.ChannelEnablingMessage;
import koh.protocol.messages.game.chat.ChatClientMultiWithObjectMessage;
import koh.protocol.messages.game.chat.ChatClientPrivateMessage;
import koh.protocol.messages.game.chat.ChatClientPrivateWithObjectMessage;
import koh.protocol.messages.game.chat.ChatErrorMessage;
import koh.protocol.messages.game.chat.ChatServerCopyMessage;
import koh.protocol.messages.game.chat.ChatServerCopyWithObjectMessage;
import koh.protocol.messages.game.chat.ChatServerMessage;
import koh.protocol.messages.game.chat.ChatServerWithObjectMessage;
import koh.protocol.messages.game.chat.EnabledChannelsMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.messages.game.tinsel.OrnamentGainedMessage;
import koh.protocol.messages.messages.game.tinsel.TitleGainedMessage;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.SetCharacterRestrictionsMessage;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.look.SubEntity;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.PaddockItem;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class ChatHandler {

    @HandlerAttribute(ID = ChannelEnablingMessage.MESSAGE_ID)
    public static void HandleChannelEnablingMessage(WorldClient Client, ChannelEnablingMessage message) {
        //System.out.println(message.channel + "" + message.enable);
        if (message.channel < CHANNEL_GLOBAL || message.channel > CHANNEL_ARENA) {

            Client.send(new BasicNoOperationMessage());
        } else if (message.enable && !Client.getCharacter().getEnabledChannels().contains(message.channel)) {
            Client.getCharacter().getEnabledChannels().add(message.channel);
            Client.getCharacter().getDisabledChannels().remove(Client.getCharacter().getDisabledChannels().indexOf(message.channel));
        } else if (!message.enable && !Client.getCharacter().getDisabledChannels().contains(message.channel)) {
            Client.getCharacter().getDisabledChannels().add(message.channel);
            Client.getCharacter().getEnabledChannels().remove(Client.getCharacter().getEnabledChannels().indexOf(message.channel));
        } else {
            //TODO: Fix this stupid flood message
            if (DAO.getSettings().getBoolElement("Logging.Debug")) {
                System.out.println("Eroror " + message.enable + " " + message.channel);
                for (byte b : Client.getCharacter().getEnabledChannels()) {
                    System.out.println("enabled " + b);
                }
                for (byte b : Client.getCharacter().getDisabledChannels()) {
                    System.out.println("Disabled " + b);
                }
            }
            /*client.sendPacket(new BasicNoOperationMessage());
             return;*/
        }

        Client.send(new EnabledChannelsMessage(Client.getCharacter().getEnabledChannels(), Client.getCharacter().getDisabledChannels()));
    }

    @HandlerAttribute(ID = 852)
    public static void handleChatClientPrivateWithObjectMessage(WorldClient client, ChatClientPrivateWithObjectMessage message) {
        if (message.objects == null) {
            handleChatClientPrivateMessage(client, message);
            return;
        }
        Player target = DAO.getPlayers().getCharacter(message.Receiver);
        if (target == null || target.getClient() == null) {
            client.send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            client.send(new ChatServerCopyWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, message.content, (int) Instant.now().getEpochSecond(), "az", target.getID(), target.getNickName(), message.objects));
            target.send(new ChatServerWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
        }
    }

    @HandlerAttribute(ID = ChatClientPrivateMessage.MESSAGE_ID)
    public static void handleChatClientPrivateMessage(WorldClient client, ChatClientPrivateMessage message) {
        Player target = DAO.getPlayers().getCharacter(message.Receiver);
        if (target == null || target.getClient() == null) {
            client.send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            client.send(new ChatServerCopyMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, message.content, (int) Instant.now().getEpochSecond(), "az", target.getID(), target.getNickName()));
            target.send(new ChatServerMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
        }
    }

    @HandlerAttribute(ID = 862)
    public static void handleChatClientMultiWithObjectMessage(WorldClient client, ChatClientMultiWithObjectMessage message) {
        if (message.objects == null) {
            handleChatClientMultiMessage(client, message);
            return;
        }
        switch (message.channel) {
            case CHANNEL_ARENA:
                if (client.getParty() == null || !(client.getParty() instanceof ArenaParty)) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'un groupe Kolizeum.");
                    return;
                }
                client.getParty().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                break;
            case CHANNEL_TEAM:
                if (client.getCharacter().getFighter() != null) {
                    client.getCharacter().getFighter().getTeam().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                }
                break;
            case CHANNEL_GLOBAL:
                if (client.getCharacter().getFighter() != null) {
                    client.getCharacter().getFight().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                } else {
                    client.getCharacter().getCurrentMap().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                }
                break;
            case CHANNEL_ADMIN:
                ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));

                break;
            case CHANNEL_GUILD:
                if (client.getCharacter().getGuild() == null) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                client.getCharacter().getGuild().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                break;
            case CHANNEL_PARTY:
                if (client.getParty() == null) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                client.getParty().sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (client.getLastChannelMessage().get(message.channel) + 60000L > System.currentTimeMillis()) {
                    client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((client.getLastChannelMessage().get(message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerWithObjectMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, message.objects));
                client.getLastChannelMessage().put(message.channel, System.currentTimeMillis());
                break;
            default:
                return;
        }
    }

    @HandlerAttribute(ID = ChatClientMultiMessage.MESSAGE_ID)
    public static void handleChatClientMultiMessage(WorldClient client, ChatClientMultiMessage message) {
        if(PlayerInst.isMuted(client.getCharacter().getID())){
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR,124, String.valueOf(PlayerInst.muteTime(client.getCharacter().getID()))));
            return;
        }
        switch (message.channel) {
            case CHANNEL_ARENA:
                if (client.getParty() == null || !(client.getParty() instanceof ArenaParty)) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'un groupe Kolizeum.");
                    return;
                }
                client.getParty().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                break;
            case CHANNEL_TEAM:
                if (client.getCharacter().getFighter() != null) {
                    client.getCharacter().getFighter().getTeam().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                }
                break;
            case CHANNEL_GLOBAL:
                if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
                    client.getCharacter().getFight().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                }
                else if(message.content.startsWith(".")){
                    String [] args = message.content.split(" ");
                    PlayerCommand chatCommand = DAO.getCommands().findChatCommand(args[0].substring(1));

                    if(chatCommand != null){
                        chatCommand.call(client,message.content.substring(args.length > 1 ? (args[0].length() +1) : args[0].length()));
                    }
                    else{
                        PlayerController.sendServerMessage(client,"<b>Commande introuvable</b> : veuillez joindre la commande .help pour explorer les commandes !","01EA85");
                    }
                }
                else {
                    client.getCharacter().getCurrentMap().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                }
                break;
            case CHANNEL_ADMIN:
                if (message.content.startsWith("!reload")) {
                    DAO.getCommands().start();
                }
                else if (message.content.startsWith("!save")) {
                    client.getCharacter().save(false);
                }
                 else if (message.content.startsWith("!dit")) {
                    client.send(new ChatServerMessage(CHANNEL_GLOBAL, message.content, (int) Instant.now().getEpochSecond(), "az", -1, "Pnj de Merde", client.getAccount().id));
                } else if (message.content.startsWith("!kmar2")) { //Familier
                    int X = Integer.parseInt(message.content.split(" ")[1]);
                    client.getCharacter().getEntityLook().subentities.clear();
                    client.getCharacter().getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET, 0, new EntityLook((short) X, new ArrayList<>(), new ArrayList<>(), new ArrayList<Short>() {
                        {
                            this.add((short) 80);
                        }
                    }, new ArrayList<>())));
                    client.getCharacter().refreshEntitie();
                } else if (message.content.startsWith("!kmar")) { //Aparence item
                    short X = Short.parseShort(message.content.split(" ")[1]);
                    client.getCharacter().getEntityLook().skins.clear();
                    client.getCharacter().getEntityLook().skins.addAll(client.getCharacter().getSkins());
                    client.getCharacter().getEntityLook().skins.add(X);
                    client.getCharacter().refreshEntitie();
                } else {
                    ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                }
                break;
            case CHANNEL_GUILD:
                if (client.getCharacter().getGuild() == null) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                client.getCharacter().getGuild().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                break;
            case CHANNEL_PARTY:
                if (client.getParty() == null) {
                    PlayerController.sendServerMessage(client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                client.getParty().sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (client.getLastChannelMessage().get(message.channel) + 60000L > System.currentTimeMillis()) {
                    client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((client.getLastChannelMessage().get(message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
                client.getLastChannelMessage().put(message.channel, System.currentTimeMillis());
                break;
            default:
                return;
        }

    }

}
