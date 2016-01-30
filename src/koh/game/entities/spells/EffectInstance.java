package koh.game.entities.spells;

import com.mysql.jdbc.StringUtils;

import java.io.Serializable;
import java.util.regex.Matcher;

import jregex.Pattern;
import jregex.REFlags;
import koh.d2o.entities.Effect;
import koh.game.dao.DAO;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstance implements Serializable {

    //private static final RegExp exclusiveTargetMasks = RegExpFactory.create("\\*?[bBeEfFzZKoOPpTWUvV][0-9]*", "g");
    private static final java.util.regex.Pattern exclusiveTargetMasks = java.util.regex.Pattern.compile("(\\*?[bBeEfFzZKoOPpTWUvV][0-9]*)");
    private static final Logger logger = LogManager.getLogger(EffectInstance.class);
    public static final int classID = 1;

    public byte serializationIdentifier() {
        return 1;
    }

    public int effectUid, effectId, targetId;
    public String targetMask;
    public int duration, random, group;
    public String rawZone;
    public int delay;
    public String triggers;
    public boolean visibleInTooltip, visibleInFightLog, visibleInBuffUi;

    private int zoneShape = -100000, zoneSize = -100000, zoneMinSize = -100000, zoneEfficiencyPercent = -100000, zoneMaxEfficiency = -100000; //Integer.NULL NOTATION
    public boolean initialized;

    public int zoneEfficiencyPercent() {
        this.parseZone();
        return this.zoneEfficiencyPercent;
    }

    public int zoneMaxEfficiency() {
        this.parseZone();
        return this.zoneMaxEfficiency;
    }

    public byte zoneMinSize() {
        this.parseZone();
        return (byte)this.zoneMinSize;
    }

    public byte zoneSize() {
        this.parseZone();
        return (byte) zoneSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public StatsEnum getEffectType() {
        return StatsEnum.valueOf(this.effectId);
    }

    

    public boolean isValidTarget(Fighter Caster, Fighter actor) {
        return targets() == SpellTargetType.NONE
                || targets() == SpellTargetType.ALL
                || Caster == actor && targets().HasFlag(SpellTargetType.SELF)
                || (!targets().HasFlag(SpellTargetType.ONLY_SELF) || actor == Caster)
                && (Caster.isFriendlyWith(actor) && Caster != actor && ((targets().HasFlag(SpellTargetType.ALLY_1) || targets().HasFlag(SpellTargetType.ALLY_2) || (targets().HasFlag(SpellTargetType.ALLY_3) || targets().HasFlag(SpellTargetType.ALLY_4)) || targets().HasFlag(SpellTargetType.ALLY_5)) && !(actor instanceof SummonedFighter) || (targets().HasFlag(SpellTargetType.ALLY_SUMMONS) || targets().HasFlag(SpellTargetType.ALLY_STATIC_SUMMONS)) && actor instanceof SummonedFighter) || Caster.isEnnemyWith(actor) && ((targets().HasFlag(SpellTargetType.ENNEMY_1) || targets().HasFlag(SpellTargetType.ENNEMY_2) || (targets().HasFlag(SpellTargetType.ENNEMY_3) || targets().HasFlag(SpellTargetType.ENNEMY_4)) || targets().HasFlag(SpellTargetType.ENNEMY_5)) && !(actor instanceof SummonedFighter) || (targets().HasFlag(SpellTargetType.ENNEMY_SUMMONS) || targets().HasFlag(SpellTargetType.ENNEMY_STATIC_SUMMONS)) && actor instanceof SummonedFighter));
    }

    public int zoneShape() {
        this.parseZone();
        return zoneShape;
    }

    public SpellShapeEnum getZoneShape() {
        this.parseZone();
        return SpellShapeEnum.valueOf(zoneShape);
    }

    public SpellTargetType targets() {
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
        if (DAO.getD2oTemplates().getEffect(this.effectId) == null) {
            return -1;
        }
        return DAO.getD2oTemplates().getEffect(this.effectId).category;
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
        boolean targetIsCaster = (pTargetId.getID() == pCasterId.getID());
        boolean targetIsCarried = pTargetId.getCarriedActor() != 0;/*((((target) && (target.parentSprite))) && ((target.parentSprite.carriedEntity == target)));*/

        GameFightFighterInformations targetInfos = (GameFightFighterInformations) pTargetId.getGameContextActorInformations(null);
        GameFightMonsterInformations monsterInfo = pTargetId.getGameContextActorInformations(null) instanceof GameFightMonsterInformations ? (GameFightMonsterInformations) pTargetId.getGameContextActorInformations(null) : null;
        boolean isTargetAlly = pCasterId.isFriendlyWith(pTargetId);
        
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
            if (((((targetIsCarried) && (!((pEffect.getZoneShape() == SpellShapeEnum.A))))) && (!((pEffect.getZoneShape() == SpellShapeEnum.a))))) {
                return (true);
            };
            if (((((targetInfos.stats.summoned) && (monsterInfo != null))) && (!(DAO.getMonsters().find(monsterInfo.creatureGenericId).isCanPlay())))) {
                targetMaskPattern = ((isTargetAlly) ? "agsj" : "ASJ");
            } else {
                if (targetInfos.stats.summoned) {
                    targetMaskPattern = ((isTargetAlly) ? "agij" : "AIJ");
                } else {
                    if ((pTargetId.getGameContextActorInformations(null) instanceof GameFightCompanionInformations)) {
                        targetMaskPattern = ((isTargetAlly) ? "agdl" : "ADL");
                    } else {
                        if ((pTargetId.getGameContextActorInformations(null) instanceof GameFightMonsterInformations)) {
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
                            verify = !pCasterId.hasState(maskState);
                        } else {
                            verify = !pTargetId.hasState(maskState);
                        }
                        break;
                    case 'E':
                        maskState = Integer.parseInt(exclusiveMaskParam);
                        if (exclusiveMaskCasterOnly) {
                            verify = pCasterId.hasState(maskState);
                        } else {
                            verify = pTargetId.hasState(maskState);
                        }
                        break;
                    case 'f':
                        verify = !((monsterInfo != null) && ((pEffect.targetMask.contains("f" + monsterInfo.creatureGenericId))));

                        break;
                    case 'F':
                        verify = ((monsterInfo != null) && ((pEffect.targetMask.contains("F" + monsterInfo.creatureGenericId))));

                        /*if (verify && pTargetId instanceof BombFighter) { //TEmpororaire = bug
                         return true;
                         }*/
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
                        verify = ((!((pTriggeringSpellCasterId == 0))) && ((pTargetId.getID() == pTriggeringSpellCasterId)));
                        break;
                    case 'p':
                        verify = !pTargetId.hasSummoner();
                        break;
                    case 'P':
                        /*if (pTargetId instanceof BombFighter || t) { //TEmpororaire = bug
                            verify = true;
                        }*/
                        verify = pTargetId.hasSummoner();
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

    private synchronized void parseZone() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        String[] params;
        boolean hasMinSize;

        if (((this.rawZone != null) && (!this.rawZone.isEmpty()))) {
            this.zoneShape = this.rawZone.charAt(0);
            params = this.rawZone.substring(1).split(",");
            hasMinSize = (((((((((this.getZoneShape() == SpellShapeEnum.C)) || ((this.getZoneShape() == SpellShapeEnum.X)))) || ((this.getZoneShape() == SpellShapeEnum.Q)))) || ((this.getZoneShape() == SpellShapeEnum.plus)))) || ((this.getZoneShape() == SpellShapeEnum.sharp)));
            if (this.rawZone.substring(1).contains(",")) {
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
                try {
                    this.zoneSize = Integer.parseInt(params[0]);
                } catch (Exception e) {
                }
            }
        } else {
            logger.error("Zone incorrect ({})",this.rawZone);
        };
        if (this.zoneMinSize >= 63) {
            this.zoneMinSize = 63;
        }
        if (this.zoneSize >= 63) {
            this.zoneSize = 63;
        }
        if (zoneShape == -100000) {
            this.zoneShape = 0;
        }
        if (zoneSize == -100000) {
            this.zoneSize = 0;
        }
        if (zoneMinSize == -100000) {
            this.zoneMinSize = 0;
        }
        if (zoneEfficiencyPercent == -100000) {
            this.zoneEfficiencyPercent = 0;
        }
        if (zoneMaxEfficiency == -100000) {
            this.zoneMaxEfficiency = 0;
        }
        this.initialized = true;
    }

    public Effect getTemplate() {
        return DAO.getD2oTemplates().getEffect(this.effectId);
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
