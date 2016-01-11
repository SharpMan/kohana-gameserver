package koh.game.fights.fighters;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.fights.*;
import koh.game.network.WorldClient;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightVanishMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.stats.FighterStatsListMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.roleplay.CurrentMapMessage;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.*;
import koh.protocol.types.game.context.roleplay.HumanOptionEmote;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;

/**
 * @author Neo-Craft
 */
public class CharacterFighter extends Fighter {

    public int fakeContextualId = -1000;
    @Getter
    private Player character;

    public CharacterFighter(Fight Fight, WorldClient client) {
        super(Fight, null);
        this.turnReady = false;
        this.character = client.getCharacter();

        this.character.setFight(Fight);
        this.character.setFighter(this);
        this.character.stopRegen();
        this.character.removeHumanOption(HumanOptionEmote.class);
        //this.character.currentMap.unregisterPlayer(character);
        this.fight.registerPlayer(character);
        super.initFighter(this.character.getStats(), this.character.getID());
        super.setLife(this.character.getLife());
        super.setLifeMax(this.character.getMaxLife());
        if (super.getLife() == 0) {
            super.setLife(1);
        }
        this.entityLook = EntityLookParser.Copy(this.character.getEntityLook());
    }

    @Override
    public GameFightMinimalStats getGameFightMinimalStats(Player character) {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.getLife(), this.getMaxLife(), this.character.getMaxLife(), this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT), this.shieldPoints, this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE), character == null ? this.visibleState.value : this.getVisibleStateFor(character), this.getInitiative(false));
        }
        return new GameFightMinimalStats(this.getLife(), this.getMaxLife(), this.character.getMaxLife(), this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT), this.shieldPoints, this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT), this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION), this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION), this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE), character == null ? this.visibleState.value : this.getVisibleStateFor(character));
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightCharacterInformations(((fakeContextualId != -1000 && !this.isMyFriend(character)) ? this.fakeContextualId : this.ID), this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, this.character.getNickName(), this.character.getPlayerStatus(), (byte) this.getLevel(), this.character.getActorAlignmentInformations(), this.character.getBreed(), this.character.hasSexe());
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, this.character.getNickName(), (byte) this.character.getLevel());
    }

    @Override
    public void joinFight() {
        this.character.destroyFromMap();
    }

    @Override
    public void middleTurn() {
        /*if (this.character.client != null) {
         this.character.client.send(this.FighterStatsListMessagePacket());
         }*/
        super.middleTurn();
    }

    @Override
    public int endTurn() {
        if (this.character.getClient() == null) {
            this.fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 162, new String[]{this.character.getNickName(), Integer.toString(this.turnRunning)}));
            this.turnRunning--;
        }
        return super.endTurn();
    }

    public void CleanClone() {
        boolean updated = false;
        for (Fighter Clone : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.getSummoner() == this)::iterator) {
            Clone.tryDie(this.ID);
            updated = true;
        }
        if (updated) {
            this.onCloneCleared();
        }
    }

    public void onCloneCleared() {
        this.fight.observers.stream().filter(x -> !this.isMyFriend(((Player) x))).forEach(o -> ((Player) o).send(new GameActionFightVanishMessage(1029, this.ID, fakeContextualId)));
        this.fakeContextualId = -1000;
        this.buff.dispell(2763);

        this.send(this.FighterStatsListMessagePacket());
    }

    @Override
    public int beginTurn() {
        this.CleanClone();
        if (this.character.getClient() == null && this.turnRunning <= 0) {
            return this.tryDie(this.ID, true);
        }
        return super.beginTurn();
    }

    @Override
    public int tryDie(int casterId, boolean Force) {

        this.CleanClone();
        return super.tryDie(casterId, Force);
    }

    @Override
    public void leaveFight() {
        super.leaveFight();
        this.endFight();
    }

    @Override
    public int getLevel() {
        return this.character.getLevel();
    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

    @Override
    public void endFight() {
        try {
            if (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE) {
                if (super.getLife() <= 0) {
                    this.character.setLife(1);
                } else if (this.character.computeLife(50) > super.getLife()) {
                    this.character.setLife(this.character.computeLife(50));
                } else {
                    this.character.setLife(super.getLife());
                }
            }

            this.fight.unregisterPlayer(character);

            if (this.character.isInWorld()) {
                this.character.getClient().endGameAction(GameActionTypeEnum.FIGHT);
                this.character.send(new GameContextDestroyMessage());
                this.character.send(new GameContextCreateMessage((byte) 1));
                this.character.setFight(null);
                this.character.refreshStats(false, true);
                if (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE && this.team.id == this.fight.getLoosers().id) {
                    this.character.teleport(this.character.getSavedMap(), this.character.getSavedCell());
                } else {
                    this.character.send(new CurrentMapMessage(this.character.getCurrentMap().getId(), "649ae451ca33ec53bbcbcc33becf15f4"));
                    //this.character.send(new BasicTimeMessage((double) (new Date().getTime()), 0));
                    this.character.getCurrentMap().spawnActor(this.character);
                }
            } else {
                if (this.isLeft() || (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE && this.team.id == this.fight.getLoosers().id)) {
                    this.character.offlineTeleport(this.character.getSavedMap(), this.character.getSavedCell());
                }
            }
            this.character.setFight(null);
            this.character.setFighter(null);
        }catch(Exception e){
            e.printStackTrace();
            this.fight.getLogger().error(e);
            this.fight.getLogger().warn(e.getMessage());
        }

    }

    @Override
    public short getMapCell() {
        return this.character.getCell().getId();
    }

    @Override
    public void send(Message packet) {
        this.character.send(packet);
    }

    public FighterStatsListMessage FighterStatsListMessagePacket() {
        return new FighterStatsListMessage(new CharacterCharacteristicsInformations((double) character.getExperience(), (double) DAO.getExps().getPlayerMinExp(character.getLevel()), (double) DAO.getExps().getPlayerMaxExp(character.getLevel()), character.getKamas(), character.getStatPoints(), 0, character.getSpellPoints(), character.getActorAlignmentExtendInformations(),
                getLife(), getMaxLife(), character.getEnergy(), PlayerEnum.MAX_ENERGY,
                (short) this.getAP(), (short) this.getMP(),
                new CharacterBaseCharacteristic(this.getInitiative(true), 0, stats.getItem(StatsEnum.INITIATIVE), 0, 0), stats.getEffect(StatsEnum.PROSPECTING), stats.getEffect(StatsEnum.ACTION_POINTS),
                stats.getEffect(StatsEnum.MOVEMENT_POINTS), stats.getEffect(StatsEnum.STRENGTH), stats.getEffect(StatsEnum.VITALITY),
                stats.getEffect(StatsEnum.WISDOM), stats.getEffect(StatsEnum.CHANCE), stats.getEffect(StatsEnum.AGILITY),
                stats.getEffect(StatsEnum.INTELLIGENCE), stats.getEffect(StatsEnum.ADD_RANGE), stats.getEffect(StatsEnum.ADD_SUMMON_LIMIT),
                stats.getEffect(StatsEnum.DamageReflection), stats.getEffect(StatsEnum.ADD_CRITICAL_HIT), character.getInventoryCache().weaponCriticalHit(),
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
                new CharacterSpellModification[0], (short) 0));
    }

    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

    @Override
    public int getInitiative(boolean base) {
        return this.character.getInitiative(base);
    }

}
