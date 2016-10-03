package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.Random;

/**
 * Created by Melancholia on 9/24/16.
 * 35,Tueur à gages,descriptionIdtype=Les adversaires doivent être tués dans l'ordre désigné. Une nouvelle cible est désignée à chaque fois que la cible précédente est tuée.
 */
public class GageKiller extends Challenge {

    private Random rnd;

    public GageKiller(Fight fight, FightTeam team) {
        super(fight, team);
        this.rnd = new Random();
    }

    @Override
    public void onFightStart() {
        this.target = getEnnemyTeam()
                .getAliveFighters()
                .toArray(Fighter[]::new)[rnd.nextInt((int) getEnnemyTeam().getAliveFighters().count())];
        this.sendSingleTarget();
    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {
        if (killer.isEnnemyWith(target)) {
            if (target != this.target) {
                this.failChallenge();
            }else{
                final int count = (int) getEnnemyTeam().getAliveFighters().count();
                if(count <= 0){
                    this.validate();
                    return;
                }
                this.target = getEnnemyTeam()
                        .getAliveFighters()
                        .toArray(Fighter[]::new)[rnd.nextInt(count)];
                this.sendSingleTarget();
            }
        }
    }

    @Override
    public void onFighterMove(Fighter fighter, MovementPath path) {

    }

    @Override
    public void onFighterSetCell(Fighter fighter, short startCell, short endCell) {

    }

    @Override
    public void onFighterCastSpell(Fighter fighter, SpellLevel spell) {

    }

    @Override
    public void onFighterCastWeapon(Fighter fighter, Weapon weapon) {

    }

    @Override
    public void onFighterTackled(Fighter fighter) {

    }

    @Override
    public void onFighterLooseLife(Fighter fighter, EffectCast cast, int damage) {

    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
