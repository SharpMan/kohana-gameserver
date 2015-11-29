package koh.game.entities.actors.character;

import koh.game.Main;
import koh.game.dao.mysql.ExpDAO;
import koh.game.dao.sqlite.MountDAO;
import koh.game.dao.sqlite.PetsDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.animal.MountInventoryItemEntity;
import koh.protocol.client.BufUtils;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.SubEntityBindingPointCategoryEnum;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.look.SubEntity;
import koh.protocol.types.game.mount.MountClientData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class MountInformations {

    public MountClientData Mount;
    public byte Ratio;
    public boolean isToogled;
    public MountInventoryItemEntity Entity;
    public Player Player;
    private GenericStats myStats;

    public MountInformations(Player P) {
        this.Player = P;
    }

    public void Save() {
        if (this.Mount != null && this.Entity != null) {
            this.SerializeInformations();
            PetsDAO.Update(Entity);
        }
    }

    public void OnRiding() {
        if (this.isToogled) {
            throw new Error("Player" + Player.NickName + " try to ride a rided mount");
        } else if (this.Player.InventoryCache.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) != null) {
            this.Player.InventoryCache.UnEquipItem(this.Player.InventoryCache.GetItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS));
        }
        this.isToogled = true;
        this.EnableStats(true);
        this.Player.GetEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, 0, new EntityLook(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, this.Player.GetEntityLook().SkinsCopy(), this.Player.GetEntityLook().ColorsCopy(), this.Player.GetEntityLook().ScalesCopy(), this.Player.GetEntityLook().SubEntityCopy())));
        this.Player.GetEntityLook().bonesId = MountDAO.Model(this.Mount.model).Look.bonesId;
        this.Player.GetEntityLook().indexedColors = MountDAO.Model(this.Mount.model).Look.indexedColors;
        this.Player.GetEntityLook().skins.clear();
        /*if (Item.TemplateId != ItemsEnum.Kramkram) { //Todo KAMELEONE
         this.Player.GetEntityLook().indexedColors.clear();
         }*/
        this.Player.GetEntityLook().scales = MountDAO.Model(this.Mount.model).Look.scales;
        this.Player.RefreshEntitie();
    }

    public void OnGettingOff() {
        if (this.isToogled) {
            this.isToogled = false;
            this.EnableStats(false);
            this.Player.GetEntityLook().bonesId = (short) 1;
            this.Player.GetEntityLook().skins = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
            this.Player.GetEntityLook().indexedColors = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
            this.Player.GetEntityLook().scales = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
            this.Player.GetEntityLook().subentities = this.Player.GetEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
            this.Player.GetEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
            this.Player.RefreshEntitie();
        }
    }

    private void ParseStats() {
        this.myStats = new GenericStats();

        StatsEnum Stat;
        for (ObjectEffectInteger e : this.Mount.effectList) {

            Stat = StatsEnum.valueOf(e.actionId);
            if (Stat == null) {
                Main.Logs().writeError("Undefinied MountStat id " + e.actionId);
                continue;
            }
            this.myStats.AddItem(Stat, e.value);

        }
        Stat = null;
    }

    public GenericStats GetStats() {
        if (this.myStats == null) {
            this.ParseStats();
        }
        return myStats;
    }

    public void addExperience(long amount) {
        this.Mount.experience += amount;

        while (this.Mount.experience >= ExpDAO.GetFloorByLevel(this.Mount.level + 1).Mount && this.Mount.level < 100) {
            levelUp();
        }

        this.Save();
    }

    public void EnableStats(boolean enable) {
        if (enable) {
            this.Player.Stats.Merge(GetStats());
            this.Player.Life += GetStats().GetTotal(StatsEnum.Vitality);
        } else {
            this.Player.Stats.UnMerge(GetStats());
            this.Player.Life -= GetStats().GetTotal(StatsEnum.Vitality);
        }
        this.Player.RefreshStats();
    }

    public void levelUp() {
        this.Mount.level++;
        this.Mount.effectList = ArrayUtils.removeAll(this.Mount.effectList);
        this.Mount.effectList = MountDAO.MountByEffect(this.Mount.model, this.Mount.level);
        if (this.isToogled) {
            this.EnableStats(false);
            this.myStats = null;
            this.EnableStats(true);
        }
        this.Mount.experienceForLevel = ExpDAO.GetFloorByLevel(this.Mount.level).Mount;
        this.Mount.experienceForNextLevel = ExpDAO.GetFloorByLevel(this.Mount.level == 100 ? 100 : this.Mount.level + 1).Mount;
    }

    public byte[] Serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(Mount == null ? -1 : (int) Mount.id);
        buf.put(this.Ratio);
        BufUtils.writeBoolean(buf, isToogled);

        return buf.array();
    }

    public void SerializeInformations() {
        IoBuffer buf = IoBuffer.allocate(65535);
        buf.setAutoExpand(true);
        this.Mount.serialize(buf);
        buf.flip();
        this.Entity.informations = buf.array();
        buf.clear();
        buf = null;
    }

    public synchronized void Initialize(int id) {
        if (id == -1) {
            return;
        }
        this.Entity = PetsDAO.GetMount(id);
        if (this.Entity != null) {
            IoBuffer buf = IoBuffer.wrap(this.Entity.informations);
            this.Mount = new MountClientData();
            this.Mount.deserialize(buf);
        }
    }

    public void Deserialize(byte[] binary) {
        if (binary.length <= 0) {
            return;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        this.Initialize(buf.getInt());
        this.Ratio = buf.get();
        this.isToogled = BufUtils.readBoolean(buf);
    }

    public void totalClear() {
        //this.Mount.clear();
        this.Mount = null;
        if (Entity != null) {
            this.Entity.totalClear();
            this.Entity = null;
        }
    }

}
