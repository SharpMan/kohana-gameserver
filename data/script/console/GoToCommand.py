from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage


class GoToCommand(PlayerCommand):

    def getDescription(self):
        return "Telpeort to the player arg1";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[0]);
        if target is None:
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            client.getCharacter().teleport(target.getMapid(),target.getCell().getId());

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;

    def argsNeeded(self):
        return 1;
