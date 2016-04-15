package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.regex.Pattern;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.GuildJoinRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
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
    private final static Pattern ENCRYPTED_MATCHER = Pattern.compile(ENCRYPTED_REGEX);

    private static boolean isValidName(String packet) {
        return ENCRYPTED_MATCHER.matcher(packet).matches();
    }

    @HandlerAttribute(ID = GuildSpellUpgradeRequestMessage.M_ID)
    public static void HandleGuildSpellUpgradeRequestMessage(WorldClient Client, GuildSpellUpgradeRequestMessage message) {
        if (Client.getCharacter().getGuild() != null && Client.getCharacter().getGuildMember().manageGuildBoosts()) {
            if (Client.getCharacter().getGuild().entity.boost <= 5 || !ArrayUtils.contains(Guild.TAX_COLLECTOR_SPELLS, message.spellId)) {
                Client.send(new BasicNoOperationMessage());
                return;
            }

            byte SpellLevel = Client.getCharacter().getGuild().spellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, message.spellId)];
            if (SpellLevel >= DAO.getSpells().findSpell(message.spellId).getSpellLevels().length) { //action Asyn ^^
                Client.send(new BasicNoOperationMessage());
                return;
            }
            Client.getCharacter().getGuild().spellLevel[ArrayUtils.indexOf(Guild.TAX_COLLECTOR_SPELLS, message.spellId)]++;
            Client.getCharacter().getGuild().entity.boost -= 5;
            Client.getCharacter().getGuild().sendToField(Client.getCharacter().getGuild().toGuildInfosUpgradeMessage());
            Client.getCharacter().getGuild().entity.spells = Enumerable.join(Client.getCharacter().getGuild().spellLevel, ',');
            DAO.getGuilds().update(Client.getCharacter().getGuild().entity);
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildCharacsUpgradeRequestMessage.ID)
    public static void HandleGuildCharacsUpgradeRequestMessage(WorldClient Client, GuildCharacsUpgradeRequestMessage Message) {
        if (Client.getCharacter().getGuild() != null && Client.getCharacter().getGuildMember().manageGuildBoosts()) {
            if (Client.getCharacter().getGuild().entity.boost <= 0) {
                Client.send(new BasicNoOperationMessage());
                return;
            }

            switch (Message.charaTypeTarget) {
                case 0:
                    if (Client.getCharacter().getGuild().entity.pods >= 5000) {
                        return;
                    }
                    Client.getCharacter().getGuild().entity.pods += 20;
                    if (Client.getCharacter().getGuild().entity.pods >= 5000) {
                        Client.getCharacter().getGuild().entity.pods = 5000;
                    }
                    break;
                case 1:
                    if (Client.getCharacter().getGuild().entity.prospecting >= 500) {
                        return;
                    }
                    Client.getCharacter().getGuild().entity.prospecting++;
                    if (Client.getCharacter().getGuild().entity.prospecting >= 500) {
                        Client.getCharacter().getGuild().entity.prospecting = 500;
                    }
                    break;
                case 2:
                    if (Client.getCharacter().getGuild().entity.wisdom >= 400) {
                        return;
                    }
                    Client.getCharacter().getGuild().entity.wisdom++;
                    if (Client.getCharacter().getGuild().entity.wisdom >= 400) {
                        Client.getCharacter().getGuild().entity.wisdom = 400;
                    }
                    break;
                case 3:
                    if (Client.getCharacter().getGuild().entity.maxTaxCollectors >= 500 || Client.getCharacter().getGuild().entity.boost <= 10) {
                        Client.send(new BasicNoOperationMessage());
                        return;
                    }
                    Client.getCharacter().getGuild().entity.boost -= 9;
                    Client.getCharacter().getGuild().entity.maxTaxCollectors++;
                    if (Client.getCharacter().getGuild().entity.maxTaxCollectors >= 50) {
                        Client.getCharacter().getGuild().entity.maxTaxCollectors = 50;
                    }
                    break;
            }
            Client.getCharacter().getGuild().entity.boost--;
            Client.getCharacter().getGuild().sendToField(Client.getCharacter().getGuild().toGuildInfosUpgradeMessage());
            DAO.getGuilds().update(Client.getCharacter().getGuild().entity);
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

    private static void inviteTargetOnGuild(WorldClient Client, Player character) {
        if (Client.getCharacter().getGuild() != null) { //TODO : hasGUILD
            if (!Client.getCharacter().getGuildMember().inviteNewMembers()) {
                Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 207, new String[0]));
            } else {
                if (character == null || character.getClient() == null) {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 208, new String[0]));
                } else {
                    if (character.getGuild()!= null) {
                        Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 206, new String[0]));
                    } else {
                        if (!character.getClient().canGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 209, new String[0]));
                        } else if (!Client.getCharacter().getGuild().canAddMember()) {
                            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 55, new String[]{String.valueOf(50)}));
                        } else {

                            GuildJoinRequest request = new GuildJoinRequest(Client, character.getClient());
                            GameRequest requestAction = new GameRequest(Client.getCharacter(), request);

                            Client.addGameAction(requestAction);
                            character.getClient().addGameAction(requestAction);

                            Client.setBaseRequest(request);
                            character.getClient().setBaseRequest(request);

                            Client.send(new GuildInvitationStateRecruterMessage(character.getNickName(), GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_SENT));
                            character.send(new GuildInvitedMessage(Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getCharacter().getGuild().getBasicGuildInformations()));
                        }
                    }
                }
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6115)
    public static void HandleGuildInvitationByNameMessage(WorldClient Client, GuildInvitationByNameMessage Message) {
        inviteTargetOnGuild(Client, DAO.getPlayers().getCharacter(Message.name));
    }

    @HandlerAttribute(ID = 5551)
    public static void HandleGuildInvitationMessage(WorldClient Client, GuildInvitationMessage Message) {
        inviteTargetOnGuild(Client, DAO.getPlayers().getCharacter(Message.targetId));
    }

    @HandlerAttribute(ID = 5549)
    public static void HandleGuildChangeMemberParametersMessage(WorldClient Client, GuildChangeMemberParametersMessage Message) {
        if (Client.getCharacter().getGuild() != null) {
            GuildMember guildMember = Client.getCharacter().getGuild().getMember(Message.memberId);
            if (guildMember != null) {
                Client.getCharacter().getGuild().changeParameters(Client.getCharacter(), guildMember, Message.rank, Message.experienceGivenPercent, Message.rights);
            }

        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = GuildKickRequestMessage.M_ID)
    public static void HandleGuildKickRequestMessage(WorldClient Client, GuildKickRequestMessage Message) {
        if (Client.getCharacter().getGuild() != null) {
            GuildMember guildMember = Client.getCharacter().getGuild().getMember(Message.kickedId);
            if (guildMember != null) {
                Client.getCharacter().getGuild().kickMember(Client.getCharacter(), guildMember);
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5550)
    public static void HandleGuildGetInformationsMessage(WorldClient Client, GuildGetInformationsMessage Message) {
        switch (Message.infoType) {
            case 1:
                Client.send(Client.getCharacter().getGuild().toGeneralInfos());
                break;
            case 2:
                Client.send(new GuildInformationsMembersMessage(Client.getCharacter().getGuild().allGuildMembers()));
                break;
            case 3:
                Client.send(Client.getCharacter().getGuild().toGuildInfosUpgradeMessage());
                break;
            case 4:
                Client.send(new GuildInformationsPaddocksMessage((byte) 5, Client.getCharacter().getGuild().toPaddockContentInformations()));
                break;
            case 5:
                Client.send(new GuildHousesInformationMessage(Client.getCharacter().getGuild().toHouseInformationsForGuild()));
                break;
            case 6:
                Client.send(Client.getCharacter().getGuild().toTaxCollectorListMessage());
                break;

        }
    }

    @HandlerAttribute(ID = 5546)
    public static void HandleGuildCreationValidMessage(WorldClient client, GuildCreationValidMessage message) {
        if (client.getCharacter().getGuild() != null) {
            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_ALREADY_IN_GROUP));
        } else if (!client.getCharacter().getInventoryCache().hasItemId(1575)) {
            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_REQUIREMENT_UNMET));
            PlayerController.sendServerMessage(client, "La création d'une guilde nécessite une guildalogemme qui est commerciable en <b>Boutique</b>");
        } else if (DAO.getGuilds().alreadyTakenEmblem(message.guildEmblem)) {
            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_EMBLEM_ALREADY_EXISTS));
        } else if (!client.isGameAction(GameActionTypeEnum.CREATE_GUILD)) {
            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_CANCEL));
        } else if (DAO.getGuilds().alreadyTakenName(message.guildName) || !isValidName(message.guildName) || message.guildName.length() < 4 || message.guildName.length() > 16) {
            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_ERROR_NAME_INVALID));
        } else {
            DAO.getGuilds().insert(new Guild(new GuildEntity() {
                {
                    this.guildID = DAO.getGuilds().nextId();
                    this.creationDate = (int) Instant.now().getEpochSecond();
                    this.emblemBackgroundColor = message.guildEmblem.backgroundColor;
                    this.emblemBackgroundShape = message.guildEmblem.backgroundShape;
                    this.emblemForegroundColor = message.guildEmblem.symbolColor;
                    this.emblemForegroundShape = message.guildEmblem.symbolShape;
                    this.level = 1;
                    this.experience = "0";
                    this.maxTaxCollectors = 1;
                    this.name = message.guildName;
                    this.pods = 1000;
                    this.prospecting = 100;

                    this.spells = "0,0,0,0,0,0,0,0,0,0,0,0";
                }
            }) {
                {
                    this.addMember(new GuildMember(this.entity.guildID) {
                        {
                            this.accountID = client.getAccount().id;
                            this.breed = client.getCharacter().getBreed();
                            this.characterID = client.getCharacter().getID();
                            this.lastConnection = System.currentTimeMillis() + "";
                            this.level = client.getCharacter().getLevel();
                            this.name = client.getCharacter().getNickName();
                            this.rank = 1;
                            this.experience = "0";
                            this.rights = GuildRightsBitEnum.GUILD_RIGHT_BOSS;
                            this.sex = client.getCharacter().hasSexe();
                            this.achievementPoints = client.getCharacter().getAchievementPoints();
                            this.alignmentSide = client.getCharacter().getAlignmentSide().value;
                            DAO.getGuildMembers().insert(this);
                        }
                    }, client.getCharacter());
                    this.registerPlayer(client.getCharacter());
                    this.setBoss(getMember(client.getCharacter().getID()));
                }
            });

            client.send(new GuildCreationResultMessage(SocialGroupCreationResultEnum.SOCIAL_GROUP_CREATE_OK));

            client.getCharacter().getInventoryCache().updateObjectquantity(client.getCharacter().getInventoryCache().getItemInTemplate(1575), client.getCharacter().getInventoryCache().getItemInTemplate(1575).getQuantity() - 1);
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{1 + "", 1575 + ""}));

            client.endGameAction(GameActionTypeEnum.CREATE_GUILD);
        }

    }

}
