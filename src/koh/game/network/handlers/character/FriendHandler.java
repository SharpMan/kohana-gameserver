package koh.game.network.handlers.character;

import koh.game.dao.DAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.AccountData.FriendContact;
import koh.game.entities.AccountData.IgnoredContact;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.client.enums.ListAddFailureEnum;
import koh.protocol.client.enums.PlayerStateEnum;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.friend.FriendAddFailureMessage;
import koh.protocol.messages.game.friend.FriendAddRequestMessage;
import koh.protocol.messages.game.friend.FriendAddedMessage;
import koh.protocol.messages.game.friend.FriendDeleteRequestMessage;
import koh.protocol.messages.game.friend.FriendDeleteResultMessage;
import koh.protocol.messages.game.friend.FriendSetWarnOnConnectionMessage;
import koh.protocol.messages.game.friend.FriendSetWarnOnLevelGainMessage;
import koh.protocol.messages.game.friend.FriendWarnOnConnectionStateMessage;
import koh.protocol.messages.game.friend.FriendWarnOnLevelGainStateMessage;
import koh.protocol.messages.game.friend.FriendsGetListMessage;
import koh.protocol.messages.game.friend.IgnoredGetListMessage;
import koh.protocol.messages.game.friend.IgnoredListMessage;
import koh.protocol.messages.game.friend.SpouseGetInformationsMessage;
import koh.protocol.messages.game.friend.FriendsListMessage;
import koh.protocol.messages.game.friend.GuildMemberSetWarnOnConnectionMessage;
import koh.protocol.messages.game.friend.GuildMemberWarnOnConnectionStateMessage;
import koh.protocol.messages.game.friend.IgnoredAddFailureMessage;
import koh.protocol.messages.game.friend.IgnoredAddRequestMessage;
import koh.protocol.messages.game.friend.IgnoredAddedMessage;
import koh.protocol.messages.game.friend.IgnoredDeleteRequestMessage;
import koh.protocol.messages.game.friend.IgnoredDeleteResultMessage;
import koh.protocol.messages.game.friend.SpouseStatusMessage;
import koh.protocol.types.game.context.roleplay.BasicGuildInformations;
import koh.protocol.types.game.friend.FriendOnlineInformations;
import koh.protocol.types.game.friend.IgnoredOnlineInformations;

/**
 *
 * @author Neo-Craft
 */
public class FriendHandler {

    @HandlerAttribute(ID = IgnoredAddRequestMessage.M_ID)
    public static void HandleIgnoredAddRequestMessage(WorldClient Client, IgnoredAddRequestMessage message) {
        Player target = DAO.getPlayers().getCharacter(message.name);
        if (target == null || target.getClient() == null) {
            Client.send(new IgnoredAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (Client.getAccount().accountData.ignore(target.getAccount().id)) {
            Client.send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_IS_DOUBLE));
        } else if (Client.getAccount().accountData.ignored.length >= ListAddFailureEnum.MAX_QUOTA) {
            Client.send(new IgnoredAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_OVER_QUOTA));
        } else {
            Client.getAccount().accountData.addIgnored(new IgnoredContact() {
                {
                    accountID = target.getAccount().id;
                    accountName = target.getAccount().nickName;
                }
            });
            Client.send(new IgnoredAddedMessage(new IgnoredOnlineInformations(target.getAccount().id, target.getAccount().nickName, target.getID(), target.getNickName(), target.getBreed(), target.hasSexe()), message.session));
        }
    }

    @HandlerAttribute(ID = FriendAddRequestMessage.ID)
    public static void HandleFriendAddRequestMessage(WorldClient Client, FriendAddRequestMessage Message) {
        Player target = DAO.getPlayers().getCharacter(Message.Name);
        if (target == null || target.getClient() == null) {
            Client.send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (target.getAccount() == null || target.getAccount().accountData == null) {
            Client.send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (Client.getAccount().accountData.hasFriend(target.getAccount().id)) {
            Client.send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_IS_DOUBLE));
        } else if (Client.getAccount().accountData.friends.length >= ListAddFailureEnum.MAX_QUOTA) {
            Client.send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_OVER_QUOTA));
        } else {
            Client.getAccount().accountData.addFriend(new FriendContact() {
                {
                    accountID = target.getAccount().id;
                    accountName = target.getAccount().nickName;
                    lastConnection = System.currentTimeMillis();
                    achievementPoints = target.getAchievementPoints();
                }
            });
            if (target.getAccount().accountData.hasFriend(Client.getAccount().id)) {
                Client.send(new FriendAddedMessage(new FriendOnlineInformations(target.getAccount().id, target.getAccount().nickName, target.getPlayerState(), -1, target.getAchievementPoints(), target.getID(), target.getNickName(), (byte) target.getLevel(), target.getAlignmentSide().value, target.getBreed(), target.hasSexe(), target.getBasicGuildInformations(), target.getMoodSmiley(), new PlayerStatus(target.getStatus().value()))));
            } else {
                Client.send(new FriendAddedMessage(new FriendOnlineInformations(target.getAccount().id, target.getAccount().nickName, PlayerStateEnum.UNKNOWN_STATE, -1, target.getAchievementPoints(), target.getID(), target.getNickName(), (byte) 0, (byte) -1, target.getBreed(), target.hasSexe(), new BasicGuildInformations(0, ""), (byte) -1, new PlayerStatus(target.getStatus().value()))));
            }
        }
    }

    @HandlerAttribute(ID = IgnoredDeleteRequestMessage.M_ID)
    public static void HandleIgnoredDeleteRequestMessage(WorldClient Client, IgnoredDeleteRequestMessage Message) {
        IgnoredContact Contact = Client.getAccount().accountData.getIgnored(Message.accountId);
        if (Contact == null) {
            Client.send(new IgnoredDeleteResultMessage(false, "", Message.session));
        } else {
            Client.getAccount().accountData.removeIgnored(Contact);
            Client.send(new IgnoredDeleteResultMessage(true, Contact.accountName, Message.session));
        }
    }

    @HandlerAttribute(ID = FriendDeleteRequestMessage.M_ID)
    public static void HandleFriendDeleteRequestMessage(WorldClient Client, FriendDeleteRequestMessage Message) {
        FriendContact Contact = Client.getAccount().accountData.getFriend(Message.accountId);
        if (Contact == null) {
            Client.send(new FriendDeleteResultMessage(false, ""));
        } else {
            Client.getAccount().accountData.removeFriend(Contact);
            Client.send(new FriendDeleteResultMessage(true, Contact.accountName));
        }
    }

    @HandlerAttribute(ID = FriendSetWarnOnConnectionMessage.M_ID)
    public static void HandleFriendSetWarnOnConnectionMessage(WorldClient Client, FriendSetWarnOnConnectionMessage Message) {
        Client.getAccount().accountData.setFriendWarnOnLogin(Message.enable);
        Client.send(new FriendWarnOnConnectionStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = FriendSetWarnOnLevelGainMessage.M_ID)
    public static void HandleFriendSetWarnOnLevelGainMessage(WorldClient Client, FriendSetWarnOnLevelGainMessage Message) {
        Client.getAccount().accountData.setFriendWarnOnLevelGain(Message.enable);
        Client.send(new FriendWarnOnLevelGainStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = GuildMemberSetWarnOnConnectionMessage.M_ID)
    public static void HandleGuildMemberSetWarnOnConnectionMessage(WorldClient Client, GuildMemberSetWarnOnConnectionMessage Message) {
        Client.getAccount().accountData.setFriendWarnOnGuildLogin(Message.enable);
        Client.send(new GuildMemberWarnOnConnectionStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = FriendsGetListMessage.MESSAGE_ID)
    public static void HandleFriendsGetListMessage(WorldClient Client, Message message) {
        Client.sequenceMessage(new FriendsListMessage(Client.getAccount().accountData.getFriendsInformations()));
    }

    @HandlerAttribute(ID = IgnoredGetListMessage.MESSAGE_ID)
    public static void HandleIgnoredGetListMessage(WorldClient Client, Message message) {
        Client.sequenceMessage(new IgnoredListMessage(Client.getAccount().accountData.getIgnoredInformations()));
    }

    @HandlerAttribute(ID = SpouseGetInformationsMessage.MESSAGE_ID)
    public static void HandleSpouseGetInformationsMessage(WorldClient Client, Message message) {
        Client.sequenceMessage(new SpouseStatusMessage(false));
    }

}
