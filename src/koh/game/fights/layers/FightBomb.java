package koh.game.fights.layers;

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

    public HashMap<Short, Short> cells = new HashMap<>();
    public BombFighter[] owner = new BombFighter[2];
    
    public Stream<FightCell> getFightCells(){
        return this.cells.keySet().stream().map(cellid -> this.m_fight.getCell(cellid));
    }

    public FightBomb(Fighter Caster, SpellLevel Spell, Color color, Short[] Cells, BombFighter[] Members) {
        m_fight = Caster.getFight();
        ID = (short) m_fight.getNextTriggerUid().incrementAndGet();
        caster = Caster;
        m_spellId = Spell.getSpellId();
        m_spell_level = Spell.getGrade();
        castSpell = Spell;
        activationType = BuffActiveType.ACTIVE_ENDMOVE;

        this.color = color;
        targets = new ArrayList<>();
        affectedCells = Cells;
        duration = 0;
        this.visibileState = GameActionFightInvisibilityStateEnum.VISIBLE;
        size = 0;
        this.shape = GameActionMarkCellsTypeEnum.CELLS_CIRCLE;

        for (EffectInstanceDice effect : castSpell.getEffects()) {
            if (EffectCast.isDamageEffect(effect.getEffectType())) {
                priority--;
            }
            if (effect.getEffectType() == StatsEnum.PULL_FORWARD || effect.getEffectType() == StatsEnum.PUSH_BACK) {
                priority += 50;
            }
        }
        cell = m_fight.getCell(affectedCells[0]);
        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : affectedCells) {
            if(!this.m_fight.getCell(cellId).isWalkable())
                continue;
            this.cells.put(cellId, (short) m_fight.getNextTriggerUid().incrementAndGet());
            if (m_fight.getCell(cellId) != null) {
                m_fight.getCell(cellId).addObject(this);
            }
        }
        this.owner = Members;
        Arrays.stream(Members).forEach(x -> x.addBomb(this));

        appearForAll();

    }

    @Override
    public void appearForAll() {
        this.cells.keySet().stream().forEach((cell) -> {
            this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.caster.getID(), GetGameActionMark(cell)));
        });
    }

    @Override
    public void appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disappearForAll() {
        //m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        this.cells.keySet().stream().forEach((cell) -> {
            this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.caster.getID(), this.cells.get(cell)));
        });
        //m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized void loadTargets(Fighter target) {
        if (!targets.contains(target)) {
            targets.add(target);
        }
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
        return new GameActionMark(this.caster.getID(), this.caster.getTeam().id, this.m_spellId, this.m_spell_level, this.cells.get(cell), getGameActionMarkType().value(), cell, new GameActionMarkedCell[]{new GameActionMarkedCell(cell, this.size, getRGB(color), this.shape.value)}, true);
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
