package koh.game.fights.effects;

import javafx.scene.paint.Color;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightGlyph;
import koh.game.fights.layers.FightPortal;
import koh.game.fights.layers.FightTrap;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.SpellShapeEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectActivableObject extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        FightActivableObject obj = null;
        switch (CastInfos.effectType) {
            case LAYING_GLYPH_RANKED:
            case LAYING_GLYPH_RANKED_2:
            case LAYING_GLYPH:
                obj = new FightGlyph(CastInfos, CastInfos.duration, getColor(CastInfos.spellId), CastInfos.effect.zoneSize(), GetMarkType(CastInfos.effect.ZoneShape()));
                break;

            case LAYING_TRAP_LEVEL:
                if (!CastInfos.caster.getFight().canPutObject(CastInfos.cellId)) {
                    return -1;
                }
                obj = new FightTrap(CastInfos, 0, getColor(CastInfos.spellId), CastInfos.effect.zoneSize(), GetMarkType(CastInfos.effect.ZoneShape()));
                break;
            case LAYING_PORTAIL:
                if (CastInfos.getCell().hasGameObject(FightObjectType.OBJECT_PORTAL)) {
                    ((FightPortal) CastInfos.getCell().getObjects(FightObjectType.OBJECT_PORTAL)[0]).remove();
                } else if (CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster) != null
                        && CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster).stream().filter(Object -> Object instanceof FightPortal).count() > 3) {
                    CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster).stream().findFirst().get().remove();
                }
                /*if (!castInfos.caster.getFight().canPutObject(castInfos.getCellId)) {
                 return -1;
                 }*/
                obj = new FightPortal(CastInfos.caster.getFight(), CastInfos.caster, CastInfos, CastInfos.cellId);
                break;
        }

        if (obj != null) {
            CastInfos.caster.getFight().addActivableObject(CastInfos.caster, obj);
        }
        return -1;
    }

    public static GameActionMarkCellsTypeEnum GetMarkType(SpellShapeEnum Shape) {
        System.out.println(Shape);
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
