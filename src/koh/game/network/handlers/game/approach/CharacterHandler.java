package koh.game.network.handlers.game.approach;

import java.sql.Timestamp;
import java.util.ArrayList;
import koh.d2o.entities.Breed;
import koh.d2o.entities.Head;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.dao.mysql.ExpDAOImpl;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.actors.character.ShortcutBook;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.utils.Settings;
import koh.inter.messages.PlayerCreatedMessage;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AggressableStatusEnum;
import koh.protocol.client.enums.CharacterCreationResultEnum;
import koh.protocol.client.enums.CharacterDeletionErrorEnum;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
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

/**
 *
 * @author Neo-Craft
 */
public class CharacterHandler {

    private static final Logger logger = LogManager.getLogger(CharacterHandler.class);

    @HandlerAttribute(ID = 6072)
    public static void HandleCharacterSelectedForceReadyMessage(WorldClient Client, CharacterSelectedForceReadyMessage Message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        if (inFight != null) {
            CharacterSelectionMessage(Client, inFight.ID);
        }
        //Else le mec est sortie du combat osef
    }

    @HandlerAttribute(ID = SetEnablePVPRequestMessage.M_ID)
    public static void HandleSetEnablePVPRequestMessage(WorldClient Client, SetEnablePVPRequestMessage Message) {
        Client.character.setEnabldPvp(Message.enable ? AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE : AggressableStatusEnum.NON_AGGRESSABLE);
    }

    @HandlerAttribute(ID = CharactersListRequestMessage.MESSAGE_ID)
    public static void HandleAuthenticationTicketMessage(WorldClient Client, Message message) {
        Player inFight = Client.getAccount().getPlayerInFight();
        Client.send(new CharactersListMessage(false, Client.getAccount().toBaseInformations()));
        if (inFight != null) {
            Client.send(new CharacterSelectedForceMessage(inFight.ID));
        }
    }

    @HandlerAttribute(ID = CharacterNameSuggestionRequestMessage.MESSAGE_ID)
    public static void HandleCharacterNameSuggestionRequestMessage(WorldClient Client, Message message) {
        Client.send(new CharacterNameSuggestionSuccessMessage(PlayerController.GenerateName()));
    }
    
    @HandlerAttribute(ID = 165) //Suppresion
    public static void HandleCharacterDeletionRequestMessage(WorldClient Client , CharacterDeletionRequestMessage Message){
        Client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
    }

    @HandlerAttribute(ID = 152)
    public static void HandleCharacterSelectionMessage(WorldClient Client, CharacterSelectionMessage message) {
        CharacterSelectionMessage(Client, message.id);
    }

    public static void CharacterSelectionMessage(WorldClient Client, int id) {
        try {

            Player Character = Client.getAccount().getPlayer(id);
            if (Character == null) {
                Client.send(new CharacterSelectedErrorMessage());
            } else {
                Client.character = Character;
                Character.client = Client;
                Character.initialize();
                Client.getAccount().currentCharacter = Character;
                Client.sequenceMessage();
                //client.send(new ComicReadingBeginMessage(79));
                Client.send(new EnabledChannelsMessage(Character.ennabledChannels, Character.DisabledChannels));
                Client.send(new NotificationListMessage(new int[]{2147483647}));
                Client.send(new CharacterSelectedSuccessMessage(Character.toBaseInformations(), false));
                Client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                Client.send(new InventoryContentMessage(Character.inventoryCache.toObjectsItem(), Character.kamas));
                Client.send(new ShortcutBarContentMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Character.shortcuts.toShortcuts(Character)));
                Client.send(new ShortcutBarContentMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, Character.mySpells.toShortcuts()));
                Client.send(new EmoteListMessage(Character.emotes));
                Client.send(new JobDescriptionMessage(Character.myJobs.getDescriptions()));
                Client.send(new JobExperienceMultiUpdateMessage(Character.myJobs.getExperiences()));
                Client.send(new JobCrafterDirectorySettingsMessage(Character.myJobs.getSettings()));

                Client.send(new SpellListMessage(true, Character.mySpells.toSpellItems()));
                Client.send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(false, false, false, false, false, false, false, false, true, false, false, false, false, true, true, true, false, false, false, false, false), Character.ID));
                Client.send(new InventoryWeightMessage(Character.inventoryCache.getWeight(), Character.inventoryCache.getTotalWeight()));
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
                if (Client.character.mountInfo.mount != null) {
                    Client.send(new MountSetMessage(Client.character.mountInfo.mount));
                    Client.send(new MountXpRatioMessage(Client.character.mountInfo.ratio));
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
        client.send(new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) client.character.experience, (double) ExpDAOImpl.persoXpMin(client.character.level), (double) ExpDAOImpl.persoXpMax(client.character.level), client.character.kamas, client.character.statPoints, 0, client.character.spellPoints, client.character.getActorAlignmentExtendInformations(),
                client.character.life, client.character.getMaxLife(), client.character.energy, PlayerEnum.MaxEnergy,
                (short) client.character.stats.getTotal(StatsEnum.ActionPoints), (short) client.character.stats.getTotal(StatsEnum.MovementPoints),
                new CharacterBaseCharacteristic(client.character.getInitiative(true), 0, client.character.stats.getItem(StatsEnum.Initiative), 0, 0), client.character.stats.getEffect(StatsEnum.Prospecting), client.character.stats.getEffect(StatsEnum.ActionPoints),
                client.character.stats.getEffect(StatsEnum.MovementPoints), client.character.stats.getEffect(StatsEnum.Strength), client.character.stats.getEffect(StatsEnum.Vitality),
                client.character.stats.getEffect(StatsEnum.Wisdom), client.character.stats.getEffect(StatsEnum.Chance), client.character.stats.getEffect(StatsEnum.Agility),
                client.character.stats.getEffect(StatsEnum.Intelligence), client.character.stats.getEffect(StatsEnum.Add_Range), client.character.stats.getEffect(StatsEnum.AddSummonLimit),
                client.character.stats.getEffect(StatsEnum.DamageReflection), client.character.stats.getEffect(StatsEnum.Add_CriticalHit), (short) client.character.inventoryCache.weaponCriticalHit(),
                client.character.stats.getEffect(StatsEnum.CriticalMiss), client.character.stats.getEffect(StatsEnum.Add_Heal_Bonus), client.character.stats.getEffect(StatsEnum.AllDamagesBonus),
                client.character.stats.getEffect(StatsEnum.WeaponDamagesBonusPercent), client.character.stats.getEffect(StatsEnum.AddDamagePercent), client.character.stats.getEffect(StatsEnum.TrapBonus),
                client.character.stats.getEffect(StatsEnum.Trap_Damage_Percent), client.character.stats.getEffect(StatsEnum.GlyphBonusPercent), client.character.stats.getEffect(StatsEnum.PermanentDamagePercent), client.character.stats.getEffect(StatsEnum.Add_TackleBlock),
                client.character.stats.getEffect(StatsEnum.Add_TackleEvade), client.character.stats.getEffect(StatsEnum.Add_RETRAIT_PA), client.character.stats.getEffect(StatsEnum.Add_RETRAIT_PM), client.character.stats.getEffect(StatsEnum.Add_Push_Damages_Bonus),
                client.character.stats.getEffect(StatsEnum.Add_Critical_Damages), client.character.stats.getEffect(StatsEnum.Add_Neutral_Damages_Bonus), client.character.stats.getEffect(StatsEnum.Add_Earth_Damages_Bonus),
                client.character.stats.getEffect(StatsEnum.Add_Water_Damages_Bonus), client.character.stats.getEffect(StatsEnum.Add_Air_Damages_Bonus), client.character.stats.getEffect(StatsEnum.Add_Fire_Damages_Bonus),
                client.character.stats.getEffect(StatsEnum.DodgePALostProbability), client.character.stats.getEffect(StatsEnum.DodgePMLostProbability), client.character.stats.getEffect(StatsEnum.NeutralElementResistPercent),
                client.character.stats.getEffect(StatsEnum.EarthElementResistPercent), client.character.stats.getEffect(StatsEnum.WaterElementResistPercent), client.character.stats.getEffect(StatsEnum.AirElementResistPercent),
                client.character.stats.getEffect(StatsEnum.FireElementResistPercent), client.character.stats.getEffect(StatsEnum.NeutralElementReduction), client.character.stats.getEffect(StatsEnum.EarthElementReduction),
                client.character.stats.getEffect(StatsEnum.WaterElementReduction), client.character.stats.getEffect(StatsEnum.AirElementReduction), client.character.stats.getEffect(StatsEnum.FireElementReduction),
                client.character.stats.getEffect(StatsEnum.Add_Push_Damages_Reduction), client.character.stats.getEffect(StatsEnum.Add_Critical_Damages_Reduction), client.character.stats.getEffect(StatsEnum.PvpNeutralElementResistPercent),
                client.character.stats.getEffect(StatsEnum.PvpEarthElementResistPercent), client.character.stats.getEffect(StatsEnum.PvpWaterElementResistPercent), client.character.stats.getEffect(StatsEnum.PvpAirElementResistPercent),
                client.character.stats.getEffect(StatsEnum.PvpFireElementResistPercent), client.character.stats.getEffect(StatsEnum.PvpNeutralElementReduction), client.character.stats.getEffect(StatsEnum.PvpEarthElementReduction),
                client.character.stats.getEffect(StatsEnum.PvpWaterElementReduction), client.character.stats.getEffect(StatsEnum.PvpAirElementReduction), client.character.stats.getEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0)));
    }

    @HandlerAttribute(ID = CharacterCreationRequestMessage.MESSAGE_ID)
    public static void HandleCharacterCreationRequestMessage(WorldClient client, CharacterCreationRequestMessage message) {
        try {
            if (client.getAccount().characters.size() >= PlayerDAOImpl.MaxCharacterSlot) {
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
                Player Character = new Player() {
                    {
                        nickName = message.Name;
                        owner = client.getAccount().id;
                        breed = (byte) message.Breed;
                        sexe =  message.Sex ? 1 : 0;
                        skins = new ArrayList<Short>() {
                            {
                                add(sexe == 1 ? breedTemplate.getFemaleLook() : breedTemplate.getMaleLook());
                                add(Short.parseShort(head.skinstype));
                            }
                        };
                        scales = new ArrayList<Short>() {
                            {
                                add(sexe == 1 ? breedTemplate.getFemaleSize() : breedTemplate.getMaleSize());

                            }
                        };
                        indexedColors = new ArrayList<Integer>(5) {
                            {
                                for (byte i = 0; i < 5; i++) {
                                    if (message.Colors.get(i) == -1) {
                                        add(breedTemplate.getColors(sexe).get(i) | (i + 1) * 0x1000000);
                                    } else {
                                        add(message.Colors.get(i) | (i + 1) * 0x1000000);
                                    }
                                }
                            }
                        };
                        account = client.getAccount();
                        level = (byte) Settings.GetIntElement("Register.StartLevel");
                        savedMap = mapid = Settings.GetIntElement("Register.StartMap");
                        savedCell = Settings.GetShortElement("Register.StartCell");
                        currentMap = DAO.getMaps().getMap(Settings.GetIntElement("Register.StartMap"));
                        if (currentMap != null) {
                            currentMap.Init();
                        }
                        cell = currentMap.getCell(Settings.GetShortElement("Register.StartCell"));
                        for (String s : Settings.GetStringElement("Register.Channels").split(",")) {
                            ennabledChannels.add(Byte.parseByte(s));
                        }
                        statPoints = (Settings.GetIntElement("Register.StartLevel") - 1) * 5;
                        spellPoints = (Settings.GetIntElement("Register.StartLevel") - 1);
                        life = breedTemplate.getHealPoint() + ((level - 1) * 5);
                        experience = DAO.getExps().getPlayerMinExp(Settings.GetIntElement("Register.StartLevel"));
                        kamas = Settings.GetIntElement("Register.KamasStart");
                        shortcuts = new ShortcutBook();
                        emotes = new byte[]{1, 8, 19};
                        ornaments = titles = new int[0];
                        this.mountInfo = new MountInformations(this);
                        this.scores.put(ScoreType.PVP_WIN, 0);
                        this.scores.put(ScoreType.PVP_LOOSE, 0);
                        this.scores.put(ScoreType.ARENA_WIN, 0);
                        this.scores.put(ScoreType.ARENA_LOOSE, 0);
                        this.scores.put(ScoreType.PVM_WIN, 0);
                        this.scores.put(ScoreType.PVM_LOOSE, 0);
                        this.scores.put(ScoreType.PVP_TOURNAMENT, 0);
                    }
                };
                if (!DAO.getPlayers().add(Character)) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                Character.initialize();

                client.getAccount().characters.add(0, Character);
                Main.InterClient().Send(new PlayerCreatedMessage(client.getAccount().characters.size(), client.getAccount().id));
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.OK.value()));
                client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

}
