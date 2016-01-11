from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage


class KickCommand(PlayerCommand):

    def getDescription(self):
        return "Expel the player arg1 from the game";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[0]);
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            target.getClient().timeOut();
            client.send(ConsoleMessage(0, "The target has been expelled"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 1;
