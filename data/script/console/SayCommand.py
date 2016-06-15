from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum;
from koh.game.controllers import PlayerController;
from koh.game.network import WorldClient;
from koh.utils import Enumerable;



class SayCommand(PlayerCommand):

    def getDescription(self):
        return "Say a sentence to everybody";

    def apply(self,client,args):
        PlayerController.sendServerMessage("<b>"+client.getCharacter().getNickName()+"</b> : "+args[0],"AC776A");

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;

    def argsNeeded(self):
        return 1;
