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
    public int ApplyEffect(EffectCast CastInfos) {
        FightActivableObject obj = null;
        switch (CastInfos.EffectType) {
            case LAYING_GLYPH_RANKED:
            case LAYING_GLYPH_RANKED_2:
            case LAYING_GLYPH:
                if (CastInfos.Caster.fight.hasObjectOnCell(FightObjectType.OBJECT_FIGHTER, CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightGlyph(CastInfos, CastInfos.Duration, GetColor(CastInfos.SpellId), CastInfos.Effect.ZoneSize(), GetMarkType(CastInfos.Effect.ZoneShape()));
                break;

            case LAYING_TRAP_LEVEL:
                if (!CastInfos.Caster.fight.canPutObject(CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightTrap(CastInfos, 0, GetColor(CastInfos.SpellId), CastInfos.Effect.ZoneSize(), GetMarkType(CastInfos.Effect.ZoneShape()));
                break;
            case LAYING_PORTAIL:
                if (CastInfos.Cell().HasGameObject(FightObjectType.OBJECT_PORTAL)) {
                    ((FightPortal) CastInfos.Cell().GetObjects(FightObjectType.OBJECT_PORTAL)[0]).remove();
                } else if (CastInfos.Caster.fight.m_activableObjects.get(CastInfos.Caster) != null
                        && CastInfos.Caster.fight.m_activableObjects.get(CastInfos.Caster).stream().filter(Object -> Object instanceof FightPortal).count() > 3) {
                    CastInfos.Caster.fight.m_activableObjects.get(CastInfos.Caster).stream().findFirst().get().remove();
                }
                /*if (!CastInfos.Caster.fight.canPutObject(CastInfos.getCellId)) {
                 return -1;
                 }*/
                obj = new FightPortal(CastInfos.Caster.fight, CastInfos.Caster, CastInfos, CastInfos.CellId);
                break;
        }

        if (obj != null) {
            CastInfos.Caster.fight.addActivableObject(CastInfos.Caster, obj);
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
