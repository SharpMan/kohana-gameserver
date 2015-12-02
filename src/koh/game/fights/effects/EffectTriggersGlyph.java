package koh.game.fights.effects;

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
        FightCell Cell = CastInfos.Caster.fight.getCell(CastInfos.CellId);
        for (IFightObject Glyph : Cell.GetObjects(IFightObject.FightObjectType.OBJECT_GLYPHE)) {
            int Score = ((FightGlyph) Glyph).LoadEnnemyTargetsAndActive(CastInfos.Caster);
            if (Score == -3) {
                return Score;
            }
        }

        return -1;
    }

}
