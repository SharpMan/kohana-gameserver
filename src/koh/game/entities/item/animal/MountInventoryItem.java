package koh.game.entities.item.animal;

import java.time.Instant;
import java.util.List;
import koh.game.dao.ExpDAO;
import koh.game.dao.ItemDAO;
import koh.game.dao.MountDAO;
import koh.game.dao.PetsDAO;
import koh.game.entities.item.InventoryItem;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.*;
import koh.protocol.types.game.mount.MountClientData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class MountInventoryItem extends InventoryItem {

    protected boolean myInitialized = false;
    public MountInventoryItemEntity Entity;
    public MountClientData Mount;

    public MountInventoryItem(int ID, int TemplateId, int Position, int Owner, int Quantity, List<ObjectEffect> Effects, boolean Create) {
        super(ID, TemplateId, Position, Owner, Quantity, Effects);
        if (!Create) {
            this.Entity = PetsDAO.GetMount(((ObjectEffectMount) this.GetEffect(995)).mountId);
            if (this.Entity != null) {
                this.Initialize();
            }
        }
        if (Create) {
            this.RemoveEffect(995);
            this.RemoveEffect(998);
            this.Effects.add(new ObjectEffectMount(955, (double) Instant.now().toEpochMilli(), 10, 0));
            this.Effects.add(new ObjectEffectDuration(998, 37, (byte) 0, (byte) 0));
        }
        if (this.Entity == null) {
            this.Entity = new MountInventoryItemEntity();
            this.Entity.AnimalID = ItemDAO.NextMountsID++;
            this.Entity.lastEat = (System.currentTimeMillis() - (24 * 3600 * 1000)) + "";
            this.Mount = new MountClientData();
            this.Mount.ownerId = Owner;
            this.Mount.energy = this.Mount.energyMax = this.Mount.loveMax = this.Mount.reproductionCountMax = this.Mount.reproductionCountMax = this.Mount.serenityMax = 10000;
            this.Mount.maxPods = 3000;
            this.Mount.experience = 0;
            this.Mount.experienceForLevel = ExpDAO.GetFloorByLevel(1).Mount;
            this.Mount.experienceForNextLevel = (double) ExpDAO.GetFloorByLevel(2).Mount;
            this.Mount.id = (double) this.Entity.AnimalID;
            this.Mount.isRideable = true;
            this.Mount.level = 1;
            this.Mount.model = MountDAO.Cache.get(this.TemplateId).Id;
            this.Mount.effectList = MountDAO.MountByEffect(this.Mount.model, this.Mount.level);
            this.SerializeInformations();

            this.RemoveEffect(995);
            this.RemoveEffect(998);

            this.Effects.add(new ObjectEffectMount(955, (double) Instant.now().toEpochMilli(), MountDAO.Cache.get(this.TemplateId).Id, this.Entity.AnimalID));
            this.Effects.add(new ObjectEffectDuration(998, 37, (byte) 0, (byte) 0));
            this.NotifiedColumn("effects");

            PetsDAO.Insert(this.Entity);
        }
    }

    public void addExperience(long amount) {
        this.Mount.experience += amount;

        while (this.Mount.experience >= ExpDAO.GetFloorByLevel(this.Mount.level + 1).Mount && this.Mount.level < 100) {
            levelUp();
        }
        this.Save();
    }

    public void levelUp() {
        this.Mount.level++;
        this.Mount.effectList = ArrayUtils.removeAll(this.Mount.effectList);
        this.Mount.effectList = MountDAO.MountByEffect(this.Mount.model, this.Mount.level);
    }

    public void Save() {
        this.SerializeInformations();
        PetsDAO.Update(Entity);
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

    public synchronized void Initialize() {
        if (myInitialized) {
            return;
        }
        IoBuffer buf = IoBuffer.wrap(this.Entity.informations);
        this.Mount = new MountClientData();
        this.Mount.deserialize(buf);

        this.myInitialized = true;
    }

    @Override
    public void totalClear() {
        //this.Mount.clear();
        this.Mount = null;
        this.myInitialized = false;
        this.Entity.totalClear();
        this.Entity = null;
        super.totalClear();
    }

}
