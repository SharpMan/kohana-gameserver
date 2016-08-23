package koh.game.fights.fighters;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.PlayerInst;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.kolissium.KolizeumExecutor;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.*;
import koh.game.fights.types.AgressionFight;
import koh.game.network.WorldClient;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightVanishMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.stats.CharacterStatsListMessage;
import koh.protocol.messages.game.character.stats.FighterStatsListMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.roleplay.CurrentMapMessage;
import koh.protocol.messages.game.context.roleplay.fight.arena.GameRolePlayArenaUpdatePlayerInfosMessage;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.*;
import koh.protocol.types.game.context.roleplay.HumanOptionEmote;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

import static koh.protocol.client.enums.StatsEnum.CAST_SPELL_ON_CRITICAL_HIT;

/**
 * @author Neo-Craft
 */
public class CharacterFighter extends Fighter {

    public int fakeContextualId = -1000;
    @Getter
    private Player character;
    @Getter
    @Setter
    private boolean needStatRefresh = false;

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

        this.stats.getEffect(StatsEnum.ACTION_POINTS).objectsAndMountBonus = Math.min(getLevel() >= 100 ? 5 : 6,this.stats.getEffect(StatsEnum.ACTION_POINTS).objectsAndMountBonus);
        this.stats.getEffect(StatsEnum.MOVEMENT_POINTS).objectsAndMountBonus = Math.min(3,this.stats.getEffect(StatsEnum.MOVEMENT_POINTS).objectsAndMountBonus);
        this.entityLook = EntityLookParser.copy(this.character.getEntityLook());
    }

    @Override
    public void computeReducedDamage(StatsEnum effect, MutableInt damages, boolean isCritical) {
        switch (effect) {
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
            case LIFE_LEFT_TO_THE_ATTACKER_NEUTRAL_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_EARTH:
            case STEAL_EARTH:
            case LIFE_LEFT_TO_THE_ATTACKER_EARTH_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_FIRE:
            case STEAL_FIRE:
            case LIFE_LEFT_TO_THE_ATTACKER_FIRE_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_AIR:
            case STEAL_AIR:
            case LIFE_LEFT_TO_THE_ATTACKER_AIR_DAMAGES:
            case PA_USED_LOST_X_PDV:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;

            case DAMAGE_WATER:
            case STEAL_WATER:
            case LIFE_LEFT_TO_THE_ATTACKER_WATER_DAMAGES:
                damages.setValue(damages.intValue() * (100 - Math.min((this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT) + (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT) : 0)), 50)) / 100
                        - this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_REDUCTION) : 0) - this.stats.getTotal(StatsEnum.ADD_MAGIC_REDUCTION));
                break;
        }
        if (isCritical) {
            damages.subtract(this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION));
        }
    }

    @Override
    public GameFightMinimalStats getGameFightMinimalStats(Player character) {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.getLife(),
                    this.getMaxLife(),
                    this.character.getMaxLife(),
                    this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT),
                    this.shieldPoints,
                    this.getAP(), this.getMaxAP(),
                    this.getMP(), this.getMaxMP(),
                    getSummonerID(), getSummonerID() != 0,
                    (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT) : 0) + this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT),
                    (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT) : 0) + this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT),
                    (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT) : 0) + this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT),
                    (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT) : 0) + this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT),
                    (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT) : 0) + this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT),
                    (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_NEUTRAL_ELEMENT_REDUCTION) : 0) + this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION),
                    (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_EARTH_ELEMENT_REDUCTION) : 0) + this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION),
                    (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_WATER_ELEMENT_REDUCTION) : 0) + this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION),
                    (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_AIR_ELEMENT_REDUCTION) : 0) + this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION),
                    (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PVP_FIRE_ELEMENT_REDUCTION) : 0) + this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION),
                    this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION),
                    this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION),
                    Math.max(this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), 0),
                    Math.max(this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), 0),
                    this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE),
                    character == null ? this.visibleState.value : this.getVisibleStateFor(character),
                    this.getInitiative(false));
        }
        return new GameFightMinimalStats(this.getLife(),
                this.getMaxLife(),
                this.character.getMaxLife(),
                this.stats.getTotal(StatsEnum.PERMANENT_DAMAGE_PERCENT),
                this.shieldPoints,
                this.getAP(), this.getMaxAP(),
                this.getMP(), this.getMaxMP(),
                getSummonerID(), getSummonerID() != 0,
                this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT),
                this.stats.getTotal(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT),
                this.stats.getTotal(StatsEnum.WATER_ELEMENT_RESIST_PERCENT),
                this.stats.getTotal(StatsEnum.AIR_ELEMENT_RESIST_PERCENT),
                this.stats.getTotal(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT),
                this.stats.getTotal(StatsEnum.NEUTRAL_ELEMENT_REDUCTION),
                this.stats.getTotal(StatsEnum.EARTH_ELEMENT_REDUCTION),
                this.stats.getTotal(StatsEnum.WATER_ELEMENT_REDUCTION),
                this.stats.getTotal(StatsEnum.AIR_ELEMENT_REDUCTION),
                this.stats.getTotal(StatsEnum.FIRE_ELEMENT_REDUCTION),
                this.stats.getTotal(StatsEnum.ADD_PUSH_DAMAGES_REDUCTION),
                this.stats.getTotal(StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION),
                Math.max(this.stats.getTotal(StatsEnum.DODGE_PA_LOST_PROBABILITY), 0),
                Math.max(this.stats.getTotal(StatsEnum.DODGE_PM_LOST_PROBABILITY), 0),
                this.stats.getTotal(StatsEnum.ADD_TACKLE_BLOCK), this.stats.getTotal(StatsEnum.ADD_TACKLE_EVADE),
                character == null ? this.visibleState.value : this.getVisibleStateFor(character));

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
         this.character.client.send(this.getFighterStatsListMessagePacket());
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

    public void cleanClone() {
        boolean updated = false;
        for (final Fighter clone : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.getSummoner() == this)::iterator) {
            clone.tryDie(this.ID);
            updated = true;
        }
        if (updated) {
            this.onCloneDisposed();
        }
    }


    @Override
    public boolean isTurnReady(){
        if(character.getClient() == null && fight.getFightState() == FightState.STATE_ACTIVE){
            return true;
        }
        return turnReady;
    }


    public void onCloneDisposed() {
        this.fight.observers.stream().filter(x -> !this.isMyFriend(((Player) x))).forEach(o -> ((Player) o).send(new GameActionFightVanishMessage(1029, this.ID, fakeContextualId)));
        this.fakeContextualId = -1000;
        this.buff.dispell(2763);

        this.send(this.getFighterStatsListMessagePacket());
    }

    @Override
    public int beginTurn() {
        if (this.needStatRefresh) {
            //this.send(this.getCharacterStatsListMessagePacket());
            this.needStatRefresh = false;
        }
        this.cleanClone();
        if (this.character.getClient() == null && this.turnRunning <= 0) {
            return super.tryDie(this.ID, true);
        }
        character.getInventoryCache().getEffects(CAST_SPELL_ON_CRITICAL_HIT.value())
                //filter veuillez
                .forEach(list -> {

                    list.filter(e -> e.diceNum == 5453 || e.diceNum == 5454)
                            .forEach(effect -> {
                                fight.launchSpell(this, DAO.getSpells().findSpell(effect.diceNum).getSpellLevel(effect.diceSide), this.getCellId(), true, true, true, -1);
                            });
                });
        /*character.getInventoryCache().getEffects(CAST_SPELL_ON_CRITICAL_HIT.value())
                //filter veuillez
                .forEach(list -> {

                    list.filter(e -> e.diceNum == 5454)
                            .findFirst()
                            .ifPresent(effect -> {
                                        fight.launchSpell(this, DAO.getSpells().findSpell(effect.diceNum).getSpellLevel((fight.getFightWorker().round %2 == 0) ? 3 : 4), this.getCellId(), true, true, true, -1);
                                    }
                            );
                });*/
        return super.beginTurn();
    }

    @Override
    public int tryDie(int casterId, boolean force) {

        this.cleanClone();
        return super.tryDie(casterId, force);
    }

    @Override
    public void leaveFight() {
        super.leaveFight();
        // if (this.fight.getFightState() != FightState.STATE_PLACE) {

        //}
        this.endFight();
    }

    @Override
    public GameFightFighterLightInformations getGameFightFighterLightInformations() {
        return new GameFightFighterNamedLightInformations(this.getID(), wave, getLevel(), character.getBreed(), character.hasSexe(), isAlive(), character.getNickName());
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
    public List<SpellLevel> getSpells() {
        return this.character.getMySpells().getSpells();
    }

    @Override
    public void endFight() {
        try {
            if (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE && fight.getFightType() != FightTypeEnum.FIGHT_TYPE_PVP_ARENA) {
                if (super.getLife() <= 0) {
                    this.character.setLife(1);
                } else if (this.character.computeLife(50) > super.getLife()) {
                    this.character.setLife(this.character.computeLife(50));
                } else {
                    this.character.setLife(super.getLife());
                }
            }

            this.fight.unregisterPlayer(character);

            if (this.character.getFight() != this.fight)
                return;

                if (character.getClient() != null)
                    this.character.getClient().endGameAction(GameActionTypeEnum.FIGHT);
                this.character.send(new GameContextDestroyMessage());
                this.character.send(new GameContextCreateMessage((byte) 1));
                this.character.setFight(null);
                this.character.refreshStats(false, true);
                if (this.isLeft()) {
                    this.character.fightTeleportation(this.character.getSavedMap(), this.character.getSavedCell());
                }
                else if (fight.getFightType() == FightTypeEnum.FIGHT_TYPE_PVP_ARENA) {
                    KolizeumExecutor.teleportLastPosition(this.character);
                    final PlayerInst inst = PlayerInst.getPlayerInst(character.getID());
                    this.character.send(new GameRolePlayArenaUpdatePlayerInfosMessage(character.getKolizeumRate().getScreenRating(), inst.getDailyCote(), character.getScores().get(ScoreType.BEST_COTE), inst.getDailyWins(), inst.getDailyFight()));

                } else if (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE
                        && this.team.id == this.fight.getLoosers().id
                        && this.character.getSavedMap() != this.character.getCurrentMap().getId()) {
                    this.character.fightTeleportation(this.character.getSavedMap(), this.character.getSavedCell());
                }
                else if(fight.getFightType() == FightTypeEnum.FIGHT_TYPE_PvM
                        && this.team.id == this.fight.getWinners().id
                        && fight.getMap().getFightActions() != null){
                    //FIXME: If maps has others actions than teleport, then edit this scope
                    fight.getMap().getFightActions().forEach(action -> {
                        action.execute(this.character);
                    });
                }
                else if(character.getClient() != null) {
                    this.character.send(new CurrentMapMessage(this.character.getCurrentMap().getId(), "649ae451ca33ec53bbcbcc33becf15f4"));
                    //this.character.send(new BasicTimeMessage((double) (new Date().getTime()), 0));
                    this.character.getCurrentMap().spawnActor(this.character);
                }


        } catch (Exception e) {
            e.printStackTrace();
            this.fight.getLogger().error(e);
            this.fight.getLogger().warn(e.getMessage());
        } finally {
            this.character.setFight(null);
            this.character.setFighter(null);
            this.character.getFightsRegistred().add(this.fight);
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

    public FighterStatsListMessage getFighterStatsListMessagePacket() {
        return new FighterStatsListMessage(new CharacterCharacteristicsInformations((double) character.getExperience(), (double) DAO.getExps().getPlayerMinExp(character.getLevel()), (double) DAO.getExps().getPlayerMaxExp(character.getLevel()), character.getKamas(), character.getStatPoints(), 0, character.getSpellPoints(), character.getActorAlignmentExtendInformations(),
                getLife(), getMaxLife(), character.getEnergy(), PlayerEnum.MAX_ENERGY,
                (short) this.getAP(), (short) this.getMP(),
                new CharacterBaseCharacteristic(this.getInitiative(true), 0, stats.getItem(StatsEnum.INITIATIVE), 0, 0), stats.getEffect(StatsEnum.PROSPECTING), stats.getEffect(StatsEnum.ACTION_POINTS),
                stats.getEffect(StatsEnum.MOVEMENT_POINTS), stats.getEffect(StatsEnum.STRENGTH), stats.getEffect(StatsEnum.VITALITY),
                stats.getEffect(StatsEnum.WISDOM), stats.getEffect(StatsEnum.CHANCE), stats.getEffect(StatsEnum.AGILITY),
                stats.getEffect(StatsEnum.INTELLIGENCE), stats.getEffect(StatsEnum.ADD_RANGE), stats.getEffect(StatsEnum.ADD_SUMMON_LIMIT),
                stats.getEffect(StatsEnum.DAMAGE_REFLECTION), stats.getEffect(StatsEnum.ADD_CRITICAL_HIT), character.getInventoryCache().weaponCriticalHit(),
                stats.getEffect(StatsEnum.CRITICAL_MISS), stats.getEffect(StatsEnum.ADD_HEAL_BONUS), stats.getEffect(StatsEnum.ALL_DAMAGES_BONUS),
                stats.getEffect(StatsEnum.WEAPON_DAMAGES_BONUS_PERCENT), stats.getEffect(StatsEnum.ADD_DAMAGE_PERCENT), stats.getEffect(StatsEnum.TRAP_BONUS),
                stats.getEffect(StatsEnum.TRAP_DAMAGE_PERCENT), stats.getEffect(StatsEnum.GLYPH_BONUS_PERCENT), stats.getEffect(StatsEnum.PERMANENT_DAMAGE_PERCENT), stats.getEffect(StatsEnum.ADD_TACKLE_BLOCK),
                new CharacterBaseCharacteristic(stats.getBase(StatsEnum.ADD_TACKLE_EVADE) + (stats.getBase(StatsEnum.WISDOM) / 4), stats.getBoost(StatsEnum.ADD_TACKLE_EVADE) + (stats.getBoost(StatsEnum.WISDOM) / 4), stats.getItem(StatsEnum.ADD_TACKLE_EVADE) + (stats.getItem(StatsEnum.WISDOM) / 4),0,0)/*stats.getEffect(StatsEnum.ADD_TACKLE_EVADE)*/, stats.getEffect(StatsEnum.ADD_RETRAIT_PA), stats.getEffect(StatsEnum.ADD_RETRAIT_PM), stats.getEffect(StatsEnum.ADD_PUSH_DAMAGES_BONUS),
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

    public CharacterStatsListMessage getCharacterStatsListMessagePacket() {
        return new CharacterStatsListMessage(new CharacterCharacteristicsInformations((double) character.getExperience(), (double) DAO.getExps().getPlayerMinExp(character.getLevel()), (double) DAO.getExps().getPlayerMaxExp(character.getLevel()), character.getKamas(), character.getStatPoints(), 0, character.getSpellPoints(), character.getActorAlignmentExtendInformations(),
                getLife(), getMaxLife(), character.getEnergy(), PlayerEnum.MAX_ENERGY,
                (short) this.getAP(), (short) this.getMP(),
                new CharacterBaseCharacteristic(this.getInitiative(true), 0, stats.getItem(StatsEnum.INITIATIVE), 0, 0), stats.getEffect(StatsEnum.PROSPECTING), stats.getEffect(StatsEnum.ACTION_POINTS),
                stats.getEffect(StatsEnum.MOVEMENT_POINTS), stats.getEffect(StatsEnum.STRENGTH), stats.getEffect(StatsEnum.VITALITY),
                stats.getEffect(StatsEnum.WISDOM), stats.getEffect(StatsEnum.CHANCE), stats.getEffect(StatsEnum.AGILITY),
                stats.getEffect(StatsEnum.INTELLIGENCE), new CharacterBaseCharacteristic(stats.getBase(StatsEnum.ADD_RANGE) + stats.getBoost(StatsEnum.ADD_RANGE), 0, stats.getItem(StatsEnum.ADD_RANGE), 0, 0), stats.getEffect(StatsEnum.ADD_SUMMON_LIMIT),
                stats.getEffect(StatsEnum.DAMAGE_REFLECTION), stats.getEffect(StatsEnum.ADD_CRITICAL_HIT), character.getInventoryCache().weaponCriticalHit(),
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
