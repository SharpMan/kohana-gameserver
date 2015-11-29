package koh.game.entities.item.animal;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.dao.sqlite.PetsDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.protocol.client.enums.ItemsEnum;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class PetsInventoryItem extends InventoryItem {

    /* @Param1 = FoodId , @Param2 = Count */
    public Map<Integer, Integer> EatedFoods = Collections.synchronizedMap(new HashMap<>());
    public Map<Integer, Integer> EatedFoodsType = Collections.synchronizedMap(new HashMap<>());

    public PetsInventoryItemEntity Entity;

    protected boolean myInitialized = false;

    public PetsInventoryItem() {
        super();
    }

    public void SerializeInformations() {
        IoBuffer buf = IoBuffer.allocate(65535);
        buf.setAutoExpand(true);

        buf.putInt(EatedFoods.size());
        for (Entry<Integer, Integer> e : EatedFoods.entrySet()) {
            buf.putInt(e.getKey());
            buf.putInt(e.getValue());
        }

        buf.putInt(EatedFoodsType.size());
        for (Entry<Integer, Integer> e : EatedFoodsType.entrySet()) {
            buf.putInt(e.getKey());
            buf.putInt(e.getValue());
        }

        buf.flip();
        this.Entity.informations = buf.array();
        buf.clear();
        buf = null;
    }

    public PetsInventoryItem(int ID, int TemplateId, int Position, int Owner, int Quantity, List<ObjectEffect> Effects, boolean Create) {
        super(ID, TemplateId, Position, Owner, Quantity, Effects);
        if (!Create) {
            this.Entity = PetsDAO.Get(((ObjectEffectInteger) this.GetEffect(995)).value);
            if (this.Entity != null) {
                this.Initialize();
            }
        }
        if (this.Entity == null) {
            this.Entity = new PetsInventoryItemEntity();
            this.Entity.PetsID = ItemTemplateDAOImpl.nextPetId++;
            this.Entity.lastEat = (System.currentTimeMillis() - (24 * 3600 * 1000)) + "";
            this.Entity.PointsUsed = 0;
            this.SerializeInformations();
            this.RemoveEffect(995);

            this.Effects.add(new ObjectEffectInteger(995, this.Entity.PetsID));
            this.NotifiedColumn("effects");

            PetsDAO.Insert(this.Entity);
        }
    }

    public synchronized void Initialize() {
        if (myInitialized) {
            return;
        }
        IoBuffer buf = IoBuffer.wrap(this.Entity.informations);
        for (int i = 0; i < buf.getInt(); ++i) {
            this.EatedFoods.put(buf.getInt(), buf.getInt());
        }
        for (int i = 0; i < buf.getInt(); ++i) {
            this.EatedFoodsType.put(buf.getInt(), buf.getInt());
        }
        this.myInitialized = true;
    }

    public Pets Animal() {
        return ItemTemplateDAOImpl.Pets.get(this.TemplateId);
    }

    public boolean Eat(Player p, InventoryItem Food) {
        //Todo refresh stat
        if (!ItemTemplateDAOImpl.Pets.containsKey(this.TemplateId)) {
            return false;
        } else if (Food.TemplateId == ItemsEnum.EneripsaPouder) {
            //TODO : Life
            return true;
        } else if (this.Entity.PointsUsed >= Animal().Hormone) {
            return false;
        } else if (((int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Long.parseLong(this.Entity.lastEat))) < ItemTemplateDAOImpl.Pets.get(this.TemplateId).minDurationBeforeMeal) {
            PlayerController.SendServerMessage(p.Client, "Veuillez patientez " + ((Animal().minDurationBeforeMeal) - ((int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Long.parseLong(this.Entity.lastEat)))) + " heures pour le prochain repas");
            return false;
        }

        for (FoodItem i : Animal().foodItems) {
            if (Food.TemplateId == i.ItemID) {
                if (!this.EatedFoods.containsKey(Food.TemplateId)) {
                    this.EatedFoods.put(Food.TemplateId, 0);
                }
                this.EatedFoods.put(Food.TemplateId, this.EatedFoods.get(Food.TemplateId) + 1);
                this.Entity.PointsUsed += i.Point;
                this.Boost(i.Stats, i.StatsPoints);
                UpdateFood(Food.TemplateId);
                this.UpdateDate();
                this.CheckLastEffect();
                p.Send(new ObjectModifiedMessage(this.ObjectItem()));
                this.Save();
                return true;
            }
        }
        for (FoodItem i : Animal().foodTypes) {
            if (Food.Template().TypeId == i.ItemID) {
                if (!this.EatedFoodsType.containsKey(Food.Template().TypeId)) {
                    this.EatedFoodsType.put(Food.Template().TypeId, 0);
                }
                this.EatedFoodsType.put(Food.Template().TypeId, this.EatedFoodsType.get(Food.Template().TypeId) + 1);
                this.Entity.PointsUsed += i.Point;
                this.Boost(i.Stats, i.StatsPoints);
                UpdateFood(Food.TemplateId);
                this.UpdateDate();
                this.CheckLastEffect();
                p.Send(new ObjectModifiedMessage(this.ObjectItem()));
                this.Save();
                return true;
            }
        }

        return false;
    }

    public int GetEatedFood(int id) {
        if (this.EatedFoods.containsKey(id)) {
            return this.EatedFoods.get(id);
        }
        return 0;
    }

    public void UpdateDate() {
        Calendar now = Calendar.getInstance();
        ((ObjectEffectDate) this.GetEffect(808)).SetDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR), now.get(Calendar.MINUTE));
    }

    public void UpdateFood(int food) {
        ((ObjectEffectInteger) this.GetEffect(807)).value = food;
    }

    public void Boost(int EffectID, int Count) {
        ObjectEffect Effect = this.GetEffect(EffectID);
        if (Effect == null) {
            this.Effects.add(new ObjectEffectInteger(EffectID, Count));
        } else {
            ((ObjectEffectInteger) Effect).value += Count;
        }
        this.Entity.lastEat = System.currentTimeMillis() + "";
        this.NotifiedColumn("effects");
    }

    public void Save() {
        this.SerializeInformations();
        PetsDAO.Update(Entity);
    }

    public void CheckLastEffect() {

    }

    public void onMonsterFightWin() {

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void totalClear() {
        this.EatedFoods.clear();
        this.EatedFoods = null;
        this.EatedFoodsType.clear();
        this.EatedFoodsType = null;
        this.myInitialized = false;
        this.Entity.totalClear();
        this.Entity = null;
        super.totalClear();
    }

}
