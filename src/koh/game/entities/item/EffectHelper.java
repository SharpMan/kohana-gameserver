package koh.game.entities.item;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import koh.game.Logs;
import koh.game.Main;
import koh.game.entities.spells.*;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.*;
import koh.utils.Couple;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectHelper {

    public static int[] DateEffect = new int[]{805, 808, 983, 998}; //971
    public static final int DAMAGE_EFFECT_CATEGORY = 2;
    public static int[] MonsterEffect = new int[]{185, 621, 1011, 905};
    public static int[] LadderEffects = new int[]{717};
    public static int[] LivingObjectEffect = new int[]{973, 971, 972, 974};
    public static int[] unRandomablesEffects = new int[]{
        96,//Effect_DamageWater
        97,//Effect_DamageEarth
        98,//Effect_DamageAir
        99,//Effect_DamageFire
        100,//Effect_DamageNeutral
        91,//Effect_StealHPWater
        92,//Effect_StealHPEarth
        93,//Effect_StealHPAir
        94,//Effect_StealHPFire
        95,//Effect_StealHPNeutral
        101//Effect_RemoveAP
    };

    public static IoBuffer SerializeEffectInstanceDice(EffectInstance[] Effects) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(Effects.length);
        for (EffectInstance e : Effects) {
            buff.put(e.SerializationIdentifier());
            e.toBinary(buff);
        }

        buff.flip();
        return buff;
    }

    public static int RandomValue(Couple<Integer, Integer> Couple) {
        return RandomValue(Couple.first, Couple.second);
    }

    public static int RandomValue(int i1, int i2) {
        Random rand = new Random();
        return rand.nextInt(i2 - i1 + 1) + i1;
    }

    public static ObjectEffect[] toObjectEffects(EffectInstance[] effects) {
        ObjectEffect[] array = new ObjectEffect[effects.length];
        for (int i = 0; i < array.length; ++i) {
            //EffectInstanceCreate 
            if (effects[i] instanceof EffectInstanceLadder) {
                array[i] = new ObjectEffectLadder((effects[i]).effectId, ((EffectInstanceLadder) effects[i]).monsterCount, ((EffectInstanceLadder) effects[i]).monsterFamilyId);
                continue;
            }
            if (effects[i] instanceof EffectInstanceCreature) {
                array[i] = new ObjectEffectCreature((effects[i]).effectId, ((EffectInstanceCreature) effects[i]).monsterFamilyId);
                continue;
            }
            if (effects[i] instanceof EffectInstanceMount) {
                array[i] = new ObjectEffectMount((effects[i]).effectId, ((EffectInstanceMount) effects[i]).date, ((EffectInstanceMount) effects[i]).modelId, ((EffectInstanceMount) effects[i]).mountId);
                continue;
            }
            if (effects[i] instanceof EffectInstanceString) {
                array[i] = new ObjectEffectString((effects[i]).effectId, ((EffectInstanceString) effects[i]).text);
                continue;
            }
            if (effects[i] instanceof EffectInstanceCreature) {
                array[i] = new ObjectEffectCreature((effects[i]).effectId, ((EffectInstanceCreature) effects[i]).monsterFamilyId);
                continue;
            }
            if (effects[i] instanceof EffectInstanceMinMax) {
                array[i] = new ObjectEffectMinMax((effects[i]).effectId, ((EffectInstanceMinMax) effects[i]).MinValue, ((EffectInstanceMinMax) effects[i]).MaxValue);
                continue;
            }
            if (effects[i] instanceof EffectInstanceDate) {
                array[i] = new ObjectEffectDate((effects[i]).effectId, ((EffectInstanceDate) effects[i]).Year, ((EffectInstanceDate) effects[i]).Mounth, ((EffectInstanceDate) effects[i]).Day, ((EffectInstanceDate) effects[i]).Hour, ((EffectInstanceDate) effects[i]).Minute);
                continue;
            }
            if (effects[i] instanceof EffectInstanceDice) {
                array[i] = new ObjectEffectDice((effects[i]).effectId, ((EffectInstanceDice) effects[i]).diceNum, ((EffectInstanceDice) effects[i]).diceSide, ((EffectInstanceInteger) effects[i]).value);
                continue;
            }
            if (effects[i] instanceof EffectInstanceInteger) {
                array[i] = new ObjectEffectInteger((effects[i]).effectId, ((EffectInstanceInteger) effects[i]).value);
            }

        }
        return array;
    }

    public static EffectInstance[] GenerateIntegerEffectArray(EffectInstance[] possibleEffects, EffectGenerationType GenType, boolean isWeapon) {
        EffectInstance[] Effects = new EffectInstance[possibleEffects.length];
        int i = 0;
        for (EffectInstance e : possibleEffects) {
            if (e instanceof EffectInstanceDice) {
                if (isWeapon && ArrayUtils.contains(unRandomablesEffects, e.effectId)) {
                    Effects[i] = e;
                    continue;
                }

                if (ArrayUtils.contains(DateEffect, e.effectId)) {
                    Calendar now = Calendar.getInstance();
                    Effects[i] = (new EffectInstanceDate(e, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR), now.get(Calendar.MINUTE)));
                    continue;
                }
                if (ArrayUtils.contains(MonsterEffect, e.effectId)) {
                    Effects[i] = new EffectInstanceCreature(e, ((EffectInstanceDice) e).diceNum);
                    continue;
                }
                if (ArrayUtils.contains(LadderEffects, e.effectId)) {
                    Effects[i] = new EffectInstanceLadder(e, ((EffectInstanceDice) e).diceNum, 0);
                    continue;
                }

                short num1 = (short) ((int) ((EffectInstanceDice) e).diceNum >= (int) ((EffectInstanceDice) e).diceSide ? ((EffectInstanceDice) e).diceNum : ((EffectInstanceDice) e).diceSide);
                short num2 = (short) ((int) ((EffectInstanceDice) e).diceNum <= (int) ((EffectInstanceDice) e).diceSide ? ((EffectInstanceDice) e).diceNum : ((EffectInstanceDice) e).diceSide);
                if (GenType == EffectGenerationType.MaxEffects) {
                    Effects[i] = (new EffectInstanceInteger(e, !e.Template().operator.equalsIgnoreCase("-") ? num1 : num2));
                    continue;
                }
                if (GenType == EffectGenerationType.MinEffects) {
                    Effects[i] = (new EffectInstanceInteger(e, !e.Template().operator.equalsIgnoreCase("-") ? num2 : num1));
                    continue;
                }
                if ((int) num2 == 0) {
                    Effects[i] = (new EffectInstanceInteger(e, num1));
                } else {
                    Effects[i] = (new EffectInstanceInteger(e, (short) RandomValue((int) num2, (int) num1 + 1)));
                }
            } else if (e != null) {
                throw new Error("Effect not suport" + e.SerializationIdentifier());
            }
            i++;
        }
        return Effects;
    }

    public static List<ObjectEffect> GenerateIntegerEffect(EffectInstance[] possibleEffects, EffectGenerationType GenType, boolean isWeapon) {
        List<ObjectEffect> Effects = new ArrayList<>();
        for (EffectInstance e : possibleEffects) {
            if (e instanceof EffectInstanceDice) {
                Main.Logs().writeDebug(e.toString());
                if (isWeapon && ArrayUtils.contains(unRandomablesEffects, e.effectId)) {
                    Effects.add(new ObjectEffectDice(e.effectId, ((EffectInstanceDice) e).diceNum, ((EffectInstanceDice) e).diceSide, ((EffectInstanceDice) e).value));
                    continue;
                }
                if (ArrayUtils.contains(LivingObjectEffect, e.effectId)) {
                    Effects.add(new ObjectEffectInteger(e.effectId, ((EffectInstanceDice) e).value));
                    continue;
                }
                if (ArrayUtils.contains(DateEffect, e.effectId)) {
                    Calendar now = Calendar.getInstance();
                    Effects.add(new ObjectEffectDate(e.effectId, now.get(Calendar.YEAR), (byte) now.get(Calendar.MONTH), (byte) now.get(Calendar.DAY_OF_MONTH), (byte) now.get(Calendar.HOUR_OF_DAY), (byte) now.get(Calendar.MINUTE)));
                    continue;
                }
                if (ArrayUtils.contains(MonsterEffect, e.effectId)) {
                    Effects.add(new ObjectEffectCreature(e.effectId, ((EffectInstanceDice) e).diceNum));
                    continue;
                }
                if (ArrayUtils.contains(LadderEffects, e.effectId)) {
                    Effects.add(new ObjectEffectLadder(e.effectId, ((EffectInstanceDice) e).diceNum, 0));
                    continue;
                }

                short num1 = (short) ((int) ((EffectInstanceDice) e).diceNum >= (int) ((EffectInstanceDice) e).diceSide ? ((EffectInstanceDice) e).diceNum : ((EffectInstanceDice) e).diceSide);
                short num2 = (short) ((int) ((EffectInstanceDice) e).diceNum <= (int) ((EffectInstanceDice) e).diceSide ? ((EffectInstanceDice) e).diceNum : ((EffectInstanceDice) e).diceSide);
                if (GenType == EffectGenerationType.MaxEffects) {
                    Effects.add(new ObjectEffectInteger(e.effectId, !e.Template().operator.equalsIgnoreCase("-") ? num1 : num2));
                    continue;
                }
                if (GenType == EffectGenerationType.MinEffects) {
                    Effects.add(new ObjectEffectInteger(e.effectId, !e.Template().operator.equalsIgnoreCase("-") ? num2 : num1));
                    continue;
                }
                if ((int) num2 == 0) {
                    Effects.add(new ObjectEffectInteger(e.effectId, num1));
                } else {
                    Effects.add(new ObjectEffectInteger(e.effectId, (short) RandomValue((int) num2, (int) num1 + 1)));
                }
            } else {
                throw new Error("Effect not suport" + e.toString());
            }
        }
        return Effects;
    }
    
    public static ObjectEffect[] ObjectEffects(List<EffectInstance> effects) {
        ObjectEffect[] array = new ObjectEffect[effects.size()];
        for (int i = 0; i < array.length; ++i) {
            //EffectInstanceCreate 
            if (effects.get(i) instanceof EffectInstanceDuration) {
                array[i] = new ObjectEffectDuration((effects.get(i)).effectId, ((EffectInstanceDuration) effects.get(i)).days, ((EffectInstanceDuration) effects.get(i)).hours, ((EffectInstanceDuration) effects.get(i)).minutes);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceLadder) {
                array[i] = new ObjectEffectLadder((effects.get(i)).effectId, ((EffectInstanceLadder) effects.get(i)).monsterFamilyId, ((EffectInstanceLadder) effects.get(i)).monsterCount);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceCreature) {
                array[i] = new ObjectEffectCreature((effects.get(i)).effectId, ((EffectInstanceCreature) effects.get(i)).monsterFamilyId);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceMount) {
                array[i] = new ObjectEffectMount((effects.get(i)).effectId, ((EffectInstanceMount) effects.get(i)).date, ((EffectInstanceMount) effects.get(i)).modelId, ((EffectInstanceMount) effects.get(i)).mountId);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceString) {
                array[i] = new ObjectEffectString((effects.get(i)).effectId, ((EffectInstanceString) effects.get(i)).text);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceMinMax) {
                array[i] = new ObjectEffectMinMax((effects.get(i)).effectId, ((EffectInstanceMinMax) effects.get(i)).MinValue, ((EffectInstanceMinMax) effects.get(i)).MaxValue);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceDate) {
                array[i] = new ObjectEffectDate((effects.get(i)).effectId, ((EffectInstanceDate) effects.get(i)).Year, ((EffectInstanceDate) effects.get(i)).Mounth, ((EffectInstanceDate) effects.get(i)).Day, ((EffectInstanceDate) effects.get(i)).Hour, ((EffectInstanceDate) effects.get(i)).Minute);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceDice) {
                array[i] = new ObjectEffectDice((effects.get(i)).effectId, ((EffectInstanceDice) effects.get(i)).diceNum, ((EffectInstanceDice) effects.get(i)).diceSide, ((EffectInstanceInteger) effects.get(i)).value);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceInteger) {
                array[i] = new ObjectEffectInteger((effects.get(i)).effectId, ((EffectInstanceInteger) effects.get(i)).value);
            }

        }
        return array;
    }

}
