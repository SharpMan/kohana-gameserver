from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum;
from koh.game.controllers import PlayerController;
from koh.game.network import WorldClient;



class TestCommand(PlayerCommand):

    def getDescription(self):
        return None;

    def apply(self,client,args):
        PlayerController.sendServerMessage(client,"parfait");

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;
