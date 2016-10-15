package koh.game.fights.effects.buff;

import koh.game.entities.environments.Pathfunction;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectPush;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffDodge extends BuffEffect {

    public BuffDodge(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_BEGINTURN);
    }

    private class ShortCast{
        public Fighter caster;
        public int AP;
        public InventoryItem weapon;
    }

    private ShortCast lastCast;

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        boolean isSameWeapon = lastCast != null &&
                damageInfos.isCAC &&
                lastCast.caster == damageInfos.caster &&
                lastCast.AP == lastCast.caster.getAP()
                && lastCast.weapon == damageInfos.caster.getPlayer().getInventoryCache().getWeapon();

        if (!(isSameWeapon) &&
                (target.getCellId() != damageInfos.targetKnownCellId ||
                        Pathfunction.goalDistance(target.getFight().getMap(), damageInfos.caster.getCellId(), target.getCellId()) > 1
        )) {
            if(!(damageInfos.spellLevel != null &&
                    ArrayUtils.indexOf(damageInfos.spellLevel.getEffects(), damageInfos.effect) > 0 &&
                    Pathfunction.goalDistance(target.getFight().getMap(), damageInfos.caster.getCellId(), target.getCellId()) > ( ArrayUtils.indexOf(damageInfos.spellLevel.getEffects(), damageInfos.effect)) &&
                    target.getCellId() != damageInfos.targetKnownCellId)){
                return -1;
            }

        }

        if(damageInfos.caster.isPlayer() && damageInfos.isCAC && lastCast == null){
            this.lastCast = new ShortCast();
            this.lastCast.caster = damageInfos.caster;
            this.lastCast.AP = damageInfos.caster.getAP();
            this.lastCast.weapon = damageInfos.caster.getPlayer().getInventoryCache().getWeapon();
        }

        damageValue.setValue(0);
        if(isSameWeapon){
            return -1;
        }

        final EffectCast subInfos = new EffectCast(StatsEnum.PUSH_BACK, 0, (short) 0, 0, null, damageInfos.caster, null, false, StatsEnum.NONE, 0, null);
        final byte direction = Pathfunction.getDirection(target.getFight().getMap(), damageInfos.caster.getCellId(), target.getCellId());

        // Application du push
        return EffectPush.applyPush(subInfos, this.target, direction, 1);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}
