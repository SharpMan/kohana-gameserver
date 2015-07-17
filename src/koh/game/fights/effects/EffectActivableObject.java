package koh.game.fights.effects;

import javafx.scene.paint.Color;
import koh.game.fights.IFightObject;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightGlyph;
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
                if (CastInfos.Caster.Fight.HasObjectOnCell(FightObjectType.OBJECT_FIGHTER, CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightGlyph(CastInfos, CastInfos.Duration, GetColor(CastInfos.SpellId), CastInfos.Effect.ZoneSize(), CastInfos.Effect.ZoneShape() == SpellShapeEnum.G ? GameActionMarkCellsTypeEnum.CELLS_SQUARE : CastInfos.Effect.ZoneShape() == SpellShapeEnum.Q ? GameActionMarkCellsTypeEnum.CELLS_CROSS : GameActionMarkCellsTypeEnum.CELLS_CIRCLE);
                break;

            case LAYING_TRAP_LEVEL:
                if (!CastInfos.Caster.Fight.CanPutObject(CastInfos.CellId)) {
                    return -1;
                }
                obj = new FightTrap(CastInfos, 0, GetColor(CastInfos.SpellId), CastInfos.Effect.ZoneSize(), CastInfos.Effect.ZoneShape() == SpellShapeEnum.G ? GameActionMarkCellsTypeEnum.CELLS_SQUARE : CastInfos.Effect.ZoneShape() == SpellShapeEnum.Q ? GameActionMarkCellsTypeEnum.CELLS_CROSS : GameActionMarkCellsTypeEnum.CELLS_CIRCLE);
                break;
        }

        if (obj != null) {
            CastInfos.Caster.Fight.AddActivableObject(CastInfos.Caster, obj);
        }
        return -1;
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
