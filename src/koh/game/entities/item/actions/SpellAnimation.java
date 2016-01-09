package koh.game.entities.item.actions;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.DelayedActionTypeEnum;
import koh.protocol.messages.game.context.roleplay.delay.GameRolePlayDelayedActionFinishedMessage;
import koh.protocol.messages.game.context.roleplay.delay.GameRolePlayDelayedObjectUseMessage;
import koh.protocol.messages.game.context.roleplay.visual.GameRolePlaySpellAnimMessage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * Created by Melancholia on 12/25/15.
 */
public class SpellAnimation extends ItemAction {

    public int spellID;

    public SpellAnimation(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.spellID = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell))
            return false;
        p.getCurrentMap().sendToField(new GameRolePlayDelayedObjectUseMessage(p.getID(), DelayedActionTypeEnum.DELAYED_ACTION_OBJECT_USE, Instant.now().plus(2, ChronoUnit.SECONDS).toEpochMilli(), this.template));
        p.getCurrentMap().sendToField(new GameRolePlaySpellAnimMessage(p.getID(), cell, spellID, (byte)5));
        new CancellableScheduledRunnable(p.getCurrentMap().getArea().getBackGroundWorker(),2000){
            @Override
            public void run() {
                p.getCurrentMap().sendToField(new GameRolePlayDelayedActionFinishedMessage(p.getID(),DelayedActionTypeEnum.DELAYED_ACTION_OBJECT_USE));
            }
        };
        return true;
    }
}
