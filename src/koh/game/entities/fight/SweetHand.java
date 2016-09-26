package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

/**
 * Created by Melancholia on 8/29/16.
 * 19,Mains propres,descriptionIdtype=Achever tous les monstres sans leur occasionner de dégâts directs pendant toute la durée du combat. Les pièges, les glyphes, les poisons, les dommages occasionnés par les déplacements, les renvois de dommages ainsi que les attaques des invocations peuvent être utilisés.
 */
public class SweetHand extends Challenge {
    public SweetHand(Fight fight, FightTeam team) {
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
        //Friend cast to a ennemy
        if(!cast.caster.hasSummoner() && cast.caster.getTeam() == team && fighter.getTeam() != team){
            if(!cast.isGlyph && !cast.isPoison && !cast.isPoutch && !cast.isReflect && !cast.isReflect && !cast.isReturnedDamages){
                this.failChallenge();
            }
        }

    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
