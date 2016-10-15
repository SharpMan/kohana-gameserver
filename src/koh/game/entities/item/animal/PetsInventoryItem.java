package koh.game.entities.item.animal;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.protocol.client.enums.ItemsEnum;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectDate;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Neo-Craft
 */
public class PetsInventoryItem extends InventoryItem {

    /* @Param1 = FoodId , @Param2 = Count */
    public Map<Integer, Integer> eatedFoods = Collections.synchronizedMap(new HashMap<>());
    public Map<Integer, Integer> eatedFoodsType = Collections.synchronizedMap(new HashMap<>());

    public PetsInventoryItemEntity entity;

    protected boolean myInitialized = false;

    public PetsInventoryItem() {
        super();
    }

    public void serializeInformations() {
        IoBuffer buf = IoBuffer.allocate(0xFFF)
                .setAutoExpand(true);

        buf.putInt(eatedFoods.size());
        for (Entry<Integer, Integer> e : eatedFoods.entrySet()) {
            buf.putInt(e.getKey());
            buf.putInt(e.getValue());
        }

        buf.putInt(eatedFoodsType.size());
        for (Entry<Integer, Integer> e : eatedFoodsType.entrySet()) {
            buf.putInt(e.getKey());
            buf.putInt(e.getValue());
        }

        this.entity.informations = buf.flip().array();
    }

    public PetsInventoryItem(int ID, int templateId, int position, int owner, int quantity, List<ObjectEffect> effects, boolean create) {
        super(ID, templateId, position, owner, quantity, effects);
        if (!create) {
            this.entity = DAO.getPetInventories().get(((ObjectEffectInteger) this.getEffect(995)).value);
            if (this.entity != null) {
                this.initialize();
            }
        }
        if (this.entity == null) {
            this.entity = new PetsInventoryItemEntity();
            this.entity.petsID = DAO.getPetInventories().nextId();
            this.entity.lastEat = (System.currentTimeMillis() - (24 * 3600 * 1000)) + "";
            this.entity.pointsUsed = 0;
            this.serializeInformations();
            this.removeEffect(995);

            this.getEffects$Notify().add(new ObjectEffectInteger(995, this.entity.petsID));

            DAO.getPetInventories().insert(this.entity);
        }
    }

    public synchronized void initialize() {
        if (myInitialized) {
            return;
        }
        final IoBuffer buf = IoBuffer.wrap(this.entity.informations);
        for (int i = 0; i < buf.getInt(); ++i) {
            this.eatedFoods.put(buf.getInt(), buf.getInt());
        }
        for (int i = 0; i < buf.getInt(); ++i) {
            this.eatedFoodsType.put(buf.getInt(), buf.getInt());
        }
        this.myInitialized = true;
    }

    public PetTemplate getAnimal() {
        return DAO.getItemTemplates().getPetTemplate(this.getTemplateId());
    }

    public synchronized boolean eat(Player p, InventoryItem food) {
        //TODO : refresh stat of the player
        final PetTemplate pet = getAnimal();
        if (pet == null) {
            return false;
        } else if (food.getTemplateId() == ItemsEnum.ENERIPSA_POUDER) {
            //TODO : life
            return true;
        } else if (this.entity.pointsUsed >= getAnimal().getHormone()) {
            return false;
        } else if (((int) TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - Long.parseLong(this.entity.lastEat))) < 60 /*pet.getMinDurationBeforeMeal()*/) {
            PlayerController.sendServerMessage(p.getClient(), "Veuillez patientez " + ((/*getAnimal().getMinDurationBeforeMeal()*/60) - ((int) TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - Long.parseLong(this.entity.lastEat)))) + " minutes pour le prochain repas");
            return false;
        }
        this.removeEffect(983);

        for (FoodItem i : getAnimal().getFoodItems()) {
            if (food.getTemplateId() == i.getItemID()) {
                if (!this.eatedFoods.containsKey(food.getTemplateId())) {
                    this.eatedFoods.put(food.getTemplateId(), 1);
                } else
                    this.eatedFoods.put(food.getTemplateId(), this.eatedFoods.get(food.getTemplateId()) + 1);

                this.entity.pointsUsed += i.getPoint();
                this.boost(i.getStats(), i.getStatsPoints());
                updateFood(food.getTemplateId());
                this.updateDate();
                this.checkLastEffect();
                p.send(new ObjectModifiedMessage(this.getObjectItem()));
                this.save();
                PlayerController.sendServerMessage(p.getClient(), "Next meal in 60 minutes");
                return true;
            }
        }
        for (FoodItem i : getAnimal().getFoodTypes()) {
            if (food.getTemplate().getTypeId() == i.getItemID()) {
                if (!this.eatedFoodsType.containsKey(food.getTemplate().getTypeId())) {
                    this.eatedFoodsType.put(food.getTemplate().getTypeId(), 0);
                }
                this.eatedFoodsType.put(food.getTemplate().getTypeId(), this.eatedFoodsType.get(food.getTemplate().getTypeId()) + 1);
                this.entity.pointsUsed += i.getPoint();
                this.boost(i.getStats(), i.getStatsPoints());
                updateFood(food.getTemplateId());
                this.updateDate();
                this.checkLastEffect();
                p.send(new ObjectModifiedMessage(this.getObjectItem()));
                this.save();
                PlayerController.sendServerMessage(p.getClient(), "Next meal in 60 minutes");
                return true;
            }
        }

        return false;
    }

    public int getEatenFood(int id) {
        if (this.eatedFoods.containsKey(id)) {
            return this.eatedFoods.get(id);
        }
        return 0;
    }

    public void updateDate() {
        final Calendar now = Calendar.getInstance();
        if (this.effects.stream().anyMatch(e -> e.actionId == 808))
            ((ObjectEffectDate) this.getEffect(808)).SetDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR), now.get(Calendar.MINUTE));
        else
            this.effects.add(new ObjectEffectDate(808, now.get(Calendar.YEAR), (byte) now.get(Calendar.MONTH), (byte) now.get(Calendar.DAY_OF_MONTH), (byte) now.get(Calendar.HOUR), (byte) now.get(Calendar.MINUTE)));
    }

    public void updateFood(int food) {
        ((ObjectEffectInteger) this.getEffect(807)).value = food;
    }

    public void boost(int effectID, int count) {
        final ObjectEffect Effect = this.getEffect(effectID);
        if (Effect == null) {
            this.effects.add(new ObjectEffectInteger(effectID, count));
        } else {
            ((ObjectEffectInteger) Effect).value += count;
        }
        this.entity.lastEat = System.currentTimeMillis() + "";
        this.notifyColumn("effects");
    }

    public void save() {
        this.serializeInformations();
        DAO.getPetInventories().update(entity);
        if (this.isNeedRemove()) {
            DAO.getItems().delete(this, "character_items");
        } else if (this.isNeedInsert()) {
            DAO.getItems().create(this, false, "character_items");
        } else {
            DAO.getItems().save(this, false, "character_items");
        }
    }

    public void checkLastEffect() {

    }

    public void onMonsterFightWin() {

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void totalClear() {
        this.eatedFoods.clear();
        this.eatedFoods = null;
        this.eatedFoodsType.clear();
        this.eatedFoodsType = null;
        this.myInitialized = false;
        this.entity.totalClear();
        this.entity = null;
        super.totalClear();
    }

}
