package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.utils.Pathfinder;

/**
 * Created by Melancholia on 9/24/16.
 * 36,Finir son tour sur une cellule adjacente à celle d'un adversaire.
 */
public class Hardi extends Challenge {
    public Hardi(Fight fight, FightTeam team) {
        super(fight, team);
    }

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {
        if(fighter.isPlayer() && fighter.getTeam() == team){
            if(Pathfunction.getFightersNext(fighter).noneMatch(f -> f.isEnnemyWith(fighter))){
                this.failChallenge();
            }
        }
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

    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
