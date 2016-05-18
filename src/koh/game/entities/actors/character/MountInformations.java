package koh.game.entities.actors.character;

import koh.game.dao.DAO;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class MountInformations {

    private static final Logger logger = LogManager.getLogger(MountInformations.class);

    public MountClientData mount;
    public byte ratio;
    public boolean isToogled;
    public MountInventoryItemEntity entity;
    public Player player;
    private GenericStats myStats;

    public MountInformations() {}

    public MountInformations(Player p) {
        this.player = p;
    }

    public void save() {
        if (this.mount != null && this.entity != null) {
            this.serializeInformations();
            DAO.getMountInventories().update(entity);
        }
    }

    public void onRiding() {
        synchronized (entity) { //FIXME inappropriate object
            if (this.isToogled) {
                throw new Error("player" + player.getNickName() + " try to ride a rided mount");
            } else if (this.player.getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) != null) {
                this.player.getInventoryCache().unEquipItem(this.player.getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS));
            }
            this.isToogled = true;
            this.enableStats(true);
            this.player.getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, 0, new EntityLook(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, this.player.getEntityLook().SkinsCopy(), this.player.getEntityLook().ColorsCopy(), this.player.getEntityLook().ScalesCopy(), this.player.getEntityLook().SubEntityCopy())));
            this.player.getEntityLook().bonesId = DAO.getMounts().find(this.mount.model).getEntityLook().bonesId;
            this.player.getEntityLook().indexedColors = DAO.getMounts().find(this.mount.model).getEntityLook().indexedColors;
            this.player.getEntityLook().skins = DAO.getMounts().find(this.mount.model).getEntityLook().skins;
            this.player.getEntityLook().scales = DAO.getMounts().find(this.mount.model).getEntityLook().scales;
            if (player.getFighter() != null) {
                player.getFighter().getEntityLook().subentities.add(new SubEntity(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, 0, new EntityLook(SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER, this.player.getEntityLook().SkinsCopy(), this.player.getEntityLook().ColorsCopy(), this.player.getEntityLook().ScalesCopy(), this.player.getEntityLook().SubEntityCopy())));
                player.getFighter().getEntityLook().bonesId = DAO.getMounts().find(this.mount.model).getEntityLook().bonesId;
                player.getFighter().getEntityLook().indexedColors = DAO.getMounts().find(this.mount.model).getEntityLook().indexedColors;
                player.getFighter().getEntityLook().skins= DAO.getMounts().find(this.mount.model).getEntityLook().skins;
                player.getFighter().getEntityLook().scales = DAO.getMounts().find(this.mount.model).getEntityLook().scales;
            }
        /*if (item.templateId != ItemsEnum.KRAMKRAM) { //Todo KAMELEONE
         this.player.getEntityLook().indexedColors.clear();
         }*/

            this.player.refreshEntitie();
        }
    }

    public void onGettingOff() {
        synchronized (entity) {
            if (this.isToogled) {
                this.isToogled = false;
                this.enableStats(false);
                this.player.getEntityLook().bonesId = 1;
                this.player.getEntityLook().skins = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                this.player.getEntityLook().indexedColors = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                this.player.getEntityLook().scales = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                this.player.getEntityLook().subentities = this.player.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                this.player.getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);
                if (player.getFighter() != null) {
                    player.getFighter().getEntityLook().bonesId = 1;
                    player.getFighter().getEntityLook().skins = player.getFighter().getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.skins;
                    player.getFighter().getEntityLook().indexedColors = player.getFighter().getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.indexedColors;
                    player.getFighter().getEntityLook().scales = player.getFighter().getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.scales;
                    player.getFighter().getEntityLook().subentities = player.getFighter().getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook.subentities;
                    player.getFighter().getEntityLook().subentities.removeIf(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER);

                }
                this.player.refreshEntitie();
            }
        }
    }

    private void parseStats() {
        this.myStats = new GenericStats();

        StatsEnum stat;
        for (ObjectEffectInteger e : this.mount.effectList) {

            stat = StatsEnum.valueOf(e.actionId);
            if (stat == null) {
                logger.error("Undefined MountStat id {} ", e.actionId);
                continue;
            }
            this.myStats.addItem(stat, e.value);

        }
        stat = null;
    }

    public GenericStats getStats() {
        if (this.myStats == null) {
            this.parseStats();
        }
        return myStats;
    }

    public void addExperience(long amount) {
        this.mount.experience += amount;

        while (this.mount.experience >= DAO.getExps().getLevel(this.mount.level + 1).getMount() && this.mount.level < 100) {
            levelUp();
        }

        this.save();
    }

    public void enableStats(boolean enable) {
        if (enable) {
            this.player.getStats().merge(getStats());
            this.player.addLife(getStats().getTotal(StatsEnum.VITALITY));
        } else {
            this.player.getStats().unMerge(getStats());
            this.player.addLife(-getStats().getTotal(StatsEnum.VITALITY));
        }
        this.player.refreshStats();
    }

    public void levelUp() {
        this.mount.level++;
        this.mount.effectList = ArrayUtils.removeAll(this.mount.effectList);
        this.mount.effectList = DAO.getMounts().getMountByEffect(this.mount.model, this.mount.level);
        if (this.isToogled) {
            this.enableStats(false);
            this.myStats = null;
            this.enableStats(true);
        }
        this.mount.experienceForLevel = DAO.getExps().getLevel(this.mount.level).getMount();
        this.mount.experienceForNextLevel = DAO.getExps().getLevel(this.mount.level == 100 ? 100 : this.mount.level + 1).getMount();
    }

    public byte[] serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(mount == null ? -1 : (int) mount.id);
        buf.put(this.ratio);
        BufUtils.writeBoolean(buf, isToogled);

        return buf.array();
    }

    public void serializeInformations() {
        IoBuffer buf = IoBuffer.allocate(0xFFF)
                .setAutoExpand(true);
        this.mount.serialize(buf);
        this.entity.informations = buf.flip().array();
    }

    public synchronized void initialize(int id) {
        if (id == -1)
            return;

        this.entity = DAO.getMountInventories().get(id);
        if (this.entity != null) {
            final IoBuffer buf = IoBuffer.wrap(this.entity.informations);
            this.mount = new MountClientData();
            this.mount.deserialize(buf);
        }
    }


    public void setPlayer(Player p){
        this.player = p;
    }


    public MountInformations deserialize(byte[] binary) {
        if (binary == null || binary.length <= 0) {
            return this;
        }
        final IoBuffer buf = IoBuffer.wrap(binary);
        this.initialize(buf.getInt());
        this.ratio = buf.get();
        this.isToogled = BufUtils.readBoolean(buf);
        return this;
    }

    public void disposeStats(){
        if(this.myStats != null){
            this.myStats.totalClear();
            this.myStats = null;
        }
    }


    public void totalClear() {
        //this.mount.clear();
        this.mount = null;
        if (entity != null) {
            this.entity.totalClear();
            this.entity = null;
        }
    }

}
