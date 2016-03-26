from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum;
from koh.game.network import WorldClient;
from koh.utils import Enumerable
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage


class SaveCommand(PlayerCommand):

    def getDescription(self):
        return "Save the world";

    def apply(self,client,args):
        players = DAO.getPlayers().getPlayers()
        for player in players:
            player.save(False)
        client.send(ConsoleMessage(0, "Save ended" ));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 3;

    def argsNeeded(self):
        return 0;
