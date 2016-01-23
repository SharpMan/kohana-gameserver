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
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
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
    public BombFighter(Fight Fight, Fighter Summoner, MonsterGrade Monster) {
        super(Fight, Summoner);
        this.grade = Monster;
        super.initFighter(this.grade.getStats(), Fight.getNextContextualId());
        this.entityLook = EntityLookParser.Copy(this.grade.getMonster().getEntityLook());
        this.adjustStats();
        this.stats.merge(this.summoner.getStats());
        this.stats.unMerge(StatsEnum.VITALITY,this.summoner.getStats().getEffect(StatsEnum.VITALITY));
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }

    @Override
    public void computeDamages(StatsEnum effect, MutableInt jet) {
        switch (effect) {
            case DAMAGE_EARTH:
            case STEAL_EARTH:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES)) / 100 + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_EARTH_DAMAGES_BONUS)));
                break;
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.STRENGTH) + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_PHYSIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS)));
                break;

            case DAMAGE_FIRE:
            case STEAL_FIRE:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.INTELLIGENCE) + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_FIRE_DAMAGES_BONUS)));
                break;

            case DAMAGE_AIR:
            case STEAL_AIR:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.AGILITY) + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_AIR_DAMAGES_BONUS)));
                break;

            case DAMAGE_WATER:
            case STEAL_WATER:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.CHANCE) + this.stats.getTotal(StatsEnum.COMBO_DAMMAGES)) / 100
                        + this.stats.getTotal(StatsEnum.ADD_DAMAGE_MAGIC) + this.stats.getTotal(StatsEnum.ALL_DAMAGES_BONUS) + this.stats.getTotal(StatsEnum.ADD_WATER_DAMAGES_BONUS)));
                break;
        }
    }

    @Override
    public List<SpellLevel> getSpells() {
        return null;
    }

    public boolean boosted = false;

    public synchronized void selfMurder(int Caster) {
        if (boosted) {
            return;
        }
        int TotalCombo = 0;
        ArrayList<BombFighter> Targets = new ArrayList<>(6);
        for (short aCell : (new Zone(SpellShapeEnum.C, (byte) 2, MapPoint.fromCellId(this.getCellId()).advancedOrientationTo(MapPoint.fromCellId(this.getCellId()), true), this.fight.getMap())).getCells(this.getCellId())) {
            FightCell FightCell = fight.getCell(aCell);
            if (FightCell != null) {
                if (FightCell.hasGameObject(IFightObject.FightObjectType.OBJECT_STATIC)) {
                    for (Fighter Target : FightCell.getObjectsAsFighter()) {
                        if (Target.getID() == this.ID) {
                            continue;
                        }
                        if (Target instanceof BombFighter /*&& ((BombFighter) target).grade.monsterId == this.grade.monsterId*/ && ((BombFighter) Target).summoner == this.summoner && !((BombFighter) Target).boosted) {
                            Targets.add((BombFighter) Target);
                            TotalCombo += 40;
                        }
                    }
                }
            }
        }
        if (TotalCombo == 0) {
            return;
        }
        fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + TotalCombo + "% dommages d'explosion"}));
        stats.addBoost(StatsEnum.COMBO_DAMMAGES, TotalCombo);
        this.boosted = true;
        for (Fighter bomb : Targets) {
            bomb.getStats().addBoost(StatsEnum.COMBO_DAMMAGES, TotalCombo);
            boosted = true;
        }
        Targets.forEach(Bomb -> Bomb.tryDie(Caster, true));
    }

    @Override
    public int tryDie(int casterId, boolean force) {
        if (this.getLife() <= 0 && !force) {
            if (this.buff.getAllBuffs().anyMatch(x -> x.activeType == BuffActiveType.ACTIVE_ON_DIE)) {
                return this.buff.getAllBuffs().filter(x -> x.activeType == BuffActiveType.ACTIVE_ON_DIE).findFirst().get().applyEffect(null, null);
            } else {
                if (this.FightBombs != null) {
                    this.FightBombs.forEach(Bomb -> Bomb.remove());
                }
                return super.tryDie(casterId, force);
            }
        }
        if (this.getLife() <= 0 || force) {
            selfMurder(casterId);
            fight.launchSpell(this, DAO.getSpells().findSpell(DAO.getSpells().findBomb(this.grade.getMonsterId()).explodSpellId).getSpellLevel(this.grade.getGrade()), this.getCellId(), true, true, false);
            if (this.FightBombs != null) {
                this.FightBombs.forEach(Bomb -> Bomb.remove());
            }
        }
        return super.tryDie(casterId, force);
    }

    public ArrayList<FightBomb> FightBombs;

    public void addBomb(FightBomb Bomb) {
        if (FightBombs == null) {
            FightBombs = new ArrayList<>(4);
        }
        this.FightBombs.add(Bomb);
    }

    @Override
    public int onCellChanged() {
        if (this.myCell != null) {
            if (this.isDead()) {
                return -2;
            }
            if (this.FightBombs != null) {
                this.FightBombs.forEach(Bomb -> Bomb.remove());
            }
            if (this.myCell.hasGameObject(FightObjectType.OBJECT_BOMB)) {
                Arrays.stream(this.myCell.getObjects(FightObjectType.OBJECT_BOMB)).forEach(Object -> ((FightBomb) Object).remove());
            }
            Short[] Cells;
            for (Fighter Friend : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof BombFighter) && Fighter.getSummoner() == this.summoner && Pathfunction.inLine(null, this.getCellId(), Fighter.getCellId()) && this.grade.getMonsterId() == ((BombFighter) Fighter).grade.getMonsterId())::iterator) {
                int Distance = Pathfunction.goalDistance(null, getCellId(), Friend.getCellId());
                logger.debug("Bomb Distance = {}" , Distance);
                if (Distance >= 2 && Distance <= 7) {
                    Cells = Pathfunction.getLineCellsBetweenBomb(fight, this.getCellId(), Pathfunction.getDirection(null, this.getCellId(), Friend.getCellId()), Friend.getCellId(), false);
                    if (Cells != null) {
                        Cells =  ArrayUtils.removeElement(Cells, this.getCellId());
                        Cells =  ArrayUtils.removeElement(Cells, Friend.getCellId());
                        FightBomb Bomb = new FightBomb(this, DAO.getSpells().findSpell(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId).getSpellLevel(this.grade.getGrade()), EffectActivableObject.getColor(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId), Cells, new BombFighter[]{this, (BombFighter) Friend});
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

}
