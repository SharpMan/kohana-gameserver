package koh.game.fights.fighters;

import koh.game.entities.actors.Player;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.look.EntityLook;

import java.util.List;

/**
 * Created by Melancholia on 1/29/16.
 */
public class StaticSummonedFighter extends StaticFighter {

    public StaticSummonedFighter(Fight fight, Fighter summoner, MonsterGrade monster) {
        super(fight, summoner,monster);
        super.initFighter(this.grade.getStats(), fight.getNextContextualId());
        this.entityLook = EntityLookParser.copy(this.grade.getMonster().getEntityLook());
        this.adjustStats();
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }

    @Override
    public short getMapCell() {
        return 0;
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberMonsterInformations(this.ID, this.grade.getMonsterId(), this.grade.getGrade());
    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

    @Override
    public int getLevel() {
        return this.grade.getLevel();
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightMonsterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, this.grade.getMonsterId(), this.grade.getGrade());
    }

    @Override
    public void send(Message Packet) {

    }

    @Override
    public void joinFight() {

    }


    @Override
    public List<SpellLevel> getSpells() {
        return null;
    }
}
