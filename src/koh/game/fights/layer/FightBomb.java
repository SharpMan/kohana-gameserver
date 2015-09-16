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
import static koh.game.fights.layer.FightActivableObject.getRGB;
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
        return this.Cells.keySet().stream().map(C -> this.m_fight.GetCell(C));
    }

    public FightBomb(Fighter Caster, SpellLevel Spell, Color color, Short[] Cells, BombFighter[] Members) {
        m_fight = Caster.Fight;
        ID = (short) m_fight.NextTriggerUid.incrementAndGet();
        m_caster = Caster;
        m_spellId = Spell.spellId;
        m_spell_level = Spell.grade;
        m_actionEffect = Spell;
        ActivationType = BuffActiveType.ACTIVE_ENDMOVE;

        Color = color;
        Targets = new ArrayList<>();
        AffectedCells = Cells;
        Duration = 0;
        this.VisibileState = GameActionFightInvisibilityStateEnum.VISIBLE;
        Size = 0;
        this.Shape = GameActionMarkCellsTypeEnum.CELLS_CIRCLE;

        for (EffectInstanceDice effect : m_actionEffect.effects) {
            if (EffectCast.IsDamageEffect(effect.EffectType())) {
                Priority--;
            }
            if (effect.EffectType() == StatsEnum.PullForward || effect.EffectType() == StatsEnum.Push_Back) {
                Priority += 50;
            }
        }
        Cell = m_fight.GetCell(AffectedCells[0]);
        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : AffectedCells) {
            if(!this.m_fight.GetCell(cellId).IsWalkable())
                continue;
            this.Cells.put(cellId, (short) m_fight.NextTriggerUid.incrementAndGet());
            if (m_fight.GetCell(cellId) != null) {
                m_fight.GetCell(cellId).AddObject(this);
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
        //m_fight.StartSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        this.Cells.keySet().stream().forEach((cell) -> {
            this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.ID, this.Cells.get(cell)));
        });
        //m_fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized int Activate(Fighter activator) {
        Targets.removeIf(Fighter -> Fighter instanceof BombFighter);
        if (Targets.isEmpty()) {
            return -1;
        }
        return super.Activate(activator);
    }

    @Override
    public GameActionMarkTypeEnum GameActionMarkType() {
        return GameActionMarkTypeEnum.WALL;
    }

    @Override
    public GameActionMark GetHiddenGameActionMark() {
        return GetGameActionMark();
    }

    @Override
    public GameActionMark GetGameActionMark() {
        return null;
    }

    public GameActionMark GetGameActionMark(short cell) {
        return new GameActionMark(this.m_caster.ID, this.m_caster.Team.Id, this.m_spellId, this.m_spell_level, this.Cells.get(cell), GameActionMarkType().value(), cell, new GameActionMarkedCell[]{new GameActionMarkedCell(cell, this.Size, getRGB(Color), this.Shape.value)}, true);
    }

    @Override
    public FightObjectType ObjectType() {
        return FightObjectType.OBJECT_BOMB;
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
