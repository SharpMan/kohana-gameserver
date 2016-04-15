package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layers.FightGlyph;

/**
 *
 * @author Neo-Craft
 */
public class EffectTriggersGlyph extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        final FightCell cell = castInfos.caster.getFight().getCell(castInfos.cellId);
        for (IFightObject Glyph : cell.getObjects(IFightObject.FightObjectType.OBJECT_GLYPHE)) {
            final int score = ((FightGlyph) Glyph).loadEnnemyTargetsAndActive(castInfos.caster, BuffActiveType.ACTIVE_BEGINTURN);
            if (score != -1) {
                return score;
            }
        }

        return -1;
    }

}
