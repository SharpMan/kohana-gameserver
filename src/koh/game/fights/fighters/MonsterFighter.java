package koh.game.fights.fighters;

import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.actors.Player;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.IFightObject;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 *
 * @author Neo-Craft
 */
public class MonsterFighter extends VirtualFighter {

    @Getter @Setter protected MonsterGrade grade;
    @Getter @Setter private MonsterGroup monsterGroup;

    public MonsterFighter(Fight fight, MonsterGrade monster, int monsterGuid, MonsterGroup monsterGroup) {
        super(fight);
        this.monsterGroup = monsterGroup;
        this.grade = monster;
        super.initFighter(this.grade.getStats(), monsterGuid);
        this.entityLook = EntityLookParser.copy(this.grade.getMonster().getEntityLook());
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }

    @Override
    public void send(Message Packet) {

    }

    @Override
    public void endFight() {
        super.endFight();
    }



    @Override
    public short getMapCell() {
        return this.monsterGroup.getCell().getId();
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
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_FIGHTER;
    }


    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

    @Override
    public List<SpellLevel> getSpells() {
        return this.grade.getSpells();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }



    
}
