package koh.game.fights.layer;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import koh.game.dao.SpellDAO;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.fighters.BombFighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightTriggerGlyphTrapMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.actions.fight.GameActionMarkedCell;

/**
 *
 * @author Neo-Craft
 */
public abstract class FightActivableObject implements IFightObject {

    public int Priority;
    public BuffActiveType ActivationType;
    public Color Color;
    public int Duration;
    public boolean Activated;
    public FightCell Cell;
    public ArrayList<Fighter> Targets;
    protected Fight m_fight;
    public Fighter m_caster;
    protected SpellLevel m_actionEffect;
    public int m_spellId;
    public byte m_spell_level;
    public Short AffectedCells[];
    public GameActionFightInvisibilityStateEnum VisibileState;
    public byte Size;
    public GameActionMarkCellsTypeEnum Shape;
    public short ID;
    
    protected MapPoint CachedMapPoints;

    public abstract void AppearForAll();

    public abstract void Appear(FightTeam dispatcher);

    public abstract void DisappearForAll();

    public FightActivableObject() {

    }

    public FightActivableObject(BuffActiveType activeType, Fight fight, Fighter caster, EffectCast castInfos, short cell, int duration, Color color, GameActionFightInvisibilityStateEnum visibleState, byte size, GameActionMarkCellsTypeEnum shap) {
        ID = (short) fight.NextTriggerUid.incrementAndGet();
        m_fight = fight;
        m_caster = caster;
        m_spellId = castInfos.SpellId;
        m_spell_level = castInfos.SpellLevel.grade;
        try{ m_actionEffect = SpellDAO.Spells.get(castInfos.Effect.diceNum).spellLevels[castInfos.Effect.diceSide - 1]; } catch(NullPointerException | ArrayIndexOutOfBoundsException e) {}
        Cell = fight.GetCell(cell);
        ActivationType = activeType;
        Color = color;
        Targets = new ArrayList<>();
        AffectedCells = (shap == GameActionMarkCellsTypeEnum.CELLS_CROSS ? new Zone(SpellShapeEnum.Q, size) : (shap == GameActionMarkCellsTypeEnum.CELLS_CIRCLE ? new Zone(SpellShapeEnum.C, size) : new Zone(SpellShapeEnum.G, size))).GetCells(Cell.Id);
        Duration = duration;
        this.VisibileState = visibleState;
        Size = size;
        Shape = shap;

        if(m_actionEffect != null)
        for (EffectInstanceDice effect : m_actionEffect.effects) {
            if (EffectCast.IsDamageEffect(effect.EffectType())) {
                Priority--;
            }
            if (effect.EffectType() == StatsEnum.PullForward || effect.EffectType() == StatsEnum.Push_Back) {
                Priority += 50;
            }
        }

        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : this.AffectedCells) {
            if (m_fight.GetCell(cellId) != null) {
                m_fight.GetCell(cellId).AddObject(this);
            }
        }

        if (visibleState == GameActionFightInvisibilityStateEnum.INVISIBLE) {
            Appear(caster.Team);
        } else {
            AppearForAll();
        }
    }

    public synchronized void LoadTargets(Fighter target) {
        if (!Targets.contains(target)) {
            Targets.add(target);
        }

        switch (ActivationType) {
            case ACTIVE_ENDMOVE:
                FightCell Cell = null;
                for (short cell : AffectedCells) {
                    Cell = m_fight.GetCell(cell);
                    if (Cell != null) {
                        Targets.addAll(Cell.GetObjectsAsFighterList(x -> x.ObjectType() == FightObjectType.OBJECT_FIGHTER && !Targets.contains(x)));
                        //Targets.AddRange(cell.FightObjects.OfType < FighterBase > ().Where(fighter =  > !Targets.Contains(fighter)));
                    }
                }
                break;
        }
    }
    
    public void Enable(Fighter fighter){
        
    }

    public int LoadEnnemyTargetsAndActive(Fighter target) {
        FightCell Cell = null;
        for (short cell : AffectedCells) {
            Cell = m_fight.GetCell(cell);
            if (Cell != null) {
                Targets.addAll(Cell.GetObjectsAsFighterList(x -> x.ObjectType() == FightObjectType.OBJECT_FIGHTER && !Targets.contains(x) && ((Fighter) x).IsEnnemyWith(target)));
            }
        }
        return this.Activate(target);
    }

    public MapPoint MapPoint(){
        if(CachedMapPoints == null){
            this.CachedMapPoints = MapPoint.fromCellId(this.CellId());
        }
        return this.CachedMapPoints;
    }
    
    public synchronized int Activate(Fighter activator) {
        Activated = true;
        if (this.ObjectType() == FightObjectType.OBJECT_TRAP) {
            Remove();
        }

        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(GameActionMarkType() == GameActionMarkTypeEnum.GLYPH ? ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH : ActionIdEnum.ACTION_FIGHT_TRIGGER_TRAP, this.m_caster.ID, this.ID, activator.ID, this.m_spellId));
        activator.Fight.StartSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        for (EffectInstanceDice Effect : m_actionEffect.effects) {
            EffectCast CastInfos = new EffectCast(Effect.EffectType(), this.m_actionEffect.spellId, Cell.Id, 100, Effect, m_caster, Targets, false, StatsEnum.NONE, 0, this.m_actionEffect);
            CastInfos.IsTrap = this.ObjectType() == FightObjectType.OBJECT_TRAP;
            if (EffectBase.TryApplyEffect(CastInfos) == -3) {
                Targets.clear();
                activator.Fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
                return -3;
            }
        }

        Targets.clear();

        activator.Fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return -1;
    }

    public void DecrementDuration() {
        Duration--;

        if (Duration <= 0) {
            Remove();
        }
    }

    public void Remove() {
        DisappearForAll();

        for (short cell : AffectedCells) {
            this.m_fight.GetCell(cell).RemoveObject(this);
        }
        if (this.m_fight.m_activableObjects.containsKey(this.m_caster)) {
            this.m_fight.m_activableObjects.get(this.m_caster).remove(this);
        }

    }

    public abstract GameActionMarkTypeEnum GameActionMarkType();

    public abstract GameActionMark GetHiddenGameActionMark();

    public abstract GameActionMark GetGameActionMark();

    public GameActionMarkedCell[] GetGameActionMarkedCell() {
        if (Shape == GameActionMarkCellsTypeEnum.CELLS_SQUARE) {
            return Arrays.stream(this.AffectedCells).map(x -> new GameActionMarkedCell(x, (byte) 0, getRGB(Color), this.Shape.value)).toArray(GameActionMarkedCell[]::new);
        } else {
            return new GameActionMarkedCell[]{new GameActionMarkedCell(this.CellId(), this.Size, getRGB(Color), this.Shape.value)};
        }
    }

    @Override
    public short CellId() {
        return this.Cell.Id;
    }

    public static int getRGB(Color col) {
        int R = (int) Math.round(255 * col.getRed());
        int G = (int) Math.round(255 * col.getGreen());
        int B = (int) Math.round(255 * col.getBlue());

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    @Override
    public Integer Priority() {
        return Priority;
    }

    @Override
    public int compareTo(IFightObject obj) {
        return Priority().compareTo(obj.Priority());
    }

}
