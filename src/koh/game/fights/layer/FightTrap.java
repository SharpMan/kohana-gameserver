package koh.game.fights.layer;

import javafx.scene.paint.Color;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import static koh.protocol.client.enums.GameActionFightInvisibilityStateEnum.VISIBLE;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightMarkCellsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightUnmarkCellsMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.actions.fight.GameActionMarkedCell;

/**
 *
 * @author Neo-Craft
 */
public class FightTrap extends FightActivableObject {

    public FightTrap(EffectCast castInfos, int duration, Color color, byte size, GameActionMarkCellsTypeEnum shap) {
        super(BuffActiveType.ACTIVE_ENDMOVE, castInfos.Caster.Fight, castInfos.Caster, castInfos, castInfos.CellId, duration, color, GameActionFightInvisibilityStateEnum.INVISIBLE, size, shap);
    }

    @Override
    public void AppearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, GetGameActionMark()));
    }

    @Override
    public void Appear(FightTeam dispatcher) {
        this.m_fight.sendToField(new FieldNotification(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, GetHiddenGameActionMark())) {
            @Override
            public boolean can(Player perso) {
                return !(perso.Client != null && perso.GetFighter() != null && perso.GetFighter().Team.Id == dispatcher.Id);
            }
        });
        dispatcher.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, GetGameActionMark()));
    }

    @Override
    public void DisappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.ID, this.ID));
    }

    @Override
    public GameActionMarkTypeEnum GameActionMarkType() {
        return GameActionMarkTypeEnum.TRAP;
    }

    @Override
    public GameActionMark GetHiddenGameActionMark() {
        return new GameActionMark(this.m_caster.ID, this.m_caster.Team.Id, this.m_spellId, this.m_spell_level, this.ID, GameActionMarkType().value(), this.VisibileState == VISIBLE ? this.CellId() : (short) -1, new GameActionMarkedCell[0], true);
    }

    @Override
    public GameActionMark GetGameActionMark() {
        return new GameActionMark(this.m_caster.ID, this.m_caster.Team.Id, this.m_spellId, this.m_spell_level, this.ID, GameActionMarkType().value(), this.CellId(), this.GetGameActionMarkedCell(), true);
    }

    @Override
    public FightObjectType ObjectType() {
        return FightObjectType.OBJECT_TRAP;
    }

    @Override
    public boolean CanWalk() {
        return true;
    }

    @Override
    public boolean CanStack() {
        return false;
    }

    @Override
    public boolean CanGoThrough() {
        return true;
    }

}
