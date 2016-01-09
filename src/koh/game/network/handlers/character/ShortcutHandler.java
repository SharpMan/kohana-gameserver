package koh.game.network.handlers.character;

import koh.game.controllers.PlayerController;
import koh.game.entities.actors.character.ItemShortcut;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellUpgradeRequestMessage;
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
    public static void HandleShortcutBarAddRequestMessage(WorldClient Client, ShortcutBarAddRequestMessage message) {
        switch (message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                if (!(message.shortcut instanceof ShortcutSpell)) {
                    logger.error("Trying to parse SpellShortcut with {}" , message.shortcut.getTypeId());
                    Client.send(new BasicNoOperationMessage());
                    break;
                }
                Client.getCharacter().getMySpells().moveSpell(Client, ((ShortcutSpell) message.shortcut).spellId, ((ShortcutSpell) message.shortcut).Slot);
                break;
            case ShortcutBarEnum.GENERAL_SHORTCUT_BAR:
                if (!(message.shortcut instanceof ShortcutObjectItem)) {
                    logger.error("Trying to parse SpellShortcut with  {}" , message.shortcut.getTypeId());
                    Client.send(new BasicNoOperationMessage());
                    break;
                }
                if (!Client.getCharacter().getShortcuts().canAddShortcutItem((ShortcutObjectItem) message.shortcut)) {
                    PlayerController.sendServerMessage(Client, "Vous ne pouvez pas dupliquez le même item ^^' ...");
                    Client.send(new BasicNoOperationMessage());
                    break;
                }
                if (Client.getCharacter().getShortcuts().myShortcuts.containsKey(message.shortcut.Slot)) {
                    Client.getCharacter().getShortcuts().myShortcuts.remove(message.shortcut.Slot);
                    Client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, message.shortcut.Slot));
                }
                Client.getCharacter().getShortcuts().add(new ItemShortcut(message.shortcut.Slot, ((ShortcutObjectItem) message.shortcut).itemUID));
                Client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Client.getCharacter().getShortcuts().myShortcuts.get(message.shortcut.Slot).toShortcut(Client.getCharacter()))); //getshortcut slto

                break;
            default:
                Client.send(new BasicNoOperationMessage());
                break;
        }
    }

}
