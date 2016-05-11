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
    public static void handleGameActionFightCastOnTargetRequestMessage(WorldClient client, GameActionFightCastOnTargetRequestMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final SpellLevel spell = client.getCharacter().getMySpells().getSpellLevel(message.spellId);
        final Fighter fighter = client.getCharacter().getFight().getFighter(message.targetId);
        if (spell != null && fighter != null) {
            try {
                fighter.getSpellLock().lock();
                if (!fighter.isVisibleFor(client.getCharacter())) {
                    client.getCharacter().getFight().launchSpell(client.getCharacter().getFighter(), spell, fighter.getLastCellSeen(), true);
                    return;
                }
                client.getCharacter().getFight().launchSpell(client.getCharacter().getFighter(), spell, fighter.getCellId(), true);
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                fighter.getSpellLock().unlock();
            }
        }
    }

    @HandlerAttribute(ID = GameActionFightCastRequestMessage.M_ID)
    public static void handleGameActionFightCastRequestMessage(WorldClient client, GameActionFightCastRequestMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final SpellLevel Spell = client.getCharacter().getMySpells().getSpellLevel(message.spellId);

        // Sort existant ?
        if (Spell != null) {
            try {
                client.getCharacter().getFighter().getSpellLock().lock();
                client.getCharacter().getFight().launchSpell(client.getCharacter().getFighter(), Spell, message.cellId, true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                client.getCharacter().getFighter().getSpellLock().unlock();
            }
        }

    }

}
