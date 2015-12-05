package koh.game.fights.effects.buff;

import java.util.ArrayList;
import java.util.List;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_CHANGE_LOOK;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightChangeLookMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSkin extends BuffEffect {

    private short oldBonesID, skinToRemove;
    private List<Short> oldScales;

    public BuffSkin(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    /**
     *
     * @param DamageValue
     * @param DamageInfos
     * @return
     */
    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        this.oldBonesID = Target.getEntityLook().bonesId;
        this.oldScales = Target.getEntityLook().scales;
        switch (this.CastInfos.SpellId) {
            case 2879: //Masque pleutre
                this.Target.entityLook.bonesId = 1576;
                break;
            case 686: //Picole
                this.Target.entityLook.bonesId = 44;
                break;
            case 2880: //Masque du Psychopathe
                this.Target.entityLook.bonesId = 1575;
                this.skinToRemove = (short) 1443;
                this.Target.entityLook.skins.add(skinToRemove);
                break;
            case 99: //Momi
                this.Target.entityLook.bonesId = 113;
                break;
            case 701: //Colére
                this.Target.entityLook.bonesId = 453;
                this.Target.entityLook.scales = new ArrayList<Short>() {
                    {
                        this.add((short) 80);
                    }
                };
                break;
            case 3202: //Scaphandre
                this.Target.entityLook.bonesId = 1;
                this.skinToRemove = (short) 1955;
                this.Target.entityLook.skins.add(skinToRemove);
                break;
            default:
                return -1;
        }

        //this.Target.entityLook.bonesId = (short) Math.abs((this.CastInfos.Effect.value * 44) / 666);
        this.Caster.fight.sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.Caster.ID, this.Target.ID, this.Target.getEntityLook()));
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        if (this.Target.entityLook.bonesId == this.oldBonesID && (skinToRemove == 0 || oldScales == null)) {
            return super.removeEffect();
        }
        this.Target.entityLook.bonesId = this.oldBonesID;
        this.Target.entityLook.scales = this.oldScales;
        if (this.skinToRemove != 0) {
            this.Target.entityLook.skins.remove(this.Target.entityLook.skins.indexOf(this.skinToRemove));
        }
        this.Caster.fight.sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.Caster.ID, this.Target.ID, this.Target.getEntityLook()));
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.DISPELLABLE_BY_DEATH, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) 0);
    }

}
