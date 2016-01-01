package koh.game.fights;

import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.StaticFighter;
import koh.protocol.client.enums.SpellShapeEnum;

/**
 *
 * @author Melancholia
 */
public class PushUtil {

    private static final byte PUSH_EFFECT_ID = 5;
    private static final byte PULL_EFFECT_ID = 6;

    private static boolean hasMinSize(SpellShapeEnum pZoneShape) {
        return ((((((((((pZoneShape == SpellShapeEnum.C)) || ((pZoneShape == SpellShapeEnum.X)))) || ((pZoneShape == SpellShapeEnum.Q)))) || ((pZoneShape == SpellShapeEnum.plus)))) || ((pZoneShape == SpellShapeEnum.sharp))));
    }

    /*  public static boolean hasPushDamages(Fighter pCasterId, Fighter pTargetId, EffectInstance[] pSpellEffects, EffectInstance pEffect, short pSpellImpactCell)
     {
     GameFightFighterInformations casterInfos;
     short origin;
     MapPoint originPoint;
     byte direction;
     int pushForce;
     MapPoint cellMp;
     MapPoint previousCell;
     MapPoint nextCell;
     int force;
     int i;
     if (((!((pEffect.effectId == PUSH_EFFECT_ID)))))
     {
     return (false);
     };
     GameFightFighterInformations targetInfos = (GameFightFighterInformations) pTargetId.getGameContextActorInformations(null);
     if (((targetInfos) && (isPushableEntity(pTargetId))))
     {
     origin = ((!(hasMinSize(pEffect.ZoneShape()))) ? pCasterId.getCellId() : pSpellImpactCell);
     originPoint = MapPoint.fromCellId(origin);
     direction = originPoint.advancedOrientationTo(MapPoint.fromCellId(targetInfos.disposition.cellId), false);
     pushForce = getPushForce(origin, targetInfos, pSpellEffects, pEffect);
     cellMp = MapPoint.fromCellId(targetInfos.disposition.cellId);
     nextCell = cellMp.getNearestCellInDirection(direction);
     force = pushForce;
     i = 0;
     while (i < pushForce)
     {
     if (nextCell)
     {
     if (isBlockingCell(nextCell.cellId, ((!(previousCell)) ? cellMp.cellId : previousCell.cellId)))
     {
     break;
     };
     force--;
     previousCell = nextCell;
     nextCell = nextCell.getNearestCellInDirection(direction);
     };
     i++;
     };
     return ((force > 0));
     };
     return (false);
     }*/
    /* private static int getPushForce(short pPushOriginCell,Fighter pTargetInfos, EffectInstance[] pSpellEffects,EffectInstance pPushEffect)
     {
     int pushForce;
     EffectInstanceDice pullEffect;
     int pushEffectForce;
     int pullEffectForce;
     MapPoint targetCell;
     MapPoint originCell;
     MapPoint cell;
     MapPoint nextCell;
     int orientation;
     int i;
     int  pullDistance;
     int pushEffectIndex = ArrayUtils.indexOf(pSpellEffects, pPushEffect);
     int pullEffectIndex = -1;
     for  (EffectInstance effect : pSpellEffects)
     {
     if (effect.effectId == PULL_EFFECT_ID)
     {
     pullEffectIndex =ArrayUtils.indexOf(pSpellEffects, effect);
     pullEffect = (EffectInstanceDice)(effect);
     break;
     };
     };
     pushEffectForce = ((EffectInstanceDice)pPushEffect).diceNum;
     if (((((!((pullEffectIndex == -1))) && ((pullEffectIndex < pushEffectIndex)))) && (isPushableEntity(pTargetInfos))))
     {
     pullEffectForce = pullEffect.diceNum;
     targetCell = MapPoint.fromCellId(pTargetInfos.getCellId());
     originCell = MapPoint.fromCellId(pPushOriginCell);
     cell = targetCell;
     orientation = targetCell.advancedOrientationTo(originCell);
     pullDistance = 0;
     i = 0;
     while (i < pullEffectForce)
     {
     nextCell = cell.getNearestCellInDirection(orientation);
     if (((nextCell) && (!(isBlockingCell(nextCell.get_cellId(), cell.get_cellId())))))
     {
     pullDistance++;
     cell = nextCell;
     }
     else
     {
     break;
     };
     i++;
     };
     pushForce = (pushEffectForce - pullDistance);
     }
     else
     {
     pushForce = pushEffectForce;
     };
     return (pushForce);
     }
     public static boolean isBlockingCell(short pCell,short pFromCell, boolean pCheckDiag)
     {
     MapPoint startCell;
     MapPoint destCell:MapPoint;
     bbyte direction:uint;
     var c1:MapPoint;
     var c2:MapPoint;
     var gc:GraphicCell = InteractiveCellManager.getInstance().getCell(pCell);
     var blocking:Boolean = ((((gc) && (!(gc.visible)))) || (EntitiesManager.getInstance().getEntityOnCell(pCell, AnimatedCharacter)));
     if (((!(blocking)) && (pCheckDiag)))
     {
     startCell = MapPoint.fromCellId(pFromCell);
     destCell = MapPoint.fromCellId(pCell);
     direction = startCell.orientationTo(destCell);
     if ((direction % 2) == 0)
     {
     switch (direction)
     {
     case DirectionsEnum.RIGHT:
     c1 = destCell.getNearestCellInDirection(DirectionsEnum.UP_LEFT);
     c2 = destCell.getNearestCellInDirection(DirectionsEnum.DOWN_LEFT);
     break;
     case DirectionsEnum.DOWN:
     c1 = destCell.getNearestCellInDirection(DirectionsEnum.UP_LEFT);
     c2 = destCell.getNearestCellInDirection(DirectionsEnum.UP_RIGHT);
     break;
     case DirectionsEnum.LEFT:
     c1 = destCell.getNearestCellInDirection(DirectionsEnum.UP_RIGHT);
     c2 = destCell.getNearestCellInDirection(DirectionsEnum.DOWN_RIGHT);
     break;
     case DirectionsEnum.UP:
     c1 = destCell.getNearestCellInDirection(DirectionsEnum.DOWN_LEFT);
     c2 = destCell.getNearestCellInDirection(DirectionsEnum.DOWN_RIGHT);
     break;
     };
     blocking = ((((c1) && (isBlockingCell(c1.cellId, -1, false)))) || (((c2) && (isBlockingCell(c2.cellId, -1, false)))));
     };
     };
     return (blocking);
     }*/
    public static boolean isPushableEntity(Fighter pEntityInfo) {
        boolean buffPreventPush = (((!((!pEntityInfo.hasState(6))) || (!((!pEntityInfo.hasState(97)))))));
        boolean canBePushed = true;
        if ((pEntityInfo instanceof MonsterFighter)) {
            canBePushed = ((MonsterFighter) pEntityInfo).getGrade().getMonster().isCanBePushed();
        } else if ((pEntityInfo instanceof StaticFighter)) {
            canBePushed = ((StaticFighter) pEntityInfo).getGrade().getMonster().isCanBePushed();
        }
        return (((!(buffPreventPush)) && (canBePushed)));
    }

}
