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
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemSet;
import koh.game.entities.item.ItemTemplate;
import koh.game.entities.item.Weapon;
import koh.game.entities.item.animal.MountInventoryItem;
import koh.game.entities.spells.*;
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
    private Player Player;
    public Map<Integer, InventoryItem> ItemsCache = Collections.synchronizedMap(new HashMap<>());
    public final static int[] UnMergeableType = new int[]{97, 121, 18};

    public CharacterInventory(Player character) {
        this.Player = character;
        ItemDAO.InitInventoryCache(Player.ID, ItemsCache, "character_items");
    }

    public void GeneralItemSetApply() {
        this.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63 && x.Template().ItemSet() != null).map(x -> x.Template().ItemSet()).distinct().forEach(Set -> {
            {
                this.ApplyItemSetEffects(Set, this.CountItemSetEquiped(Set.id), true, false);
            }
        });
    }

    public boolean Add(InventoryItem Item, boolean merge) //muste be true
    {
        if (merge && !Ints.contains(UnMergeableType, Item.Template().TypeId) && TryMergeItem(Item.TemplateId, Item.Effects, Item.Slot(), Item.GetQuantity(), Item, false)) {
            return false;
        }
        if (Item.GetOwner() != this.Player.ID) {
            Item.SetOwner(this.Player.ID);
        }
        if (ItemsCache.containsKey(Item.ID)) {
            RemoveFromDic(Item.ID);
        }
        ItemsCache.put(Item.ID, Item);

        Player.Send(new ObjectAddedMessage(Item.ObjectItem()));
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
        if (Item.GetPosition() != 63) {
            if (Item.Apparrance() != 0) {
                this.AddApparence(Item.Apparrance());
            }
            Player.RefreshEntitie();
            Player.Stats.Merge(Item.GetStats());
            Player.Life += Item.GetStats().GetTotal(StatsEnum.Vitality);
            Player.RefreshStats();
        }
        return true;
    }

    public boolean TryMergeItem(int TemplateId, List<ObjectEffect> Stats, CharacterInventoryPositionEnum Slot, int Quantity, InventoryItem RemoveItem) {
        return TryMergeItem(TemplateId, Stats, Slot, Quantity, RemoveItem, true);
    }

    public boolean TryMergeItem(int TemplateId, List<ObjectEffect> Stats, CharacterInventoryPositionEnum Slot, int Quantity, InventoryItem RemoveItem, boolean Send) {
        if (Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            for (InventoryItem Item : this.ItemsCache.values()) {
                if (Item.TemplateId == TemplateId && Item.Slot() == Slot && !(RemoveItem != null && RemoveItem.ID == Item.ID) && Item.Equals(Stats)) {
                    if (RemoveItem != null) {
                        this.RemoveFromDic(RemoveItem.ID);
                        RemoveItem.NeedInsert = false;
                        ItemDAO.Remove(RemoveItem, "character_items");
                        RemoveItem.ColumsToUpdate = null;
                        if (Send) {
                            Player.Send(new ObjectDeletedMessage(RemoveItem.ID));
                        }
                    }
                    this.UpdateObjectquantity(Item, Item.GetQuantity() + Quantity);
                    return true;
                }
            }
        }

        return false;
    }

    public List<ObjectItem> toObjectsItem() {
        return ItemsCache.values().stream().map(x -> x.ObjectItem()).collect(Collectors.toList());
    }

    public CharacterInventoryPositionEnum GetLivingObjectSlot(int id) {
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
        try {
            return (MountInventoryItem) this.ItemsCache.values().stream().filter(x -> x instanceof MountInventoryItem && ((MountInventoryItem) x).Entity.AnimalID == (int) id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized void RemoveApparence(short Apparence) {
        if (Apparence != 0) {
            if (this.Player.GetEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER)) {
                if (this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.indexOf(Apparence) != -1) {
                    this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.remove(this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.indexOf(Apparence));
                }
            } else if (this.Player.GetEntityLook().skins.indexOf(Apparence) != -1) {
                this.Player.GetEntityLook().skins.remove(this.Player.GetEntityLook().skins.indexOf(Apparence));
            }
        }
    }

    public synchronized void AddApparence(short Apparence) {
        if (Apparence != 0) {
            if (this.Player.GetEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER)) {
                this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins.add(Apparence);
            } else {
                this.Player.GetEntityLook().skins.add(Apparence);
            }
        }
    }

    public synchronized void MoveItem(int Guid, CharacterInventoryPositionEnum Slot, int quantity) {
        InventoryItem Item = this.ItemsCache.get(Guid);
        if (Item == null || Slot == null || Item.Slot() == Slot) {
            Player.Send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_UNEQUIP));
            return;
        }
        int count = this.CountItemSetEquiped(Item.Template().itemSetId);
        if (Slot != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            if (Item.Template().TypeId == 113) {
                Slot = this.GetLivingObjectSlot(Item.TemplateId);
                InventoryItem exItem = this.GetItemInSlot(Slot);
                if (exItem == null) {
                    Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 161, new String[0]));
                    return;
                }
                ObjectEffectInteger obviXp = (ObjectEffectInteger) Item.GetEffect(974), obviType = (ObjectEffectInteger) Item.GetEffect(973), obviState = (ObjectEffectInteger) Item.GetEffect(971), obviSkin = (ObjectEffectInteger) Item.GetEffect(972);
                ObjectEffectDate obviTime = (ObjectEffectDate) Item.GetEffect(808), exchangeTime = (ObjectEffectDate) Item.GetEffect(983);
                if (exItem.GetEffect(970) != null) {
                    PlayerController.SendServerMessage(Player.Client, "Action Impossible : cet objet est déjà associé à un objet d'apparance.");
                    return;
                }
                if (obviXp == null || obviType == null || obviState == null || obviSkin == null || obviTime == null) {
                    return;
                }
                if (exItem.GetEffect(983) != null || exItem.GetQuantity() != 1) {

                    PlayerController.SendServerMessage(Player.Client, "Action Impossible : cet objet ne peut pas être associé." + exItem.GetEffect(983).toString());
                    return;
                }
                if (exItem.Apparrance() != 0) {
                    this.RemoveApparence(exItem.Apparrance());
                    //this.Player.GetEntityLook().skins.remove(this.Player.GetEntityLook().skins.indexOf(exItem.Apparrance()));
                }

                exItem.getEffects().add(new ObjectEffectInteger(970, Item.TemplateId));
                exItem.getEffects().add(obviXp.Clone());
                exItem.getEffects().add(obviTime.Clone());
                exItem.getEffects().add(obviType.Clone());
                exItem.getEffects().add(obviState.Clone());
                exItem.getEffects().add(obviSkin.Clone());

                if (exchangeTime != null) {
                    exItem.getEffects().add(exchangeTime.Clone());
                }

                if (Item.GetQuantity() == 1) {
                    RemoveItem(Item);
                } else {
                    this.UpdateObjectquantity(Item, Item.GetQuantity() - 1);
                }
                if (exItem.Apparrance() != 0) {
                    this.AddApparence(exItem.Apparrance());
                }

                Player.Send(new ObjectModifiedMessage(exItem.ObjectItem()));
                Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
                Player.RefreshEntitie();
                return;
            }
            if (!ItemTemplate.CanPlaceInSlot(Item.GetSuperType(), Slot)) {
                Player.Send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_EQUIP_HERE));
                return;
            }
            this.UnEquipItem(this.GetItemInSlot(Slot));
            this.UnEquipedDouble(Item);

            if (Item.Template().level > Player.Level) {
                Player.Send(new ObjectErrorMessage(ObjectErrorEnum.LEVEL_TOO_LOW));
                return;
            }
            if (Item.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_SHIELD && hasWeaponTwoHanded()) //Todo IsValidConditions
            {
                Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 78, new String[0]));
                return;
            }
            if (Item.isWeapon() && Item.Template().twoHanded && GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_SHIELD) != null) {
                Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 79, new String[0]));
                return;
            }

            if (!Item.AreConditionFilled(Player)) {
                Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 19, new String[0]));
                return;
            }

            if (Item.GetQuantity() > 1) {
                /*InventoryItem NewItem = */
                TryCreateItem(Item.TemplateId, this.Player, 1, Slot.value(), Item.getEffectsCopy());
                this.UpdateObjectquantity(Item, Item.GetQuantity() - 1);
                return;
            }
            Player.Stats.Merge(Item.GetStats());
            this.Player.Life += Item.GetStats().GetTotal(StatsEnum.Vitality);
            Item.SetPosition(Slot);
            Player.Send(new ObjectMovementMessage(Item.ID, (byte) Item.GetPosition()));
            if (Item.Apparrance() != 0) {
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) {
                    if (this.Player.MountInfo.isToogled) {
                        this.Player.MountInfo.OnGettingOff();
                    }
                    //TODO:  Clean Code + Clear old ArrayList from  memory
                    if (Item.Template().TypeId == 121) { //Montelier
                        this.Player.GetEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, 0, new EntityLook(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, this.Player.GetEntityLook().SkinsCopy(), this.Player.GetEntityLook().ColorsCopy(), this.Player.GetEntityLook().ScalesCopy(), this.Player.GetEntityLook().SubEntityCopy())));
                        this.Player.GetEntityLook().bonesId = Item.Apparrance();
                        this.Player.GetEntityLook().skins.clear();
                        if (Item.TemplateId != ItemsEnum.Kramkram) {
                            this.Player.GetEntityLook().indexedColors.clear();
                        }
                        this.Player.GetEntityLook().scales.clear();
                    } else {
                        this.Player.GetEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET, 0, new EntityLook(Item.Apparrance(), new ArrayList<>(), new ArrayList<>(), new ArrayList<Short>() {
                            {
                                this.add((short) 80);
                            }
                        }, new ArrayList<>())));
                    }
                } else {
                    this.AddApparence(Item.Apparrance());
                }
            }
        } else {
            if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
                //Retire les stats
                this.Player.Stats.UnMerge(Item.GetStats());
                this.Player.Life -= Item.GetStats().GetTotal(StatsEnum.Vitality);
                if (Player.Life <= 0) {
                    Player.Life = 1;
                }
                this.Player.RefreshStats();
            }
            // On tente de fusionner
            if (Item.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET || !this.TryMergeItem(Item.TemplateId, Item.Effects, Slot, Item.GetQuantity(), Item)) {
                Item.SetPosition(CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED);
                Player.Send(new ObjectMovementMessage(Item.ID, (byte) Item.GetPosition()));
            }

            if (Item.Apparrance() != 0) {
                if (Item.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                    if (Item.Template().TypeId == 121) { //Montelier
                        try {
                            this.Player.GetEntityLook().bonesId = (short) 1;
                            this.Player.GetEntityLook().skins = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                            this.Player.GetEntityLook().indexedColors = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                            this.Player.GetEntityLook().scales = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                            this.Player.GetEntityLook().subentities = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                            this.Player.GetEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
                        } catch (Exception e) {
                        }
                    } else {
                        this.Player.GetEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET);
                    }
                } else {
                    this.RemoveApparence(Item.Apparrance());
                }
            }
        }
        this.CheckItemsCriterias();
        if (Item.Template().ItemSet() != null) {
            if (count >= 0) {
                this.ApplyItemSetEffects(Item.Template().ItemSet(), count, false, true);
            }
            count = this.CountItemSetEquiped(Item.Template().itemSetId);
            if (count > 0) {
                this.ApplyItemSetEffects(Item.Template().ItemSet(), count, true, false);
            }
            this.SendSetUpdateMessage(Item.Template().ItemSet(), count);
        }
        Player.RefreshStats();
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
        //System.out.println(this.Player.GetEntityLook().toString());
        Player.RefreshEntitie();
    }

    public void BeforeItemSet(InventoryItem Item) {
        if (Item.Template().ItemSet() != null) {
            int count = this.CountItemSetEquiped(Item.Template().itemSetId);
            if (count >= 0) {
                this.ApplyItemSetEffects(Item.Template().ItemSet(), count, false, true);
            }
        }
    }

    public void AfterItemSet(InventoryItem Item) {
        if (Item.Template().ItemSet() != null) {
            int count = this.CountItemSetEquiped(Item.Template().itemSetId);
            if (count > 0) {
                this.ApplyItemSetEffects(Item.Template().ItemSet(), count, true, false);
            }
            this.SendSetUpdateMessage(Item.Template().ItemSet(), count);
        }
    }

    public void SendSetUpdateMessage(ItemSet set, int count) {
        this.Player.Send(new SetUpdateMessage(set.id, this.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63 && x.Template().itemSetId == set.id).mapToInt(x -> x.TemplateId).toArray(), set.toObjectEffects(count)));
    }

    private int CountItemSetEquiped(int id) {
        return (int) this.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63 && x.Template().itemSetId == id).count();
    }

    private void ApplyItemSetEffects(ItemSet itemSet, int count, boolean apply, boolean send) {
        try {
            if (apply) {
                Player.Stats.Merge(itemSet.GetStats(count));
                this.Player.Life += itemSet.GetStats(count).GetTotal(StatsEnum.Vitality);
            } else {
                Player.Stats.UnMerge(itemSet.GetStats(count));
                this.Player.Life -= itemSet.GetStats(count).GetTotal(StatsEnum.Vitality);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (send) {
            this.Player.RefreshStats();;
        }
    }

    public static InventoryItem TryCreateItem(int templateId, Player Character, int quantity, byte position, List<ObjectEffect> Stats) {
        return TryCreateItem(templateId, Character, quantity, position, Stats, false);
    }

    public static InventoryItem TryCreateItem(int templateId, Player Character, int quantity, byte position, List<ObjectEffect> Stats, boolean Merge) {
        if (!ItemDAO.Cache.containsKey(templateId)) // Template inexistant
        {
            return null;
        }

        // Recup template
        ItemTemplate Template = ItemDAO.Cache.get(templateId);

        // Creation
        InventoryItem Item = InventoryItem.Instance(ItemDAO.NextID++, templateId, position, Character != null ? Character.ID : -1, quantity, (Stats == null ? EffectHelper.GenerateIntegerEffect(Template.possibleEffects, EffectGenerationType.Normal, Template instanceof Weapon) : Stats));
        Item.NeedInsert = true;
        Item.GetStats();
        if (Character != null) {
            Character.InventoryCache.Add(Item, Merge);
        }

        return Item;
    }

    private boolean UnEquipedDouble(InventoryItem itemToEquip) {
        if (itemToEquip.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_DOFUS) {
            Optional<InventoryItem> playerItem = this.GetEquipedItems().stream().filter(x -> x.ID != itemToEquip.ID && x.TemplateId == itemToEquip.TemplateId).findFirst();

            if (playerItem.isPresent()) {
                this.MoveItem(playerItem.get().ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
                return true;
            }
        }
        if (itemToEquip.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_RING) {
            Optional<InventoryItem> playerItem = this.GetEquipedItems().stream().filter(x -> x.ID != itemToEquip.ID && x.TemplateId == itemToEquip.TemplateId && x.Template().itemSetId > 0).findFirst();

            if (playerItem.isPresent()) {
                this.MoveItem(playerItem.get().ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
                return true;
            }
        }
        return false;
    }

    public Collection<InventoryItem> GetEquipedItems() {
        return this.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63).collect(Collectors.toSet());
    }

    public InventoryItem GetItemInTemplate(int Template) {
        try {
            return this.ItemsCache.values().stream().filter(x -> x.TemplateId == Template).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public InventoryItem GetItemInSlot(CharacterInventoryPositionEnum Slot) {
        try {
            return this.ItemsCache.values().stream().filter(x -> x.Slot() == Slot).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public InventoryItem GetWeapon() {
        try {
            return this.ItemsCache.values().stream().filter(x -> x.Slot() == CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasWeaponTwoHanded() {
        return GetWeapon() != null && GetWeapon().Template().twoHanded;
    }

    public int Weight() {
        return this.ItemsCache.values().stream().mapToInt(x -> x.Weight()).sum();
    }

    public int WeightTotal() {
        return 1000 + this.Player.Stats.GetTotal(StatsEnum.AddPods);
    }

    public void UpdateObjectquantity(InventoryItem Item, int Quantity) {
        Item.SetQuantity(Quantity);
        if (Item.GetQuantity() <= 0) {
            this.RemoveItem(Item);
            return;
        }
        Player.Send(new ObjectQuantityMessage(Item.ID, Item.GetQuantity()));
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
    }

    public void RemoveItem(InventoryItem Item) {
        Item.NeedInsert = false;
        this.RemoveFromDic(Item.ID);
        Player.Send(new ObjectDeletedMessage(Item.ID));
        ItemDAO.Remove(Item, "character_items");
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
    }

    public void ChangeOwner(InventoryItem Item, Player Trader) {
        this.RemoveFromDic(Item.ID);
        Player.Send(new ObjectDeletedMessage(Item.ID));
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
        Trader.InventoryCache.Add(Item, true);
    }

    public void RemoveItemFromInventory(InventoryItem Item) {
        Item.NeedInsert = false;
        Item.SetOwner(-1);
        this.RemoveFromDic(Item.ID);
        Player.Send(new ObjectDeletedMessage(Item.ID));
        Player.Send(new InventoryWeightMessage(Weight(), WeightTotal()));
    }

    public void RemoveFromDic(int id) {
        ItemsCache.remove(id);
        try {
            this.Player.Shortcuts.myShortcuts.entrySet().stream().filter(x -> x.getValue() instanceof ItemShortcut && ((ItemShortcut) x.getValue()).ItemID == id).map(x -> x.getKey()).forEach(y -> {
                this.Player.Shortcuts.myShortcuts.remove(y);
                this.Player.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, y));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AddKamas(int Value) {
        AddKamas(Value, true);
    }

    public void AddKamas(int Value, boolean send) {
        this.Player.Kamas += Value;
        this.Player.Send(new KamasUpdateMessage(this.Player.Kamas));
        //this.Player.RefreshStats();
        if (send) {
            this.Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 45, new String[]{Value + ""}));
        }
    }

    public void SubstractKamas(int Value) {
        SubstractKamas(Value, true);
    }

    public void SubstractKamas(int Value, boolean send) {
        this.Player.Kamas -= Value;
        this.Player.Send(new KamasUpdateMessage(this.Player.Kamas));
        //this.Player.RefreshStats();
        if (send) {
            this.Player.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 46, new String[]{Value + ""}));
        }
    }

    public boolean isDriven() {
        return this.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) != null && this.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS).Template().TypeId == 121;
    }

    public void UnEquipItem(InventoryItem EquipedItem) {
        if (EquipedItem == null) {
            return;
        }
        this.BeforeItemSet(EquipedItem);
        //Deplacement dans l'inventaire
        Player.Stats.UnMerge(EquipedItem.GetStats());
        this.Player.Life -= EquipedItem.GetStats().GetTotal(StatsEnum.Vitality);
        if (EquipedItem.Apparrance() != 0) {
            if (EquipedItem.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
                if (EquipedItem.Template().TypeId == 121) { //Montelier
                    try {
                        this.Player.GetEntityLook().bonesId = (short) 1;
                        this.Player.GetEntityLook().skins = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                        this.Player.GetEntityLook().indexedColors = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                        this.Player.GetEntityLook().scales = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                        this.Player.GetEntityLook().subentities = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                        this.Player.GetEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
                    } catch (Exception e) {

                    }
                } else {
                    this.Player.GetEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_PET);
                }
            } else {
                this.RemoveApparence(EquipedItem.Apparrance());
            }
        }
        EquipedItem.SetPosition(CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED);
        this.AfterItemSet(EquipedItem);
        Player.Send(new ObjectMovementMessage(EquipedItem.ID, (byte) EquipedItem.GetPosition()));

    }

    public boolean HasItemId(int parseInt) {
        return this.ItemsCache.values().stream().filter(x -> x.TemplateId == parseInt).findAny().isPresent();
    }

    private void CheckItemsCriterias() {
        //(!ConditionParser.validConditions(Player, x.Template().criteria)))
        this.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63 && !x.AreConditionFilled(Player)).forEach((Item) -> {
            this.MoveItem(Item.ID, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED, 1);
        });
    }

    public short WeaponCriticalHit() {
        InventoryItem playerItem;
        if ((playerItem = this.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON)) != null) {
            return playerItem.Template() instanceof Weapon ? (short) ((Weapon) playerItem.Template()).criticalHitBonus : 0;
        } else {
            return 0;
        }
    }

    public void Save(boolean Clear) {
        this.ItemsCache.values().parallelStream().forEach(Item -> {
            if (Item.NeedRemove) {
                ItemDAO.Remove(Item, "character_items");
            } else if (Item.NeedInsert) {
                ItemDAO.Insert(Item, Clear, "character_items");
            } else if (Item.ColumsToUpdate != null && !Item.ColumsToUpdate.isEmpty()) {
                ItemDAO.Update(Item, Clear, "character_items");
            } else if (Clear) {
                Item.totalClear();
            }
        });
        if (!Clear) {
            return;
        }
        this.Player = null;
        this.ItemsCache.clear();
        this.ItemsCache = null;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }

    }

}
