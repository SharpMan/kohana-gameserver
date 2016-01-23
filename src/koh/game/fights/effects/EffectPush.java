package koh.game.fights.effects;

import java.util.Random;
import koh.game.entities.environments.Pathfunction;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.SpellIDEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSlideMessage;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectPush extends EffectBase {

    @Getter
    private static final Random RANDOM_PUSHDAMAGE = new Random();

    @Override
    public int applyEffect(EffectCast castInfos) {
        byte direction = 0;
        for (Fighter Target : castInfos.targets.stream().filter(target -> /*!(target instanceof StaticFighter) &&*/ !target.getStates().hasState(FightStateEnum.CARRIED) && !target.getStates().hasState(FightStateEnum.Inébranlable) && !target.getStates().hasState(FightStateEnum.ENRACINÉ) && !target.getStates().hasState(FightStateEnum.Indéplaçable)).toArray(Fighter[]::new)) {
            switch (castInfos.effectType) {
                case PUSH_X_CELL:
                case PUSH_BACK:
                    if(castInfos.spellId == SpellIDEnum.DESTIN_ECA && Pathfunction.inLine(Target.getFight().getMap(), castInfos.cellId, Target.getCellId())){
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), castInfos.caster.getCellId(), Target.getCellId());
                    }
                    else if (Pathfunction.inLine(Target.getFight().getMap(), castInfos.cellId, Target.getCellId()) && castInfos.cellId != Target.getCellId()) {
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), castInfos.cellId, Target.getCellId());
                    } else if (Pathfunction.inLine(Target.getFight().getMap(), castInfos.caster.getCellId(), Target.getCellId())) {
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), castInfos.caster.getCellId(), Target.getCellId());
                    } else {
                        return -1;
                    }
                    break;
                case ADVANCE_CELL:
                    Fighter pp = castInfos.caster;
                    castInfos.caster = Target;
                    Target = pp;
                    castInfos.targets.remove(0);
                    direction = Pathfunction.getDirection(Target.getFight().getMap(), Target.getCellId(), castInfos.caster.getCellId());
                    break;
                case PULL_FORWARD:
                    direction = Pathfunction.getDirection(Target.getFight().getMap(), Target.getCellId(), castInfos.caster.getCellId());
                    if(castInfos.spellId == 5382 || castInfos.spellId == 5475){
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), Target.getCellId(), castInfos.targetKnownCellId);
                    }
                    break;
                case BACK_CELL:
                    Fighter p = castInfos.caster;
                    castInfos.caster = Target;
                    Target = p;
                    castInfos.targets.remove(0);
                    if (Pathfunction.inLine(Target.getFight().getMap(), castInfos.cellId, Target.getCellId()) && castInfos.cellId != Target.getCellId()) {
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), castInfos.cellId, Target.getCellId());
                    } else if (Pathfunction.inLine(Target.getFight().getMap(), castInfos.caster.getCellId(), Target.getCellId())) {
                        direction = Pathfunction.getDirection(Target.getFight().getMap(), castInfos.caster.getCellId(), Target.getCellId());
                    }
                    break;
            }
            if (EffectPush.ApplyPush(castInfos, Target, direction, castInfos.randomJet(Target)) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public static int ApplyPush(EffectCast CastInfos, Fighter target, byte direction, int length) {
        FightCell currentCell = target.getMyCell();
        short StartCell = target.getCellId();
        for (int i = 0; i < length; i++) {
            FightCell nextCell = target.getFight().getCell(Pathfunction.nextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.canWalk()) {
                if (nextCell.hasObject(FightObjectType.OBJECT_TRAP)) {
                    target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, nextCell.Id));
                    return target.setCell(nextCell);
                }
            } else {
                int pushResult = -1;
                if (CastInfos.effectType == StatsEnum.PUSH_BACK) {
                    pushResult = EffectPush.ApplyPushBackDamages(CastInfos, target, length, i);
                    if (pushResult != -1) {
                        return pushResult;
                    }
                }

                if (i != 0) {
                    target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(StartCell)));
                    target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

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

        target.getFight().sendToField(new GameActionFightSlideMessage(CastInfos.effect == null ? 5 : CastInfos.effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

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
        double pushDmg = (CastInfos.caster.getLevel() / 2 + (CastInfos.caster.getStats().getTotal(StatsEnum.ADD_PUSH_DAMAGES_BONUS) - Target.getStats().getTotal(StatsEnum.ADD_PUSH_DAMAGES_BONUS)) + 32) * CastInfos.effect.diceNum / (4 * Math.pow(2, CurrentLength));
        MutableInt DamageValue = new MutableInt(pushDmg);
        //MutableInt damageValue = new MutableInt(Math.floor(DamageCoef * LevelCoef) * (Length - CurrentLength + 1));

        EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, CastInfos.spellId, CastInfos.cellId, 0, null, Target, null, false, StatsEnum.NONE, 0, null);

        return EffectDamage.applyDamages(SubInfos, Target, DamageValue);
    }

}
