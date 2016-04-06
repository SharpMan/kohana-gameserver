package koh.game.fights.effects.buff;

import java.util.ArrayList;
import java.util.List;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_CHANGE_LOOK;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.SubEntityBindingPointCategoryEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightChangeLookMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import koh.protocol.types.game.look.EntityLook;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSkin extends BuffEffect {

    private short oldBonesID, skinToRemove;
    private List<Short> oldScales;
    private EntityLook look;

    public BuffSkin(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    /**
     *
     * @param damageValue
     * @param damageInfos
     * @return
     */
    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        if (target.getEntityLook().subentities.stream().anyMatch(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER)) {
            look = target.getEntityLook().subentities.stream().filter(x -> x.bindingPointCategory == SubEntityBindingPointCategoryEnum.HOOK_POINT_CATEGORY_MOUNT_DRIVER).findFirst().get().subEntityLook;
        } else {
            look = target.getEntityLook();
        }
        this.oldBonesID = look.bonesId;
        this.oldScales = look.scales;
        switch (this.castInfos.spellId) {
            case 2879: //Masque pleutre
                look.bonesId = 1576;
                break;
            case 686: //Picole
                look.bonesId = 44;
                break;
            case 2880: //Masque du Psychopathe
                look.bonesId = 1575;
                this.skinToRemove = (short) 1443;
                look.skins.add(skinToRemove);
                break;
            case 99: //Momi
                look.bonesId = 113;
                break;
            case 701: //Colére
                look.bonesId = 453;
                look.scales = new ArrayList<Short>() {
                    {
                        this.add((short) 80);
                    }
                };
                break;
            case 3202: //Scaphandre
                look.bonesId = 1;
                this.skinToRemove = (short) 1955;
                look.skins.add(skinToRemove);
                break;
            default:
                return -1;
        }

        //this.target.getEntityLook().bonesId = (short) Math.abs((this.castInfos.effect.value * 44) / 666);
        this.caster.getFight().sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.caster.getID(), this.target.getID(), this.target.getEntityLook()));
        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public int removeEffect() {
        if (look.bonesId == this.oldBonesID && (skinToRemove == 0 || oldScales == null)) {
            return super.removeEffect();
        }
        look.bonesId = this.oldBonesID;
        look.scales = this.oldScales;
        if (this.skinToRemove != 0) {
            look.skins.remove(look.skins.indexOf(this.skinToRemove));
        }
        this.caster.getFight().sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.caster.getID(), this.target.getID(), this.target.getEntityLook()));
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE_BY_DEATH, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) 0);
    }

}
