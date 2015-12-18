package koh.game.fights.fighters;

import java.util.Arrays;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
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

    public IllusionFighter(koh.game.fights.Fight Fight, Fighter Summoner) {
        super(Fight, Summoner);
        this.stats = new GenericStats();
        this.stats.merge(Summoner.stats);
        super.initFighter(this.stats, Fight.getNextContextualId());
        this.entityLook = EntityLookParser.Copy(Summoner.getEntityLook());
        super.setLife(Summoner.getLife());
        super.setLifeMax(Summoner.getMaxLife());
    }

    @Override
    public int tryDie(int casterId) {
        if (Arrays.stream(new Exception().getStackTrace()).anyMatch(Method -> Method.getMethodName().equalsIgnoreCase("joinFightTeam"))) { //Il viens de rejoindre la team rofl on le tue pas
            return -1;
        }
        this.setLife(0);

        this.fight.sendToField(new GameActionFightVanishMessage(1029, this.getSummonerID(), ID));

        if (this.fight.m_activableObjects.containsKey(this)) {
            this.fight.m_activableObjects.get(this).stream().forEach(y -> y.remove());
        }

        myCell.RemoveObject(this);

        if (!this.team.getAliveFighters().anyMatch(x -> x instanceof IllusionFighter && x.getSummonerID() == this.getSummonerID())) {
            ((CharacterFighter) this.summoner).onCloneCleared();
        }

        if (this.fight.tryEndFight()) {
            return -3;
        }
        if (this.fight.currentFighter == this) {
            this.fight.fightLoopState = fight.fightLoopState.STATE_END_TURN;
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
        return new GameFightCharacterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.Id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, ((CharacterFighter) this.summoner).character.getNickName(), ((CharacterFighter) this.summoner).character.getPlayerStatus(), (byte) this.getLevel(), ((CharacterFighter) this.summoner).character.getActorAlignmentInformations(), ((CharacterFighter) this.summoner).character.getBreed(), ((CharacterFighter) this.summoner).character.hasSexe());
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, ((CharacterFighter) this.summoner).character.getNickName(), (byte) this.getLevel());
    }

    @Override
    public void send(Message Packet) {

    }

    @Override
    public void JoinFight() {

    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

}
