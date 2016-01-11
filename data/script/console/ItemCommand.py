from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.protocol.client.enums import EffectGenerationType
from koh.protocol.client.enums import ItemSuperTypeEnum
from koh.game.dao import DAO
from koh.game.entities.item import InventoryItem
from koh.game.entities.item import EffectHelper
from java.lang import String
from koh.protocol.messages.authorized import ConsoleMessage


class ItemCommand(PlayerCommand):

    def getDescription(self):
        return "Add item arg1 quantity arg2 type arg3 to me;

    def apply(self,client,args):
        id = int(args[0]);
        qua = int(args[1]);
        if args[2] == "max":
            type = EffectGenerationType.MAX_EFFECTS;
        elif args[2] == "min":
            type = EffectGenerationType.MIN_EFFECTS;
        else:
            type = EffectGenerationType.NORMAL;

        template = DAO.getItemTemplates().getTemplate(id);
        if template is None:
            client.send(ConsoleMessage(0, "Inexistant item"));
            pass;
        if template.getSuperType() is ItemSuperTypeEnum.SUPERTYPE_PET :
            qua = 1;
        item = InventoryItem.getInstance(DAO.getItems().nextItemId(), id, 63, client.getCharacter().getID(), qua, EffectHelper.generateIntegerEffect(template.getPossibleEffects(), type, template.isWeapon()));
        if client.getCharacter().getInventoryCache().add(item, True) :
            item.setNeedInsert(True);

        client.send(ConsoleMessage(0, String.format("%s  added to your inventory with %s stats", template.getNameId(), type.toString())));


    def can(self,client):
        return True;

    def roleRestrained(self):
        return 4;

    def argsNeeded(self):
        return 3;
