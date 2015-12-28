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
    public static void HandleCharacterSelectedForceReadyMessage(WorldClient Client, CharacterSelectedForceReadyMessage Message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        if (inFight != null) {
            characterSelectionMessage(Client, inFight.getID());
        }
        //Else le mec est sortie du combat osef
    }

    @HandlerAttribute(ID = SetEnablePVPRequestMessage.M_ID)
    public static void HandleSetEnablePVPRequestMessage(WorldClient Client, SetEnablePVPRequestMessage Message) {
        Client.getCharacter().setEnabldPvp(Message.enable ? AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE : AggressableStatusEnum.NON_AGGRESSABLE);
    }

    @HandlerAttribute(ID = CharactersListRequestMessage.MESSAGE_ID)
    public static void HandleAuthenticationTicketMessage(WorldClient Client, Message message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        Client.send(new CharactersListMessage(false, Client.getAccount().toBaseInformations()));
        if (inFight != null) {
            Client.send(new CharacterSelectedForceMessage(inFight.getID()));
        }
    }

    @HandlerAttribute(ID = CharacterNameSuggestionRequestMessage.MESSAGE_ID)
    public static void HandleCharacterNameSuggestionRequestMessage(WorldClient Client, Message message) {
        Client.send(new CharacterNameSuggestionSuccessMessage(PlayerController.GenerateName()));
    }

    @HandlerAttribute(ID = 165) //Suppresion
    public static void HandleCharacterDeletionRequestMessage(WorldClient Client, CharacterDeletionRequestMessage Message) {
        Client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
    }

    @HandlerAttribute(ID = 152)
    public static void HandleCharacterSelectionMessage(WorldClient Client, CharacterSelectionMessage message) {
        characterSelectionMessage(Client, message.id);
    }

    public static void characterSelectionMessage(WorldClient Client, int id) {
        try {

            Player character = Client.getAccount().getPlayer(id);
            if (character == null) {
                Client.send(new CharacterSelectedErrorMessage());
            } else {
                Client.setCharacter(character);
                character.setClient(Client);
                character.initialize();
                Client.getAccount().currentCharacter = character;
                Client.sequenceMessage();
                //client.send(new ComicReadingBeginMessage(79));
                Client.send(new EnabledChannelsMessage(character.getEnabledChannels(), character.getDisabledChannels()));
                Client.send(new NotificationListMessage(new int[]{2147483647}));
                Client.send(new CharacterSelectedSuccessMessage(character.toBaseInformations(), false));
                Client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                Client.send(new InventoryContentMessage(character.getInventoryCache().toObjectsItem(), character.getKamas()));
                Client.send(new ShortcutBarContentMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, character.getShortcuts().toShortcuts(character)));
                Client.send(new ShortcutBarContentMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, character.getMySpells().toShortcuts()));
                Client.send(new EmoteListMessage(character.getEmotes()));
                Client.send(new JobDescriptionMessage(character.getMyJobs().getDescriptions()));
                Client.send(new JobExperienceMultiUpdateMessage(character.getMyJobs().getExperiences()));
                Client.send(new JobCrafterDirectorySettingsMessage(character.getMyJobs().getSettings()));

                Client.send(new SpellListMessage(true, character.getMySpells().toSpellItems()));
                Client.send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(false, false, false, false, false, false, false, false, true, false, false, false, false, true, true, true, false, false, false, false, false), character.getID()));
                Client.send(new InventoryWeightMessage(character.getInventoryCache().getWeight(), character.getInventoryCache().getTotalWeight()));
                //GuilMember =! null
                Client.send(new FriendWarnOnConnectionStateMessage(Client.getAccount().accountData.friend_warn_on_login));
                Client.send(new FriendWarnOnLevelGainStateMessage(Client.getAccount().accountData.friend_warn_on_level_gain));
                Client.send(new GuildMemberWarnOnConnectionStateMessage(Client.getAccount().accountData.guild_warn_on_login));
                Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 89, new String[0]));

                Client.send(new ServerSettingsMessage("fr", (byte) 1, (byte) 0));

                //System.out.println(String.valueOf(client.getAccount().last_login.getYear() +" "+String.valueOf(client.getAccount().last_login.getMonth())+" "+String.valueOf(client.getAccount().last_login.getDay()))+" "+String.valueOf(client.getAccount().last_login.getHours()));
                try {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 152, new String[]{
                            String.valueOf(Client.getAccount().last_login.getYear() - 100 + 2000),
                            String.valueOf(Client.getAccount().last_login.getDay()),
                            String.valueOf(Client.getAccount().last_login.getDate()),
                            String.valueOf(Client.getAccount().last_login.getHours()),
                            String.valueOf(Client.getAccount().last_login.getMinutes()), Client.getAccount().lastIP}));

                } catch (Exception e) {
                }
                Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 153, new String[]{Client.getIP()}));

                //client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                Client.send(new CharacterCapabilitiesMessage(4095));
                if (Client.getCharacter().getMountInfo().mount != null) {
                    Client.send(new MountSetMessage(Client.getCharacter().getMountInfo().mount));
                    Client.send(new MountXpRatioMessage(Client.getCharacter().getMountInfo().ratio));
                    Client.send(new MountRidingMessage(true));
                }
                Client.getAccount().last_login = new Timestamp(System.currentTimeMillis());
                Client.getAccount().lastIP = Client.getIP();
                //Todo : LastCharacter? + UPdate

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SendCharacterStatsListMessage(WorldClient client) {
        client.send(new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) client.getCharacter().getExperience(), (double) DAO.getExps().getPlayerMinExp(client.getCharacter().getLevel()), (double) DAO.getExps().getPlayerMaxExp(client.getCharacter().getLevel()), client.getCharacter().getKamas(), client.getCharacter().getStatPoints(), 0, client.getCharacter().getSpellPoints(), client.getCharacter().getActorAlignmentExtendInformations(),
                client.getCharacter().getLife(), client.getCharacter().getMaxLife(), client.getCharacter().getEnergy(), PlayerEnum.MaxEnergy,
                (short) client.getCharacter().getStats().getTotal(StatsEnum.ActionPoints), (short) client.getCharacter().getStats().getTotal(StatsEnum.MovementPoints),
                new CharacterBaseCharacteristic(client.getCharacter().getInitiative(true), 0, client.getCharacter().getStats().getItem(StatsEnum.Initiative), 0, 0), client.getCharacter().getStats().getEffect(StatsEnum.Prospecting), client.getCharacter().getStats().getEffect(StatsEnum.ActionPoints),
                client.getCharacter().getStats().getEffect(StatsEnum.MovementPoints), client.getCharacter().getStats().getEffect(StatsEnum.Strength), client.getCharacter().getStats().getEffect(StatsEnum.Vitality),
                client.getCharacter().getStats().getEffect(StatsEnum.Wisdom), client.getCharacter().getStats().getEffect(StatsEnum.Chance), client.getCharacter().getStats().getEffect(StatsEnum.Agility),
                client.getCharacter().getStats().getEffect(StatsEnum.Intelligence), client.getCharacter().getStats().getEffect(StatsEnum.Add_Range), client.getCharacter().getStats().getEffect(StatsEnum.AddSummonLimit),
                client.getCharacter().getStats().getEffect(StatsEnum.DamageReflection), client.getCharacter().getStats().getEffect(StatsEnum.Add_CriticalHit), (short) client.getCharacter().getInventoryCache().weaponCriticalHit(),
                client.getCharacter().getStats().getEffect(StatsEnum.CriticalMiss), client.getCharacter().getStats().getEffect(StatsEnum.Add_Heal_Bonus), client.getCharacter().getStats().getEffect(StatsEnum.AllDamagesBonus),
                client.getCharacter().getStats().getEffect(StatsEnum.WeaponDamagesBonusPercent), client.getCharacter().getStats().getEffect(StatsEnum.AddDamagePercent), client.getCharacter().getStats().getEffect(StatsEnum.TrapBonus),
                client.getCharacter().getStats().getEffect(StatsEnum.Trap_Damage_Percent), client.getCharacter().getStats().getEffect(StatsEnum.GlyphBonusPercent), client.getCharacter().getStats().getEffect(StatsEnum.PermanentDamagePercent), client.getCharacter().getStats().getEffect(StatsEnum.Add_TackleBlock),
                client.getCharacter().getStats().getEffect(StatsEnum.Add_TackleEvade), client.getCharacter().getStats().getEffect(StatsEnum.Add_RETRAIT_PA), client.getCharacter().getStats().getEffect(StatsEnum.Add_RETRAIT_PM), client.getCharacter().getStats().getEffect(StatsEnum.Add_Push_Damages_Bonus),
                client.getCharacter().getStats().getEffect(StatsEnum.Add_Critical_Damages), client.getCharacter().getStats().getEffect(StatsEnum.Add_Neutral_Damages_Bonus), client.getCharacter().getStats().getEffect(StatsEnum.Add_Earth_Damages_Bonus),
                client.getCharacter().getStats().getEffect(StatsEnum.Add_Water_Damages_Bonus), client.getCharacter().getStats().getEffect(StatsEnum.Add_Air_Damages_Bonus), client.getCharacter().getStats().getEffect(StatsEnum.Add_Fire_Damages_Bonus),
                client.getCharacter().getStats().getEffect(StatsEnum.DodgePALostProbability), client.getCharacter().getStats().getEffect(StatsEnum.DodgePMLostProbability), client.getCharacter().getStats().getEffect(StatsEnum.NeutralElementResistPercent),
                client.getCharacter().getStats().getEffect(StatsEnum.EarthElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.WaterElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.AirElementResistPercent),
                client.getCharacter().getStats().getEffect(StatsEnum.FireElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.NeutralElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.EarthElementReduction),
                client.getCharacter().getStats().getEffect(StatsEnum.WaterElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.AirElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.FireElementReduction),
                client.getCharacter().getStats().getEffect(StatsEnum.Add_Push_Damages_Reduction), client.getCharacter().getStats().getEffect(StatsEnum.Add_Critical_Damages_Reduction), client.getCharacter().getStats().getEffect(StatsEnum.PvpNeutralElementResistPercent),
                client.getCharacter().getStats().getEffect(StatsEnum.PvpEarthElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.PvpWaterElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.PvpAirElementResistPercent),
                client.getCharacter().getStats().getEffect(StatsEnum.PvpFireElementResistPercent), client.getCharacter().getStats().getEffect(StatsEnum.PvpNeutralElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.PvpEarthElementReduction),
                client.getCharacter().getStats().getEffect(StatsEnum.PvpWaterElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.PvpAirElementReduction), client.getCharacter().getStats().getEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0)));
    }

    @HandlerAttribute(ID = CharacterCreationRequestMessage.MESSAGE_ID)
    public static void HandleCharacterCreationRequestMessage(WorldClient client, CharacterCreationRequestMessage message) {
        try {
            if (client.getAccount().characters.size() >= PlayerDAOImpl.MAX_CHARACTER_SLOT) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_TOO_MANY_CHARACTERS.value()));
            } else if (!PlayerController.isValidName(((CharacterCreationRequestMessage) message).Name)) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));

            } else if (DAO.getPlayers().containsName(((CharacterCreationRequestMessage) message).Name)) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
            } else {
                Breed breedTemplate = DAO.getD2oTemplates().getBreed(((CharacterCreationRequestMessage) message).Breed);
                if (breedTemplate == null) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NOT_ALLOWED.value()));
                    return;
                }
                Head head = DAO.getD2oTemplates().getHead(((CharacterCreationRequestMessage) message).cosmeticId);
                if (head == null || head.breedtype != breedTemplate.id || head.gendertype == 1 != ((CharacterCreationRequestMessage) message).Sex) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                Player character = Player.builder()
                        .nickName(message.Name)
                        .regenRate((byte)10)
                        .breed((byte) message.Breed)
                        .owner(client.getAccount().id)
                        .sexe(message.Sex ? 1 : 0)
                        .skins(new ArrayList<Short>(Arrays.asList(message.Sex ? breedTemplate.getFemaleLook() : breedTemplate.getMaleLook(), Short.parseShort(head.skinstype))))
                        .scales(new ArrayList<Short>(Arrays.asList(message.Sex ? breedTemplate.getFemaleSize() : breedTemplate.getMaleSize())))
                        .indexedColors(new ArrayList<Integer>(5) {
                            {
                                for (byte i = 0; i < 5; i++) {
                                    if (message.Colors.get(i) == -1) {
                                        add(breedTemplate.getColors(message.Sex ? 1 : 0).get(i) | (i + 1) * 0x1000000);
                                    } else {
                                        add(message.Colors.get(i) | (i + 1) * 0x1000000);
                                    }
                                }
                            }
                        })
                        .account(client.getAccount())
                        .level((byte) DAO.getSettings().getIntElement("Register.StartLevel"))
                        .savedMap(DAO.getSettings().getIntElement("Register.StartMap"))
                        .savedCell(DAO.getSettings().getShortElement("Register.getStartCell"))
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
                character.setActorCell(character.getCurrentMap().getCell(DAO.getSettings().getShortElement("Register.getStartCell")));

                if (!DAO.getPlayers().add(character)) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                character.initialize();

                client.getAccount().characters.add(0, character);
                Main.interClient().Send(new PlayerCreatedMessage(client.getAccount().characters.size(), client.getAccount().id));
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.OK.value()));
                client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

}
