package koh.game.fights.effects;

import java.util.Random;
import koh.game.entities.environments.Pathfinder;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSlideMessage;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectPush extends EffectBase {

    private static final Random RANDOM_PUSHDAMAGE = new Random();

    @Override
    public int applyEffect(EffectCast CastInfos) {
        byte Direction = 0;
        for (Fighter Target : CastInfos.Targets.stream().filter(target -> /*!(target instanceof StaticFighter) &&*/ !target.getStates().hasState(FightStateEnum.Porté) && !target.getStates().hasState(FightStateEnum.Inébranlable) && !target.getStates().hasState(FightStateEnum.Enraciné) && !target.getStates().hasState(FightStateEnum.Indéplaçable)).toArray(Fighter[]::new)) {
            switch (CastInfos.EffectType) {
                case PUSH_X_CELL:
                case Push_Back:
                    if (Pathfinder.inLine(Target.getFight().getMap(), CastInfos.CellId, Target.getCellId()) && CastInfos.CellId != Target.getCellId()) {
                        Direction = Pathfinder.getDirection(Target.getFight().getMap(), CastInfos.CellId, Target.getCellId());
                    } else if (Pathfinder.inLine(Target.getFight().getMap(), CastInfos.caster.getCellId(), Target.getCellId())) {
                        Direction = Pathfinder.getDirection(Target.getFight().getMap(), CastInfos.caster.getCellId(), Target.getCellId());
                    } else {
                        return -1;
                    }
                    break;
                case ADVANCE_CELL:
                    Fighter pp = CastInfos.caster;
                    CastInfos.caster = Target;
                    Target = pp;
                    CastInfos.Targets.remove(0);
                    Direction = Pathfinder.getDirection(Target.getFight().getMap(), Target.getCellId(), CastInfos.caster.getCellId());
                    break;
                case PullForward:
                    Direction = Pathfinder.getDirection(Target.getFight().getMap(), Target.getCellId(), CastInfos.caster.getCellId());
                    if(CastInfos.SpellId == 5382 || CastInfos.SpellId == 5475){
                        Direction = Pathfinder.getDirection(Target.getFight().getMap(), Target.getCellId(), CastInfos.targetKnownCellId);
                    }
                    break;
                case BACK_CELL:
                    Fighter p = CastInfos.caster;
                    CastInfos.caster = Target;
                    Target = p;
                    CastInfos.Targets.remove(0);
                    if (Pathfinder.inLine(Target.getFight().getMap(), CastInfos.CellId, Target.getCellId()) && CastInfos.CellId != Target.getCellId()) {
                        Direction = Pathfinder.getDirection(Target.getFight().getMap(), CastInfos.CellId, Target.getCellId());
                    } else if (Pathfinder.inLine(Target.getFight().getMap(), CastInfos.caster.getCellId(), Target.getCellId())) {
                        Direction = Pathfinder.getDirection(Target.getFight().getMap(), CastInfos.caster.getCellId(), Target.getCellId());
                    }
                    break;
            }
            if (EffectPush.ApplyPush(CastInfos, Target, Direction, CastInfos.randomJet(Target)) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public static int ApplyPush(EffectCast CastInfos, Fighter target, byte direction, int length) {
        FightCell currentCell = target.getMyCell();
        short StartCell = target.getCellId();
        for (int i = 0; i < length; i++) {
            FightCell nextCell = target.getFight().getCell(Pathfinder.nextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.CanWalk()) {
                if (nextCell.HasObject(FightObjectType.OBJECT_TRAP)) {
                    target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, nextCell.Id));
                    return target.setCell(nextCell);
                }
            } else {
                int pushResult = -1;
                if (CastInfos.EffectType == StatsEnum.Push_Back) {
                    pushResult = EffectPush.ApplyPushBackDamages(CastInfos, target, length, i);
                    if (pushResult != -1) {
                        return pushResult;
                    }
                }

                if (i != 0) {
                    target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(StartCell)));
                    target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

                }

                int result = target.setCell(currentCell);

                if (pushResult < result) {
                    return pushResult;
                }
                return result;
            }

            currentCell = nextCell;
        }

        int result = target.setCell(currentCell);

        target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.Effect == null ? 5 : CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

        target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(StartCell)));

        return result;
    }

    public static int ApplyPushBackDamages(EffectCast CastInfos, Fighter Target, int Length, int CurrentLength) {
        int DamageCoef = 0;
        if (Target.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMaximiseEffects)) {
            DamageCoef = 7;
        } else if (CastInfos.caster.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects)) {
            DamageCoef = 4;
        } else {
            DamageCoef = 4 + EffectPush.RANDOM_PUSHDAMAGE.nextInt(3);
        }

        double LevelCoef = CastInfos.caster.getLevel() / 50;
        if (LevelCoef < 0.1) {
            LevelCoef = 0.1;
        }
        double pushDmg = (CastInfos.caster.getLevel() / 2 + (CastInfos.caster.getStats().getTotal(StatsEnum.Add_Push_Damages_Bonus) - Target.getStats().getTotal(StatsEnum.Add_Push_Damages_Bonus)) + 32) * CastInfos.Effect.diceNum / (4 * Math.pow(2, CurrentLength));
        MutableInt DamageValue = new MutableInt(pushDmg);
        //MutableInt DamageValue = new MutableInt(Math.floor(DamageCoef * LevelCoef) * (Length - CurrentLength + 1));

        EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, CastInfos.SpellId, CastInfos.CellId, 0, null, Target, null, false, StatsEnum.NONE, 0, null);

        return EffectDamage.ApplyDamages(SubInfos, Target, DamageValue);
    }

}
