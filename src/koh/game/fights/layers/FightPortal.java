package koh.game.fights.layers;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.scene.paint.Color;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.effects.buff.BuffCastSpellOnPortal;
import koh.game.fights.effects.buff.BuffStatsByPortalTeleport;
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

    public boolean enabled = true;

    public FightPortal(Fight fight, Fighter caster, EffectCast castInfos, short cell) {
        super(BuffActiveType.ACTIVE_ENDMOVE, fight, caster, castInfos, cell, 0, caster.getTeam().id == 0 ? javafx.scene.paint.Color.LIGHTBLUE : Color.ROSYBROWN, GameActionFightInvisibilityStateEnum.VISIBLE, (byte) 0, GameActionMarkCellsTypeEnum.CELLS_CIRCLE);
        this.damageValue = castInfos.effect.value;
        priority += 50;
    }

    @Override
    public void appearForAll() {
        this.m_fight.sendToField(new GameActionFightMarkCellsMessage(ActionIdEnum.ACTION_FIGHT_ADD_GLYPH_CASTING_SPELL, this.caster.getID(), getGameActionMark()));
        this.m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, this.caster.getID(), this.ID, true));
    }

    @Override
    public void appear(FightTeam dispatcher) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disappearForAll() {
        this.m_fight.sendToField(new GameActionFightUnmarkCellsMessage((short) 310, this.caster.getID(), this.ID));
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

    public boolean disabledByCaster = false;

    public void forceEnable(Fighter fighter) {
        if (disabledByCaster) {
            if (!this.cell.hasGameObject(FightObjectType.OBJECT_FIGHTER) && !this.cell.hasGameObject(FightObjectType.OBJECT_STATIC)) {
                this.onEnable(fighter);
            }
            this.disabledByCaster = false;
        }
    }

    public synchronized void enable(Fighter fighter, boolean check) {
        if (disabledByCaster || enabled || (check && turnUsed == m_fight.getFightWorker().fightTurn) || this.cell.hasGameObject(FightObjectType.OBJECT_FIGHTER) || this.cell.hasGameObject(FightObjectType.OBJECT_STATIC)) {
            return;
        }
        this.onEnable(fighter);
    }

    public void onEnable(Fighter fighter) {
        this.enabled = true;
        m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.getID(), this.ID, true));
        m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    public void disable(Fighter fighter) {
        /*if (!enabled) {
            return;
        }*/
        if(disabledByCaster){
            return;
        }
        this.enabled = false;
        this.disabledByCaster = true;
        m_fight.startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, fighter.getID(), this.ID, false));
        m_fight.endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
    }

    @Override
    public synchronized int activate(Fighter activator,BuffActiveType buffActiveType) {
        if (Arrays.stream(new Exception().getStackTrace()).filter(Method -> Method.getMethodName().equalsIgnoreCase("FightPortal.activate")).count() > 1) { //Il viens de rejoindre la team rofl on le tue pas
            return -1;
        }
        if (!enabled) {
            return -1;
        }

        activator.getFight().startSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);

        m_fight.sendToField(new GameActionFightTriggerGlyphTrapMessage(ActionIdEnum.ACTION_FIGHT_TRIGGER_GLYPH, this.caster.getID(), this.ID, activator.getID(), this.m_spellId));

        FightPortal[] portals = new FightPortal[0];
        for (CopyOnWriteArrayList<FightActivableObject> Objects : this.m_fight.getActivableObjects().values()) {
            for (FightActivableObject obj : Objects) {
                if (obj instanceof FightPortal && ((FightPortal) obj).enabled && obj.ID != this.ID && obj.caster.getTeam() == this.caster.getTeam()) {
                    portals = ArrayUtils.add(portals, (FightPortal) obj);
                }
            }
        }
        this.turnUsed = m_fight.getFightWorker().fightTurn;
        this.enabled = false;
        if (portals.length == 0) {
            m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), this.ID, false));
            activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
            return -1;
        }

        final int[] Links = LinkedCellsManager.getLinks(this.getMapPoint(), Arrays.stream(portals).map(Portail -> Portail.getMapPoint()).toArray(MapPoint[]::new));
        //System.out.println(Enumerable.join(Links));
        final FightPortal lastPortal = Arrays.stream(portals).filter(x -> x.getCellId() == (short) Links[Links.length - 1]).findFirst().get();

        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), ID, false));
        m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.getID(), lastPortal.ID, false));



        this.caster.getBuff().getAllBuffs()
                .filter(buff -> buff instanceof BuffCastSpellOnPortal)
                .filter(buff -> buff.getCastInfos().effect.isValidTarget(caster, activator) && (EffectInstanceDice.verifySpellEffectMask(caster, activator, buff.getCastInfos().effect,activator.getCellId())))
                .forEach(buff -> ((BuffCastSpellOnPortal) buff).applyEffect(activator));

        //Portails = Arrays.stream(Portails).sorted((FightPortail b2, FightPortail b1) -> (int) (b2.id) - b1.id).toArray(FightPortail[]::new);
        //Arrays.stream(Portails).forEach(PORTAL -> m_fight.sendToField(new GameActionFightActivateGlyphTrapMessage(1181, activator.id, PORTAL.id, false)));
        m_fight.affectSpellTo(caster, activator, 1, 5426);

        m_fight.sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, activator.getID(), activator.getID(), lastPortal.getCellId()));

        lastPortal.enabled = false;
        lastPortal.turnUsed = m_fight.getFightWorker().fightTurn;

        targets.clear();

        activator.getBuff().getAllBuffs()
                .filter(buff -> buff instanceof BuffStatsByPortalTeleport)
                .forEach(bf-> bf.applyEffect(null,null));

        activator.getFight().endSequence(SequenceTypeEnum.SEQUENCE_GLYPH_TRAP);
        return activator.setCell(lastPortal.cell);
    }

    static void shuffle(FightPortal[] array) {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            // get a random index of the array past i.
            final int random = i + (int) (Math.random() * (n - i));
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
        return new GameActionMark(this.caster.getID(), this.caster.getTeam().id, this.m_spellId, this.m_spell_level, this.ID, getGameActionMarkType().value(), this.getCellId(), this.getGameActionMarkedCell(), true);
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
