package koh.game.fights.AI;

import koh.concurrency.ImprovedCachedThreadPool;
import koh.game.actions.GameMapMovement;
import koh.game.dao.DAO;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.maps.pathfinding.*;
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
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.VirtualFighter;
import koh.game.paths.PathNotFoundException;
import koh.game.paths.Pathfinder;
import koh.protocol.client.enums.IAMindEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.ShowCellMessage;
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


    @Getter
    private Fight fight;
    @Getter
    private VirtualFighter fighter;
    protected List<SpellLevel> mySpells;
    @Getter
    private AINeuron neuron;
    private AIAction.AIActionEnum mode;
    private int type;
    @Getter
    private int usedNeurons = 0;
    protected long whenIStartIA;

    public AIProcessor(Fight fight, VirtualFighter fighter) {
        this.type = IAMindOf(fighter);
        this.fight = fight;
        this.fighter = fighter;
        this.mySpells = fighter.getSpells();
        this.mode = AIAction.AIActionEnum.SELF_ACTING;
    }

    public static int IAMindOf(Fighter fr) {
        if (fr == null) {
            return IAMindEnum.PASSIVE.value;
        } else if (fr instanceof MonsterFighter) {
            return ((MonsterFighter) fr).getGrade().getMonster().getMonsterAI();
        }
        /*else if (f instanceof DoubleFighter)
        {
            return IAMindEnum.BLOCKER;
        }
        else if (f instanceof PercepteurFighter)
        {
            return IAMindEnum.TAXCOLLECTOR;
        }*/
        else {
            return IAMindEnum.PASSIVE.value;
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

                usedNeurons++;
                try {
                    mind.play(this);
                } catch (FightException Fe) {
                    throw Fe;
                } catch (FighterException fe) {
                    throw fe;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e);
                } finally {
                    if (neuron != null) {
                        neuron.finalize();
                        neuron = null;
                    }
                }
                BACKGROUND_WORKER.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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

    public long getRemainingTime()
    {
        return (whenIStartIA + fight.getTurnTime()) - System.currentTimeMillis();
    }

    public void stop() throws StopAIException {
        throw new StopAIException("Arrêt de l'IA demandé", fight, fighter);
    }


    public boolean canPlay()
    {
        return fighter != null
                && fighter.isAlive()
                && (getRemainingTime() > 0)
                && (fighter.getAP() > 0 || fighter.getMP() > 0)
                && usedNeurons <= 10
                && !fight.onlyOneTeam();
    }

    public void selectBestAction()
    {
        AIAction action = AIAction.AI_ACTIONS.get(mode);
        if (action != null){
            for (short cell : neuron.myReachableCells)
            {
                for (SpellLevel spell : this.mySpells)
                {
                    this.selectBestSpell(action, spell, cell);
                }
            }
        }
    }


    public void applyBestAction()
    {
        if (neuron.myBestSpell != null)
        {
            if (neuron.myBestMoveCell != this.fighter.getCellId() && neuron.myBestMoveCell != 0)
            {
                try {
                    Pathfinder path = new Pathfinder(this.fighter.getDirection(), this.fighter.getCellId(), neuron.myBestMoveCell, false, fighter);
                    path.find();
                    GameMapMovement action = this.fight.tryMove(this.fighter, path);
                    if (action != null)
                    {
                        BACKGROUND_WORKER.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(path.getPath().estimateTimeOn());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        fighter.setDirection(path.getPath().last().getOrientation());
                    }
                } catch (PathNotFoundException e) {
                }
            }
            if (neuron.myFirstTargetIsMe)
            {
                neuron.myBestCastCell = this.fighter.getCellId();
            }

            this.fight.launchSpell(this.fighter, neuron.myBestSpell, neuron.myBestCastCell, true);
            neuron.myAttacked = true;
            BACKGROUND_WORKER.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1250 + koh.game.entities.environments.Pathfinder.getGoalDistance(fight.getMap(), fighter.getCellId(), neuron.myBestCastCell)*250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            //this.fight.stopAction(this.fighter);
        }
    }

    protected void selectBestSpell(AIAction Action, SpellLevel spell, short currentCell) {
        Short[] cells = fighter.getCastZone(spell,currentCell);
        for (Short cell : cells)
        {
            FightCell fightCell = this.fight.getCell(cell);

            if (fightCell != null)
            {
                Fighter firstTarget = fightCell.getFighter();
                if (this.fight.canLaunchSpell(this.fighter, spell, currentCell, cell, firstTarget == null ? -1 : firstTarget.getID()))
                {
                    double score = this.getSpellScore(Action, spell, currentCell, cell);

                    int distance = (koh.game.entities.environments.Pathfinder.getGoalDistance(this.fight.getMap(), this.fighter.getCellId(), currentCell) * 5);
                    if (score > 0)
                        if (score - distance < 0)
                            score = 1;
                        else
                            score -= distance;

                    if (fightCell.hasEnnemy(this.fighter.getTeam()) != null)
                    {
                        if (score > 0)
                            score += 50;
                    }

                    if (score >  neuron.myBestScore)
                    {
                        neuron.myBestScore = (int)score;
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
            }
        }
    }

    public void initCells(){
        neuron.myReachableCells.clear();
        neuron.myReachableCells.add(this.fighter.getCellId());

        this.fight.getFightCells().values().stream()
                .filter(cell -> cell.canWalk()
                        && koh.game.entities.environments.Pathfinder.getGoalDistance(this.fight.getMap(), this.fighter.getCellId(), cell.getId()) <= this.fighter.getMP())
                .forEach(cell -> neuron.myReachableCells.add(cell.getId()));
    }

    protected double getSpellScore(AIAction action, SpellLevel spell, short currentCellId, short castCell)
    {
        double score = 0;
        for (EffectInstanceDice effect : spell.getEffects())
        {
            List<Fighter> targets = Arrays.stream((new Zone(effect.getZoneShape(), effect.zoneSize(), MapPoint.fromCellId(fighter.getCellId()).advancedOrientationTo(MapPoint.fromCellId(castCell), true), this.fight.getMap()))
                    .getCells(castCell))
                    .map(cell -> fight.getCell(cell))
                    .filter(cell -> cell != null && cell.hasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER, IFightObject.FightObjectType.OBJECT_STATIC))
                    .map(fightCell -> fightCell.getObjectsAsFighter()[0]).collect(Collectors.toList());

            targets.removeIf(target -> !((((ArrayUtils.contains(BLACKLISTED_EFFECTS,effect.effectUid)
                || EffectHelper.verifyEffectTrigger(fighter, target, spell.getEffects(), effect, false, effect.triggers, castCell))
                && effect.isValidTarget(fighter, target)
                && EffectInstanceDice.verifySpellEffectMask(fighter, target, effect)))));

            if (targets.size() > 0 || (spell.isNeedFreeCell() && targets.size() == 0))
            {
                score += Math.floor(action.getEffectScore(this, currentCellId, castCell, effect, targets,false,false));
            }
        }
        return score;
    }

    private boolean react()
    {
        selectBestAction();
        if (neuron.myBestScore > 0)//En fonction des calculs, signifie "Action voulue trouvée"
        {
            applyBestAction();
            return true;
        }
        return false;
    }

    public boolean selfAction()
    {
        this.mode = AIAction.AIActionEnum.SELF_ACTING;
        return react();
    }

    public boolean madSelfAction()
    {
        this.mode = AIAction.AIActionEnum.MAD;
        return react();
    }

    /*
     * attack
     */
    public boolean attack()
    {
        this.mode = AIAction.AIActionEnum.ATTACK;
        return react();
    }

    /*
     * Buff Actions
     */
    public boolean buffMe()
    {
        this.mode = AIAction.AIActionEnum.BUFF_HIMSELF;
        return react();
    }

    public boolean buffAlly()
    {
        this.mode = AIAction.AIActionEnum.BUFF_ALLY;
        return react();
    }

    public boolean buff()
    {
        return buffAlly() || buffMe();
    }

    /*
     * Debuff Actions
     */
    public boolean debuffAlly()
    {
        this.mode = AIAction.AIActionEnum.DEBUFF_ALLY;
        return react();
    }

    public boolean debuffEnnemy()
    {
        this.mode = AIAction.AIActionEnum.DEBUFF_ENNEMY;
        return react();
    }

    public boolean debuff()
    {
        return debuffAlly() || debuffEnnemy();
    }

    /*
     * Heal Actions
     */
    public boolean healMe()
    {
        this.mode = AIAction.AIActionEnum.HEAL_HIMSELF;
        return react();
    }

    public boolean healAlly()
    {
        this.mode = AIAction.AIActionEnum.HEAL_ALLY;
        return react();
    }

    public boolean heal()
    {
        return healAlly() || healMe();
    }

    /*
     * Actions de support
     */
    public boolean support()
    {
        //return Repels() || HealAlly() || Debuff() || Subbuff() || BuffAlly() || Invocate();
        this.mode = AIAction.AIActionEnum.SUPPORT;
        return react();
    }

    /*
     * Invocation
     */
    public boolean invocate()
    {
        this.mode = AIAction.AIActionEnum.INVOK;
        return react();
    }

    /*
     * Repousse les ennemis
     */
    public boolean repels()
    {
        this.mode = AIAction.AIActionEnum.REPELS;
        return react();
    }

    /*
     * Ralenti les ennemis
     */
    public boolean subbuff()
    {
        this.mode = AIAction.AIActionEnum.SUBBUFF;
        return react();
    }


}


