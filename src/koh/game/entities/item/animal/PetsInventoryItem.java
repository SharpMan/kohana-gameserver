package koh.game.entities.item.animal;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.fighters.MonsterFighter;
import koh.protocol.client.enums.ItemsEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.inventory.InventoryWeightMessage;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectLadder;
import koh.protocol.types.game.data.items.effects.ObjectEffectDate;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.utils.SimpleLogger;
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
    public Map<Integer, Integer> eatenFoods = Collections.synchronizedMap(new HashMap<>());
    public Map<Integer, Integer> eatenFoodsType = Collections.synchronizedMap(new HashMap<>());

    public PetsInventoryItemEntity entity;

    protected boolean myInitialized = false;

    public PetsInventoryItem() {
        super();
    }

    public void serializeInformations() {
        IoBuffer buf = IoBuffer.allocate(0xFFF)
                .setAutoExpand(true);

        buf.putInt(eatenFoods.size());
        for (Entry<Integer, Integer> e : eatenFoods.entrySet()) {
            buf.putInt(e.getKey());
            buf.putInt(e.getValue());
        }

        buf.putInt(eatenFoodsType.size());
        for (Entry<Integer, Integer> e : eatenFoodsType.entrySet()) {
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
            this.eatenFoods.put(buf.getInt(), buf.getInt());
        }
        for (int i = 0; i < buf.getInt(); ++i) {
            this.eatenFoodsType.put(buf.getInt(), buf.getInt());
        }
        this.myInitialized = true;
    }

    public PetTemplate getAnimal() {
        return DAO.getItemTemplates().getPetTemplate(this.getTemplateId());
    }


    public synchronized void eat(Player p, MonsterFighter[] deadMobs){
        final PetTemplate pet = getAnimal();
        if (pet == null) {
            return;
        }  else if (this.entity.pointsUsed >= getAnimal().getHormone()) {
            return;
        }
        this.removeEffect(983);

        boolean needUpdate = false ,needRefresh = false;


        for(MonsterFighter mob : deadMobs){
            final Optional<ObjectEffectLadder> effectContainer = this.getEffects().stream()
                    .filter(e -> e instanceof ObjectEffectLadder && ((ObjectEffectLadder)e).monsterFamilyId == mob.getGrade().getMonsterId())
                    .map(e -> (ObjectEffectLadder) e)
                    .findFirst();
            if(effectContainer.isPresent()){
                final ObjectEffectLadder effect = effectContainer.get();
                effect.monsterCount += 1;
                this.notifyColumn("effects");
                needRefresh = true;
                for (MonsterBooster boost : pet.getMonsterBoosts()) {
                    if (boost.monsterFamily == effect.monsterFamilyId && effect.monsterCount % boost.deathNumber == 0) {
                        this.entity.pointsUsed += boost.getPoint();
                        for(int stat : boost.getStats()){
                            this.boost(stat, boost.getStatsBoost(stat));
                        }
                        needUpdate = true;
                    }
                }

            }
            if (this.entity.pointsUsed >= getAnimal().getHormone()) {
                break;
            }
        }

        if(needUpdate) {
            this.updateDate();
            this.save();
        }

        if(needRefresh){
            //this.checkLastEffect(pet);
            p.send(new ObjectModifiedMessage(this.getObjectItem()));
            p.send(new InventoryWeightMessage(p.getInventoryCache().getWeight(), p.getInventoryCache().getTotalWeight()));
        }


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

        for (FoodItem i : pet.getFoodItems()) {
            if (food.getTemplateId() == i.getItemID()) {
                if (!this.eatenFoods.containsKey(food.getTemplateId())) {
                    this.eatenFoods.put(food.getTemplateId(), 1);
                } else
                    this.eatenFoods.put(food.getTemplateId(), this.eatenFoods.get(food.getTemplateId()) + 1);

                this.entity.pointsUsed += i.getPoint();
                this.boost(i.getStats(), i.getStatsPoints());
                updateFood(food.getTemplateId());
                this.updateDate();
                this.checkLastEffect(pet,null);
                p.send(new ObjectModifiedMessage(this.getObjectItem()));
                this.save();
                PlayerController.sendServerMessage(p.getClient(), "Next meal in 60 minutes");
                return true;
            }
        }
        for (FoodItem i : pet.getFoodTypes()) {
            if (food.getTemplate().getTypeId() == i.getItemID()) {
                if (!this.eatenFoodsType.containsKey(food.getTemplate().getTypeId())) {
                    this.eatenFoodsType.put(food.getTemplate().getTypeId(), 0);
                }
                this.eatenFoodsType.put(food.getTemplate().getTypeId(), this.eatenFoodsType.get(food.getTemplate().getTypeId()) + 1);
                this.entity.pointsUsed += i.getPoint();
                this.boost(i.getStats(), i.getStatsPoints());
                updateFood(food.getTemplateId());
                this.updateDate();
                this.checkLastEffect(pet,null);
                p.send(new ObjectModifiedMessage(this.getObjectItem()));
                this.save();
                PlayerController.sendServerMessage(p.getClient(), "Next meal in 60 minutes");
                return true;
            }
        }

        return false;
    }

    public int getEatenFood(int id) {
        if (this.eatenFoods.containsKey(id)) {
            return this.eatenFoods.get(id);
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

    public void checkLastEffect(PetTemplate animal, Player p) {
        if(animal == null){
            return;
        }
        int hormone = 0;
        for (ObjectEffect effect : this.getEffects()) {
            //animal.gete

            final EffectInstanceDice parent =  animal.getEffectDice(effect.actionId);
            if(parent == null || !(effect instanceof ObjectEffectInteger))
                continue;
            final ObjectEffectInteger type = (ObjectEffectInteger) effect;
            final int biggestValue = parent.diceNum >= parent.diceSide ? parent.diceNum : parent.diceSide;
            final FoodItem item = Arrays.stream(this.getAnimal().getFoodItems())
                    .filter(e -> e.getStats() == effect.actionId)
                    .findFirst()
                    .orElse(
                            Arrays.stream(this.getAnimal().getFoodTypes())
                                    .filter(e -> e.getStats() == effect.actionId)
                                    .findFirst()
                            .orElse(null)
                    );
            if(item != null){
                hormone += (((float) ((ObjectEffectInteger) effect).value) / item.getStatsPoints()) * item.getStatsPoints();
            }
            if(type.value > biggestValue){
                try {
                    final SimpleLogger koliseoLog = new SimpleLogger("logs/koli/cheat_" + SimpleLogger.getCurrentDayStamp() + ".txt", 0);
                    koliseoLog.write(this.getTemplate().getNameId() +" gives "+type.value+" on "+ StatsEnum.valueOf(effect.actionId));
                    koliseoLog.newLine();
                    koliseoLog.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                type.value = biggestValue;
                if(p != null) {
                    p.getInventoryCache().safeDelete(this, this.getQuantity());
                    return;
                }
            }
        }
        if(hormone > getAnimal().getHormone()){
            final SimpleLogger koliseoLog = new SimpleLogger("logs/koli/cheat_" + SimpleLogger.getCurrentDayStamp() + ".txt", 0);
            this.effects.forEach(e -> {
                if(e instanceof ObjectEffectInteger)
                    koliseoLog.write(this.getTemplate().getNameId() +" gives "+(((float) ((ObjectEffectInteger) e).value)+" on "+ StatsEnum.valueOf(e.actionId)));
            });
            koliseoLog.newLine();
            koliseoLog.close();
            if(p != null) {
                p.getInventoryCache().safeDelete(this, this.getQuantity());
                return;
            }
        }
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void totalClear() {
        this.eatenFoods.clear();
        this.eatenFoods = null;
        this.eatenFoodsType.clear();
        this.eatenFoodsType = null;
        this.myInitialized = false;
        this.entity.totalClear();
        this.entity = null;
        super.totalClear();
    }

}
