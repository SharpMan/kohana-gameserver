package koh.game.fights.effects.buff;

import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.SpellIDEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public abstract class BuffEffect {

    public static final int[] EROSION_DAMAGE_EFFECTS_IDS = new int[]{1092, 1093, 1094, 1095, 1096};
    public static final int[] HEALING_EFFECTS_IDS = new int[]{81, 108, 1109, 90};
    public static final int[] IMMEDIATE_BOOST_EFFECTS_IDS = new int[]{266, 268, 269, 271, 414};
    public static final int[] BOMB_SPELLS_IDS = new int[]{2796, 2797, 2808};
    public static final int[] SPLASH_EFFECTS_IDS = new int[]{1123, 1124, 1125, 1126, 1127, 1128};
    public static final int[] MP_BASED_DAMAGE_EFFECTS_IDS = new int[]{1012, 1013, 1014, 1015, 1016};
    public static final int[] HP_BASED_DAMAGE_EFFECTS_IDS = new int[]{672, 85, 86, 87, 88, 89};
    public static final int[] TARGET_HP_BASED_DAMAGE_EFFECTS_IDS = new int[]{1067, 1068, 1069, 1070, 1071};
    public static final int[] TRIGGERED_EFFECTS_IDS = new int[]{138, 1040};
    public static final int[] NO_BOOST_EFFECTS_IDS = new int[]{144, 82};

    protected static final Logger logger = LogManager.getLogger(BuffEffect.class);

    public BuffDecrementType decrementType;
    public BuffActiveType activeType;
    @Getter
    public EffectCast castInfos;
    public Fighter caster;
    public Fighter target;
    public int duration, delay;
    private int uid = -1;

    public int getId() {
        if (this.uid == -1) {
            uid = Fight.POSIBLE ? target.getFight().getNextBuffUid().incrementAndGet() : target.getNextBuffUid().incrementAndGet();
        }
        return this.uid;
    }

    //TODO: Create List in Setting
    public boolean isDebuffable() {
        if(this.castInfos.spellId == 103 || castInfos.spellId == 59 || castInfos.spellId == 686){
            return false;
        }
        logger.debug("Checkinf if buff {} is debuffable",this.castInfos.effectType);
        switch (this.castInfos.effectType) {
            case DAMAGE_ARMOR_REDUCTION:
                return castInfos.spellId != SpellIDEnum.TREVE;
           // case ADD_STATE:
            case CHANGE_APPEARANCE:
            case SACRIFICE:
            case CHATIMENT:
            case ENDS_ROUND:
            //Domage de sort
            case SUB_PA_ESQUIVE_2: //xelor
            case TRANSFORMATION:
                return false;
        }
        return this.castInfos.subEffect != StatsEnum.NOT_DISPELLABLE;

        //return true;
    }

    public BuffEffect(EffectCast CastInfos, Fighter target, BuffActiveType activeType, BuffDecrementType decrementType) {
        this.castInfos = CastInfos;

        //this.duration = (castInfos.duration == -1) ? -1 : (target.fight.currentFighter == target /*&& castInfos.duration == 0*/ ? castInfos.duration + 1 : castInfos.duration) - castInfos.getDelay();
        this.duration = (CastInfos.duration == -1) ? -1 : (decrementType == BuffDecrementType.TYPE_ENDTURN ? CastInfos.duration : (CastInfos.duration) - CastInfos.getDelay());
        //Why do i use this
       /* if (decrementType == BuffDecrementType.TYPE_ENDTURN && target.getID() == CastInfos.caster.getID() && this.duration != -1) {
            this.duration++;
        }*/
        this.delay = CastInfos.getDelay();
        this.caster = CastInfos.caster;
        this.target = target;

        this.activeType = activeType;
        this.decrementType = decrementType;
    }

    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        return this.target.tryDie(this.caster.getID());
    }

    public abstract AbstractFightDispellableEffect getAbstractFightDispellableEffect();

    /// <summary>
    /// Fin du buff
    /// </summary>
    /// <returns></returns>
    public int removeEffect() {
        return this.target.tryDie(this.caster.getID());
    }

    /// <summary>
    /// decrement le buff
    /// </summary>
    public int decrementDuration() {
        this.duration--;

        this.castInfos.fakeValue = 0;

        return this.duration;
    }

    public int decrementDuration(int Duration) {
        this.duration -= Duration;

        this.castInfos.fakeValue = 0;

        return this.duration;
    }

}
