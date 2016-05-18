from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage
from koh.game.entities.actors import Player

class HonorCommand(PlayerCommand):

    def getDescription(self):
        return "AddHonor arg1 count arg2 target";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[1])
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"))
        else:
            target.addHonor(int(args[0]),True)

    def can(self,client):
        return True

    def roleRestrained(self):
        return 4

    def argsNeeded(self):
        return 2
