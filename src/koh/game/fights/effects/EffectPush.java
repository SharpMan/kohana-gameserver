package koh.game.fights.effects;

import java.util.Random;
import koh.game.entities.environments.Pathfinder;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.game.fights.fighters.StaticFighter;
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
    public int ApplyEffect(EffectCast CastInfos) {
        byte Direction = 0;
        for (Fighter Target : CastInfos.Targets.stream().filter(target -> /*!(target instanceof StaticFighter) &&*/ !target.States.HasState(FightStateEnum.Porté) && !target.States.HasState(FightStateEnum.Inébranlable) && !target.States.HasState(FightStateEnum.Enraciné) && !target.States.HasState(FightStateEnum.Indéplaçable)).toArray(Fighter[]::new)) {
            switch (CastInfos.EffectType) {
                case Push_Back:
                    if (Pathfinder.InLine(Target.Fight.Map, CastInfos.CellId, Target.CellId()) && CastInfos.CellId != Target.CellId()) {
                        Direction = Pathfinder.GetDirection(Target.Fight.Map, CastInfos.CellId, Target.CellId());
                    } else if (Pathfinder.InLine(Target.Fight.Map, CastInfos.Caster.CellId(), Target.CellId())) {
                        Direction = Pathfinder.GetDirection(Target.Fight.Map, CastInfos.Caster.CellId(), Target.CellId());
                    } else {
                        return -1;
                    }
                    break;
                case PullForward:
                    Direction = Pathfinder.GetDirection(Target.Fight.Map, Target.CellId(), CastInfos.Caster.CellId());
                    break;
                case BACK_CELL:
                    Fighter p = CastInfos.Caster;
                    CastInfos.Caster = Target;
                    Target = p;
                    CastInfos.Targets.remove(0);
                    if (Pathfinder.InLine(Target.Fight.Map, CastInfos.CellId, Target.CellId()) && CastInfos.CellId != Target.CellId()) {
                        Direction = Pathfinder.GetDirection(Target.Fight.Map, CastInfos.CellId, Target.CellId());
                    } else if (Pathfinder.InLine(Target.Fight.Map, CastInfos.Caster.CellId(), Target.CellId())) {
                        Direction = Pathfinder.GetDirection(Target.Fight.Map, CastInfos.Caster.CellId(), Target.CellId());
                    }
                    break;
            }
            if (EffectPush.ApplyPush(CastInfos, Target, Direction, CastInfos.RandomJet(Target)) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public static int ApplyPush(EffectCast CastInfos, Fighter target, byte direction, int length) {
        FightCell currentCell = target.myCell;
        short StartCell = target.CellId();
        for (int i = 0; i < length; i++) {
            FightCell nextCell = target.Fight.GetCell(Pathfinder.NextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.CanWalk()) {
                if (nextCell.HasObject(FightObjectType.OBJECT_TRAP)) {
                    target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, nextCell.Id));
                    return target.SetCell(nextCell);
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
                    target.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).forEach(x -> x.Target.SetCell(target.Fight.GetCell(StartCell)));
                    target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, currentCell.Id));

                }

                int result = target.SetCell(currentCell);

                if (pushResult < result) {
                    return pushResult;
                }
                return result;
            }

            currentCell = nextCell;
        }

        int result = target.SetCell(currentCell);

        target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect == null ? 5 : CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, currentCell.Id));

        target.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorteur && x.Duration != 0).forEach(x -> x.Target.SetCell(target.Fight.GetCell(StartCell)));

        return result;
    }

    public static int ApplyPushBackDamages(EffectCast CastInfos, Fighter Target, int Length, int CurrentLength) {
        int DamageCoef = 0;
        if (Target.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMaximiseEffects)) {
            DamageCoef = 7;
        } else if (CastInfos.Caster.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects)) {
            DamageCoef = 4;
        } else {
            DamageCoef = 4 + EffectPush.RANDOM_PUSHDAMAGE.nextInt(3);
        }

        double LevelCoef = CastInfos.Caster.Level() / 50;
        if (LevelCoef < 0.1) {
            LevelCoef = 0.1;
        }
        double pushDmg = (CastInfos.Caster.Level() / 2 + (CastInfos.Caster.Stats.GetTotal(StatsEnum.Add_Push_Damages_Bonus) - Target.Stats.GetTotal(StatsEnum.Add_Push_Damages_Bonus)) + 32) * CastInfos.Effect.diceNum / (4 * Math.pow(2,CurrentLength));
        MutableInt DamageValue = new MutableInt(pushDmg);
        //MutableInt DamageValue = new MutableInt(Math.floor(DamageCoef * LevelCoef) * (Length - CurrentLength + 1));

        EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, CastInfos.SpellId, CastInfos.CellId, 0, null, Target, null, false, StatsEnum.NONE, 0, null);

        return EffectDamage.ApplyDamages(SubInfos, Target, DamageValue);
    }

}
