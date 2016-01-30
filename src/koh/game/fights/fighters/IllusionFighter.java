package koh.game.fights.fighters;

import java.util.Arrays;
import java.util.List;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.messages.game.actions.fight.GameActionFightVanishMessage;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberCharacterInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.GameFightCharacterInformations;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public class IllusionFighter extends StaticFighter {

    public IllusionFighter(koh.game.fights.Fight fight, Fighter summoner) {
        super(fight, summoner,null);
        this.stats = new GenericStats();
        this.stats.merge(summoner.getStats());
        super.initFighter(this.stats, fight.getNextContextualId());
        this.entityLook = EntityLookParser.copy(summoner.getEntityLook());
        super.setLife(summoner.getLife());
        super.setLifeMax(summoner.getMaxLife());
    }

    @Override
    public int tryDie(int casterId) {
        if (Arrays.stream(new Exception().getStackTrace()).anyMatch(Method -> Method.getMethodName().equalsIgnoreCase("joinFightTeam"))) { //Il viens de rejoindre la team rofl on le tue pas
            return -1;
        }
        this.setLife(0);

        this.fight.sendToField(new GameActionFightVanishMessage(1029, this.getSummonerID(), ID));

        if (this.fight.getActivableObjects().containsKey(this)) {
            this.fight.getActivableObjects().get(this).stream().forEach(y -> y.remove());
        }

        myCell.removeObject(this);

        if (!this.team.getAliveFighters().anyMatch(teamMate -> teamMate instanceof IllusionFighter && teamMate.getSummonerID() == this.getSummonerID())) {
            summoner.asPlayer().onCloneCleared();
        }

        if (this.fight.tryEndFight()) {
            return -3;
        }
        if (this.fight.getCurrentFighter() == this) {
            this.fight.setFightLoopState(Fight.FightLoopState.STATE_END_TURN);
        }
        return -2;
    }

    @Override
    public int getLevel() {
        return summoner.getLevel();
    }

    @Override
    public short getMapCell() {
        return 0;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightCharacterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, summoner.getPlayer().getNickName(), summoner.getPlayer().getPlayerStatus(), (byte) this.getLevel(), summoner.getPlayer().getActorAlignmentInformations(), summoner.getPlayer().getBreed(), summoner.getPlayer().hasSexe());
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, summoner.getPlayer().getNickName(), (byte) this.getLevel());
    }

    @Override
    public void send(Message Packet) {

    }

    @Override
    public void joinFight() {

    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

    @Override
    public List<SpellLevel> getSpells() {
        return null;
    }

}
