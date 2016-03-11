package koh.game.network.handlers.game.approach;

import koh.d2o.entities.Breed;
import koh.d2o.entities.Head;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.ShortcutBook;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.inter.messages.PlayerCreatedMessage;
import koh.protocol.client.Message;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.approach.CharactersListRequestMessage;
import koh.protocol.messages.game.approach.ServerSettingsMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.choice.*;
import koh.protocol.messages.game.character.deletion.CharacterDeletionErrorMessage;
import koh.protocol.messages.game.character.deletion.CharacterDeletionRequestMessage;
import koh.protocol.messages.game.character.stats.CharacterStatsListMessage;
import koh.protocol.messages.game.chat.EnabledChannelsMessage;
import koh.protocol.messages.game.context.mount.MountRidingMessage;
import koh.protocol.messages.game.context.mount.MountSetMessage;
import koh.protocol.messages.game.context.mount.MountXpRatioMessage;
import koh.protocol.messages.game.context.notification.NotificationListMessage;
import koh.protocol.messages.game.context.roleplay.emote.EmoteListMessage;
import koh.protocol.messages.game.context.roleplay.fight.arena.GameRolePlayArenaUpdatePlayerInfosMessage;
import koh.protocol.messages.game.context.roleplay.job.JobCrafterDirectorySettingsMessage;
import koh.protocol.messages.game.context.roleplay.job.JobDescriptionMessage;
import koh.protocol.messages.game.context.roleplay.job.JobExperienceMultiUpdateMessage;
import koh.protocol.messages.game.friend.FriendWarnOnConnectionStateMessage;
import koh.protocol.messages.game.friend.FriendWarnOnLevelGainStateMessage;
import koh.protocol.messages.game.friend.GuildMemberWarnOnConnectionStateMessage;
import koh.protocol.messages.game.initialization.CharacterCapabilitiesMessage;
import koh.protocol.messages.game.inventory.InventoryContentMessage;
import koh.protocol.messages.game.inventory.InventoryWeightMessage;
import koh.protocol.messages.game.inventory.SpellListMessage;
import koh.protocol.messages.game.pvp.SetEnablePVPRequestMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarContentMessage;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.SetCharacterRestrictionsMessage;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Neo-Craft
 */
public class CharacterHandler {

    private static final Logger logger = LogManager.getLogger(CharacterHandler.class);

    @HandlerAttribute(ID = 6072)
    public static void handleCharacterSelectedForceReadyMessage(WorldClient Client, CharacterSelectedForceReadyMessage Message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        if (inFight != null) {
            characterSelectionMessage(Client, inFight.getID());
        }
        //Else le mec est sortie du combat osef
    }

    @HandlerAttribute(ID = SetEnablePVPRequestMessage.M_ID)
    public static void handleSetEnablePVPRequestMessage(WorldClient Client, SetEnablePVPRequestMessage Message) {
        Client.getCharacter().setEnabldPvp(Message.enable ? AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE : AggressableStatusEnum.NON_AGGRESSABLE);
    }

    @HandlerAttribute(ID = CharactersListRequestMessage.MESSAGE_ID)
    public static void handleAuthenticationTicketMessage(WorldClient Client, Message message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        Client.send(new CharactersListMessage(false, Client.getAccount().toBaseInformations()));
        if (inFight != null) {
            Client.send(new CharacterSelectedForceMessage(inFight.getID()));
        }
    }

    @HandlerAttribute(ID = CharacterNameSuggestionRequestMessage.MESSAGE_ID)
    public static void handleCharacterNameSuggestionRequestMessage(WorldClient Client, Message message) {
        Client.send(new CharacterNameSuggestionSuccessMessage(PlayerController.GenerateName()));
    }

    @HandlerAttribute(ID = 165) //Suppresion
    public static void handleCharacterDeletionRequestMessage(WorldClient Client, CharacterDeletionRequestMessage Message) {
        Client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
    }

    @HandlerAttribute(ID = 152)
    public static void handleCharacterSelectionMessage(WorldClient Client, CharacterSelectionMessage message) {
        characterSelectionMessage(Client, message.id);
    }

    public static void characterSelectionMessage(WorldClient client, int id) {
        try {

            Player character = client.getAccount().getPlayer(id);
            if (character == null) {
                client.send(new CharacterSelectedErrorMessage());
            } else {
                client.setCharacter(character);
                character.setClient(client);
                character.initialize();
                client.getAccount().currentCharacter = character;
                client.sequenceMessage();
                //client.send(new ComicReadingBeginMessage(79));
                client.send(new EnabledChannelsMessage(character.getEnabledChannels(), character.getDisabledChannels()));
                client.send(new NotificationListMessage(new int[]{2147483647}));
                client.send(new CharacterSelectedSuccessMessage(character.toBaseInformations(), false));
                client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                client.send(new InventoryContentMessage(character.getInventoryCache().toObjectsItem(), character.getKamas()));
                client.send(new ShortcutBarContentMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, character.getShortcuts().toShortcuts(character)));
                client.send(new ShortcutBarContentMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, character.getMySpells().toShortcuts()));
                client.send(new EmoteListMessage(character.getEmotes()));
                client.send(new JobDescriptionMessage(character.getMyJobs().getDescriptions()));
                client.send(new JobExperienceMultiUpdateMessage(character.getMyJobs().getExperiences()));
                client.send(new JobCrafterDirectorySettingsMessage(character.getMyJobs().getSettings()));

                client.send(new SpellListMessage(true, character.getMySpells().toSpellItems()));
                client.send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(false, false, false, false, false, false, false, false, true, false, false, false, false, true, true, true, false, false, false, false, false), character.getID()));
                client.send(new InventoryWeightMessage(character.getInventoryCache().getWeight(), character.getInventoryCache().getTotalWeight()));
                //GuilMember =! null
                client.send(new FriendWarnOnConnectionStateMessage(client.getAccount().accountData.friend_warn_on_login));
                client.send(new FriendWarnOnLevelGainStateMessage(client.getAccount().accountData.friend_warn_on_level_gain));
                client.send(new GuildMemberWarnOnConnectionStateMessage(client.getAccount().accountData.guild_warn_on_login));
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 89, new String[0]));

                client.send(new ServerSettingsMessage("fr", (byte) 1, (byte) 0));

                try {
                    client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 152,
                            String.valueOf(client.getAccount().last_login.getYear() - 100 + 2000),
                            String.valueOf(client.getAccount().last_login.getDay()),
                            String.valueOf(client.getAccount().last_login.getDate()),
                            String.valueOf(client.getAccount().last_login.getHours()),
                            String.valueOf(client.getAccount().last_login.getMinutes()), client.getAccount().lastIP));
                    client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 153, client.getIP()));


                } catch (Exception e) {
                }

                //client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                client.send(new CharacterCapabilitiesMessage(4095));
                if (client.getCharacter().getMountInfo().mount != null) {
                    client.send(new MountSetMessage(client.getCharacter().getMountInfo().mount));
                    client.send(new MountXpRatioMessage(client.getCharacter().getMountInfo().ratio));
                    client.send(new MountRidingMessage(true));
                }
                client.getAccount().last_login = new Timestamp(System.currentTimeMillis());
                client.getAccount().lastIP = client.getIP();

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    public static void sendCharacterStatsListMessage(WorldClient client) {
        client.send(new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) client.getCharacter().getExperience(), (double) DAO.getExps().getPlayerMinExp(client.getCharacter().getLevel()), (double) DAO.getExps().getPlayerMaxExp(client.getCharacter().getLevel()), client.getCharacter().getKamas(), client.getCharacter().getStatPoints(), 0, client.getCharacter().getSpellPoints(), client.getCharacter().getActorAlignmentExtendInformations(),
                client.getCharacter().getLife(), client.getCharacter().getMaxLife(), client.getCharacter().getEnergy(), PlayerEnum.MAX_ENERGY,
                (short) client.getCharacter().getStats().getTotal(StatsEnum.ACTION_POINTS), (short) client.getCharacter().getStats().getTotal(StatsEnum.MOVEMENT_POINTS),
                new CharacterBaseCharacteristic(client.getCharacter().getInitiative(true), 0, client.getCharacter().getStats().getItem(StatsEnum.INITIATIVE), 0, 0), client.getCharacter().getStats().getEffect(StatsEnum.PROSPECTING), client.getCharacter().getStats().getEffect(StatsEnum.ACTION_POINTS),
                client.getCharacter().getStats().getEffect(StatsEnum.MOVEMENT_POINTS), client.getCharacter().getStats().getEffect(StatsEnum.STRENGTH), client.getCharacter().getStats().getEffect(StatsEnum.VITALITY),
                client.getCharacter().getStats().getEffect(StatsEnum.WISDOM), client.getCharacter().getStats().getEffect(StatsEnum.CHANCE), client.getCharacter().getStats().getEffect(StatsEnum.AGILITY),
                client.getCharacter().getStats().getEffect(StatsEnum.INTELLIGENCE), client.getCharacter().getStats().getEffect(StatsEnum.ADD_RANGE), client.getCharacter().getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT),
                client.getCharacter().getStats().getEffect(StatsEnum.DAMAGE_REFLECTION), client.getCharacter().getStats().getEffect(StatsEnum.ADD_CRITICAL_HIT), (short) client.getCharacter().getInventoryCache().weaponCriticalHit(),
                client.getCharacter().getStats().getEffect(StatsEnum.CRITICAL_MISS), client.getCharacter().getStats().getEffect(StatsEnum.ADD_HEAL_BONUS), client.getCharacter().getStats().getEffect(StatsEnum.ALL_DAMAGES_BONUS),
                client.getCharacter().getStats().getEffect(StatsEnum.WEAPON_DAMAGES_BONUS_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.ADD_DAMAGE_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.TRAP_BONUS),
                client.getCharacter().getStats().getEffect(StatsEnum.TRAP_DAMAGE_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.GLYPH_BONUS_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.PERMANENT_DAMAGE_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.ADD_TACKLE_BLOCK),
                client.getCharacter().getStats().getEffect(StatsEnum.ADD_TACKLE_EVADE), client.getCharacter().getStats().getEffect(StatsEnum.ADD_RETRAIT_PA), client.getCharacter().getStats().getEffect(StatsEnum.ADD_RETRAIT_PM), client.getCharacter().getStats().getEffect(StatsEnum.ADD_PUSH_DAMAGES_BONUS),
                client.getCharacter().getStats().getEffect(StatsEnum.ADD_CRITICAL_DAMAGES), client.getCharacter().getStats().getEffect(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS), client.getCharacter().getStats().getEffect(StatsEnum.ADD_EARTH_DAMAGES_BONUS),
                client.getCharacter().getStats().getEffect(StatsEnum.ADD_WATER_DAMAGES_BONUS), client.getCharacter().getStats().getEffect(StatsEnum.ADD_AIR_DAMAGES_BONUS), client.getCharacter().getStats().getEffect(StatsEnum.ADD_FIRE_DAMAGES_BONUS),
                client.getCharacter().getStats().getEffect(StatsEnum.DODGE_PA_LOST_PROBABILITY), client.getCharacter().getStats().getEffect(StatsEnum.DODGE_PM_LOST_PROBABILITY), client.getCharacter().getStats().getEffect(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT),
                client.getCharacter().getStats().getEffect(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.AIR_ELEMENT_RESIST_PERCENT),
                client.getCharacter().getStats().getEffect(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.EARTH_ELEMENT_REDUCTION),
                client.getCharacter().getStats().getEffect(StatsEnum.WATER_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.AIR_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.FIRE_ELEMENT_REDUCTION),
                client.getCharacter().getStats().getEffect(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT),
                client.getCharacter().getStats().getEffect(StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT),
                client.getCharacter().getStats().getEffect(StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT), client.getCharacter().getStats().getEffect(StatsEnum.PVP_NEUTRAL_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.PVP_EARTH_ELEMENT_REDUCTION),
                client.getCharacter().getStats().getEffect(StatsEnum.PVP_WATER_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.PVP_AIR_ELEMENT_REDUCTION), client.getCharacter().getStats().getEffect(StatsEnum.PVP_FIRE_ELEMENT_REDUCTION),
                new CharacterSpellModification[0], (short) 0)));
    }

    @HandlerAttribute(ID = CharacterCreationRequestMessage.MESSAGE_ID)
    public static void handleCharacterCreationRequestMessage(WorldClient client, CharacterCreationRequestMessage message) {
        try {
            if (client.getAccount().characters.size() >= PlayerDAOImpl.MAX_CHARACTER_SLOT) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_TOO_MANY_CHARACTERS.value()));
            } else if (!PlayerController.isValidName(((CharacterCreationRequestMessage) message).name)) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));

            } else if (DAO.getPlayers().containsName(((CharacterCreationRequestMessage) message).name)) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
            } else {
                final Breed breedTemplate = DAO.getD2oTemplates().getBreed(((CharacterCreationRequestMessage) message).breed);
                if (breedTemplate == null) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NOT_ALLOWED.value()));
                    return;
                }
                final Head head = DAO.getD2oTemplates().getHead(((CharacterCreationRequestMessage) message).cosmeticId);
                if (head == null || head.breedtype != breedTemplate.id || head.gendertype == 1 != ((CharacterCreationRequestMessage) message).sex) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                Player character = Player.builder()
                        .nickName(message.name)
                        .regenRate((byte)10)
                        .breed((byte) message.breed)
                        .owner(client.getAccount().id)
                        .sexe(message.sex ? 1 : 0)
                        .skins(new ArrayList<>(Arrays.asList(message.sex ? breedTemplate.getFemaleLook() : breedTemplate.getMaleLook(), Short.parseShort(head.skinstype))))
                        .scales(new ArrayList<>(Arrays.asList(message.sex ? breedTemplate.getFemaleSize() : breedTemplate.getMaleSize())))
                        .indexedColors(new ArrayList<Integer>(5) {
                            {
                                for (byte i = 0; i < 5; i++) {
                                    if (message.colors.get(i) == -1) {
                                        add(breedTemplate.getColors(message.sex ? 1 : 0).get(i) | (i + 1) * 0x1000000);
                                    } else {
                                        add(message.colors.get(i) | (i + 1) * 0x1000000);
                                    }
                                }
                            }
                        })
                        .account(client.getAccount())
                        .level((byte) DAO.getSettings().getIntElement("Register.StartLevel"))
                        .savedMap(DAO.getSettings().getIntElement("Register.StartMap"))
                        .savedCell(DAO.getSettings().getShortElement("Register.StartCell"))
                        .currentMap(DAO.getMaps().findTemplate(DAO.getSettings().getIntElement("Register.StartMap")).init$Return())

                        .enabledChannels(Arrays.stream(DAO.getSettings().getRegistredChannels()).collect(Collectors.toList()))
                        .statPoints((DAO.getSettings().getIntElement("Register.StartLevel") - 1) * 5)
                        .spellPoints((DAO.getSettings().getIntElement("Register.StartLevel") - 1))
                        .life(breedTemplate.getHealPoint() + (((byte) DAO.getSettings().getIntElement("Register.StartLevel") - 1) * 5))
                        .experience(DAO.getExps().getPlayerMinExp(DAO.getSettings().getIntElement("Register.StartLevel")))
                        .kamas(DAO.getSettings().getIntElement("Register.KamasStart"))
                        .shortcuts(new ShortcutBook())
                        .emotes(new byte[]{1, 8, 19})
                        .ornaments(new int[0])
                        .titles(new int[0])
                        .moodSmiley((byte)-1)
                        .alignmentSide(AlignmentSideEnum.ALIGNMENT_NEUTRAL)
                        .build();
                character.setMountInfo(new MountInformations(character));
                character.initScore();
                character.setMapid(DAO.getSettings().getIntElement("Register.StartMap")); //Abstract can not be called from builder
                character.setActorCell(character.getCurrentMap().getCell(DAO.getSettings().getShortElement("Register.StartCell")));
                character.setOnTutorial(true);
                character.initialize();

                if (!DAO.getPlayers().add(character)) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }


                client.getAccount().characters.add(0, character);
                Main.interClient().send(new PlayerCreatedMessage(client.getAccount().characters.size(), client.getAccount().id));
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.OK.value()));
                client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

}
