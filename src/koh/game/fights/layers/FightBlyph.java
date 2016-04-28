package koh.game.fights.layers;

import javafx.scene.paint.Color;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightTriggerGlyphTrapMessage;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

/**
 * Created by Melancholia on 4/26/16.
 */
public class FightBlyph extends FightGlyph {

    public FightBlyph(EffectCast castInfos, int duration, Color color, byte size, GameActionMarkCellsTypeEnum Shape) {
        super(castInfos, duration, color, size, Shape);
        this.activationType = BuffActiveType.ACTIVE_ENDTURN;
    }

    @Override
    public synchronized int activate(Fighter activator, BuffActiveType activationType) {
        this.activated = true;
        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage( ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH, this.caster.getID(), this.ID, activator.getID(), this.m_spellId));
        activator.getFight().startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        final ArrayList<Fighter> targetsPerEffect = new ArrayList<>(3);
        int bestResult = -1;
        for (EffectInstanceDice effect : castSpell.getEffects()) {
            if(activationType != BuffActiveType.ACTIVE_ENDTURN && ArrayUtils.contains(EffectHelper.DAMAGE_EFFECTS_IDS,effect.effectId)){
                continue;
            }
            else if (activationType == BuffActiveType.ACTIVE_ENDTURN && !ArrayUtils.contains(EffectHelper.DAMAGE_EFFECTS_IDS,effect.effectId)){
                continue;
            }
            targetsPerEffect.addAll(targets);
            targetsPerEffect.removeIf(target -> !(EffectHelper.verifyEffectTrigger(caster, target, castSpell.getEffects(), effect, false, effect.triggers, target.getCellId())
                    && effect.isValidTarget(caster, target)
                    && EffectInstanceDice.verifySpellEffectMask(caster, target, effect)));

            final EffectCast castInfos = new EffectCast(effect.getEffectType(), this.castSpell.getSpellId(), cell.Id, 100, effect, caster, targetsPerEffect, false, StatsEnum.NONE, 0, this.castSpell);
            castInfos.isGlyph = true;
            castInfos.glyphId = this.ID;
            final int result = EffectBase.tryApplyEffect(castInfos);
            if(result < bestResult){
                bestResult = result;
            }
            if (result == -3) {
                targetsPerEffect.clear();
                targets.clear();
                activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
                return result;
            }
            targetsPerEffect.clear();

        }
        targets.clear();

        activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return bestResult;
    }

}
