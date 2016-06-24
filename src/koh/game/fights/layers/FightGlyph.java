package koh.game.fights.layers;

import javafx.scene.paint.Color;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.actions.fight.GameActionFightMarkCellsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTriggerGlyphTrapMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightUnmarkCellsMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

/**
 *
 * @author Neo-Craft
 */
public class FightGlyph extends FightActivableObject {

    @Getter
    @Setter
    private int lastTurnActivated;

    public FightGlyph(EffectCast castInfos, int duration, Color color, byte size, GameActionMarkCellsTypeEnum Shape) {
        super(BuffActiveType.ACTIVE_BEGINTURN, castInfos.caster.getFight(), castInfos.caster, castInfos, castInfos.cellId, duration, color, GameActionFightInvisibilityStateEnum.VISIBLE, size, Shape);
    }

    @Override
    public void appearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.caster.getID(), getGameActionMark()));
    }

    @Override
    public void appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.caster.getID(), this.ID));
    }

    @Override
    public synchronized int activate(Fighter activator, BuffActiveType activationType) {
        this.activated = true;
        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage( ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH, this.caster.getID(), this.ID, activator.getID(), this.m_spellId));
        activator.getFight().startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        final ArrayList<Fighter> targetsPerEffect = new ArrayList<>(3);
        int bestResult = -1;
        for (EffectInstanceDice effect : castSpell.getEffects()) {
            if(activationType != BuffActiveType.ACTIVE_BEGINTURN && ArrayUtils.contains(EffectHelper.DAMAGE_EFFECTS_IDS,effect.effectId)){
                continue;
            }
            else if (activationType == BuffActiveType.ACTIVE_BEGINTURN && !ArrayUtils.contains(EffectHelper.DAMAGE_EFFECTS_IDS,effect.effectId)){
                continue;
            }
            this.logger.debug("** {}",effect.toString());
            targetsPerEffect.addAll(targets);
            targetsPerEffect.removeIf(target -> !(EffectHelper.verifyEffectTrigger(caster, target, castSpell.getEffects(), effect, false, effect.triggers, target.getCellId())
                    && effect.isValidTarget(caster, target)
                    && EffectInstanceDice.verifySpellEffectMask(caster, target, effect,target.getCellId())));

            final EffectCast castInfos = new EffectCast(effect.getEffectType(), this.castSpell.getSpellId(), cell.Id, 100, effect, caster, targetsPerEffect, false, StatsEnum.NONE, 0, this.castSpell);
            castInfos.isGlyph = true;
            castInfos.glyphId = this.ID;
            final int result = EffectBase.tryApplyEffect(castInfos);
            /*System.out.println(effect.toString());
            System.out.println("a"+result);*/
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

    @Override
    public GameActionMarkTypeEnum getGameActionMarkType() {
        return GameActionMarkTypeEnum.GLYPH;
    }

    @Override
    public GameActionMark getHiddenGameActionMark() {
       return getGameActionMark();
    }

    @Override
    public GameActionMark getGameActionMark() {
        return new GameActionMark(this.caster.getID(), this.caster.getTeam().id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.getCellId(), this.getGameActionMarkedCell(), true);
    }

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_GLYPHE;
    }

    @Override
    public boolean canWalk() {
        return true;
    }

    @Override
    public boolean canStack() {
        return true;
    }

    @Override
    public boolean canGoThrough() {
       return true;
    }

}
