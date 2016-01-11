from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage



class RemovePaddockItemCommand(PlayerCommand):

    def getDescription(self):
        return "Remove the cell arg1 item from the map's paddock";

    def apply(self,client,args):
        try:
            DAO.getPaddocks().find(client.getCharacter().getCurrentMap().getId()).removePaddockItem(int(args[0]));
            client.send(ConsoleMessage(0, "Item successfully removed"));
        except:
            client.send(ConsoleMessage(0, "Error"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 3;

    def argsNeeded(self):
        return 1;
