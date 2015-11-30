package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.regex.Pattern;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.GuildJoinRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.SpellDAOImpl;
import koh.game.dao.sqlite.GuildDAO;
import koh.game.dao.mysql.PlayerDAOImpl;
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
        if (Client.character.guild != null && Client.character.getGuildMember().manageGuildBoosts()) {
            if (Client.character.guild.Entity.Boost <= 5 || !ArrayUtils.contains(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)) {
                Client.send(new BasicNoOperationMessage());
                return;
            }

            byte SpellLevel = Client.character.guild.SpellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)];
            if (SpellLevel >= SpellDAOImpl.spells.get(Message.pellId).spellLevels.length) { //action Asyn ^^
                Client.send(new BasicNoOperationMessage());
                return;
            }
            Client.character.guild.SpellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, Message.pellId)]++;
            Client.character.guild.Entity.Boost -= 5;
            Client.character.guild.sendToField(Client.character.guild.toGuildInfosUpgradeMessage());

            Client.character.guild.Entity.Spells = Enumerable.Join(Client.character.guild.SpellLevel, ',');
            GuildDAO.Update(Client.character.guild.Entity);
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildCharacsUpgradeRequestMessage.ID)
    public static void HandleGuildCharacsUpgradeRequestMessage(WorldClient Client, GuildCharacsUpgradeRequestMessage Message) {
        if (Client.character.guild != null && Client.character.getGuildMember().manageGuildBoosts()) {
            if (Client.character.guild.Entity.Boost <= 0) {
                Client.send(new BasicNoOperationMessage());
                return;
            }

            switch (Message.charaTypeTarget) {
                case 0:
                    if (Client.character.guild.Entity.Pods >= 5000) {
                        return;
                    }
                    Client.character.guild.Entity.Pods += 20;
                    if (Client.character.guild.Entity.Pods >= 5000) {
                        Client.character.guild.Entity.Pods = 5000;
                    }
                    break;
                case 1:
                    if (Client.character.guild.Entity.Prospecting >= 500) {
                        return;
                    }
                    Client.character.guild.Entity.Prospecting++;
                    if (Client.character.guild.Entity.Prospecting >= 500) {
                        Client.character.guild.Entity.Prospecting = 500;
                    }
                    break;
                case 2:
                    if (Client.character.guild.Entity.Wisdom >= 400) {
                        return;
                    }
                    Client.character.guild.Entity.Wisdom++;
                    if (Client.character.guild.Entity.Wisdom >= 400) {
                        Client.character.guild.Entity.Wisdom = 400;
                    }
                    break;
                case 3:
                    if (Client.character.guild.Entity.MaxTaxCollectors >= 500 || Client.character.guild.Entity.Boost <= 10) {
                        Client.send(new BasicNoOperationMessage());
                        return;
                    }
                    Client.character.guild.Entity.Boost -= 9;
                    Client.character.guild.Entity.MaxTaxCollectors++;
                    if (Client.character.guild.Entity.MaxTaxCollectors >= 50) {
                        Client.character.guild.Entity.MaxTaxCollectors = 50;
                    }
                    break;
            }
            Client.character.guild.Entity.Boost--;
            Client.character.guild.sendToField(Client.character.guild.toGuildInfosUpgradeMessage());
            GuildDAO.Update(Client.character.guild.Entity);
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildInvitationAnswerMessage.M_ID)
    public static void HandleGuildInvitationAnswerMessage(WorldClient Client, GuildInvitationAnswerMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new BasicNoOperationMessage());
        }
        GuildJoinRequest guildInvitationRequest = (GuildJoinRequest) Client.getBaseRequest();
        if (Client == guildInvitationRequest.requester && !Message.accept) {
            guildInvitationRequest.declin();
        } else if (Client == guildInvitationRequest.requested) {
            if (Message.accept) {
                guildInvitationRequest.accept();
            } else {
                guildInvitationRequest.declin();
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6115)
    public static void HandleGuildInvitationByNameMessage(WorldClient Client, GuildInvitationByNameMessage Message) {
        if (Client.character.guild != null) {
            if (!Client.character.getGuildMember().inviteNewMembers()) {
                Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 207, new String[0]));
            } else {
                Player character = PlayerDAOImpl.getCharacter(Message.name);
                if (character == null || character.client == null) {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 208, new String[0]));
                } else {
                    if (character.guild != null) {
                        Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 206, new String[0]));
                    } else {
                        if (!character.client.canGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 209, new String[0]));
                        } else if (!Client.character.guild.canAddMember()) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 55, new String[]{String.valueOf(50)}));
                        } else {

                            GuildJoinRequest Request = new GuildJoinRequest(Client, character.client);
                            GameRequest RequestAction = new GameRequest(Client.character, Request);

                            Client.addGameAction(RequestAction);
                            character.client.addGameAction(RequestAction);

                            Client.setBaseRequest(Request);
                            character.client.setBaseRequest(Request);

                            Client.send(new GuildInvitationStateRecruterMessage(character.nickName, GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitedMessage(Client.character.ID, Client.character.nickName, Client.character.guild.GetBasicGuildInformations()));
                        }
                    }
                }
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5551)
    public static void HandleGuildInvitationMessage(WorldClient Client, GuildInvitationMessage Message) {
        if (Client.character.guild != null) {
            if (!Client.character.getGuildMember().inviteNewMembers()) {
                Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 207, new String[0]));
            } else {
                Player character = PlayerDAOImpl.getCharacter(Message.targetId);
                if (character == null || character.client == null) {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 208, new String[0]));
                } else {
                    if (character.guild != null) {
                        Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 206, new String[0]));
                    } else {
                        if (!character.client.canGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 209, new String[0]));
                        } else if (!Client.character.guild.canAddMember()) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 55, new String[]{String.valueOf(50)}));
                        } else {

                            GuildJoinRequest Request = new GuildJoinRequest(Client, character.client);
                            GameRequest RequestAction = new GameRequest(Client.character, Request);

                            Client.addGameAction(RequestAction);
                            character.client.addGameAction(RequestAction);

                            Client.setBaseRequest(Request);
                            character.client.setBaseRequest(Request);

                            Client.send(new GuildInvitationStateRecruterMessage(character.nickName, GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitedMessage(Client.character.ID, Client.character.nickName, Client.character.guild.GetBasicGuildInformations()));
                        }
                    }
                }
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5549)
    public static void HandleGuildChangeMemberParametersMessage(WorldClient Client, GuildChangeMemberParametersMessage Message) {
        if (Client.character.guild != null) {
            GuildMember guildMember = Client.character.guild.Members.get(Message.memberId);
            if (guildMember != null) {
                Client.character.guild.ChangeParameters(Client.character, guildMember, Message.rank, Message.experienceGivenPercent, Message.rights);
            }

        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildKickRequestMessage.M_ID)
    public static void HandleGuildKickRequestMessage(WorldClient Client, GuildKickRequestMessage Message) {
        if (Client.character.guild != null) {
            GuildMember guildMember = Client.character.guild.Members.get(Message.kickedId);
            if (guildMember != null) {
                Client.character.guild.KickMember(Client.character, guildMember);
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5550)
    public static void HandleGuildGetInformationsMessage(WorldClient Client, GuildGetInformationsMessage Message) {
        switch (Message.infoType) {
            case 1:
                Client.send(Client.character.guild.toGeneralInfos());
                break;
            case 2:
                Client.send(new GuildInformationsMembersMessage(Client.character.guild.allGuildMembers()));
                break;
            case 3:
                Client.send(Client.character.guild.toGuildInfosUpgradeMessage());
                break;
            case 4:
                Client.send(new GuildInformationsPaddocksMessage((byte) 5, Client.character.guild.toPaddockContentInformations()));
                break;
            case 5:
                Client.send(new GuildHousesInformationMessage(Client.character.guild.toHouseInformationsForGuild()));
                break;
            case 6:
                Client.send(Client.character.guild.toTaxCollectorListMessage());
                break;

        }
    }

    @HandlerAttribute(ID = 5546)
    public static void HandleGuildCreationValidMessage(WorldClient Client, GuildCreationValidMessage Message) {
        if (Client.character.guild != null) {
            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_ALREADY_IN_GROUP));
        } else if (!Client.character.inventoryCache.hasItemId(1575)) {
            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_REQUIREMENT_UNMET));
            PlayerController.sendServerMessage(Client, "La crétion d'une guilde nécessite une guildalogemme qui est commerciable en <b>Boutique</b>");
        } else if (GuildDAO.EmblemExist(Message.guildEmblem)) {
            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_EMBLEM_ALREADY_EXISTS));
        } else if (!Client.isGameAction(GameActionTypeEnum.CREATE_GUILD)) {
            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_CANCEL));
        } else if (GuildDAO.NameExist(Message.guildName) || !isValidName(Message.guildName) || Message.guildName.length() < 4 || Message.guildName.length() > 16) {
            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_NAME_INVALID));
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
                            this.AccountID = Client.getAccount().id;
                            this.Breed = Client.character.breed;
                            this.CharacterID = Client.character.ID;
                            this.LastConnection = System.currentTimeMillis() + "";
                            this.Level = Client.character.level;
                            this.Name = Client.character.nickName;
                            this.Rank = 1;
                            this.Experience = "0";
                            this.Rights = GuildRightsBitEnum.GUILD_RIGHT_BOSS;
                            this.Sex = Client.character.sexe == 1;
                            this.achievementPoints = Client.character.achievementPoints;
                            this.alignmentSide = Client.character.alignmentSide.value;
                            GuildDAO.Insert(this);
                        }
                    }, Client.character);
                    this.registerPlayer(Client.character);
                    this.SetBoss(this.Members.get(Client.character.ID));
                }
            };

            Client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_OK));

            Client.character.inventoryCache.updateObjectquantity(Client.character.inventoryCache.getItemInTemplate(1575), Client.character.inventoryCache.getItemInTemplate(1575).getQuantity() - 1);
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{1 + "", 1575 + ""}));

            Client.endGameAction(GameActionTypeEnum.CREATE_GUILD);
        }

    }

}
