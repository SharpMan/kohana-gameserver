package koh.game.fights.effects;

import java.util.Arrays;
import koh.game.fights.FightCell;
import koh.game.fights.IFightObject;
import koh.game.fights.layer.FightGlyph;

/**
 *
 * @author Neo-Craft
 */
public class EffectTriggersGlyph extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        FightCell Cell = CastInfos.Caster.Fight.GetCell(CastInfos.CellId);
        Arrays.stream(Cell.GetObjects(IFightObject.FightObjectType.OBJECT_GLYPHE)).forEach(Glyph -> ((FightGlyph) Glyph).LoadEnnemyTargetsAndActive(CastInfos.Caster));

        return -1;
    }

}
