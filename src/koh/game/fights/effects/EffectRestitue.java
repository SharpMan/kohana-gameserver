package koh.game.fights.effects;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.fights.FightState;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.fighters.CharacterFighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.messages.game.context.fight.GameFightUpdateTeamMessage;
import koh.protocol.messages.game.context.fight.character.GameFightShowFighterMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

import java.util.Optional;

/**
 * Created by Melancholia on 8/15/16.
 */
public class EffectRestitue extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {

        final Fighter zomby = castInfos.caster.getTeam().getDeadFighters()
                .filter(f -> !f.hasSummoner() && !f.isLeft())
                .findFirst()
                .orElse(castInfos.caster.getTeam().getDeadFighters()
                        .filter(f -> !f.isLeft())
                        .findFirst()
                        .orElse(null)
                );

        if(zomby != null && !castInfos.getCell().hasFighter() && castInfos.getCell().canWalk()){
            zomby.setLife((int) Math.floor(zomby.getMaxLife() * 0.01f * castInfos.randomJet(zomby)));
            zomby.setSummoner(castInfos.caster);
            zomby.setDead(false);

            castInfos.caster.getFight().getFightWorker().fighters().remove(zomby);
            castInfos.caster.getFight().getFightWorker().summonFighter(zomby);
            joinFightTeam(zomby, castInfos.caster.getTeam(), false, castInfos.cellId, true);
            castInfos.caster.getFight().sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_CHARACTER_SUMMON_DEAD_ALLY_IN_FIGHT, castInfos.caster.getID(), (GameFightFighterInformations) zomby.getGameContextActorInformations(Pl)));

            castInfos.caster.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base--;
            if (castInfos.caster instanceof CharacterFighter)
                castInfos.caster.send(castInfos.caster.asPlayer().getCharacterStatsListMessagePacket());
        }


        return -1;
    }

    protected static void joinFightTeam(Fighter fighter, FightTeam team, boolean leader, short cell, boolean sendInfos) {

        // Ajout a la team
        team.fighterJoin(fighter);

        fighter.setCell(fighter.getFight().getCell(cell));


        if (sendInfos) {
            fighter.getFight().sendToField(new GameFightShowFighterMessage(fighter.getGameContextActorInformations(null)));
        }

        fighter.getFight().sendToField(fighter.getFight().getFightTurnListMessage());
    }


}
