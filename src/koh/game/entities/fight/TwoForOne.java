package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

/**
 * Created by Melancholia on 9/24/16.
 * 42,Lorsqu'un personnage achève un adversaire, il doit obligatoirement achever un (et un seul) deuxième adversaire pendant son tour de jeu.
 */
public class TwoForOne extends Challenge {
    public TwoForOne(Fight fight, FightTeam team) {
        super(fight, team);
    }

    private int turn = -2;

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {
        if(turn != -2){
            if(turn != -1){
                this.failChallenge();
            }
        }

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {
        if(target.getTeam() != team){
            if(turn == -2){
                this.target = killer;
                turn = fight.getFightWorker().fightTurn;
            }else if(turn == fight.getFightWorker().fightTurn && killer == this.target){
                turn = -1;
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
