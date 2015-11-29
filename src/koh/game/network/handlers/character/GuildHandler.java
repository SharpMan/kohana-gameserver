package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.regex.Pattern;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.GuildJoinRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.SpellDAOImpl;
import koh.game.dao.sqlite.GuildDAO;
import koh.game.dao.mysql.PlayerDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.guilds.Guild;
import koh.game.entities.guilds.GuildEntity;
import koh.game.entities.guilds.GuildMember;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.GuildInvitationStateEnum;
import koh.protocol.client.enums.GuildRightsBitEnum;
import koh.protocol.client.enums.SocialGroupCreationResultEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.guild.GuildChangeMemberParametersMessage;
import koh.protocol.messages.game.guild.GuildCharacsUpgradeRequestMessage;
import koh.protocol.messages.game.guild.GuildCreationResultMessage;
import koh.protocol.messages.game.guild.GuildCreationValidMessage;
import koh.protocol.messages.game.guild.GuildGetInformationsMessage;
import koh.protocol.messages.game.guild.GuildHousesInformationMessage;
import koh.protocol.messages.game.guild.GuildInformationsMembersMessage;
import koh.protocol.messages.game.guild.GuildInformationsPaddocksMessage;
import koh.protocol.messages.game.guild.GuildInvitationAnswerMessage;
import koh.protocol.messages.game.guild.GuildInvitationByNameMessage;
import koh.protocol.messages.game.guild.GuildInvitationMessage;
import koh.protocol.messages.game.guild.GuildInvitationStateRecrutedMessage;
import koh.protocol.messages.game.guild.GuildInvitationStateRecruterMessage;
import koh.protocol.messages.game.guild.GuildInvitedMessage;
import koh.protocol.messages.game.guild.GuildKickRequestMessage;
import koh.protocol.messages.game.guild.GuildSpellUpgradeRequestMessage;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class GuildHandler {

    private final static String ENCRYPTED_REGEX = "[a-zA-Z0-9éèêëïöôùç -]*";
    public final static Pattern ENCRYPTED_MATCHER = Pattern.compile(ENCRYPTED_REGEX);

    public static boolean isValidName(String packet) {
        return ENCRYPTED_MATCHER.matcher(packet).matches();
    }

    @HandlerAttribute(ID = GuildSpellUpgradeRequestMessage.M_ID)
    public static void HandleGuildSpellUpgradeRequestMessage(WorldClient Client, GuildSpellUpgradeRequestMessage Message) {
        if (Client.Character.Guild != null && Client.Character.GuildMember().manageGuildBoosts()) {
            if (Client.Character.Guild.Entity.Boost <= 5 || !ArrayUtils.contains(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)) {
                Client.Send(new BasicNoOperationMessage());
                return;
            }

            byte SpellLevel = Client.Character.Guild.SpellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)];
            if (SpellLevel >= SpellDAOImpl.spells.get(Message.pellId).spellLevels.length) { //Action Asyn ^^
                Client.Send(new BasicNoOperationMessage());
                return;
            }
            Client.Character.Guild.SpellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)]++;
            Client.Character.Guild.Entity.Boost -= 5;
            Client.Character.Guild.sendToField(Client.Character.Guild.toGuildInfosUpgradeMessage());

            Client.Character.Guild.Entity.Spells = Enumerable.Join(Client.Character.Guild.SpellLevel, ',');
            GuildDAO.Update(Client.Character.Guild.Entity);
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildCharacsUpgradeRequestMessage.ID)
    public static void HandleGuildCharacsUpgradeRequestMessage(WorldClient Client, GuildCharacsUpgradeRequestMessage Message) {
        if (Client.Character.Guild != null && Client.Character.GuildMember().manageGuildBoosts()) {
            if (Client.Character.Guild.Entity.Boost <= 0) {
                Client.Send(new BasicNoOperationMessage());
                return;
            }

            switch (Message.charaTypeTarget) {
                case 0:
                    if (Client.Character.Guild.Entity.Pods >= 5000) {
                        return;
                    }
                    Client.Character.Guild.Entity.Pods += 20;
                    if (Client.Character.Guild.Entity.Pods >= 5000) {
                        Client.Character.Guild.Entity.Pods = 5000;
                    }
                    break;
                case 1:
                    if (Client.Character.Guild.Entity.Prospecting >= 500) {
                        return;
                    }
                    Client.Character.Guild.Entity.Prospecting++;
                    if (Client.Character.Guild.Entity.Prospecting >= 500) {
                        Client.Character.Guild.Entity.Prospecting = 500;
                    }
                    break;
                case 2:
                    if (Client.Character.Guild.Entity.Wisdom >= 400) {
                        return;
                    }
                    Client.Character.Guild.Entity.Wisdom++;
                    if (Client.Character.Guild.Entity.Wisdom >= 400) {
                        Client.Character.Guild.Entity.Wisdom = 400;
                    }
                    break;
                case 3:
                    if (Client.Character.Guild.Entity.MaxTaxCollectors >= 500 || Client.Character.Guild.Entity.Boost <= 10) {
                        Client.Send(new BasicNoOperationMessage());
                        return;
                    }
                    Client.Character.Guild.Entity.Boost -= 9;
                    Client.Character.Guild.Entity.MaxTaxCollectors++;
                    if (Client.Character.Guild.Entity.MaxTaxCollectors >= 50) {
                        Client.Character.Guild.Entity.MaxTaxCollectors = 50;
                    }
                    break;
            }
            Client.Character.Guild.Entity.Boost--;
            Client.Character.Guild.sendToField(Client.Character.Guild.toGuildInfosUpgradeMessage());
            GuildDAO.Update(Client.Character.Guild.Entity);
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildInvitationAnswerMessage.M_ID)
    public static void HandleGuildInvitationAnswerMessage(WorldClient Client, GuildInvitationAnswerMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.Send(new BasicNoOperationMessage());
        }
        GuildJoinRequest guildInvitationRequest = (GuildJoinRequest) Client.GetBaseRequest();
        if (Client == guildInvitationRequest.Requester && !Message.accept) {
            guildInvitationRequest.Declin();
        } else if (Client == guildInvitationRequest.Requested) {
            if (Message.accept) {
                guildInvitationRequest.Accept();
            } else {
                guildInvitationRequest.Declin();
            }
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6115)
    public static void HandleGuildInvitationByNameMessage(WorldClient Client, GuildInvitationByNameMessage Message) {
        if (Client.Character.Guild != null) {
            if (!Client.Character.GuildMember().inviteNewMembers()) {
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 207, new String[0]));
            } else {
                Player character = PlayerDAO.GetCharacter(Message.name);
                if (character == null || character.Client == null) {
                    Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 208, new String[0]));
                } else {
                    if (character.Guild != null) {
                        Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 206, new String[0]));
                    } else {
                        if (!character.Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 209, new String[0]));
                        } else if (!Client.Character.Guild.canAddMember()) {
                            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 55, new String[]{String.valueOf(50)}));
                        } else {

                            GuildJoinRequest Request = new GuildJoinRequest(Client, character.Client);
                            GameRequest RequestAction = new GameRequest(Client.Character, Request);

                            Client.AddGameAction(RequestAction);
                            character.Client.AddGameAction(RequestAction);

                            Client.SetBaseRequest(Request);
                            character.Client.SetBaseRequest(Request);

                            Client.Send(new GuildInvitationStateRecruterMessage(character.NickName, GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.Send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.Send(new GuildInvitedMessage(Client.Character.ID, Client.Character.NickName, Client.Character.Guild.GetBasicGuildInformations()));
                        }
                    }
                }
            }
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5551)
    public static void HandleGuildInvitationMessage(WorldClient Client, GuildInvitationMessage Message) {
        if (Client.Character.Guild != null) {
            if (!Client.Character.GuildMember().inviteNewMembers()) {
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 207, new String[0]));
            } else {
                Player character = PlayerDAO.GetCharacter(Message.targetId);
                if (character == null || character.Client == null) {
                    Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 208, new String[0]));
                } else {
                    if (character.Guild != null) {
                        Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 206, new String[0]));
                    } else {
                        if (!character.Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 209, new String[0]));
                        } else if (!Client.Character.Guild.canAddMember()) {
                            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 55, new String[]{String.valueOf(50)}));
                        } else {

                            GuildJoinRequest Request = new GuildJoinRequest(Client, character.Client);
                            GameRequest RequestAction = new GameRequest(Client.Character, Request);

                            Client.AddGameAction(RequestAction);
                            character.Client.AddGameAction(RequestAction);

                            Client.SetBaseRequest(Request);
                            character.Client.SetBaseRequest(Request);

                            Client.Send(new GuildInvitationStateRecruterMessage(character.NickName, GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.Send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.Send(new GuildInvitedMessage(Client.Character.ID, Client.Character.NickName, Client.Character.Guild.GetBasicGuildInformations()));
                        }
                    }
                }
            }
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5549)
    public static void HandleGuildChangeMemberParametersMessage(WorldClient Client, GuildChangeMemberParametersMessage Message) {
        if (Client.Character.Guild != null) {
            GuildMember guildMember = Client.Character.Guild.Members.get(Message.memberId);
            if (guildMember != null) {
                Client.Character.Guild.ChangeParameters(Client.Character, guildMember, Message.rank, Message.experienceGivenPercent, Message.rights);
            }

        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildKickRequestMessage.M_ID)
    public static void HandleGuildKickRequestMessage(WorldClient Client, GuildKickRequestMessage Message) {
        if (Client.Character.Guild != null) {
            GuildMember guildMember = Client.Character.Guild.Members.get(Message.kickedId);
            if (guildMember != null) {
                Client.Character.Guild.KickMember(Client.Character, guildMember);
            }
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5550)
    public static void HandleGuildGetInformationsMessage(WorldClient Client, GuildGetInformationsMessage Message) {
        switch (Message.infoType) {
            case 1:
                Client.Send(Client.Character.Guild.toGeneralInfos());
                break;
            case 2:
                Client.Send(new GuildInformationsMembersMessage(Client.Character.Guild.allGuildMembers()));
                break;
            case 3:
                Client.Send(Client.Character.Guild.toGuildInfosUpgradeMessage());
                break;
            case 4:
                Client.Send(new GuildInformationsPaddocksMessage((byte) 5, Client.Character.Guild.toPaddockContentInformations()));
                break;
            case 5:
                Client.Send(new GuildHousesInformationMessage(Client.Character.Guild.toHouseInformationsForGuild()));
                break;
            case 6:
                Client.Send(Client.Character.Guild.toTaxCollectorListMessage());
                break;

        }
    }

    @HandlerAttribute(ID = 5546)
    public static void HandleGuildCreationValidMessage(WorldClient Client, GuildCreationValidMessage Message) {
        if (Client.Character.Guild != null) {
            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_ALREADY_IN_GROUP));
        } else if (!Client.Character.InventoryCache.HasItemId(1575)) {
            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_REQUIREMENT_UNMET));
            PlayerController.SendServerMessage(Client, "La crétion d'une guilde nécessite une guildalogemme qui est commerciable en <b>Boutique</b>");
        } else if (GuildDAO.EmblemExist(Message.guildEmblem)) {
            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_EMBLEM_ALREADY_EXISTS));
        } else if (!Client.IsGameAction(GameActionTypeEnum.CREATE_GUILD)) {
            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_CANCEL));
        } else if (GuildDAO.NameExist(Message.guildName) || !isValidName(Message.guildName) || Message.guildName.length() < 4 || Message.guildName.length() > 16) {
            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_NAME_INVALID));
        } else {
            new Guild(new GuildEntity() {
                {
                    this.GuildID = GuildDAO.NextGuildID++;
                    this.CreationDate = (int) Instant.now().getEpochSecond();
                    this.EmblemBackgroundColor = Message.guildEmblem.backgroundColor;
                    this.EmblemBackgroundShape = Message.guildEmblem.backgroundShape;
                    this.EmblemForegroundColor = Message.guildEmblem.symbolColor;
                    this.EmblemForegroundShape = Message.guildEmblem.symbolShape;
                    this.Level = 1;
                    this.Experience = "0";
                    this.MaxTaxCollectors = 1;
                    this.Name = Message.guildName;
                    this.Pods = 1000;
                    this.Prospecting = 100;

                    this.Spells = "0,0,0,0,0,0,0,0,0,0,0,0";
                    GuildDAO.Insert(this);
                }
            }) {
                {
                    this.addMember(new GuildMember(this.Entity.GuildID) {
                        {
                            this.AccountID = Client.getAccount().ID;
                            this.Breed = Client.Character.Breed;
                            this.CharacterID = Client.Character.ID;
                            this.LastConnection = System.currentTimeMillis() + "";
                            this.Level = Client.Character.Level;
                            this.Name = Client.Character.NickName;
                            this.Rank = 1;
                            this.Experience = "0";
                            this.Rights = GuildRightsBitEnum.GUILD_RIGHT_BOSS;
                            this.Sex = Client.Character.Sexe == 1;
                            this.achievementPoints = Client.Character.achievementPoints;
                            this.alignmentSide = Client.Character.AlignmentSide.value;
                            GuildDAO.Insert(this);
                        }
                    }, Client.Character);
                    this.registerPlayer(Client.Character);
                    this.SetBoss(this.Members.get(Client.Character.ID));
                }
            };

            Client.Send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_OK));

            Client.Character.InventoryCache.UpdateObjectquantity(Client.Character.InventoryCache.GetItemInTemplate(1575), Client.Character.InventoryCache.GetItemInTemplate(1575).GetQuantity() - 1);
            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{1 + "", 1575 + ""}));

            Client.EndGameAction(GameActionTypeEnum.CREATE_GUILD);
        }

    }

}
