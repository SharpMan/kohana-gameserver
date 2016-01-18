package koh.game.fights.layers;

import javafx.scene.paint.Color;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
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
import koh.protocol.types.game.actions.fight.GameActionMarkedCell;

import static koh.protocol.client.enums.GameActionFightInvisibilityStateEnum.VISIBLE;

/**
 * @author Neo-Craft
 */
public class FightTrap extends FightActivableObject {

    public FightTrap(EffectCast castInfos, int duration, Color color, byte size, GameActionMarkCellsTypeEnum shap) {
        super(BuffActiveType.ACTIVE_ENDMOVE, castInfos.caster.getFight(), castInfos.caster, castInfos, castInfos.cellId, duration, color, GameActionFightInvisibilityStateEnum.INVISIBLE, size, shap);
    }

    @Override
    public void appearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.caster.getID(), getGameActionMark()));
    }

    @Override
    public void appear(FightTeam dispatcher) {
        this.m_fight.sendToField(new FieldNotification(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.caster.getID(), getHiddenGameActionMark())) {
            @Override
            public boolean can(Player perso) {
                return !(perso.getClient() != null
                        && perso.getFighter() != null
                        && perso.getFighter().getTeam().id == dispatcher.id);
            }
        });
        dispatcher.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_TRAP_CASTING_SPELL, this.caster.getID(), getGameActionMark()));
    }

    @Override
    public void disappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.caster.getID(), this.ID));
    }

    @Override
    public GameActionMarkTypeEnum getGameActionMarkType() {
        return GameActionMarkTypeEnum.TRAP;
    }

    @Override
    public GameActionMark getHiddenGameActionMark() {
        return new GameActionMark(this.caster.getID(), this.caster.getTeam().id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.visibileState == VISIBLE ? this.getCellId() : (short) -1, new GameActionMarkedCell[0], true);
    }

    @Override
    public GameActionMark getGameActionMark() {
        return new GameActionMark(this.caster.getID(), this.caster.getTeam().id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.getCellId(), this.getGameActionMarkedCell(), true);
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
