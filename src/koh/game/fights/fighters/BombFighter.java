package koh.game.fights.fighters;

import java.util.ArrayList;
import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectActivableObject;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layer.FightBomb;
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
import org.apache.commons.lang.ArrayUtils;
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
        this.stats.unMerge(StatsEnum.Vitality,this.summoner.getStats().getEffect(StatsEnum.Vitality));
        super.setLife(this.getLife());
        super.setLifeMax(this.getMaxLife());
    }

    @Override
    public void computeDamages(StatsEnum effect, MutableInt jet) {
        switch (effect) {
            case Damage_Earth:
            case Steal_Earth:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.Combo_Dammages)) / 100 + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Earth_Damages_Bonus)));
                break;
            case Damage_Neutral:
            case Steal_Neutral:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Neutral_Damages_Bonus)));
                break;

            case Damage_Fire:
            case Steal_Fire:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence) + this.stats.getTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus)));
                break;

            case Damage_Air:
            case Steal_Air:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Agility) + this.stats.getTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Air_Damages_Bonus)));
                break;

            case Damage_Water:
            case Steal_Water:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Chance) + this.stats.getTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Water_Damages_Bonus)));
                break;
        }
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
                if (FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_STATIC)) {
                    for (Fighter Target : FightCell.GetObjectsAsFighter()) {
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
        stats.addBoost(StatsEnum.Combo_Dammages, TotalCombo);
        this.boosted = true;
        for (Fighter bomb : Targets) {
            bomb.getStats().addBoost(StatsEnum.Combo_Dammages, TotalCombo);
            boosted = true;
        }
        Targets.forEach(Bomb -> Bomb.tryDie(Caster, true));
    }

    @Override
    public int tryDie(int casterId, boolean force) {
        if (this.getLife() <= 0 && !force) {
            if (this.buff.getAllBuffs().anyMatch(x -> x.ActiveType == BuffActiveType.ACTIVE_ON_DIE)) {
                return this.buff.getAllBuffs().filter(x -> x.ActiveType == BuffActiveType.ACTIVE_ON_DIE).findFirst().get().applyEffect(null, null);
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
            if (this.myCell.HasGameObject(FightObjectType.OBJECT_BOMB)) {
                Arrays.stream(this.myCell.GetObjects(FightObjectType.OBJECT_BOMB)).forEach(Object -> ((FightBomb) Object).remove());
            }
            Short[] Cells;
            for (Fighter Friend : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof BombFighter) && Fighter.getSummoner() == this.summoner && Pathfinder.inLine(null, this.getCellId(), Fighter.getCellId()) && this.grade.getMonsterId() == ((BombFighter) Fighter).grade.getMonsterId())::iterator) {
                int Distance = Pathfinder.getGoalDistance(null, getCellId(), Friend.getCellId());
                logger.debug("Bomb Distance = {}" , Distance);
                if (Distance >= 2 && Distance <= 7) {
                    Cells = Pathfinder.getLineCellsBetweenBomb(fight, this.getCellId(), Pathfinder.getDirection(null, this.getCellId(), Friend.getCellId()), Friend.getCellId(), false);
                    if (Cells != null) {
                        Cells = (Short[]) ArrayUtils.removeElement(Cells, this.getCellId());
                        Cells = (Short[]) ArrayUtils.removeElement(Cells, Friend.getCellId());
                        FightBomb Bomb = new FightBomb(this, DAO.getSpells().findSpell(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId).getSpellLevel(this.grade.getGrade()), EffectActivableObject.GetColor(DAO.getSpells().findBomb(grade.getMonsterId()).wallSpellId), Cells, new BombFighter[]{this, (BombFighter) Friend});
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
