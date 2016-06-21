package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.SlaveFighter;
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
        final Fighter caster = client.getCharacter().getFight().getCurrentFighter() instanceof SlaveFighter
                && client.getCharacter().getFight().getCurrentFighter().getSummoner() == client.getCharacter().getFighter() ?
                client.getCharacter().getFight().getCurrentFighter() : client.getCharacter().getFighter();
        if (spell != null && fighter != null) {
            try {
                if (!fighter.isVisibleFor(client.getCharacter())) {
                    client.getCharacter().getFight().launchSpell(caster, spell, fighter.getLastCellSeen(), true);
                    return;
                }
                client.getCharacter().getFight().launchSpell(caster, spell, fighter.getCellId(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(caster != client.getCharacter().getFighter() & fighter != null){
            client.getCharacter().getFight().launchSpell(caster, caster.getSpells().stream().filter(s -> s.getSpellId() == message.spellId).findFirst().get(), fighter.getCellId(), true);
        }
    }

    @HandlerAttribute(ID = GameActionFightCastRequestMessage.M_ID)
    public static void handleGameActionFightCastRequestMessage(WorldClient client, GameActionFightCastRequestMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final Fighter caster = client.getCharacter().getFight().getCurrentFighter() instanceof SlaveFighter
                && client.getCharacter().getFight().getCurrentFighter().getSummoner() == client.getCharacter().getFighter() ?
                client.getCharacter().getFight().getCurrentFighter() : client.getCharacter().getFighter();

        final SpellLevel spell = client.getCharacter().getMySpells().getSpellLevel(message.spellId);

        // Sort existant ?
        if (spell != null) {
            try {
                client.getCharacter().getFight().launchSpell(caster, spell, message.cellId, true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(caster != client.getCharacter().getFighter()){
            client.getCharacter().getFight().launchSpell(caster, caster.getSpells().stream().filter(s -> s.getSpellId() == message.spellId).findFirst().get(), message.cellId, true);
        }

    }

}
