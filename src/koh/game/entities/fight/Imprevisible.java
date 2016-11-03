package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Melancholia on 9/24/16.
 * 34,Toutes les attaques doivent être concentrées sur la cible désignée à chaque nouveau tour d'un personnage.
 */
public class Imprevisible extends Challenge {

    private Random random;

    public Imprevisible(Fight fight, FightTeam team) {
        super(fight, team);
        this.random = new SecureRandom();
    }

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {
        if (fighter.isPlayer() && fighter.getTeam() == team) {
            int num = random.nextInt((int) getEnnemyTeam().getAliveFighters().count());
            if(num < 0){
                num = 0;
            }
            this.target = getEnnemyTeam()
                    .getAliveFighters()
                    .toArray(Fighter[]::new)[num];
            this.sendSingleTarget();
        }

    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {

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
        if (cast.caster.getTeam() == team &&
                cast.caster.isPlayer() &&
                cast.caster == fight.getCurrentFighter() &&
                fighter.isFriendlyWith(target) &&
                fighter != target) {
            this.failChallenge();
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
