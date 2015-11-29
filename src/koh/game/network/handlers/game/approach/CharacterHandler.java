package koh.game.network.handlers.game.approach;

import java.sql.Timestamp;
import java.util.ArrayList;
import koh.d2o.entities.Breed;
import koh.d2o.entities.Head;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.D2oDaoImpl;
import koh.game.dao.mysql.ExpDAOImpl;
import koh.game.dao.mysql.MapDAOImpl;
import koh.game.dao.mysql.PlayerDAO;
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

/**
 *
 * @author Neo-Craft
 */
public class CharacterHandler {

    @HandlerAttribute(ID = 6072)
    public static void HandleCharacterSelectedForceReadyMessage(WorldClient Client, CharacterSelectedForceReadyMessage Message) {
        Player inFight = Client.getAccount().GetPlayerInFight();
        if (inFight != null) {
            CharacterSelectionMessage(Client, inFight.ID);
        }
        //Else le mec est sortie du combat osef
    }

    @HandlerAttribute(ID = SetEnablePVPRequestMessage.M_ID)
    public static void HandleSetEnablePVPRequestMessage(WorldClient Client, SetEnablePVPRequestMessage Message) {
        Client.Character.setEnabldPvp(Message.enable ? AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE : AggressableStatusEnum.NON_AGGRESSABLE);
    }

    @HandlerAttribute(ID = CharactersListRequestMessage.MESSAGE_ID)
    public static void HandleAuthenticationTicketMessage(WorldClient Client, Message message) {
        Player inFight = Client.getAccount().GetPlayerInFight();
        Client.Send(new CharactersListMessage(false, Client.getAccount().ToBaseInformations()));
        if (inFight != null) {
            Client.Send(new CharacterSelectedForceMessage(inFight.ID));
        }
    }

    @HandlerAttribute(ID = CharacterNameSuggestionRequestMessage.MESSAGE_ID)
    public static void HandleCharacterNameSuggestionRequestMessage(WorldClient Client, Message message) {
        Client.Send(new CharacterNameSuggestionSuccessMessage(PlayerController.GenerateName()));
    }
    
    @HandlerAttribute(ID = 165) //Suppresion
    public static void HandleCharacterDeletionRequestMessage(WorldClient Client , CharacterDeletionRequestMessage Message){
        Client.Send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
    }

    @HandlerAttribute(ID = 152)
    public static void HandleCharacterSelectionMessage(WorldClient Client, CharacterSelectionMessage message) {
        CharacterSelectionMessage(Client, message.id);
    }

    public static void CharacterSelectionMessage(WorldClient Client, int id) {
        try {

            Player Character = Client.getAccount().getPlayer(id);
            if (Character == null) {
                Client.Send(new CharacterSelectedErrorMessage());
            } else {
                Client.Character = Character;
                Character.Client = Client;
                Character.Initialize();
                Client.getAccount().currentCharacter = Character;
                Client.SequenceMessage();
                //Client.Send(new ComicReadingBeginMessage(79));
                Client.Send(new EnabledChannelsMessage(Character.EnnabledChannels, Character.DisabledChannels));
                Client.Send(new NotificationListMessage(new int[]{2147483647}));
                Client.Send(new CharacterSelectedSuccessMessage(Character.toBaseInformations(), false));
                Client.Send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                Client.Send(new InventoryContentMessage(Character.InventoryCache.toObjectsItem(), Character.Kamas));
                Client.Send(new ShortcutBarContentMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Character.Shortcuts.toShortcuts(Character)));
                Client.Send(new ShortcutBarContentMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, Character.mySpells.toShortcuts()));
                Client.Send(new EmoteListMessage(Character.Emotes));
                Client.Send(new JobDescriptionMessage(Character.myJobs.GetDescriptions()));
                Client.Send(new JobExperienceMultiUpdateMessage(Character.myJobs.GetExperiences()));
                Client.Send(new JobCrafterDirectorySettingsMessage(Character.myJobs.GetSettings()));

                Client.Send(new SpellListMessage(true, Character.mySpells.toSpellItems()));
                Client.Send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(false, false, false, false, false, false, false, false, true, false, false, false, false, true, true, true, false, false, false, false, false), Character.ID));
                Client.Send(new InventoryWeightMessage(Character.InventoryCache.Weight(), Character.InventoryCache.WeightTotal()));
                //GuilMember =! null
                Client.Send(new FriendWarnOnConnectionStateMessage(Client.getAccount().Data.friend_warn_on_login));
                Client.Send(new FriendWarnOnLevelGainStateMessage(Client.getAccount().Data.friend_warn_on_level_gain));
                Client.Send(new GuildMemberWarnOnConnectionStateMessage(Client.getAccount().Data.guild_warn_on_login));
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 89, new String[0]));

                Client.Send(new ServerSettingsMessage("fr", (byte) 1, (byte) 0));

                //System.out.println(String.valueOf(Client.getAccount().last_login.getYear() +" "+String.valueOf(Client.getAccount().last_login.getMonth())+" "+String.valueOf(Client.getAccount().last_login.getDay()))+" "+String.valueOf(Client.getAccount().last_login.getHours()));
                try {
                    Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 152, new String[]{
                        String.valueOf(Client.getAccount().last_login.getYear() - 100 + 2000),
                        String.valueOf(Client.getAccount().last_login.getDay()),
                        String.valueOf(Client.getAccount().last_login.getDate()),
                        String.valueOf(Client.getAccount().last_login.getHours()),
                        String.valueOf(Client.getAccount().last_login.getMinutes()), Client.getAccount().LastIP}));

                } catch (Exception e) {
                }
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 153, new String[]{Client.getIP()}));

                //Client.Send(new GameRolePlayArenaUpdatePlayerInfosMessage(0, 0, 0, 0, 0));
                Client.Send(new CharacterCapabilitiesMessage(4095));
                if (Client.Character.MountInfo.Mount != null) {
                    Client.Send(new MountSetMessage(Client.Character.MountInfo.Mount));
                    Client.Send(new MountXpRatioMessage(Client.Character.MountInfo.Ratio));
                    Client.Send(new MountRidingMessage(true));
                }
                Client.getAccount().last_login = new Timestamp(System.currentTimeMillis());
                Client.getAccount().LastIP = Client.getIP();
                //Todo : LastCharacter? + UPdate

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SendCharacterStatsListMessage(WorldClient Client) {
        Client.Send(new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) Client.Character.Experience, (double) ExpDAOImpl.persoXpMin(Client.Character.Level), (double) ExpDAOImpl.persoXpMax(Client.Character.Level), Client.Character.Kamas, Client.Character.StatPoints, 0, Client.Character.SpellPoints, Client.Character.GetActorAlignmentExtendInformations(),
                Client.Character.Life, Client.Character.MaxLife(), Client.Character.Energy, PlayerEnum.MaxEnergy,
                (short) Client.Character.Stats.GetTotal(StatsEnum.ActionPoints), (short) Client.Character.Stats.GetTotal(StatsEnum.MovementPoints),
                new CharacterBaseCharacteristic(Client.Character.Initiative(true), 0, Client.Character.Stats.GetItem(StatsEnum.Initiative), 0, 0), Client.Character.Stats.GetEffect(StatsEnum.Prospecting), Client.Character.Stats.GetEffect(StatsEnum.ActionPoints),
                Client.Character.Stats.GetEffect(StatsEnum.MovementPoints), Client.Character.Stats.GetEffect(StatsEnum.Strength), Client.Character.Stats.GetEffect(StatsEnum.Vitality),
                Client.Character.Stats.GetEffect(StatsEnum.Wisdom), Client.Character.Stats.GetEffect(StatsEnum.Chance), Client.Character.Stats.GetEffect(StatsEnum.Agility),
                Client.Character.Stats.GetEffect(StatsEnum.Intelligence), Client.Character.Stats.GetEffect(StatsEnum.Add_Range), Client.Character.Stats.GetEffect(StatsEnum.AddSummonLimit),
                Client.Character.Stats.GetEffect(StatsEnum.DamageReflection), Client.Character.Stats.GetEffect(StatsEnum.Add_CriticalHit), (short) Client.Character.InventoryCache.WeaponCriticalHit(),
                Client.Character.Stats.GetEffect(StatsEnum.CriticalMiss), Client.Character.Stats.GetEffect(StatsEnum.Add_Heal_Bonus), Client.Character.Stats.GetEffect(StatsEnum.AllDamagesBonus),
                Client.Character.Stats.GetEffect(StatsEnum.WeaponDamagesBonusPercent), Client.Character.Stats.GetEffect(StatsEnum.AddDamagePercent), Client.Character.Stats.GetEffect(StatsEnum.TrapBonus),
                Client.Character.Stats.GetEffect(StatsEnum.Trap_Damage_Percent), Client.Character.Stats.GetEffect(StatsEnum.GlyphBonusPercent), Client.Character.Stats.GetEffect(StatsEnum.PermanentDamagePercent), Client.Character.Stats.GetEffect(StatsEnum.Add_TackleBlock),
                Client.Character.Stats.GetEffect(StatsEnum.Add_TackleEvade), Client.Character.Stats.GetEffect(StatsEnum.Add_RETRAIT_PA), Client.Character.Stats.GetEffect(StatsEnum.Add_RETRAIT_PM), Client.Character.Stats.GetEffect(StatsEnum.Add_Push_Damages_Bonus),
                Client.Character.Stats.GetEffect(StatsEnum.Add_Critical_Damages), Client.Character.Stats.GetEffect(StatsEnum.Add_Neutral_Damages_Bonus), Client.Character.Stats.GetEffect(StatsEnum.Add_Earth_Damages_Bonus),
                Client.Character.Stats.GetEffect(StatsEnum.Add_Water_Damages_Bonus), Client.Character.Stats.GetEffect(StatsEnum.Add_Air_Damages_Bonus), Client.Character.Stats.GetEffect(StatsEnum.Add_Fire_Damages_Bonus),
                Client.Character.Stats.GetEffect(StatsEnum.DodgePALostProbability), Client.Character.Stats.GetEffect(StatsEnum.DodgePMLostProbability), Client.Character.Stats.GetEffect(StatsEnum.NeutralElementResistPercent),
                Client.Character.Stats.GetEffect(StatsEnum.EarthElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.WaterElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.AirElementResistPercent),
                Client.Character.Stats.GetEffect(StatsEnum.FireElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.NeutralElementReduction), Client.Character.Stats.GetEffect(StatsEnum.EarthElementReduction),
                Client.Character.Stats.GetEffect(StatsEnum.WaterElementReduction), Client.Character.Stats.GetEffect(StatsEnum.AirElementReduction), Client.Character.Stats.GetEffect(StatsEnum.FireElementReduction),
                Client.Character.Stats.GetEffect(StatsEnum.Add_Push_Damages_Reduction), Client.Character.Stats.GetEffect(StatsEnum.Add_Critical_Damages_Reduction), Client.Character.Stats.GetEffect(StatsEnum.PvpNeutralElementResistPercent),
                Client.Character.Stats.GetEffect(StatsEnum.PvpEarthElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.PvpWaterElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.PvpAirElementResistPercent),
                Client.Character.Stats.GetEffect(StatsEnum.PvpFireElementResistPercent), Client.Character.Stats.GetEffect(StatsEnum.PvpNeutralElementReduction), Client.Character.Stats.GetEffect(StatsEnum.PvpEarthElementReduction),
                Client.Character.Stats.GetEffect(StatsEnum.PvpWaterElementReduction), Client.Character.Stats.GetEffect(StatsEnum.PvpAirElementReduction), Client.Character.Stats.GetEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0)));
    }

    @HandlerAttribute(ID = CharacterCreationRequestMessage.MESSAGE_ID)
    public static void HandleCharacterCreationRequestMessage(WorldClient ClientO, Message message) {
        try {
            if (ClientO.getAccount().Characters.size() >= PlayerDAO.MaxCharacterSlot) {
                ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_TOO_MANY_CHARACTERS.value()));
            } else if (!PlayerController.isValidName(((CharacterCreationRequestMessage) message).Name)) {
                ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));

            } else if (PlayerDAO.DoesNameExist(((CharacterCreationRequestMessage) message).Name)) {
                ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
            } else {
                Breed breed = D2oDaoImpl.getBreed(((CharacterCreationRequestMessage) message).Breed);
                if (breed == null) {
                    ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NOT_ALLOWED.value()));
                    return;
                }
                Head head = D2oDaoImpl.getHead(((CharacterCreationRequestMessage) message).cosmeticId);
                if (head == null || head.breedtype != breed.id || head.gendertype == 1 != ((CharacterCreationRequestMessage) message).Sex) {
                    ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                Player Character = new Player() {
                    {
                        ID = PlayerDAO.NextID++;
                        NickName = ((CharacterCreationRequestMessage) message).Name;
                        Owner = ClientO.getAccount().ID;
                        Breed = (byte) ((CharacterCreationRequestMessage) message).Breed;
                        Sexe = ((CharacterCreationRequestMessage) message).Sex ? 1 : 0;
                        Skins = new ArrayList<Short>() {
                            {
                                add(Sexe == 1 ? breed.getFemaleLook() : breed.getMaleLook());
                                add(Short.parseShort(head.skinstype));
                            }
                        };
                        Scales = new ArrayList<Short>() {
                            {
                                add(Sexe == 1 ? breed.getFemaleSize() : breed.getMaleSize());

                            }
                        };
                        IndexedColors = new ArrayList<Integer>(5) {
                            {
                                for (byte i = 0; i < 5; i++) {
                                    if (((CharacterCreationRequestMessage) message).Colors.get(i) == -1) {
                                        add(breed.getColors(Sexe).get(i) | (i + 1) * 0x1000000);
                                    } else {
                                        add(((CharacterCreationRequestMessage) message).Colors.get(i) | (i + 1) * 0x1000000);
                                    }
                                }
                            }
                        };
                        Account = ClientO.getAccount();
                        Level = (byte) Settings.GetIntElement("Register.StartLevel");
                        SavedMap = Mapid = Settings.GetIntElement("Register.StartMap");
                        SavedCell = Settings.GetShortElement("Register.StartCell");
                        CurrentMap = MapDAOImpl.dofusMaps.get(Settings.GetIntElement("Register.StartMap"));
                        if (CurrentMap != null) {
                            CurrentMap.Init();
                        }
                        Cell = CurrentMap.getCell(Settings.GetShortElement("Register.StartCell"));
                        for (String s : Settings.GetStringElement("Register.Channels").split(",")) {
                            EnnabledChannels.add(Byte.parseByte(s));
                        }
                        StatPoints = (Settings.GetIntElement("Register.StartLevel") - 1) * 5;
                        SpellPoints = (Settings.GetIntElement("Register.StartLevel") - 1);
                        Life = breed.getHealPoint() + ((Level - 1) * 5);
                        Experience = ExpDAOImpl.persoXpMin(Settings.GetIntElement("Register.StartLevel"));
                        Kamas = Settings.GetIntElement("Register.KamasStart");
                        Shortcuts = new ShortcutBook();
                        Emotes = new byte[]{1, 8, 19};
                        Ornaments = Titles = new int[0];
                        this.MountInfo = new MountInformations(this);
                        this.Scores.put(ScoreType.PVP_WIN, 0);
                        this.Scores.put(ScoreType.PVP_LOOSE, 0);
                        this.Scores.put(ScoreType.ARENA_WIN, 0);
                        this.Scores.put(ScoreType.ARENA_LOOSE, 0);
                        this.Scores.put(ScoreType.PVM_WIN, 0);
                        this.Scores.put(ScoreType.PVM_LOOSE, 0);
                        this.Scores.put(ScoreType.PVP_TOURNAMENT, 0);
                    }
                };

                Character.Initialize();
                if (!PlayerDAO.Insert(Character)) {
                    ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                ClientO.getAccount().Characters.add(0, Character);
                Main.InterClient().Send(new PlayerCreatedMessage(ClientO.getAccount().Characters.size(), ClientO.getAccount().ID));
                ClientO.Send(new CharacterCreationResultMessage(CharacterCreationResultEnum.OK.value()));
                ClientO.Send(new CharactersListMessage(false, ClientO.getAccount().ToBaseInformations()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
