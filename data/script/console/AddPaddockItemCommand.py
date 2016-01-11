from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage
from koh.protocol.types.game.paddock import PaddockItem
from koh.protocol.types.game.mount import ItemDurability

class AddPaddockItemCommand(PlayerCommand):

    def getDescription(self):
        return "add the cell arg1 item arg2 durability arg3 durabilityMax arg4 to the map's paddock";

    def apply(self,client,args):
        try:
            cellid = int(args[0]);
            object = int(args[1]);
            durability = int(args[2]);
            durabilityMax = int(args[3]);
            DAO.getPaddocks().find(client.getCharacter().getCurrentMap().getId()).addPaddockItem(PaddockItem(cellid, object, ItemDurability(durability, durabilityMax)));
            client.send(ConsoleMessage(0, "Item successfully added"));
        except:
            client.send(ConsoleMessage(0, "Error"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 4;

    def argsNeeded(self):
        return 4;
