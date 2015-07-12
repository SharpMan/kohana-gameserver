package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightCastOnTargetRequestMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightCastRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightActionHandler {

    @HandlerAttribute(ID = GameActionFightCastOnTargetRequestMessage.M_ID)
    public static void HandleGameActionFightCastOnTargetRequestMessage(WorldClient Client, GameActionFightCastOnTargetRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        SpellLevel Spell = Client.Character.mySpells.GetSpellLevel(Message.spellId);
        Fighter Fighter = Client.Character.GetFight().GetFighter(Message.targetId);
        if (Spell != null && Fighter != null) {
            Client.Character.GetFight().LaunchSpell(Client.Character.GetFighter(), Spell, Fighter.CellId(), false);
        }
    }

    @HandlerAttribute(ID = GameActionFightCastRequestMessage.M_ID)
    public static void HandleGameActionFightCastRequestMessage(WorldClient Client, GameActionFightCastRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        SpellLevel Spell = Client.Character.mySpells.GetSpellLevel(Message.spellId);

        // Sort existant ?
        if (Spell != null) {
            Client.Character.GetFight().LaunchSpell(Client.Character.GetFighter(), Spell, Message.cellId, false);
        }

    }

}
