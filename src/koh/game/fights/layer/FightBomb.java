package koh.game.fights.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.FightCell;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.fighters.BombFighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightMarkCellsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightUnmarkCellsMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.actions.fight.GameActionMarkedCell;

/**
 *
 * @author Neo-Craft
 */
public class FightBomb extends FightActivableObject {

    public HashMap<Short, Short> Cells = new HashMap<>();
    public BombFighter[] Owner = new BombFighter[2];
    
    public Stream<FightCell> FightCells(){
        return this.Cells.keySet().stream().map(C -> this.m_fight.getCell(C));
    }

    public FightBomb(Fighter Caster, SpellLevel Spell, Color color, Short[] Cells, BombFighter[] Members) {
        m_fight = Caster.fight;
        ID = (short) m_fight.NextTriggerUid.incrementAndGet();
        m_caster = Caster;
        m_spellId = Spell.getSpellId();
        m_spell_level = Spell.getGrade();
        m_actionEffect = Spell;
        activationType = BuffActiveType.ACTIVE_ENDMOVE;

        Color = color;
        targets = new ArrayList<>();
        affectedCells = Cells;
        Duration = 0;
        this.visibileState = GameActionFightInvisibilityStateEnum.VISIBLE;
        size = 0;
        this.shape = GameActionMarkCellsTypeEnum.CELLS_CIRCLE;

        for (EffectInstanceDice effect : m_actionEffect.effects) {
            if (EffectCast.IsDamageEffect(effect.EffectType())) {
                Priority--;
            }
            if (effect.EffectType() == StatsEnum.PullForward || effect.EffectType() == StatsEnum.Push_Back) {
                Priority += 50;
            }
        }
        Cell = m_fight.getCell(affectedCells[0]);
        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : affectedCells) {
            if(!this.m_fight.getCell(cellId).IsWalkable())
                continue;
            this.Cells.put(cellId, (short) m_fight.NextTriggerUid.incrementAndGet());
            if (m_fight.getCell(cellId) != null) {
                m_fight.getCell(cellId).AddObject(this);
            }
        }
        this.Owner = Members;
        Arrays.stream(Members).forEach(x -> x.addBomb(this));

        AppearForAll();

    }

    @Override
    public void AppearForAll() {
        this.Cells.keySet().stream().forEach((cell) -> {
            this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.m_caster.ID, GetGameActionMark(cell)));
        });
    }

    @Override
    public void Appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void DisappearForAll() {
        //m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        this.Cells.keySet().stream().forEach((cell) -> {
            this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.ID, this.Cells.get(cell)));
        });
        //m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized int activate(Fighter activator) {
        targets.removeIf(Fighter -> Fighter instanceof BombFighter);
        if (targets.isEmpty()) {
            return -1;
        }
        return super.activate(activator);
    }

    @Override
    public GameActionMarkTypeEnum getGameActionMarkType() {
        return GameActionMarkTypeEnum.WALL;
    }

    @Override
    public GameActionMark getHiddenGameActionMark() {
        return getGameActionMark();
    }

    @Override
    public GameActionMark getGameActionMark() {
        return null;
    }

    public GameActionMark GetGameActionMark(short cell) {
        return new GameActionMark(this.m_caster.ID, this.m_caster.team.Id, this.m_spellId, this.m_spell_level, this.Cells.get(cell), getGameActionMarkType().value(), cell, new GameActionMarkedCell[]{new GameActionMarkedCell(cell, this.size, getRGB(Color), this.shape.value)}, true);
    }

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_BOMB;
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
