package koh.game.fights.fighters;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.IFightObject;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.*;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Created by Melancholia on 12/10/16.
 */
public class TaxCollectorFighter extends VirtualFighter {

    @Getter
    @Setter
    protected TaxCollector grade;

    public TaxCollectorFighter(Fight fight, TaxCollector monster) {
        super(fight);
        this.grade = monster;
        this.stats = new GenericStats();
        this.stats.addBase(StatsEnum.INTELLIGENCE, getLevel());
        this.stats.addBase(StatsEnum.INITIATIVE, getLevel());
        this.stats.addBase(StatsEnum.STRENGTH, getLevel());
        this.stats.addBase(StatsEnum.CHANCE, getLevel());
        this.stats.addBase(StatsEnum.AGILITY, getLevel());
        this.stats.addBase(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.WATER_ELEMENT_RESIST_PERCENT, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.AIR_ELEMENT_RESIST_PERCENT, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.DODGE_PA_LOST_PROBABILITY, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.DODGE_PM_LOST_PROBABILITY, (int) Math.floor(getLevel() / 2));
        this.stats.addBase(StatsEnum.ADD_TACKLE_BLOCK, 50);
        this.stats.addBase(StatsEnum.ADD_TACKLE_EVADE, 50);
        this.stats.addBase(StatsEnum.ACTION_POINTS, 6);
        this.stats.addBase(StatsEnum.MOVEMENT_POINTS, 5);
        this.stats.addBase(StatsEnum.WISDOM, monster.getGuild().getEntity().wisdom);
        this.stats.addBase(StatsEnum.PROSPECTING, monster.getGuild().getEntity().prospecting);


        this.ID = fight.getNextContextualId();
        this.entityLook = EntityLookParser.copy(this.grade.getEntityLook());
        super.setLife(monster.getTaxCollectorHealth());
        super.setLifeMax(monster.getTaxCollectorHealth());
    }

    @Override
    public void send(Message Packet) {

    }

    @Override
    public void endFight() {
        super.endFight();
    }

    @Override
    public GameFightFighterLightInformations getGameFightFighterLightInformations() {
        return new GameFightFighterTaxCollectorLightInformations(getID(), wave, getLevel(), (byte) 0, false, isAlive(), grade.getFirstName(),grade.getLastName());
    }


    @Override
    public short getMapCell() {
        return this.grade.getCellID();
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberTaxCollectorInformations(this.ID, this.grade.getFirstName(),grade.getLastName(), (byte) grade.getLevel(),grade.getGuild().getEntity().guildID, grade.getIden());
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
        return new GameFightTaxCollectorInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, this.grade.getFirstName(), this.grade.getLastName(),(byte) grade.getLevel());
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
