package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.IFightObject;
import koh.game.fights.layers.FightGlyph;

/**
 *
 * @author Neo-Craft
 */
public class EffectTriggersGlyph extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        FightCell cell = castInfos.caster.getFight().getCell(castInfos.cellId);
        for (IFightObject Glyph : cell.getObjects(IFightObject.FightObjectType.OBJECT_GLYPHE)) {
            int Score = ((FightGlyph) Glyph).loadEnnemyTargetsAndActive(castInfos.caster);
            if (Score == -3) {
                return Score;
            }
        }

        return -1;
    }

}
