package koh.game.network.handlers.character;

import com.google.common.primitives.Bytes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.ExpDAO;
import koh.game.dao.ItemDAO;
import koh.game.dao.MapDAO;
import koh.game.dao.PaddockDAO;
import koh.game.dao.PlayerDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.MapPosition;
import koh.game.entities.environments.SubArea;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.network.ChatChannel;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.utils.Settings;
import koh.look.EntityLookParser;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_CHANGE_LOOK;
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
import koh.protocol.messages.game.actions.fight.GameActionFightChangeLookMessage;
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
import koh.protocol.messages.game.context.GameContextRefreshEntityLookMessage;
import koh.protocol.messages.game.context.mount.GameDataPaddockObjectAddMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.messages.game.tinsel.OrnamentGainedMessage;
import koh.protocol.messages.messages.game.tinsel.TitleGainedMessage;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.SetCharacterRestrictionsMessage;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.look.SubEntity;
import koh.protocol.types.game.mount.ItemDurability;
import koh.protocol.types.game.paddock.PaddockItem;
import koh.utils.Enumerable;
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

            Client.Send(new BasicNoOperationMessage());
        } else if (message.enable && !Client.Character.EnnabledChannels.contains(message.channel)) {
            Client.Character.EnnabledChannels.add(message.channel);
            Client.Character.DisabledChannels.remove(Client.Character.DisabledChannels.indexOf(message.channel));
        } else if (!message.enable && !Client.Character.DisabledChannels.contains(message.channel)) {
            Client.Character.DisabledChannels.add(message.channel);
            Client.Character.EnnabledChannels.remove(Client.Character.EnnabledChannels.indexOf(message.channel));
        } else {
            if (Settings.GetBoolElement("Logging.Debug")) {
                System.out.println("Eroror " + message.enable + " " + message.channel);
                for (byte b : Client.Character.EnnabledChannels) {
                    System.out.println("enabled " + b);
                }
                for (byte b : Client.Character.DisabledChannels) {
                    System.out.println("Disabled " + b);
                }
            }
            /*Client.sendPacket(new BasicNoOperationMessage());
             return;*/
        }

        Client.Send(new EnabledChannelsMessage(Client.Character.EnnabledChannels, Client.Character.DisabledChannels));
    }

    @HandlerAttribute(ID = 852)
    public static void HandleChatClientPrivateWithObjectMessage(WorldClient Client, ChatClientPrivateWithObjectMessage Message) {
        if (Message.objects == null) {
            HandleChatClientPrivateMessage(Client, Message);
            return;
        }
        Player Target = PlayerDAO.GetCharacter(Message.Receiver);
        if (Target == null || Target.Client == null) {
            Client.Send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            Client.Send(new ChatServerCopyWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", Target.ID, Target.NickName, Message.objects));
            Target.Send(new ChatServerWithObjectMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
        }
    }

    @HandlerAttribute(ID = ChatClientPrivateMessage.MESSAGE_ID)
    public static void HandleChatClientPrivateMessage(WorldClient Client, ChatClientPrivateMessage Message) {
        Player Target = PlayerDAO.GetCharacter(Message.Receiver);
        if (Target == null || Target.Client == null) {
            Client.Send(new ChatErrorMessage(ChatErrorEnum.CHAT_ERROR_RECEIVER_NOT_FOUND));
        } else {
            Client.Send(new ChatServerCopyMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", Target.ID, Target.NickName));
            Target.Send(new ChatServerMessage(ChatActivableChannelsEnum.PSEUDO_CHANNEL_PRIVATE, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
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
                if (Client.Character.GetFighter() != null) {
                    Client.Character.GetFighter().Team.sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                }
                break;
            case CHANNEL_GLOBAL:
                if (Client.Character.GetFighter() != null) {
                    Client.Character.GetFight().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                } else {
                    Client.Character.CurrentMap.sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                }
                break;
            case CHANNEL_ADMIN:
                ChatChannel.Channels.get(Message.channel).sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));

                break;
            case CHANNEL_GUILD:
                if (Client.Character.Guild == null) {
                    PlayerController.SendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                Client.Character.Guild.sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                break;
            case CHANNEL_PARTY:
                if (Client.GetParty() == null) {
                    PlayerController.SendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                Client.GetParty().sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (Client.LastChannelMessage.get(Message.channel) + 60000L > System.currentTimeMillis()) {
                    Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.LastChannelMessage.get(Message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.Channels.get(Message.channel).sendToField(new ChatServerWithObjectMessage(Message.channel, Message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID, Message.objects));
                Client.LastChannelMessage.put(Message.channel, System.currentTimeMillis());
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
                if (Client.Character.GetFighter() != null) {
                    Client.Character.GetFighter().Team.sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                }
                break;
            case CHANNEL_GLOBAL:
                if (Client.Character.GetFighter() != null) {
                    Client.Character.GetFight().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                } else {
                    Client.Character.CurrentMap.sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));

                }
                break;
            case CHANNEL_ADMIN:
                if (message.Content.startsWith("!teleport")) {
                    try {
                        int mapid = Integer.parseInt(message.Content.split(" ")[1]);
                        int cellid = message.Content.split(" ").length < 2 ? -1 : Integer.parseInt(message.Content.split(" ")[2]);

                        Client.Character.teleport(mapid, cellid);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;//RemovePaddockItem
                } else if (message.Content.startsWith("!remove_item")) {
                    try {
                        if (!PaddockDAO.Cache.containsKey(Client.Character.CurrentMap.Id)) {
                            break;
                        }
                        int cellid = Integer.parseInt(message.Content.split(" ")[1]);
                        PaddockDAO.Cache.get(Client.Character.CurrentMap.Id).RemovePaddockItem(cellid);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (message.Content.startsWith("!alterte")) {
                    byte duration = Byte.parseByte(message.Content.split(" ")[1]);
                    Main.WorldServer().SendPacket(new PopupWarningMessage(duration, Client.Character.NickName, message.Content.split(" ")[2]));
                    break;
                } else if (message.Content.startsWith("!item_paddock")) {
                    try {
                        if (!PaddockDAO.Cache.containsKey(Client.Character.CurrentMap.Id)) {
                            break;
                        }
                        int cellid = Integer.parseInt(message.Content.split(" ")[1]);
                        int object = Integer.parseInt(message.Content.split(" ")[2]);
                        short durability = Short.parseShort(message.Content.split(" ")[3]);
                        short durabilityMax = Short.parseShort(message.Content.split(" ")[4]);
                        PaddockDAO.Cache.get(Client.Character.CurrentMap.Id).AddPaddockItem(new PaddockItem(cellid, object, new ItemDurability(durability, durabilityMax)));

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

                        MapPosition[] SubAreas = MapDAO.GetSubAreaOfPos(X, Y);
                        if (SubAreas.length > 1 && subArea == -1) {
                            PlayerController.SendServerMessage(Client, "This position contains a lots of SubArea so try one of this ..");
                            for (MapPosition s : SubAreas) {
                                PlayerController.SendServerMessage(Client, "!go " + X + " " + Y + " " + s.subAreaId +" (Or choose Mapid "+s.id);
                            }
                        } else {
                            if (subArea == -1) {
                                subArea = SubAreas[0].subAreaId;
                            }
                            DofusMap Map = MapDAO.GetMapByPos(X, Y, subArea);
                            if (Map == null) {
                                PlayerController.SendServerMessage(Client, "Map Inconnue");
                                break;
                            }

                            Client.Character.teleport(Map.Id, Map.GetAnyCellWalakable().Id); //Todo Random Walakable Cell
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (message.Content.startsWith("!save")) {
                    Client.Character.Save(false);
                } else if (message.Content.startsWith("!bones")) {
                    short Id = Short.parseShort(message.Content.split(" ")[1]);
                    //Client.GetFighter().entityLook.bonesId = Id;
                    //Client.GetFight().sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, Client.GetFighter().ID, Client.GetFighter().ID, Client.GetFighter().GetEntityLook()));
                    Client.Character.GetEntityLook().bonesId = Id;
                    Client.Character.RefreshEntitie();
                } else if (message.Content.startsWith("!clearitems")) {
                    Client.Character.CurrentMap.ClearDroppedItems();
                } else if (message.Content.startsWith("!level")) {
                    int Level = Integer.parseInt(message.Content.split(" ")[1]);
                    Player Target = message.Content.length() >= 2 ? Client.Character : PlayerDAO.GetCharacter(message.Content.split(" ")[2]);
                    if (Target == null || !Target.IsInWorld) {
                        PlayerController.SendServerMessage(Client, "Player " + message.Content.split(" ")[2] + " Offline");
                        return;
                    }
                    Target.AddExperience((ExpDAO.PersoXpMin(Level) + 1) - Target.Experience);
                    PlayerController.SendServerMessage(Client, "Level seted successfully");
                } else if (message.Content.startsWith("!ange")) {
                    Client.Character.ChangeAlignementSide(AlignmentSideEnum.ALIGNMENT_ANGEL);
                } else if (message.Content.startsWith("!demon")) {
                    Client.Character.ChangeAlignementSide(AlignmentSideEnum.ALIGNMENT_EVIL);
                }else if (message.Content.startsWith("!dit")) {
                    Client.Send(new ChatServerMessage(CHANNEL_GLOBAL, message.Content, (int) Instant.now().getEpochSecond(), "az", -1, "Pnj de Merde", Client.getAccount().ID));
                }
                else if (message.Content.startsWith("!set")) {
                    //Client.Send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(), 5));
                    Client.Send(new SetCharacterRestrictionsMessage(new ActorRestrictionsInformations(new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean()), Client.Character.ID));

                } else if (message.Content.startsWith("!kmar2")) {
                    int X = Integer.parseInt(message.Content.split(" ")[1]);
                    Client.Character.GetEntityLook().subentities.clear();
                    Client.Character.GetEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET, 0, new EntityLook((short) X, new ArrayList<>(), new ArrayList<>(), new ArrayList<Short>() {
                        {
                            this.add((short) 80);
                        }
                    }, new ArrayList<>())));
                    Client.Character.RefreshEntitie();
                } else if (message.Content.startsWith("!learnspell")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    Client.Character.mySpells.AddSpell(X, (byte) 1, Client.Character.mySpells.getFreeSlot(), Client);
                } else if (message.Content.startsWith("!kmar")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    Client.Character.GetEntityLook().skins.clear();
                    Client.Character.GetEntityLook().skins.addAll(Client.Character.Skins);
                    Client.Character.GetEntityLook().skins.add(X);
                    Client.Character.RefreshEntitie();
                } else if (message.Content.startsWith("!title")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    if (ArrayUtils.contains(Client.Character.Titles, X)) {
                        PlayerController.SendServerMessage(Client, "Erreur : vous le possez déjà .");
                        return;
                    }
                    Client.Character.Titles = ArrayUtils.add(Client.Character.Titles, X);
                    Client.Send(new TitleGainedMessage(X));
                } else if (message.Content.startsWith("!ornament")) {
                    short X = Short.parseShort(message.Content.split(" ")[1]);
                    if (ArrayUtils.contains(Client.Character.Ornaments, X)) {
                        PlayerController.SendServerMessage(Client, "Erreur : vous le possez déjà .");
                        return;
                    }
                    Client.Character.Ornaments = ArrayUtils.add(Client.Character.Ornaments, X);
                    Client.Send(new OrnamentGainedMessage(X));
                } else if (message.Content.startsWith("!entity")) {
                    /*test  EntityLook en = new EntityLook((short)3,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>() {{ add(new SubEntity(17,10, new EntityLook((short)3,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>()))); 
                     add(new SubEntity(177,110, new EntityLook((short)7,  new ArrayList<Short>() { { add((short)1); add((short) 3); }},new ArrayList<Integer>() { { add(14); add(47); }},new ArrayList<Short>() { { add((short)8); add((short) 9); }},new ArrayList<SubEntity>()))); 
                     add(new SubEntity(175876,175770, new EntityLook((short)778,  new ArrayList<Short>() { { add((short)81); add((short) 35); }},new ArrayList<Integer>() { { add(1574); add(547); }},new ArrayList<Short>() { { add((short)857); add((short) 579); }},new ArrayList<SubEntity>()))); }});
                     System.out.println(en.toString());
                     System.out.println(EntityLookParser.ConvertToString(en));
                     System.out.println(EntityLookParser.fromString(EntityLookParser.ConvertToString(en)).toString());
                     */
                    System.out.println(Client.Character.GetEntityLook().toString());
                    System.out.println(EntityLookParser.ConvertToString(Client.Character.GetEntityLook()));
                    System.out.println(EntityLookParser.fromString(EntityLookParser.ConvertToString(Client.Character.GetEntityLook())).toString());
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
                        if (ItemDAO.Cache.get(Id) == null) {
                            PlayerController.SendServerMessage(Client, "Inexistant Item");
                            return;
                        }
                        if (ItemDAO.Cache.get(Id).GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                            Qua = 1;
                        }
                        InventoryItem Item = InventoryItem.Instance(ItemDAO.NextID++, Id, 63, Client.Character.ID, Qua, EffectHelper.GenerateIntegerEffect(ItemDAO.Cache.get(Id).possibleEffects, Type, ItemDAO.Cache.get(Id) instanceof Weapon));

                        if (Client.Character.InventoryCache.Add(Item, true)) {
                            Item.NeedInsert = true;
                        }
                        PlayerController.SendServerMessage(Client, String.format("%s  added to your inventory with %s stats", ItemDAO.Cache.get(Id).nameId, Type.toString()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    ChatChannel.Channels.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                }
                break;
            case CHANNEL_GUILD:
                if (Client.Character.Guild == null) {
                    PlayerController.SendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'une guilde.");
                    return;
                }
                Client.Character.Guild.sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                break;
            case CHANNEL_PARTY:
                if (Client.GetParty() == null) {
                    PlayerController.SendServerMessage(Client, "Erreur : Vous ne faîtes pas partie d'un groupe.");
                    return;
                }
                Client.GetParty().sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                break;
            case CHANNEL_SEEK:
            case CHANNEL_SALES:
            case CHANNEL_NOOB:
            case CHANNEL_ADS:
                if (Client.LastChannelMessage.get(message.channel) + 60000L > System.currentTimeMillis()) {
                    Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 115, new String[]{((Client.LastChannelMessage.get(message.channel) + 60000L - System.currentTimeMillis()) / 1000) + ""}));
                    return;
                }
                ChatChannel.Channels.get(message.channel).sendToField(new ChatServerMessage(message.channel, message.Content, (int) Instant.now().getEpochSecond(), "az", Client.Character.ID, Client.Character.NickName, Client.getAccount().ID));
                Client.LastChannelMessage.put(message.channel, System.currentTimeMillis());
                break;
            default:
                return;
        }

    }

}
