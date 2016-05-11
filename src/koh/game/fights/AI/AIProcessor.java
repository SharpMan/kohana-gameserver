package koh.game.fights.AI;

import koh.concurrency.ImprovedCachedThreadPool;
import koh.game.actions.GameMapMovement;
import koh.game.dao.DAO;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.mob.IAMind;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.exceptions.FightException;
import koh.game.fights.exceptions.FighterException;
import koh.game.fights.exceptions.StopAIException;
import koh.game.fights.fighters.DoubleFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.VirtualFighter;
import koh.game.fights.utils.Path;
import koh.protocol.client.enums.IAMindEnum;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Melancholia on 1/11/16.
 */
public class AIProcessor {

    private static final ImprovedCachedThreadPool BACKGROUND_WORKER = new ImprovedCachedThreadPool("AIServicesEventsExecutor", 20, 50, 10, 300);
    private static final Logger logger = LogManager.getLogger(AIProcessor.class);
    private static final int[] BLACKLISTED_EFFECTS = DAO.getSettings().getIntArray("Effect.BlacklistedByTriggers");
    protected List<SpellLevel> mySpells;
    protected long whenIStartIA;
    @Getter
    private Fight fight;
    @Getter
    private VirtualFighter fighter;
    @Getter
    private AINeuron neuron;
    private AIAction.AIActionEnum mode;
    private int type;
    @Getter
    private int usedNeurons = 0;

    public AIProcessor(Fight fight, VirtualFighter fighter) {
        this.type = IAMindOf(fighter);
        logger.debug("AI {} instanced",this.type);
        this.fight = fight;
        this.fighter = fighter;
        this.mySpells = fighter.getSpells();
        this.mode = AIAction.AIActionEnum.SELF_ACTING;
    }

    public static int IAMindOf(Fighter fr) {
        if (fr == null) {
            return IAMindEnum.PASSIVE;
        } else if (fr instanceof MonsterFighter) {
            return ((MonsterFighter) fr).getGrade().getMonster().getMonsterAI();
        }
        else if (fr instanceof DoubleFighter)
        {
            return IAMindEnum.BLOCKER;
        }/*
        else if (f instanceof PercepteurFighter)
        {
            return IAMindEnum.TAXCOLLECTOR;
        }*/
        else {
            return IAMindEnum.PASSIVE;
        }
    }

    public static IAMind getMindOf(int type) {
        IAMind mind = DAO.getAI_Minds().find(type);
        if (mind == null) {
            logger.error("JSIAMind[{}] doesn't exist", type);
            mind = DAO.getAI_Minds().find(1);
        }
        return mind;
    }

    public void runAI() {

        whenIStartIA = System.currentTimeMillis();
        usedNeurons = 0;
        IAMind mind = getMindOf(type);
        try {
            do {
                if (!canPlay()) {
                    break;
                }

                neuron = new AINeuron();
                logger.debug("IA runned");

                usedNeurons++;
                try {
                    mind.play(this);
                } catch (FightException Fe) {
                    throw Fe;
                } catch (FighterException fe) {
                    throw fe;
                } catch (Exception e) {
                    logger.error("bugged grade {}",this.fighter.asMonster().getGrade().getMonsterId());
                    e.printStackTrace();
                    logger.error(e);
                } finally {
                    if (neuron != null) {
                        neuron.finalize();
                        neuron = null;
                    }
                }
                BACKGROUND_WORKER.execute(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } while (canPlay());
        } catch (FightException Fe) {
            Fe.finalAction();
        } catch (FighterException fe) {
            fe.finalAction();
        } finally {
            whenIStartIA = 0;
            usedNeurons = 0;
            mind = null;
        }
    }

    public long getRemainingTime() {
        return (whenIStartIA + fight.getTurnTime()) - System.currentTimeMillis();
    }

    public void stop() throws StopAIException {
        throw new StopAIException("Arrêt de l'IA demandé", fight, fighter);
    }


    public boolean canPlay() {
        return fighter != null
                && fighter.isAlive()
                && (getRemainingTime() > 0)
                && (fighter.getAP() > 0 || fighter.getMP() > 0)
                && usedNeurons <= 10
                && !fight.onlyOneTeam();
    }

    public void selectBestAction() {
        final AIAction action = AIAction.AI_ACTIONS.get(mode);
        if (action != null) {
            for (short cell : neuron.myReachableCells) {
                for (SpellLevel spell : this.mySpells) {
                    this.selectBestSpell(action, spell, cell);
                }
            }
        }
    }


    public void applyBestAction() {
        if (neuron.myBestSpell != null) {
            if (neuron.myBestMoveCell != this.fighter.getCellId() && neuron.myBestMoveCell != 0) {
                try {

                    //final Pathfinder path = new Pathfinder(this.fighter.getDirection(), this.fighter.getCellId(), neuron.myBestMoveCell, false, fighter);
                    //path.find();
                    final Path path = new koh.game.fights.utils.Pathfinder(fighter.getFight().getMap(), fight, true, false).findPath(fight.getMap().getCell(fighter.getCellId()), fight.getMap().getCell(neuron.myBestMoveCell), false,fighter.getMP());
                    GameMapMovement action = this.fight.tryMove(this.fighter, path);
                    if (action != null) {
                        BACKGROUND_WORKER.execute(() -> {
                            try {
                                Thread.sleep(path.getMovementTime());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        action.execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (neuron.myFirstTargetIsMe) {
                neuron.myBestCastCell = this.fighter.getCellId();
            }

            this.fight.launchSpell(this.fighter, neuron.myBestSpell, neuron.myBestCastCell, true);
            neuron.setAttacked(true);
            final long timeToSleep = 1250 + Pathfunction.goalDistance(fight.getMap(), fighter.getCellId(), neuron.myBestCastCell) * 250;
            BACKGROUND_WORKER.execute(() -> {
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            //this.fight.stopAction(this.fighter);
        }
    }

    protected void selectBestSpell(AIAction action, SpellLevel spell, short currentCell) {
        Short[] cells = fighter.getCastZone(spell, currentCell);
        for (Short cell : cells) {
            final FightCell fightCell = this.fight.getCell(cell);

            if (fightCell != null) {
                final Fighter firstTarget = fightCell.getFighter();
                if (this.fight.canLaunchSpell(this.fighter, spell, currentCell, cell, firstTarget == null ? -1 : firstTarget.getID())) {
                    double score = this.getSpellScore(action, spell, currentCell, cell);

                    int distance = (Pathfunction.goalDistance(this.fight.getMap(), this.fighter.getCellId(), currentCell) * 5);
                    if (score > 0)
                        if (score - distance < 0)
                            score = 1;
                        else
                            score -= distance;

                    if (fightCell.hasEnnemy(this.fighter.getTeam()) != null) {
                        /*if(firstTarget != null && !firstTarget.isVisibleFor(this.fighter)){
                            score = 0;
                        }
                        else*/ if (score > 0)
                            score += 50;
                    }
                    if (score > neuron.myBestScore) {
                        neuron.myBestScore = (int) score;
                        neuron.myBestSpell = spell;
                        neuron.myFirstTargetIsMe = firstTarget == this.fighter;
                        //if (myNeuron.myFirstTargetIsMe)
                        //{
                        //    myNeuron.myBestCastCell = this.myFighter.CellId;
                        //}
                        //else
                        //{
                        neuron.myBestCastCell = cell;
                        //}
                        neuron.myBestMoveCell = currentCell;
                    }
                }
                else{
                    //System.out.println(spell.toString());
                }
            }
        }
    }

    public void initCells() {
        neuron.myReachableCells.clear();
        neuron.myReachableCells.add(this.fighter.getCellId());

        this.fight.getFightCells().values().stream()
                .filter(cell -> cell.canWalk()
                        && Pathfunction.goalDistance(this.fight.getMap(), this.fighter.getCellId(), cell.getId()) <= this.fighter.getMP())
                .forEach(cell -> neuron.myReachableCells.add(cell.getId()));
    }

    protected double getSpellScore(AIAction action, SpellLevel spell, short currentCellId, short castCell) {
        double score = 0;
        for (EffectInstanceDice effect : spell.getEffects()) {
            final List<Fighter> targets = Arrays.stream((new Zone(effect.getZoneShape(), effect.zoneSize(), MapPoint.fromCellId(currentCellId).advancedOrientationTo(MapPoint.fromCellId(castCell), true), this.fight.getMap()))
                    .getCells(castCell))
                    .map(cell -> fight.getCell(cell))
                    .filter(cell -> cell != null && cell.hasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER, IFightObject.FightObjectType.OBJECT_STATIC))
                    .map(fightCell -> fightCell.getObjectsAsFighter()[0]).collect(Collectors.toList());


            targets.removeIf(target -> !((((ArrayUtils.contains(BLACKLISTED_EFFECTS, effect.effectUid)
                    || EffectHelper.verifyEffectTrigger(fighter, target, spell.getEffects(), effect, false, effect.triggers, castCell))
                    && effect.isValidTarget(fighter, target)
                    && target.isVisibleFor(fighter)
                    && EffectInstanceDice.verifySpellEffectMask(fighter, target, effect)))));

            if (targets.size() > 0 || ((spell.isNeedFreeCell() || spell.isNeedFreeTrapCell()) && targets.size() == 0)) {
                score += Math.floor(action.getEffectScore(this, currentCellId, castCell, effect, targets, false, false));
                //logger.debug("effect {} score {}",effect.getEffectType(), Math.floor(action.getEffectScore(this, currentCellId, castCell, effect, targets, false, false)));
            }else{
                //logger.debug("effect {} mask{} trigger {} ",effect.getEffectType(),effect.targetMask,effect.triggers);
            }

        }
        return score;
    }

    public boolean moveTo(Fighter target, int maxDistance) {
        int baseDistance = Pathfunction.goalDistance(this.fight.getMap(), this.fighter.getCellId(), target.getCellId());
        if (maxDistance > baseDistance) {
            maxDistance = baseDistance;
        }
        int baseDistanceDisplacement = baseDistance - this.fighter.getMP();
        if (baseDistanceDisplacement <= 1) baseDistanceDisplacement = 1;

        if (baseDistanceDisplacement <= maxDistance) {
            short bestCell = -1;
            int bestDistance = maxDistance;

            for (short cell : Pathfunction.getCircleZone(target.getCellId(), maxDistance)) {
                FightCell fCell = fight.getCell(cell);
                if (fCell != null && fCell.canWalk()) {
                    int distance = Pathfunction.goalDistance(this.fight.getMap(), target.getCellId(), cell);
                    if (distance < bestDistance) {
                        bestCell = cell;
                        bestDistance = distance;
                    }
                }
            }
            if (bestCell != -1 && bestCell != this.fighter.getCellId()) {
                final Path path = new koh.game.fights.utils.Pathfinder(fighter.getFight().getMap(), fight, true, false).findPath(fight.getMap().getCell(fighter.getCellId()), fight.getMap().getCell(bestCell), false,fighter.getMP());
                final GameMapMovement action = this.fight.tryMove(this.fighter, path);
                if (action != null) {
                    BACKGROUND_WORKER.execute(() -> {
                        try {
                            Thread.sleep(path.getMovementTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    action.execute();
                }
                return true;
            }
        }
        return false;
    }

    public boolean moveTo(Fighter target, int minDistance, int maxDistance)
    {
       int baseDistance = Pathfunction.goalDistance(this.fight.getMap(), this.fighter.getCellId(), target.getCellId());
        if (maxDistance < minDistance)
        {
            maxDistance = minDistance;
        }
        if (maxDistance > baseDistance)
        {
            maxDistance = baseDistance;
        }
        int baseDistanceDisplacement = baseDistance - this.fighter.getMP();
        if (baseDistanceDisplacement <= 1) baseDistanceDisplacement = 1;
        if (baseDistanceDisplacement <= maxDistance && baseDistanceDisplacement >= minDistance)
        {
            short bestCell = -1;
            int bestDistance = maxDistance;

            for(short Cell : Pathfunction.getCircleZone(target.getCellId(), maxDistance))
            {
                FightCell fCell = fight.getCell(Cell);
                if (fCell != null && fCell.canWalk())
                {
                    int distance = Pathfunction.goalDistance(this.fight.getMap(), target.getCellId(), Cell);
                    if (distance < bestDistance && distance > minDistance)
                    {
                        bestCell = Cell;
                        bestDistance = distance;
                    }
                }
            }

            if (bestCell != -1 && bestCell != this.fighter.getCellId())
            {
                final Path path = new koh.game.fights.utils.Pathfinder(fighter.getFight().getMap(), fight, true, false).findPath(fight.getMap().getCell(fighter.getCellId()), fight.getMap().getCell(bestCell), false,fighter.getMP());
                final GameMapMovement action = this.fight.tryMove(this.fighter, path);
                if (action != null) {
                    BACKGROUND_WORKER.execute(() -> {
                        try {
                            Thread.sleep(path.getMovementTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    action.execute();
                }
                return true;

            }
        }
        return false;
    }

    public boolean moveToEnnemy()
    {
        int bestDistance = 64;
        Fighter bestEnnemy = null;
        for (Fighter ennemy : (Iterable<Fighter>) this.fight.getEnnemyTeam(this.fighter.getTeam()).getAliveFighters()::iterator)
        {
            if(!ennemy.isVisibleFor(this.fighter)){
                continue;
            }
           int distance = Pathfunction.goalDistance(this.fight.getMap(),this.fighter.getCellId(), ennemy.getCellId());

            if (distance < bestDistance)
            {
                bestDistance = distance;
                bestEnnemy = ennemy;
            }
        }
        if (bestEnnemy != null)
        {
            return moveTo(bestEnnemy, 64);
        }
        else return false;
    }

    public boolean moveToEnnemyByCell(int minDistance,int maxDistance)
    {
        int bestDistance = 74;
        Fighter bestEnnemy = null;
        for (Fighter ennemy : (Iterable<Fighter>) this.fight.getEnnemyTeam(this.fighter.getTeam()).getAliveFighters()::iterator)
        {
            if(!ennemy.isVisibleFor(fighter)){
                continue;
            }
            final int distance = Pathfunction.goalDistance(this.fight.getMap(),this.fighter.getCellId(), ennemy.getCellId());

            if (distance < bestDistance)
            {
                bestDistance = distance;
                bestEnnemy = ennemy;
            }
        }
        if (bestEnnemy != null)
        {
            return moveTo(bestEnnemy, minDistance, maxDistance);
        }
        else return false;
    }

    public boolean moveFar() {
        return moveFar(64);
    }

    public boolean moveFar(int maxDistance)
    {
        int bestDistance = maxDistance;
        short bestEnnemyCell = -1;

        for (Fighter ennemy : (Iterable<Fighter>) this.fight.getEnnemyTeam(this.fighter.getTeam()).getAliveFighters()::iterator)
        {
           int distance = Pathfunction.goalDistance(this.fight.getMap(),this.fighter.getCellId(), ennemy.getCellId());

            if (distance < bestDistance)
            {
                bestDistance = distance;
                bestEnnemyCell = ennemy.getCellId();
            }
        }

        if (bestEnnemyCell != -1)
        {
            short bestCell = -1;
            bestDistance = 0;

            for (short cell : Pathfunction.getCircleZone(this.fighter.getCellId(), fighter.getMP()))
            {
                FightCell fCell = fight.getCell(cell);
                if (fCell != null && fCell.canWalk())
                {
                    int distance = Pathfunction.goalDistance(this.fight.getMap(), bestEnnemyCell, cell);
                    if (distance > bestDistance)
                    {
                        boolean Ok = true;
                        for (short AroundCell : Pathfunction.getCircleZone(fighter.getCellId(), distance))
                        {
                            FightCell fAroundCell =fight.getCell(AroundCell);
                            if (fAroundCell != null && fAroundCell.hasEnnemy(fighter.getTeam()) != null)
                            {
                                if (Pathfunction.goalDistance(this.fight.getMap(), cell, AroundCell) < distance)
                                {
                                    Ok = false;
                                    break;
                                }
                            }
                        }
                        if (Ok)
                        {
                            bestCell = cell;
                            bestDistance = distance;
                        }
                    }
                }
            }

            if (bestCell != -1 && bestCell != this.fighter.getCellId())
            {
                final Path path = new koh.game.fights.utils.Pathfinder(fighter.getFight().getMap(), fight, true, false).findPath(fight.getMap().getCell(fighter.getCellId()), fight.getMap().getCell(bestCell), false,fighter.getMP());
                GameMapMovement action = this.fight.tryMove(this.fighter, path);
                if (action != null) {
                    BACKGROUND_WORKER.execute(() -> {
                        try {
                            Thread.sleep(path.getMovementTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    action.execute();
                }
                return true;
            }
        }
        return false;
    }

    public void wait(int millis) //upper bcs wait exist
    {
        BACKGROUND_WORKER.execute(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
            }
        });
    }




    private boolean react() {
        selectBestAction();
        if (neuron.myBestScore > 0)//En fonction des calculs, signifie "Action voulue trouvée"
        {
            applyBestAction();
            return true;
        }
        return false;
    }

    public boolean selfAction() {
        this.mode = AIAction.AIActionEnum.SELF_ACTING;
        return react();
    }

    public boolean madSelfAction() {
        this.mode = AIAction.AIActionEnum.MAD;
        return react();
    }

    /*
     * attack
     */
    public boolean attack() {
        this.mode = AIAction.AIActionEnum.ATTACK;
        return react();
    }

    /*
     * Buff Actions
     */
    public boolean buffMe() {
        this.mode = AIAction.AIActionEnum.BUFF_HIMSELF;
        return react();
    }

    public boolean buffAlly() {
        this.mode = AIAction.AIActionEnum.BUFF_ALLY;
        return react();
    }

    public boolean buff() {
        return buffAlly() || buffMe();
    }

    /*
     * Debuff Actions
     */
    public boolean debuffAlly() {
        this.mode = AIAction.AIActionEnum.DEBUFF_ALLY;
        return react();
    }

    public boolean debuffEnnemy() {
        this.mode = AIAction.AIActionEnum.DEBUFF_ENNEMY;
        return react();
    }

    public boolean debuff() {
        return debuffAlly() || debuffEnnemy();
    }

    /*
     * Heal Actions
     */
    public boolean healMe() {
        this.mode = AIAction.AIActionEnum.HEAL_HIMSELF;
        return react();
    }

    public boolean healAlly() {
        this.mode = AIAction.AIActionEnum.HEAL_ALLY;
        return react();
    }

    public boolean heal() {
        return healAlly() || healMe();
    }

    /*
     * Actions de support
     */
    public boolean support() {
        //return Repels() || HealAlly() || Debuff() || Subbuff() || BuffAlly() || Invocate();
        this.mode = AIAction.AIActionEnum.SUPPORT;
        return react();
    }

    /*
     * Invocation
     */
    public boolean invocate() {
        this.mode = AIAction.AIActionEnum.INVOK;
        return react();
    }

    /*
     * Repousse les ennemis
     */
    public boolean repels() {
        this.mode = AIAction.AIActionEnum.REPELS;
        return react();
    }

    /*
     * Ralenti les ennemis
     */
    public boolean subbuff() {
        this.mode = AIAction.AIActionEnum.SUBBUFF;
        return react();
    }


}


