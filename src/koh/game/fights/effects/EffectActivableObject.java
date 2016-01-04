package koh.game.fights.effects;

import javafx.scene.paint.Color;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightGlyph;
import koh.game.fights.layer.FightPortal;
import koh.game.fights.layer.FightTrap;
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
        switch (CastInfos.EffectType) {
            case LAYING_GLYPH_RANKED:
            case LAYING_GLYPH_RANKED_2:
            case LAYING_GLYPH:
                if (CastInfos.caster.getFight().hasObjectOnCell(FightObjectType.OBJECT_FIGHTER, CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightGlyph(CastInfos, CastInfos.Duration, GetColor(CastInfos.SpellId), CastInfos.effect.ZoneSize(), GetMarkType(CastInfos.effect.ZoneShape()));
                break;

            case LAYING_TRAP_LEVEL:
                if (!CastInfos.caster.getFight().canPutObject(CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightTrap(CastInfos, 0, GetColor(CastInfos.SpellId), CastInfos.effect.ZoneSize(), GetMarkType(CastInfos.effect.ZoneShape()));
                break;
            case LAYING_PORTAIL:
                if (CastInfos.getCell().HasGameObject(FightObjectType.OBJECT_PORTAL)) {
                    ((FightPortal) CastInfos.getCell().GetObjects(FightObjectType.OBJECT_PORTAL)[0]).remove();
                } else if (CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster) != null
                        && CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster).stream().filter(Object -> Object instanceof FightPortal).count() > 3) {
                    CastInfos.caster.getFight().getActivableObjects().get(CastInfos.caster).stream().findFirst().get().remove();
                }
                /*if (!castInfos.caster.getFight().canPutObject(castInfos.getCellId)) {
                 return -1;
                 }*/
                obj = new FightPortal(CastInfos.caster.getFight(), CastInfos.caster, CastInfos, CastInfos.CellId);
                break;
        }

        if (obj != null) {
            CastInfos.caster.getFight().addActivableObject(CastInfos.caster, obj);
        }
        return -1;
    }

    public static GameActionMarkCellsTypeEnum GetMarkType(SpellShapeEnum Shape) {
        switch (Shape) {
            case P:
            case G:
                return GameActionMarkCellsTypeEnum.CELLS_SQUARE;
            case Q:
                return GameActionMarkCellsTypeEnum.CELLS_CROSS;

            default:
                return GameActionMarkCellsTypeEnum.CELLS_CIRCLE;
        }
    }

    public static Color GetColor(int Spell) {
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
