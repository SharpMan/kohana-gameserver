package koh.game.fights.fighters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectActivableObject;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layers.FightBomb;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.look.EntityLook;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class BombFighter extends StaticFighter {


    private static final Logger logger = LogManager.getLogger(BombFighter.class);

    public BombFighter(Fight fight, Fighter summoner, MonsterGrade monster) {
        super(fight, summoner,monster);
        super.initFighter(this.grade.getStats(), fight.getNextContextualId());
        this.entityLook = EntityLookParser.copy(this.grade.getMonster().getEntityLook());
        this.adjustStats();
        this.stats.merge(this.summoner.getStats(),StatsEnum.VITALITY);
        this.stats.addBoost(StatsEnum.VITALITY, -this.summoner.getStats().getBoost(StatsEnum.VITALITY));
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }


    @Override
    public void adjustStats() {
        this.stats.addBase(StatsEnum.VITALITY, (short) Math.max(((double) this.summoner.getStats().getTotal(StatsEnum.VITALITY)) * (0.2f),0));
        this.stats.addBase(StatsEnum.INTELLIGENCE, (short) ((double) this.stats.getEffect(StatsEnum.INTELLIGENCE).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.CHANCE, (short) ((double) this.stats.getEffect(StatsEnum.CHANCE).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.STRENGTH, (short) ((double) this.stats.getEffect(StatsEnum.STRENGTH).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.AGILITY, (short) ((double) this.stats.getEffect(StatsEnum.AGILITY).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_AIR, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_EARTH, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_FIRE, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_EARTH, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_NEUTRAL, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.DAMAGE_WATER, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.ADD_DAMAGE_PHYSIC, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));
        this.stats.addBase(StatsEnum.ALL_DAMAGES_BONUS, (short) ((double) this.stats.getEffect(StatsEnum.WISDOM).base * (1.0 + (double) this.summoner.getLevel() / 100.0)));

    }

    /*@Override
    public void calculBonusDamages(EffectInstanceDice effect, MutableInt jet, short castCell, short targetCell, short truedCell) {

        //double bonus = 0;

        double bonus = getShapeEfficiency(effect.zoneShape(), castCell, targetCell, effect.zoneSize() != -100000 ? effect.zoneSize() : EFFECTSHAPE_DEFAULT_AREA_SIZE, effect.zoneMinSize() != -100000 ? effect.zoneMinSize() : EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE, effect.zoneEfficiencyPercent() != -100000 ? effect.zoneEfficiencyPercent() : EFFECTSHAPE_DEFAULT_EFFICIENCY, effect.zoneMaxEfficiency() != -100000 ? effect.zoneMaxEfficiency() : EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY);

        //bonus *= getPortalsSpellEfficiencyBonus(truedCell, this.fight);

        bonus += this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 200;

        System.out.println("Final bonus "+bonus);

        jet.setValue((jet.floatValue() * bonus));
    }*/

    @Override
    public void computeDamages(StatsEnum effect, MutableInt jet) {
        //System.out.println(this.stats.getTotal(StatsEnum.COMBO_DAMMAGES));
        switch (effect) {
            case DAMAGE_EARTH:
            case STEAL_EARTH:
                jet.setValue(Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)) / 100 + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_EARTH_DAMAGES_BONUS)) * (1 + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 100));
                break;
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
                jet.setValue(Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS)) * (1 + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 100));
                break;

            case DAMAGE_FIRE:
            case STEAL_FIRE:
                jet.setValue( Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.INTELLIGENCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_FIRE_DAMAGES_BONUS)) * (1 + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 100));
                break;

            case DAMAGE_AIR:
            case STEAL_AIR:
                jet.setValue( Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.AGILITY) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_AIR_DAMAGES_BONUS)) * (1 + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 100) );
                break;

            case DAMAGE_WATER:
            case STEAL_WATER:
                jet.setValue( Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.CHANCE) + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PERCENT)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_WATER_DAMAGES_BONUS)) * (1 + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES) / 100) );
                break;
        }
    }

    @Override
    public List<SpellLevel> getSpells() {
        return null;
    }

    public boolean boosted = false;

    public synchronized int selfMurder(int caster) {
        if (boosted) {
            return -1;
        }
        int totalCombo = 0;
        final ArrayList<BombFighter> targets = new ArrayList<>(6);
        for (short aCell : (new Zone(SpellShapeEnum.C, (byte) 2, MapPoint.fromCellId(this.getCellId()).advancedOrientationTo(MapPoint.fromCellId(this.getCellId()), true), this.fight.getMap())).getCells(this.getCellId())) {
            FightCell fightCell = fight.getCell(aCell);
            if (fightCell != null) {
                if (fightCell.hasFighter()) {
                    for (Fighter target : fightCell.getObjectsAsFighter()) {
                        if (target.getID() == this.ID) {
                            continue;
                        }
                        if (target instanceof BombFighter /*&& ((BombFighter) target).grade.monsterId == this.grade.monsterId*/ && ((BombFighter) target).summoner == this.summoner && !((BombFighter) target).boosted) {
                            targets.add((BombFighter) target);
                            totalCombo += 40;
                        }
                    }
                }
            }
        }
        if (totalCombo == 0) {
            return -1;
        }
        fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + totalCombo + "% dommages d'explosion"}));
        stats.addBase(StatsEnum.COMBO_DAMMAGES, totalCombo);
        this.boosted = true;

        for (Fighter bomb : targets) {
            bomb.getStats().addBase(StatsEnum.COMBO_DAMMAGES, totalCombo);
            boosted = true;
        }
        int bestValue = -1;
        for (BombFighter bomb : targets) {
            final int result = bomb.tryDie(caster, true);
            if(result < bestValue){
                bestValue = result;
            }

        }
        targets.clear();
        return bestValue;
    }

    @Override
    public int tryDie(int casterId, boolean force) {
        if (this.getLife() <= 0 && !force) {
            if (this.buff.getAllBuffs().anyMatch(x -> x.activeType == BuffActiveType.ACTIVE_ON_DIE)) {
                return this.buff.getAllBuffs().filter(x -> x.activeType == BuffActiveType.ACTIVE_ON_DIE).findFirst().get().applyEffect(null, null);
            } else {
                if (this.fightBombs != null) {
                    this.fightBombs.forEach(Bomb -> Bomb.remove());
                }
                return super.tryDie(casterId, force);
            }
        }
        if (this.getLife() <= 0 || force) {
            final int result = selfMurder(casterId);
            fight.launchSpell(this, DAO.getSpells().findSpell(DAO.getSpells().findBomb(this.grade.getMonsterId()).explodSpellId).getSpellLevel(this.grade.getGrade()), this.getCellId(), true, true, false,-1);
            if (this.fightBombs != null) {
                this.fightBombs.forEach(Bomb -> Bomb.remove());
            }
            final int result2 = super.tryDie(casterId, force);
            return result < result2 ? result : result2;
        }
        return super.tryDie(casterId, force);
    }

    public ArrayList<FightBomb> fightBombs;

    public void addBomb(FightBomb Bomb) {
        if (fightBombs == null) {
            fightBombs = new ArrayList<>(4);
        }
        this.fightBombs.add(Bomb);
    }

    @Override
    public int onCellChanged() {
        if (this.myCell != null) {
            if (this.isDead()) {
                return -2;
            }
            if (this.fightBombs != null) {
                this.fightBombs.forEach(Bomb -> Bomb.remove());
            }
            if (this.myCell.hasGameObject(FightObjectType.OBJECT_BOMB)) {
                Arrays.stream(this.myCell.getObjects(FightObjectType.OBJECT_BOMB)).forEach(Object -> ((FightBomb) Object).remove());
            }
            Short[] cells;
            for (Fighter Friend : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof BombFighter) && Fighter.getSummoner() == this.summoner && Pathfunction.inLine(null, this.getCellId(), Fighter.getCellId()) && this.grade.getMonsterId() == ((BombFighter) Fighter).grade.getMonsterId())::iterator) {
                final int distance = Pathfunction.goalDistance(null, getCellId(), Friend.getCellId());
                logger.debug("Bomb Distance = {}" , distance);
                if (distance >= 2 && distance <= 7) {
                    cells = Pathfunction.getLineCellsBetweenBomb(fight, this.getCellId(), Pathfunction.getDirection(null, this.getCellId(), Friend.getCellId()), Friend.getCellId(), false);
                    if (cells != null) {
                        cells =  ArrayUtils.removeElement(cells, this.getCellId());
                        cells =  ArrayUtils.removeElement(cells, Friend.getCellId());
                        final FightBomb Bomb = new FightBomb(this, DAO.getSpells().findSpell(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId).getSpellLevel(this.grade.getGrade()), EffectActivableObject.getColor(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId), cells, new BombFighter[]{this, (BombFighter) Friend});
                        fight.addActivableObject(this, Bomb);
                    }
                }
            }

            return this.buff.endMove();
        }
        return -1;
    }

    @Override
    public int beginTurn() {
        super.onBeginTurn();
        return super.beginTurn();
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
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_FIGHTER;
    }

}
