package koh.game.fights.effects;

import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;

import java.util.ArrayList;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostState extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            try {
                final BuffEffect buff = target.getStates().getBuffByState(FightStateEnum.valueOf(castInfos.effect.value));
                final int spellSource = buff.castInfos.spellId;
                if(spellSource == 99 &&
                        castInfos.caster.isEnnemyWith(buff.caster) &&
                        (castInfos.effect.value == 251 || castInfos.effect.value == 244)
                        ){
                    final EffectCast castInfos2 = new EffectCast(mummification.getEffectType(), castInfos.spellId, target.getCellId(), 0, mummification, castInfos.caster, new ArrayList<Fighter>() {{ this.add(castInfos.caster); }}, false, StatsEnum.NONE, 0, castInfos.spellLevel);
                    castInfos2.targetKnownCellId = target.getCellId();
                    if (EffectBase.tryApplyEffect(castInfos2) == -3) {
                        break;
                    }
                }
                target.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_FIGHT_UNSET_STATE, castInfos.caster.getID(), target.getID(), spellSource));
                target.getBuff().getBuffsAct().get(buff.activeType).remove(buff);
                target.getBuff().getBuffsDec().get(buff.decrementType).remove(buff);

            }
            catch (Exception e){
                target.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_FIGHT_UNSET_STATE, castInfos.caster.getID(), target.getID(), castInfos.spellId));

            }
            finally {
                target.getStates().removeState(FightStateEnum.valueOf(castInfos.effect.value));
            }
        }

        return -1;
    }

    private static final EffectInstanceDice mummification = new EffectInstanceDice(new EffectInstance(131900, 138,0, "a", 0, 0,0, "P1,", 2, "I", false, true, true) , 100, 0,0);


}
