package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.character.SpellBook;
import koh.game.entities.actors.character.shortcut.ItemShortcut;
import koh.game.entities.actors.character.shortcut.PresetShortcut;
import koh.game.entities.spells.SpellLevel;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellForgottenMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellUpgradeFailureMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellUpgradeRequestMessage;
import koh.protocol.messages.game.context.roleplay.spell.ValidateSpellForgetMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarAddRequestMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRefreshMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRemoveRequestMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRemovedMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarSwapRequestMessage;
import koh.protocol.types.game.shortcut.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ShortcutHandler {

    private static final Logger logger = LogManager.getLogger(ShortcutHandler.class);

    @HandlerAttribute(ID = 1700)
    public static void handleValidateSpellForgetMessage(WorldClient client, ValidateSpellForgetMessage message){
        if(client.isGameAction(GameActionTypeEnum.SPELL_UI)){
            return;
        }
        SpellBook.SpellInfo spell =  client.getCharacter().getMySpells().getSpelInfo(message.spellId);
        if(spell == null || spell.level <= 1){
            client.send(new SpellUpgradeFailureMessage());
            return;
        }
        int point;
        switch(spell.level){
            case 3:
                point = 3;
                break;
            case 4:
                point = 6;
                break;
            case 5:
                point = 10;
                break;
            case 6:
                point = 15;
                break;
            case 2:
            default:
                point = 1;
                break;
        }
        client.send(new SpellForgottenMessage(new int[] {spell.id},point));
        client.getCharacter().setSpellPoints(client.getCharacter().getSpellPoints() + point);
        client.getCharacter().refreshStats(true, false);

    }

    @HandlerAttribute(ID = SpellUpgradeRequestMessage.MESSAGE_ID)
    public static void HandleSpellUpgradeRequestMessage(WorldClient Client, SpellUpgradeRequestMessage Message) {

        //IsInFight
        Client.getCharacter().getMySpells().boostSpell(Client, Message.spellId, Message.spellLevel);
        Client.getCharacter().refreshStats();
    }

    @HandlerAttribute(ID = ShortcutBarRemoveRequestMessage.MESSAGE_ID)
    public static void HandleShortcutBarRemoveRequestMessage(WorldClient Client, ShortcutBarRemoveRequestMessage Message) {
        switch (Message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                Client.getCharacter().getMySpells().removeSpellSlot(Client , Message.slot);
                break;
            default:
                if (Client.getCharacter().getShortcuts().myShortcuts.containsKey(Message.slot)) {
                    Client.getCharacter().getShortcuts().myShortcuts.remove(Message.slot);
                    Client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Message.slot));
                } else {
                    //Todo ShortcutErrorMessage
                    Client.send(new BasicNoOperationMessage());
                }

                break;
        }
    }

    @HandlerAttribute(ID = ShortcutBarSwapRequestMessage.MESSAGE_ID)
    public static void HandleShortcutBarSwapRequestMessage(WorldClient Client, ShortcutBarSwapRequestMessage Message) {
        switch (Message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                Client.getCharacter().getMySpells().swapShortcuts(Client, Message.firstSlot, Message.secondSlot);
                break;
            default:
                Client.getCharacter().getShortcuts().swapShortcuts(Client, Message.firstSlot, Message.secondSlot);
                break;
        }
    }

    @HandlerAttribute(ID = 6225)
    public static void HandleShortcutBarAddRequestMessage(WorldClient client, ShortcutBarAddRequestMessage message) {
        switch (message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                if (!(message.shortcut instanceof ShortcutSpell)) {
                    logger.error("Trying to parse SpellShortcut with {}" , message.shortcut.getTypeId());
                    client.send(new BasicNoOperationMessage());
                    break;
                }
                client.getCharacter().getMySpells().moveSpell(client, ((ShortcutSpell) message.shortcut).spellId, ((ShortcutSpell) message.shortcut).Slot);
                break;
            case ShortcutBarEnum.GENERAL_SHORTCUT_BAR:
                if (!(message.shortcut instanceof ShortcutObjectItem) && !(message.shortcut instanceof ShortcutObjectPreset)) {
                    logger.error("Trying to parse SpellShortcut with  {}" , message.shortcut.getTypeId());
                    client.send(new BasicNoOperationMessage());
                    break;
                }
                if (message.shortcut instanceof ShortcutObjectItem && !client.getCharacter().getShortcuts().canAddShortcutItem((ShortcutObjectItem) message.shortcut)) {
                    PlayerController.sendServerMessage(client, "Vous ne pouvez pas dupliquez le même item ^^' ...");
                    client.send(new BasicNoOperationMessage());
                    break;
                }
                if (client.getCharacter().getShortcuts().myShortcuts.containsKey(message.shortcut.Slot)) {
                    client.getCharacter().getShortcuts().myShortcuts.remove(message.shortcut.Slot);
                    client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, message.shortcut.Slot));
                }

                switch (message.shortcut.getTypeId()){
                    case 370:
                        if(!client.getCharacter().getPresets().contains(((ShortcutObjectPreset) message.shortcut).presetId)){
                            return;
                        }
                        client.getCharacter().getShortcuts().add(new PresetShortcut(message.shortcut.Slot, ((ShortcutObjectPreset) message.shortcut).presetId));
                        break;
                    case 371:
                        client.getCharacter().getShortcuts().add(new ItemShortcut(message.shortcut.Slot, ((ShortcutObjectItem) message.shortcut).itemUID));
                        break;
                    default:
                        client.send(new BasicNoOperationMessage());
                        break;
                }
                client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, client.getCharacter().getShortcuts().myShortcuts.get(message.shortcut.Slot).toShortcut(client.getCharacter())));

                break;
            default:
                client.send(new BasicNoOperationMessage());
                break;
        }
    }

}
