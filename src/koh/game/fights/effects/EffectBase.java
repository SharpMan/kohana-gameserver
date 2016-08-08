package koh.game.fights.effects;

import java.util.HashMap;

import koh.protocol.client.enums.StatsEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public abstract class EffectBase {

    private static final Logger logger = LogManager.getLogger(EffectBase.class);
    //TODO ADD_PA_BIS + IA

    //TODO effect 406
    private static final HashMap<StatsEnum, EffectBase> EFFECTS = new HashMap<StatsEnum, EffectBase>() {
        {
            this.put(StatsEnum.DAMAGE_NEUTRAL, new EffectDamage());
            this.put(StatsEnum.DAMAGE_EARTH, new EffectDamage());
            this.put(StatsEnum.DAMAGE_AIR, new EffectDamage());
            this.put(StatsEnum.DAMAGE_FIRE, new EffectDamage());
            this.put(StatsEnum.DAMAGE_WATER, new EffectDamage());

            //Xelor
            this.put(StatsEnum.TELEPORT_PREVIOUS_POSITION, new EffectTpPrevPos());
            this.put(StatsEnum.TELEPORT_STARTING_TURN_CELL, new EffectTpFirstPos());
            this.put(StatsEnum.TELEPORT_SYMETRIC_FROM_CASTER, new EffectTeleportSymetricFromCaster());
            this.put(StatsEnum.TELEPORT_SYMETRIC_MYSELF, new EffectTeleportSymetricMySelf());
            this.put(StatsEnum.TELEPORT_SYMETRIC_FROM_IMPACT_CELL, new EffectTeleportSymetricImpactcell());

            //Téleportation
            this.put(StatsEnum.TELEPORT_4, new EffectTeleport());
            this.put(StatsEnum.TELEPORT, new EffectTeleport());

            //Déclanche les Glyphe
            this.put(StatsEnum.TRIGGERS_GLYPHS, new EffectTriggersGlyph());

            //Echange de places
            this.put(StatsEnum.SWITCH_POSITIONS, new EffectTranspose());
            this.put(StatsEnum.SWITCH_POSITION, new EffectTranspose());
            this.put(StatsEnum.SACRIFICE, new EffectSacrifice());

            // Effet de push back/fear
            this.put(StatsEnum.PUSH_X_CELL, new EffectPush());
            this.put(StatsEnum.PUSH_BACK, new EffectPush());
            this.put(StatsEnum.PUSH_FEAR, new EffectPushFear());
            this.put(StatsEnum.PULL_FORWARD, new EffectPush());
            this.put(StatsEnum.DODGE, new EffectDodge());


            //Effet de IOP
            this.put(StatsEnum.POUTCH, new EffectPoutch());
            this.put(StatsEnum.DISTRIBUTES_DAMAGES_OCCASIONED, new EffectDistributesDamagesOccasioned());
            this.put(StatsEnum.LIFE_PERCENT_ERODED_NEUTRAL, new EffectLifeEroded());
            this.put(StatsEnum.LIFE_PERCENT_ERODED_AIR, new EffectLifeEroded());
            this.put(StatsEnum.LIFE_PERCENT_ERODED_FIRE, new EffectLifeEroded());
            this.put(StatsEnum.LIFE_PERCENT_ERODED_WATER, new EffectLifeEroded());
            this.put(StatsEnum.LIFE_PERCENT_ERODED_EARTH, new EffectLifeEroded());

            this.put(StatsEnum.PUNITION, new EffectPunishment());
            this.put(StatsEnum.LAYING_GLYPH_RANKED, new EffectActivableObject());
            this.put(StatsEnum.LAYING_BLYPH, new EffectActivableObject());
            this.put(StatsEnum.LAYING_GLYPH, new EffectActivableObject());
            this.put(StatsEnum.LAYING_TRAP_LEVEL, new EffectActivableObject());
            this.put(StatsEnum.REVEALS_INVISIBLE, new EffectPerception());
            this.put(StatsEnum.LAYING_PORTAIL, new EffectActivableObject());

            // 1 PA,PM Utilisé = X Pdv perdu
            this.put(StatsEnum.LOSE_PV_BY_USING_PA, new EffectDamagePerPA());
            this.put(StatsEnum.PA_USED_LOST_X_PDV_2, new EffectDamagePerPA());
            this.put(StatsEnum.PA_USED_LOST_X_PDV, new EffectDamagePerPA());
            this.put(StatsEnum.PM_USED_LOST_X_PDV, new EffectDamagePerPM());


            this.put(StatsEnum.CHANGE_APPEARANCE, new EffectSkin());
            this.put(StatsEnum.CHANGE_LOOK, new EffectSkin());

            //CoolDown
            this.put(StatsEnum.SPELL_COOLDOWN, new EffectSpellCoolDown());

            //Soin
            this.put(StatsEnum.HEAL, new EffectHeal());

            // Augmente de X les domamges de base du sort Y
            this.put(StatsEnum.ADD_BASE_DAMAGE_SPELL, new EffectIncreaseSpellJet());

            // Ajout d'un etat / changement de skin
            this.put(StatsEnum.INVISIBILITY, new EffectAddState());
            this.put(StatsEnum.ADD_STATE, new EffectAddState());
            this.put(StatsEnum.REMOVE_STATE, new EffectLostState());

            this.put(StatsEnum.STEAL_NEUTRAL, new EffectLifeSteal());
            this.put(StatsEnum.STEAL_EARTH, new EffectLifeSteal());
            this.put(StatsEnum.STEAL_AIR, new EffectLifeSteal());
            this.put(StatsEnum.STEAL_FIRE, new EffectLifeSteal());
            this.put(StatsEnum.STEAL_WATER, new EffectLifeSteal());
            this.put(StatsEnum.STEAL_PV_FIX, new EffectLifeSteal());

            this.put(StatsEnum.SUB_VITALITY_PERCENT, new EffectSubLifePercent());

            this.put(StatsEnum.REFLECT_SPELL, new EffectReflectSpell());
            this.put(StatsEnum.DESENVOUTEMENT, new EffectDebuff());
            this.put(StatsEnum.DECREASE_EFFECT_DURATION, new EffectDispellEffectDuration());

            // Vol de statistique
            this.put(StatsEnum.STEAL_VITALITY, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_STRENGTH, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_INTELLIGENCE, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_AGILITY, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_WISDOM, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_CHANCE, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_PA, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_PM, new EffectStatsSteal());
            this.put(StatsEnum.STEAL_RANGE, new EffectStatsSteal());

            // Armure et bouclié feca
            this.put(StatsEnum.DAMAGE_ARMOR_REDUCTION, new EffectReduceDamage());
            this.put(StatsEnum.DAMAGE_REDUCTION, new EffectArmor());

            //chance Eca
            this.put(StatsEnum.DAMAGE_BECOME_HEAL, new EffectDamageBecomeHeal());

            //HEAL
            this.put(StatsEnum.PDV_PERCENT_REPORTED, new EffectHealPercent());

            //Coffre au tresor
            this.put(StatsEnum.LIFE_LEFT_TO_THE_ATTACKER_WATER_DAMAGES, new EffectDamageBasedLifeLeft());
            this.put(StatsEnum.LIFE_LEFT_TO_THE_ATTACKER_FIRE_DAMAGES, new EffectDamageBasedLifeLeft());
            this.put(StatsEnum.LIFE_LEFT_TO_THE_ATTACKER_AIR_DAMAGES, new EffectDamageBasedLifeLeft());
            this.put(StatsEnum.LIFE_LEFT_TO_THE_ATTACKER_NEUTRAL_DAMAGES, new EffectDamageBasedLifeLeft());
            this.put(StatsEnum.LIFE_LEFT_TO_THE_ATTACKER_EARTH_DAMAGES, new EffectDamageBasedLifeLeft());

            //AddPdVPercent 
            this.put(StatsEnum.ADD_VITALITY_PERCENT, new EffectVitalityPercent());

            //Lose LifePer
            this.put(StatsEnum.DAMAGE_LIFE_NEUTRE, new EffectLifeDamage());
            this.put(StatsEnum.DAMAGE_LIFE_WATER, new EffectLifeDamage());
            this.put(StatsEnum.DAMAGE_LIFE_TERRE, new EffectLifeDamage());
            this.put(StatsEnum.DAMAGE_LIFE_AIR, new EffectLifeDamage());
            this.put(StatsEnum.DAMAGE_LIFE_FEU, new EffectLifeDamage());
            this.put(StatsEnum.DAMAGE_DROP_LIFE, new EffectDamageDropLife());

            //Ajout ou reduction PA/PM/PO/Dommages

            this.put(StatsEnum.ACTION_POINTS, new EffectStats());
            this.put(StatsEnum.MOVEMENT_POINTS, new EffectStats());
            this.put(StatsEnum.SUB_PM, new EffectStats());
            this.put(StatsEnum.SUB_PA, new EffectStats());
            this.put(StatsEnum.SUB_RANGE, new EffectStats());
            this.put(StatsEnum.ADD_RANGE, new EffectStats());
            this.put(StatsEnum.DODGE_PA_LOST_PROBABILITY, new EffectStats());
            this.put(StatsEnum.DODGE_PM_LOST_PROBABILITY, new EffectStats());
            this.put(StatsEnum.SUB_DODGE_PA_PROBABILITY, new EffectStats());
            this.put(StatsEnum.SUB_DODGE_PM_PROBABILITY, new EffectStats());
            this.put(StatsEnum.ADD_DAMAGE_PHYSIC, new EffectStats());
            this.put(StatsEnum.SPELL_POWER, new EffectStats());

            this.put(StatsEnum.SUB_PA_ESQUIVE_2, new EffectSubPAEsquive());
            this.put(StatsEnum.SUB_PA_ESQUIVE, new EffectSubPAEsquive());
            this.put(StatsEnum.SUB_PM_ESQUIVE_2, new EffectSubPMEsquive());
            this.put(StatsEnum.SUB_PM_ESQUIVE, new EffectSubPMEsquive());

            //DommageSubis
            this.put(StatsEnum.DAMMAGES_OCASSIONED, new EffectDammageOcassioned());
            this.put(StatsEnum.REDUCE_FINAl_DAMAGE_PERCENT, new EffectDamagePercentReduced());
            this.put(StatsEnum.INCREASE_FINAL_DAMAGES_PERCENT, new EffectIncreaseFinalDamages());
            //this.put(StatsEnum.SPELL_POWER, new EffectSpellPower());



            //Enleve les effets du sort
            this.put(StatsEnum.DISPELL_SPELL, new EffectDispellSpell());

            // Chatiment sacris
            this.put(StatsEnum.CHATIMENT, new EffectChatiment());
            this.put(StatsEnum.EROSION, new EffectErosion());

            
            //Corruption
            this.put(StatsEnum.SKIP_TURN, new EffectEndTurn());
            
            //Caracteristiques Ajout/Reduction
            this.put(StatsEnum.STRENGTH, new EffectStats());
            this.put(StatsEnum.INTELLIGENCE, new EffectStats());
            this.put(StatsEnum.CHANCE, new EffectStats());
            this.put(StatsEnum.WISDOM, new EffectStats());
            this.put(StatsEnum.AGILITY, new EffectStats());
            this.put(StatsEnum.VITALITY, new EffectStats());
            this.put(StatsEnum.ADD_VIE, new EffectStats());

            this.put(StatsEnum.SUB_STRENGTH, new EffectStats());
            this.put(StatsEnum.SUB_INTELLIGENCE, new EffectStats());
            this.put(StatsEnum.SUB_CHANCE, new EffectStats());
            this.put(StatsEnum.SUB_AGILITY, new EffectStats());
            this.put(StatsEnum.SUB_WISDOM, new EffectStats());
            this.put(StatsEnum.SUB_VITALITY, new EffectStats());

            this.put(StatsEnum.PROSPECTING, new EffectStats());
            this.put(StatsEnum.ADD_DAMAGE_MAGIC, new EffectStats());
            this.put(StatsEnum.WEAPON_DAMAGE_PERCENT, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_BONUS, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_CRITICAL_DAMAGES, new EffectStats());
            this.put(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_PROSPECTING, new EffectStats());
            this.put(StatsEnum.SUB_DAMAGE, new EffectStats());
            this.put(StatsEnum.SUB_DAMAGE_BONUS_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_PUSH_DAMAGES_BONUS, new EffectStats());
            this.put(StatsEnum.SUB_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_CRITICAL_DAMAGES, new EffectStats());
            this.put(StatsEnum.SUB_CRITICAL_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_CRITICAL_DAMAGES, new EffectStats());
            this.put(StatsEnum.SUB_CRITICAL_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_RETRAIT_PA, new EffectStats());
            this.put(StatsEnum.ADD_RETRAIT_PM, new EffectStats());
            this.put(StatsEnum.SUB_RETRAIT_PA, new EffectStats());
            this.put(StatsEnum.SUB_RETRAIT_PM, new EffectStats());
            this.put(StatsEnum.WEAPON_DAMAGES_BONUS_PERCENT, new EffectStats());

            //Soins
            this.put(StatsEnum.ADD_HEAL_BONUS, new EffectStats());
            this.put(StatsEnum.SUB_HEAL_BONUS, new EffectStats());

            //Resistances ajout/suppressions
            this.put(StatsEnum.NEUTRAL_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.EARTH_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.WATER_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.AIR_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.FIRE_ELEMENT_REDUCTION, new EffectStats());

            this.put(StatsEnum.WATER_ELEMENT_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.AIR_ELEMENT_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT, new EffectStats());

            this.put(StatsEnum.SUB_FIRE_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_EARTH_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_WATER_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_AIR_ELEMENT_REDUCTION, new EffectStats());
            this.put(StatsEnum.SUB_NEUTRAL_ELEMENT_REDUCTION, new EffectStats());

            this.put(StatsEnum.ADD_MAGIC_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_PHYSICAL_REDUCTION, new EffectStats());

            this.put(StatsEnum.SUB_WATER_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_EARTH_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_AIR_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_FIRE_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_NEUTRAL_RESIST_PERCENT, new EffectStats());

            //Ajout ou reduction de dommage
            this.put(StatsEnum.ADD_CRITICAL_HIT, new EffectStats());
            this.put(StatsEnum.ALL_DAMAGES_BONUS, new EffectStats());
            this.put(StatsEnum.CRITICAL_MISS1, new EffectStats());
            this.put(StatsEnum.DAMAGE_RETURN, new EffectStats()); //TODO: attackAfterJet so can handle triger
            this.put(StatsEnum.SUB_CRITICAL_HIT, new EffectStats());
            this.put(StatsEnum.ADD_GLOBAL_DAMAGE_REDUCTION, new EffectStats());
            this.put(StatsEnum.WEAPON_DAMAGE_PERCENT, new EffectStats());
            this.put(StatsEnum.ADD_DAMAGE_PERCENT, new EffectStats());
            this.put(StatsEnum.ADD_SUMMON_LIMIT, new EffectStats());
            this.put(StatsEnum.ADD_DAMAGE_MAGIC, new EffectStats());
            this.put(StatsEnum.ADD_PHYSICAL_DAMAGE, new EffectStats());
            this.put(StatsEnum.ADD_TACKLE_BLOCK, new EffectStats());
            this.put(StatsEnum.ADD_TACKLE_EVADE, new EffectStats());
            this.put(StatsEnum.SUB_TACKLE_BLOCK, new EffectStats());
            this.put(StatsEnum.SUB_TACKLE_EVADE, new EffectStats());

            //Panda
            this.put(StatsEnum.PORTER_TARGET, new EffectPorter());
            this.put(StatsEnum.LAUNCHER_ENTITY, new EffectLancer());

            this.put(StatsEnum.KILL, new EffectDieFighter());

            //AlterJet
            this.put(StatsEnum.MAXIMIZE_EFFECTS, new EffectAlterJet());
            this.put(StatsEnum.MINIMIZE_EFFECTS, new EffectAlterJet());

            //Invocation
            this.put(StatsEnum.SUMMON, new EffectSummon());
            this.put(StatsEnum.SUMMON_DOUBLE , new EffectSummonDouble());
            this.put(StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION, new EffectSummon());
            this.put(StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION_SLAVE, new EffectSummonSlave());
            this.put(StatsEnum.SUMMON_SLAVE, new EffectSummonSlave());


            //Roublard
            this.put(StatsEnum.SUMMON_BOMB, new EffectSummonBomb());
            this.put(StatsEnum.COMBO_DAMMAGES, new EffectStats());
            this.put(StatsEnum.EXPAND_SIZE, new EffectExpandSize());
            this.put(StatsEnum.ENABLE_BOMB, new EffectEnableBomb());
            this.put(StatsEnum.DAMAGE_NEUTRAL_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_AIR_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_WATER_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_FIRE_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_EARTH_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.ENDS_ROUND, new EffectFinishTour());
            this.put(StatsEnum.CREATE_ILLUSION, new EffectCreateIllusion());
            this.put(StatsEnum.REFOULLAGE, new EffectPoutch());

            //Steamer
            this.put(StatsEnum.TRANSKO, new EffectPoutch());

            //Eliatrope
            this.put(StatsEnum.LOST_PDV_PERCENT, new EffectLostPdvPercent());
            this.put(StatsEnum.PORTAL_TELEPORTATION, new EffectPortalTeleportation());
            this.put(StatsEnum.ADD_SPELL_PO, new EffectAddSpellRange());
            this.put(StatsEnum.BACK_CELL, new EffectPush());
            this.put(StatsEnum.DISABLE_PORTAL, new EffectDisablePortal());

            //Zobal
            this.put(StatsEnum.ADVANCE_CELL, new EffectAdvance());
            this.put(StatsEnum.BOOST_SHIELD_LIFE_PERCENT, new EffectShieldLifePoint());

            //Roublard
            this.put(StatsEnum.ATTRACT_TO_CELL,new EffectAttract());

            //Resistance% ALL
            this.put(StatsEnum.ADD_ALL_RESITANCES_PERCENT, new EffectAddAllResist());
            this.put(StatsEnum.SUB_ALl_RESISTANCES_PERCENT, new EffectSubAllResist());

            //Sadida
            this.put(StatsEnum.CAST_SPELL, new EffectCastSpell());
            this.put(StatsEnum.SHARE_DAMAGES, new EffectShareDamages());

            //Arbre de vie
            this.put(StatsEnum.HEAL_ATTACKER_DAMAGE_PERCENT_INCURED, new EffectHealDamageIncured());

        }
    };

    public static EffectBase getEffect(StatsEnum Effect) {
        return EffectBase.EFFECTS.get(Effect);
    }

    public static int tryApplyEffect(EffectCast castInfos) {

        if (!EffectBase.EFFECTS.containsKey(castInfos.effectType)) {
            logger.debug("Unexist effect {} " , castInfos.effect.effectId);
            return -1;
        }

        return EffectBase.EFFECTS.get(castInfos.effectType).applyEffect(castInfos);
    }

    /// <summary>
    /// Application de l'effet
    /// </summary>
    /// <param name="Fighter"></param>
    public abstract int applyEffect(EffectCast castInfos);

    public boolean silentCast() {
        return false;
    }

}
