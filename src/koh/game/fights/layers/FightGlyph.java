package koh.game.fights.layers;

import javafx.scene.paint.Color;
import koh.game.fights.FightTeam;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightMarkCellsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightUnmarkCellsMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import lombok.Getter;
import lombok.Setter;

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
