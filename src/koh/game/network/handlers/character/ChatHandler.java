package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.MapPosition;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.item.Weapon;
import koh.game.network.ChatChannel;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.ChatActivableChannelsEnum;
import koh.protocol.messages.game.chat.ChatClientMultiMessage;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.*;
import koh.protocol.client.enums.ChatErrorEnum;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.client.enums.SubEntityBindingPointCategoryEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.chat.ChannelEnablingMessage;
import koh.protocol.messages.game.chat.ChatClientMultiWithObjectMessage;
import koh.protocol.messages.game.chat.ChatClientPrivateMessage;
import koh.protocol.messages.game.chat.ChatClientPrivateWithObjectMessage;
import koh.protocol.messages.game.chat.ChatErrorMessage;
import koh.protocol.messages.game.chat.ChatServerCopyMessage;
import koh.protocol.messages.game.chat.ChatServerCopyWithObjectMessage;
import koh.protocol.messages.game.chat.ChatServerMessage;
import koh.protocol.messages.game.chat.ChatServerWithObjectMessage;
import koh.protocol.messages.game.chat.EnabledChannelsMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.messages.game.tinsel.OrnamentGainedMessage;
import koh.protocol.messages.messages.game.tinsel.TitleGainedMessage;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.SetCharacterRestrictionsMessage;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.look.SubEntity;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.PaddockItem;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class ChatHandler {

    @HandlerAttribute(ID = ChannelEnablingMessage.MESSAGE_ID)
    public static void HandleChannelEnablingMessage(WorldClient Client, ChannelEnablingMessage message) {
        //System.out.println(message.channel + "" + message.enable);
        if (message.channel < CHANNEL_GLOBAL || message.channel > CHANNEL_ARENA) {

            Client.send(new BasicNoOperationMessage());
        } else if (message.enable && !Client.getCharacter().getEnabledChannels().contains(message.channel)) {
            Client.getCharacter().getEnabledChannels().add(message.channel);
            Client.getCharacter().getDisabledChannels().remove(Client.getCharacter().getDisabledChannels().indexOf(message.channel));
        } else if (!message.enable && !Client.getCharacter().getDisabledChannels().contains(message.channel)) {
            Client.getCharacter().getDisabledChannels().add(message.channel);
            Client.getCharacter().getEnabledChannels().remove(Client.getCharacter().getEnabledChannels().indexOf(message.channel));
        } else {
            //TODO: Fix this stupid flood message
            if (DAO.getSettings().getBoolElement("Logging.Debug")) {
                System.out.println("Eroror " + message.enable + " " + message.channel);
                for (byte b : Client.getCharacter().getEnabledChannels()) {
                    System.out.println("enabled " + b);
                }
                for (byte b : Client.getCharacter().getDisabledChannels()) {
                    System.out.println("Disabled " + b);
                }
            }
            /*client.sendPacket(new BasicNoOperationMessage());
             return;*/
        }

        Client.send(new EnabledChannelsMessage(Client.getCharacter().getEnabledChannels(), Client.getCharacter().getDisabledChannels()));
    }

    @HandlerAttribute(ID = 852)
    public static void HandleChatClientPrivateWithObjectMessage(WorldClient client, ChatClientPrivateWithObjectMessage Message) {
        if (Message.objects == null) {
            HandleChatClientPrivateMessage(client, Message);
            return;
        }
        Player target = DAO.getPlayers().getCharacter(Message.Receiver);
        if (target == null || target.getClient() == null) {
            client.send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            client.send(new ChatServerCopyWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", target.getID(), target.getNickName(), Message.objects));
            target.send(new ChatServerWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id, Message.objects));
        }
    }

    @HandlerAttribute(ID = ChatClientPrivateMessage.MESSAGE_ID)
    public static void HandleChatClientPrivateMessage(WorldClient client, ChatClientPrivateMessage Message) {
        Player target = DAO.getPlayers().getCharacter(Message.Receiver);
        if (target == null || target.getClient() == null) {
            client.send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            client.send(new ChatServerCopyMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", target.getID(), target.getNickName()));
            target.send(new ChatServerMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", client.getCharacter().getID(), client.getCharacter().getNickName(), client.getAccount().id));
        }
    }

    @HandlerAttribute(ID = 862)
    public static void HandleChatClientMultiWithObjectMessage(WorldClient Client, ChatClientMultiWithObjectMessage Message) {
        if (Message.objects == null) {
            HandleChatClientMultiMessage(Client, Message);
            return;
        }
        switch (Message.channel) {
            case CHANNEL_TEAM:
                if (Client.getCharacter().getFighter() != null) {
                    Client.getCharacter().getFighter().getTeam().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                }
                break;
            case CHANNEL_GLOBAL:
                if (Client.getCharacter().getFighter() != null) {
                    Client.getCharacter().getFight().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                } else {
                    Client.getCharacter().getCurrentMap().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                }
                break;
            case CHANNEL_ADMIN:
                ChatChannel.CHANNELS.get(Message.channel).sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));

                break;
            case CHANNEL_GUILD:
                if (Client.getCharacter().getGuild() == null) {
                    PlayerController.sendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                Client.getCharacter().getGuild().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                break;
            case CHANNEL_PARTY:
                if (Client.getParty() == null) {
                    PlayerController.sendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                Client.getParty().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (Client.getLastChannelMessage().get(Message.channel) + 60000L > System.currentTimeMillis()) {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.getLastChannelMessage().get(Message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.CHANNELS.get(Message.channel).sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id, Message.objects));
                Client.getLastChannelMessage().put(Message.channel, System.currentTimeMillis());
                break;
            default:
                return;
        }
    }

    @HandlerAttribute(ID = ChatClientMultiMessage.MESSAGE_ID)
    public static void HandleChatClientMultiMessage(WorldClient Client, ChatClientMultiMessage message) {
        //TODO : Filter message ?
        switch (message.channel) {
            case CHANNEL_TEAM:
                if (Client.getCharacter().getFighter() != null) {
                    Client.getCharacter().getFighter().getTeam().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                }
                break;
            case CHANNEL_GLOBAL:
                if (Client.getCharacter().getFighter() != null) {
                    Client.getCharacter().getFight().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                } else {
                    Client.getCharacter().getCurrentMap().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));

                }
                break;
            case CHANNEL_ADMIN:
                if (message.Content.startsWith("!teleport")) {
                    try {
                        int mapid = Integer.parseInt(message.Content.split(" ")[1]);
                        int cellid = message.Content.split(" ").length < 2 ? -1 : Integer.parseInt(message.Content.split(" ")[2]);

                        Client.getCharacter().teleport(mapid, cellid);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;//removePaddockItem
                } else if (message.Content.startsWith("!remove_item")) {
                    try {
                        /* check map null if (!PaddockDAOImpl.paddocks.containsKey(Client.getCharacter().currentMap.id)) {
                            break;
                        }*/
                        int cellid = Integer.parseInt(message.Content.split(" ")[1]);
                        DAO.getPaddocks().find(Client.getCharacter().getCurrentMap().getId()).removePaddockItem(cellid);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (message.Content.startsWith("!alterte")) {
                    byte duration = Byte.parseByte(message.Content.split(" ")[1]);
                    Main.worldServer().SendPacket(new PopupWarningMessage(duration, Client.getCharacter().getNickName(), message.Content.split(" ")[2]));
                    break;
                } else if (message.Content.startsWith("!item_paddock")) {
                    try {
                        /* check map null if (!PaddockDAOImpl.paddocks.containsKey(Client.getCharacter().currentMap.id)) {
                            break;
                        } */
                        int cellid = Integer.parseInt(message.Content.split(" ")[1]);
                        int object = Integer.parseInt(message.Content.split(" ")[2]);
                        short durability = Short.parseShort(message.Content.split(" ")[3]);
                        short durabilityMax = Short.parseShort(message.Content.split(" ")[4]);
                        DAO.getPaddocks().find(Client.getCharacter().getCurrentMap().getId()).addPaddockItem(new PaddockItem(cellid, object, new ItemDurability(durability, durabilityMax)));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (message.Content.startsWith("!go")) {
                    try {
                        int X = Integer.parseInt(message.Content.split(" ")[1]);
                        int Y = Integer.parseInt(message.Content.split(" ")[2]);
                        int subArea = -1;
                        try {
                            subArea = Integer.parseInt(message.Content.split(" ")[3]);
                        } catch (Exception e) {

                        }

                        MapPosition[] SubAreas = DAO.getMaps().getSubAreaOfPos(X, Y);
                        if (SubAreas.length > 1 && subArea == -1) {
                            PlayerController.sendServerMessage(Client, "This position contains a lots of subArea so try one of this ..");
                            for (MapPosition s : SubAreas) {
                                PlayerController.sendServerMessage(Client, "!go " + X + " " + Y + " " + s.getSubAreaId() + " (Or choose mapid " + s.getId());
                            }
                        } else {
                            if (subArea == -1) {
                                subArea = SubAreas[0].getSubAreaId();
                            }
                            DofusMap Map = DAO.getMaps().findMapByPos(X, Y, subArea);
                            if (Map == null) {
                                PlayerController.sendServerMessage(Client, "map Inconnue");
                                break;
                            }

                            Client.getCharacter().teleport(Map.getId(), Map.getAnyCellWalakable().getId()); //Todo random walakable cell
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (message.Content.startsWith("!save")) {
                    Client.getCharacter().save(false);
                }
                else if (message.Content.startsWith("!kamas")) {
                    short Id = Short.parseShort(message.Content.split(" ")[1]);
                    Client.getCharacter().addKamas(Id);
                }
                else if (message.Content.startsWith("!bones")) {
                    short Id = Short.parseShort(message.Content.split(" ")[1]);
                    //client.getFighter().entityLook.bonesId = id;
                    //client.getFight().sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, client.getFighter().id, client.getFighter().id, client.getFighter().getEntityLook()));
                    Client.getCharacter().getEntityLook().bonesId = Id;
                    Client.getCharacter().refreshEntitie();
                } else if (message.Content.startsWith("!clearitems")) {
                    Client.getCharacter().getCurrentMap().clearDroppedItems();
                } else if (message.Content.startsWith("!setpdvper")) {
                    int level = Integer.parseInt(message.Content.split(" ")[1]);
                    if (Client.getCharacter().getFight() != null || level > 100) {
                        return;
                    }
                    Client.getCharacter().setLife((int) (((float) Client.getCharacter().getMaxLife() * 100) / level));
                    PlayerController.sendServerMessage(Client, "PdV Updated");
                    Client.getCharacter().refreshStats();
                } else if (message.Content.startsWith("!spellpoint")) {
                    int Level = Integer.parseInt(message.Content.split(" ")[1]);
                    Client.getCharacter().setSpellPoints(Client.getCharacter().getSpellPoints()  +Level);
                    PlayerController.sendServerMessage(Client, "spellPoints added");
                    Client.getCharacter().refreshStats();
                    return;
                } else if (message.Content.startsWith("!level")) {
                    int Level = Integer.parseInt(message.Content.split(" ")[1]);
                    Player Target = message.Content.length() >= 2 ? Client.getCharacter() : DAO.getPlayers().getCharacter(message.Content.split(" ")[2]);
                    if (Target == null || !Target.isInWorld()) {
                        PlayerController.sendServerMessage(Client, "player " + message.Content.split(" ")[2] + " Offline");
                        return;
                    }
                    Target.addExperience((DAO.getExps().getPlayerMinExp(Level) + 1) - Target.getExperience());
                    PlayerController.sendServerMessage(Client, "level seted successfully");
                } else if (message.Content.startsWith("!ange")) {
                    Client.getCharacter().changeAlignementSide(AlignmentSideEnum.ALIGNMENT_ANGEL);
                } else if (message.Content.startsWith("!demon")) {
                    Client.getCharacter().changeAlignementSide(AlignmentSideEnum.ALIGNMENT_EVIL);
                } else if (message.Content.startsWith("!dit")) {
                    Client.send(new ChatServerMessage(CHANNEL_GLOBAL, message.Content, (int) Instant.now().getEpochSecond(), "az", -1, "Pnj de Merde", Client.getAccount().id));
                } else if (message.Content.startsWith("!set")) {
                    //client.send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(), 5));
                    Client.send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean()), Client.getCharacter().getID()));

                } else if (message.Content.startsWith("!kmar2")) {
                    int X = Integer.parseInt(message.Content.split(" ")[1]);
                    Client.getCharacter().getEntityLook().subentities.clear();
                    Client.getCharacter().getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET, 0, new EntityLook((short) X, new ArrayList<>(), new ArrayList<>(), new ArrayList<Short>() {
                        {
                            this.add((short) 80);
                        }
                    }, new ArrayList<>())));
                    Client.getCharacter().refreshEntitie();
                } else if (message.Content.startsWith("!learnspell")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    Client.getCharacter().getMySpells().addSpell(X, (byte) 1, Client.getCharacter().getMySpells().getFreeSlot(), Client);
                } else if (message.Content.startsWith("!kmar")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    Client.getCharacter().getEntityLook().skins.clear();
                    Client.getCharacter().getEntityLook().skins.addAll(Client.getCharacter().getSkins());
                    Client.getCharacter().getEntityLook().skins.add(X);
                    Client.getCharacter().refreshEntitie();
                } else if (message.Content.startsWith("!title")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    if (ArrayUtils.contains(Client.getCharacter().getTitles(), X)) {
                        PlayerController.sendServerMessage(Client, "Erreur : vous le possez déjà .");
                        return;
                    }
                    Client.getCharacter().setTitles(ArrayUtils.add(Client.getCharacter().getTitles(), X));
                    Client.send(new TitleGainedMessage(X));
                } else if (message.Content.startsWith("!ornament")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    if (ArrayUtils.contains(Client.getCharacter().getOrnaments(), X)) {
                        PlayerController.sendServerMessage(Client, "Erreur : vous le possez déjà .");
                        return;
                    }
                    Client.getCharacter().setOrnaments( ArrayUtils.add(Client.getCharacter().getOrnaments(), X));
                    Client.send(new OrnamentGainedMessage(X));
                } else if (message.Content.startsWith("!entity")) {
                    /*test  EntityLook en = new EntityLook((short)3,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>() {{ add(new SubEntity(17,10, new EntityLook((short)3,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>()))); 
                     add(new SubEntity(177,110, new EntityLook((short)7,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>()))); 
                     add(new SubEntity(175876,175770, new EntityLook((short)778,  new ArrayList<Short>() { { add((short)81); add((short) 35); }},new ArrayList<Integer>() { { add(1574); add(547); }},new ArrayList<Short>() { { add((short)857); add((short) 579); }},new ArrayList<SubEntity>()))); }});
                     System.out.println(en.toString());
                     System.out.println(EntityLookParser.ConvertToString(en));
                     System.out.println(EntityLookParser.fromString(EntityLookParser.ConvertToString(en)).toString());
                     */
                    System.out.println(Client.getCharacter().getEntityLook().toString());
                    System.out.println(EntityLookParser.ConvertToString(Client.getCharacter().getEntityLook()));
                    System.out.println(EntityLookParser.fromString(EntityLookParser.ConvertToString(Client.getCharacter().getEntityLook())).toString());
                } else if (message.Content.startsWith("!item")) {
                    try {
                        int Id = Integer.parseInt(message.Content.split(" ")[1]);
                        int Qua = Integer.parseInt(message.Content.split(" ")[2]);
                        EffectGenerationType Type;
                        switch (message.Content.split(" ")[3]) {
                            case "max":
                                Type = EffectGenerationType.MaxEffects;
                                break;
                            case "min":
                                Type = EffectGenerationType.MinEffects;
                                break;
                            default:
                                Type = EffectGenerationType.Normal;
                                break;
                        }
                        ItemTemplate template = DAO.getItemTemplates().getTemplate(Id);
                        if (template == null) {
                            PlayerController.sendServerMessage(Client, "Inexistant item");
                            return;
                        }
                        if (template.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                            Qua = 1;
                        }
                        InventoryItem Item = InventoryItem.getInstance(DAO.getItems().nextItemId(), Id, 63, Client.getCharacter().getID(), Qua, EffectHelper.generateIntegerEffect(template.getPossibleEffects(), Type, template instanceof Weapon));

                        if (Client.getCharacter().getInventoryCache().add(Item, true)) {
                            Item.setNeedInsert(true);
                        }
                        PlayerController.sendServerMessage(Client, String.format("%s  added to your inventory with %s stats", template.getNameId(), Type.toString()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                }
                break;
            case CHANNEL_GUILD:
                if (Client.getCharacter().getGuild() == null) {
                    PlayerController.sendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                Client.getCharacter().getGuild().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                break;
            case CHANNEL_PARTY:
                if (Client.getParty() == null) {
                    PlayerController.sendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                Client.getParty().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (Client.getLastChannelMessage().get(message.channel) + 60000L > System.currentTimeMillis()) {
                    Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.getLastChannelMessage().get(message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.CHANNELS.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.getCharacter().getID(), Client.getCharacter().getNickName(), Client.getAccount().id));
                Client.getLastChannelMessage().put(message.channel, System.currentTimeMillis());
                break;
            default:
                return;
        }

    }

}
