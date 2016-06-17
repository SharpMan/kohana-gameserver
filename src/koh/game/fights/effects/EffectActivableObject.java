package koh.game.fights.effects;

import javafx.scene.paint.Color;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layers.*;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.SpellShapeEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectActivableObject extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        FightActivableObject obj = null;
        switch (castInfos.effectType) {
            case LAYING_BLYPH:
                obj = new FightBlyph(castInfos, castInfos.duration, getColor(castInfos.spellId), castInfos.effect.zoneSize(), getMarkType(castInfos.effect.getZoneShape()));
                obj.loadEnnemyTargetsAndActive(castInfos.caster, BuffActiveType.ACTIVE_ENDMOVE);
                //obj.activate(castInfos.caster, BuffActiveType.ACTIVE_ENDMOVE);
                break;
            case LAYING_GLYPH_RANKED:
            case LAYING_GLYPH:
                obj = new FightGlyph(castInfos, castInfos.duration, getColor(castInfos.spellId), castInfos.effect.zoneSize(), getMarkType(castInfos.effect.getZoneShape()));
                obj.loadEnnemyTargetsAndActive(castInfos.caster, BuffActiveType.ACTIVE_ENDMOVE);
                //obj.activate(castInfos.caster, BuffActiveType.ACTIVE_ENDMOVE);
                break;

            case LAYING_TRAP_LEVEL:
                if (!castInfos.caster.getFight().canPutObject(castInfos.cellId)) {
                    return -1;
                }
                obj = new FightTrap(castInfos, 0, getColor(castInfos.spellId), castInfos.effect.zoneSize(), getMarkType(castInfos.effect.getZoneShape()));
                break;
            case LAYING_PORTAIL:
                if (castInfos.getCell().hasGameObject(FightObjectType.OBJECT_PORTAL)) {

                    if(!((FightPortal) castInfos.getCell().getObjects(FightObjectType.OBJECT_PORTAL)[0]).enabled){
                        break;
                    }
                    ((FightPortal) castInfos.getCell().getObjects(FightObjectType.OBJECT_PORTAL)[0]).remove();
                } else if (castInfos.caster.getFight().getActivableObjects().get(castInfos.caster) != null
                        && castInfos.caster.getFight().getActivableObjects().get(castInfos.caster)
                        .stream()
                        .filter(ob -> ob instanceof FightPortal)
                        .count() > 3) {
                    castInfos.caster.getFight().getActivableObjects().get(castInfos.caster).stream().findFirst().get().remove();
                }
                /*if (!castInfos.caster.getFight().canPutObject(castInfos.getCellId)) {
                 return -1;
                 }*/
                obj = new FightPortal(castInfos.caster.getFight(), castInfos.caster, castInfos, castInfos.cellId);
                break;
        }

        if (obj != null) {
            castInfos.caster.getFight().addActivableObject(castInfos.caster, obj);
        }
        return -1;
    }

    public static GameActionMarkCellsTypeEnum getMarkType(SpellShapeEnum Shape) {
        switch (Shape) {
            case C:
            case Q:
            case star:
            case P:
            case G:
                return GameActionMarkCellsTypeEnum.CELLS_SQUARE;

                //return GameActionMarkCellsTypeEnum.CELLS_CROSS;
            default:
                return GameActionMarkCellsTypeEnum.CELLS_CIRCLE;
        }
    }

    public static final Color getColor(int Spell) {
        switch (Spell) {
            case 77:
            case 13:
                return Color.BLUE;
            case 367:
                return Color.WHITE;
            case 2825:
            case 10:
                return Color.RED;
            case 69:
            case 17:
                return Color.LIGHTGREEN;
            case 80:
                return Color.BLACK;
            case 16:
                return Color.YELLOW;
            case 65:
            case 79:
            case 12:
                return Color.BROWN;
            case 71:
                return Color.VIOLET;
            case 73:
            case 2833:
            case 15:
                return Color.LIGHTBLUE;
            case 2829:
                return Color.GREEN;
            default:
                return Color.BROWN;
        }
    }

}
