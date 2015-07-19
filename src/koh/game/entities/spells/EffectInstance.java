package koh.game.entities.spells;

import com.mysql.jdbc.StringUtils;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.regex.Matcher;
import jregex.MatchIterator;
import jregex.Pattern;
import jregex.REFlags;
import koh.d2o.entities.Effect;
import koh.game.Main;
import koh.game.dao.D2oDao;
import koh.game.dao.MonsterDAO;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.protocol.client.BufUtils;
import static koh.protocol.client.BufUtils.writeBoolean;
import static koh.protocol.client.BufUtils.writeUTF;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.SpellTargetType;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.context.fight.GameFightCompanionInformations;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstance implements Serializable {

    //private static final RegExp exclusiveTargetMasks = RegExpFactory.create("\\*?[bBeEfFzZKoOPpTWUvV][0-9]*", "g");
    private static final java.util.regex.Pattern exclusiveTargetMasks = java.util.regex.Pattern.compile("(\\*?[bBeEfFzZKoOPpTWUvV][0-9]*)");

    public static final int classID = 1;

    public byte SerializationIdentifier() {
        return 1;
    }

    public int effectUid, effectId, targetId;
    public String targetMask;
    public int duration, random, group;
    public String rawZone;
    public int delay;
    public String triggers;
    public boolean visibleInTooltip, visibleInFightLog, visibleInBuffUi;

    private int zoneShape, zoneSize, zoneMinSize, zoneEfficiencyPercent, zoneMaxEfficiency;
    public boolean initialized;

    public byte ZoneSize() {
        return (byte) zoneSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public StatsEnum EffectType() {
        return StatsEnum.valueOf(this.effectId);
    }

    public boolean IsValidTarget(Fighter Caster, Fighter actor) {
        return Targets() == SpellTargetType.NONE
                || Targets() == SpellTargetType.ALL
                || Caster == actor && Targets().HasFlag(SpellTargetType.SELF)
                || (!Targets().HasFlag(SpellTargetType.ONLY_SELF) || actor == Caster)
                && (Caster.IsFriendlyWith(actor) && Caster != actor && ((Targets().HasFlag(SpellTargetType.ALLY_1) || Targets().HasFlag(SpellTargetType.ALLY_2) || (Targets().HasFlag(SpellTargetType.ALLY_3) || Targets().HasFlag(SpellTargetType.ALLY_4)) || Targets().HasFlag(SpellTargetType.ALLY_5)) && !(actor instanceof SummonedFighter) || (Targets().HasFlag(SpellTargetType.ALLY_SUMMONS) || Targets().HasFlag(SpellTargetType.ALLY_STATIC_SUMMONS)) && actor instanceof SummonedFighter) || Caster.IsEnnemyWith(actor) && ((Targets().HasFlag(SpellTargetType.ENNEMY_1) || Targets().HasFlag(SpellTargetType.ENNEMY_2) || (Targets().HasFlag(SpellTargetType.ENNEMY_3) || Targets().HasFlag(SpellTargetType.ENNEMY_4)) || Targets().HasFlag(SpellTargetType.ENNEMY_5)) && !(actor instanceof SummonedFighter) || (Targets().HasFlag(SpellTargetType.ENNEMY_SUMMONS) || Targets().HasFlag(SpellTargetType.ENNEMY_STATIC_SUMMONS)) && actor instanceof SummonedFighter));
    }

    public SpellShapeEnum ZoneShape() {
        if (!this.initialized) {
            this.parseZone();
        }
        return SpellShapeEnum.valueOf(zoneShape);
    }

    public SpellTargetType Targets() {
        return SpellTargetType.valueOf(this.targetId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstance)) {
            return false;
        }
        EffectInstance rhs = (EffectInstance) obj;
        return new EqualsBuilder().
                append(effectUid, rhs.effectUid).
                append(effectId, rhs.effectId).
                append(targetMask, rhs.targetMask).
                append(targetId, rhs.targetId).
                append(duration, rhs.duration).
                append(random, rhs.random).
                append(group, rhs.group).
                append(rawZone, rhs.rawZone).
                append(delay, rhs.delay).
                append(triggers, rhs.triggers).
                append(visibleInTooltip, rhs.visibleInTooltip).
                append(visibleInFightLog, rhs.visibleInFightLog).
                append(visibleInBuffUi, rhs.visibleInBuffUi).
                isEquals();
    }

    public EffectInstance Clone() {
        return new EffectInstance(effectUid, effectId, targetId, targetMask, duration, random, group, rawZone, delay, triggers, visibleInTooltip, visibleInFightLog, visibleInBuffUi);
    }

    public EffectInstance(int effectUid, int effectId, int targetId, String targetMask, int duration, int random, int group, String rawZone, int delay, String triggers, boolean visibleInTooltip, boolean visibleInFightLog, boolean visibleInBuffUi) {
        this.effectUid = effectUid;
        this.effectId = effectId;
        this.targetId = targetId;
        this.targetMask = targetMask;
        this.duration = duration;
        this.random = random;
        this.group = group;
        this.rawZone = rawZone;
        this.delay = delay;
        this.triggers = triggers;
        this.visibleInTooltip = visibleInTooltip;
        this.visibleInFightLog = visibleInFightLog;
        this.visibleInBuffUi = visibleInBuffUi;
    }

    public EffectInstance(int effectId) {
        this.effectUid = 0;
        this.effectId = effectId;
        this.targetId = 0;
        this.targetMask = "";
        this.duration = 0;
        this.random = 0;
        this.group = 0;
        this.rawZone = "";
        this.delay = 0;
        this.triggers = "";
    }

    public static Boolean verifySpellEffectMask(Fighter pCasterId, Fighter pTargetId, EffectInstance pEffect) {
        return verifySpellEffectMask(pCasterId, pTargetId, pEffect, 0);
    }

    public int category() {
        if (D2oDao.getEffect(this.effectId) == null) {
            return -1;
        }
        return D2oDao.getEffect(this.effectId).category;
    }

    public static Boolean verifySpellEffectMask(Fighter pCasterId, Fighter pTargetId, EffectInstance pEffect, int pTriggeringSpellCasterId) {
        Pattern r;
        String targetMaskPattern;
        Matcher exclusiveMasks;
        String exclusiveMask;
        String exclusiveMaskParam;
        Boolean exclusiveMaskCasterOnly;
        Boolean verify;
        int maskState;
        if ((((((((pEffect == null)) /*|| ((pEffect.delay > 0))*/)) || ((StringUtils.isNullOrEmpty(pEffect.targetMask))))))) {
            return (false);
        };
        boolean targetIsCaster = (pTargetId.ID == pCasterId.ID);
        boolean targetIsCarried = pTargetId.GetCarriedActor() != 0;/*((((target) && (target.parentSprite))) && ((target.parentSprite.carriedEntity == target)));*/

        GameFightFighterInformations targetInfos = (GameFightFighterInformations) pTargetId.GetGameContextActorInformations(null);
        GameFightMonsterInformations monsterInfo = pTargetId.GetGameContextActorInformations(null) instanceof GameFightMonsterInformations ? (GameFightMonsterInformations) pTargetId.GetGameContextActorInformations(null) : null;
        boolean isTargetAlly = pCasterId.IsFriendlyWith(pTargetId);
        if ((((((pCasterId.ID == pCasterId.Fight.CurrentFighter.ID)) && ((pEffect.category() == 0)))) && (((pEffect.targetMask.equals("C")))))) {
            return (true);
        };
        if (targetIsCaster) {
            if (pEffect.effectId == 90) {
                return (true);
            };
            if (pEffect.targetMask.indexOf("g") == -1) {
                targetMaskPattern = "caC";
            } else {
                return (false);
            };
        } else {
            if (((((targetIsCarried) && (!((pEffect.ZoneShape() == SpellShapeEnum.A))))) && (!((pEffect.ZoneShape() == SpellShapeEnum.a))))) {
                return (true);
            };
            if (((((targetInfos.stats.summoned) && (monsterInfo != null))) && (!(MonsterDAO.Cache.get(monsterInfo.creatureGenericId).canPlay)))) {
                targetMaskPattern = ((isTargetAlly) ? "agsj" : "ASJ");
            } else {
                if (targetInfos.stats.summoned) {
                    targetMaskPattern = ((isTargetAlly) ? "agij" : "AIJ");
                } else {
                    if ((pTargetId.GetGameContextActorInformations(null) instanceof GameFightCompanionInformations)) {
                        targetMaskPattern = ((isTargetAlly) ? "agdl" : "ADL");
                    } else {
                        if ((pTargetId.GetGameContextActorInformations(null) instanceof GameFightMonsterInformations)) {
                            targetMaskPattern = ((isTargetAlly) ? "agm" : "AM");
                        } else {
                            targetMaskPattern = ((isTargetAlly) ? "gahl" : "AHL");
                        };
                    };
                };
            };
        };
        r = new Pattern("[" + targetMaskPattern + "]", REFlags.DOTALL);
        verify = r.matcher(pEffect.targetMask).find();

        if (verify) {
            exclusiveMasks = exclusiveTargetMasks.matcher(pEffect.targetMask);
            boolean verifyInitialized = false;

            while (exclusiveMasks.find()) {
                if (!verifyInitialized) {
                    verify = false;
                    verifyInitialized = true;
                }
                exclusiveMask = exclusiveMasks.group(1);
                exclusiveMaskCasterOnly = (exclusiveMask.charAt(0) == '*');
                exclusiveMask = ((exclusiveMaskCasterOnly) ? exclusiveMask.substring(1, (exclusiveMask.length())) : exclusiveMask);
                exclusiveMaskParam = (((exclusiveMask.length() > 1)) ? exclusiveMask.substring(1, (exclusiveMask.length())) : null);
                switch (exclusiveMask.charAt(0)) {
                    case 'b':
                        break;
                    case 'B':
                        break;
                    case 'e':
                        maskState = Integer.parseInt(exclusiveMaskParam);
                        if (exclusiveMaskCasterOnly) {
                            verify = !pCasterId.HasState(maskState);
                        } else {
                            verify = !pTargetId.HasState(maskState);
                        }
                        break;
                    case 'E':
                        maskState = Integer.parseInt(exclusiveMaskParam);
                        if (exclusiveMaskCasterOnly) {
                            verify = pCasterId.HasState(maskState);
                        } else {
                            verify = pTargetId.HasState(maskState);
                        }
                        break;
                    case 'f':
                        verify = (((monsterInfo == null)) || (!((monsterInfo.creatureGenericId == Integer.parseInt(exclusiveMaskParam)))));
                        break;
                    case 'F':
                        verify = ((monsterInfo != null) && ((monsterInfo.creatureGenericId == Integer.parseInt(exclusiveMaskParam))));
                        if (verify && pTargetId instanceof BombFighter) { //TEmpororaire = bug
                            return true;
                        }
                        break;
                    case 'z':
                        break;
                    case 'Z':
                        break;
                    case 'K':
                        break;
                    case 'o':
                        verify = true;
                        break;
                    case 'O':
                        verify = ((!((pTriggeringSpellCasterId == 0))) && ((pTargetId.ID == pTriggeringSpellCasterId)));
                        break;
                    case 'p':
                        break;
                    case 'P':
                        if (pTargetId instanceof BombFighter) { //TEmpororaire = bug
                            verify = true;
                        }
                        break;
                    case 'T':
                        break;
                    case 'W':
                        break;
                    case 'U':
                        break;
                    case 'v':
                        verify = (((targetInfos.stats.lifePoints / targetInfos.stats.maxLifePoints) * 100) > Integer.parseInt(exclusiveMaskParam));
                        break;
                    case 'V':
                        verify = (((targetInfos.stats.lifePoints / targetInfos.stats.maxLifePoints) * 100) <= Integer.parseInt(exclusiveMaskParam));
                        break;
                };
                if (!(verify)) {
                    return (false);
                };
            };
            //};
        };
        return (verify);
    }

    public void parseZone() {
        String[] params;
        boolean hasMinSize;
        this.initialized = true;
        if (((this.rawZone != null) && (!this.rawZone.isEmpty()))) {
            this.zoneShape = this.rawZone.charAt(0);
            params = this.rawZone.substring(1).split(",");
            hasMinSize = (((((((((this.ZoneShape() == SpellShapeEnum.C)) || ((this.ZoneShape() == SpellShapeEnum.X)))) || ((this.ZoneShape() == SpellShapeEnum.Q)))) || ((this.ZoneShape() == SpellShapeEnum.plus)))) || ((this.ZoneShape() == SpellShapeEnum.sharp)));
            switch (params.length) {
                case 1:
                    this.zoneSize = Integer.parseInt(params[0]);
                    break;
                case 2:
                    this.zoneSize = Integer.parseInt(params[0]);
                    if (hasMinSize) {
                        this.zoneMinSize = Integer.parseInt(params[1]);
                    } else {
                        this.zoneEfficiencyPercent = Integer.parseInt(params[1]);
                    }
                    ;
                    break;
                case 3:
                    this.zoneSize = Integer.parseInt(params[0]);
                    if (hasMinSize) {
                        this.zoneMinSize = Integer.parseInt(params[1]);
                        this.zoneEfficiencyPercent = Integer.parseInt(params[2]);
                    } else {
                        this.zoneEfficiencyPercent = Integer.parseInt(params[1]);
                        this.zoneMaxEfficiency = Integer.parseInt(params[2]);
                    }
                    ;
                    break;
                case 4:
                    this.zoneSize = Integer.parseInt(params[0]);
                    this.zoneMinSize = Integer.parseInt(params[1]);
                    this.zoneEfficiencyPercent = Integer.parseInt(params[2]);
                    this.zoneMaxEfficiency = Integer.parseInt(params[3]);
                    break;
            };
        } else {
            Main.Logs().writeError(("Zone incorrect (" + this.rawZone) + ")");
        };
        /*if(this.zoneMinSize >=63)
         this.zoneMinSize = 63;
         if(this.zoneSize >=63)
         this.zoneSize = 63;*/
    }

    public Effect Template() {
        return D2oDao.getEffect(this.effectId);
    }

    public EffectInstance(IoBuffer buf) {
        if (buf == null) {
            return;
        }
        this.effectUid = buf.getInt();
        this.effectId = buf.getInt();
        this.targetId = buf.getInt();
        this.targetMask = BufUtils.readUTF(buf);
        this.duration = buf.getInt();
        this.random = buf.getInt();
        this.group = buf.getInt();
        this.visibleInTooltip = BufUtils.readBoolean(buf);
        this.visibleInBuffUi = BufUtils.readBoolean(buf);
        this.visibleInFightLog = BufUtils.readBoolean(buf);
        this.rawZone = BufUtils.readUTF(buf);
        this.delay = buf.getInt();
        this.triggers = BufUtils.readUTF(buf);
    }

    public void toBinary(IoBuffer buf) {
        buf.putInt(this.effectUid);
        buf.putInt(this.effectId);
        buf.putInt(this.targetId);
        writeUTF(buf, this.targetMask);
        buf.putInt(this.duration);
        buf.putInt(this.random);
        buf.putInt(this.group);
        writeBoolean(buf, this.visibleInTooltip);
        writeBoolean(buf, this.visibleInBuffUi);
        writeBoolean(buf, this.visibleInFightLog);
        writeUTF(buf, this.rawZone);
        buf.putInt(this.delay);
        writeUTF(buf, this.triggers);
    }

    public void totalClear() {
        effectUid = 0;
        effectId = 0;
        targetId = 0;
        targetMask = null;
        duration = 0;
        random = 0;
        group = 0;
        rawZone = null;
        delay = 0;
        triggers = null;
        this.visibleInBuffUi = false;
        this.visibleInFightLog = false;
        this.visibleInTooltip = false;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
