package koh.game.fights.AI.actions;

import koh.game.dao.DAO;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.Spell;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.AI.AIAction;
import koh.game.fights.AI.AIProcessor;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectPush;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightTrap;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Melancholia on 1/11/16.
 */
public class HealHimselfAction extends AIAction {

    @Override
    protected double scoreHeal(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        double score = 0;
        int baseScore = 25;
        baseScore *= effect.randomJet();

        for (Fighter target : targets)
        {
            int currScore = baseScore;
            if (target.getStates().hasState(FightStateEnum.INVISIBLE) && effect.getEffectType() == StatsEnum.INVISIBILITY)
            {
                score -= currScore;
                continue;
            }
            double percentLife = Math.ceil((double)(target.getLife() / target.getMaxLife()) * 100);
            if (percentLife < 10)
                currScore *= 12;
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;
            else if (percentLife >= 95)
            {
                continue;
            }

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (target == AI.getFighter())
                {
                    score -= currScore * 4;
                }
                else if (target.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (target == AI.getFighter())
                {
                    score += currScore * 4;
                }
                else  if (target.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreBuff_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        double score = 0;
        int baseScore = 10;

        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter target : targets)
        {
            int currScore = baseScore;
            if (target.getStates().hasState(FightStateEnum.INVISIBLE) && effect.getEffectType() == StatsEnum.INVISIBILITY)
            {
                score -= currScore;
                continue;
            }
            double percentLife = Math.ceil((double)(target.getLife() / target.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (target.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (target.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreBuff_II(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        double score = 0;
        int baseScore = 12;

        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter target : targets)
        {
            int currScore = baseScore;
            if (target.getStates().hasState(FightStateEnum.INVISIBLE) && effect.getEffectType() == StatsEnum.INVISIBILITY)
            {
                score -= currScore;
                continue;
            }
            double percentLife = Math.ceil((double)(target.getLife() / target.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (target.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (target.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreBuff_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        double score = 0;
        int baseScore = 15;

        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter target : targets)
        {
            int currScore = baseScore;
            if (target.getStates().hasState(FightStateEnum.INVISIBLE) && effect.getEffectType() == StatsEnum.INVISIBILITY)
            {
                score -= currScore;
                continue;
            }
            double percentLife = Math.ceil((double)(target.getLife() / target.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (target.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (target.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreDamage_0(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        double score = 0;
        int baseScore = 2;
        baseScore *= effect.randomJet();
        for (Fighter target : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(target.getLife() / target.getMaxLife()) * 100);
            if (percentLife < 5)
                currScore *= 8;
            else if (percentLife < 10)
                currScore *= 5;
            else if (percentLife < 25)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (target.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
            else
            {
                if (target.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreDamage_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 15;
        baseScore *= Math.abs(effect.diceNum);
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 5)
                currScore *= 8;
            else if (percentLife < 10)
                currScore *= 5;
            else if (percentLife < 25)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreDamage_II(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 18;
        baseScore *= Math.abs(effect.diceNum);
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 5)
                currScore *= 8;
            else if (percentLife < 10)
                currScore *= 5;
            else if (percentLife < 25)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreDamage_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 20  ;
        baseScore *= Math.abs(effect.diceNum);
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 5)
                currScore *= 8;
            else if (percentLife < 10)
                currScore *= 5;
            else if (percentLife < 25)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 3;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreDamagesPerPA(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;

        int baseScore = 11;

        baseScore *= effect.randomJet();

        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            currScore *= fighter.getMaxAP();
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreSubBuff_I(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        int score = 0;
        int baseScore = 10;
        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreSubBuff_II(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        int score = 0;
        int baseScore = 12;
        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreSubBuff_III(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        int score = 0;
        int baseScore = 15;
        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreSubBuff_IV(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        int score = 0;
        int baseScore = 18;
        if (!notUseJet)
        {
            baseScore *= effect.randomJet();
        }

        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;
            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreAddStateGood(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 11;
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }
        return score;
    }

    @Override
    protected double scoreAddStateBad(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 11;
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }
        return score;
    }

    @Override
    protected double scoreRemStateGood(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int BaseScore = 11;
        for (Fighter fighter : targets)
        {
            int currScore = BaseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 2;
            else if (percentLife < 30)
                currScore *= 3;
            else if (percentLife < 50)
                currScore *= 5;
            else if (percentLife < 75)
                currScore *= 8;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }
        return score;
    }

    @Override
    protected double scoreRemStateBad(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        int score = 0;
        int baseScore = 11;
        for (Fighter fighter : targets)
        {
            int currScore = baseScore;
            double percentLife = Math.ceil((double)(fighter.getLife() / fighter.getMaxLife()) * 100);
            if (percentLife < 20)
                currScore *= 8;
            else if (percentLife < 30)
                currScore *= 5;
            else if (percentLife < 50)
                currScore *= 3;
            else if (percentLife < 75)
                currScore *= 2;

            if (effect.duration > 0)
                currScore *= effect.duration;

            if (reverse)
            {
                if (fighter.getTeam() == AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
            else
            {
                if (fighter.getTeam() != AI.getFighter().getTeam())
                    score -= currScore * 2;
                else
                    score += currScore;
            }
        }
        return score;
    }

    @Override
    protected double scoreDebuff(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse) {
        if (reverse)//On evite la boucle infinie
        {
            return 0;
        }
        int score = 0;
        int baseScore = 11;

        //BaseScore *= effect.randomJet();

        for (Fighter target : targets)
        {
            int currScore = baseScore;
            List<Fighter> cible = new ArrayList<>();
            cible.add(target);
            for(ArrayList<BuffEffect> buffs : target.getBuff().getBuffsDec().values()){
                for(BuffEffect buff : buffs){
                    currScore += (int)this.getEffectScore(AI, (short)-1, (short)-1, buff.castInfos.effect, cible, true,false);
                }
            }
            score += currScore;
        }

        return score;
    }

    @Override
    protected double scoreInvocation(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean invokPreview) {
        if (reverse)//On evite la boucle infinie
        {
            return 0;
        }
        int baseScore = 11;
        int score = baseScore;

        final int invocationId = effect.diceNum;
        final int invocationLevel = effect.diceSide;
        if (invokPreview)
        {
            return baseScore * invocationLevel;
        }
        if (!AI.getNeuron().myScoreInvocations.containsKey(invocationId))
        {
            MonsterTemplate monster = DAO.getMonsters().find(invocationId);
            // Template de monstre existante
            if (monster != null)
            {
                final MonsterGrade monsterLevel = monster.getLevelOrNear(invocationLevel);
                // Level de monstre existant
                if (monsterLevel != null)
                {
                    List<Fighter> possibleTargets = AI.getFight().getAllyTeam(AI.getFighter().getTeam()).getFighters().filter(x -> x.isAlive()).collect(Collectors.toList());
                    for (SpellLevel spell : monsterLevel.getSpells())
                    {
                        for (EffectInstanceDice spellEffect : spell.getEffects())
                        {
                            int currScore = (int)this.getEffectScore(AI, (short)-1, (short)-1, spellEffect, possibleTargets, false, true);
                            if (currScore > 0)
                            {
                                score += currScore;
                            }
                        }
                    }
                    possibleTargets = AI.getFight().getEnnemyTeam(AI.getFighter().getTeam()).getFighters().filter(x -> x.isAlive()).collect(Collectors.toList());
                    for (SpellLevel spell : monsterLevel.getSpells())
                    {
                        for (EffectInstanceDice spellEffect : spell.getEffects())
                        {
                            int currScore = (int)this.getEffectScore(AI, (short)-1, (short)-1, spellEffect, possibleTargets, false, true);
                            if (currScore > 0)
                            {
                                score += currScore;
                            }
                        }
                    }
                    score += monsterLevel.getStats().totalBasePoints();
                    score *= monsterLevel.getLevel();
                    AI.getNeuron().myScoreInvocations.put(invocationId, score);
                    return score;
                }
            }
        }
        else
        {
            return AI.getNeuron().myScoreInvocations.get(invocationId);
        }
        return 0;
    }

    @Override
    protected double scoreInvocationStatic(AIProcessor AI, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean invokPreview) {
        if (reverse)//On evite la boucle infinie
        {
            return 0;
        }
        int baseScore = 11;
        int score = baseScore;

        int invocationId = effect.diceNum;
        int invocationLevel = effect.diceSide;
        if (invokPreview)
        {
            return baseScore * invocationLevel;
        }
        if (!AI.getNeuron().myScoreInvocations.containsKey(invocationId))
        {

            MonsterGrade monsterLevel = DAO.getMonsters().find(invocationId).getLevelOrNear(invocationLevel);
            // Level de monstre existant
            if (monsterLevel != null)
            {
                List<Fighter> possibleTargets = AI.getFight().getAllyTeam(AI.getFighter().getTeam()).getFighters().filter(x -> x.isDead()).collect(Collectors.toList());
                for (SpellLevel spell : monsterLevel.getSpells())
                {
                    for (EffectInstanceDice spellEffect : spell.getEffects())
                    {
                        int currScore = (int) this.getEffectScore(AI, (short)-1, (short)-1, spellEffect, possibleTargets, false,true);
                        if (currScore > 0)
                        {
                            score += currScore;
                        }
                    }
                }
                score += monsterLevel.getStats().totalBasePoints();
                score *= monsterLevel.getLevel();
                AI.getNeuron().myScoreInvocations.put(invocationId, score);
                return score;
            }

        }
        else
        {
            return AI.getNeuron().myScoreInvocations.get(invocationId);
        }
        return 0;
    }

    private int scorePush(AIProcessor AI, Fighter target, byte direction, int length, boolean fear)
    {
        boolean isAlly = target.getTeam() == AI.getFighter().getTeam();
        List<Fighter> fighterList = new ArrayList<Fighter>();
        fighterList.add(target);
        FightCell lastCell = target.getMyCell();
        int score = 0;

        for (FightActivableObject layer : target.getMyCell().getObjectsLayer())//On cherche à savoir si décaller de cette cellule est utile
        {
            int layerScore = 0;
            for (EffectInstanceDice effect : layer.getCastSpell().getEffects())
            {
                layerScore = (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, fighterList, true, true));
            }
            /*if (Layer is FightBlypheLayer)
            {
                LayerScore *= 2;
            }*/
            score += layerScore;
        }

        int pathScore = 4;
        int finalLength = 0;
        for (int i = 0; i < length; i++)
        {
            FightCell nextCell = target.getFight().getCell(Pathfinder.nextCell(lastCell.getId(), direction));
            if (nextCell != null)
            {
                lastCell = nextCell;
            }

            if (nextCell != null && nextCell.isWalkable())
            {
                if (nextCell.hasGameObject(IFightObject.FightObjectType.OBJECT_FIGHTER) || nextCell.hasGameObject(IFightObject.FightObjectType.OBJECT_STATIC) || target.getStates().hasState(FightStateEnum.ENRACINÉ))
                {
                    if (!fear)
                    {
                        pathScore *= EffectPush.getRANDOM_PUSHDAMAGE().nextInt(4) + 4;
                        if (isAlly)
                        {
                            pathScore *= -1;
                        }
                    }
                    break;
                }
                else if (nextCell.hasGameObject(IFightObject.FightObjectType.OBJECT_TRAP))
                {//On Stop seulement : ce genre de calcul se fera a la fin.
                    break;
                }
            }
            else
            {
                if (!fear)
                {
                    pathScore *= EffectPush.getRANDOM_PUSHDAMAGE().nextInt(4) + 4;;
                    if (isAlly)
                    {
                        pathScore *= -1;
                    }
                }
                break;
            }
            finalLength += 1;
        }
        score += finalLength * pathScore;
        if (lastCell != target.getMyCell())
        {
            for (FightActivableObject layer : target.getMyCell().getObjectsLayer())
            {
                int layerScore = 0;
                for (EffectInstanceDice Effect : layer.getCastSpell().getEffects())
                {
                    layerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, Effect, fighterList, false, true));
                }
                if (layer instanceof FightTrap)// TODO : Calculate if traplayer others targets
                {
                    layerScore *= 4;//Immediat
                }
                /*else if (layer instanceof FightBlypheLayer)
                {
                    layerScore *= 2;//Debut de tour
                }*/
                score += layerScore;
            }
        }

        return score;
    }

    @Override
    protected double scoreRepulse(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview, boolean isFear) {
        if (invokPreview)
        {
            return 0;
        }
        int score = 0;
        if (isFear)
        {
            byte d = Pathfinder.getDirection(AI.getFight().getMap(),AI.getFighter().getCellId(), castCell);
            FightCell startCell = AI.getFight().getCell(Pathfinder.nextCell(AI.getFighter().getCellId(), d));
            FightCell endCell = AI.getFight().getCell(castCell);
            if (startCell != null && endCell != null)
            {
                Fighter target = startCell.getFighter();
                if (target != null)
                {
                    score += scorePush(AI, target, Pathfinder.getDirection(AI.getFight().getMap(),AI.getFighter().getCellId(), castCell),
                            Pathfinder.getGoalDistance(AI.getFight().getMap(),AI.getFighter().getCellId(), castCell), true);
                }
            }
        }
        else
        {
            FightCell startCell = AI.getFight().getCell(castCell);
            if (startCell != null)
            {
                Fighter target = startCell.getFighter();
                if (target != null)
                {
                    score += scorePush(AI, target, Pathfinder.getDirection(AI.getFight().getMap(),AI.getFighter().getCellId(),castCell), effect.randomJet(), false);
                }
            }
        }

        return score;
    }

    @Override
    protected double scoreAttract(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview) {
        if (invokPreview)
        {
            return 0;
        }

        int score = 0;
        byte d = Pathfinder.getDirection(AI.getFight().getMap(),castCell, AI.getFighter().getCellId());
        FightCell endCell = AI.getFight().getCell(Pathfinder.nextCell(AI.getFighter().getCellId(), d));
        FightCell startCell = AI.getFight().getCell(castCell);
        if (startCell != null && endCell != null)
        {
            Fighter target = startCell.getFighter();
            if (target != null)
            {
                score += scorePush(AI, target, Pathfinder.getDirection(AI.getFight().getMap(),castCell, AI.getFighter().getCellId()), Pathfinder.getGoalDistance(AI.getFight().getMap(), castCell, AI.getFighter().getCellId()), true);
            }
        }

        return score;
    }

    @Override
    protected double scoreDeplace(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview, boolean isThrow) {
        if (invokPreview)
        {
            return 0;
        }
        int score = 0;

        if (isThrow)
        {
            FightCell targetCell = AI.getFight().getCell(castCell);
            if (targetCell != null)
            {
                BuffEffect infos = AI.getFighter().getStates().findState(FightStateEnum.PORTEUR);
                if (infos != null)
                {
                    Fighter target = infos.target;
                    if (target != null && target.getStates().hasState(FightStateEnum.PORTÉ))
                    {
                        List<Fighter> targetList = new ArrayList<>();
                        targetList.add(target);
                        for (FightActivableObject layer : targetCell.getObjectsLayer())
                        {
                            int layerScore = 0;
                            for (EffectInstanceDice effectLayer : layer.getCastSpell().getEffects())
                            {
                                layerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, targetList, false, true));
                            }
                            if (layer instanceof FightTrap)
                            {
                                layerScore *= 4;//Immediat
                            }
                    /*else if (Layer ista FightBlypheLayer)
                    {
                        LayerScore *= 2;//Debut de tour
                    }*/
                            score += layerScore;
                        }
                    }
                }
            }
        }
        else
        {

        }

        return score;
    }

    @Override
    protected double scoreExchangePlace(AIProcessor AI, short casterCell, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean invokPreview) {
        int score = 0;
        FightCell targetCell = AI.getFight().getCell(castCell);
        FightCell launchCell = AI.getFight().getCell(casterCell);
        if (targetCell != null)
        {
            Fighter target = targetCell.getFighter();
            if (target != null)
            {
                List<Fighter> targetList = new ArrayList<>();
                targetList.add(AI.getFighter());
                for (FightActivableObject layer : targetCell.getObjectsLayer())
                {
                    int layerScore = 0;
                    for (EffectInstanceDice effectLayer : layer.getCastSpell().getEffects())
                    {
                        layerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, targetList, false, true));
                    }
                    if (layer instanceof FightTrap)
                    {
                        layerScore *= 4;//Immediat
                    }
                    /*else if (Layer ista FightBlypheLayer)
                    {
                        LayerScore *= 2;//Debut de tour
                    }*/
                    score += layerScore;
                }

                targetList = new ArrayList<Fighter>();
                targetList.add(target);
                for (FightActivableObject layer : launchCell.getObjectsLayer())
                {
                    int layerScore = 0;
                    for (EffectInstanceDice effectLayer : layer.getCastSpell().getEffects())
                    {
                        layerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, targetList, false, true));
                    }
                    if (layer instanceof FightTrap)
                    {
                        layerScore *= 4;//Immediat
                    }
                    /*else if (Layer ista FightBlypheLayer)
                    {
                        LayerScore *= 2;//Debut de tour
                    }*/
                    score += layerScore;
                }
            }
        }
        return score;
    }

    @Override
    protected double scoreUseLayer(AIProcessor AI, short castCell, EffectInstanceDice effect, List<Fighter> targets, boolean reverse, boolean notUseJet) {
        int score = 0;
        Spell spell = DAO.getSpells().findSpell(effect.diceNum);
        if (spell != null)
        {
            SpellLevel spellLevel = spell.getSpellLevel(effect.diceSide);
            if (spellLevel != null)
            {
                List<Fighter> possibleTargets = AI.getFight().getEnnemyTeam(AI.getFighter().getTeam()).getFighters().filter(x -> x.isAlive()).collect(Collectors.toList());
                int LayerScore = 0;
                for (EffectInstanceDice effectLayer : spellLevel.getEffects())
                {
                    LayerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, possibleTargets, false, true));
                }
                if (LayerScore <= 0)
                {
                    LayerScore = 0;
                    possibleTargets = AI.getFight().getAllyTeam(AI.getFighter().getTeam()).getFighters().filter(x -> x.isAlive()).collect(Collectors.toList());
                    for (EffectInstanceDice effectLayer : spellLevel.getEffects())
                    {
                        LayerScore += (int)Math.floor(AIAction.AI_ACTIONS.get(AIActionEnum.SELF_ACTING).getEffectScore(AI, (short)-1, (short)-1, effect, possibleTargets, false, true));
                    }
                }
                score += LayerScore;
            }
        }

        return score;
    }
}
