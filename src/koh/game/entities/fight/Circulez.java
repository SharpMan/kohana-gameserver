package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.StatsEnum;

import java.util.Arrays;

/**
 * Created by Melancholia on 8/29/16.
 * TODO MASK
 * 21,Ne pas tenter de retirer de PM aux adversaires pendant toute la durée du combat.
 */
public class Circulez extends Challenge{
    public Circulez(Fight fight, FightTeam team) {
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
        if(Arrays.stream(spell.getEffects()).anyMatch(e-> e.getEffectType() == StatsEnum.SUB_PM
        || e.getEffectType() == StatsEnum.SUB_PM_ESQUIVE
        || e.getEffectType() == StatsEnum.SUB_PM_ESQUIVE_2)){
            this.failChallenge();
        }
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
