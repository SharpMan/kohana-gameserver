package koh.game.fights.layer;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.Fight;
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
import koh.protocol.types.game.actions.fight.GameActionMark;
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
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.m_caster.getID(), getGameActionMark()));
        this.m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, this.m_caster.getID(), this.ID, true));
    }

    @Override
    public void Appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void DisappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.m_caster.getID(), this.ID));
    }

    @Override
    public GameActionMarkTypeEnum getGameActionMarkType() {
        return GameActionMarkTypeEnum.PORTAL;
    }

    private int turnUsed;

    @Override
    public synchronized void enable(Fighter fighter) {
        enable(fighter, false);
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

    public synchronized void enable(Fighter fighter, boolean check) {
        if (DisabledByCaster || Enabled || (check && turnUsed == m_fight.myWorker.fightTurn) || this.Cell.HasGameObject(FightObjectType.OBJECT_FIGHTER) || this.Cell.HasGameObject(FightObjectType.OBJECT_STATIC)) {
            return;
        }
        this.onEnable(fighter);
    }

    public void onEnable(Fighter fighter) {
        this.Enabled = true;
        m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.getID(), this.ID, true));
        m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    public void disable(Fighter fighter) {
        if (!Enabled) {
            return;
        }
        this.Enabled = false;
        this.DisabledByCaster = true;
        m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.getID(), this.ID, false));
        m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized int activate(Fighter activator) {
        if (Arrays.stream(new Exception().getStackTrace()).filter(Method -> Method.getMethodName().equalsIgnoreCase("FightPortal.activate")).count() > 1) { //Il viens de rejoindre la team rofl on le tue pas
            return -1;
        }
        if (!Enabled) {
            return -1;
        }

        activator.fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);

        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH, this.m_caster.getID(), this.ID, activator.getID(), this.m_spellId));

        FightPortal[] Portails = new FightPortal[0];
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.m_fight.m_activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled && Object.ID != this.ID && Object.m_caster.team == this.m_caster.team) {
                    Portails = ArrayUtils.add(Portails, (FightPortal) Object);
                }
            }
        }
        this.turnUsed = m_fight.myWorker.fightTurn;
        Enabled = false;
        if (Portails.length == 0) {
            m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), this.ID, false));
            activator.fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
            return -1;
        }

        final int[] Links = LinkedCellsManager.getLinks(this.getMapPoint(), Arrays.stream(Portails).map(Portail -> Portail.getMapPoint()).toArray(MapPoint[]::new));
        //System.out.println(Enumerable.Join(Links));
        FightPortal lastPortal = Arrays.stream(Portails).filter(x -> x.getCellId() == (short) Links[Links.length - 1]).findFirst().get();

        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), ID, false));
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), lastPortal.ID, false));

        //Portails = Arrays.stream(Portails).sorted((FightPortail b2, FightPortail b1) -> (int) (b2.id) - b1.id).toArray(FightPortail[]::new);
        //Arrays.stream(Portails).forEach(Portail -> m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.id, Portail.id, false)));
        m_fight.affectSpellTo(m_caster, activator, 1, 5426);

        m_fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, activator.getID(), activator.getID(), lastPortal.getCellId()));

        lastPortal.Enabled = false;
        lastPortal.turnUsed = m_fight.myWorker.fightTurn;

        targets.clear();

        activator.fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return activator.setCell(lastPortal.Cell);
    }

    static void shuffle(FightPortal[] array) {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            // get a random index of the array past i.
            int random = i + (int) (Math.random() * (n - i));
            // Swap the random element with the present element.
            FightPortal randomElement = array[random];
            array[random] = array[i];
            array[i] = randomElement;
        }
    }

    @Override
    public GameActionMark getHiddenGameActionMark() {
        return getGameActionMark();
    }

    @Override
    public GameActionMark getGameActionMark() {
        return new GameActionMark(this.m_caster.getID(), this.m_caster.team.Id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.getCellId(), this.getGameActionMarkedCell(), true);
    }

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_PORTAL;
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
