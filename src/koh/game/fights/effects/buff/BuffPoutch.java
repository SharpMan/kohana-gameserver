package koh.game.fights.effects.buff;

import java.util.ArrayList;
import java.util.Arrays;
import koh.game.Main;
import koh.game.dao.DAO;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffPoutch extends BuffEffect {

    public BuffPoutch(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (DamageInfos.IsReflect || DamageInfos.IsReturnedDamages || DamageInfos.IsPoison) {
            return -1;
        }
        // mort
        if (Caster.isDead()) {
            //Target.buff.RemoveBuff(this);
            return -1;
        }

        if (CastInfos.SpellId == 2809) {
            if(Pathfinder.getGoalDistance(null, DamageInfos.Caster.getCellId(), Target.getCellId()) > 1){
                return -1;
            }
            //Target = DamageInfos.Caster;
        }

        SpellLevel SpellLevel = DAO.getSpells().findSpell(CastInfos.Effect.diceNum).spellLevels[CastInfos.Effect.diceSide == 0 ? 0 : CastInfos.Effect.diceSide - 1];
        double num1 = Fight.RANDOM.nextDouble();
        double num2 = (double) Arrays.stream(SpellLevel.effects).mapToInt(x -> x.random).sum();
        boolean flag = false;
        for (EffectInstanceDice Effect : SpellLevel.effects) {
            Main.Logs().writeDebug(Effect.toString());
            ArrayList<Fighter> Targets = new ArrayList<>();
            for (short Cell : (new Zone(Effect.ZoneShape(), Effect.ZoneSize(), MapPoint.fromCellId(Target.getCellId()).advancedOrientationTo(MapPoint.fromCellId(Target.getCellId()), true), this.Caster.fight.map)).getCells(Target.getCellId())) {
                FightCell FightCell = Target.fight.getCell(Cell);
                if (FightCell != null) {
                    if (FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_STATIC)) {
                        for (Fighter Target2 : FightCell.GetObjectsAsFighter()) {
                            if (CastInfos.SpellId == 2809 && Target2 == Target) {
                                continue;
                            }
                            if (Effect.isValidTarget(this.Target, Target2) && EffectInstanceDice.verifySpellEffectMask(this.Target, Target2, Effect,Target2.ID)) {
                                if (Effect.targetMask.equals("C") && this.Target.getCarriedActor() == Target2.ID) {
                                    continue;
                                } else if (Effect.targetMask.equals("a,A") && this.Target.getCarriedActor() != 0 & this.Target.ID == Target2.ID) {
                                    continue;
                                }
                                Targets.add(Target2);

                            }
                        }
                    }
                }
            }
            if(CastInfos.SpellId == 94){
                Targets.clear();
                Targets.add(DamageInfos.Caster);
            }
            if (Effect.random > 0) {
                if (!flag) {
                    if (num1 > (double) Effect.random / num2) {
                        num1 -= (double) Effect.random / num2;
                        continue;
                    } else {
                        flag = true;
                    }
                } else {
                    continue;
                }
            }
            EffectCast Cast2 = new EffectCast(Effect.EffectType(), SpellLevel.spellId, (CastInfos.EffectType == StatsEnum.Refoullage) ? Caster.getCellId() : this.Target.getCellId(), num1, Effect, this.Target, Targets, false, StatsEnum.NONE, DamageValue.intValue(), SpellLevel);
            Cast2.targetKnownCellId = Target.getCellId();
            if (EffectBase.TryApplyEffect(Cast2) == -3) {
                return -3;
            }
        }

        /*int apply = -1;
         for (short cell : (new Zone(X, (byte) 1, MapPoint.fromCellId(Target.getCellId()).advancedOrientationTo(MapPoint.fromCellId(Target.getCellId()), true))).getCells(Target.getCellId())) {
         FightCell FightCell = this.Target.fight.getCell(cell);
         if (FightCell != null) {
         if (FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER) | FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_CAWOTTE)) {
         for (Fighter Target : FightCell.GetObjectsAsFighter()) {
         int newValue = EffectDamage.ApplyDamages(DamageInfos, Target, new MutableInt((DamageInfos.RandomJet(Target) * 20) / 100));
         if (newValue < apply) {
         apply = newValue;
         }
         }
         }
         }
         }
         return apply;*/
        //return EffectDamage.ApplyDamages(DamageInfos, Target, new MutableInt((DamageInfos.RandomJet(Target) * 20) / 100)); //TIDO: ChangeRandom Jet to DamageJet direct
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
