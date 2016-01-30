package koh.game.fights.effects.buff;

import java.util.ArrayList;
import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.entities.environments.Pathfunction;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class BuffPoutch extends BuffEffect {

    private static final Logger logger = LogManager.getLogger(BuffPoutch.class);

    public BuffPoutch(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (DamageInfos.isReflect || DamageInfos.isReturnedDamages || DamageInfos.isPoison) {
            return -1;
        }
        // mort
        if (caster.isDead()) {
            //target.buff.RemoveBuff(this);
            return -1;
        }

        if (castInfos.spellId == 2809) {
            if(Pathfunction.goalDistance(null, DamageInfos.caster.getCellId(), target.getCellId()) > 1){
                return -1;
            }
            //target = DamageInfos.caster;
        }

        final SpellLevel SpellLevel = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevels()[castInfos.effect.diceSide == 0 ? 0 : castInfos.effect.diceSide - 1];
        double num1 = Fight.RANDOM.nextDouble();
        double num2 = (double) Arrays.stream(SpellLevel.getEffects()).mapToInt(x -> x.random).sum();
        boolean flag = false;
        for (EffectInstanceDice Effect : SpellLevel.getEffects()) {
            logger.debug(Effect.toString());
            final ArrayList<Fighter> targets = new ArrayList<>();
            for (short Cell : (new Zone(Effect.getZoneShape(), Effect.zoneSize(), MapPoint.fromCellId(target.getCellId()).advancedOrientationTo(MapPoint.fromCellId(target.getCellId()), true), this.caster.getFight().getMap())).getCells(target.getCellId())) {
                final FightCell FightCell = target.getFight().getCell(Cell);
                if (FightCell != null) {
                    for (final Fighter target2 : FightCell.getObjectsAsFighter()) {
                            if (castInfos.spellId == 2809 && target2 == target) {
                                continue;
                            }
                            if (Effect.isValidTarget(this.target, target2) && EffectInstanceDice.verifySpellEffectMask(this.target, target2, Effect,target2.getID())) {
                                if (Effect.targetMask.equals("C") && this.target.getCarriedActor() == target2.getID()) {
                                    continue;
                                } else if (Effect.targetMask.equals("a,A") && this.target.getCarriedActor() != 0 & this.target.getID() == target2.getID()) {
                                    continue;
                                }
                                targets.add(target2);

                            }
                        }

                }
            }
            if(castInfos.spellId == 94){
                targets.clear();
                targets.add(DamageInfos.caster);
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
            final EffectCast cast = new EffectCast(Effect.getEffectType(), SpellLevel.getSpellId(), (castInfos.effectType == StatsEnum.REFOULLAGE) ? caster.getCellId() : this.target.getCellId(), num1, Effect, this.target, targets, false, StatsEnum.NONE, DamageValue.intValue(), SpellLevel);
            cast.targetKnownCellId = target.getCellId();
            if (EffectBase.tryApplyEffect(cast) == -3) {
                return -3;
            }
        }

        /*int apply = -1;
         for (short cell : (new Zone(X, (byte) 1, MapPoint.fromCellId(target.getCellId()).advancedOrientationTo(MapPoint.fromCellId(target.getCellId()), true))).getCells(target.getCellId())) {
         FightCell FightCell = this.target.fight.getCell(cell);
         if (FightCell != null) {
         if (FightCell.hasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER) | FightCell.hasGameObject(IFightObject.FightObjectType.OBJECT_CAWOTTE)) {
         for (Fighter target : FightCell.getObjectsAsFighter()) {
         int newValue = EffectDamage.applyDamages(DamageInfos, target, new MutableInt((DamageInfos.randomJet(target) * 20) / 100));
         if (newValue < apply) {
         apply = newValue;
         }
         }
         }
         }
         }
         return apply;*/
        //return EffectDamage.applyDamages(DamageInfos, target, new MutableInt((DamageInfos.randomJet(target) * 20) / 100)); //TIDO: ChangeRandom Jet to DamageJet direct
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
