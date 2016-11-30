package koh.game.network.handlers.game.approach;

import koh.d2o.entities.Breed;
import koh.d2o.entities.Head;
import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.PlayerInst;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.actors.character.shortcut.ShortcutBook;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.glicko.Glicko2Player;
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
import koh.protocol.messages.game.inventory.items.InventoryContentAndPresetMessage;
import koh.protocol.messages.game.pvp.SetEnablePVPRequestMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarContentMessage;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.SetCharacterRestrictionsMessage;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import koh.protocol.types.game.character.choice.CharacterToRecolorInformation;
import koh.protocol.types.game.character.choice.CharacterToRelookInformation;
import koh.protocol.types.game.character.choice.CharacterToRemodelInformations;
import koh.protocol.types.game.inventory.preset.Preset;
import koh.protocol.types.game.look.EntityLook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Neo-Craft
 */
public class CharacterHandler {

    private static final Logger logger = LogManager.getLogger(CharacterHandler.class);

    @HandlerAttribute(ID = 6072)
    public static void handleCharacterSelectedForceReadyMessage(WorldClient Client, CharacterSelectedForceReadyMessage Message) {
        final Player inFight = Client.getAccount().getPlayerInFight();
        if (inFight != null) {
            characterSelectionMessage(Client, inFight.getID());
        }
        //Else le mec est sortie du combat osef
    }

    @HandlerAttribute(ID = SetEnablePVPRequestMessage.M_ID)
    public static void handleSetEnablePVPRequestMessage(WorldClient client, SetEnablePVPRequestMessage message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
            return;
        }
        final PlayerInst inst = PlayerInst.getPlayerInst(client.getCharacter().getID());
        if (inst.getAlignmentChange() > 5 && !message.enable) {
            PlayerController.sendServerErrorMessage(client, "Vous ne pouvez pas changer vos ailes plus de 5 fois par jour");
        }
        else if(client.getCharacter().getAlignmentGrade() >= 10 && !message.enable){
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR,317));
        }
        else {
            if(!client.canParsePacket(message.getClass().getName(), 900)){
                return;
            }
            inst.setAlignmentChange(inst.getAlignmentChange() + 1);
            client.getCharacter().setEnabldPvp(message.enable ? AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE : AggressableStatusEnum.NON_AGGRESSABLE);
        }

    }

    private static final int
            CHANGE_COLOR = 1,
            CHANGE_NAME = 3,
                    CHANGE_SEX = 0,
    CHANGE_FACE_T = 2;
    private static final byte CHANGE_FACE = 2;


    @HandlerAttribute(ID = 6121)
    public static void handleCharacterSelectionWithRenameMessage(WorldClient client, CharacterSelectionWithRenameMessage message){
        final Player character = client.getAccount().getPlayer(message.id);
        if(character != null){
            if (!PlayerController.isValidName((message.name))) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));

            } else if (DAO.getPlayers().containsName((message.name))) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
            }else{
                client.getCharacter().setNickName(message.name);
                characterSelectionMessage(client, message.id);
                character.getBooleans().replace(CHANGE_COLOR, FALSE);
            }
        }
    }

    @HandlerAttribute(ID = 6549)
    public static void handleCharacterSelectionWithRemodelMessage(WorldClient client, CharacterSelectionWithRemodelMessage message){
        if(client.getCharacter() != null){
            return;
        }
        final Player character = client.getAccount().getPlayer(message.id);

        if(character != null){
            if(!character.getNickName().toLowerCase().equalsIgnoreCase(message.remodel.name)){
                if (!PlayerController.isValidName((message.remodel.name))) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
                    return;

                } else if (DAO.getPlayers().containsName((message.remodel.name))) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
                    return;
                }
                else{
                    DAO.getPlayers().delCharacter(character);
                    character.setNickName(message.remodel.name);
                    DAO.getPlayers().addCharacter(character);
                    DAO.getPlayers().updateName(character);
                }
            }
            final EntityLook target;
            if (character.getEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER))
                target = character.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook;
            else
                target = character.getEntityLook();

            final Breed breedTemplate = DAO.getD2oTemplates().getBreed((message.remodel.breed));
            if (breedTemplate == null) {
                characterSelectionMessage(client, message.id);
                return;
            }
            final ArrayList<Integer> oldColors = new ArrayList<>(character.getIndexedColors());
            try {
                character.getIndexedColors().clear();
                character.setIndexedColors(new ArrayList<Integer>(5) {
                    {
                        for (byte i = 0; i < 5; i++) {
                            try {
                                if (message.remodel.colors[i] == -1) {
                                    add(breedTemplate.getColors(message.remodel.sex ? 1 : 0).get(i) | (i + 1) * 0x1000000);
                                } else {
                                    add(message.remodel.colors[i] | (i + 1) * 0x1000000);
                                }
                            }catch (ArrayIndexOutOfBoundsException e){
                                if (oldColors.get(i) == -1) {
                                    add(breedTemplate.getColors(message.remodel.sex ? 1 : 0).get(i) | (i + 1) * 0x1000000);
                                } else {
                                    add(oldColors.get(i) | (i + 1) * 0x1000000);
                                }
                            }
                        }
                    }
                });
                target.indexedColors.clear();
                target.indexedColors = (new ArrayList<Integer>(character.getIndexedColors()));
            }
            catch (Exception e){
                e.printStackTrace();
                character.setIndexedColors(oldColors);
            }



            final Head head = DAO.getD2oTemplates().getHead(message.remodel.cosmeticId);
            if (head != null && head.breedtype == breedTemplate.id /*|| head.gendertype == 1 != message.remodel.sex)*/) {
                target.skins.remove(0);
                target.skins.remove(0);
                target.skins.add(0,message.remodel.sex ? breedTemplate.getFemaleLook() : breedTemplate.getMaleLook());
                target.skins.add(1,Short.parseShort(head.skinstype));
                character.getSkins().clear();
                character.getSkins().add(0,message.remodel.sex ? breedTemplate.getFemaleLook() : breedTemplate.getMaleLook());
                character.getSkins().add(1,Short.parseShort(head.skinstype));
            }

            character.getScales().remove(0);
            character.getScales().add(0,message.remodel.sex ? breedTemplate.getFemaleSize() : breedTemplate.getMaleSize());
            target.scales.remove(0);
            target.scales.add(0,message.remodel.sex ? breedTemplate.getFemaleSize() : breedTemplate.getMaleSize());
            character.setSexe(message.remodel.sex ? 1 : 0);

        }
        characterSelectionMessage(client, message.id);
    }

    private static final short FALSE = 0;

    @HandlerAttribute(ID = CharactersListRequestMessage.MESSAGE_ID)
    public static void handleAuthenticationTicketMessage(WorldClient client, Message message) {
        final Player inFight = client.getAccount().getPlayerInFight();

        final Player model = client.getAccount().getCharacters().filter(p -> p.getBooleans() != null && p.getBooleans().entrySet().stream().anyMatch(x -> x.getKey() < 4 && x.getValue() == 1)).findFirst().orElse(null);
        if(model != null && inFight == null){
            switch (model.getBooleans().entrySet().stream().filter(x -> x.getKey() <4 && x.getValue() == 1).findAny().get().getKey()){
                case CHANGE_COLOR:
                    model.getBooleans().replace(CHANGE_COLOR, FALSE);
                    client.send(new CharactersListWithRemodelingMessage(false, client.getAccount().toBaseInformations(), new CharacterToRemodelInformations[] { new CharacterToRemodelInformations(model.getID(), model.getNickName(), model.getBreed(), model.getSexe() == 1, model.getCosmetic(), model.getIndexedColors().stream().mapToInt(x -> x).toArray(),CHANGE_FACE,CHANGE_FACE) }));
                    break;
                case CHANGE_SEX:
                    model.getBooleans().replace(CHANGE_SEX, FALSE);
                    client.send(new CharactersListWithRemodelingMessage(false, client.getAccount().toBaseInformations(), new CharacterToRemodelInformations[] { new CharacterToRemodelInformations(model.getID(), model.getNickName(), model.getBreed(), model.getSexe() == 1, model.getCosmetic(), model.getIndexedColors().stream().mapToInt(x -> x).toArray(),(byte)20,(byte) 20) }));
                    break;
                case CHANGE_FACE_T:
                    model.getBooleans().replace(CHANGE_FACE_T, FALSE);
                    client.send(new CharactersListWithRemodelingMessage(false, client.getAccount().toBaseInformations(), new CharacterToRemodelInformations[] { new CharacterToRemodelInformations(model.getID(), model.getNickName(), model.getBreed(), model.getSexe() == 1, model.getCosmetic(), model.getIndexedColors().stream().mapToInt(x -> x).toArray(),(byte)4,(byte) 4) }));
                break;
                case CHANGE_NAME:
                    model.getBooleans().replace(CHANGE_NAME, FALSE);
                    client.send(new CharactersListWithRemodelingMessage(false, client.getAccount().toBaseInformations(), new CharacterToRemodelInformations[] { new CharacterToRemodelInformations(model.getID(), model.getNickName(), model.getBreed(), model.getSexe() == 1, model.getCosmetic(), model.getIndexedColors().stream().mapToInt(x -> x).toArray(),(byte)1,(byte) 1) }));
                    break;
                default:
                    client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
                    break;
            }
        }else{
            client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
        }
        //client.send(new CharactersListWithRemodelingMessage(false, client.getAccount().toBaseInformations(), new CharacterToRemodelInformations[] { new CharacterToRemodelInformations(first.getID(), first.getNickName(), first.getBreed(), first.getSexe() == 1, first.getSkins().get(1), first.getIndexedColors().stream().mapToInt(x -> x).toArray(),(byte)first.getSkins().get(1).byteValue(),(byte)first.getSkins().get(1).byteValue()) }));

        if (inFight != null) {
            client.send(new CharacterSelectedForceMessage(inFight.getID()));
        }
    }

    @HandlerAttribute(ID = CharacterNameSuggestionRequestMessage.MESSAGE_ID)
    public static void handleCharacterNameSuggestionRequestMessage(WorldClient Client, Message message) {
        Client.send(new CharacterNameSuggestionSuccessMessage(PlayerController.GenerateName()));
    }

    private static MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    @HandlerAttribute(ID = 165) //Suppresion
    public static void handleCharacterDeletionRequestMessage(WorldClient client, CharacterDeletionRequestMessage message) {
        final Player character = client.getAccount().getPlayer(message.characterId);
        final StringBuffer answer = new StringBuffer();
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((message.characterId + "~" + client.getAccount().secretAnswer).getBytes());
            byte byteData[] = md.digest();
            for (int i = 0; i < byteData.length; i++) {
                answer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (character == null) {
            client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
        } else if (character.getLevel() > 20 && !answer.toString().equalsIgnoreCase(message.secretAnswerHash)) {
            client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_BAD_SECRET_ANSWER));
        } else if (!client.getAccount().remove(message.characterId)) {
            client.send(new CharacterDeletionErrorMessage(CharacterDeletionErrorEnum.DEL_ERR_RESTRICED_ZONE));
        } else {
            DAO.getPlayers().remove(message.characterId);
            client.send(new CharactersListMessage(false, client.getAccount().toBaseInformations()));
        }

    }

    @HandlerAttribute(ID = 152)
    public static void handleCharacterSelectionMessage(WorldClient Client, CharacterSelectionMessage message) {
        characterSelectionMessage(Client, message.id);
    }

    public static void characterSelectionMessage(WorldClient client, int id) {
        try {

            final Player character = client.getAccount().getPlayer(id);
            if(client.getCharacter() != null){
                client.send(new CharacterSelectedErrorMessage());
                return;
            }
            if (character == null) {
                client.send(new CharacterSelectedErrorMessage());
            } else {
                client.setCharacter(character);
                character.setClient(client);
                character.initialize();
                client.getAccount().currentCharacter = character;
                client.sequenceMessage();
                final PlayerInst inst = PlayerInst.getPlayerInst(character.getID());
                //client.send(new ComicReadingBeginMessage(79));
                client.send(new EnabledChannelsMessage(character.getEnabledChannels(), character.getDisabledChannels()));
                client.send(new NotificationListMessage(new int[]{2147483647}));
                client.send(new CharacterSelectedSuccessMessage(character.toBaseInformations(), false));
                client.send(new GameRolePlayArenaUpdatePlayerInfosMessage(character.getKolizeumRate().getScreenRating(), inst.getDailyCote(), character.getScores().get(ScoreType.BEST_COTE), inst.getDailyWins(), inst.getDailyFight()));
                if (client.getCharacter().getPresets().size() > 0) {
                    client.send(new InventoryContentAndPresetMessage(
                            character.getInventoryCache().toObjectsItem(),
                            character.getKamas(),
                            character.getPresets().getValues(character)
                                    //.filter(pr -> Arrays.stream(pr.objects).allMatch(i -> client.getCharacter().getInventoryCache().contains(i.objUid) ))
                                    .toArray(Preset[]::new)
                    ));
                } else
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


                //

                client.getAccount().last_login = new Timestamp(System.currentTimeMillis());
                client.getAccount().lastIP = client.getIP();

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    public static void sendCharacterStatsListMessage(WorldClient client, boolean fight) {

        client.send(new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) client.getCharacter().getExperience(), (double) DAO.getExps().getPlayerMinExp(client.getCharacter().getLevel()), (double) DAO.getExps().getPlayerMaxExp(client.getCharacter().getLevel()), client.getCharacter().getKamas(), client.getCharacter().getStatPoints(), 0, client.getCharacter().getSpellPoints(), client.getCharacter().getActorAlignmentExtendInformations(),
                client.getCharacter().getLife(), client.getCharacter().getMaxLife(), client.getCharacter().getEnergy(), PlayerEnum.MAX_ENERGY,
                (short) client.getCharacter().getStats().getTotal(StatsEnum.ACTION_POINTS, fight), (short) client.getCharacter().getStats().getTotal(StatsEnum.MOVEMENT_POINTS, fight),
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


    private static final byte REGEN_RATE = 10;

    //private final static ScheduledExecutorService backgroundWorker = Executors.newScheduledThreadPool(30);

    @HandlerAttribute(ID = CharacterCreationRequestMessage.MESSAGE_ID)
    public static void handleCharacterCreationRequestMessage(WorldClient client, CharacterCreationRequestMessage message) {
        try {
            if(client.getCharacter() != null){
                return;
            }
            if (client.getAccount().characters.size() >= PlayerDAOImpl.MAX_CHARACTER_SLOT) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_TOO_MANY_CHARACTERS.value()));
            } else if (!PlayerController.isValidName((message.name))) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));

            } else if (DAO.getPlayers().containsName((message.name))) {
                client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NAME_ALREADY_EXISTS.value()));
            } else {
                final Breed breedTemplate = DAO.getD2oTemplates().getBreed((message.breed));
                if (breedTemplate == null) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NOT_ALLOWED.value()));
                    return;
                }
                final Head head = DAO.getD2oTemplates().getHead(message.cosmeticId);
                if (head == null || head.breedtype != breedTemplate.id || head.gendertype == 1 != message.sex) {
                    client.send(new CharacterCreationResultMessage(CharacterCreationResultEnum.ERR_NO_REASON.value()));
                    return;
                }
                final Player character = Player.builder()
                        .nickName(message.name)
                        .regenRate(REGEN_RATE)
                        .breed(message.breed)
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
                        .level(DAO.getSettings().getByteElement("Register.StartLevel"))
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
                        .additionalStats(new ConcurrentHashMap<>(6))
                        .ornaments(new int[0])
                        .titles(new int[0])
                        .kolizeumRate(Glicko2Player.defaultValue())
                        .fighterLook(new Object())
                        .moodSmiley((byte) -1)
                        .fighterLook(new Object())
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

                final PlayerCreatedMessage interMessage = new PlayerCreatedMessage(client.getAccount().characters.size(), client.getAccount().id);

                Main.getInterClient().send(interMessage);
                //backgroundWorker.schedule(() -> Main.getInterClient().send(interMessage),10, TimeUnit.MINUTES);

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
