package koh.game.fights.effects.buff;

import koh.game.dao.DAO;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectCastSpell;
import koh.game.fights.fighters.BombFighter;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Melancholia on 6/3/16.
 */
public class BuffCastSpell extends BuffEffect {

    private static final Logger logger = LogManager.getLogger(BuffCastSpell.class);

    public BuffCastSpell(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_BEGINTURN, BuffDecrementType.TYPE_ENDTURN);
        /*if(castInfos.effect.diceNum == 5574){
            this.delay ==
        }*/
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        return EffectCastSpell.castSpell(castInfos);
        //return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}