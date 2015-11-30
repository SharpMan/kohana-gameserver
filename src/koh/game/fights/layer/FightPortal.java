package koh.game.fights.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.protocol.client.enums.ActionIdEnum;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionMarkCellsTypeEnum;
import koh.protocol.client.enums.GameActionMarkTypeEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightActivateGlyphTrapMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightMarkCellsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTriggerGlyphTrapMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightUnmarkCellsMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.types.game.actions.fight.GameActionMark;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class FightPortal extends FightActivableObject {

    public int damageValue;

    public boolean Enabled = true;

    public FightPortal(Fight fight, Fighter caster, EffectCast castInfos, short cell) {
        super(BuffActiveType.ACTIVE_ENDMOVE, fight, caster, castInfos, cell, 0, javafx.scene.paint.Color.LIGHTBLUE, GameActionFightInvisibilityStateEnum.VISIBLE, (byte) 0, GameActionMarkCellsTypeEnum.CELLS_CIRCLE);
        this.damageValue = castInfos.Effect.value;
        Priority += 50;
    }

    @Override
    public void AppearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.m_caster.ID, GetGameActionMark()));
        this.m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, this.m_caster.ID, this.ID, true));
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
        return GameActionMarkTypeEnum.PORTAL;
    }

    private int turnUsed;

    @Override
    public synchronized void Enable(Fighter fighter) {
        Enable(fighter, false);
    }

    public boolean DisabledByCaster = false;

    public void ForceEnable(Fighter fighter) {
        if (DisabledByCaster) {
            if (!this.Cell.HasGameObject(FightObjectType.OBJECT_FIGHTER) && !this.Cell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
                this.onEnable(fighter);
            }
            this.DisabledByCaster = false;
        }
    }

    public synchronized void Enable(Fighter fighter, boolean check) {
        if (DisabledByCaster || Enabled || (check && turnUsed == m_fight.myWorker.FightTurn) || this.Cell.HasGameObject(FightObjectType.OBJECT_FIGHTER) || this.Cell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
            return;
        }
        this.onEnable(fighter);
    }

    public void onEnable(Fighter fighter) {
        this.Enabled = true;
        m_fight.StartSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.ID, this.ID, true));
        m_fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    public void Disable(Fighter fighter) {
        if (!Enabled) {
            return;
        }
        this.Enabled = false;
        this.DisabledByCaster = true;
        m_fight.StartSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.ID, this.ID, false));
        m_fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized int Activate(Fighter activator) {
        if (Arrays.stream(new Exception().getStackTrace()).filter(Method -> Method.getMethodName().equalsIgnoreCase("FightPortal.Activate")).count() > 1) { //Il viens de rejoindre la team rofl on le tue pas
            return -1;
        }
        if (!Enabled) {
            return -1;
        }

        activator.Fight.StartSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);

        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH, this.m_caster.ID, this.ID, activator.ID, this.m_spellId));

        FightPortal[] Portails = new FightPortal[0];
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.m_fight.m_activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled && Object.ID != this.ID && Object.m_caster.Team == this.m_caster.Team) {
                    Portails = ArrayUtils.add(Portails, (FightPortal) Object);
                }
            }
        }
        this.turnUsed = m_fight.myWorker.FightTurn;
        Enabled = false;
        if (Portails.length == 0) {
            m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.ID, this.ID, false));
            activator.Fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
            return -1;
        }

        final int[] Links = LinkedCellsManager.getLinks(this.MapPoint(), Arrays.stream(Portails).map(Portail -> Portail.MapPoint()).toArray(MapPoint[]::new));
        //System.out.println(Enumerable.Join(Links));
        FightPortal lastPortal = Arrays.stream(Portails).filter(x -> x.CellId() == (short) Links[Links.length - 1]).findFirst().get();

        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.ID, ID, false));
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.ID, lastPortal.ID, false));

        //Portails = Arrays.stream(Portails).sorted((FightPortail b2, FightPortail b1) -> (int) (b2.id) - b1.id).toArray(FightPortail[]::new);
        //Arrays.stream(Portails).forEach(Portail -> m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.id, Portail.id, false)));
        m_fight.AffectSpellTo(m_caster, activator, 1, 5426);

        m_fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, activator.ID, activator.ID, lastPortal.CellId()));

        lastPortal.Enabled = false;
        lastPortal.turnUsed = m_fight.myWorker.FightTurn;

        Targets.clear();

        activator.Fight.EndSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return activator.SetCell(lastPortal.Cell);
    }

    static void shuffle(FightPortal[] array) {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            // Get a random index of the array past i.
            int random = i + (int) (Math.random() * (n - i));
            // Swap the random element with the present element.
            FightPortal randomElement = array[random];
            array[random] = array[i];
            array[i] = randomElement;
        }
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
        return FightObjectType.OBJECT_PORTAL;
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
