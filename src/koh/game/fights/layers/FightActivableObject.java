package koh.game.fights.layers;

import javafx.scene.paint.Color;
import koh.game.dao.DAO;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.*;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.fighters.SummonedReplacerFighter;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.actions.fight.GameActionFightTriggerGlyphTrapMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.protocol.types.game.actions.fight.GameActionMarkedCell;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Neo-Craft
 */
@Log4j2
public abstract class FightActivableObject implements IFightObject {

    public int priority;
    public BuffActiveType activationType;
    public Color color;
    public int duration;
    public boolean activated;
    public FightCell cell;
    public ArrayList<Fighter> targets;
    public Fighter caster;
    public int m_spellId;
    public byte m_spell_level;
    public Short affectedCells[];
    public GameActionFightInvisibilityStateEnum visibileState;
    public byte size;
    public GameActionMarkCellsTypeEnum shape;
    public short ID;
    protected Fight m_fight;
    @Getter
    protected SpellLevel castSpell;
    protected MapPoint cachedMapPoints;

    public FightActivableObject() {

    }

    public FightActivableObject(BuffActiveType activeType, Fight fight, Fighter caster, EffectCast castInfos, short cell, int duration, Color color, GameActionFightInvisibilityStateEnum visibleState, byte size, GameActionMarkCellsTypeEnum shap) {
        ID = (short) fight.getNextTriggerUid().incrementAndGet();
        m_fight = fight;
        this.caster = caster;
        m_spellId = castInfos.spellId;
        m_spell_level = castInfos.spellLevel.getGrade();
        try {
            castSpell = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevels()[castInfos.effect.diceSide - 1];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
        }
        this.cell = fight.getCell(cell);
        activationType = activeType;
        this.color = color;
        targets = new ArrayList<>();
        affectedCells = new Zone(castInfos.effect.getZoneShape(), size, (byte) 0, fight.getMap(), castInfos.effect.zoneMinSize()).getCells(this.cell.Id);
        this.duration = duration;
        this.visibileState = visibleState;
        this.size = size;
        shape = shap;

        if (castSpell != null) {
            for (EffectInstanceDice effect : castSpell.getEffects()) {
                if (EffectCast.isDamageEffect(effect.getEffectType())) {
                    priority--;
                }
                if (effect.getEffectType() == StatsEnum.PULL_FORWARD || effect.getEffectType() == StatsEnum.PUSH_BACK) {
                    priority += 50;
                }
            }
        }

        // On ajout l'objet a toutes les cells qu'il affecte
        for (short cellId : this.affectedCells) {
            if (m_fight.getCell(cellId) != null) {
                m_fight.getCell(cellId).addObject(this);
            }
        }

        if (visibleState == GameActionFightInvisibilityStateEnum.INVISIBLE) {
            appear(caster.getTeam());
        } else {
            appearForAll();
        }
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

    public abstract void appearForAll();

    public abstract void appear(FightTeam dispatcher);

    public abstract void disappearForAll();

    public synchronized void loadTargets(Fighter target) {
        if (!targets.contains(target)) {
            targets.add(target);
        }

        switch (activationType) {
            case ACTIVE_ENDMOVE:
                FightCell fCell;
                for (short cell : affectedCells) {
                    fCell = m_fight.getCell(cell);
                    if (fCell != null) {
                        targets.addAll(fCell.getObjectsAsFighterList(obj -> (obj instanceof Fighter) && !targets.contains(obj)));
                        //targets.AddRange(cell.FightObjects.OfType < FighterBase > ().Where(fighter =  > !targets.Contains(fighter)));
                    }
                }
                break;
        }
    }

    public void enable(Fighter fighter) {

    }

    public int loadEnnemyTargetsAndActive(Fighter target,BuffActiveType activationType) {
        FightCell myCell;
        for (short cell : affectedCells) {
            myCell = m_fight.getCell(cell);
            if (myCell != null) {
                targets.addAll(myCell.getObjectsAsFighterList(obj -> (obj instanceof Fighter) && !targets.contains(obj) /*&& ((Fighter) obj).isEnnemyWith(target)*/));
            }
        }
        return this.activate(target,activationType);
    }

    public MapPoint getMapPoint() {
        if (cachedMapPoints == null) {
            this.cachedMapPoints = MapPoint.fromCellId(this.getCellId());
        }
        return this.cachedMapPoints;
    }

    public synchronized int activate(Fighter activator, BuffActiveType activationType) {
        this.activated = true;
        if (this.getObjectType() == FightObjectType.OBJECT_TRAP) {
            remove();
        }
        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(getGameActionMarkType() == GameActionMarkTypeEnum.GLYPH ? ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH : ActionIdEnum.ACTION_FIGHT_TRIGGER_TRAP, this.caster.getID(), this.ID, activator.getID(), this.m_spellId));
        activator.getFight().startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        final ArrayList<Fighter> targetsPerEffect = new ArrayList<>();
        int bestResult = -1;
        for (EffectInstanceDice effect : castSpell.getEffects()) {
            System.out.println(castSpell.getSpellId() + " "+effect);
            targetsPerEffect.addAll(targets);
            targetsPerEffect.removeIf(f -> f instanceof SummonedReplacerFighter && ((SummonedReplacerFighter)f).isDying());
            targetsPerEffect.removeIf(target -> !(EffectHelper.verifyEffectTrigger(caster, target, castSpell.getEffects(), effect, false, effect.triggers, target.getCellId())
                    && effect.isValidTarget(caster, target)
                    && EffectInstanceDice.verifySpellEffectMask(caster, target, effect,target.getCellId())));

            final EffectCast castInfos = new EffectCast(effect.getEffectType(), this.castSpell.getSpellId(), cell.Id, 100, effect, caster, targetsPerEffect, false, StatsEnum.NONE, 0, this.castSpell);
            castInfos.isTrap = this.getObjectType() == FightObjectType.OBJECT_TRAP;
            castInfos.isGlyph = this.getObjectType() == FightObjectType.OBJECT_GLYPHE;
            final int result = EffectBase.tryApplyEffect(castInfos);
            if(result < bestResult){
                bestResult = result;
            }
            if (result == -3) {
                targetsPerEffect.clear();
                targets.clear();
                activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
                return -3;
            }
            targetsPerEffect.clear();

        }
        targets.clear();

        activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return bestResult;
    }

    public void decrementDuration() {
        duration--;

        if (duration <= 0) {
            remove();
        }
    }

    public void remove() {
        disappearForAll();

        for (short cell : affectedCells) {
            this.m_fight.getCell(cell).removeObject(this);
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
            return Arrays.stream(this.affectedCells).map(x -> new GameActionMarkedCell(x, (byte) 0, getRGB(color), this.shape.value)).toArray(GameActionMarkedCell[]::new);
        } else {
            return new GameActionMarkedCell[]{new GameActionMarkedCell(this.getCellId(), this.size, getRGB(color), this.shape.value)};
        }
    }

    public FightGlyph getAsGlyph(){
        return (FightGlyph) this;
    }

    @Override
    public short getCellId() {
        return this.cell.Id;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

}
