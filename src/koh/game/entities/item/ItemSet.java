package koh.game.entities.item;

import koh.game.Main;
import koh.game.dao.api.AccountDataDAO;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceCreature;
import koh.game.entities.spells.EffectInstanceDate;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.EffectInstanceInteger;
import koh.game.entities.spells.EffectInstanceLadder;
import koh.game.entities.spells.EffectInstanceMinMax;
import koh.game.entities.spells.EffectInstanceMount;
import koh.game.entities.spells.EffectInstanceString;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectCreature;
import koh.protocol.types.game.data.items.effects.ObjectEffectDate;
import koh.protocol.types.game.data.items.effects.ObjectEffectDice;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.protocol.types.game.data.items.effects.ObjectEffectLadder;
import koh.protocol.types.game.data.items.effects.ObjectEffectMinMax;
import koh.protocol.types.game.data.items.effects.ObjectEffectMount;
import koh.protocol.types.game.data.items.effects.ObjectEffectString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ItemSet {

    public int id;
    public int[] items;
    public boolean bonusIsSecret;
    public EffectInstance[][] effects; //Dice
    private GenericStats[] myStats;

    private static final Logger logger = LogManager.getLogger(ItemSet.class);

    //TODO: Create dofusMaps  ObjectEffect[] toObjectEffects
    //TODO@: se rappeller poruquoi je voulais faire sa

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private void parseStats() {
        StatsEnum Stat;
        this.myStats = new GenericStats[this.effects.length];
        for (int i = 0; i < this.effects.length; i++) {

            this.myStats[i] = new GenericStats();
            for (EffectInstance e : EffectHelper.generateIntegerEffectArray(this.effects[i], EffectGenerationType.Normal, false)) {
                if (e == null) {
                    continue;
                }
                if (e instanceof EffectInstanceInteger) {
                    Stat = StatsEnum.valueOf(e.effectId);
                    if (Stat == null) {
                        logger.error("Undefined Stat id {} ", e.effectId);
                        continue;
                    }
                    this.myStats[i].addItem(Stat, ((EffectInstanceInteger) e).value);
                }

            }
        }

        Stat = null;
    }

    public GenericStats getStats(int round) {
        try {
            if (this.myStats == null) {
                this.parseStats();
            }
            return myStats[round - 1];
        } catch (Exception e) {
            return null;
        }
    }

    public ObjectEffect[] toObjectEffects(int round) {
        try {
            ObjectEffect[] array = new ObjectEffect[effects[round - 1].length];
            for (int i = 0; i < array.length; ++i) {
                //EffectInstanceCreate 
                if (array[i] == null) {
                    return new ObjectEffect[0];
                }
                if (effects[round - 1][i] instanceof EffectInstanceLadder) {
                    array[i] = new ObjectEffectLadder((effects[round - 1][i]).effectId, ((EffectInstanceLadder) effects[round - 1][i]).monsterCount, ((EffectInstanceLadder) effects[round - 1][i]).monsterFamilyId);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceCreature) {
                    array[i] = new ObjectEffectCreature((effects[round - 1][i]).effectId, ((EffectInstanceCreature) effects[round - 1][i]).monsterFamilyId);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceMount) {
                    array[i] = new ObjectEffectMount((effects[round - 1][i]).effectId, ((EffectInstanceMount) effects[round - 1][i]).date, ((EffectInstanceMount) effects[round - 1][i]).modelId, ((EffectInstanceMount) effects[round - 1][i]).mountId);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceString) {
                    array[i] = new ObjectEffectString((effects[round - 1][i]).effectId, ((EffectInstanceString) effects[round - 1][i]).text);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceCreature) {
                    array[i] = new ObjectEffectCreature((effects[round - 1][i]).effectId, ((EffectInstanceCreature) effects[round - 1][i]).monsterFamilyId);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceMinMax) {
                    array[i] = new ObjectEffectMinMax((effects[round - 1][i]).effectId, ((EffectInstanceMinMax) effects[round - 1][i]).MinValue, ((EffectInstanceMinMax) effects[round - 1][i]).MaxValue);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceDate) {
                    array[i] = new ObjectEffectDate((effects[round - 1][i]).effectId, ((EffectInstanceDate) effects[round - 1][i]).Year, ((EffectInstanceDate) effects[round - 1][i]).Mounth, ((EffectInstanceDate) effects[round - 1][i]).Day, ((EffectInstanceDate) effects[round - 1][i]).Hour, ((EffectInstanceDate) effects[round - 1][i]).Minute);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceDice) {
                    array[i] = new ObjectEffectDice((effects[round - 1][i]).effectId, ((EffectInstanceDice) effects[round - 1][i]).diceNum, ((EffectInstanceDice) effects[round - 1][i]).diceSide, ((EffectInstanceInteger) effects[round - 1][i]).value);
                    continue;
                }
                if (effects[round - 1][i] instanceof EffectInstanceInteger) {
                    array[i] = new ObjectEffectInteger((effects[round - 1][i]).effectId, ((EffectInstanceInteger) effects[round - 1][i]).value);
                }

            }
            return array;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            return new ObjectEffect[0];
        }
    }

}
