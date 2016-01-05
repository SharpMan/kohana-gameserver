package koh.game.fights.layer;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
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
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
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
    public BuffActiveType activationType;
    public Color Color;
    public int Duration;
    public boolean activated;
    public FightCell cell;
    public ArrayList<Fighter> targets;
    protected Fight m_fight;
    public Fighter caster;
    protected SpellLevel m_actionEffect;
    public int m_spellId;
    public byte m_spell_level;
    public Short affectedCells[];
    public GameActionFightInvisibilityStateEnum visibileState;
    public byte size;
    public GameActionMarkCellsTypeEnum shape;
    public short ID;

    protected MapPoint CachedMapPoints;

    public abstract void appearForAll();

    public abstract void appear(FightTeam dispatcher);

    public abstract void disappearForAll();

    public FightActivableObject() {

    }

    public FightActivableObject(BuffActiveType activeType, Fight fight, Fighter caster, EffectCast castInfos, short cell, int duration, Color color, GameActionFightInvisibilityStateEnum visibleState, byte size, GameActionMarkCellsTypeEnum shap) {
        ID = (short) fight.getNextTriggerUid().incrementAndGet();
        m_fight = fight;
        this.caster = caster;
        m_spellId = castInfos.spellId;
        m_spell_level = castInfos.spellLevel.getGrade();
        try {
            m_actionEffect = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevels()[castInfos.effect.diceSide - 1];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
        }
        this.cell = fight.getCell(cell);
        activationType = activeType;
        Color = color;
        targets = new ArrayList<>();
        affectedCells = new Zone(castInfos.effect.ZoneShape(), size,fight.getMap()).getCells(this.cell.Id) /*shap == GameActionMarkCellsTypeEnum.CELLS_CROSS ? new Zone(SpellShapeEnum.Q, size) : (shap == GameActionMarkCellsTypeEnum.CELLS_CIRCLE ? new Zone(SpellShapeEnum.C, size) : new Zone(SpellShapeEnum.G, size))).getCells(cell.id)*/;
        Duration = duration;
        this.visibileState = visibleState;
        this.size = size;
        shape = shap;

        if (m_actionEffect != null) {
            for (EffectInstanceDice effect : m_actionEffect.getEffects()) {
                if (EffectCast.isDamageEffect(effect.getEffectType())) {
                    Priority--;
                }
                if (effect.getEffectType() == StatsEnum.PULL_FORWARD || effect.getEffectType() == StatsEnum.PUSH_BACK) {
                    Priority += 50;
                }
            }
        }

        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : this.affectedCells) {
            if (m_fight.getCell(cellId) != null) {
                m_fight.getCell(cellId).AddObject(this);
            }
        }

        if (visibleState == GameActionFightInvisibilityStateEnum.INVISIBLE) {
            appear(caster.getTeam());
        } else {
            appearForAll();
        }
    }

    public synchronized void loadTargets(Fighter target) {
        if (!targets.contains(target)) {
            targets.add(target);
        }

        switch (activationType) {
            case ACTIVE_ENDMOVE:
                FightCell fCell = null;
                for (short cell : affectedCells) {
                    fCell = m_fight.getCell(cell);
                    if (fCell != null) {
                        targets.addAll(fCell.GetObjectsAsFighterList(obj -> (obj.getObjectType() == FightObjectType.OBJECT_FIGHTER || obj.getObjectType() == FightObjectType.OBJECT_STATIC) && !targets.contains(obj)));
                        //targets.AddRange(cell.FightObjects.OfType < FighterBase > ().Where(fighter =  > !targets.Contains(fighter)));
                    }
                }
                break;
        }
    }

    public void enable(Fighter fighter) {

    }

    public int loadEnnemyTargetsAndActive(Fighter target) {
        FightCell Cell = null;
        for (short cell : affectedCells) {
            Cell = m_fight.getCell(cell);
            if (Cell != null) {
                targets.addAll(Cell.GetObjectsAsFighterList(obj ->  (obj.getObjectType() == FightObjectType.OBJECT_FIGHTER || obj.getObjectType() == FightObjectType.OBJECT_STATIC)  && !targets.contains(obj) && ((Fighter) obj).isEnnemyWith(target)));
            }
        }
        return this.activate(target);
    }

    public MapPoint getMapPoint() {
        if (CachedMapPoints == null) {
            this.CachedMapPoints = MapPoint.fromCellId(this.getCellId());
        }
        return this.CachedMapPoints;
    }

    public synchronized int activate(Fighter activator) {
        this.activated = true;
        if (this.getObjectType() == FightObjectType.OBJECT_TRAP) {
            remove();
        }

        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(getGameActionMarkType() == GameActionMarkTypeEnum.GLYPH ? ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH : ActionIdEnum.ACTION_FIGHT_TRIGGER_TRAP, this.caster.getID(), this.ID, activator.getID(), this.m_spellId));
        activator.getFight().startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        for (EffectInstanceDice Effect : m_actionEffect.getEffects()) {
            //TODO : MASK
            EffectCast CastInfos = new EffectCast(Effect.getEffectType(), this.m_actionEffect.getSpellId(), cell.Id, 100, Effect, caster, targets, false, StatsEnum.NONE, 0, this.m_actionEffect);
            CastInfos.isTrap = this.getObjectType() == FightObjectType.OBJECT_TRAP;
            if (EffectBase.tryApplyEffect(CastInfos) == -3) {
                targets.clear();
                activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
                return -3;
            }
        }

        targets.clear();

        activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return -1;
    }

    public void decrementDuration() {
        Duration--;

        if (Duration <= 0) {
            remove();
        }
    }

    public void remove() {
        disappearForAll();

        for (short cell : affectedCells) {
            this.m_fight.getCell(cell).RemoveObject(this);
        }
        if (this.m_fight.getActivableObjects().containsKey(this.caster)) {
            this.m_fight.getActivableObjects().get(this.caster).remove(this);
        }

    }

    public abstract GameActionMarkTypeEnum getGameActionMarkType();

    public abstract GameActionMark getHiddenGameActionMark();

    public abstract GameActionMark getGameActionMark();

    public GameActionMarkedCell[] getGameActionMarkedCell() {
        if (shape == GameActionMarkCellsTypeEnum.CELLS_SQUARE) {
            return Arrays.stream(this.affectedCells).map(x -> new GameActionMarkedCell(x, (byte) 0, getRGB(Color), this.shape.value)).toArray(GameActionMarkedCell[]::new);
        } else {
            return new GameActionMarkedCell[]{new GameActionMarkedCell(this.getCellId(), this.size, getRGB(Color), this.shape.value)};
        }
    }

    @Override
    public short getCellId() {
        return this.cell.Id;
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
    public Integer getPriority() {
        return Priority;
    }

    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

}
