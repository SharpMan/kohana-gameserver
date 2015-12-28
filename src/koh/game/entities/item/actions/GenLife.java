package koh.game.entities.item.actions;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.DelayedActionTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.delay.GameRolePlayDelayedActionFinishedMessage;
import koh.protocol.messages.game.context.roleplay.delay.GameRolePlayDelayedObjectUseMessage;
import koh.protocol.types.game.context.roleplay.HumanOptionObjectUse;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by Melancholia on 12/13/15.
 */
public class GenLife  extends ItemAction {

    private short min;
    private short max;

    public GenLife(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.min = Short.parseShort(args[0]);
        this.max = Short.parseShort(args[1]);
        if(max == 0) max = min;
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        final int val = EffectHelper.randomValue(min,max);
        final int copy = p.getLife() + val > p.getMaxLife() ? p.getMaxLife() - p.getLife() : val;

        p.getCurrentMap().sendToField(new GameRolePlayDelayedObjectUseMessage(p.getID(), DelayedActionTypeEnum.DELAYED_ACTION_OBJECT_USE, Instant.now().plusMillis(3500).toEpochMilli(), this.template));
        p.getHumanInformations().options = ArrayUtils.add(p.getHumanInformations().options, new HumanOptionObjectUse(DelayedActionTypeEnum.DELAYED_ACTION_OBJECT_USE, Instant.now().plusMillis(3500).toEpochMilli(), this.template));

        new CancellableScheduledRunnable(p.getCurrentMap().getArea().getBackGroundWorker(),3500){
            @Override
            public void run() {
                p.getCurrentMap().sendToField(new GameRolePlayDelayedActionFinishedMessage(p.getID(), DelayedActionTypeEnum.DELAYED_ACTION_OBJECT_USE));
                p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE,1, String.valueOf(copy)));
                p.addLife(copy);
                p.refreshStats();
                p.removeHumanOption(HumanOptionObjectUse.class);
            }
        };


        return true;
    }
}
