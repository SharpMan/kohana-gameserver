package koh.game.fights.effects;

import java.util.Random;
import koh.game.entities.environments.Pathfunction;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.SummonedFighter;
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
        for (Fighter target : castInfos.targets.stream().filter(tarrget -> /*!(target instanceof StaticFighter) &&*/
                !tarrget.getStates().hasState(FightStateEnum.CARRIED)
                        && !tarrget.getStates().hasState(FightStateEnum.INÉBRANLABLE)
                        && !tarrget.getStates().hasState(FightStateEnum.ENRACINÉ)
                        && !tarrget.getStates().hasState(FightStateEnum.INDÉPLAÇABLE))
                .toArray(Fighter[]::new)) {
            switch (castInfos.effectType) {
                case PUSH_X_CELL:
                case PUSH_BACK:
                    if(castInfos.spellId == SpellIDEnum.DESTIN_ECA && Pathfunction.inLine(target.getFight().getMap(), castInfos.cellId, target.getCellId())){
                        direction = Pathfunction.getDirection(target.getFight().getMap(), castInfos.caster.getCellId(), target.getCellId());
                    }
                    else if(castInfos.caster instanceof SummonedFighter
                            && castInfos.caster.asSummon().getGrade().getMonsterId() == 3289 //Tactirelle
                            && target == castInfos.caster ){
                        continue;
                    }
                    else if (Pathfunction.inLine(target.getFight().getMap(), castInfos.cellId, target.getCellId()) && castInfos.cellId != target.getCellId()) {
                        direction = Pathfunction.getDirection(target.getFight().getMap(), castInfos.cellId, target.getCellId());
                    } else if (Pathfunction.inLine(target.getFight().getMap(), castInfos.caster.getCellId(), target.getCellId())) {
                        direction = Pathfunction.getDirection(target.getFight().getMap(), castInfos.caster.getCellId(), target.getCellId());
                    }
                    else if(target instanceof MonsterFighter && target.asMonster().getGrade().getMonster().isCanBePushed()){
                        continue;
                    }
                    else {
                        return -1;
                    }
                    break;
                case ADVANCE_CELL:
                    final Fighter pp = castInfos.caster;
                    castInfos.caster = target;
                    target = pp;
                    castInfos.targets.remove(0);
                    direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId());
                    break;
                case PULL_FORWARD:
                    direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.caster.getCellId());
                    if(castInfos.isTrap){
                        if(target.getCellId() == castInfos.targetKnownCellId)
                            continue;
                        direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.cellId);
                    }
                    else if(castInfos.isGlyph){
                        direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.cellId);
                    }
                    else if(castInfos.spellId == 5382 || castInfos.spellId == 5475){
                        direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.targetKnownCellId);
                    }
                    else if(castInfos.spellId == 2801 ){
                        direction = Pathfunction.getDirection(target.getFight().getMap(), target.getCellId(), castInfos.targetKnownCellId);
                         if(/*castInfos.caster == target || */castInfos.targetKnownCellId == target.getCellId())
                            continue;
                    }
                    break;
                case BACK_CELL:
                    final Fighter p = castInfos.caster;
                    castInfos.caster = target;
                    target = p;
                    castInfos.targets.remove(0);
                    if (Pathfunction.inLine(target.getFight().getMap(), castInfos.cellId, target.getCellId()) && castInfos.cellId != target.getCellId()) {
                        direction = Pathfunction.getDirection(target.getFight().getMap(), castInfos.cellId, target.getCellId());
                    } else if (Pathfunction.inLine(target.getFight().getMap(), castInfos.caster.getCellId(), target.getCellId())) {
                        direction = Pathfunction.getDirection(target.getFight().getMap(), castInfos.caster.getCellId(), target.getCellId());
                    }
                    break;
            }
            if (EffectPush.applyPush(castInfos, target, direction, castInfos.randomJet(target)) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public static int applyPush(EffectCast CastInfos, Fighter target, byte direction, int length) {
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
                    pushResult = EffectPush.applyPushBackDamages(CastInfos, target, length, i);
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

    public static int applyPushBackDamages(EffectCast CastInfos, Fighter target, int Length, int CurrentLength) {
        int damageCoef = 0;
        if (target.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMaximiseEffects)) {
            damageCoef = 7;
        } else if (CastInfos.caster.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects)) {
            damageCoef = 4;
        } else {
            damageCoef = 4 + EffectPush.RANDOM_PUSHDAMAGE.nextInt(3);
        }

        double levelCoef = CastInfos.caster.getLevel() / 50;
        if (levelCoef < 0.1) {
            levelCoef = 0.1;
        }
        double pushDmg = (CastInfos.caster.getLevel() / 2 + (CastInfos.caster.getStats().getTotal(StatsEnum.ADD_PUSH_DAMAGES_BONUS) - target.getStats().getTotal(StatsEnum.ADD_PUSH_DAMAGES_BONUS)) + 32) * CastInfos.effect.diceNum / (4 * Math.pow(2, CurrentLength));
        final MutableInt damageValue = new MutableInt(pushDmg);
        //MutableInt damageValue = new MutableInt(Math.floor(DamageCoef * LevelCoef) * (Length - CurrentLength + 1));

        final EffectCast subInfos = new EffectCast(StatsEnum.DAMAGE_BRUT, CastInfos.spellId, CastInfos.cellId, 0, null, target, null, false, StatsEnum.NONE, 0, null);


        return EffectDamage.applyDamages(subInfos, target, damageValue);
    }

}
