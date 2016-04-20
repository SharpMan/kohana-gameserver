package koh.game.fights.AI;

import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.AI.actions.*;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.FightStateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Melancholia on 1/11/16.
 */
public abstract class AIAction {

    private static final Logger logger = LogManager.getLogger(AIAction.class);

    public static final Map<AIActionEnum, AIAction> AI_ACTIONS = new HashMap<AIActionEnum, AIAction>() {
        {
            this.put(AIActionEnum.SELF_ACTING, new SelfActingAction());

            this.put(AIActionEnum.BUFF_HIMSELF, new BuffHimselfAction());
            this.put(AIActionEnum.BUFF_ALLY, new BuffAllyAction());

            this.put(AIActionEnum.SUBBUFF, new SubBuffAction());

            this.put(AIActionEnum.HEAL_HIMSELF, new HealHimselfAction());
            this.put(AIActionEnum.HEAL_ALLY, new BuffAllyAction());

            this.put(AIActionEnum.ATTACK, new AttackAction());

            this.put(AIActionEnum.SUPPORT, new SupportAction());

            this.put(AIActionEnum.DEBUFF_ALLY, new DebuffAllyAction());
            this.put(AIActionEnum.DEBUFF_ENNEMY, new DebuffEnnemyAction());

            this.put(AIActionEnum.REPELS, new RepelsAction());
            this.put(AIActionEnum.INVOK, new InvokAction());
            this.put(AIActionEnum.MAD, new MadAction());
        }
    };

    private static boolean isGoodState(FightStateEnum state) {
        switch (state) {
            case ENRACINÉ:
            case INVISIBLE:
            case STATE_REFLECT_SPELL:
            case SAOUL:
            case CARRIED:
            case CARRIER: {
                return true;
            }

            case AFFAIBLI:
            case ALTRUISTE:
            case PESANTEUR:
            default: {
                return false;
            }
        }
    }

    protected abstract double scoreHeal(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse); //false all

    protected abstract double scoreBuff_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreBuff_II(AIProcessor AI, EffectInstanceDice Effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreBuff_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreDamage_0(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreDamage_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreDamage_II(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreDamage_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreDamagesPerPA(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreSubBuff_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreSubBuff_II(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreSubBuff_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreSubBuff_IV(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreAddStateGood(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreAddStateBad(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreRemStateGood(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreRemStateBad(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreDebuff(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    protected abstract double scoreInvocation(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean invokPreview);

    protected abstract double scoreInvocationStatic(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean invokPreview);

    protected abstract double scoreRepulse(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview, boolean isFear);

    protected abstract double scoreAttract(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview);

    protected abstract double scoreDeplace(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview, boolean isThrow);

    protected abstract double scoreExchangePlace(AIProcessor AI, short casterCell, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview);

    protected abstract double scoreUseLayer(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet);

    protected abstract double scoreLaunchSpell(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse);

    private static final ArrayList<String> effects = new ArrayList<>();

    public double getEffectScore(AIProcessor AI, short casterCell, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean invokPreview) {
       try {
           switch (effect.getEffectType()) {

               /*TODO TELEPORT_4* LIFE_PERCENT_ERODED_AIR* DISPELL_SPELL
               17:37:48.255 [pool-61-thread-45] ERROR fights.AI.AIAction -     Effect[DODGE] non defini pour l'IA
17:39:04.716 [pool-61-thread-38] ERROR fights.AI.AIAction -     Effect[DAMAGE_NEUTRAL_FIX] non defini pour l'IA
EROSION
TELEPORT_STARTING_TURN_CELL
               * */


               case HEAL:
               case HEAl_OR_DAMAGE:
               case HEAL_PDV_RENDUS:
               case Heal_PV:
               case PDV_PERCENT_REPORTED: //Not sure
                   return scoreHeal(AI, effect, targets, reverse);

               case DAMAGE_BECOME_HEAL:
                   return targets.contains(AI.getFighter()) ? (scoreBuff_I(AI, effect, targets, reverse, false)) : (-scoreBuff_I(AI, effect, targets, reverse, false));

             /* BUFFS LEVEL 1*/
               case MAXIMIZE_EFFECTS:
               case ADD_DAMAGE_PERCENT:
               case COMBO_DAMMAGES: //TODO roub
               case ADD_DAMAGE_PHYSIC:
               case WEAPON_DAMAGE_PERCENT:
               case ADD_PUSH_DAMAGES_BONUS:
               case ADD_CRITICAL_DAMAGES:
               case ADD_TACKLE_BLOCK:
               case ADD_TACKLE_EVADE:
               case STRENGTH:
               case CHANCE:
               case INTELLIGENCE:
               case AGILITY:
               case WISDOM:
                   return scoreBuff_I(AI, effect, targets, reverse, false);

               case INVISIBILITY:
                   return scoreBuff_II(AI, effect, targets, reverse, true);

             /* BUFFS LEVEL 2*/
               case MOVEMENT_POINTS:
               case ACTION_POINTS:
               case VITALITY:
               case ADD_VITALITY_PERCENT:
               case ADD_RANGE:
               case PROSPECTING:
               case ADD_RETRAIT_PA:
               case ADD_RETRAIT_PM:
               case ADD_CRITICAL_HIT:
               case ADD_DAMAGE_MAGIC:
               case ADD_HEAL_BONUS:
               case ALL_DAMAGES_BONUS:
               case ADD_SUMMON_LIMIT:
               case ADD_PHYSICAL_DAMAGE:
               case INCREASE_FINAL_DAMAGES_PERCENT:
                   return scoreBuff_II(AI, effect, targets, reverse, false);

             /* BUFFS LEVEL 3*/
               case DAMAGE_ARMOR_REDUCTION:
               case DAMMAGES_OCASSIONED:
               case ADD_CRITICAL_DAMAGES_REDUCTION:
               case ADD_PUSH_DAMAGES_REDUCTION:
               case BOOST_SHIELD_BASED_ON_CASTER_LIFE:
               case ADD_ALL_RESITANCES_PERCENT:
               case DAMAGE_RETURN:
               case NEUTRAL_ELEMENT_REDUCTION:
               case EARTH_ELEMENT_REDUCTION:
               case WATER_ELEMENT_REDUCTION:
               case AIR_ELEMENT_REDUCTION:
               case FIRE_ELEMENT_REDUCTION:
               case WATER_ELEMENT_RESIST_PERCENT:
               case EARTH_ELEMENT_RESIST_PERCENT:
               case AIR_ELEMENT_RESIST_PERCENT:
               case FIRE_ELEMENT_RESIST_PERCENT:
               case NEUTRAL_ELEMENT_RESIST_PERCENT:
               case SACRIFICE:
                   return scoreBuff_III(AI, effect, targets, reverse, false);

             /* DAMAGE LEVEL 3*/
               case STEAL_WATER:
               case STEAL_AIR:
               case STEAL_EARTH:
               case STEAL_FIRE:
               case STEAL_NEUTRAL:
               case PUNITION:
                   return scoreDamage_III(AI, effect, targets, reverse);

             /* DAMAGE LEVEL 2*/
               case STEAL_PV_FIX:
               case DAMAGE_LIFE_NEUTRE:
               case DAMAGE_LIFE_WATER:
               case DAMAGE_LIFE_TERRE:
               case DAMAGE_LIFE_AIR:
               case DAMAGE_LIFE_FEU:
               case DAMAGE_DROP_LIFE:
                   return scoreDamage_II(AI, effect, targets, reverse);


             /* DAMAGE LEVEL 1*/
               case DAMAGE_NEUTRAL:
               case DAMAGE_EARTH:
               case DAMAGE_AIR:
               case DAMAGE_FIRE:
               case DAMAGE_WATER:
               case DAMAGE_NEUTRAL_PER_PM_PERCENT:
               case DAMAGE_AIR_PER_PM_PERCENT:
               case DAMAGE_WATER_PER_PM_PERCENT:
               case DAMAGE_FIRE_PER_PM_PERCENT:
               case DAMAGE_EARTH_PER_PM_PERCENT:
               case LIFE_LEFT_TO_THE_ATTACKER_WATER_DAMAGES:
               case LIFE_LEFT_TO_THE_ATTACKER_AIR_DAMAGES:
               case LIFE_LEFT_TO_THE_ATTACKER_FIRE_DAMAGES:
               case LIFE_LEFT_TO_THE_ATTACKER_EARTH_DAMAGES:
               case LIFE_LEFT_TO_THE_ATTACKER_NEUTRAL_DAMAGES:
                   return scoreDamage_I(AI, effect, targets, reverse);

               case DAMAGE_TO_LAUNCHER:
                   return scoreDamage_0(AI, effect, targets, reverse);

             /* SUBBUFF LEVEL 4*/
               case SKIP_TURN:
               case KILL:
                   return scoreSubBuff_IV(AI, effect, targets, reverse, true);


            /* SUBBUFF LEVEL 3*/
               case STEAL_VITALITY:
               case STEAL_STRENGTH:
               case STEAL_INTELLIGENCE:
               case STEAL_AGILITY:
               case STEAL_WISDOM:
               case STEAL_CHANCE:
               case STEAL_PA:
               case STEAL_PM:
               case STEAL_RANGE:
               case SUB_PM:
               case SUB_PA:
               case SUB_RANGE:
               case SUB_WATER_RESIST_PERCENT:
               case SUB_EARTH_RESIST_PERCENT:
               case SUB_AIR_RESIST_PERCENT:
               case SUB_FIRE_RESIST_PERCENT:
               case SUB_NEUTRAL_RESIST_PERCENT:
               case SUB_ALl_RESISTANCES_PERCENT:
                   return scoreSubBuff_III(AI, effect, targets, reverse, false);



             /* SUBBUFF LEVEL 2*/
               case LOSE_PV_BY_USING_PA:
               case PA_USED_LOST_X_PDV_2:
               case PA_USED_LOST_X_PDV:
               case PM_USED_LOST_X_PDV:
                   return scoreDamagesPerPA(AI, effect, targets, reverse);

               case SUB_TACKLE_BLOCK:
               case SUB_TACKLE_EVADE:
               case SUB_CRITICAL_HIT:
               case SUB_HEAL_BONUS:
               case SUB_RETRAIT_PA:
               case SUB_RETRAIT_PM:
               case SUB_PROSPECTING:
               case DODGE_PM_LOST_PROBABILITY: //not the right place
               case DODGE_PA_LOST_PROBABILITY:
               case SUB_DAMAGE:
               case SUB_DAMAGE_BONUS_PERCENT:
               case SUB_PUSH_DAMAGES_BONUS:
               case SUB_PUSH_DAMAGES_REDUCTION:
               case SUB_CRITICAL_DAMAGES:
               case SUB_CRITICAL_DAMAGES_REDUCTION:
               case SUB_DODGE_PA_PROBABILITY:
               case SUB_DODGE_PM_PROBABILITY:
                   return scoreSubBuff_II(AI, effect, targets, reverse, false);

               case SUB_STRENGTH:
               case SUB_INTELLIGENCE:
               case SUB_CHANCE:
               case SUB_AGILITY:
               case SUB_WISDOM:
               case SUB_VITALITY:
               case SUB_PA_ESQUIVE_2:
               case SUB_PA_ESQUIVE:
               case SUB_PM_ESQUIVE_2:
               case SUB_PM_ESQUIVE:
               case SUB_FIRE_ELEMENT_REDUCTION:
               case SUB_EARTH_ELEMENT_REDUCTION:
               case SUB_WATER_ELEMENT_REDUCTION:
               case SUB_AIR_ELEMENT_REDUCTION:
               case SUB_NEUTRAL_ELEMENT_REDUCTION:
               case MINIMIZE_EFFECTS:
                   return scoreSubBuff_I(AI, effect, targets, reverse, false);


               //TODO SPELL_COOLDOWN if caster == target

               case CHANGE_LOOK:
               case EXPAND_SIZE:
               case CHANGE_APPEARANCE:
                   return scoreAddStateGood(AI, effect, targets, reverse);

               case ADD_STATE: {
                   if (isGoodState(FightStateEnum.valueOf(effect.value))) {
                       return scoreAddStateGood(AI, effect, targets, reverse);
                   } else {
                       return scoreAddStateBad(AI, effect, targets, reverse);
                   }
               }

               case REMOVE_STATE:
               case DISABLES_STATE: {
                   if (isGoodState(FightStateEnum.valueOf(effect.value))) {
                       return scoreRemStateGood(AI, effect, targets, reverse);
                   } else {
                       return scoreRemStateBad(AI, effect, targets, reverse);
                   }
               }

            /* PUSH BACK */
               case BACK_CELL:
               case PUSH_BACK:
               case PUSH_X_CELL:
                   return scoreRepulse(AI, castCell, effect, targets, invokPreview, false);
               case PUSH_FEAR:
                   return scoreRepulse(AI, castCell, effect, targets, invokPreview, true);

             /* ATTRACT */
               case PULL_FORWARD:
                   return scoreAttract(AI, castCell, effect, targets, invokPreview);

               //TODO ADVANCE_CELL

               case PORTER_TARGET:
                   return scoreDeplace(AI, castCell, effect, targets, invokPreview, false);
               case LAUNCHER_ENTITY:
                   return scoreDeplace(AI, castCell, effect, targets, invokPreview, true);

               case SWITCH_POSITIONS:
               case SWITCH_POSITION:
                   return scoreExchangePlace(AI, casterCell, castCell, effect, targets, invokPreview);

               case DESENVOUTEMENT:
               case DECREASE_EFFECT_DURATION:
                   return scoreDebuff(AI, effect, targets, reverse);

               case SUMMON_DOUBLE:
               case STATIC_SUMMON:
                   return scoreInvocationStatic(AI, effect, targets, reverse, invokPreview);
               case SUMMON:
               case ACTION_SUMMON_BOMB:
                   return scoreInvocation(AI, effect, targets, reverse, invokPreview);
               case KILL_TARGET_TO_REPLACE_INVOCATION:
                   return scoreSubBuff_IV(AI, effect, targets, reverse, true) + scoreInvocation(AI, effect, targets, reverse, invokPreview);

               case LAYING_BLYPH:
               case LAYING_GLYPH:
               case LAYING_GLYPH_RANKED:
                   return scoreUseLayer(AI, castCell, effect, targets, reverse, false);

               //LAYING PORTAIL Amakna dont use it in dopeul

               case NONE:
               case DO_NOTHING:
                   return 0;

               case TRANSKO:
               case POUTCH:
               case REFOULLAGE:
               case CAST_SPELL: // shoult not be there
                   return scoreLaunchSpell(AI, effect, targets, reverse);

               default: {
                   if(!effects.contains(effect.getEffectType().toString())) {
                       logger.error("Effect[{}] non defini pour l'IA", effect.getEffectType());
                       effects.add(effect.getEffectType().toString());
                   }
                   return scoreDamage_I(AI, effect, targets, reverse);
               }
           }
       }
       catch(Exception e){
           e.printStackTrace();
           logger.error("Stupid effect {}", effect.toString());
           return 0;
       }
    }

    public enum AIActionEnum {
        SELF_ACTING,//IA Automatique

        BUFF_HIMSELF,//Se buff en priorité
        BUFF_ALLY,///Buff les alliés en priorité

        SUBBUFF,//Ralenti les ennemis

        HEAL_HIMSELF,//Se soigne en priorité
        HEAL_ALLY,//Soigne les alliés en priorité

        ATTACK,//Attaque

        SUPPORT,//N'attaque pas mais soutient les alliés.

        DEBUFF_ALLY,//Debuff les alliés en priorité
        DEBUFF_ENNEMY,//Debuff les ennemis en priorité

        REPELS,//Repousse les ennemis.

        INVOK,//Invoque

        MAD,//Fou => Chaferfu
    }
}
