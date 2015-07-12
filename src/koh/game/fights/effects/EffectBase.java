package koh.game.fights.effects;

import java.util.HashMap;
import koh.game.Main;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public abstract class EffectBase {

    //TODO Effect 406
    private static final HashMap<StatsEnum, EffectBase> Effects = new HashMap<StatsEnum, EffectBase>() {
        {
            this.put(StatsEnum.Damage_Neutral, new EffectDamage());
            this.put(StatsEnum.Damage_Earth, new EffectDamage());
            this.put(StatsEnum.Damage_Air, new EffectDamage());
            this.put(StatsEnum.Damage_Fire, new EffectDamage());
            this.put(StatsEnum.Damage_Water, new EffectDamage());

            //Téleportation
            this.put(StatsEnum.Teleport_4, new EffectTeleport());
            this.put(StatsEnum.Teleport, new EffectTeleport());

            //Déclanche les Glyphe
            this.put(StatsEnum.Triggers_Glyphs, new EffectTriggersGlyph());

            //Echange de places
            this.put(StatsEnum.Switch_Position, new EffectTranspose());
            this.put(StatsEnum.Sacrifice, new EffectSacrifice());

            // Effet de push back/fear
            this.put(StatsEnum.Push_Back, new EffectPush());
            this.put(StatsEnum.PushFear, new EffectPushFear());
            this.put(StatsEnum.PullForward, new EffectPush());
            this.put(StatsEnum.Dodge, new EffectDodge());

            //Effet de IOP
            this.put(StatsEnum.Poutch, new EffectPoutch());
            this.put(StatsEnum.DistributesDamagesOccasioned, new EffectDistributesDamagesOccasioned());

            //this.put(StatsEnum.PUNITION, new Effec
            this.put(StatsEnum.LAYING_GLYPH_RANKED, new EffectActivableObject());
            this.put(StatsEnum.LAYING_GLYPH_RANKED_2, new EffectActivableObject());
            this.put(StatsEnum.LAYING_GLYPH, new EffectActivableObject());
            this.put(StatsEnum.LAYING_TRAP_LEVEL, new EffectActivableObject());
            this.put(StatsEnum.Reveals_Invisible, new EffectPerception());

            // 1 PA,PM Utilisé = X Pdv perdu
            this.put(StatsEnum.Lose_PV_By_Using_PA, new EffectDamagePerPA());
            this.put(StatsEnum.PA_USED_LOST_X_PDV, new EffectDamagePerPA());
            this.put(StatsEnum.PM_USED_LOST_X_PDV, new EffectDamagePerPM());

            this.put(StatsEnum.Change_Appearance, new EffectSkin());
            this.put(StatsEnum.CHANGE_LOOK, new EffectSkin());

            //CoolDown
            this.put(StatsEnum.SPELL_COOLDOWN, new EffectSpellCoolDown());

            //Soin
            this.put(StatsEnum.Heal, new EffectHeal());

            // Augmente de X les domamges de base du sort Y
            this.put(StatsEnum.ADD_BASE_DAMAGE_SPELL, new EffectIncreaseSpellJet());

            // Ajout d'un etat / changement de skin
            this.put(StatsEnum.Invisibility, new EffectAddState());
            this.put(StatsEnum.Add_State, new EffectAddState());
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
            this.put(StatsEnum.Steal_Vitality, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Strength, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Intelligence, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Agility, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Wisdom, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Chance, new EffectStatsSteal());
            this.put(StatsEnum.Steal_PA, new EffectStatsSteal());
            this.put(StatsEnum.Steal_PM, new EffectStatsSteal());
            this.put(StatsEnum.Steal_Range, new EffectStatsSteal());

            // Armure et bouclié feca
            this.put(StatsEnum.Damage_Armor_Reduction, new EffectArmor());
            this.put(StatsEnum.Damage_Reduction, new EffectArmor());

            //Heal
            this.put(StatsEnum.PDV_PERCENT_REPORTED, new EffectHealPercent());

            //AddPdVPercent 
            this.put(StatsEnum.AddVitalityPercent, new EffectVitalityPercent());

            //Lose LifePer
            this.put(StatsEnum.DamageLifeNeutre, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeEau, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeTerre, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeAir, new EffectLifeDamage());
            this.put(StatsEnum.DamageLifeFeu, new EffectLifeDamage());
            this.put(StatsEnum.DamageDropLife, new EffectDamageDropLife());

            //Ajout ou reduction PA/PM/PO/Dommages
            this.put(StatsEnum.ActionPoints, new EffectStats());
            this.put(StatsEnum.MovementPoints, new EffectStats());
            this.put(StatsEnum.Sub_PM, new EffectStats());
            this.put(StatsEnum.Sub_PA, new EffectStats());
            this.put(StatsEnum.Sub_Range, new EffectStats());
            this.put(StatsEnum.Add_Range, new EffectStats());
            this.put(StatsEnum.DodgePALostProbability, new EffectStats());
            this.put(StatsEnum.DodgePMLostProbability, new EffectStats());
            this.put(StatsEnum.Sub_Dodge_PA_Probability, new EffectStats());
            this.put(StatsEnum.Sub_Dodge_PM_Probability, new EffectStats());
            this.put(StatsEnum.AddDamagePhysic, new EffectStats());
            this.put(StatsEnum.SubPAEsquive_2, new EffectSubPAEsquive());
            this.put(StatsEnum.SubPAEsquive, new EffectSubPAEsquive());
            this.put(StatsEnum.SubPMEsquive_2, new EffectSubPMEsquive());
            this.put(StatsEnum.SubPMEsquive, new EffectSubPMEsquive());

            //DommageSubis
            this.put(StatsEnum.DammagesOcassioned, new EffectDammageOcassioned());
            this.put(StatsEnum.REDUCE_FINAl_DAMAGE_PERCENT, new EffectDamagePercentReduced());
            //Enleve les effets du sort
            this.put(StatsEnum.DISPELL_SPELL, new EffectDispellSpell());

            // Chatiment sacris
            this.put(StatsEnum.CHATIMENT, new EffectChatiment());
            this.put(StatsEnum.Erosion, new EffectErosion());

            //Caracteristiques Ajout/Reduction
            this.put(StatsEnum.Strength, new EffectStats());
            this.put(StatsEnum.Intelligence, new EffectStats());
            this.put(StatsEnum.Chance, new EffectStats());
            this.put(StatsEnum.Wisdom, new EffectStats());
            this.put(StatsEnum.Agility, new EffectStats());
            this.put(StatsEnum.Vitality, new EffectStats());
            this.put(StatsEnum.AddVie, new EffectStats());
            this.put(StatsEnum.Sub_Strength, new EffectStats());
            this.put(StatsEnum.Sub_Intelligence, new EffectStats());
            this.put(StatsEnum.Sub_Chance, new EffectStats());
            this.put(StatsEnum.Sub_Agility, new EffectStats());
            this.put(StatsEnum.Sub_Wisdom, new EffectStats());
            this.put(StatsEnum.Sub_Vitality, new EffectStats());
            this.put(StatsEnum.Prospecting, new EffectStats());
            this.put(StatsEnum.AddDamageMagic, new EffectStats());
            this.put(StatsEnum.Add_Damage_Bonus_Percent, new EffectStats());
            this.put(StatsEnum.Add_Push_Damages_Bonus, new EffectStats());
            this.put(StatsEnum.Add_Push_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Critical_Damages, new EffectStats());
            this.put(StatsEnum.Add_Critical_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Push_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Critical_Damages, new EffectStats());
            this.put(StatsEnum.Add_Critical_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Prospecting, new EffectStats());
            this.put(StatsEnum.Sub_Damage, new EffectStats());
            this.put(StatsEnum.SubDamageBonusPercent, new EffectStats());
            this.put(StatsEnum.Sub_Push_Damages_Bonus, new EffectStats());
            this.put(StatsEnum.Sub_Push_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Critical_Damages, new EffectStats());
            this.put(StatsEnum.Sub_Critical_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Push_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Critical_Damages, new EffectStats());
            this.put(StatsEnum.Sub_Critical_Damages_Reduction, new EffectStats());
            this.put(StatsEnum.Add_RETRAIT_PA, new EffectStats());
            this.put(StatsEnum.Add_RETRAIT_PM, new EffectStats());
            this.put(StatsEnum.Sub_RETRAIT_PA, new EffectStats());
            this.put(StatsEnum.SUB_RETRAIT_PM, new EffectStats());

            //Soins
            this.put(StatsEnum.Add_Heal_Bonus, new EffectStats());
            this.put(StatsEnum.Sub_Heal_Bonus, new EffectStats());

            //Resistances ajout/suppressions
            this.put(StatsEnum.NeutralElementReduction, new EffectStats());
            this.put(StatsEnum.EarthElementReduction, new EffectStats());
            this.put(StatsEnum.WaterElementReduction, new EffectStats());
            this.put(StatsEnum.AirElementReduction, new EffectStats());
            this.put(StatsEnum.FireElementReduction, new EffectStats());

            this.put(StatsEnum.WaterElementResistPercent, new EffectStats());
            this.put(StatsEnum.EarthElementResistPercent, new EffectStats());
            this.put(StatsEnum.AirElementResistPercent, new EffectStats());
            this.put(StatsEnum.FireElementResistPercent, new EffectStats());
            this.put(StatsEnum.NeutralElementResistPercent, new EffectStats());

            this.put(StatsEnum.Sub_Fire_Element_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Earth_Element_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Water_Element_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Air_Element_Reduction, new EffectStats());
            this.put(StatsEnum.Sub_Neutral_Element_Reduction, new EffectStats());

            this.put(StatsEnum.Add_Magic_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Physical_Reduction, new EffectStats());

            this.put(StatsEnum.Sub_Water_Resist_Percent, new EffectStats());
            this.put(StatsEnum.Sub_Earth_Resist_Percent, new EffectStats());
            this.put(StatsEnum.Sub_Air_Resist_Percent, new EffectStats());
            this.put(StatsEnum.Sub_Fire_Resist_Percent, new EffectStats());
            this.put(StatsEnum.Sub_Neutral_Resist_Percent, new EffectStats());

            //Ajout ou reduction de dommage
            this.put(StatsEnum.Add_CriticalHit, new EffectStats());
            this.put(StatsEnum.AllDamagesBonus, new EffectStats());
            this.put(StatsEnum.Critical_Miss, new EffectStats());
            this.put(StatsEnum.Damage_Return, new EffectStats());
            this.put(StatsEnum.Sub_Critical_Hit, new EffectStats());
            this.put(StatsEnum.Add_Global_Damage_Reduction, new EffectStats());
            this.put(StatsEnum.Add_Damage_Bonus_Percent, new EffectStats());
            this.put(StatsEnum.SubDamageBonusPercent, new EffectStats());
            this.put(StatsEnum.AddDamagePercent, new EffectStats());
            this.put(StatsEnum.AddSummonLimit, new EffectStats());
            this.put(StatsEnum.AddDamageMagic, new EffectStats());
            this.put(StatsEnum.Add_Physical_Damage, new EffectStats());
            this.put(StatsEnum.Add_TackleBlock, new EffectStats());
            this.put(StatsEnum.Add_TackleEvade, new EffectStats());
            this.put(StatsEnum.Sub_TackleBlock, new EffectStats());
            this.put(StatsEnum.Sub_TackleEvade, new EffectStats());

            //Panda
            this.put(StatsEnum.PORTER_TARGET, new EffectPorter());
            this.put(StatsEnum.LAUNCHER_ENTITY, new EffectLancer());

            this.put(StatsEnum.Kill, new EffectDieFighter());

            //AlterJet
            this.put(StatsEnum.MaximizeEffects, new EffectAlterJet());
            this.put(StatsEnum.MinimizeEffects, new EffectAlterJet());

        }
    };

    public static int TryApplyEffect(EffectCast CastInfos) {

        if (!EffectBase.Effects.containsKey(CastInfos.EffectType)) {
            Main.Logs().writeDebug("Unexist effect " + CastInfos.Effect.effectId);
            return -1;
        }

        return EffectBase.Effects.get(CastInfos.EffectType).ApplyEffect(CastInfos);
    }

    /// <summary>
    /// Application de l'effet
    /// </summary>
    /// <param name="Fighter"></param>
    public abstract int ApplyEffect(EffectCast CastInfos);

    public boolean SilentCast() {
        return false;
    }

}
