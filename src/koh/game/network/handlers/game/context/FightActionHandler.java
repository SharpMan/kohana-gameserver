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
    public static void HandleGameActionFightCastOnTargetRequestMessage(WorldClient client, GameActionFightCastOnTargetRequestMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final SpellLevel spell = client.getCharacter().getMySpells().getSpellLevel(message.spellId);
        final Fighter fighter = client.getCharacter().getFight().getFighter(message.targetId);
        if (spell != null && fighter != null) {
            if(!fighter.isVisibleFor(client.getCharacter())){
                client.getCharacter().getFight().launchSpell(client.getCharacter().getFighter(), spell, fighter.getLastCellSeen(), true);
                return;
            }
            client.getCharacter().getFight().launchSpell(client.getCharacter().getFighter(), spell, fighter.getCellId(), true);
        }
    }

    @HandlerAttribute(ID = GameActionFightCastRequestMessage.M_ID)
    public static void HandleGameActionFightCastRequestMessage(WorldClient Client, GameActionFightCastRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        SpellLevel Spell = Client.getCharacter().getMySpells().getSpellLevel(Message.spellId);

        // Sort existant ?
        if (Spell != null) {
            Client.getCharacter().getFight().launchSpell(Client.getCharacter().getFighter(), Spell, Message.cellId, true);
        }

    }

}
