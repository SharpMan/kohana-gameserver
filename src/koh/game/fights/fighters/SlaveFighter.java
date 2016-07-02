package koh.game.fights.fighters;

import koh.game.entities.actors.Player;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AggressableStatusEnum;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.fight.SlaveSwitchContextMessage;
import koh.protocol.types.game.character.alignment.ActorExtendedAlignmentInformations;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.data.items.SpellItem;
import koh.protocol.types.game.look.EntityLook;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutSpell;

import java.util.List;

/**
 * Created by Melancholia on 6/20/16.
 */
public class SlaveFighter extends StaticFighter {
    //TODO intaclable

    private final SpellItem[] spellCuts;
    private final Shortcut[] shortcuts;

    public SlaveFighter(Fight fight, MonsterGrade monster, Fighter summoner) {
        super(fight, summoner, monster);
        super.initFighter(this.grade.getStats(), fight.getNextContextualId());
        this.entityLook = EntityLookParser.copy(this.grade.getMonster().getEntityLook());
        this.summoner = summoner;
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
        this.spellCuts = new SpellItem[this.getSpells().size()];
        this.shortcuts = new Shortcut[this.getSpells().size()];
        for (int i = 0; i < this.getSpells().size(); i++) {
            this.spellCuts[i] = new SpellItem((byte) (i + 63), this.getSpells().get(i).getSpellId(), this.getSpells().get(i).getGrade());
            this.shortcuts[i] = new ShortcutSpell((byte) (i), this.getSpells().get(i).getSpellId());
        }
    }


    public SlaveSwitchContextMessage getSwitchContextMessage(){
        return new SlaveSwitchContextMessage(summoner.getID(), this.getID(), spellCuts, getCharacteristics(), shortcuts);
    }

    @Override
    public int beginTurn() {
        final int r = super.beginTurn();
        if (r == -1) {
            this.summoner.send(getSwitchContextMessage());
        }
        return r;
    }

    @Override
    public int endTurn() {
        return this.tryDieSilencious(this.ID,true);
    }


    public CharacterCharacteristicsInformations getCharacteristics() {
        return new CharacterCharacteristicsInformations((double) 0, grade.getLevel(), (double) 0, 0, 0, 0, 0, getActorAlignmentExtendInformations(),
                getLife(), getMaxLife(), PlayerEnum.MAX_ENERGY, PlayerEnum.MAX_ENERGY,
                (short) this.getAP(), (short) this.getMP(),
                new CharacterBaseCharacteristic(this.getInitiative(true), 0, stats.getItem(StatsEnum.INITIATIVE), 0, 0), stats.getEffect(StatsEnum.PROSPECTING), stats.getEffect(StatsEnum.ACTION_POINTS),
                stats.getEffect(StatsEnum.MOVEMENT_POINTS), stats.getEffect(StatsEnum.STRENGTH), stats.getEffect(StatsEnum.VITALITY),
                stats.getEffect(StatsEnum.WISDOM), stats.getEffect(StatsEnum.CHANCE), stats.getEffect(StatsEnum.AGILITY),
                stats.getEffect(StatsEnum.INTELLIGENCE), stats.getEffect(StatsEnum.ADD_RANGE), stats.getEffect(StatsEnum.ADD_SUMMON_LIMIT),
                stats.getEffect(StatsEnum.DAMAGE_REFLECTION), stats.getEffect(StatsEnum.ADD_CRITICAL_HIT), (short) 0,
                stats.getEffect(StatsEnum.CRITICAL_MISS), stats.getEffect(StatsEnum.ADD_HEAL_BONUS), stats.getEffect(StatsEnum.ALL_DAMAGES_BONUS),
                stats.getEffect(StatsEnum.WEAPON_DAMAGES_BONUS_PERCENT), stats.getEffect(StatsEnum.ADD_DAMAGE_PERCENT), stats.getEffect(StatsEnum.TRAP_BONUS),
                stats.getEffect(StatsEnum.TRAP_DAMAGE_PERCENT), stats.getEffect(StatsEnum.GLYPH_BONUS_PERCENT), stats.getEffect(StatsEnum.PERMANENT_DAMAGE_PERCENT), stats.getEffect(StatsEnum.ADD_TACKLE_BLOCK),
                stats.getEffect(StatsEnum.ADD_TACKLE_EVADE), stats.getEffect(StatsEnum.ADD_RETRAIT_PA), stats.getEffect(StatsEnum.ADD_RETRAIT_PM), stats.getEffect(StatsEnum.ADD_PUSH_DAMAGES_BONUS),
                stats.getEffect(StatsEnum.ADD_CRITICAL_DAMAGES), stats.getEffect(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS), stats.getEffect(StatsEnum.ADD_EARTH_DAMAGES_BONUS),
                stats.getEffect(StatsEnum.ADD_WATER_DAMAGES_BONUS), stats.getEffect(StatsEnum.ADD_AIR_DAMAGES_BONUS), stats.getEffect(StatsEnum.ADD_FIRE_DAMAGES_BONUS),
                stats.getEffect(StatsEnum.DODGE_PA_LOST_PROBABILITY), stats.getEffect(StatsEnum.DODGE_PM_LOST_PROBABILITY), stats.getEffect(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT),
                stats.getEffect(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.AIR_ELEMENT_RESIST_PERCENT),
                stats.getEffect(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.EARTH_ELEMENT_REDUCTION),
                stats.getEffect(StatsEnum.WATER_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.AIR_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.FIRE_ELEMENT_REDUCTION),
                stats.getEffect(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), stats.getEffect(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), stats.getEffect(StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT),
                stats.getEffect(StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT),
                stats.getEffect(StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT), stats.getEffect(StatsEnum.PVP_NEUTRAL_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.PVP_EARTH_ELEMENT_REDUCTION),
                stats.getEffect(StatsEnum.PVP_WATER_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.PVP_AIR_ELEMENT_REDUCTION), stats.getEffect(StatsEnum.PVP_FIRE_ELEMENT_REDUCTION),
                new CharacterSpellModification[0], (short) 0);
    }


    public ActorExtendedAlignmentInformations getActorAlignmentExtendInformations() {
        return new ActorExtendedAlignmentInformations((byte) 0,
                (byte) 0,
                AggressableStatusEnum.NON_AGGRESSABLE,
                this.getCharacterPower(),
                0,
                0,
                0,
                (byte) 0);
    }

    public int getCharacterPower() {
        return Math.abs(this.ID) + this.grade.getLevel();
    }


    @Override
    public void joinFight() {

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
    public short getMapCell() {
        return 0;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightMonsterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, this.grade.getMonsterId(), this.grade.getGrade());
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberMonsterInformations(this.ID, this.grade.getMonsterId(), this.grade.getGrade());
    }

    @Override
    public List<SpellLevel> getSpells() {
        return this.grade.getSpells();
    }

    //TODO
    public int tryDieSilencious(int casterId, boolean force) {
        final int result = super.tryDie(casterId, force);
        if (result == -2 || result == -3) {
            if (this.grade.getMonster().isUseSummonSlot()) {
                this.summoner.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base++;
            }
        }
        return result;
    }


    @Override
    public int tryDie(int casterId, boolean force) {
        final int result = super.tryDie(casterId, force);
        if (result == -2 || result == -3) {
            if (this.grade.getMonster().isUseSummonSlot()) {
                this.summoner.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base++;
                if (summoner instanceof CharacterFighter) {
                    summoner.send(summoner.asPlayer().getCharacterStatsListMessagePacket());
                }
            }
            //5435
        }
        return result;
    }


    @Override
    public void send(Message Packet) {

    }

    @Override
    public int getMaxAP() {
        return this.stats.getTotal(StatsEnum.ACTION_POINTS);
    }

    @Override
    public int getMaxMP() {
        return this.stats.getTotal(StatsEnum.MOVEMENT_POINTS);
    }

    @Override
    public int getAP() {
        return this.stats.getTotal(StatsEnum.ACTION_POINTS,false) - this.usedAP;
    }

    @Override
    public int getMP() {
        return this.stats.getTotal(StatsEnum.MOVEMENT_POINTS,false) - this.usedMP;
    }


}
