package koh.game.fights.fighters;

import java.util.ArrayList;
import java.util.Arrays;
import koh.game.dao.SpellDAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellableEffectMessage;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public abstract class StaticFighter extends Fighter {

    public MonsterGrade Grade;

    public StaticFighter(koh.game.fights.Fight Fight, Fighter Summoner) {
        super(Fight, Summoner);
    }

    @Override
    public int MaxAP() {
        return 0;
    }

    @Override
    public int MaxMP() {
        return 0;
    }

    @Override
    public int AP() {
        return 0;

    }

    @Override
    public int MP() {
        return 0;
    }

    private boolean firstTurn = true;

    public void onBeginTurn() {
        if (firstTurn) {
            this.Fight.AffectSpellTo(this, this, this.Grade.Grade, this.Grade.Monster().spells);
            this.firstTurn = false;
        }
    }

    @Override
    public int compareTo(IFightObject obj) {
        return Priority().compareTo(obj.Priority());
    }

    @Override
    public FightObjectType ObjectType() {
        return FightObjectType.OBJECT_STATIC;
    }

}
