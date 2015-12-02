package koh.game.entities.actors.character;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemSet;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.item.Weapon;
import koh.game.entities.item.animal.MountInventoryItem;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.client.enums.ItemsEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.SubEntityBindingPointCategoryEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.inventory.InventoryWeightMessage;
import koh.protocol.messages.game.inventory.KamasUpdateMessage;
import koh.protocol.messages.game.inventory.items.ObjectAddedMessage;
import koh.protocol.messages.game.inventory.items.ObjectDeletedMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.messages.game.inventory.items.ObjectMovementMessage;
import koh.protocol.messages.game.inventory.items.ObjectQuantityMessage;
import koh.protocol.messages.game.inventory.items.SetUpdateMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRemovedMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.ObjectItem;
import koh.protocol.types.game.data.items.effects.ObjectEffectDate;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.look.SubEntity;

/**
 *
 * @author Neo-Craft
 */
public class CharacterInventory {

    //TODO : Updater PartyEntityLook if Dissociate/Associate LivingObject
    private Player player;
    public Map<Integer, InventoryItem> itemsCache = Collections.synchronizedMap(new HashMap<>());
    public final static int[] unMergeableType = new int[]{97, 121, 18};

    public CharacterInventory(Player character) {
        this.player = character;
        DAO.getItems().initInventoryCache(player.ID, itemsCache, "character_items");
    }

    public int getItemSetCount() {
        return (int) this.itemsCache.values().stream().filter(x -> x.getPosition() != 63 && x.getTemplate().getItemSet() != null).map(x -> x.getTemplate().getItemSet()).distinct().count();
    }

    public void generalItemSetApply() {
        this.itemsCache.values().stream().filter(x -> x.getPosition() != 63 && x.getTemplate().getItemSet() != null).map(x -> x.getTemplate().getItemSet()).distinct().forEach(Set -> {
            {
                this.applyItemSetEffects(Set, this.countItemSetEquiped(Set.id), true, false);
            }
        });
    }

    public boolean add(InventoryItem item, boolean merge) //muste be true
    {
        if (merge && !Ints.contains(unMergeableType, item.getTemplate().typeId) && tryMergeItem(item.templateId, item.effects, item.getSlot(), item.getQuantity(), item, false)) {
            return false;
        }
        if (item.getOwner() != this.player.ID) {
            item.setOwner(this.player.ID);
        }
        if (itemsCache.containsKey(item.ID)) {
            removeFromDic(item.ID);
        }
        itemsCache.put(item.ID, item);

        player.send(new ObjectAddedMessage(item.getObjectItem()));
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
        if (item.getPosition() != 63) {
            if (item.getApparrance() != 0) {
                this.addApparence(item.getApparrance());
            }
            player.refreshEntitie();
            player.stats.merge(item.getStats());
            player.life += item.getStats().getTotal(StatsEnum.Vitality);
            player.refreshStats();
        }
        return true;
    }

    public boolean tryMergeItem(int templateId, List<ObjectEffect> stats, CharacterInventoryPositionEnum slot, int Quantity, InventoryItem removeItem) {
        return tryMergeItem(templateId, stats, slot, Quantity, removeItem, true);
    }

    public boolean tryMergeItem(int templateId, List<ObjectEffect> stats, CharacterInventoryPositionEnum slot, int quantity, InventoryItem removeItem, boolean send) {
        if (slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            for (InventoryItem Item : this.itemsCache.values()) {
                if (Item.templateId == templateId && Item.getSlot() == slot && !(removeItem != null && removeItem.ID == Item.ID) && Item.Equals(stats)) {
                    if (removeItem != null) {
                        this.removeFromDic(removeItem.ID);
                        removeItem.needInsert = false;
                        DAO.getItems().delete(removeItem, "character_items");
                        removeItem.columsToUpdate = null;
                        if (send) {
                            player.send(new ObjectDeletedMessage(removeItem.ID));
                        }
                    }
                    this.updateObjectquantity(Item, Item.getQuantity() + quantity);
                    return true;
                }
            }
        }

        return false;
    }

    public List<ObjectItem> toObjectsItem() {
        return itemsCache.values().stream().map(x -> x.getObjectItem()).collect(Collectors.toList());
    }

    public CharacterInventoryPositionEnum getLivingObjectSlot(int id) {
        switch (id) {
            case 12425:
            case 9233:
            case 13211:
                return CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE;
            case 12424:
            case 9234:
            case 13213:
                return CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT;
            case 9255:
                return CharacterInventoryPositionEnum.ACCESSORY_POSITION_AMULET;
            case 9256:
                return CharacterInventoryPositionEnum.INVENTORY_POSITION_RING_LEFT; //right
            case 12426:
            case 13212:
                return CharacterInventoryPositionEnum.ACCESSORY_POSITION_BELT;
            case 12427:
            case 13210:
                return CharacterInventoryPositionEnum.ACCESSORY_POSITION_BOOTS;
            default:
                throw new Error("Unknow Living Object " + id);

        }
    }

    public MountInventoryItem GetMount(double id) {
        return (MountInventoryItem) this.itemsCache.values().stream().filter(x -> x instanceof MountInventoryItem && ((MountInventoryItem) x).entity.animalID == (int) id).findFirst().orElse(null);
    }

    public synchronized void removeApparence(short appearence) {
        if (appearence != 0) {
            if (this.player.getEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER)) {
                if (this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.indexOf(appearence) != -1) {
                    this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.remove(this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.indexOf(appearence));
                }
            } else if (this.player.getEntityLook().skins.indexOf(appearence) != -1) {
                this.player.getEntityLook().skins.remove(this.player.getEntityLook().skins.indexOf(appearence));
            }
        }
    }

    public synchronized void addApparence(short appearence) {
        if (appearence != 0) {
            if (this.player.getEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER)) {
                this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.add(appearence);
            } else {
                this.player.getEntityLook().skins.add(appearence);
            }
        }
    }

    public void MoveLivingItem(int guid, CharacterInventoryPositionEnum slot, int quantity) {
        InventoryItem Item = this.itemsCache.get(guid);
        slot = this.getLivingObjectSlot(Item.templateId);
        InventoryItem exItem = this.getItemInSlot(slot);
        if (exItem == null) {
            player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 161, new String[0]));
            return;
        }
        ObjectEffectInteger obviXp = (ObjectEffectInteger) Item.getEffect(974), obviType = (ObjectEffectInteger) Item.getEffect(973), obviState = (ObjectEffectInteger) Item.getEffect(971), obviSkin = (ObjectEffectInteger) Item.getEffect(972);
        ObjectEffectDate obviTime = (ObjectEffectDate) Item.getEffect(808), exchangeTime = (ObjectEffectDate) Item.getEffect(983);
        if (exItem.getEffect(970) != null) {
            PlayerController.sendServerMessage(player.client, "action Impossible : cet objet est déjà associé à un objet d'apparance.");
            return;
        }
        if (obviXp == null || obviType == null || obviState == null || obviSkin == null || obviTime == null) {
            return;
        }
        if (exItem.getEffect(983) != null || exItem.getQuantity() != 1) {

            PlayerController.sendServerMessage(player.client, "action Impossible : cet objet ne peut pas être associé." + exItem.getEffect(983).toString());
            return;
        }
        if (exItem.getApparrance() != 0) {
            this.removeApparence(exItem.getApparrance());
            //this.player.getEntityLook().skins.remove(this.player.getEntityLook().skins.indexOf(exItem.getApparrance()));
        }

        exItem.getEffects().add(new ObjectEffectInteger(970, Item.templateId));
        exItem.getEffects().add(obviXp.Clone());
        exItem.getEffects().add(obviTime.Clone());
        exItem.getEffects().add(obviType.Clone());
        exItem.getEffects().add(obviState.Clone());
        exItem.getEffects().add(obviSkin.Clone());

        if (exchangeTime != null) {
            exItem.getEffects().add(exchangeTime.Clone());
        }

        if (Item.getQuantity() == 1) {
            removeItem(Item);
        } else {
            this.updateObjectquantity(Item, Item.getQuantity() - 1);
        }
        if (exItem.getApparrance() != 0) {
            this.addApparence(exItem.getApparrance());
        }

        player.send(new ObjectModifiedMessage(exItem.getObjectItem()));
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
        player.refreshEntitie();
    }

    public void moveItem(int guid, CharacterInventoryPositionEnum slot, int quantity) {
        InventoryItem item = this.itemsCache.get(guid);
        if (item == null || slot == null || item.getSlot() == slot) {
            player.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_UNEQUIP));
            return;
        }
        int count = this.countItemSetEquiped(item.getTemplate().itemSetId);
        if (slot != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            if (item.getTemplate().typeId == 113) {
                this.MoveLivingItem(guid, slot, quantity);
                return;
            }

            if (!ItemTemplate.canPlaceInSlot(item.getSuperType(), slot)) {
                player.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_EQUIP_HERE));
                return;
            }
            this.unEquipItem(this.getItemInSlot(slot));
            this.unEquipedDouble(item);

            if (item.getTemplate().level > player.level) {
                player.send(new ObjectErrorMessage(ObjectErrorEnum.LEVEL_TOO_LOW));
                return;
            }
            if (item.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_SHIELD && hasWeaponTwoHanded()) //Todo IsValidConditions
            {
                player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 78, new String[0]));
                return;
            }
            if (item.isWeapon() && item.getTemplate().twoHanded && getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_SHIELD) != null) {
                player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 79, new String[0]));
                return;
            }

            if (!item.areConditionFilled(player)) {
                player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 19, new String[0]));
                return;
            }

            if (item.getQuantity() > 1) {
                /*InventoryItem NewItem = */
                tryCreateItem(item.templateId, this.player, 1, slot.value(), item.getEffectsCopy());
                this.updateObjectquantity(item, item.getQuantity() - 1);
                return;
            }
            if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
                item.setPosition(slot);
                player.send(new ObjectMovementMessage(item.ID, (byte) item.getPosition()));
            } else {
                player.stats.merge(item.getStats());
                this.player.life += item.getStats().getTotal(StatsEnum.Vitality);
                item.setPosition(slot);
                player.send(new ObjectMovementMessage(item.ID, (byte) item.getPosition()));
                if (item.getApparrance() != 0) {
                    if (slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) {
                        if (this.player.mountInfo.isToogled) {
                            this.player.mountInfo.onGettingOff();
                        }
                        //TODO:  clean Code + clear old ArrayList from  memory
                        if (item.getTemplate().typeId == 121) { //Montelier
                            this.player.getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, 0, new EntityLook(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, this.player.getEntityLook().SkinsCopy(), this.player.getEntityLook().ColorsCopy(), this.player.getEntityLook().ScalesCopy(), this.player.getEntityLook().SubEntityCopy())));
                            this.player.getEntityLook().bonesId = item.getApparrance();
                            this.player.getEntityLook().skins.clear();
                            if (item.templateId != ItemsEnum.Kramkram) {
                                this.player.getEntityLook().indexedColors.clear();
                            }
                            this.player.getEntityLook().scales.clear();
                        } else {
                            this.player.getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET, 0, new EntityLook(item.getApparrance(), new ArrayList<>(), new ArrayList<>(), new ArrayList<Short>() {
                                {
                                    this.add((short) 80);
                                }
                            }, new ArrayList<>())));
                        }
                    } else {
                        this.addApparence(item.getApparrance());
                    }
                }
            }
        } else {
            if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
                //Retire les stats
                this.player.stats.unMerge(item.getStats());
                this.player.life -= item.getStats().getTotal(StatsEnum.Vitality);
                if (player.life <= 0) {
                    player.life = 1;
                }
                this.player.refreshStats();
            }
            // On tente de fusionner
            if (item.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET || !this.tryMergeItem(item.templateId, item.effects, slot, item.getQuantity(), item)) {
                item.setPosition(CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED);
                player.send(new ObjectMovementMessage(item.ID, (byte) item.getPosition()));
            }

            if (item.getApparrance() != 0) {
                if (item.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                    if (item.getTemplate().typeId == 121) { //Montelier
                        try {
                            this.player.getEntityLook().bonesId = (short) 1;
                            this.player.getEntityLook().skins = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                            this.player.getEntityLook().indexedColors = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                            this.player.getEntityLook().scales = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                            this.player.getEntityLook().subentities = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                            this.player.getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
                        } catch (Exception e) {
                        }
                    } else {
                        this.player.getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET);
                    }
                } else {
                    this.removeApparence(item.getApparrance());
                }
            }
        }
        this.checkItemsCriterias();
        if (item.getTemplate().getItemSet() != null) {
            if (count >= 0) {
                this.applyItemSetEffects(item.getTemplate().getItemSet(), count, false, true);
            }
            count = this.countItemSetEquiped(item.getTemplate().itemSetId);
            if (count > 0) {
                this.applyItemSetEffects(item.getTemplate().getItemSet(), count, true, false);
            }
            this.sendSetUpdateMessage(item.getTemplate().getItemSet(), count);
        }
        player.refreshStats();
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
        //System.out.println(this.player.getEntityLook().toString());
        player.refreshEntitie();
    }

    public void beforeItemSet(InventoryItem Item) {
        if (Item.getTemplate().getItemSet() != null) {
            int count = this.countItemSetEquiped(Item.getTemplate().itemSetId);
            if (count >= 0) {
                this.applyItemSetEffects(Item.getTemplate().getItemSet(), count, false, true);
            }
        }
    }

    public void afterItemSet(InventoryItem Item) {
        if (Item.getTemplate().getItemSet() != null) {
            int count = this.countItemSetEquiped(Item.getTemplate().itemSetId);
            if (count > 0) {
                this.applyItemSetEffects(Item.getTemplate().getItemSet(), count, true, false);
            }
            this.sendSetUpdateMessage(Item.getTemplate().getItemSet(), count);
        }
    }

    public void sendSetUpdateMessage(ItemSet set, int count) {
        this.player.send(new SetUpdateMessage(set.id, this.itemsCache.values().stream().filter(x -> x.getPosition() != 63 && x.getTemplate().itemSetId == set.id).mapToInt(x -> x.templateId).toArray(), set.toObjectEffects(count)));
    }

    private int countItemSetEquiped(int id) {
        return (int) this.itemsCache.values().stream().filter(x -> x.getPosition() != 63 && x.getTemplate().itemSetId == id).count();
    }

    private void applyItemSetEffects(ItemSet itemSet, int count, boolean apply, boolean send) {
        try {
            if (apply) {
                player.stats.merge(itemSet.getStats(count));
                this.player.life += itemSet.getStats(count).getTotal(StatsEnum.Vitality);
            } else {
                player.stats.unMerge(itemSet.getStats(count));
                this.player.life -= itemSet.getStats(count).getTotal(StatsEnum.Vitality);
            }
            if (send) {
                this.player.refreshStats();;
            }
        } catch (NullPointerException e) { //Well confortable than check nullable getItemSet , nulltables getItemSet with this count of items .. ect
        }
    }

    public static InventoryItem tryCreateItem(int templateId, Player Character, int quantity, byte position, List<ObjectEffect> Stats) {
        return tryCreateItem(templateId, Character, quantity, position, Stats, false);
    }

    public static InventoryItem tryCreateItem(int templateId, Player character, int quantity, byte position, List<ObjectEffect> Stats, boolean Merge) {

        // Recup template
        ItemTemplate Template = DAO.getItemTemplates().getTemplate(templateId);

        if(Template == null)
            return null;

        // Creation
        InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), templateId, position, character != null ? character.ID : -1, quantity, (Stats == null ? EffectHelper.generateIntegerEffect(Template.possibleEffects, EffectGenerationType.Normal, Template instanceof Weapon) : Stats));
        item.needInsert = true;
        item.getStats();
        if (character != null) {
            character.inventoryCache.add(item, Merge);
        }

        return item;
    }

    private boolean unEquipedDouble(InventoryItem itemToEquip) {
        if (itemToEquip.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_DOFUS) {
            Optional<InventoryItem> playerItem = this.getEquipedItems().stream().filter(x -> x.ID != itemToEquip.ID && x.templateId == itemToEquip.templateId).findFirst();

            if (playerItem.isPresent()) {
                this.moveItem(playerItem.get().ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
                return true;
            }
        }
        if (itemToEquip.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_RING) {
            Optional<InventoryItem> playerItem = this.getEquipedItems().stream().filter(x -> x.ID != itemToEquip.ID && x.templateId == itemToEquip.templateId && x.getTemplate().itemSetId > 0).findFirst();

            if (playerItem.isPresent()) {
                this.moveItem(playerItem.get().ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
                return true;
            }
        }
        return false;
    }

    public Collection<InventoryItem> getEquipedItems() {
        return this.itemsCache.values().stream().filter(x -> x.getPosition() != 63).collect(Collectors.toSet());
    }

    public InventoryItem getItemInTemplate(int template) {
            return this.itemsCache.values().stream().filter(x -> x.templateId == template).findFirst().orElse(null);
    }

    public InventoryItem getItemInSlot(CharacterInventoryPositionEnum slot) {
            return this.itemsCache.values().stream().filter(x -> x.getSlot() == slot).findFirst().orElse(null);
    }

    public InventoryItem getWeapon() {
            return this.itemsCache.values().stream().filter(x -> x.getSlot() == CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON).findFirst().orElse(null);
    }

    public boolean hasWeaponTwoHanded() {
        return getWeapon() != null && getWeapon().getTemplate().twoHanded;
    }

    public int getWeight() {
        return this.itemsCache.values().stream().mapToInt(x -> x.Weight()).sum();
    }

    public int getTotalWeight() {
        return 1000 + this.player.stats.getTotal(StatsEnum.AddPods);
    }

    public void updateObjectquantity(InventoryItem item, int quantity) {
        item.SetQuantity(quantity);
        if (item.getQuantity() <= 0) {
            this.removeItem(item);
            return;
        }
        player.send(new ObjectQuantityMessage(item.ID, item.getQuantity()));
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
    }

    public void removeItem(InventoryItem item) {
        item.needInsert = false;
        this.removeFromDic(item.ID);
        player.send(new ObjectDeletedMessage(item.ID));
        DAO.getItems().delete(item, "character_items");
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
    }

    public void ChangeOwner(InventoryItem item, Player trader) {
        this.removeFromDic(item.ID);
        player.send(new ObjectDeletedMessage(item.ID));
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
        trader.inventoryCache.add(item, true);
    }

    public void removeItemFromInventory(InventoryItem item) {
        item.needInsert = false;
        item.setOwner(-1);
        this.removeFromDic(item.ID);
        player.send(new ObjectDeletedMessage(item.ID));
        player.send(new InventoryWeightMessage(getWeight(), getTotalWeight()));
    }

    public void removeFromDic(int id) {
        itemsCache.remove(id);
        try {
            this.player.shortcuts.myShortcuts.entrySet().stream().filter(x -> x.getValue() instanceof ItemShortcut && ((ItemShortcut) x.getValue()).itemID == id).map(x -> x.getKey()).forEach(y -> {
                this.player.shortcuts.myShortcuts.remove(y);
                this.player.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, y));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addKamas(int value) {
        addKamas(value, true);
    }

    public void addKamas(int value, boolean send) {
        this.player.kamas += value;
        this.player.send(new KamasUpdateMessage(this.player.kamas));
        //this.player.refreshStats();
        if (send) {
            this.player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 45, new String[]{value + ""}));
        }
    }

    public void substractKamas(int value) {
        substractKamas(value, true);
    }

    public void substractKamas(int value, boolean send) {
        this.player.kamas -= value;
        this.player.send(new KamasUpdateMessage(this.player.kamas));
        //this.player.refreshStats();
        if (send) {
            this.player.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 46, new String[]{value + ""}));
        }
    }

    public boolean isDriven() {
        return this.getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) != null && this.getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS).getTemplate().typeId == 121;
    }

    public void unEquipItem(InventoryItem equipedItem) {
        if (equipedItem == null) {
            return;
        }
        this.beforeItemSet(equipedItem);
        //Deplacement dans l'inventaire
        player.stats.unMerge(equipedItem.getStats());
        this.player.life -= equipedItem.getStats().getTotal(StatsEnum.Vitality);
        if (equipedItem.getApparrance() != 0) {
            if (equipedItem.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                if (equipedItem.getTemplate().typeId == 121) { //Montelier
                    try {
                        this.player.getEntityLook().bonesId = (short) 1;
                        this.player.getEntityLook().skins = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                        this.player.getEntityLook().indexedColors = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                        this.player.getEntityLook().scales = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                        this.player.getEntityLook().subentities = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                        this.player.getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
                    } catch (Exception e) {

                    }
                } else {
                    this.player.getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET);
                }
            } else {
                this.removeApparence(equipedItem.getApparrance());
            }
        }
        equipedItem.setPosition(CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED);
        this.afterItemSet(equipedItem);
        player.send(new ObjectMovementMessage(equipedItem.ID, (byte) equipedItem.getPosition()));

    }

    public boolean hasItemId(int parseInt) {
        return this.itemsCache.values().stream().filter(x -> x.templateId == parseInt).findAny().isPresent();
    }

    private void checkItemsCriterias() {
        //(!ConditionParser.validConditions(player, x.getTemplate().criteria)))
        this.itemsCache.values().stream().filter(x -> x.getPosition() != 63 && !x.areConditionFilled(player)).forEach((Item) -> {
            this.moveItem(Item.ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
        });
    }

    public short weaponCriticalHit() {
        InventoryItem playerItem;
        if ((playerItem = this.getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON)) != null) {
            return playerItem.getTemplate() instanceof Weapon ? (short) ((Weapon) playerItem.getTemplate()).criticalHitBonus : 0;
        } else {
            return 0;
        }
    }

    public void save(boolean Clear) {
        this.itemsCache.values().parallelStream().forEach(Item -> {
            if (Item.NeedRemove) {
                DAO.getItems().delete(Item, "character_items");
            } else if (Item.needInsert) {
                DAO.getItems().create(Item, Clear, "character_items");
            } else if (Item.columsToUpdate != null && !Item.columsToUpdate.isEmpty()) {
                DAO.getItems().create(Item, Clear, "character_items");
            } else if (Clear) {
                Item.totalClear();
            }
        });
        if (!Clear) {
            return;
        }
        this.player = null;
        this.itemsCache.clear();
        this.itemsCache = null;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }

    }

}
