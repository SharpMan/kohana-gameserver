package koh.game.network.handlers.character;

import java.time.Instant;
import koh.game.dao.PlayerDAO;
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
import koh.protocol.types.game.friend.FriendInformations;
import koh.protocol.types.game.friend.IgnoredInformations;
import koh.protocol.types.game.context.roleplay.BasicGuildInformations;
import koh.protocol.types.game.friend.FriendOnlineInformations;
import koh.protocol.types.game.friend.IgnoredOnlineInformations;

/**
 *
 * @author Neo-Craft
 */
public class FriendHandler {

    @HandlerAttribute(ID = IgnoredAddRequestMessage.M_ID)
    public static void HandleIgnoredAddRequestMessage(WorldClient Client, IgnoredAddRequestMessage Message) {
        Player Target = PlayerDAO.GetCharacter(Message.name);
        if (Target == null || Target.Client == null) {
            Client.Send(new IgnoredAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (Client.getAccount().Data.Ignore(Target.Account.ID)) {
            Client.Send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_IS_DOUBLE));
        } else if (Client.getAccount().Data.Ignored.length >= ListAddFailureEnum.MAX_QUOTA) {
            Client.Send(new IgnoredAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_OVER_QUOTA));
        } else {
            Client.getAccount().Data.AddIgnored(new IgnoredContact() {
                {
                    AccountID = Target.Account.ID;
                    accountName = Target.Account.NickName;
                }
            });
            Client.Send(new IgnoredAddedMessage(new IgnoredOnlineInformations(Target.Account.ID, Target.Account.NickName, Target.ID, Target.NickName, Target.Breed, Target.Sexe == 1), Message.session));
        }
    }

    @HandlerAttribute(ID = FriendAddRequestMessage.ID)
    public static void HandleFriendAddRequestMessage(WorldClient Client, FriendAddRequestMessage Message) {
        Player Target = PlayerDAO.GetCharacter(Message.Name);
        if (Target == null || Target.Client == null) {
            Client.Send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (Target.Account == null || Target.Account.Data == null) {
            Client.Send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_NOT_FOUND));
        } else if (Client.getAccount().Data.HasFriend(Target.Account.ID)) {
            Client.Send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_IS_DOUBLE));
        } else if (Client.getAccount().Data.Friends.length >= ListAddFailureEnum.MAX_QUOTA) {
            Client.Send(new FriendAddFailureMessage(ListAddFailureEnum.LIST_ADD_FAILURE_OVER_QUOTA));
        } else {
            Client.getAccount().Data.AddFriend(new FriendContact() {
                {
                    AccountID = Target.Account.ID;
                    accountName = Target.Account.NickName;
                    lastConnection = System.currentTimeMillis();
                    achievementPoints = Target.achievementPoints;
                }
            });
            if (Target.Account.Data.HasFriend(Client.getAccount().ID)) {
                Client.Send(new FriendAddedMessage(new FriendOnlineInformations(Target.Account.ID, Target.Account.NickName, Target.GetPlayerState(), -1, Target.achievementPoints, Target.ID, Target.NickName, (byte) Target.Level, Target.AlignmentSide.value, Target.Breed, Target.Sexe == 1, Target.GetBasicGuildInformations(), Target.MoodSmiley, new PlayerStatus(Target.Status.value()))));
            } else {
                Client.Send(new FriendAddedMessage(new FriendOnlineInformations(Target.Account.ID, Target.Account.NickName, PlayerStateEnum.UNKNOWN_STATE, -1, Target.achievementPoints, Target.ID, Target.NickName, (byte) 0, (byte) -1, Target.Breed, Target.Sexe == 1, new BasicGuildInformations(0, ""), (byte) -1, new PlayerStatus(Target.Status.value()))));
            }
        }
    }

    @HandlerAttribute(ID = IgnoredDeleteRequestMessage.M_ID)
    public static void HandleIgnoredDeleteRequestMessage(WorldClient Client, IgnoredDeleteRequestMessage Message) {
        IgnoredContact Contact = Client.getAccount().Data.GetIgnored(Message.accountId);
        if (Contact == null) {
            Client.Send(new IgnoredDeleteResultMessage(false, "", Message.session));
        } else {
            Client.getAccount().Data.RemoveIgnored(Contact);
            Client.Send(new IgnoredDeleteResultMessage(true, Contact.accountName, Message.session));
        }
    }

    @HandlerAttribute(ID = FriendDeleteRequestMessage.M_ID)
    public static void HandleFriendDeleteRequestMessage(WorldClient Client, FriendDeleteRequestMessage Message) {
        FriendContact Contact = Client.getAccount().Data.GetFriend(Message.accountId);
        if (Contact == null) {
            Client.Send(new FriendDeleteResultMessage(false, ""));
        } else {
            Client.getAccount().Data.RemoveFriend(Contact);
            Client.Send(new FriendDeleteResultMessage(true, Contact.accountName));
        }
    }

    @HandlerAttribute(ID = FriendSetWarnOnConnectionMessage.M_ID)
    public static void HandleFriendSetWarnOnConnectionMessage(WorldClient Client, FriendSetWarnOnConnectionMessage Message) {
        Client.getAccount().Data.setFriendWarnOnLogin(Message.enable);
        Client.Send(new FriendWarnOnConnectionStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = FriendSetWarnOnLevelGainMessage.M_ID)
    public static void HandleFriendSetWarnOnLevelGainMessage(WorldClient Client, FriendSetWarnOnLevelGainMessage Message) {
        Client.getAccount().Data.setFriendWarnOnLevelGain(Message.enable);
        Client.Send(new FriendWarnOnLevelGainStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = GuildMemberSetWarnOnConnectionMessage.M_ID)
    public static void HandleGuildMemberSetWarnOnConnectionMessage(WorldClient Client, GuildMemberSetWarnOnConnectionMessage Message) {
        Client.getAccount().Data.setFriendWarnOnGuildLogin(Message.enable);
        Client.Send(new GuildMemberWarnOnConnectionStateMessage(Message.enable));
    }

    @HandlerAttribute(ID = FriendsGetListMessage.MESSAGE_ID)
    public static void HandleFriendsGetListMessage(WorldClient Client, Message message) {
        Client.SequenceMessage(new FriendsListMessage(Client.getAccount().Data.GetFriendsInformations()));
    }

    @HandlerAttribute(ID = IgnoredGetListMessage.MESSAGE_ID)
    public static void HandleIgnoredGetListMessage(WorldClient Client, Message message) {
        Client.SequenceMessage(new IgnoredListMessage(Client.getAccount().Data.GetIgnoredInformations()));
    }

    @HandlerAttribute(ID = SpouseGetInformationsMessage.MESSAGE_ID)
    public static void HandleSpouseGetInformationsMessage(WorldClient Client, Message message) {
        Client.SequenceMessage(new SpouseStatusMessage(false));
    }

}
