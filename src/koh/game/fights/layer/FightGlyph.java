package koh.game.fights.layer;

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

/**
 *
 * @author Neo-Craft
 */
public class FightGlyph extends FightActivableObject {

    public FightGlyph(EffectCast castInfos, int duration, Color color, byte size, GameActionMarkCellsTypeEnum Shape) {
        super(BuffActiveType.ACTIVE_BEGINTURN, castInfos.Caster.Fight, castInfos.Caster, castInfos, castInfos.CellId, duration, color, GameActionFightInvisibilityStateEnum.VISIBLE, size, Shape);
    }

    @Override
    public void AppearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.m_caster.ID, GetGameActionMark()));
    }

    @Override
    public void Appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void DisappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.ID, this.ID));
    }

    @Override
    public GameActionMarkTypeEnum GameActionMarkType() {
        return GameActionMarkTypeEnum.GLYPH;
    }

    @Override
    public GameActionMark GetHiddenGameActionMark() {
       return GetGameActionMark();
    }

    @Override
    public GameActionMark GetGameActionMark() {
        return new GameActionMark(this.m_caster.ID, this.m_caster.Team.Id, this.m_spellId, this.m_spell_level, this.ID, GameActionMarkType().value(), this.CellId(), this.GetGameActionMarkedCell(), true);
    }

    @Override
    public FightObjectType ObjectType() {
        return FightObjectType.OBJECT_GLYPHE;
    }

    @Override
    public boolean CanWalk() {
        return true;
    }

    @Override
    public boolean CanStack() {
        return true;
    }

    @Override
    public boolean CanGoThrough() {
       return true;
    }

}
