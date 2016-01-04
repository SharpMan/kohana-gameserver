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

    //TODO effect 406
    private static final HashMap<StatsEnum, EffectBase> Effects = new HashMap<StatsEnum, EffectBase>() {
        {
            this.put(StatsEnum.Damage_Neutral, new EffectDamage());
            this.put(StatsEnum.Damage_Earth, new EffectDamage());
            this.put(StatsEnum.Damage_Air, new EffectDamage());
            this.put(StatsEnum.Damage_Fire, new EffectDamage());
            this.put(StatsEnum.Damage_Water, new EffectDamage());

            //Xelor
            this.put(StatsEnum.TELEPORT_PREVIOUS_POSITION, new EffectTpPrevPos());
            this.put(StatsEnum.TELEPORT_STARTING_TURN_CELL, new EffectTpFirstPos());
            this.put(StatsEnum.TELEPORT_SYMETRIC, new EffectTeleportSymetric());
            this.put(StatsEnum.TELEPORT_SYMETRIC_MYSELF, new EffectTeleportSymetricMySelf());

            //Téleportation
            this.put(StatsEnum.Teleport_4, new EffectTeleport());
            this.put(StatsEnum.Teleport, new EffectTeleport());

            //Déclanche les Glyphe
            this.put(StatsEnum.TRIGGERS_GLYPHS, new EffectTriggersGlyph());

            //Echange de places
            this.put(StatsEnum.Switch_Position, new EffectTranspose());
            this.put(StatsEnum.Sacrifice, new EffectSacrifice());

            // Effet de push back/fear
            this.put(StatsEnum.PUSH_X_CELL, new EffectPush());
            this.put(StatsEnum.PUSH_BACK, new EffectPush());
            this.put(StatsEnum.PUSH_FEAR, new EffectPushFear());
            this.put(StatsEnum.PULL_FORWARD, new EffectPush());
            this.put(StatsEnum.DODGE, new EffectDodge());

            //Effet de IOP
            this.put(StatsEnum.POUTCH, new EffectPoutch());
            this.put(StatsEnum.DISTRIBUTES_DAMAGES_OCCASIONED, new EffectDistributesDamagesOccasioned());

            this.put(StatsEnum.PUNITION, new EffectPunishment());
            this.put(StatsEnum.LAYING_GLYPH_RANKED, new EffectActivableObject());
            this.put(StatsEnum.LAYING_GLYPH_RANKED_2, new EffectActivableObject());
            this.put(StatsEnum.LAYING_GLYPH, new EffectActivableObject());
            this.put(StatsEnum.LAYING_TRAP_LEVEL, new EffectActivableObject());
            this.put(StatsEnum.Reveals_Invisible, new EffectPerception());
            this.put(StatsEnum.LAYING_PORTAIL, new EffectActivableObject());

            // 1 PA,PM Utilisé = X Pdv perdu
            this.put(StatsEnum.Lose_PV_By_Using_PA, new EffectDamagePerPA());
            this.put(StatsEnum.PA_USED_LOST_X_PDV_2, new EffectDamagePerPA());
            this.put(StatsEnum.PA_USED_LOST_X_PDV, new EffectDamagePerPA());
            this.put(StatsEnum.PM_USED_LOST_X_PDV, new EffectDamagePerPM());

            this.put(StatsEnum.CHANGE_APPEARANCE, new EffectSkin());
            this.put(StatsEnum.CHANGE_LOOK, new EffectSkin());

            //CoolDown
            this.put(StatsEnum.SPELL_COOLDOWN, new EffectSpellCoolDown());

            //Soin
            this.put(StatsEnum.Heal, new EffectHeal());

            // Augmente de X les domamges de base du sort Y
            this.put(StatsEnum.ADD_BASE_DAMAGE_SPELL, new EffectIncreaseSpellJet());

            // Ajout d'un etat / changement de skin
            this.put(StatsEnum.Invisibility, new EffectAddState());
            this.put(StatsEnum.ADD_STATE, new EffectAddState());
            this.put(StatsEnum.Removes_State_3, new EffectLostState());

            this.put(StatsEnum.Steal_Neutral, new EffectLifeSteal());
            this.put(StatsEnum.Steal_Earth, new EffectLifeSteal());
            this.put(StatsEnum.Steal_Air, new EffectLifeSteal());
            this.put(StatsEnum.Steal_Fire, new EffectLifeSteal());
            this.put(StatsEnum.Steal_Water, new EffectLifeSteal());
            this.put(StatsEnum.Steal_PV_Fix, new EffectLifeSteal());

            this.put(StatsEnum.REFLECT_SPELL, new EffectReflectSpell());
            this.put(StatsEnum.Desenvoutement, new EffectDebuff());
            this.put(StatsEnum.Increase_Effect_Duration, new EffectDispellEffectDuration());

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
            this.put(StatsEnum.DAMAGE_ARMOR_REDUCTION, new EffectArmor());
            this.put(StatsEnum.Damage_Reduction, new EffectArmor());

            //chance Eca
            this.put(StatsEnum.DAMAGE_BECOME_HEAL, new EffectDamageBecomeHeal());

            //Heal
            this.put(StatsEnum.PDV_PERCENT_REPORTED, new EffectHealPercent());

            //AddPdVPercent 
            this.put(StatsEnum.ADD_VITALITY_PERCENT, new EffectVitalityPercent());

            //Lose LifePer
            this.put(StatsEnum.DamageLifeNeutre, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeEau, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeTerre, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeAir, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeFeu, new EffectLifeDamage());
            this.put(StatsEnum.DamageDropLife, new EffectDamageDropLife());

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
            this.put(StatsEnum.AddDamagePhysic, new EffectStats());
            this.put(StatsEnum.SUB_PA_ESQUIVE_2, new EffectSubPAEsquive());
            this.put(StatsEnum.SubPAEsquive, new EffectSubPAEsquive());
            this.put(StatsEnum.SUB_PM_ESQUIVE_2, new EffectSubPMEsquive());
            this.put(StatsEnum.SubPMEsquive, new EffectSubPMEsquive());

            //DommageSubis
            this.put(StatsEnum.DAMMAGES_OCASSIONED, new EffectDammageOcassioned());
            this.put(StatsEnum.REDUCE_FINAl_DAMAGE_PERCENT, new EffectDamagePercentReduced());
            //Enleve les effets du sort
            this.put(StatsEnum.DISPELL_SPELL, new EffectDispellSpell());

            // Chatiment sacris
            this.put(StatsEnum.CHATIMENT, new EffectChatiment());
            this.put(StatsEnum.Erosion, new EffectErosion());

            
            //Corruption
            this.put(StatsEnum.Skip_Turn, new EffectEndTurn());
            
            //Caracteristiques Ajout/Reduction
            this.put(StatsEnum.STRENGTH, new EffectStats());
            this.put(StatsEnum.INTELLIGENCE, new EffectStats());
            this.put(StatsEnum.CHANCE, new EffectStats());
            this.put(StatsEnum.WISDOM, new EffectStats());
            this.put(StatsEnum.AGILITY, new EffectStats());
            this.put(StatsEnum.VITALITY, new EffectStats());
            this.put(StatsEnum.AddVie, new EffectStats());
            this.put(StatsEnum.SUB_STRENGTH, new EffectStats());
            this.put(StatsEnum.SUB_INTELLIGENCE, new EffectStats());
            this.put(StatsEnum.SUB_CHANCE, new EffectStats());
            this.put(StatsEnum.SUB_AGILITY, new EffectStats());
            this.put(StatsEnum.SUB_WISDOM, new EffectStats());
            this.put(StatsEnum.SUB_VITALITY, new EffectStats());
            this.put(StatsEnum.PROSPECTING, new EffectStats());
            this.put(StatsEnum.AddDamageMagic, new EffectStats());
            this.put(StatsEnum.Add_Damage_Final_Percent, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_BONUS, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_CRITICAL_DAMAGES, new EffectStats());
            this.put(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION, new EffectStats());
            this.put(StatsEnum.ADD_CRITICAL_DAMAGES, new EffectStats());
            this.put(StatsEnum.SUB_PROSPECTING, new EffectStats());
            this.put(StatsEnum.SUB_DAMAGE, new EffectStats());
            this.put(StatsEnum.SubDamageBonusPercent, new EffectStats());
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

            this.put(StatsEnum.Add_Magic_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Physical_Reduction, new EffectStats());

            this.put(StatsEnum.SUB_WATER_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_EARTH_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_AIR_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_FIRE_RESIST_PERCENT, new EffectStats());
            this.put(StatsEnum.SUB_NEUTRAL_RESIST_PERCENT, new EffectStats());

            //Ajout ou reduction de dommage
            this.put(StatsEnum.ADD_CRITICAL_HIT, new EffectStats());
            this.put(StatsEnum.ALL_DAMAGES_BONUS, new EffectStats());
            this.put(StatsEnum.Critical_Miss, new EffectStats());
            this.put(StatsEnum.Damage_Return, new EffectStats());
            this.put(StatsEnum.SUB_CRITICAL_HIT, new EffectStats());
            this.put(StatsEnum.Add_Global_Damage_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Damage_Final_Percent, new EffectStats());
            this.put(StatsEnum.SubDamageBonusPercent, new EffectStats());
            this.put(StatsEnum.AddDamagePercent, new EffectStats());
            this.put(StatsEnum.ADD_SUMMON_LIMIT, new EffectStats());
            this.put(StatsEnum.AddDamageMagic, new EffectStats());
            this.put(StatsEnum.Add_Physical_Damage, new EffectStats());
            this.put(StatsEnum.ADD_TACKLE_BLOCK, new EffectStats());
            this.put(StatsEnum.ADD_TACKLE_EVADE, new EffectStats());
            this.put(StatsEnum.SUB_TACKLE_BLOCK, new EffectStats());
            this.put(StatsEnum.SUB_TACKLE_EVADE, new EffectStats());

            //Panda
            this.put(StatsEnum.PORTER_TARGET, new EffectPorter());
            this.put(StatsEnum.LAUNCHER_ENTITY, new EffectLancer());

            this.put(StatsEnum.Kill, new EffectDieFighter());

            //AlterJet
            this.put(StatsEnum.MaximizeEffects, new EffectAlterJet());
            this.put(StatsEnum.MinimizeEffects, new EffectAlterJet());

            //Roublard
            this.put(StatsEnum.ACTION_SUMMON_BOMB, new EffectSummonBomb());
            this.put(StatsEnum.COMBO_DAMMAGES, new EffectStats());
            this.put(StatsEnum.EXPAND_SIZE, new EffectExpandSize());
            this.put(StatsEnum.ENABLE_BOMB, new EffectEnableBomb());
            this.put(StatsEnum.DAMAGE_NEUTRAL_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_AIR_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_WATER_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_FIRE_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.DAMAGE_EARTH_PER_PM_PERCENT, new EffectDamage());
            this.put(StatsEnum.Ends_Round, new EffectFinishTour());
            this.put(StatsEnum.CREATE_ILLUSION, new EffectCreateIllusion());
            this.put(StatsEnum.Refoullage, new EffectPoutch());

            //Eliatrope
            this.put(StatsEnum.LOST_PDV_PERCENT, new EffectLostPdvPercent());
            this.put(StatsEnum.PORTAL_TELEPORTATION, new EffectPortalTeleportation());
            this.put(StatsEnum.ADD_SPELL_PO, new EffectAddSpellRange());
            this.put(StatsEnum.BACK_CELL, new EffectPush());
            this.put(StatsEnum.DISABLE_PORTAL, new EffectDisablePortal());

            //Zobal
            this.put(StatsEnum.ADVANCE_CELL, new EffectPush());
            this.put(StatsEnum.BOOST_SHIELD_BASED_ON_CASTER_LIFE, new EffectShieldLifePoint());

            //Resistance% ALL
            this.put(StatsEnum.ADD_ALL_RESITANCES_PERCENT, new EffectAddAllResist());
            this.put(StatsEnum.SUB_ALl_RESISTANCES_PERCENT, new EffectSubAllResist());

            //Sadida
            this.put(StatsEnum.CAST_SPELL_ALL_ENEMY , new EffectCastSpell());

        }
    };

    public static EffectBase getEffect(StatsEnum Effect) {
        return EffectBase.Effects.get(Effect);
    }

    public static int TryApplyEffect(EffectCast CastInfos) {

        if (!EffectBase.Effects.containsKey(CastInfos.EffectType)) {
            logger.debug("Unexist effect {} " , CastInfos.effect.effectId);
            return -1;
        }

        return EffectBase.Effects.get(CastInfos.EffectType).applyEffect(CastInfos);
    }

    /// <summary>
    /// Application de l'effet
    /// </summary>
    /// <param name="Fighter"></param>
    public abstract int applyEffect(EffectCast CastInfos);

    public boolean silentCast() {
        return false;
    }

}
