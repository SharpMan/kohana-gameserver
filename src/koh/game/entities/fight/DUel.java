package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.HashMap;

/**
 * Created by Melancholia on 9/24/16.
 * 45,=Lorsqu'un personnage attaque un adversaire, aucun autre personnage ne doit attaquer cet adversaire pendant toute la durée du combat.
 */
public class Duel extends Challenge {

    public Duel(Fight fight, FightTeam team) {
        super(fight, team);
        this.oppositions = new HashMap<>(8);
    }

    private HashMap<Fighter,Fighter> oppositions;

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {

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
        if(fighter.getTeam() != team && cast.caster.isPlayer()){
            if(oppositions.containsKey(fighter) && oppositions.get(fighter) != cast.caster){
                this.failChallenge();
            }
            else if(!oppositions.containsKey(fighter)){
                this.oppositions.put(fighter,cast.caster);
            }
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
