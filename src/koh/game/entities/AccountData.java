package koh.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import koh.game.dao.mysql.AccountDataDAOImpl;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.dao.mysql.PlayerDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.protocol.client.BufUtils;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.PlayerStateEnum;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.inventory.storage.StorageObjectRemoveMessage;
import koh.protocol.messages.game.inventory.storage.StorageObjectUpdateMessage;
import koh.protocol.types.game.friend.FriendInformations;
import koh.protocol.types.game.context.roleplay.BasicGuildInformations;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.ObjectItem;
import koh.protocol.types.game.friend.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class AccountData {

    public int Id;
    public int Kamas;
    public FriendContact[] Friends;
    public IgnoredContact[] Ignored;
    public IgnoredContact Spouse;
    public boolean friend_warn_on_login;
    public boolean friend_warn_on_level_gain;
    public boolean guild_warn_on_login;
    public Map<Integer, InventoryItem> ItemsCache = Collections.synchronizedMap(new HashMap<>());

    public List<String> ColumsToUpdate = null;

    public List<ObjectItem> toObjectsItem() {
        return ItemsCache.values().stream().map(InventoryItem::ObjectItem).collect(Collectors.toList());
    }

    public void UpdateObjectquantity(Player Player, InventoryItem Item, int Quantity) {
        Item.SetQuantity(Quantity);
        if (Item.GetQuantity() <= 0) {
            this.RemoveItem(Player, Item);
            return;
        }
        Player.Send(new StorageObjectUpdateMessage(Item.ObjectItem()));
    }

    public void RemoveItem(Player Player, InventoryItem Item) {
        Item.NeedInsert = false;
        Item.ColumsToUpdate = null;
        this.RemoveFromDic(Item.ID);
        Player.Send(new StorageObjectRemoveMessage(Item.ID));
        ItemTemplateDAOImpl.Remove(Item, "storage_items");
    }

    public void SetBankKamas(int Price) {
        this.Kamas = Price;
        this.NotifiedColumn("kamas");
    }

    public void RemoveFromDic(int id) {
        ItemsCache.remove(id);
    }

    public void setFriendWarnOnGuildLogin(boolean b) {
        this.guild_warn_on_login = b;
        this.NotifiedColumn("friend_warn_on_login");
    }

    public void setFriendWarnOnLevelGain(boolean b) {
        this.friend_warn_on_level_gain = b;
        this.NotifiedColumn("friend_warn_on_level_gain");
    }

    public boolean Add(Player Player, InventoryItem Item, boolean merge) //muste be true
    {
        if (merge && TryMergeItem(Player, Item.TemplateId, Item.Effects, Item.Slot(), Item.GetQuantity(), Item, false)) {
            return false;
        }
        if (Item.GetOwner() != Id) {
            Item.SetOwner(Id);
        }
        if (ItemsCache.containsKey(Item.ID)) {
            RemoveFromDic(Item.ID);
        }
        ItemsCache.put(Item.ID, Item);

        Player.Send(new StorageObjectUpdateMessage(Item.ObjectItem()));

        return true;
    }

    public boolean TryMergeItem(Player Player, int TemplateId, List<ObjectEffect> Stats, CharacterInventoryPositionEnum Slot, int Quantity, InventoryItem RemoveItem, boolean Send) {
        if (Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            for (InventoryItem Item : this.ItemsCache.values()) {
                if (Item.TemplateId == TemplateId && Item.Slot() == Slot && !(RemoveItem != null && RemoveItem.ID == Item.ID) && Item.Equals(Stats) ) {
                    if (RemoveItem != null) {
                        this.RemoveFromDic(RemoveItem.ID);
                        RemoveItem.NeedInsert = false;
                        ItemTemplateDAOImpl.Remove(RemoveItem, "storage_items");
                        RemoveItem.ColumsToUpdate = null;
                    }
                    this.UpdateObjectquantity(Player, Item, Item.GetQuantity() + Quantity);
                    return true;
                }
            }
        }

        return false;
    }

    public void setFriendWarnOnLogin(boolean b) {
        this.friend_warn_on_login = b;
        this.NotifiedColumn("friend_warn_on_login");
    }

    public void AddFriend(FriendContact Friend) {
        this.Friends = ArrayUtils.add(Friends, Friend);
        this.NotifiedColumn("friends");
    }

    public void AddIgnored(IgnoredContact Friend) {
        this.Ignored = ArrayUtils.add(Ignored, Friend);
        this.NotifiedColumn("ignored");
    }

    public FriendContact GetFriend(int account) {
        try {
            return Arrays.stream(Friends).filter(x -> x.AccountID == account).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public IgnoredContact GetIgnored(int account) {
        try {
            return Arrays.stream(Ignored).filter(x -> x.AccountID == account).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean HasFriend(int account) {
        return Arrays.stream(Friends).anyMatch(x -> x.AccountID == account);
    }

    public boolean Ignore(int account) {
        return Arrays.stream(Ignored).anyMatch(x -> x.AccountID == account);
    }

    public List<FriendInformations> GetFriendsInformations() {
        List<FriendInformations> Friends = new ArrayList<>();
        for (FriendContact Friend : this.Friends) {
            Player Target = PlayerDAO.GetCharacterByAccount(Friend.AccountID);
            if (Target == null || Target.Client == null) {
                Friends.add(new FriendInformations(Friend.AccountID, Friend.accountName, PlayerStateEnum.NOT_CONNECTED, (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Friend.lastConnection), Friend.achievementPoints));
            } else {
                Friend.achievementPoints = Target.achievementPoints;
                Friend.lastConnection = System.currentTimeMillis();
                this.NotifiedColumn("friends");
                if (Target.Account.Data != null && Target.Account.Data.HasFriend(this.Id)) {
                    Friends.add(new FriendOnlineInformations(Target.Account.ID, Target.Account.NickName, Target.GetPlayerState(), -1, Target.achievementPoints, Target.ID, Target.NickName, (byte) Target.Level, Target.AlignmentSide.value, Target.Breed, Target.Sexe == 1, Target.GetBasicGuildInformations(), Target.MoodSmiley, new PlayerStatus(Target.Status.value())));
                } else {
                    Friends.add(new FriendOnlineInformations(Target.Account.ID, Target.Account.NickName, Target.GetPlayerState(), -1, Target.achievementPoints, Target.ID, Target.NickName, (byte) 0, (byte) -1, Target.Breed, Target.Sexe == 1, new BasicGuildInformations(0, ""), (byte) -1, new PlayerStatus(Target.Status.value())));
                }
            }
        }
        return Friends;
    }

    public List<IgnoredInformations> GetIgnoredInformations() {
        List<IgnoredInformations> Friends = new ArrayList<>();
        for (IgnoredContact Friend : this.Ignored) {
            Player Target = PlayerDAO.GetCharacterByAccount(Friend.AccountID);
            if (Target == null || Target.Client == null) {
                Friends.add(new IgnoredInformations(Friend.AccountID, Friend.accountName));
            } else {
                Friends.add(new IgnoredOnlineInformations(Target.Account.ID, Target.Account.NickName, Target.ID, Target.NickName, Target.Breed, Target.Sexe == 1));
            }
        }
        return Friends;
    }

    public boolean RemoveIgnored(int ID) {
        IgnoredContact Person = null;
        try {
            Person = Arrays.stream(Ignored).filter(x -> x.AccountID == ID).findFirst().get();
        } catch (Exception e) {
            return false;
        }
        this.Ignored = ArrayUtils.removeElement(Ignored, Person);
        this.NotifiedColumn("ignored");
        return true;
    }

    public void RemoveIgnored(IgnoredContact Person) {
        this.Ignored = ArrayUtils.removeElement(Ignored, Person);
        this.NotifiedColumn("ignored");
    }

    public void RemoveFriend(FriendContact Friend) {
        this.Friends = ArrayUtils.removeElement(Friends, Friend);
        this.NotifiedColumn("friends");
    }

    public boolean RemoveFriend(int ID) {
        FriendContact Friend = null;
        try {
            Friend = Arrays.stream(Friends).filter(x -> x.AccountID == ID).findFirst().get();
        } catch (Exception e) {
            return false;
        }
        this.Friends = ArrayUtils.removeElement(Friends, Friend);
        this.NotifiedColumn("friends");
        return true;
    }

    public void Save(boolean Clear) {
        synchronized (ItemsCache) {
            this.ItemsCache.values().parallelStream().forEach(Item -> {
                if (Item.NeedRemove) {
                    ItemTemplateDAOImpl.Remove(Item, "storage_items");
                } else if (Item.NeedInsert) {
                    ItemTemplateDAOImpl.Insert(Item, Clear, "storage_items");
                } else if (Item.ColumsToUpdate != null && !Item.ColumsToUpdate.isEmpty()) {
                    ItemTemplateDAOImpl.Update(Item, Clear, "storage_items");
                } else if (Clear) {
                    Item.totalClear();
                }
            });
            if (!Clear && this.ColumsToUpdate != null) {
                AccountDataDAOImpl.Update(this, null);
            }
        }
    }

    public void totalClear(Account c) {
        try {
            this.Save(true);
            Id = 0;
            Kamas = 0;
            Friends = null;
            Ignored = null;
            friend_warn_on_login = false;
            friend_warn_on_level_gain = false;
            guild_warn_on_login = false;
            this.ItemsCache.clear();
            this.ItemsCache = null;
            c.continueClear();
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public byte[] SerializeFriends() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);
        buf.putInt(Friends.length);
        Arrays.stream(Friends).forEach(Contact -> Contact.Serialize(buf));

        return buf.array();
    }

    public byte[] SerializeIgnored() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);
        buf.putInt(Ignored.length);
        Arrays.stream(Ignored).forEach(Contact -> Contact.Serialize(buf));

        return buf.array();
    }

    public static class FriendContact {

        public int AccountID;
        public String accountName;
        public long lastConnection;
        public int achievementPoints;

        public void Serialize(IoBuffer buf) {
            buf.putInt(AccountID);
            BufUtils.writeUTF(buf, accountName);
            buf.putLong(lastConnection);
            buf.putInt(achievementPoints);
        }

        public static FriendContact[] Deserialize(byte[] binary) {
            IoBuffer buf = IoBuffer.wrap(binary);
            int len = buf.getInt();
            FriendContact[] toReturn = new FriendContact[len];
            for (int i = 0; i < len; i++) {
                toReturn[i] = new FriendContact() {
                    {
                        AccountID = buf.getInt();
                        accountName = BufUtils.readUTF(buf);
                        lastConnection = buf.getLong();
                        achievementPoints = buf.getInt();
                    }
                };

            }
            return toReturn;
        }

    }

    public static class IgnoredContact {

        public int AccountID;
        public String accountName;

        public void Serialize(IoBuffer buf) {
            buf.putInt(AccountID);
            BufUtils.writeUTF(buf, accountName);
        }

        public static IgnoredContact[] Deserialize(byte[] binary) {
            IoBuffer buf = IoBuffer.wrap(binary);
            int len = buf.getInt();
            IgnoredContact[] toReturn = new IgnoredContact[len];
            for (int i = 0; i < len; i++) {
                toReturn[i] = new IgnoredContact() {
                    {
                        AccountID = buf.getInt();
                        accountName = BufUtils.readUTF(buf);
                    }
                };
            }
            return toReturn;
        }
    }

    public void NotifiedColumn(String C) {
        if (this.ColumsToUpdate == null) {
            this.ColumsToUpdate = new ArrayList<>();
        }
        if (!this.ColumsToUpdate.contains(C)) {
            this.ColumsToUpdate.add(C);
        }
    }

}
