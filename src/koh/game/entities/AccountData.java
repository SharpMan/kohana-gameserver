package koh.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import koh.game.dao.DAO;
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

    public int id;
    public int kamas;
    public FriendContact[] friends;
    public IgnoredContact[] ignored;
    public IgnoredContact spouse;
    public boolean friend_warn_on_login;
    public boolean friend_warn_on_level_gain;
    public boolean guild_warn_on_login;
    public Map<Integer, InventoryItem> itemscache = Collections.synchronizedMap(new HashMap<>());

    public List<String> columsToUpdate = null;

    public List<ObjectItem> toObjectsItem() {
        return itemscache.values().stream().map(InventoryItem::getObjectItem).collect(Collectors.toList());
    }

    public void updateObjectQuantity(Player player, InventoryItem item, int quantity) {
        item.setQuantity(quantity);
        if (item.getQuantity() <= 0) {
            this.removeItem(player, item);
            return;
        }
        player.send(new StorageObjectUpdateMessage(item.getObjectItem()));
    }

    public void removeItem(Player player, InventoryItem item) {
        item.setNeedInsert(false);
        item.columsToUpdate = null;
        this.removeFromDic(item.getID());
        player.send(new StorageObjectRemoveMessage(item.getID()));
        DAO.getItems().delete(item, "storage_items");
    }

    public void setBankKamas(int Price) {
        this.kamas = Price;
        this.notifyColumn("kamas");
    }

    public void removeFromDic(int id) {
        itemscache.remove(id);
    }

    public void setFriendWarnOnGuildLogin(boolean b) {
        this.guild_warn_on_login = b;
        this.notifyColumn("friend_warn_on_login");
    }

    public void setFriendWarnOnLevelGain(boolean b) {
        this.friend_warn_on_level_gain = b;
        this.notifyColumn("friend_warn_on_level_gain");
    }

    public boolean add(Player player, InventoryItem item, boolean merge) //muste be true
    {
        if (merge && tryMergeItem(player, item.getTemplateId(), item.getEffects$Notify(), item.getSlot(), item.getQuantity(), item, false)) {
            return false;
        }
        if (item.getOwner() != id) {
            item.setOwner(id);
        }
        if (itemscache.containsKey(item.getID())) {
            removeFromDic(item.getID());
        }
        itemscache.put(item.getID(), item);

        player.send(new StorageObjectUpdateMessage(item.getObjectItem()));

        return true;
    }

    public boolean tryMergeItem(Player player, int templateId, List<ObjectEffect> stats, CharacterInventoryPositionEnum slot, int quantity, InventoryItem removeItem, boolean send) {
        if (slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            for (InventoryItem Item : this.itemscache.values()) {
                if (Item.getTemplateId() == templateId && Item.getSlot() == slot && !(removeItem != null && removeItem.getID() == Item.getID()) && Item.Equals(stats) ) {
                    if (removeItem != null) {
                        this.removeFromDic(removeItem.getID());
                        removeItem.setNeedInsert(false);
                        DAO.getItems().delete(removeItem, "storage_items");
                        removeItem.columsToUpdate = null;
                    }
                    this.updateObjectQuantity(player, Item, Item.getQuantity() + quantity);
                    return true;
                }
            }
        }

        return false;
    }

    public void setFriendWarnOnLogin(boolean b) {
        this.friend_warn_on_login = b;
        this.notifyColumn("friend_warn_on_login");
    }

    public void addFriend(FriendContact friend) {
        this.friends = ArrayUtils.add(friends, friend);
        this.notifyColumn("friends");
    }

    public void addIgnored(IgnoredContact friend) {
        this.ignored = ArrayUtils.add(ignored, friend);
        this.notifyColumn("ignored");
    }

    public FriendContact getFriend(int account) {
        try {
            return Arrays.stream(friends).filter(x -> x.accountID == account).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public IgnoredContact getIgnored(int account) {
         return Arrays.stream(ignored).filter(x -> x.accountID == account).findFirst().orElse(null);
    }

    public boolean hasFriend(int account) {
        return Arrays.stream(friends).anyMatch(x -> x.accountID == account);
    }

    public boolean ignore(int account) {
        return Arrays.stream(ignored).anyMatch(x -> x.accountID == account);
    }

    public List<FriendInformations> getFriendsInformations() {
        List<FriendInformations> friends = new ArrayList<>();
        for (FriendContact friend : this.friends) {
            Player target = DAO.getPlayers().getCharacterByAccount(friend.accountID);
            if (target == null || target.getClient() == null) {
                friends.add(new FriendInformations(friend.accountID, friend.accountName, PlayerStateEnum.NOT_CONNECTED, (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - friend.lastConnection), friend.achievementPoints));
            } else {
                friend.achievementPoints = target.getAchievementPoints();
                friend.lastConnection = System.currentTimeMillis();
                this.notifyColumn("friends");
                if (target.getAccount().accountData != null && target.getAccount().accountData.hasFriend(this.id)) {
                    friends.add(new FriendOnlineInformations(target.getAccount().id, target.getAccount().nickName, target.getPlayerState(), -1, target.getAchievementPoints(), target.getID(), target.getNickName(), (byte) target.getLevel(), target.getAlignmentSide().value, target.getBreed(), target.hasSexe(), target.getBasicGuildInformations(), target.getMoodSmiley(), new PlayerStatus(target.getStatus().value())));
                } else {
                    friends.add(new FriendOnlineInformations(target.getAccount().id, target.getAccount().nickName, target.getPlayerState(), -1, target.getAchievementPoints(), target.getID(), target.getNickName(), (byte) 0, (byte) -1, target.getBreed(), target.hasSexe(), new BasicGuildInformations(0, ""), (byte) -1, new PlayerStatus(target.getStatus().value())));
                }
            }
        }
        return friends;
    }

    public List<IgnoredInformations> getIgnoredInformations() {
        List<IgnoredInformations> friends = new ArrayList<>();
        for (IgnoredContact Friend : this.ignored) {
            Player target = DAO.getPlayers().getCharacterByAccount(Friend.accountID);
            if (target == null || target.getClient() == null) {
                friends.add(new IgnoredInformations(Friend.accountID, Friend.accountName));
            } else {
                friends.add(new IgnoredOnlineInformations(target.getAccount().id, target.getAccount().nickName, target.getID(), target.getNickName(), target.getBreed(), target.hasSexe()));
            }
        }
        return friends;
    }

    public boolean removeIgnored(int id) {
        IgnoredContact person = person = Arrays.stream(ignored).filter(x -> x.accountID == id).findFirst().orElse(null);
        if(person == null)
            return false;
        this.ignored = ArrayUtils.removeElement(ignored, person);
        this.notifyColumn("ignored");
        return true;
    }

    public void removeIgnored(IgnoredContact person) {
        this.ignored = ArrayUtils.removeElement(ignored, person);
        this.notifyColumn("ignored");
    }

    public void removeFriend(FriendContact friend) {
        this.friends = ArrayUtils.removeElement(friends, friend);
        this.notifyColumn("friends");
    }

    public boolean removeFriend(int id) {
        FriendContact friend = friend = Arrays.stream(friends).filter(x -> x.accountID == id).findFirst().orElse(null);
        if(friend == null)
            return false;
        this.friends = ArrayUtils.removeElement(friends, friend);
        this.notifyColumn("friends");
        return true;
    }

    public void save(boolean clear) {
        synchronized (itemscache) {
            this.itemscache.values().parallelStream().forEach(Item -> {
                if (Item.isNeedRemove()) {
                    DAO.getItems().delete(Item, "storage_items");
                } else if (Item.isNeedInsert()) {
                    DAO.getItems().create(Item, clear, "storage_items");
                } else if (Item.columsToUpdate != null && !Item.columsToUpdate.isEmpty()) {
                    DAO.getItems().save(Item, clear, "storage_items");
                } else if (clear) {
                    Item.totalClear();
                }
            });
            if (!clear && this.columsToUpdate != null) {
                DAO.getAccountDatas().save(this, null);
            }
        }
    }

    public void totalClear(Account c) {
        try {
            this.save(true);
            id = 0;
            kamas = 0;
            friends = null;
            ignored = null;
            friend_warn_on_login = false;
            friend_warn_on_level_gain = false;
            guild_warn_on_login = false;
            this.itemscache.clear();
            this.itemscache = null;
            c.continueClear();
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public byte[] serializeFriends() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);
        buf.putInt(friends.length);
        Arrays.stream(friends).forEach(contact -> contact.serialize(buf));

        return buf.array();
    }

    public byte[] serializeIgnored() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);
        buf.putInt(ignored.length);
        Arrays.stream(ignored).forEach(contact -> contact.serialize(buf));

        return buf.array();
    }

    public static class FriendContact {

        public int accountID;
        public String accountName;
        public long lastConnection;
        public int achievementPoints;

        public void serialize(IoBuffer buf) {
            buf.putInt(accountID);
            BufUtils.writeUTF(buf, accountName);
            buf.putLong(lastConnection);
            buf.putInt(achievementPoints);
        }

        public static FriendContact[] deserialize(byte[] binary) {
            IoBuffer buf = IoBuffer.wrap(binary);
            int len = buf.getInt();
            FriendContact[] toReturn = new FriendContact[len];
            for (int i = 0; i < len; i++) {
                toReturn[i] = new FriendContact() {
                    {
                        accountID = buf.getInt();
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

        public int accountID;
        public String accountName;

        public void serialize(IoBuffer buf) {
            buf.putInt(accountID);
            BufUtils.writeUTF(buf, accountName);
        }

        public static IgnoredContact[] deserialize(byte[] binary) {
            IoBuffer buf = IoBuffer.wrap(binary);
            int len = buf.getInt();
            IgnoredContact[] toReturn = new IgnoredContact[len];
            for (int i = 0; i < len; i++) {
                toReturn[i] = new IgnoredContact() {
                    {
                        accountID = buf.getInt();
                        accountName = BufUtils.readUTF(buf);
                    }
                };
            }
            return toReturn;
        }
    }

    public void notifyColumn(String C) {
        if (this.columsToUpdate == null) {
            this.columsToUpdate = new ArrayList<>();
        }
        if (!this.columsToUpdate.contains(C)) {
            this.columsToUpdate.add(C);
        }
    }

}
