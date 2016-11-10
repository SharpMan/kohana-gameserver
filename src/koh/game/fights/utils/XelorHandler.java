package koh.game.fights.utils;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightState;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.layers.FightPortal;
import koh.game.utils.Three;
import koh.protocol.client.enums.*;
import koh.protocol.messages.game.actions.fight.GameActionFightPointsVariationMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightSpellCastMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static koh.protocol.client.enums.StatsEnum.CAST_SPELL_ON_CRITICAL_HIT;

/**
 * Created by Melancholia on 10/29/16.
 */
public class XelorHandler {

    private final static EffectInstanceDice SYMMETRIC_EFFECT = Arrays.stream(DAO.getSpells().findLevel(451).getEffects())
            .filter(e -> e.effectId == 1105)
            .findFirst()
            .get();
    private final static EffectInstanceDice GELURE_EFFECT = Arrays.stream(DAO.getSpells().findLevel(416).getEffects())
            .filter(e -> e.effectId == 1100)
            .findFirst()
            .get();
    private final static EffectInstanceDice DUST_EFFECT = Arrays.stream(DAO.getSpells().findLevel(476).getEffects())
            .filter(e -> e.effectId == 1106)
            .findFirst()
            .get();


    //@Spell 96 Poussiere
    public static void temporalDust(Fighter fighter, SpellLevel spellLevel, Short cellId, boolean friend) {
        if (fighter.getMutex() == null) {
            return;
        }
        synchronized (fighter.getMutex()) {
            try {
                final short oldCell = cellId;
                final short castingCell = fighter.getCellId();
                final MutableObject<Three<Integer, int[], Integer>> portalQuery = new MutableObject<>();
                if (!preCast(fighter, spellLevel, oldCell, cellId, fighter.getFight(), friend, portalQuery)) {
                    return;
                }
                final MutableObject<EffectInstanceDice[]> spellEffects = new MutableObject<>();

                final boolean isCritical = isCritical(fighter, spellLevel, cellId, portalQuery.getValue().tree, spellEffects, portalQuery);

                //PART 2

                final ArrayList<Fighter> targets = Arrays.stream((new Zone(DUST[0].getZoneShape(), DUST[0].zoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(portalQuery.getValue().first != -1 ? oldCell : cellId), true), fighter.getFight().getMap()))
                        .getCells(cellId))
                        .map(cell -> fighter.getFight().getCell(cell))
                        .filter(cell -> cell != null && cell.hasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER, IFightObject.FightObjectType.OBJECT_STATIC))
                        .map(fightCell -> fightCell.getObjectsAsFighter()[0])
                        .filter(f -> f != null && !f.isDead())
                        .collect(Collectors.toCollection(ArrayList::new));


                while (true) {
                    EffectCast castInfos;
                    castInfos = new EffectCast(DUST_EFFECT.getEffectType(), spellLevel.getSpellId(), cellId, 0, DUST_EFFECT, fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                    castInfos.targetKnownCellId = cellId;
                    castInfos.oldCell = oldCell;
                    castInfos.casterOldCell = castingCell;
                    castInfos.setCritical(isCritical);
                    if (EffectBase.tryApplyEffect(castInfos) == -3) {
                        break;
                    }
                    targets.removeIf(f -> !f.isEnnemyWith(fighter));
                    castInfos = new EffectCast(DUST[spellLevel.getGrade()].getEffectType(), spellLevel.getSpellId(), cellId, 0, DUST[spellLevel.getGrade() + (isCritical ? 5 : -1)], fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                    castInfos.targetKnownCellId = cellId;
                    castInfos.oldCell = oldCell;
                    castInfos.casterOldCell = castingCell;
                    castInfos.setCritical(isCritical);
                    if (EffectBase.tryApplyEffect(castInfos) == -3) {
                        break;
                    }
                    break;
                }

                postCast(fighter, fighter.getFight(), spellLevel, portalQuery);
                targets.clear();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //@Spell 84 Gelure
    public static void frostBite(Fighter fighter, SpellLevel spellLevel, Short cellId, boolean friend) {
        if (fighter.getMutex() == null) {
            return;
        }
        synchronized (fighter.getMutex()) {
            try {
                final short oldCell = cellId;
                final short castingCell = fighter.getCellId();
                final MutableObject<Three<Integer, int[], Integer>> portalQuery = new MutableObject<>();
                if (!preCast(fighter, spellLevel, oldCell, cellId, fighter.getFight(), friend, portalQuery)) {
                    return;
                }
                final MutableObject<EffectInstanceDice[]> spellEffects = new MutableObject<>();

                final boolean isCritical = isCritical(fighter, spellLevel, cellId, portalQuery.getValue().tree, spellEffects, portalQuery);

                //PART 2

                final ArrayList<Fighter> targets = new ArrayList<>(1);
                targets.add(fighter.getFight().getCell(cellId).getFighter());
                if (targets.get(0) == null || targets.get(0).isDead()) {
                    return;
                }


                if (targets.get(0).getStates().hasState(FightStateEnum.TÉLÉFRAG) || targets.get(0).getStates().hasState(FightStateEnum.TELEFRAG)) {
                    while (true) { //GOTO alternative
                        EffectCast castInfos;
                        if (fighter.isEnnemyWith(targets.get(0))) {
                            castInfos = new EffectCast(GELURE[(spellLevel.getGrade())].getEffectType(), spellLevel.getSpellId(), cellId, 0, GELURE[spellLevel.getGrade() + (isCritical ? 5 : -1)], fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                            castInfos.targetKnownCellId = cellId;
                            castInfos.oldCell = oldCell;
                            castInfos.casterOldCell = castingCell;
                            castInfos.setCritical(isCritical);
                            if (EffectBase.tryApplyEffect(castInfos) == -3) {
                                break;
                            }
                        }

                        castInfos = new EffectCast(GELURE_EFFECT.getEffectType(), spellLevel.getSpellId(), cellId, 0, GELURE_EFFECT, fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        break;
                    }
                } else {
                    while (true) { //GOTO alternative
                        EffectCast castInfos;
                        if (fighter.isEnnemyWith(targets.get(0))) {
                            castInfos = new EffectCast(GELURE[(spellLevel.getGrade())].getEffectType(), spellLevel.getSpellId(), cellId, 0, GELURE[spellLevel.getGrade() + (isCritical ? 5 : -1)], fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                            castInfos.targetKnownCellId = cellId;
                            castInfos.oldCell = oldCell;
                            castInfos.casterOldCell = castingCell;
                            castInfos.setCritical(isCritical);
                            if (EffectBase.tryApplyEffect(castInfos) == -3) {
                                break;
                            }
                        }

                        castInfos = new EffectCast(GELURE_EFFECT.getEffectType(), spellLevel.getSpellId(), cellId, 0, GELURE_EFFECT, fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        castInfos = new EffectCast(StatsEnum.SPELL_COOLDOWN, spellLevel.getSpellId(), cellId, 0, COOLDOWN, fighter, new ArrayList<Fighter>(1) {{
                            this.add(fighter);
                        }}, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }




                        break;
                    }

                }

                postCast(fighter, fighter.getFight(), spellLevel, portalQuery);
                targets.clear();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //@Spell 91 Frappe du xelôr
    public static void xelorStrike(Fighter fighter, SpellLevel spellLevel, Short cellId, boolean friend) {
        if (fighter.getMutex() == null) {
            return;
        }
        synchronized (fighter.getMutex()) {
            try {
                final short oldCell = cellId;
                final short castingCell = fighter.getCellId();
                final MutableObject<Three<Integer, int[], Integer>> portalQuery = new MutableObject<>();
                if (!preCast(fighter, spellLevel, oldCell, cellId, fighter.getFight(), friend, portalQuery)) {
                    return;
                }
                final MutableObject<EffectInstanceDice[]> spellEffects = new MutableObject<>();

                final boolean isCritical = isCritical(fighter, spellLevel, cellId, portalQuery.getValue().tree, spellEffects, portalQuery);

                //PART 2

                final ArrayList<Fighter> targets = new ArrayList<>(1);
                targets.add(fighter.getFight().getCell(cellId).getFighter());
                if (targets.get(0) == null || targets.get(0).isDead()) {
                    return;
                }


                if (targets.get(0).getStates().hasState(FightStateEnum.TÉLÉFRAG) || targets.get(0).getStates().hasState(FightStateEnum.TELEFRAG)) {
                    while (true) { //GOTO alternative
                        EffectCast castInfos = new EffectCast(SYMMETRIC_EFFECT.getEffectType(), spellLevel.getSpellId(), cellId, 0, SYMMETRIC_EFFECT, fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        castInfos = new EffectCast(StatsEnum.ACTION_POINTS, spellLevel.getSpellId(), cellId, 0, BOOST2, fighter, new ArrayList<Fighter>(1) {{
                            this.add(fighter);
                        }}, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        break;
                    }
                } else {
                    while (true) { //GOTO alternative
                        EffectCast castInfos;
                        if (fighter.isEnnemyWith(targets.get(0))) {
                            castInfos = new EffectCast(STRIKES[spellLevel.getGrade()].getEffectType(), spellLevel.getSpellId(), cellId, 0, STRIKES[spellLevel.getGrade() + (isCritical ? 5 : -1)], fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                            castInfos.targetKnownCellId = cellId;
                            castInfos.oldCell = oldCell;
                            castInfos.casterOldCell = castingCell;
                            castInfos.setCritical(isCritical);
                            if (EffectBase.tryApplyEffect(castInfos) == -3) {
                                break;
                            }
                        }

                        castInfos = new EffectCast(SYMMETRIC_EFFECT.getEffectType(), spellLevel.getSpellId(), cellId, 0, SYMMETRIC_EFFECT, fighter, targets, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        castInfos = new EffectCast(StatsEnum.SPELL_COOLDOWN, spellLevel.getSpellId(), cellId, 0, COOLDOWN, fighter, new ArrayList<Fighter>(1) {{
                            this.add(fighter);
                        }}, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = cellId;
                        castInfos.oldCell = oldCell;
                        castInfos.casterOldCell = castingCell;
                        castInfos.setCritical(isCritical);
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }

                        


                        break;
                    }

                }

                postCast(fighter, fighter.getFight(), spellLevel, portalQuery);
                targets.clear();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void boostSynchro(Fighter fighter, SpellLevel spellLevel) {
        fighter.getTeam().getAliveFighters()
                .filter(f -> f instanceof SummonedFighter && f.getSummonerID() == fighter.getID() && ((SummonedFighter) f).getGrade().getMonsterId() == 3958)
                .forEach(target -> {
                    final ArrayList<Fighter> synchro = new ArrayList<Fighter>(1);
                    synchro.add(target);
                    for (EffectInstanceDice effect : SYNCHRO) {
                        if(!target.getSpellsController().canTurnSpellID(5492,1)){
                            continue;
                        }
                        final EffectCast castInfos = new EffectCast(effect.getEffectType(), spellLevel.getSpellId(), target.getCellId(), 0, effect, fighter, synchro, false, StatsEnum.NONE, 0, spellLevel);
                        castInfos.targetKnownCellId = target.getCellId();
                        if (EffectBase.tryApplyEffect(castInfos) == -3) {
                            break;
                        }
                        target.getSpellsController().actualize(5492,target.getID());
                    }
                });
    }


    private static boolean preCast(Fighter fighter, SpellLevel spellLevel, final short oldCell, Short cellId, final Fight fight, boolean friend, final MutableObject<Three<Integer, int[], Integer>> portalQuery) {
        if (fight.getFightState() != FightState.STATE_ACTIVE || fight.getFightLoopState() == Fight.FightLoopState.STATE_WAIT_END) {
            return false;
        }
        fight.getChallenges()
                .values()
                .stream()
                .filter(cl -> cl.canBeAnalyzed())
                .forEach(ch -> ch.onFighterCastSpell(fighter, spellLevel));


        if (fight.getCell(cellId).hasGameObject(IFightObject.FightObjectType.OBJECT_PORTAL)) {
            portalQuery.setValue(fight.getTargetThroughPortal(fighter,
                    cellId,
                    true,
                    fight.getCell(cellId).getObjects().stream()
                            .filter(x -> x.getObjectType() == IFightObject.FightObjectType.OBJECT_PORTAL)
                            .findFirst()
                            .map(f -> (FightPortal) f)
                            .get().caster.getTeam()
            ));
            cellId = portalQuery.getValue().first.shortValue();
        } else {
            portalQuery.setValue(new Three<>(-1, EMPTY_INTS, 0));
        }

        Fighter targetE = fight.hasEnnemyInCell(cellId, fighter.getTeam());
        if (friend && targetE == null) {
            targetE = fight.hasFriendInCell(cellId, fighter.getTeam());
        }

        final int targetId = targetE == null ? -1 : targetE.getID();

        if (!fight.canLaunchSpell(fighter, spellLevel, fighter.getCellId(), oldCell, targetId)) {
            fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 175));
            fight.startSequence(SequenceTypeEnum.SEQUENCE_SPELL);
            fighter.send(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, fighter.getID(), fighter.getID(), (short) 0));
            fight.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
            return false;
        }

        portalQuery.getValue().tree = targetId;
        fight.startSequence(SequenceTypeEnum.SEQUENCE_SPELL);
        fighter.setUsedAP(fighter.getUsedAP() + spellLevel.getApCost());

        fighter.getSpellsController().actualize(spellLevel, targetId);

        return true;
    }

    private static void postCast(final Fighter fighter, final Fight fight, SpellLevel spellLevel, final MutableObject<Three<Integer, int[], Integer>> portalQuery) {

        fight.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_USE, fighter.getID(), fighter.getID(), (short) -spellLevel.getApCost()));


        if (fighter.getVisibleState() == GameActionFightInvisibilityStateEnum.INVISIBLE) {
            fight.sendToField(new ShowCellMessage(fighter.getID(), fighter.getCellId()));
        }
        fight.endSequence(SequenceTypeEnum.SEQUENCE_SPELL, false);
        portalQuery.getValue().clear();
    }

    public static boolean isCritical(Fighter fighter, SpellLevel spellLevel, Short cellId, int targetId, final MutableObject<EffectInstanceDice[]> spellEffects, final MutableObject<Three<Integer, int[], Integer>> portalQuery) {
        boolean isCc = false;
        if (spellLevel.getCriticalHitProbability() != 0 && spellLevel.getCriticalEffect().length > 0) {
            final int tauxCC = fighter.getStats().getTotal(StatsEnum.ADD_CRITICAL_HIT) + spellLevel.getCriticalHitProbability();

            if (tauxCC > (Fight.RANDOM.nextDouble() * 100)) {
                isCc = true;
            }
        }

        isCc &= !fighter.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects);
        if (isCc && fighter.getStats().getTotal(CAST_SPELL_ON_CRITICAL_HIT) > 0 && fighter.isPlayer()) { //Turquoise
            fighter.getPlayer().getInventoryCache().getEffects(CAST_SPELL_ON_CRITICAL_HIT.value())
                    //filter turquoise
                    .forEach(list -> {
                        list.filter(e -> e.diceNum == 5952)
                                .forEach(effect -> {
                                    fighter.getFight().launchSpell(fighter, DAO.getSpells().findSpell(effect.diceNum).getSpellLevel(effect.diceSide), fighter.getCellId(), true, true, true, -1);
                                });
                    });
        }

        spellEffects.setValue(isCc || spellLevel.getEffects() == null ? spellLevel.getCriticalEffect() : spellLevel.getEffects());

        final boolean silentCast = Arrays.stream(spellEffects.getValue()).allMatch(x -> !ArrayUtils.contains(Fight.EFFECT_NOT_SILENCED, x.getEffectType()));


        for (Player player : fighter.getFight().observable$Stream()) {
            player.send(new GameActionFightSpellCastMessage(ActionIdEnum.ACTION_FIGHT_CAST_SPELL,
                    fighter.getID(),
                    targetId,
                    ((!fighter.isVisibleFor(player) && silentCast) || (Arrays.stream(spellEffects.getValue()).anyMatch(e -> e.getEffectType() == StatsEnum.LAYING_TRAP_LEVEL))) ? 0 : cellId,
                    (byte) (isCc ? 2 : 1),
                    (!fighter.isVisibleFor(player) && silentCast), spellLevel.getSpellId() == 0 ? 2 : spellLevel.getSpellId(),
                    spellLevel.getGrade(),
                    portalQuery.getValue().second)
            );
        }

        return isCc;
    }

    private static final int[] EMPTY_INTS = new int[0];


    private static final EffectInstanceDice COOLDOWN = Arrays.stream(DAO.getSpells().findLevel(24073).getEffects())
            .filter(e -> e.effectId == 1045)
            .findFirst()
            .get();
    private static final EffectInstanceDice[] SYNCHRO = Arrays.stream(DAO.getSpells().findLevel(24863).getEffects()).filter(e -> e.targetMask.contains("a,F3958")).toArray(EffectInstanceDice[]::new);
    private static final EffectInstanceDice BOOST2 = Arrays.stream(DAO.getSpells().findLevel(24318).getEffects()).filter(e -> e.effectId == 111).findFirst().get();


    private static final EffectInstanceDice[] DUST = new EffectInstanceDice[]{
            Arrays.stream(DAO.getSpells().findLevel(476).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(477).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(478).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(479).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(480).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(3037).getEffects()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(476).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(477).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(478).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(479).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(480).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(3037).getCriticalEffect()).filter(e -> e.effectId == 99).findFirst().get()
    };
    private static final EffectInstanceDice[] GELURE = new EffectInstanceDice[]{
            Arrays.stream(DAO.getSpells().findLevel(416).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(417).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(418).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(419).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(420).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(3032).getEffects()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(416).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(417).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(418).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(419).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(420).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(3032).getCriticalEffect()).filter(e -> e.effectId == 98).findFirst().get()
    };
    private static final EffectInstanceDice[] STRIKES = new EffectInstanceDice[]{
            Arrays.stream(DAO.getSpells().findLevel(451).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(452).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(453).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(454).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(455).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(10475).getEffects()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(451).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(452).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(453).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(454).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(455).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get(),
            Arrays.stream(DAO.getSpells().findLevel(10475).getCriticalEffect()).filter(e -> e.effectId == 97).findFirst().get()
    };


}
