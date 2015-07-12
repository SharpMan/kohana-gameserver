package koh.game.network.handlers.character;

import koh.game.Main;
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

/**
 *
 * @author Neo-Craft
 */
public class ShortcutHandler {

    @HandlerAttribute(ID = SpellUpgradeRequestMessage.MESSAGE_ID)
    public static void HandleSpellUpgradeRequestMessage(WorldClient Client, SpellUpgradeRequestMessage Message) {

        //IsInFight
        Client.Character.mySpells.BoostSpell(Client, Message.spellId, Message.spellLevel);
        Client.Character.RefreshStats();
    }

    @HandlerAttribute(ID = ShortcutBarRemoveRequestMessage.MESSAGE_ID)
    public static void HandleShortcutBarRemoveRequestMessage(WorldClient Client, ShortcutBarRemoveRequestMessage Message) {
        switch (Message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                Client.Character.mySpells.RemoveSpellSlot(Client , Message.slot);
                break;
            default:
                if (Client.Character.Shortcuts.myShortcuts.containsKey(Message.slot)) {
                    Client.Character.Shortcuts.myShortcuts.remove(Message.slot);
                    Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Message.slot));
                } else {
                    //Todo ShortcutErrorMessage
                    Client.Send(new BasicNoOperationMessage());
                }

                break;
        }
    }

    @HandlerAttribute(ID = ShortcutBarSwapRequestMessage.MESSAGE_ID)
    public static void HandleShortcutBarSwapRequestMessage(WorldClient Client, ShortcutBarSwapRequestMessage Message) {
        switch (Message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                Client.Character.mySpells.SwapShortcuts(Client, Message.firstSlot, Message.secondSlot);
                break;
            default:
                Client.Character.Shortcuts.SwapShortcuts(Client, Message.firstSlot, Message.secondSlot);
                break;
        }
    }

    @HandlerAttribute(ID = 6225)
    public static void HandleShortcutBarAddRequestMessage(WorldClient Client, ShortcutBarAddRequestMessage Message) {
        switch (Message.barType) {
            case ShortcutBarEnum.SPELL_SHORTCUT_BAR:
                if (!(Message.shortcut instanceof ShortcutSpell)) {
                    Main.Logs().writeError("Trying to parse SpellShortcut with " + Message.shortcut.getTypeId());
                    Client.Send(new BasicNoOperationMessage());
                    break;
                }
                Client.Character.mySpells.MoveSpell(Client, ((ShortcutSpell) Message.shortcut).spellId, ((ShortcutSpell) Message.shortcut).Slot);
                break;
            case ShortcutBarEnum.GENERAL_SHORTCUT_BAR:
                if (!(Message.shortcut instanceof ShortcutObjectItem)) {
                    Main.Logs().writeError("Trying to parse SpellShortcut with " + Message.shortcut.getTypeId());
                    Client.Send(new BasicNoOperationMessage());
                    break;
                }
                if (!Client.Character.Shortcuts.CanAddShortcutItem((ShortcutObjectItem) Message.shortcut)) {
                    PlayerController.SendServerMessage(Client, "Vous ne pouvez pas dupliquez le même item ^^' ...");
                    Client.Send(new BasicNoOperationMessage());
                    break;
                }
                if (Client.Character.Shortcuts.myShortcuts.containsKey(Message.shortcut.Slot)) {
                    Client.Character.Shortcuts.myShortcuts.remove(Message.shortcut.Slot);
                    Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Message.shortcut.Slot));
                }
                Client.Character.Shortcuts.Add(new ItemShortcut(Message.shortcut.Slot, ((ShortcutObjectItem) Message.shortcut).itemUID));
                Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, Client.Character.Shortcuts.myShortcuts.get(Message.shortcut.Slot).toShortcut(Client.Character))); //getshortcut slto

                break;
            default:
                Client.Send(new BasicNoOperationMessage());
                break;
        }
    }

}
