package koh.game.fights.layer;

import javafx.scene.paint.Color;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.fights.FightTeam;
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
        super(BuffActiveType.ACTIVE_ENDMOVE, castInfos.Caster.fight, castInfos.Caster, castInfos, castInfos.CellId, duration, color, GameActionFightInvisibilityStateEnum.INVISIBLE, size, shap);
    }

    @Override
    public void AppearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, getGameActionMark()));
    }

    @Override
    public void Appear(FightTeam dispatcher) {
        this.m_fight.sendToField(new FieldNotification(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, getHiddenGameActionMark())) {
            @Override
            public boolean can(Player perso) {
                return !(perso.getClient() != null && perso.getFighter() != null && perso.getFighter().team.Id == dispatcher.Id);
            }
        });
        dispatcher.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.m_caster.ID, getGameActionMark()));
    }

    @Override
    public void DisappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.ID, this.ID));
    }

    @Override
    public GameActionMarkTypeEnum getGameActionMarkType() {
        return GameActionMarkTypeEnum.TRAP;
    }

    @Override
    public GameActionMark getHiddenGameActionMark() {
        return new GameActionMark(this.m_caster.ID, this.m_caster.team.Id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.visibileState == VISIBLE ? this.getCellId() : (short) -1, new GameActionMarkedCell[0], true);
    }

    @Override
    public GameActionMark getGameActionMark() {
        return new GameActionMark(this.m_caster.ID, this.m_caster.team.Id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.getCellId(), this.getGameActionMarkedCell(), true);
    }

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_TRAP;
    }

    @Override
    public boolean canWalk() {
        return true;
    }

    @Override
    public boolean canStack() {
        return false;
    }

    @Override
    public boolean canGoThrough() {
        return true;
    }

}
